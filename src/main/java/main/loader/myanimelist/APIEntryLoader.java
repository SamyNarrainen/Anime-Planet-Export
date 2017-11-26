package main.loader.myanimelist;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Samy on 26/11/2017.
 */
public class APIEntryLoader {

    private final Document document;
    private final NodeList nodes;
    private int index = 0;

    public APIEntryLoader(String searchTerm, String authentication) throws IOException, ParserConfigurationException, org.xml.sax.SAXException {

        URL url = new URL("https://myanimelist.net/api/anime/search.xml?q=" + searchTerm.replace(" ", "%20"));
        String basicAuth = "Basic " + authentication;
        HttpURLConnection httpURLConnection = (HttpURLConnection)  url.openConnection();
        httpURLConnection.addRequestProperty("User-Agent", "Chrome");
        httpURLConnection.setRequestProperty("Authorization", basicAuth);
        BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

        String inputLine, contents = "";
        while ((inputLine = in.readLine()) != null) {
            contents += inputLine;
        }

        in.close();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        StringReader reader = new StringReader(contents);
        InputSource inputSource = new InputSource(reader);
        document = dBuilder.parse(inputSource);
        document.normalizeDocument();

        nodes = document.getElementsByTagName("entry");
    }

    public Node getNextRawEntry() {
        if(index < nodes.getLength()) {
            return nodes.item(index++);
        }
        return null;
    }

    public Element getNextElement() {
        Node node = getNextRawEntry();
        if(node != null && node.getNodeType() == Node.ELEMENT_NODE) {
            return (Element) node;
        }
        return null;
    }


}
