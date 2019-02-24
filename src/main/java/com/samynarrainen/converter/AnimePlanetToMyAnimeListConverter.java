package com.samynarrainen.converter;

import com.samynarrainen.MyAnimeListManager;
import com.samynarrainen.Result;
import com.samynarrainen.domain.domain.animeplanet.AnimePlanetAnimeListEntry;
import com.samynarrainen.domain.domain.animeplanet.MyAnimeListAnimeListEntry;
import com.samynarrainen.parser.parser.animeplanet.AnimePlanetAnimeListJsonParser;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class AnimePlanetToMyAnimeListConverter {

    private static final boolean VERBOSE = true;
    private static final int REQUEST_INTERVAL_MS = 2000;

    private final List<AnimePlanetAnimeListEntry> entries;

    public AnimePlanetToMyAnimeListConverter(final List<AnimePlanetAnimeListEntry> entries) {
        this.entries = entries;
    }

    public List<MyAnimeListAnimeListEntry> convert() {
        try {
            List<MyAnimeListAnimeListEntry> parsedListEntries = new ArrayList<>();

            for(AnimePlanetAnimeListEntry listEntry : entries) {
                final Result result = MyAnimeListManager.getMALID(listEntry.getName());
                if (result.getId() != -1) {
                    final MyAnimeListAnimeListEntry parsedListEntry = new MyAnimeListAnimeListEntry(result.getId(), listEntry);
                    parsedListEntries.add(parsedListEntry);
                    if (VERBOSE) {
                        System.out.println("Parsed " + listEntry.getName() + " to MAL ID " + result.getId());
                    }
                }
                else {
                    if (VERBOSE) {
                        System.out.println("Failed to parse " + listEntry.getName());
                        return parsedListEntries;
                    }
                }
                Thread.sleep(REQUEST_INTERVAL_MS);
            }

            return parsedListEntries;
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
