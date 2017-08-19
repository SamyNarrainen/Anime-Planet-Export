package com.samynarrainen;

import com.samynarrainen.Data.Type;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Data structure for anime entires.
 */
public class Entry {

    /**
     * The entrie's main title.
     */
    public String name = "";

    /**
     * Stored in a 0-10 rating system.
     */
    public int rating = -1;

    /**
     * The status of this anime in the list. E.g watching.
     */
    public String status = Main.NONE;

    /**
     * The anime's corresponding MAL id.
     */
    public int id = -1;

    /**
     * Alternative titles of the anime.
     */
    public List<String> altTitles = new ArrayList<String>();

    /**
     * The id was found by a perfect match. Thus little chance of mismatch.
     */
    public boolean perfectMatch = false;

    /**
     * Number of episodes seen by the user.
     * Relevant if DROPPED or STALLED, WATCHED where all episodes have been seen.
     */
    public int episodes = -1;

    /**
     * The AP equivalent of id, used as a unique identifier by AP.
     */
    public String AnimePlanetURL = "";

    /**
     * The start and end date that the user watched this anime from.
     * The end date is not used if the anime has not been completely watched.
     * Purposely kept null to prevent un-assigned dates from being used.
     */
    public Date start, end;

    /**
     * The number of times the series has been watched.
     * 1 implies the show has been completed, 2 implies it has been completed and re-watched once.
     */
    public int watchCount = -1;

    /**
     * The studios that produced this show.
     */
    public List<String> studios = new ArrayList<String>();

    /**
     * The publishing year and the year publishing ended, AKA the airing of the last episode.
     */
    public int year = -1, yearEnd = -1;

    /**
     * The season this show belonged to, e.g Summer 2017.
     */
    public String season = "";

    /**
     * The number of episodes available in this show.
     * @see this.episodes
     */
    public int totalEpisodes = -1;

    /**
     * The type of show, for example TV, Movie, Web...
     */
    public Type type = Type.None;

    /**
     * Date format used by MAL.
     */
    private final SimpleDateFormat MAL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Normalises a 0-5 float scale rating system to 0-10 integer which is used by MAL.
     * @param rating a String representing a numerical value.
     */
    public static int convertRating(String rating) {
        //AP uses a 0-5 float rating system.
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
