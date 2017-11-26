package main.loader.animeplanet;

import com.samynarrainen.Main;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Samy on 25/11/2017.
 */
public class PageLoader {

    public static String getPage(String username, int page) throws IOException {
        return Main.getPageContents(new URL("http://www.anime-planet.com/users/" + username + "/anime?sort=title&page=" + page));
    }

    /**
     * The number of pages that a user's anime-planet list contains.
     * @param username
     * @return
     * @throws IOException
     * TODO this is clearer, but address that we'll load the page twice.
     */
    public static int numberOfPages(String username) throws IOException {
        int pages = 1;

        String contents = getPage(username, 1);

        String regexPages = "sort=title&amp;page=(\\d*?)'";
        Matcher matcherPages = Pattern.compile(regexPages).matcher(contents);

        while(matcherPages.find()) {
            int n = Integer.parseInt(matcherPages.group(1));
            if(n > pages) {
                pages = n;
            }
        }
        return pages;
    }



}
