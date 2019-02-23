package com.samynarrainen;

import com.samynarrainen.domain.Entry;

import java.util.List;

/**
 * Created by Samy on 12/07/2017.
 * A threaded implementation to run one of the processing methods.
 * Useful as threads will block on IO.
 */
public class Handler implements Runnable {

    private final Entry entry;
    private final List<Entry> entries;
    private final String authentication;

    /**
     * The number of attempts made processing an entry.
     */
    private int attempts = 0;

    public Handler(Entry e, List<Entry> entries, String authentication) {
        this.entry = e;
        this.entries = entries;
        this.authentication = authentication;
    }

    @Override
    public void run() {
        while(attempts < 2) {
            attempts++;
            try {
                Main.processEntry(entry, entries, authentication);
                break;
            } catch(Exception e) {
                if(Main.VERBOS) System.out.println("Timed out whilst processing " + entry.name);
            }
        }
    }

}
