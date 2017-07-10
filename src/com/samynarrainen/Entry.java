package com.samynarrainen;

/**
 * Data structure for anime entires.
 */
public class Entry {

    public String name = "";
    public int rating = -1;
    public String status = Main.NONE;
    public int id = -1;

    /*
     * Relevant if DROPPED or STALLED, WATCHED assumes all episodes seen.
     */
    public int episodes = -1;


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
        to_return += "(ID: " + id + ") ";
        to_return += "(STATUS: " + statusLiteral(status) + ") ";
        to_return += "(RATING: " + rating + ") ";
        to_return += "(EPS: " + episodes + ") ";

        return to_return;
    }

    /**
     * NAME,ID,STATUS,RATING,EPS
     */
    public String toCSV() {
        String to_return = "";

        to_return += name + ",";
        to_return += id + ",";
        to_return += statusLiteral(status) + ",";
        to_return += rating + ",";
        to_return += episodes + ",";

        return to_return;
    }


    public String toXML() {
        if(id == -1) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<anime>");
        sb.append("<series_animedb_id>" + id + "</series_animedb_id>");


        if(episodes >= 0) {
            sb.append("<my_watched_episodes>" + episodes + "</my_watched_episodes>");
        }

        sb.append("<my_status>" + statusLiteral(status) + "</my_status>");


        if(rating >= 0) {
            sb.append("<my_score>" + rating + "</my_score>");
        }

        sb.append("<update_on_import>1</update_on_import></anime>");

        return sb.toString();
    }
}
