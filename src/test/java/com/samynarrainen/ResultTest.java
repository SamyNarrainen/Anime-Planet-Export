package com.samynarrainen;

import junit.framework.TestCase;

import javax.xml.bind.DatatypeConverter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by Samy on 12/07/2017.
 */
public class ResultTest extends TestCase {

    private String authentication = "";

    List<Entry> entries = new ArrayList<Entry>();

    protected void setUp() throws Exception {
        authentication = DatatypeConverter.printBase64Binary((System.getProperty("User_MAL") + ':' + System.getProperty("Pass_MAL")).getBytes());

        String contents = Main.getPageContents(new URL("http://www.anime-planet.com/users/" + System.getProperty("User_AP") + "/anime?sort=title&page=1"));
        int pages = 1;

        //Look for more pages...
        String regexPage = "'pagination aligncenter'.*(page=(.)).*class='next'>";
        Pattern patternPage = Pattern.compile(regexPage);
        Matcher matcherPage = patternPage.matcher(contents);
        if(matcherPage.find()) {
            pages = Integer.parseInt(matcherPage.group(2)) + 1; //TODO the page count given is 1 less, so add 1. Find out why.
        }

        entries.addAll(Main.getEntries(contents));

        //Already searched page 1, so start on 2.
        for(int i = 2; i < pages; i++) {
            String pageContents = Main.getPageContents(new URL("http://www.anime-planet.com/users/" + System.getProperty("User_AP") + "/anime?sort=title&page=" + i));
            entries.addAll(Main.getEntries(pageContents));
        }
    }

    public void testEnglish() throws Exception {
        Entry e = new Entry();
        e.name = "Fate/Kaleid Liner Prisma Illya";
        Main.processEntry(e, new ArrayList<Entry>(), authentication);
        assertEquals(14829, e.id);
    }

    public void testEnglish2() throws Exception {
        Entry e = new Entry();
        e.name = "Laputa: Castle in the Sky";
        e.altTitles.add("Castle in the Sky");
        Main.processEntry(e, new ArrayList<Entry>(), authentication);
        assertEquals(513, e.id);
    }

    public void testEnglish3() throws Exception {
        Entry e = new Entry();
        e.name = "Kizumonogatari Part 1: Tekketsu";
        Main.processEntry(e, new ArrayList<Entry>(), authentication);
        assertEquals(9260, e.id);
    }

    public void testIgnoreCase() throws Exception {
        Entry e = new Entry();
        e.name = "Boku wa Tomodachi ga Sukunai NEXT";
        e.altTitles.add("Haganai: I Don't Have Many Friends NEXT");
        Main.processEntry(e, new ArrayList<Entry>(), authentication);
        assertEquals(14967, e.id);
    }

    public void testLavenDistance() throws Exception {
        int id1 = -1;
        int id2 = -1;
        for(Entry e : entries) {
            if(e.name.equals("Nisekoi")) {
                Main.processEntry(e, new ArrayList<Entry>(), authentication);
                id1 = e.id;
            }
        }
        for(Entry e : entries) {
            if(e.name.equals("Nisekoi:")) {
                Main.processEntry(e, new ArrayList<Entry>(), authentication);
                id2 = e.id;
            }
        }
        assertEquals(18897, id1);
        assertEquals(27787, id2);
    }

    // TODO look onto the individual pages for synonyms as sometimes the search API doesn't work, but the
    /*
    public void testAgainstList() throws Exception {
        for(Entry e : entries) {
            if(e.name.equals("JoJo’s Bizarre Adventure: Stardust Crusaders - Battle in Egypt")) {
                Main.processEntry(e, new ArrayList<Entry>(), authentication);
                assertEquals(26055, e.id);
            }
        }
        fail();
    }
    */
}