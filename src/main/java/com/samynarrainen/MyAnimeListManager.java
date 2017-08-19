package com.samynarrainen;

import com.samynarrainen.Data.Type;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Samy on 15/08/2017.
 * Functionality relevant to a MAL account
 */
public class MyAnimeListManager {

    private static boolean VERBOS = true;

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

        //Group 1: Type
        //Group 2: Episodes
        //Group 3: Year data
        //Group 4: Studio
        String regexInfo = "Information.*?\\?type=.*?\">(.*?)<.*?Episodes:</span>(.*?)<.*?Aired:</span>.*?,(.*?)<.*?Studios:.*?producer.*?title=\"(.*?)\"";
        Matcher matcherInfo = Pattern.compile(regexInfo).matcher(contents);

        //Group 1: Season
        String regexSeason = "Premiered:.*?season.*?\">(.*?)<";
        Matcher matcherSeason = Pattern.compile(regexSeason).matcher(contents);

        //Group 1: Year
        String regexYear = "(\\d{4})";

        if(matcherInfo.find()) {
            String type = matcherInfo.group(1);
            if(type.equals(Type.Movie.MAL)) {
                entry.type = Type.Movie;
            } else if(type.equals(Type.OVA.MAL)) {
                entry.type = Type.OVA;
            } else if(type.equals(Type.Special.MAL)) {
                entry.type = Type.Special;
            } else if(type.equals(Type.TV.MAL)) {
                entry.type = Type.TV;
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

            entry.totalEpisodes = Integer.parseInt(matcherInfo.group(2).replace(" ", ""));
            //TODO MAL separates producers and studio, whilst AP doesn't.
            entry.studios.add(matcherInfo.group(4));

            if(matcherSeason.find()) {
                entry.season = matcherSeason.group(1);
            }
        }

        return entry;
    }
}
