package com.samynarrainen.domain.domain.animeplanet;

import com.samynarrainen.Data.Status;
import com.samynarrainen.domain.ListEntry;
import com.samynarrainen.domain.ListEntryStatus;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyAnimeListAnimeListEntry implements ListEntry {

    /**
     * Date format used by MAL.
     */
    private final SimpleDateFormat MAL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private final int id;
    private final String name;
    private final ListEntryStatus status;
    private final Date startDate;
    private final Date endDate;
    private final int rating;
    private final int timesWatched;
    private final int episodesWatched;

    public MyAnimeListAnimeListEntry(final int id) {
        this(id, null, null, null, null, -1, -1, -1);
    }

    public MyAnimeListAnimeListEntry(final int id, final AnimePlanetAnimeListEntry animePlanetAnimeListEntry) {
        this(id, null, MyAnimeListAnimeStatus.convert(animePlanetAnimeListEntry.getStatus()),
                animePlanetAnimeListEntry.getStartedDate(),
                animePlanetAnimeListEntry.getCompletedDate(),
                animePlanetAnimeListEntry.getRating(),
                animePlanetAnimeListEntry.getTimesWatched(),
                animePlanetAnimeListEntry.getEpisodesWatched());
    }

    public MyAnimeListAnimeListEntry(final int id, final String name, final ListEntryStatus status,
                                     final Date startDate, final Date endDate, final int rating, final int timesWatched,
                                     final int episodesWatched) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.rating = rating;
        this.timesWatched = timesWatched;
        this.episodesWatched = episodesWatched;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ListEntryStatus getStatus() {
        return status;
    }

    @Override
    public Date getStartedDate() {
        return startDate;
    }

    @Override
    public Date getCompletedDate() {
        return endDate;
    }

    @Override
    public int getRating() {
        return rating;
    }

    public int getTimesWatched() {
        return timesWatched;
    }

    public int getEpisodesWatched() {
        return episodesWatched;
    }

    public String export() {
        final StringBuilder sb = new StringBuilder();
        sb.append("\t<anime>\n");
        sb.append("\t\t<series_animedb_id>" + id + "</series_animedb_id>\n");
        sb.append("\t\t<update_on_import>1</update_on_import>\n");

        if (episodesWatched >= 0) {
            sb.append("\t\t<my_watched_episodes>" + episodesWatched + "</my_watched_episodes>\n");
        }

        if (rating >= 0) {
            sb.append("\t\t<my_score>" + rating + "</my_score>\n");
        }

        if (startDate != null) {
            sb.append("\t\t<my_start_date>" + MAL_DATE_FORMAT.format(startDate) + "</my_start_date>\n");
        }

        if (status != null) {
            sb.append("\t\t<my_status>" + status.getName() + "</my_status>\n");

            if (status.isWatched()) {
                //'finish date' only makes sense if the show is finished.
                if (endDate != null) {
                    sb.append("\t\t<my_finish_date>" + MAL_DATE_FORMAT.format(endDate) + "</my_finish_date>\n");
                }

                //Accounting for number of times re-watched, so -1 for for the initial viewing.
                sb.append("\t\t<my_rewatching>" + (timesWatched - 1) +"</my_rewatching>\n");
            }
        }

        sb.append("\t</anime>\n");
        return sb.toString();
    }
}
