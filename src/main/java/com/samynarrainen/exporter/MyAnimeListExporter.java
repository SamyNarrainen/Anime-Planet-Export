package com.samynarrainen.exporter;

import com.samynarrainen.domain.domain.animeplanet.MyAnimeListAnimeListEntry;
import com.samynarrainen.domain.domain.animeplanet.MyAnimeListAnimeStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class MyAnimeListExporter {

    private final List<MyAnimeListAnimeListEntry> entries;

    private final String exportFilePath;

    public MyAnimeListExporter(final List<MyAnimeListAnimeListEntry> entries, final String exportFilePath) {
        this.entries = entries;
        this.exportFilePath = exportFilePath;
    }

    public void export() {
        try {
            final StringBuilder sb = new StringBuilder();
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
            sb.append("<myanimelist>\n");

            sb.append("\t<myinfo>\n");
            sb.append("\t\t<user_export_type>1</user_export_type>\n");
            sb.append("\t\t<user_total_anime>" + entries.size() + "</user_total_anime>\n");
            sb.append("\t\t<user_total_watching>" + getCountByStatus(MyAnimeListAnimeStatus.WATCHING) + "</user_total_watching>\n");
            sb.append("\t\t<user_total_completed>" + getCountByStatus(MyAnimeListAnimeStatus.WATCHED) + "</user_total_completed>\n");
            sb.append("\t\t<user_total_onhold>" + getCountByStatus(MyAnimeListAnimeStatus.STALLED) + "</user_total_onhold>\n");
            sb.append("\t\t<user_total_dropped>" + getCountByStatus(MyAnimeListAnimeStatus.DROPPED) + "</user_total_dropped>\n");
            sb.append("\t\t<user_total_plantowatch>" + getCountByStatus(MyAnimeListAnimeStatus.WANT_TO_WATCH) + "</user_total_plantowatch>\n");
            sb.append("\t</myinfo>\n");
            for (final MyAnimeListAnimeListEntry entry : entries) {
                sb.append(entry.export());
            }
            sb.append("</myanimelist>\n");

            Files.write(Paths.get(exportFilePath), sb.toString().getBytes());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getCountByStatus(final MyAnimeListAnimeStatus status) {
        return (int) entries.stream().filter(entry -> entry.getStatus().equals(status)).count();
    }
}
