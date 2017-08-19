package com.samynarrainen.Data;

/**
 * Created by Samy on 18/08/2017.
 */
public enum Type {
    TV("TV", "TV"),
    Special("TV Special", "Special"),
    Movie("Movie", "Movie"),
    OVA("OVA", "OVA"),
    DVD_Special("DVD Special", "Special"),
    Web("Web", "ONA"),
    Music("Music Video", "Music"),
    Other("Other", "Special"), //TODO check out Steins;Gate 0 which is 'Other' on AP, but 'Unknown' on MAL.
    None("", "");

    public final String AP, MAL;

    Type(String ap, String mal) {
        AP = ap;
        MAL = mal;
    }
}
