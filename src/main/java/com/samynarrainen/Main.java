package com.samynarrainen;

import com.samynarrainen.Data.FeedResult;
import com.samynarrainen.Data.Status;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    /**
     * Collection of anime entries that were scraped from the AP account.
     */
    public static final List<Entry> entries = new ArrayList<Entry>();

    /**
     * The maximum exclusive Levenshtein Distance that is considered when comparing names.
     */
    public static final int LAVEN_DIST = 2;

    /**
     * To prevent the execution of code only necessary during testing/development.
     */
    public static final boolean VERBOS = false;

    /**
     * Whether or not output intended for a user is printed.
     */
    public static final boolean USER_OUTPUT = true;

    /**
     * args: AP username, MAL username, MAL password
     */
    public static void main(String[] args) throws Exception {
        final String USERNAME_AP = args[0];
        final String USERNAME_MAL = args[1];
        //Authentication for MAL API
        final String authentication = DatatypeConverter.printBase64Binary((USERNAME_MAL + ':' + args[2]).getBytes());
        if(USER_OUTPUT) System.out.println("Exporting " + USERNAME_AP + "'s anime-planet account...");

        try {
            entries.addAll(AnimePlanetManager.exportAnimeList(USERNAME_AP));
        } catch(Exception e) {
            if(USER_OUTPUT) System.out.println("Failed to export anime-planet list.");
            System.exit(1);
        }

        try {
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


        //Attempt to resolve entries that weren't successfully converted...
        for(Entry e : entries) {
            if(e.id == -1) {
                e.id = compareAdditionalInfo(e);
                if(e.id != -1) {
                    if(USER_OUTPUT) System.out.println("Matched http://www.anime-planet.com/anime/" + e.AnimePlanetURL + " to https://myanimelist.net/anime/" + e.id);
                }
            }
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
        Result result = searchForId(e.name, entries, authentication);

        if(result.id == -1) {
            if(VERBOS) System.out.println("Couldn't find match for \"" + e.name + "\" on first attempt.");
            for(int i = 0; i < e.altTitles.size(); i++) {
                result = searchForId(e.altTitles.get(i), entries, authentication);
                if(result.id != -1) {
                    e.id = result.id;
                    e.perfectMatch = result.perfectMatch;
                    if(VERBOS) System.out.println("Found Match for \"" + e.name + "\" on attempt " + i + 2);
                    break;
                }
            }
            //Looked through the alternative titles, but still couldn't find a match.
            if(result.id == -1) {
                if(VERBOS) System.out.println("Couldn't find match for \"" + e.name);
            }
        } else {
            e.id = result.id;
            e.perfectMatch = result.perfectMatch;
            if(VERBOS) System.out.println("Found Match for \"" + e.name + "\" first try");
        }

        if(e.id != -1) {
            if(USER_OUTPUT) System.out.println("Matched http://www.anime-planet.com/anime/" + e.AnimePlanetURL + " to https://myanimelist.net/anime/" + e.id);
        }
    }

    public static Result searchForId(String name, List<Entry> entries, String authentication) throws Exception {
        final int sleepTime = 2000;
        Thread.sleep(sleepTime); //Otherwise requests are sent too quickly.
        Result result = MyAnimeListManager.getMALID(name);
        if(result.id != -1) {
            for(Entry e : entries) {
                if (e.id == result.id) {
                    if(VERBOS) System.out.println("Warning: conflict with \"" + name + "\" with \"" + e.name + "\" trying search API...");
                    Thread.sleep(sleepTime); //Otherwise requests are sent too quickly.
                    //The id from website search produced a conflict ID, attempt the search API.
                    result = MyAnimeListManager.getMALIDAPI(name, authentication);

                    //No match with the search API.
                    if (result.id == -1) {
                        //TODO Remove e as there could also be a problem with this entry?
                        if (!e.perfectMatch) {
                            e.id = -1;
                            if(VERBOS) System.out.println("Error: \"" + name + "\" involved in a search conflict with \"" + e.name + "\"");
                        }
                        return result;
                    } else {
                        //Found a new ID with the search API, but it could be a conflict with another.
                        for (Entry e2 : entries) {
                            if (e2.id == result.id) {
                                if (!e2.perfectMatch) {
                                    e2.id = -1;
                                    if(VERBOS) System.out.println("Error: \"" + e2.name + "\" involved in a API search conflict with \"" + name + "\"");
                                }
                                result.id = -1;
                                break;
                            }
                        }
                    }
                }
            }
        } else {
            if(VERBOS) System.out.println("No search result for \"" + name + "\", trying API search...");
            Thread.sleep(sleepTime); //Otherwise requests are sent too quickly.
            result = MyAnimeListManager.getMALIDAPI(name, authentication);
            if(result.id != -1) {
                for(Entry e2 : entries) {
                    if(e2.id == result.id) {
                        if(!e2.perfectMatch) {
                            e2.id = -1;
                            if(VERBOS) System.out.println("Error: \"" + e2.name + "\" involved in a API search conflict with \"" + name + "\"");
                        }
                        result.id = -1;
                        break;
                    }
                }
            }
        }
        return result;
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
        BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

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

            //if(VERBOS) System.out.println(entry.name + "," + entry.year + "," + entry.yearEnd + "," + entry.season + "," + entry.totalEpisodes + "," + entry.type + ",");
            //if(VERBOS) System.out.println(entry.name + "," + malEntry.year + "," + malEntry.yearEnd + "," + malEntry.season + "," + malEntry.totalEpisodes + "," + malEntry.type + "\n");

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
                System.out.println("studios don't match.");
            }
        }
        //Couldn't find a match
        return -1;
    }
}
