package com.samynarrainen.domain.domain.animeplanet;

import com.samynarrainen.domain.ListEntryStatus;

import java.util.stream.Stream;

public enum AnimePlanetAnimeStatus implements ListEntryStatus {

    WATCHED(1, "watched"),
    WATCHING(2, "watching"),
    DROPPED(3, "dropped"),
    WANT_TO_WATCH(4, "want to watch"),
    STALLED(5, "stalled"),
    WONT_WATCH(6, "won't watch"),
    ;

    private final int id;
    private final String name;

    AnimePlanetAnimeStatus(final int id, final String name) {
        this.name = name;
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
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
        return this.equals(WONT_WATCH);
    }

    public static AnimePlanetAnimeStatus getFromName(final String name) {
        return Stream.of(AnimePlanetAnimeStatus.values()).filter(status -> status.name.equals(name)).findFirst().get();
    }

}
