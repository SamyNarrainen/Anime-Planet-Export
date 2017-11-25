package main.exporter;


import com.samynarrainen.Data.Status;
import com.samynarrainen.Data.Type;

import java.util.List;


public interface Exporter {

    String getTitle() throws Exception;

    int getRating() throws Exception;

    Status getStatus() throws Exception;

    int getNumberOfEpisodes() throws Exception;

    int getEpisodesWatched() throws Exception;

    String getURI() throws Exception;

    /** The number of times an individual anime has been re-watched. */
    int getTimesWatched() throws Exception;

    Type getType() throws Exception;

    List<String> getAlternativeTitles() throws Exception;

    String getStudio() throws Exception;

    int getStartYear() throws Exception;

    int getEndYear() throws Exception;

    //Season getSeason(String contents);
}
