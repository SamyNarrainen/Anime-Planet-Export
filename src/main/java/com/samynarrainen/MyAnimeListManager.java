package com.samynarrainen;

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
}
