package main.exporter.myanimelist;

import com.samynarrainen.Data.Status;
import com.samynarrainen.Data.Type;
import main.exporter.Exporter;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Samy on 26/11/2017.
 * Export directly from the MAL API
 */
public class MALAPIExporter implements Exporter {

    private final Element element;

    public MALAPIExporter(Element element) {
        this.element = element;
    }

    @Override
    public String getTitle() throws Exception {
        return getTextContext("title");
    }

    @Override
    public int getNumberOfEpisodes() throws Exception {
        String episodes = getTextContext("episodes");
        return episodes == null ? null : Integer.parseInt(episodes);
    }

    @Override
    public String getURI() throws Exception {
        return getTextContext("id");
    }

    @Override
    public Type getType() throws Exception {
        String type = getTextContext("type");
        return type == null ? null : Type.fromMALString(type);
    }

    @Override
    public List<String> getAlternativeTitles() throws Exception {
        String englishTitle = getTextContext("english");

        //TODO check if this can have multiple elements.
        String synonyms = getTextContext("synonyms");

        List<String> alternativeTitles = new ArrayList<String>();
        if(englishTitle != null) {
            alternativeTitles.add(englishTitle);
        }
        if(synonyms != null) {
            alternativeTitles.add(synonyms);
        }

        return alternativeTitles;
    }

    /**
     * There's no studio available from the MAL API :)
     */
    @Override
    public String getStudio() throws Exception {
        return null;
    }

    @Override
    public int getStartYear() throws Exception {
        String startDate = getTextContext("start_date");
        int startYear = -1;
        if(startDate != null) {
            startYear = Integer.parseInt(startDate.substring(0, 4));
        }
        return startYear;
    }

    @Override
    public int getEndYear() throws Exception {
        String endDate = getTextContext("end_date");
        int endYear = -1;
        if(endDate != null) {
            endYear = Integer.parseInt(endDate.substring(0, 4));
        }
        return endYear;
    }

    public String toString() {
        return prettyPrint();
    }

    private String getTextContext(String tagName) {
        NodeList nodeList = element.getElementsByTagName(tagName);
        if(nodeList.item(0) != null) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }
}
