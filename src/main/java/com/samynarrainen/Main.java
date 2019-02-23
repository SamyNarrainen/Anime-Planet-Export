package com.samynarrainen;

import com.samynarrainen.Data.FeedResult;
import com.samynarrainen.domain.Entry;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    /**
     * The maximum exclusive Levenshtein Distance that is considered when comparing names.
     */
    public static final int LAVEN_DIST = 2;

    /**
     * To prevent the execution of code only necessary during testing/development.
     */
    public static final boolean VERBOS = true;

    /**
     * Whether or not output intended for a user is printed.
     */
    public static final boolean USER_OUTPUT = true;

    /**
     * args: AP username, MAL username, MAL password
     */
    public static void main(String[] args) throws Exception {
        final List<Entry> entries = new ArrayList<Entry>();
        final String USERNAME_AP = args[0];
        final String USERNAME_MAL = args[1];
        //Authentication for MAL API
        final String authentication = DatatypeConverter.printBase64Binary((USERNAME_MAL + ':' + args[2]).getBytes());
        if(USER_OUTPUT) System.out.println("Exporting " + USERNAME_AP + "'s anime-planet account...");

        try {
            if(USER_OUTPUT) System.out.println("Exporting " + USERNAME_AP + "'s anime-planet anime list...");
            entries.addAll(AnimePlanetManager.exportAnimeList(USERNAME_AP));
        } catch(Exception e) {
            if(USER_OUTPUT) System.out.println("Failed to export anime-planet list.");
            System.exit(1);
        }

        try {
            if(USER_OUTPUT) System.out.println("Exporting " + USERNAME_AP + "'s anime-planet feed...");
            List<FeedResult> feed = AnimePlanetManager.exportFeed(USERNAME_AP);
            AnimePlanetManager.calculateDates(feed, entries);
        } catch(Exception e) {
            if(USER_OUTPUT) System.out.println("Failed to export dates from anime-planet feed.");
            System.exit(1);
        }

        //Start up threads to handle the conversion process...
        ExecutorService executor = Executors.newFixedThreadPool(5);
        for(Entry e : entries) {
            Handler h = new Handler(e, entries, authentication);
            executor.execute(h);
        }
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Group all actual problems together when printing...
        for(Entry e : entries) {
            if(e.id == -1) {
                if(USER_OUTPUT) System.out.println("Couldn't find a match for: http://www.anime-planet.com/anime/" + e.AnimePlanetURL);
            }
        }

        //TODO export this to a file for the user to simply upload?
        for(Entry e : entries) {
            if(e.id != -1) {
                System.out.println(e.toXML());
            }
        }
    }

    public static void processEntry(Entry e, List<Entry> entries, String authentication) throws Exception {
        Result result = searchForId(e, entries, authentication);

        if(result.id == -1) {
            if(VERBOS) System.out.println("Couldn't find match for \"" + e.name + "\" on first attempt.");
        } else {
            e.id = result.id;
            e.perfectMatch = result.perfectMatch;

            //Double check that a perfect result can't be found...
            if(!result.perfectMatch) {
                Result result2 = MyAnimeListManager.getMALIDAPI(e, authentication);
                if(result.id != -1 && result2.perfectMatch && find(entries, result2.id) == null) {
                    if(VERBOS) System.out.println("Replacing " + result.id + " with perfect result " + result2.id + " for " + e.name);
                    e.id = result2.id;
                    e.perfectMatch = result2.perfectMatch;
                }
            }

            if(VERBOS) System.out.println("Found Match for \"" + e.name + "\" first try");
        }

        if(e.id != -1) {
            if(!e.perfectMatch) {
                int id = compareAdditionalInfo(e);
                if(id == -1 || id != e.id) {
                    //There wasn't a match using this data!
                    e.id = -1;
                    //TODO should results from this be forbidden from further inspection?
                    if(VERBOS) System.out.println("Warning: erased id " + e.id + " from imperfect result for " + e.name + " as additional info doesn't match.");
                }
            }

            if(e.id != -1) {
                if(USER_OUTPUT) System.out.println("Matched http://www.anime-planet.com/anime/" + e.AnimePlanetURL + " to https://myanimelist.net/anime/" + e.id);
            }
        } else {
            //Couldn't find a result... Try using additional information?
            int id = compareAdditionalInfo(e);
            if(id != -1) {
                //Need to double check this doesn't cause a conflict...
                Entry entry = find(entries, id);
                if(entry == null) {
                    //There's no conflict with this id, so it's safe to use!
                    e.id = id;
                    if(USER_OUTPUT) System.out.println("Matched http://www.anime-planet.com/anime/" + e.AnimePlanetURL + " to https://myanimelist.net/anime/" + e.id);
                } else {
                    if(VERBOS) System.out.println("Giving up on finding " + e.name);
                }
            }
        }
    }

    /**
     * Returns the Entry in the collection with the same id.
     * @param entries
     * @param id
     */
    private static Entry find(List<Entry> entries, int id) {
        if(id != -1) {
            for(Entry entry : entries) {
                if(entry.id == id) {
                    return entry;
                }
            }
        }
        return null;
    }

    public static Result searchForId(Entry entryIn, List<Entry> entries, String authentication) throws Exception {
        final String name = entryIn.name;
        final int sleepTime = 2000;
        Thread.sleep(sleepTime); //Otherwise requests are sent too quickly.
        Result result = MyAnimeListManager.getMALID(name);

        Entry entry = find(entries, result.id);
        if(entry != null) {
            if(VERBOS) System.out.println("Warning: " + name + "(" + result.id + ") conflicts with existing " + entry.name + "(" + entry.id + ")");
            //There's already an entry with this id...
            if(entry.perfectMatch) {
                //This entry is a perfect match, so don't erase it! Instead, try again...
                Thread.sleep(sleepTime);
                result = MyAnimeListManager.getMALIDAPI(entryIn, authentication);
                entry = find(entries, result.id);

                //There's another conflict...
                if(entry != null) {
                    if(!entry.perfectMatch) {
                        entry.id = -1;
                    }
                    //Since this anime was involved with a previous conflict, only accept it if it's perfect.
                    return result.perfectMatch ? result : new Result();
                } else {
                    return result;
                }
            } else {
                if(VERBOS) System.out.println("Warning: erasing id " + entry.id + " from " + entry.name);
                entry.id = -1;

                if(result.perfectMatch) {
                    return result;
                } else {
                    Thread.sleep(sleepTime);
                    result = MyAnimeListManager.getMALIDAPI(entryIn, authentication);
                    entry = find(entries, result.id);

                    //There's another conflict...
                    if(entry != null) {
                        if(!entry.perfectMatch) {
                            if(VERBOS) System.out.println("Warning: erasing id " + entry.id + " from " + entry.name);
                            entry.id = -1;
                        }
                        return result.perfectMatch ? result : new Result();
                    } else {
                        return result;
                    }
                }
            }
        } else {
            if(result.id == -1) {
                if(VERBOS) System.out.println("No result for \"" + name + "\", trying API...");
                Thread.sleep(sleepTime);
                result = MyAnimeListManager.getMALIDAPI(entryIn, authentication);

                entry = find(entries, result.id);
                if(entry != null) {
                    if(VERBOS) System.out.println("Warning: " + name + "(" + result.id + ") conflicts with existing " + entry.name + "(" + entry.id + ")");
                    //There's a conflict with this entry.
                    if(!entry.perfectMatch) {
                        if(VERBOS) System.out.println("Warning: erasing id " + entry.id + " from " + entry.name);
                        entry.id = -1;
                    }
                    return result.perfectMatch ? result : new Result();
                } else {
                    return result;
                }
            } else {
                return result;
            }
        }
    }

    /**
     * Reads the contents of the given URL and returns it as a String.
     * @param url
     * @return
     * @throws IOException
     */
    public static String getPageContents(URL url) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection)  url.openConnection();
        httpURLConnection.addRequestProperty("User-Agent", "Chrome");
        BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), StandardCharsets.UTF_8));

        //Load contents of site into single string.
        String inputLine, contents = "";
        while ((inputLine = in.readLine()) != null) {
            contents += inputLine;

            //Only read what's necessary...
            if(inputLine.contains("<h2 id=\"characters\">Characters</h2>")) {
                break;
            }
        }

        in.close();

        return contents;
    }

    /**
     * Uses the additional info of the entry and compares it against additional info found on MAL to find a match.
     * @param entry
     */
    public static int compareAdditionalInfo(Entry entry) throws IOException, InterruptedException {
        if(entry.year == -1) {
            //Additional information isn't set for this entry
            AnimePlanetManager.getAdditionalInfo(entry);
        }

        String contents = getPageContents(new URL("https://myanimelist.net/search/all?q=" + entry.name.replace(" ", "%20")));
        //Group 1: ID
        //Group 2: Title
        String regexId = "anime/(\\d*?)/.*?hoverinfo_trigger*.?fw-b fl-l.*?#revInfo\\d*?\">(.*?)<";
        Matcher matcherId = Pattern.compile(regexId).matcher(contents);

        while(matcherId.find()) {
            int id = Integer.parseInt(matcherId.group(1));

            Thread.sleep(500);
            Entry malEntry = MyAnimeListManager.getAdditionalInfo(id);

            if(VERBOS) System.out.println(entry.name + "," + entry.year + "," + entry.yearEnd + "," + entry.season + "," + entry.totalEpisodes + "," + entry.type + ",");
            if(VERBOS) System.out.println(entry.name + "," + malEntry.year + "," + malEntry.yearEnd + "," + malEntry.season + "," + malEntry.totalEpisodes + "," + malEntry.type + "\n");

            if(entry.year == malEntry.year
                    && entry.yearEnd == malEntry.yearEnd
                    && entry.season.equals(malEntry.season)
                    && entry.totalEpisodes == malEntry.totalEpisodes
                    && entry.type.equals(malEntry.type)) {

                for(String studio : entry.studios) {
                    for(String malStudio : malEntry.studios) {
                        if(malStudio.compareToIgnoreCase(studio) == 0) {
                            return id;
                        } else if(studio.compareToIgnoreCase("Studio Trigger") == 0) {
                            //Studio Trigger is known as Trigger on MAL.
                            if(malStudio.compareToIgnoreCase("Trigger") == 0) {
                                return id;
                            }
                        } else if(studio.compareToIgnoreCase("J.C. Staff") == 0) {
                            if(malStudio.compareToIgnoreCase("J.C.Staff") == 0) {
                                return id;
                            }
                        }
                    }
                }
                if(VERBOS) System.out.println("Studios don't match for " + entry.name);
            }
        }
        //Couldn't find a match
        return -1;
    }

    public static List<Entry> getEntries(final String s) {
        throw new RuntimeException("This function isn't supported");
    }
}
