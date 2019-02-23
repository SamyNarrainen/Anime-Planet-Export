package com.samynarrainen.parser.parser.animeplanet;

import com.samynarrainen.domain.ListEntry;
import com.samynarrainen.domain.domain.animeplanet.AnimePlanetAnimeListEntry;
import com.samynarrainen.domain.domain.animeplanet.AnimePlanetAnimeStatus;
import com.samynarrainen.parser.ListParser;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class AnimePlanetAnimeListJsonParser implements ListParser {

    /** This is the version of the anime-planet JSON export format that this parser was written for. */
    private static String VERSION = "0.1a";

    private static String JSON_LIST_ENTRIES = "entries";
    private static String JSON_ENTRY_NAME = "name";
    private static String JSON_ENTRY_STATUS = "status";
    private static String JSON_ENTRY_STARTED = "started";
    private static String JSON_ENTRY_COMPLETED = "completed";
    private static String JSON_ENTRY_RATING = "rating";
    private static String JSON_ENTRY_TIMESWATCHED = "times";
    private static String JSON_ENTRY_EPISODESSEEN = "eps";

    //2017-05-11 12:25:57-07
    private static DateFormat animePlanetDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS-Z");

    private final String animePlanetAnimeListJsonFile;
    private JSONArray entries;
    private int header;

    public AnimePlanetAnimeListJsonParser(final String animePlanetAnimeListJsonFile) {
        this.animePlanetAnimeListJsonFile = animePlanetAnimeListJsonFile;
        this.entries = null;
        this.header = 0;
    }

    @Override
    public void parse() {
        try {
            final String jsonString = new String(Files.readAllBytes(Paths.get(animePlanetAnimeListJsonFile)));
            final JSONObject rootJsonObject = new JSONObject(jsonString);
            // TODO parse the version to present a warning if it's changed.
            entries = rootJsonObject.getJSONArray(JSON_LIST_ENTRIES);

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public AnimePlanetAnimeListEntry getNext() {
        if (entries != null && header < entries.length()) {
            return parse(entries.getJSONObject(header++));
        }
        return null;
    }

    @Override
    public AnimePlanetAnimeListEntry getIndex(int index) {
        if (entries != null && index < entries.length()) {
            return parse(entries.getJSONObject(index));
        }
        return null;
    }

    @Override
    public int getCount() {
        return entries == null ? 0 : entries.length();
    }

    private AnimePlanetAnimeListEntry parse(final JSONObject jsonEntry) {
        AnimePlanetAnimeListEntry animePlanetAnimeListEntry = null;
        try {
            animePlanetAnimeListEntry = new AnimePlanetAnimeListEntry(
                    jsonEntry.getString(JSON_ENTRY_NAME),
                    AnimePlanetAnimeStatus.getFromName(jsonEntry.getString(JSON_ENTRY_STATUS)),
                    jsonEntry.isNull(JSON_ENTRY_STARTED) ? null : animePlanetDateFormat.parse(jsonEntry.getString(JSON_ENTRY_STARTED)),
                    jsonEntry.isNull(JSON_ENTRY_COMPLETED) ? null : animePlanetDateFormat.parse(jsonEntry.getString(JSON_ENTRY_COMPLETED)),
                    (int) (jsonEntry.getFloat(JSON_ENTRY_RATING) * 2F),
                    jsonEntry.getInt(JSON_ENTRY_TIMESWATCHED),
                    jsonEntry.getInt(JSON_ENTRY_EPISODESSEEN)
            );
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return animePlanetAnimeListEntry;
    }

}
