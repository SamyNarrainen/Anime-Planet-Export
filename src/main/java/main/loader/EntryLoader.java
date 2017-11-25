package main.loader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Samy on 25/11/2017.
 * Logic for exporting individual pages, stateful.
 */
public class EntryLoader { //TODO requires a better name!

    private final String page;

    private final Matcher matcher;

    public EntryLoader(String page) {
        this.page = page;
        String regex = "<a title=\"<h5>(.*?)</h5>.*?class=\"card pure-1-6";
        Pattern pattern = Pattern.compile(regex);
        this.matcher = pattern.matcher(page);
    }

    public String getNextRawEntry() {
        if(matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }

}
