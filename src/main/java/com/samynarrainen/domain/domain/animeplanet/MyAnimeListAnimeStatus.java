package com.samynarrainen.domain.domain.animeplanet;

import com.samynarrainen.domain.ListEntryStatus;

public enum MyAnimeListAnimeStatus implements ListEntryStatus {

    WATCHED(1, "Completed"),
    WATCHING(2, "watching"),
    DROPPED(3, "Dropped"),
    WANT_TO_WATCH(4, "Plan to Watch"),
    STALLED(5, "On-Hold"),
    ;

    private final int id;
    private final String name;

    MyAnimeListAnimeStatus(final int id, final String name) {
        this.name = name;
        this.id = id;
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isWatched() {
        return this.equals(WATCHED);
    }

    @Override
    public boolean isWatching() {
        return this.equals(WATCHING);
    }

    @Override
    public boolean isDropped() {
        return this.equals(DROPPED);
    }

    @Override
    public boolean isWantToWatch() {
        return this.equals(WANT_TO_WATCH);
    }

    @Override
    public boolean isStalled() {
        return this.equals(STALLED);
    }

    @Override
    public boolean isWontWatch() {
        return false;
    }

    public static MyAnimeListAnimeStatus convert(final ListEntryStatus listEntryStatus) {
        if (listEntryStatus.isWatched()) {
            return WATCHED;
        }
        else if (listEntryStatus.isWatching()){
            return WATCHING;
        }
        else if (listEntryStatus.isDropped()) {
            return DROPPED;
        }
        else if (listEntryStatus.isWantToWatch()) {
            return WANT_TO_WATCH;
        }
        else if (listEntryStatus.isStalled()) {
            return  STALLED;
        }
        else  {
            return null;
        }
    }
}
