package com.samynarrainen.Data;

/**
 * Created by Samy on 19/08/2017.
 */
public enum Status {
    None("-1", ""),
    Watched("1", "Completed"),
    Watching("2", "Watching"),
    Dropped("3", "Dropped"),
    WantToWatch("4", "Plan to Watch"),
    Stalled("5", "On-Hold"),
    WontWatch("6", "");

    /**
     * The value used by AP to represent a given status.
     */
    public final String code;

    /**
     * The literal English used to describe a status.
     * Also used by MAL.
     */
    public final String literal;

    Status(String apCode, String literal) {
        this.code = apCode;
        this.literal = literal;
    }

    /**
     * Returns the corresponding Status to the Anime-Planet code.
     * @param apCode
     */
    public static Status get(String apCode) {
        for(Status status : Status.values()) {
            if(status.code.equals(apCode)) {
                return status;
            }
        }
        return None;
    }
}
