package com.samynarrainen;

import junit.framework.TestCase;

import javax.xml.bind.DatatypeConverter;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by Samy on 12/07/2017.
 */
public class ResultTest extends TestCase {

    private String authentication = "";

    protected void setUp(){
        authentication = DatatypeConverter.printBase64Binary((System.getProperty("User_MAL") + ':' + System.getProperty("Pass_MAL")).getBytes());
    }

    public void testEnglish() throws Exception {
        Entry e = new Entry();
        e.name = "Fate/Kaleid Liner Prisma Illya";
        Main.processEntry(e, new ArrayList<Entry>(), authentication);
        assertEquals(e.id, 14829);
    }

    public void testEnglish2() throws Exception {
        Entry e = new Entry();
        e.name = "Laputa: Castle in the Sky";
        e.altTitles.add("Castle in the Sky");
        Main.processEntry(e, new ArrayList<Entry>(), authentication);
        assertEquals(e.id, 513);
    }

    public void testIgnoreCase() throws Exception {
        Entry e = new Entry();
        e.name = "Boku wa Tomodachi ga Sukunai NEXT";
        e.altTitles.add("Haganai: I Don't Have Many Friends NEXT");
        Main.processEntry(e, new ArrayList<Entry>(), authentication);
        assertEquals(e.id, 14967);
    }
}