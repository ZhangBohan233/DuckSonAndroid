package com.trashsoftware.ducksontranslator.util;

import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChangelogReader {

    private static ChangelogReader reader;
    private final Changelog[] changelogs;

    private ChangelogReader(Context context) throws IOException {
        String fileContent = AssetsReader.readAssetsLinesAsString(context, "changelog.txt");
        String[] versions = fileContent.split("==========\n");

        List<Changelog> changelogList = new ArrayList<>();
        for (String ver : versions) {
            if (!ver.trim().isEmpty()) {
                String[] lines = ver.split("\n");
                Changelog changelog = new Changelog();
                changelog.setVersionName(lines[0]);
                changelog.setDateStr(lines[1]);
                StringBuilder contentBuilder = new StringBuilder();
                for (int i = 2; i < lines.length; i++) {
                    contentBuilder.append(lines[i]).append('\n');
                }
                changelog.setContent(contentBuilder.toString());
                changelogList.add(changelog);
            }
        }
        changelogs = changelogList.toArray(new Changelog[0]);
    }

    public static ChangelogReader getInstance(Context context) throws IOException {
        if (reader == null) {
            reader = new ChangelogReader(context);
        }
        return reader;
    }

    public Changelog getMostRecent() {
        return changelogs[0];
    }

    /**
     * Returns an array of all available changelogs, sorted.
     */
    public Changelog[] getAllChangelogs() {
        return changelogs;
    }
}
