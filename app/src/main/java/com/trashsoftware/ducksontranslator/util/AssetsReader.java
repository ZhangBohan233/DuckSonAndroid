package com.trashsoftware.ducksontranslator.util;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AssetsReader {

    public static List<String> readAssetsLinesAsList(Context context, String fileName)
            throws IOException {
        List<String> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(context.getAssets().open(fileName)))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    list.add(line);  // 这里不能trim
                }
            }
        }
        return list;
    }

    public static String readAssetsLinesAsString(Context context, String fileName)
            throws IOException {
        return String.join("\n", readAssetsLinesAsList(context, fileName));
    }
}
