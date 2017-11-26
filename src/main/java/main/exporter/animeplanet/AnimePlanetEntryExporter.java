package main.exporter.animeplanet;

import com.samynarrainen.Data.Status;
import com.samynarrainen.Data.Type;
import main.exporter.Exporter;
import main.exporter.PersonalExporter;
import main.loader.animeplanet.EntryLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Scrapes information from an individual entry available on an Anime Planet account's list.
 * To get an entry, see {@link EntryLoader}.
 */
public class AnimePlanetEntryExporter implements Exporter, PersonalExporter {

    /**
     * The entry scraped from {@link EntryLoader} that this exporter is reading from.
     */
    private final String rawEntry;

    /**
     * Patterns
     * @note use an or condition for quotations as Anime Planet isn't consistent with their usage of " and '.
     */
    private static final Pattern patternTitle = Pattern.compile("<h5>(.*?)</h5");
    private static final Pattern patternRating = Pattern.compile("statusArea.*?Rating\">(.*?)<");
    private static final Pattern patternStatus = Pattern.compile("statusArea.*?status(.)");
    private static final Pattern patternNumberOfEpisodes = Pattern.compile("class='type'>.*?(\\d+).*?<");
    private static final Pattern patternEpisodesWatched = Pattern.compile("statusArea\"><span class=[\"|']status[2|3|5][\"|']></span>.*?(\\d+).*?<");
    private static final Pattern patternURL = Pattern.compile("href=\"/anime/(.*?)\"");
    private static final Pattern patternTimesWatched = Pattern.compile("Watched.*?(\\d.*?)x");
    private static final Pattern patternType = Pattern.compile("class=[\"|']type[\"|']>(.*?) ");
    private static final Pattern patternAltTitle = Pattern.compile("Alt title: (.*?)<");
    private static final Pattern patternStudio = Pattern.compile("type.*?<li>(.*?)</li>");
    private static final Pattern patternStartYear = Pattern.compile("iconYear[\"|']>.*?(\\d+)");
    private static final Pattern patternEndYear = Pattern.compile("iconYear[\"|']>.*?-.*?(\\d+)<");
    /**
     * Specifically for when there are multiple alternative titles available.
     * In this case, {@link #patternAltTitle} will fail to match.
     */
    private static final Pattern patternAltTitles = Pattern.compile("Alt titles: (.*?)<");


    /**
     * @param rawEntry an entry scraped from {@link EntryLoader}
     */
    public AnimePlanetEntryExporter(String rawEntry) {
        this.rawEntry = rawEntry;

        System.out.println(rawEntry);
    }

    @Override
    public String getTitle() throws Exception {
        return parseEssentialPattern(patternTitle, rawEntry);
    }

    @Override
    public int getRating() throws Exception {
        Matcher matcher = patternRating.matcher(rawEntry);
        if(matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return -1;
    }

    @Override
    public Status getStatus() throws Exception {
        return Status.get(parseEssentialPattern(patternStatus, rawEntry));
    }

    @Override
    public int getNumberOfEpisodes() throws Exception {
        Matcher matcher = patternNumberOfEpisodes.matcher(rawEntry);
        if(matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return -1;
    }

    @Override
    public int getEpisodesWatched() throws Exception {
        Matcher matcher = patternEpisodesWatched.matcher(rawEntry);
        if(matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return -1;
    }

    @Override
    public String getURI() throws Exception {
        return parseEssentialPattern(patternURL, rawEntry);
    }

    @Override
    public int getTimesWatched() throws Exception {
        Matcher matcher = patternTimesWatched.matcher(rawEntry);
        if(matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return -1;
    }

    @Override
    public Type getType() throws Exception {
        Matcher matcher = patternType.matcher(rawEntry);
        if(matcher.find()) {
            return Type.fromAnimePlanetString(matcher.group(1));
        }
        return null;
    }

    @Override
    public List<String> getAlternativeTitles() throws Exception {
        List<String> alternativeTitles = new ArrayList<String>();

        Matcher matcher =  patternAltTitle.matcher(rawEntry);
        if(matcher.find()) {
            alternativeTitles.add(matcher.group(1));
        }
        else {
            //Perhaps this entry has multiple alternative titles...
            //e.g attack-on-titan-no-regrets
            matcher = patternAltTitles.matcher(rawEntry);
            if(matcher.find()) {
                alternativeTitles.addAll(Arrays.asList(matcher.group(1).split(",")));
            }
        }

        return alternativeTitles;
    }

    @Override
    public String getStudio() throws Exception {
       return parsePattern(patternStudio, rawEntry);
    }

    @Override
    public int getStartYear() throws Exception {
        String result = parsePattern(patternStartYear, rawEntry);
        return result != null ? Integer.parseInt(result) : -1;
    }

    @Override
    public int getEndYear() throws Exception {
        String result = parsePattern(patternEndYear, rawEntry);
        return result != null ? Integer.parseInt(result) : -1;
    }

    private String parseEssentialPattern(Pattern pattern, String input) throws Exception {
        String result = parsePattern(pattern, input);
        if(result != null) {
            return result;
        }
        throw new Exception("Couldn't read...");
    }

    private String parsePattern(Pattern pattern, String input) {
        Matcher matcher = pattern.matcher(input);
        if(matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    @Override
    public String prettyPrint() {
        return toString();
    }

    public String toString() {
        return Exporter.super.prettyPrint() + System.getProperty("line.separator") + PersonalExporter.super.prettyPrint();
    }
}
