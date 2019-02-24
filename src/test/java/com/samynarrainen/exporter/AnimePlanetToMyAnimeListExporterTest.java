package com.samynarrainen.exporter;

import com.samynarrainen.converter.AnimePlanetToMyAnimeListConverter;
import com.samynarrainen.domain.domain.animeplanet.AnimePlanetAnimeListEntry;
import com.samynarrainen.parser.parser.animeplanet.AnimePlanetAnimeListJsonParser;
import junit.framework.TestSuite;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class AnimePlanetToMyAnimeListExporterTest extends TestSuite {

    private final String JSON_FILE_PATH = "src\\res\\export-anime-08935730249582347503_full.json";
    private final String TEMP_EXPORT_FILE_PATH = "src\\res\\export.xml";

    private AnimePlanetAnimeListJsonParser parser;

    @Before
    public void onSetup() {
        parser = new AnimePlanetAnimeListJsonParser(JSON_FILE_PATH);
        parser.parse();
    }

    @Test
    public void file_exists() {
        final List<AnimePlanetAnimeListEntry> entries = new ArrayList<>();
        entries.add(parser.getIndex(0));
        final AnimePlanetToMyAnimeListConverter converter = new AnimePlanetToMyAnimeListConverter(entries);
        final MyAnimeListExporter exporter = new MyAnimeListExporter(converter.convert(), TEMP_EXPORT_FILE_PATH);
        exporter.export();
        assertTrue(new File(TEMP_EXPORT_FILE_PATH).exists());
    }

}
