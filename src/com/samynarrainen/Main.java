package com.samynarrainen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static final String WATCHED = "1", WATCHING = "2", DROPPED = "3", WANT_TO_WATCH = "4", STALLED = "5", NONE = "-1";

    public static void main(String[] args) throws Exception {
        int pages = 1;
        final String USER_AP = args[0];
        List<Entry> entries = new ArrayList<Entry>();
        List<Entry> problems = new ArrayList<Entry>();


        String contents = getPageContents(new URL("http://www.anime-planet.com/users/" + USER_AP + "/anime?sort=title&page=1"));

        //Look for more pages...
        String regexPage = "'pagination aligncenter'.*(page=(.)).*class='next'>";
        Pattern patternPage = Pattern.compile(regexPage);
        Matcher matcherPage = patternPage.matcher(contents);
        if(matcherPage.find()) {
            pages = Integer.parseInt(matcherPage.group(2)) + 1; //TODO the page count given is 1 less, so add 1. Find out why.
        }

        entries.addAll(getEntries(contents));


        for(int i = 2; i < pages; i++) {
            String pageContents = getPageContents(new URL("http://www.anime-planet.com/users/" + USER_AP + "/anime?sort=title&page=" + i));
            entries.addAll(getEntries(pageContents));
        }

        for(Entry e : entries) {
            int id = Main.getMALID(e.name);

            if (id == -1) {
                System.out.println("COULDN'T FIND ID FOR: " + e.name);
                problems.add(e);
            } else {
                //Check this entry doesn't match one that already exists.
                //If it does, the search has failed. TODO select the next entry?
                boolean exists = false;
                for(Entry e2 : entries) {
                    if(id == e2.id) {
                        System.out.println("ERROR: ID OF \"" + e2.name + "\" MATCHES \"" + e.name + "\"");
                        e2.id = -1;
                        problems.add(e);
                        problems.add(e2);
                        exists = true;
                        break;
                    }
                }
                if(!exists) {
                    e.id = id;
                }
            }

            Thread.sleep(1000); //Otherwise requests are sent too quickly.
        }

        for(Entry e : problems) {
            System.out.println("PROBLEM: " + e);
        }

        for(Entry e : entries) {
            System.out.println(e.toXML());
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

        //GROUP 1: Status
        String regexStatus = "statusArea.*?status(.)";
        Pattern patternStatus = Pattern.compile(regexStatus);

        //GROUP 1: Rating
        String regexRating = "statusArea.*?Rating'>(.*?)<";
        Pattern patternRating = Pattern.compile(regexRating);

        //GROUP 1: Episodes
        String regexEpisodes = "statusArea\"><span class='status[2|3]'></span>(.*?)eps";
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

    public static int getMALID(String name) throws IOException, URISyntaxException {
        String contents = getPageContents(new URL("https://myanimelist.net/search/all?q=" + name.replace(" ", "%20")));
        //System.out.println(contents);

        //Group 1: ID
        //Group 2: Name
        String regexId = "href=\"https://myanimelist.net/anime/(\\d*?)/.*?fw-b fl-l.*?#revInfo\\d*?\">(.*?)<";

        //String regexId = "<h2 id=\"anime\">Anime</h2>.*?href=\"https://myanimelist.net/anime/(.*?)/";
        Pattern patternId = Pattern.compile(regexId);

        Matcher matcherId = patternId.matcher(contents);
        if(matcherId.find()) {
            int id = Integer.parseInt(matcherId.group(1));
            if(!name.equals(matcherId.group(2))) {
                System.out.println("DEBUG:\"" + name + "\"" + " MATCHED TO \"" + matcherId.group(2) + "\"");
            }
            return id;
        }
        return -1;
    }
}
