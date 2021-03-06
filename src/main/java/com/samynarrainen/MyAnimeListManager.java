package com.samynarrainen;

import com.samynarrainen.Data.Type;
import com.samynarrainen.domain.Entry;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Samy on 15/08/2017.
 * Functionality relevant to a MAL account
 */
public class MyAnimeListManager {

    private static boolean VERBOS = true;

    /** The amount of time between each page request to avoid spam. */
    private static long REQUEST_INTERVAL_MS = 2000;

    /**
     * Deletes ALL anime entries conntained in the specified user's MAL account.
     * @param authentication
     * @param username
     * @throws IOException
     */
    public static void deleteAnime(String authentication, String username) throws IOException {
        String contents = Main.getPageContents(new URL("https://myanimelist.net/malappinfo.php?u=" + username + "&status=all&type=anime"));
        System.out.println(username);

        String regex = "<series_animedb_id>(\\d*?)</series_animedb_id>";
        Pattern searchPattern = Pattern.compile(regex);
        Matcher matcher = searchPattern.matcher(contents);

        while(matcher.find()) {
            int animeId = Integer.parseInt(matcher.group(1));
            deleteAnime(authentication, animeId);

            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Deletes the given entry from the user's MAL account.
     * @param authentication
     * @param animeId
     * @throws IOException
     */
    public static void deleteAnime(String authentication, int animeId) throws IOException {
        URL url = new URL("https://myanimelist.net/api/animelist/delete/" + animeId + ".xml");
        String basicAuth = "Basic " + authentication;

        HttpURLConnection httpURLConnection = (HttpURLConnection)  url.openConnection();
        httpURLConnection.setRequestMethod("DELETE");
        httpURLConnection.addRequestProperty("User-Agent", "Chrome");
        httpURLConnection.setRequestProperty("Authorization", basicAuth);
        httpURLConnection.setDoOutput(true);

        //TODO is this necessary?
        BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

        if(VERBOS) {
            String line;
            while((line = reader.readLine()) != null) {
                System.out.println(line + " " + animeId);
            }
        }
        reader.close();
    }

    /**
     * Extracts additional info from the specified page from MAL and returns the info contained in an Entry.
     * TODO scape description, might be worth doing it at API search level though.
     * @param id
     * @return
     */
    public static Entry getAdditionalInfo(int id) throws IOException {
        Entry entry = new Entry();

        String contents = Main.getPageContents(new URL("https://myanimelist.net/anime/" + id));
        //System.out.println(contents);

        //Group 1: Type data
        //Group 2: Episodes
        //Group 3: Year data
        //Group 4: Studio
        //TODO, not all types have links, e.g Music
        //String regexInfo = "Information.*?\\?type=.*?\">(.*?)<.*?Episodes:</span>(.*?)<.*?Aired:</span>.*?,(.*?)<.*?Studios:.*?producer.*?title=\"(.*?)\"";
        String regexInfo = "Information.*?Type:.*?>(.*?)<\\/div.*?Episodes:<\\/span>(.*?)<.*?Aired:<\\/span>.*?,(.*?)<.*?Studios:.*?producer.*?title=\"(.*?)\"";
        Matcher matcherInfo = Pattern.compile(regexInfo).matcher(contents);

        //Group 1: Season
        String regexSeason = "Premiered:.*?season.*?\">(.*?)<";
        Matcher matcherSeason = Pattern.compile(regexSeason).matcher(contents);

        //Group 1: Year
        String regexYear = "(\\d{4})";

        //Group 1: Type
        String regexTypeLinked = "type.*?>(.*?)<";

        if(matcherInfo.find()) {
            String type = "";
            Matcher matcherTypeLinked = Pattern.compile(regexTypeLinked).matcher(matcherInfo.group(1));
            if(matcherTypeLinked.find()) {
                type = matcherTypeLinked.group(1);
            } else {
                //The type is specified by one which has no link, such as Music.
                type = matcherInfo.group(1).trim();
            }

            if(type.equals(Type.Movie.MAL)) {
                entry.type = Type.Movie;
            } else if(type.equals(Type.OVA.MAL)) {
                entry.type = Type.OVA;
            } else if(type.equals(Type.Special.MAL)) {
                entry.type = Type.Special;
            } else if(type.equals(Type.TV.MAL)) {
                entry.type = Type.TV;
            } else if(type.equals(Type.Web.MAL)) {
                entry.type = Type.Web;
            } else if(type.equals(Type.Music.MAL)) {
                entry.type = Type.Music;
            } else if(type.equals(Type.Other.MAL)) {
                entry.type = Type.Other;
            }

            Matcher matcherYear = Pattern.compile(regexYear).matcher(matcherInfo.group(3));
            while(matcherYear.find()) {
                if(entry.year == -1) {
                    entry.year = Integer.parseInt(matcherYear.group(1));
                } else if(entry.yearEnd == -1) {
                    entry.yearEnd = Integer.parseInt(matcherYear.group(1));
                    if(entry.yearEnd == entry.year) {
                        entry.yearEnd = -1; //AP doesn't give an end year if they're the same.
                    }
                }
            }

            try {
                entry.totalEpisodes = Integer.parseInt(matcherInfo.group(2).replace(" ", ""));
            } catch(NumberFormatException e) {}
            //TODO MAL separates producers and studio, whilst AP doesn't.
            entry.studios.add(matcherInfo.group(4));

            if(matcherSeason.find()) {
                entry.season = matcherSeason.group(1);
            }
        }

        return entry;
    }

    /**
     * Uses the MAL search API to receive an id.
     * @param entry
     * @param authentication username:password in base64 binary
     * @return an ID only if the name matches perfectly.
     * @throws MalformedURLException
     * @throws IOException
     */
    public static Result getMALIDAPI(Entry entry, String authentication) throws IOException {
        String name = entry.name;
        URL url = new URL("https://myanimelist.net/api/anime/search.xml?q=" + name.replace(" ", "%20"));
        String basicAuth = "Basic " + authentication;
        HttpURLConnection httpURLConnection = (HttpURLConnection)  url.openConnection();
        httpURLConnection.addRequestProperty("User-Agent", "Chrome");
        httpURLConnection.setRequestProperty("Authorization", basicAuth);
        BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

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
        Matcher matcher = Pattern.compile(regex).matcher(contents);

        int shortestDistance = -1, shortestDistanceId = -1;

        while(matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            String title = matcher.group(2).toLowerCase();
            String english = matcher.group(3).toLowerCase();
            List<String> synonyms = new ArrayList<String>();
            synonyms.addAll(Arrays.asList(matcher.group(4).toLowerCase().split(";")));
            name = name.toLowerCase();

            //A collection of all the names this entry goes by...
            List<String> entryNames = new ArrayList<String>();
            entryNames.add(name);
            entryNames.addAll(entry.altTitles);

            for(String n : entryNames) {
                System.out.println("N:" + n);
                if (title.equals(n) || synonyms.contains(n) || english.equals(n)) {
                    return new Result(id, true);
                } else {
                    List<String> names = new ArrayList<String>();
                    names.addAll(synonyms);
                    names.add(title);
                    names.add(english);

                    for(String s : names) {
                        int distance = StringUtils.getLevenshteinDistance(name, s);
                        if(distance < Main.LAVEN_DIST && (shortestDistance == -1 || distance < shortestDistance)) {
                            shortestDistance = distance;
                            shortestDistanceId = id;
                        }
                    }
                }
            }
        }

        //There wasn't a perfect match across all the search results.
        //Try using the result which had the shortest lavenshtein distance...
        if(shortestDistanceId != -1) {
            return new Result(shortestDistanceId, false);
        }

        if(VERBOS) System.out.println("Search API couldn't find match for \"" + name + "\"");
        return new Result();
    }

    /**
     * Compares the search results from the MAL website to find a matching name.
     * @param name
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public static Result getMALID(String name) throws IOException, URISyntaxException, InterruptedException {
        String contents = Main.getPageContents(new URL("https://myanimelist.net/search/all?q=" + name.replace(" ", "%20")));

        //Group 1: ID
        //Group 2: Title
        String regexId = "anime/(\\d*?)/.*?hoverinfo_trigger*.?fw-b fl-l.*?#revInfo\\d*?\">(.*?)<";
        Matcher matcherId = Pattern.compile(regexId).matcher(contents);

        int shortestDistance = -1, shortestDistanceId = -1;

        while(matcherId.find()) {
            int id = Integer.parseInt(matcherId.group(1));
            String title = StringEscapeUtils.unescapeHtml4(matcherId.group(2)); //TODO should be doing this more, all around?

            Thread.sleep(REQUEST_INTERVAL_MS);
            if(name.compareToIgnoreCase(title) == 0) {
                return new Result(id, true);
            }
            else {
                final Result pageResult = getMALIDFromPage(name, id);
                if (pageResult.getId() != -1) {
                    return pageResult;
                }
                else {
                    int distance = StringUtils.getLevenshteinDistance(name, title);
                    if(distance < Main.LAVEN_DIST && (shortestDistance == -1 || distance < shortestDistance)) {
                        if(VERBOS) System.out.println("Distance change for " + name + " with " + title + " for distance " + distance);
                        shortestDistance = distance;
                        shortestDistanceId = id;
                    }
                }
            }
        }

        if(shortestDistanceId != -1) {
            if(VERBOS) System.out.println("Warning: matched " + name + " to " + shortestDistanceId + " with laven dist of " + shortestDistance);
            return new Result(shortestDistanceId, false);
        }

        return new Result(-1, false);
    }

    public static Result getMALIDFromPage(final String name, final int id) throws IOException {
        final String contents = Main.getPageContents(new URL("https://myanimelist.net/anime/" + id));

        //Group 1: English title
        String regexEnglishTitle = "English:.*?>\\s(.*?)\\s\\s<";
        Matcher matcherEnglishTitle = Pattern.compile(regexEnglishTitle).matcher(contents);
        while (matcherEnglishTitle.find()) {
            final String englishTitle = StringEscapeUtils.unescapeHtml4(matcherEnglishTitle.group(1));
            if (name.compareToIgnoreCase(englishTitle) == 0) {
                return new Result(id, true);
            }
        }
        return new Result(-1, false);
    }
}
