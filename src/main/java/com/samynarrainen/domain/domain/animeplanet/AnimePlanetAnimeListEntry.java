package com.samynarrainen.domain.domain.animeplanet;

import com.samynarrainen.domain.ListEntry;
import com.samynarrainen.domain.ListEntryStatus;

import java.util.Date;

public class AnimePlanetAnimeListEntry implements ListEntry {

    private final String name;
    private final ListEntryStatus status;
    private final Date startDate;
    private final Date endDate;
    private final int rating;
    private final int timesWatched;
    private final int episodesWatched;

    public AnimePlanetAnimeListEntry(final String name, final ListEntryStatus status, final Date startDate,
                                     final Date endDate, final int rating, final int timesWatched, final int episodesWatched) {
        this.name = name;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.rating = rating;
        this.timesWatched = timesWatched;
        this.episodesWatched = episodesWatched;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ListEntryStatus getStatus() {
        return status;
    }

    @Override
    public Date getStartedDate() {
        return startDate;
    }

    @Override
    public Date getCompletedDate() {
        return endDate;
    }

    @Override
    public int getRating() {
        return rating;
    }

    public int getTimesWatched() {
        return timesWatched;
    }

    public int getEpisodesWatched() {
        return episodesWatched;
    }
}
