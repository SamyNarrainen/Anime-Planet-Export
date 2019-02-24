package com.samynarrainen.converter;

import com.samynarrainen.Result;
import com.samynarrainen.domain.ListEntry;
import com.samynarrainen.parser.parser.animeplanet.AnimePlanetAnimeListJsonParser;
import junit.framework.TestSuite;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static com.samynarrainen.MyAnimeListManager.getMALID;
import static org.junit.Assert.assertEquals;

public class AnimePlanetToMyAnimeListConverterTest extends TestSuite {
    private static final String jsonFilePath = "src\\res\\export-anime-08935730249582347503.json";

    private AnimePlanetAnimeListJsonParser parser;

    @Before
    public void setUp() {
        parser = new AnimePlanetAnimeListJsonParser(jsonFilePath);
        parser.parse();
    }

    @Test
    public void test_ben_to() throws IOException, URISyntaxException, InterruptedException {
        ListEntry listEntry = parser.getIndex(0);
        final Result result = getMALID(listEntry.getName());
        assertEquals(10396, result.getId());
    }

    @Test
    public void test_air() throws IOException, URISyntaxException, InterruptedException {
        ListEntry listEntry = parser.getIndex(1);
        final Result result = getMALID(listEntry.getName());
        assertEquals(101, result.getId());
    }

    @Test
    public void test_yuru_yuri_S2() throws IOException, URISyntaxException, InterruptedException {
        ListEntry listEntry = parser.getIndex(2);
        final Result result = getMALID(listEntry.getName());
        assertEquals(12403, result.getId());
    }

    @Test
    public void test_clannad() throws IOException, URISyntaxException, InterruptedException {
        ListEntry listEntry = parser.getIndex(3);
        final Result result = getMALID(listEntry.getName());
        assertEquals(2167, result.getId());
    }

    @Test
    public void test_5_centimeters_per_second() throws IOException, URISyntaxException, InterruptedException {
        ListEntry listEntry = parser.getIndex(4);
        final Result result = getMALID(listEntry.getName());
        assertEquals(1689, result.getId());
    }

}
