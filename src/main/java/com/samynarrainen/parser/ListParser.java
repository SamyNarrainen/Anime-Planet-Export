package com.samynarrainen.parser;

import com.samynarrainen.domain.ListEntry;

/**
 * Prior to anime-planet adding a export list feature, we scraped the values directly from the site. While scraping
 * provided more granular data than what the export feature provides, we now have two options for 'parsing' the list.
 * The purpose of this interface is to provide an abstraction for which of these options are used.
 */
public interface ListParser {

    void parse();

    ListEntry getNext();

}