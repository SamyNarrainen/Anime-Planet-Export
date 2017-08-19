package com.samynarrainen;

import com.samynarrainen.Data.FeedResult;
import com.samynarrainen.Data.Status;
import com.samynarrainen.Data.Type;

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
     * TODO currently doesn't distinguish between anime and AP achievements.
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

    /**
     * Visits the individual page for the given entry on AP and scrapes additional information
     * that can be used to find a match for entries that haven't been found yet.
     * @param entry
     */
    public static void getAdditionalInfo(Entry entry) throws IOException {
        String contents = Main.getPageContents(new URL("http://www.anime-planet.com/anime/" + entry.AnimePlanetURL));
        //System.out.println(contents);

        //Group 1: Type data
        //Group 2: Studio data
        //Group 3: Year data
        //String regexInfo = "pure-g entryBar.*?class=\"type\".*?>(.*?)<.*?studios.*?>(.*?)<.*?years.*?>(\\d*?)<.*?section.*?itemprop=\"description\">.*?>(.*?)<";
        //String regexInfo = "pure-g entryBar.*?class=\"type\".*?>(.*?)<.*?class=\"pure-.*?>(.*?)</div.*?years.*?>(\\d*?)<.*?section.*?itemprop=\"description\">.*?>(.*?)<";
        //TODO Discontinue description until a use is found for it. Make sure to account for case of no description.
        //String regexInfo = "pure-g entryBar.*?class=\"type\".*?>(.*?)<.*?class=\"pure-.*?>(.*?)</div.*?datePublished\">(.*?)</div.*?section.*?itemprop=\"description\">.*?>(.*?)<";
        String regexInfo = "pure-g entryBar.*?class=\"type\".*?>(.*?)<.*?class=\"pure-.*?>(.*?)</div.*?section";

        Pattern patternInfo = Pattern.compile(regexInfo);
        Matcher matcherInfo = patternInfo.matcher(contents);

        //Group 1: Season
        String regexSeason = "seasons.*?>(.*?)<";

        //Group 1: Studio
        String regexStudio = "studios/.*?>(.*?)<";

        //Group 1: Year Data
        //Some unreleased titles do not have year data.
        String regexYearData = "datePublished\">(.*?)<\\/div";

        //Group 1: Year
        String regexYear = "years/.*?>(\\d{4})";

        if(matcherInfo.find()) {
            String type = matcherInfo.group(1);
            //System.out.println(type);
            if(type.contains("(")) {
                //This anime contains episode information.
                type = type.substring(0, type.indexOf('(') - 1); //The last character is an empty space.

                //Group 1: Episodes
                String regexTotalEpisodes = "\\((\\d*?) ";
                Matcher matcherTotalEpisodes = Pattern.compile(regexTotalEpisodes).matcher(matcherInfo.group(1));
                if(matcherTotalEpisodes.find()) {
                    entry.totalEpisodes = Integer.parseInt(matcherTotalEpisodes.group(1));
                }
            } else {
                type = type.substring(0, type.length() - 1); //The last character is an empty space.
            }

            if(type.equals(Type.Movie.AP)) {
                entry.type = Type.Movie;
            } else if(type.equals(Type.OVA.AP)) {
                entry.type = Type.OVA;
            } else if(type.equals(Type.Special.AP) || type.equals(Type.DVD_Special.AP)) {
                //MAL doesn't distinguish between DVD and ordinary specials.
                entry.type = Type.Special;
            } else if(type.equals(Type.TV.AP)) {
                entry.type = Type.TV;
            } else if(type.equals(Type.Web.AP)) {
                entry.type = Type.Web;
            } else if(type.equals(Type.Music.AP)) {
                entry.type = Type.Music;
            } else if(type.equals(Type.Other.AP)) {
                entry.type = Type.Other;
            }


            if(entry.status.equals(Status.Watched)) {
                entry.totalEpisodes = entry.episodes;
            } else if(entry.totalEpisodes == -1) {
                if(entry.type.equals(Type.Movie)) {
                    //MAL gives an episode count for movies which always seems to be 1.
                    //AP sometimes doesn't have this information, especially for new releases.
                    entry.totalEpisodes = 1;
                }
            }
            //OVA (2 eps)
            //TV (12 eps)
            //TV Special (1 ep x 45 min)
            //Movie (1 ep x 107 min)

            Matcher matcherStudio = Pattern.compile(regexStudio).matcher(matcherInfo.group(2));
            while(matcherStudio.find()) {
                entry.studios.add(matcherStudio.group(1));
            }

            Matcher matcherYearData = Pattern.compile(regexYearData).matcher(matcherInfo.group(0));
            if(matcherYearData.find()) {
                Matcher matcherYear = Pattern.compile(regexYear).matcher(matcherYearData.group(1));
                while(matcherYear.find()) {
                    if(entry.year == -1) {
                        entry.year = Integer.parseInt(matcherYear.group(1));
                    } else if(entry.yearEnd == -1) {
                        //The publishing year has already been set, this final year must be the publishing end year.
                        entry.yearEnd = Integer.parseInt(matcherYear.group(1));
                    }
                }
            }


            Matcher matcherSeason = Pattern.compile(regexSeason).matcher(matcherInfo.group(0));
            if(matcherSeason.find()) {
                entry.season = matcherSeason.group(1);
            }
        }
    }
}
