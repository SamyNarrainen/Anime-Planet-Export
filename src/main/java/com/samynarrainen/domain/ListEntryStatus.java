package com.samynarrainen.domain;

/**
 * This interface provides an abstraction for the status of an entry within a list (represented by {@link ListEntry}).
 * Much like {@link ListEntry}, this is intended to be site and type (i.e. anime and manga) agnostic.
 */
public interface ListEntryStatus {

    int getId();

    String getName();

    boolean isWatched();

    boolean isWatching();

    boolean isDropped();

    boolean isWantToWatch();

    boolean isStalled();

    boolean isWontWatch();

}
