package com.samynarrainen;

/**
 * Created by Samy on 12/07/2017.
 */
public class Result {
    int id = -1;
    boolean perfectMatch = false;

    public Result() {
        this(-1, false);
    }

    public Result(int id, boolean perfectMatch) {
        this.id = id;
        this.perfectMatch = perfectMatch;
    }

    public int getId() {
        return id;
    }
}
