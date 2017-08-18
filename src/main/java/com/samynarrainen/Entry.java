package com.samynarrainen;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Data structure for anime entires.
 */
public class Entry {

    public String name = "";
    public int rating = -1;
    public String status = Main.NONE;
    public int id = -1;
    public List<String> altTitles = new ArrayList<String>();
    //The ID of this Entry was found by matching it's name perfectly.
    //Therefore little chance of mismatch.
    public boolean perfectMatch = false;

    /*
     * Relevant if DROPPED or STALLED, WATCHED assumes all episodes seen.
     */
    public int episodes = -1;

    public String AnimePlanetURL = "";

    /**
     * @note purposely kept null to prevent un assigned dates from being used.
     */
    public Date start, end;

    /**
     * The number of times the series has been watched.
     */
    public int watchCount = -1;

    /**
     * Date format used by MAL.
     */
    private final SimpleDateFormat MAL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");



    public static int convertRating(String rating) {
        return (int) Float.parseFloat(rating) * 2;
    }

    public static String statusLiteral(String status) {
        switch(status) {
            case Main.WATCHED : return "Completed";
            case Main.WATCHING : return "Watching";
            case Main.WANT_TO_WATCH : return "Plan to Watch";
            case Main.STALLED : return "On-Hold";
            case Main.DROPPED : return "Dropped";
        }
        return ""; //Also for NONE
    }


    public String toString() {
        String to_return = "";

        to_return += "(NAME: " + name + ") ";
        if(!AnimePlanetURL.equals("")) {
            to_return += "(AP URL: http://www.anime-planet.com/anime/" +  AnimePlanetURL + ") "; //TODO need to change anime to manga if we add manga.
        }
        to_return += "(ID: " + id + ") ";
        to_return += "(STATUS: " + statusLiteral(status) + ") ";
        to_return += "(RATING: " + rating + ") ";
        to_return += "(EPS: " + episodes + ") ";
        if(id != -1) {
            to_return += "(URL: " + "https://myanimelist.net/anime/" + id + "/)";
        }
        if(start != null && end != null) {
            to_return += "(START: " + start + ") ";
            to_return += "(END: " + end + ") ";
        }
        if(status.equals(Main.WATCHED)) {
            to_return += "(WATCH_COUNT: " + watchCount + ") ";
        }

        return to_return;
    }

    @Override
    public boolean equals(Object e) {
        if(e instanceof Entry) {
            return ((Entry) e).id == this.id;
        } else if(e instanceof Integer) {
            return ((Integer) e) == this.id;
        }
        return false;
    }

    public String toXML() {
        if (id == -1) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<anime>");
        sb.append("<series_animedb_id>" + id + "</series_animedb_id>");


        if (episodes >= 0) {
            sb.append("<my_watched_episodes>" + episodes + "</my_watched_episodes>");
        }

        sb.append("<my_status>" + statusLiteral(status) + "</my_status>");


        if (rating >= 0) {
            sb.append("<my_score>" + rating + "</my_score>");
        }

        if (start != null) {
            sb.append("<my_start_date>" + MAL_DATE_FORMAT.format(start) + "</my_start_date>");
        }

        if (status.equals(Main.WATCHED)) {
            //'finish date' only makes sense if the show is finished.
            if (end != null) {
                sb.append("<my_finish_date>" + MAL_DATE_FORMAT.format(end) + "</my_finish_date>");
            }

            //Accounting for number of times re-watched, so -1 for for the initial viewing.
            sb.append("<my_rewatching>" + (watchCount - 1) +"</my_rewatching>");
        }

        sb.append("<update_on_import>1</update_on_import></anime>");

        return sb.toString();
    }
}
