package main;

import main.exporter.AnimePlanetEntryExporter;
import main.loader.EntryLoader;
import main.loader.PageLoader;

import java.io.IOException;

/**
 * Created by Samy on 25/11/2017.
 * Primary entry point to the program
 */
public class AnimePlanetExport {

    public static void main(String[] args) {

        try {
            String page = PageLoader.getPage("08935730249582347503", 1);
            EntryLoader entryLoader = new EntryLoader(page);
            String rawEntry = entryLoader.getNextRawEntry();
            rawEntry = entryLoader.getNextRawEntry();
            rawEntry = entryLoader.getNextRawEntry();
            AnimePlanetEntryExporter entryExporter = new AnimePlanetEntryExporter(rawEntry);
            System.out.println(entryExporter);



        }
        catch(IOException e) {
            e.printStackTrace();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
