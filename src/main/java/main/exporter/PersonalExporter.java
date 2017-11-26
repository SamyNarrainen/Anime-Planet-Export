package main.exporter;

import com.samynarrainen.Data.Status;

/**
 * An exporter for data specific to a user, for example the rating they've given a show.
 */
public interface PersonalExporter {

    int getRating() throws Exception;
    Status getStatus() throws Exception;
    int getEpisodesWatched() throws Exception;
    /** The number of times an individual anime has been re-watched. */
    int getTimesWatched() throws Exception;

    default String prettyPrint() {
        StringBuilder sb = new StringBuilder();

        try {
            sb.append("Rating: " + getRating() + System.getProperty("line.separator"));
            sb.append("Status: " + getStatus() + System.getProperty("line.separator"));
            sb.append("Episodes Watched: " + getEpisodesWatched() + System.getProperty("line.separator"));
            sb.append("Times Watched: " + getTimesWatched() + System.getProperty("line.separator"));
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

}
