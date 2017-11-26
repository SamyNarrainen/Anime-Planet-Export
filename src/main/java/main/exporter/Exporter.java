package main.exporter;


import com.samynarrainen.Data.Type;

import java.util.List;


/**
 * Exports generic information from an entry, i.e information that isn't specific to a user.
 */
public interface Exporter {

    String getTitle() throws Exception;

    int getNumberOfEpisodes() throws Exception;

    String getURI() throws Exception;

    Type getType() throws Exception;

    List<String> getAlternativeTitles() throws Exception;

    String getStudio() throws Exception;

    int getStartYear() throws Exception;

    int getEndYear() throws Exception;

    //Season getSeason(String contents);

    default String prettyPrint() {
        StringBuilder sb = new StringBuilder();

        try {
            sb.append("Title: " + getTitle() + System.getProperty("line.separator"));
            sb.append("Episodes: " + getNumberOfEpisodes() + System.getProperty("line.separator"));
            sb.append("URL: " + getURI() + System.getProperty("line.separator"));
            sb.append("Type: " + getType() + System.getProperty("line.separator"));
            sb.append("Alternative Title: " + getAlternativeTitles() + System.getProperty("line.separator"));
            sb.append("Studio: " + getStudio() + System.getProperty("line.separator"));
            sb.append("Start Year: " + getStartYear() + System.getProperty("line.separator"));
            sb.append("End Year: " + getEndYear() + System.getProperty("line.separator"));
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
}
