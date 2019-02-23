package com.samynarrainen.parser;

import com.samynarrainen.domain.domain.animeplanet.AnimePlanetAnimeListEntry;
import com.samynarrainen.domain.domain.animeplanet.AnimePlanetAnimeStatus;
import com.samynarrainen.parser.parser.animeplanet.AnimePlanetAnimeListJsonParser;
import junit.framework.TestSuite;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;

public class AnimePlanetAnimeListJsonParserTest extends TestSuite {

    private static final String jsonFilePath = "src\\res\\export-anime-08935730249582347503.json";

    private AnimePlanetAnimeListJsonParser parser;

    @Before
    public void setUp() {
        parser = new AnimePlanetAnimeListJsonParser(jsonFilePath);
        parser.parse();
    }

    @Test
    public void first_parsed_entry_matches_expected_values() {
        final AnimePlanetAnimeListEntry listEntry = parser.getNext();
        assertEquals("Ben-To", listEntry.getName());
        assertEquals(AnimePlanetAnimeStatus.WATCHED, listEntry.getStatus());
        assertNull(listEntry.getStartedDate());
        assertNull(listEntry.getCompletedDate());
        assertEquals(8, listEntry.getRating());
        assertEquals(1, listEntry.getTimesWatched());
        assertEquals(12, listEntry.getEpisodesWatched());
    }

    @Test
    public void float_rating_correctly_parsed_to_int() {
        final AnimePlanetAnimeListEntry listEntry = parser.getIndex(1);
        assertEquals(7, listEntry.getRating());
    }
}
