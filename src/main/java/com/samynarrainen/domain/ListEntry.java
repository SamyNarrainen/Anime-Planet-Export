package com.samynarrainen.domain;

import java.util.Date;

/**
 * This interface provides an abstraction for the fundamental components that make up an entry in a list. The intention
 * is that it is agnostic to site and list type (i.e anime and manga).
 */
public interface ListEntry {

    String getName();

    ListEntryStatus getStatus();

    Date getStartedDate();

    Date getCompletedDate();

    /**
     * @return a rating value from 0 - 10.
     */
    int getRating();

}
