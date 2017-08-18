package com.samynarrainen.Data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Samy on 18/08/2017.
 * Contains the result of scraping an AP feed.
 */
public class FeedResult {

    /**
     * The date format given by AP, for example 'Jun 19, 2015 2:54 pm'.
     */
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("MMMM d, yyyy hh:mm a", Locale.ENGLISH);


    public String URL = "";
    public Date timestamp;

    public String toString() {
        String to_return = "";
        to_return += URL + ",";
        to_return += timestamp;
        return to_return;
    }
}
