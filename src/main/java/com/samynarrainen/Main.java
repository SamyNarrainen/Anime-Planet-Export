package com.samynarrainen;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static final String WATCHED = "1", WATCHING = "2", DROPPED = "3", WANT_TO_WATCH = "4", STALLED = "5", NONE = "-1";
    public static final List<Entry> entries = new ArrayList<Entry>();
    public static final List<Entry> problems = new ArrayList<Entry>();

    public static final boolean STRICT = true;


    public static void main(String[] args) throws Exception {

        int pages = 1;
        final String USER_AP = args[0];
        final String authentication = DatatypeConverter.printBase64Binary((args[1] + ':' + args[2]).getBytes());

        String contents = getPageContents(new URL("http://www.anime-planet.com/users/" + USER_AP + "/anime?sort=title&page=1"));

        //Look for more pages...
        String regexPage = "'pagination aligncenter'.*(page=(.)).*class='next'>";
        Pattern patternPage = Pattern.compile(regexPage);
        Matcher matcherPage = patternPage.matcher(contents);
        if(matcherPage.find()) {
            pages = Integer.parseInt(matcherPage.group(2)) + 1; //TODO the page count given is 1 less, so add 1. Find out why.
        }

        entries.addAll(getEntries(contents));

        //Already searched page 1, so start on 2.
        for(int i = 2; i < pages; i++) {
            String pageContents = getPageContents(new URL("http://www.anime-planet.com/users/" + USER_AP + "/anime?sort=title&page=" + i));
            entries.addAll(getEntries(pageContents));
        }

        for(Entry e : entries) {
            try {
                processEntry(e, entries, authentication);
            } catch(SocketTimeoutException ex) {
                System.out.println("Timed out connecting using \"" + e.name + "\"");
            }
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
            System.out.println("Couldn't find match for \"" + e.name + "\" on first attempt.");
            for(int i = 0; i < e.altTitles.size(); i++) {
                result = searchForId(e.altTitles.get(i), entries, authentication);
                if(result.id != -1) {
                    e.id = result.id;
                    e.perfectMatch = result.perfectMatch;
                    System.out.println("Found Match for \"" + e.name + "\" on attempt " + i + 2);
                    break;
                } else {
                    problems.add(e);
                    System.out.println("Couldn't find match for \"" + e.name + "\" on attempt " + i + 2);
                }
            }
        } else {
            e.id = result.id;
            e.perfectMatch = result.perfectMatch;
            System.out.println("Found Match for \"" + e.name + "\" first try");
        }

        System.out.println(e + "\n");
    }

    public static Result searchForId(String name, List<Entry> entries, String authentication) throws Exception {
        Result result = Main.getMALID(name);
        if(result.id != -1) {
            for(Entry e : entries) {
                if (e.id == result.id) {
                    System.out.println("Warning: conflict with \"" + name + "\" with \"" + e.name + "\" trying search API...");
                    //The id from website search produced a conflict ID, attempt the search API.
                    result = getMALIDAPI(name, authentication);

                    //No match with the search API.
                    if (result.id == -1) {
                        //Remove e as there could also be a problem with this entry?
                        if (!e.perfectMatch) {
                            e.id = -1;
                            problems.add(e);
                            System.out.println("Error: \"" + name + "\" involved in a search conflict with \"" + e.name + "\"");
                        }
                        return result;
                    } else {
                        //Found a new ID with the search API, but it could be a conflict with another.
                        for (Entry e2 : entries) {
                            if (e2.id == result.id) {
                                if (!e2.perfectMatch) {
                                    e2.id = -1;
                                    problems.add(e2);
                                    System.out.println("Error: \"" + e2.name + "\" involved in a API search conflict with \"" + name + "\"");
                                }
                                result.id = -1;
                                break;
                            }
                        }
                    }
                }
            }
        } else {
            System.out.println("No search result for \"" + name + "\", trying API search...");
            result = getMALIDAPI(name, authentication);
            if(result.id != -1) {
                for(Entry e2 : entries) {
                    if(e2.id == result.id) {
                        if(!e2.perfectMatch) {
                            e2.id = -1;
                            problems.add(e2);
                            System.out.println("Error: \"" + e2.name + "\" involved in a API search conflict with \"" + name + "\"");
                        }
                        result.id = -1;
                        break;
                    }
                }
            }
        }
        Thread.sleep(1000);
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
        String regexRating = "statusArea.*?Rating'>(.*?)<";
        Pattern patternRating = Pattern.compile(regexRating);

        //GROUP 1: Episodes
        String regexEpisodes = "statusArea\"><span class='status[2|3|5]'></span>(.*?)eps";
        Pattern patternEpisodes = Pattern.compile(regexEpisodes);

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

    public static Result getMALID(String name) throws IOException, URISyntaxException {
        String contents = getPageContents(new URL("https://myanimelist.net/search/all?q=" + name.replace(" ", "%20")));

        //Group 1: ID
        //Group 2: Name
        //String regexId = "href=\"https://myanimelist.net/anime/(\\d*?)/.*?fw-b fl-l.*?#revInfo\\d*?\">(.*?)<";
        String regexId = "myanimelist.net/anime/(\\d*?)/.*?hoverinfo_trigger*.?fw-b fl-l.*?#revInfo\\d*?\">(.*?)<.*?picSurround di-tc thumb\">";
        //String regexId = "myanimelist.net/anime/(\\d*)/\\w*?\"\\sclass=\"hoverinfo_trigger\"*.?fw-b fl-l.*?#revInfo\\d*?\">(.*?)<";
        //String regexId = "<h2 id=\"anime\">Anime</h2>.*?href=\"https://myanimelist.net/anime/(.*?)/";
        Pattern patternId = Pattern.compile(regexId);

        Matcher matcherId = patternId.matcher(contents);
        int idFirst = -1; //The first ID found, not necessarily the best though.
        String nameFirst = "";

        //Look through the entries for one that matches the given name perfectly.
        //Else use the first result.
        while(matcherId.find()) {

            if(name.equals(matcherId.group(2))) {
                return new Result(Integer.parseInt(matcherId.group(1)), true);
            } else {
                int distance = StringUtils.getLevenshteinDistance(name, matcherId.group(2));
                if(distance < 3) {
                    System.out.println("Lavenshtein Distance < 3 for \"" + name + "\" and \"" + matcherId.group(2) + "\"");
                    return new Result(Integer.parseInt(matcherId.group(1)), true); //TODO should this really be a perfect match?
                }
            }

            if(!STRICT) {
                //If strict only accept perfectly matching results.
                if(idFirst == -1) {
                    idFirst = Integer.parseInt(matcherId.group(1));
                    nameFirst = matcherId.group(2);
                }
            }
        }

        if(idFirst != -1) {
            if(!name.equals(nameFirst)) {
                System.out.println("Warning: \"" + name + "\" matched with \"" + nameFirst + "\"");
            }
            return new Result(idFirst, false);
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
        httpURLConnection.setConnectTimeout(1000);
        httpURLConnection.setReadTimeout(1000);
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

        while(matcher.find()) {
            if(matcher.group(2).equals(name) || matcher.group(4).contains(name) || matcher.group(3).equals(name)) {
                return new Result(Integer.parseInt(matcher.group(1)), true);
            } else {
                List<String> names = new ArrayList<String>();
                names.addAll(Arrays.asList((matcher.group(4).split(";"))));
                names.add(matcher.group(2));
                names.add(matcher.group(3));
                for(String s : names) {
                    int distance = StringUtils.getLevenshteinDistance(name, s);
                    if(distance < 3) {
                        System.out.println("Lavenshtein Distance < 3 for \"" + name + "\" and \"" + s + "\"");
                        return new Result(Integer.parseInt(matcher.group(1)), true);
                    }
                }
            }
        }
        System.out.println("Warning: search API couldn't find match for \"" + name + "\"");
        return new Result();
    }
}
