package com.samynarrainen;

import java.util.List;

/**
 * Created by Samy on 12/07/2017.
 */
public class Handler implements Runnable {

    private final Entry entry;
    private final List<Entry> entries;
    private final String authentication;

    public Handler(Entry e, List<Entry> entries, String authentication) {
        this.entry = e;
        this.entries = entries;
        this.authentication = authentication;
    }

    @Override
    public void run() {
        try {
            Main.processEntry(entry, entries, authentication);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
