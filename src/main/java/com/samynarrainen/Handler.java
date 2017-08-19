package com.samynarrainen;

import java.io.IOException;
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
        } catch (IOException e) {
          if(e.getMessage().contains("HTTP response code: 429")) {
              //Got timed out! Let's retry?
              while(true) {
                  try {
                      Main.processEntry(entry, entries, authentication);
                      break;
                  } catch (Exception e2) {
                      System.out.println("Encountered error once again whilst trying to recover from timeout exception.");
                  }
              }
          }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
