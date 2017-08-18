package com.samynarrainen;

import com.samynarrainen.Data.FeedResult;
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
     * IDs used by AP for status.
     */
    public static final String NONE = "-1", WATCHED = "1", WATCHING = "2", DROPPED = "3", WANT_TO_WATCH = "4", STALLED = "5";

    /**
     * Collection of anime entries that were scraped from the AP account.
     */
    //TODO split entries across status to improve performance?
    public static final List<Entry> entries = new ArrayList<Entry>();

    /**
     * Anime entries that encountered problems in the conversion process. E.g couldn't be found on MAL.
     */
    public static final List<Entry> problems = new ArrayList<Entry>();

    /**
     * Whether case is considered when comparing names of anime entries.
     */
    public static final boolean IGNORE_CASE = true;

    /**
     * The maximum exclusive Levenshtein Distance that is considered when comparing names.
     */
    public static final int LAVEN_DIST = 2;

    /**
     * To prevent the execution of code only necessary during testing/development.
     */
    public static final boolean VERBOS = true;

    public static void main(String[] args) throws Exception {
        int pages = 1;
        final String USERNAME_AP = args[0];
        final String USERNAME_MAL = args[1];
        //Authentication for MAL API
        final String authentication = DatatypeConverter.printBase64Binary((USERNAME_MAL + ':' + args[2]).getBytes());

        String contents = getPageContents(new URL("http://www.anime-planet.com/users/" + USERNAME_AP + "/anime?sort=title&page=1"));
        //Look for more pages...
        String regexPages = "\"pagination aligncenter\".*(page=(.)).*class=\"next\">";
        Matcher matcherPages = Pattern.compile(regexPages).matcher(contents);
        if(matcherPages.find()) {
            pages = Integer.parseInt(matcherPages.group(2)) + 1; //TODO the page count given is 1 less, so add 1. Find out why.
        }

        entries.addAll(getEntries(contents));

        //Already searched page 1, so start on 2.
        for(int i = 2; i < pages; i++) {
            String pageContents = getPageContents(new URL("http://www.anime-planet.com/users/" + USERNAME_AP + "/anime?sort=title&page=" + i));
            entries.addAll(getEntries(pageContents));
        }

        //TESTING
        List<FeedResult> feed = AnimePlanetManager.exportFeed(USERNAME_AP);
        AnimePlanetManager.calculateDates(feed, entries);
        if(true) return;
        //TESTING END

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

        for(Entry e : problems) {
            System.out.println("PROBLEM: " + e);
        }

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
                problems.add(e);
                if(VERBOS) System.out.println("Couldn't find match for \"" + e.name);
            }
        } else {
            e.id = result.id;
            e.perfectMatch = result.perfectMatch;
            if(VERBOS) System.out.println("Found Match for \"" + e.name + "\" first try");
        }

        System.out.println((e.id == -1 ? "COULDN'T FIND: " : "FOUND: ") + e);
    }

    public static Result searchForId(String name, List<Entry> entries, String authentication) throws Exception {
        Result result = Main.getMALID(name);
        if(result.id != -1) {
            for(Entry e : entries) {
                if (e.id == result.id) {
                    if(VERBOS) System.out.println("Warning: conflict with \"" + name + "\" with \"" + e.name + "\" trying search API...");
                    //The id from website search produced a conflict ID, attempt the search API.
                    result = getMALIDAPI(name, authentication);

                    //No match with the search API.
                    if (result.id == -1) {
                        //TODO Remove e as there could also be a problem with this entry?
                        if (!e.perfectMatch) {
                            e.id = -1;
                            problems.add(e);
                            if(VERBOS) System.out.println("Error: \"" + name + "\" involved in a search conflict with \"" + e.name + "\"");
                        }
                        return result;
                    } else {
                        //Found a new ID with the search API, but it could be a conflict with another.
                        for (Entry e2 : entries) {
                            if (e2.id == result.id) {
                                if (!e2.perfectMatch) {
                                    e2.id = -1;
                                    problems.add(e2);
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
            result = getMALIDAPI(name, authentication);
            if(result.id != -1) {
                for(Entry e2 : entries) {
                    if(e2.id == result.id) {
                        if(!e2.perfectMatch) {
                            e2.id = -1;
                            problems.add(e2);
                            if(VERBOS) System.out.println("Error: \"" + e2.name + "\" involved in a API search conflict with \"" + name + "\"");
                        }
                        result.id = -1;
                        break;
                    }
                }
            }
        }
        //Otherwise requests are sent too quickly.
        Thread.sleep(500);
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
     * Returns the anime entries contained within an anime-planet page source.
     * @param contents
     * @return
     * @throws IOException
     */
    public static List<Entry> getEntries(String contents) throws IOException {
        //GROUP 0: anime entry HTML
        //GROUP 1: Name
        String regex = "<a title=\"<h5>(.*?)</h5>.*?class=\"card pure-1-6";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(contents);

        //GROUP 1: Alt Title (
        String regexAltTitle = "Alt title: (.*?)<";
        Pattern patternAltTitle = Pattern.compile(regexAltTitle);

        String regexAltTitles = "Alt titles: (.*?)<";
        Pattern patternAltTitles = Pattern.compile(regexAltTitles);

        //GROUP 1: Status
        String regexStatus = "statusArea.*?status(.)";
        Pattern patternStatus = Pattern.compile(regexStatus);

        //GROUP 1: Rating
        String regexRating = "statusArea.*?Rating\">(.*?)<";
        Pattern patternRating = Pattern.compile(regexRating);

        //GROUP 1: Episodes
        String regexEpisodes = "statusArea\"><span class=\"status[2|3|5]\"></span>(.*?)eps";
        Pattern patternEpisodes = Pattern.compile(regexEpisodes);

        //GROUP 1: URL
        String regexURL = "href=\"/anime/(.*?)\"";
        Pattern patternURL = Pattern.compile(regexURL);

        //For a watched series, the episodes aren't shown where they usually are, so grab them from the anime information section.
        //GROUP 1: Episodes
        String regexEpisodesWatched = "(\\d*?)\\sep.*?\\)";
        Pattern patternEpisodesWatched = Pattern.compile(regexEpisodesWatched);

        List<Entry> entries = new ArrayList<Entry>();

        while(matcher.find()) {
            Matcher matcherStatus = patternStatus.matcher(matcher.group(0));
            if(matcherStatus.find()) {

                Entry entry = new Entry();
                entry.name = matcher.group(1);
                entry.status = matcherStatus.group(1);

                Matcher matcherURL = patternURL.matcher(matcher.group(0));
                if(matcherURL.find()) {
                    entry.AnimePlanetURL = matcherURL.group(1);
                }

                Matcher matcherAltTitle = patternAltTitle.matcher(matcher.group(0));
                if(matcherAltTitle.find()) {
                    entry.altTitles.add(matcherAltTitle.group(1));
                } else {
                    Matcher matcherAltTitles = patternAltTitles.matcher(matcher.group(0));
                    if(matcherAltTitles.find()) {
                        //TODO what if the title itself contains a comma. That's why this is different from alttitle.
                        List<String> titles = Arrays.asList(matcherAltTitles.group(1).split(","));
                        entry.altTitles.addAll(titles);
                    }
                }

                if(entry.status.equals(WATCHED) || entry.status.equals(STALLED) || entry.status.equals(DROPPED) || entry.status.equals(WATCHING)) {
                    Matcher matcherRating = patternRating.matcher(matcher.group(0));
                    if(matcherRating.find()) {
                        entry.rating = Entry.convertRating(matcherRating.group(1));
                    }

                    if(entry.status.equals(WATCHED)) {
                        Matcher matcherEpisodesWatched = patternEpisodesWatched.matcher(matcher.group(0));
                        if(matcherEpisodesWatched.find()) {
                            entry.episodes = Integer.parseInt(matcherEpisodesWatched.group(1).replace(" ", ""));
                        }
                    } else {
                        Matcher matcherEpisodes = patternEpisodes.matcher(matcher.group(0));
                        if(matcherEpisodes.find()) {
                            entry.episodes = Integer.parseInt(matcherEpisodes.group(1).replace(" ", ""));
                        }
                    }
                }

                entries.add(entry);
            }

        }
        return entries;
    }

    /**
     * Compares the search results from the MAL website to find a matching name.
     * @param name
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public static Result getMALID(String name) throws IOException, URISyntaxException {
        String contents = getPageContents(new URL("https://myanimelist.net/search/all?q=" + name.replace(" ", "%20")));

        //Group 1: ID
        //Group 2: Title
        String regexId = "anime/(\\d*?)/.*?hoverinfo_trigger*.?fw-b fl-l.*?#revInfo\\d*?\">(.*?)<";
        Matcher matcherId = Pattern.compile(regexId).matcher(contents);

        if(IGNORE_CASE) {
            name = name.toLowerCase();
        }

        int shortestDistance = -1, shortestDistanceId = -1;

        while(matcherId.find()) {
            int id = Integer.parseInt(matcherId.group(1));
            String title = matcherId.group(2);

            if(IGNORE_CASE) {
                title = title.toLowerCase();
            }

            if(name.equals(title)) {
                return new Result(id, true);
            } else {
                //TODO check the actual page, sometimes there are differences between the API and the page names, AKA JoJo S2.
                int distance = StringUtils.getLevenshteinDistance(name, title);
                if(distance < LAVEN_DIST && (shortestDistance == -1 || distance < shortestDistance)) {
                    if(VERBOS) System.out.println("Distance change for " + name + " with " + title + " for distance " + distance);
                    shortestDistance = distance;
                    shortestDistanceId = id;
                }
            }
        }

        if(shortestDistanceId != -1) {
            if(VERBOS) System.out.println("Warning: matched " + name + " to " + shortestDistanceId + " with laven dist of " + shortestDistance);
            return new Result(shortestDistanceId, false);
        }

        return new Result(-1, false);
    }

    /**
     * Uses the MAL search API to receive an id.
     * @param name
     * @param authentication username:password in base64 binary
     * @return an ID only if the name matches perfectly.
     * @throws MalformedURLException
     * @throws IOException
     */
    public static Result getMALIDAPI(String name, String authentication) throws IOException {
        URL url = new URL("https://myanimelist.net/api/anime/search.xml?q=" + name.replace(" ", "%20"));
        String basicAuth = "Basic " + authentication;
        HttpURLConnection httpURLConnection = (HttpURLConnection)  url.openConnection();
        httpURLConnection.addRequestProperty("User-Agent", "Chrome");
        httpURLConnection.setRequestProperty("Authorization", basicAuth);
        BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

        //Load contents of site into single string.
        String inputLine, contents = "";
        while ((inputLine = in.readLine()) != null) {
            contents += inputLine;
        }
        in.close();

        //Group 1: ID
        //Group 2: Title
        //GROUP 3: English
        //Group 4: Synonyms
        String regex = "<id>(\\d*?)</id>.*?<title>(.*?)</title>.*?<english>(.*?)</english>.*?<synonyms>(.*?)</synonyms>";
        Pattern searchPattern = Pattern.compile(regex);
        Matcher matcher = searchPattern.matcher(contents);

        int shortestDistance = -1;
        int shortestDistanceId = -1;

        while(matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            String title = matcher.group(2);
            String english = matcher.group(3);
            String synonyms = matcher.group(4);

            if(IGNORE_CASE) {
                name = name.toLowerCase();
                title = title.toLowerCase();
                english = english.toLowerCase();
                synonyms = synonyms.toLowerCase();
            }

            if(title.equals(name) || synonyms.contains(name) || english.equals(name)) {
                return new Result(id, true);
            } else {
                List<String> names = new ArrayList<String>();
                names.addAll(Arrays.asList((synonyms.split(";"))));
                names.add(title);
                names.add(english);

                for(String s : names) {
                    int distance = StringUtils.getLevenshteinDistance(name, s);
                    if(distance < LAVEN_DIST && (shortestDistance == -1 || distance < shortestDistance)) {
                        shortestDistance = distance;
                        shortestDistanceId = id;
                    }
                }

                //Couldn't find a perfect match... Was there a close one?
                if(shortestDistanceId != -1) {
                    return new Result(shortestDistanceId, false);
                }
            }
        }

        if(VERBOS) System.out.println("Warning: search API couldn't find match for \"" + name + "\"");
        return new Result();
    }
}
