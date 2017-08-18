package com.samynarrainen;

import com.samynarrainen.Data.FeedResult;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains functionality for scraping anime-planet.
 * Created by Samy on 18/08/2017.
 */
public class AnimePlanetManager {

    /**
     * Exports the personal feed of an anime-planet account into a list of Strings.
     * @param username
     * @return
     */
    public static List<FeedResult> exportFeed(String username) throws IOException, ParseException {
        List<FeedResult> feed = new ArrayList<FeedResult>();

        boolean pageRemaining = true;

        //Returns a match if there is still another page to scrape.
        String regexNextPage = "class=\"next\"";
        Pattern patternNextPage = Pattern.compile(regexNextPage);

        //Group 1: Timestamp
        //Group 2: URL ID, more reliable than title.
        //String regexFeedEntry = "timestamp.*?title=\"(.*?)\".*?<h5>.*?>(.*?)</h5>.*?</tr>";
        String regexFeedEntry = "timestamp.*?title=\"(.*?)\".*?crop\" href=\"/anime/(.*?)\".*?</tr>";
        Pattern patternFeedEntry = Pattern.compile(regexFeedEntry);

        for(int page = 1; pageRemaining; page++) {
            String contents = Main.getPageContents(new URL("http://www.anime-planet.com/users/" + username + "/feed?feed_type=self&page=" + page));

            Matcher matcherNextPage = patternNextPage.matcher(contents);
            pageRemaining = matcherNextPage.find();

            Matcher matcherFeedEntry = patternFeedEntry.matcher(contents);
            while(matcherFeedEntry.find()) {
                String url = matcherFeedEntry.group(2);
                String timestamp = matcherFeedEntry.group(1);

                FeedResult feedResult = new FeedResult();
                feedResult.URL = url;
                feedResult.timestamp = FeedResult.DATE_FORMAT.parse(timestamp);
                feed.add(feedResult);
            }
        }

        return feed;
    }

    /**
     * Examines the AP feed data, calculating the start and end dates for each entry.
     * TODO a better data structure for entries in this instance would be map.
     * @param feed
     * @param entries
     */
    public static void calculateDates(List<FeedResult> feed, List<Entry> entries) {
        for(FeedResult result : feed) {
            //Find the matching entry.
            Entry entry = null;
            for(Entry e : entries) {
                if(e.AnimePlanetURL.equals(result.URL)) {
                    entry = e;
                    break;
                }
            }

            if(entry != null) {
                if(entry.start == null && entry.end == null) {
                    entry.start = result.timestamp;
                    entry.end = result.timestamp;
                } else {
                    if(result.timestamp.before(entry.start)) {
                        entry.start = result.timestamp;
                    } else if(result.timestamp.after(entry.end)) {
                        entry.end = result.timestamp;
                    }
                }
            }
        }
    }

}
