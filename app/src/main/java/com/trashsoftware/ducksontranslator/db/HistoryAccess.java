package com.trashsoftware.ducksontranslator.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class HistoryAccess extends SQLiteOpenHelper {

    public static final String DB_NAME = "history.db";
    public static final String TABLE_NAME = "history";
    private static final String TAG = "HISTORY";
    private static final int VERSION = 1;
    private static final String TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " +
            TABLE_NAME + "(time INTEGER, coreVersion TEXT, " +
            "srcLang TEXT, dstLang TEXT, origText TEXT, translatedText TEXT, " +
            "useBaseDict INTEGER, useSameSound INTEGER, isCq INTEGER, " +
            "wordPickerName TEXT);";

    private static HistoryAccess dbAccess;
//    private SQLiteDatabase db;

    public HistoryAccess(@Nullable Context context) {
        this(context, VERSION);
    }

    public HistoryAccess(@Nullable Context context, int version) {
        super(context, DB_NAME, null, version);

        try (SQLiteDatabase db = getWritableDatabase()) {
            db.execSQL(TABLE_CREATE);
        } catch (SQLException e) {
            Log.e(TAG, e.toString());
        }
    }

    public static HistoryAccess getInstance(Context context) {
        if (dbAccess == null) {
            dbAccess = new HistoryAccess(context);
        }
        return dbAccess;
    }

    public boolean patchDelete(List<HistoryItem> items) {
        String[] queryWhere = new String[items.size()];
        String[] clause = new String[items.size()];
        for (int i = 0 ; i < queryWhere.length; i++) {
            clause[i] = "?";
            queryWhere[i] = String.valueOf(items.get(i).time);
        }
        String clauseAll = String.join(", ", clause);

        try (SQLiteDatabase db = getWritableDatabase()) {
            int res = db.delete(TABLE_NAME,
                    "time IN (" + clauseAll + ")",
                    queryWhere);
            return res == queryWhere.length;
        } catch (SQLException e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    @Deprecated
    public boolean delete(HistoryItem item) {
        try (SQLiteDatabase db = getWritableDatabase()) {
            int res = db.delete(TABLE_NAME,
                    "time = ?",
                    new String[]{String.valueOf(item.time)});
            return res > 0;
        } catch (SQLException e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    @Deprecated
    public boolean deleteAll() {
        try (SQLiteDatabase db = getWritableDatabase()) {
            db.delete(TABLE_NAME,
                    null,
                    new String[]{});
            return true;
        } catch (SQLException e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }

    public void insert(HistoryItem item) {
        ContentValues values = new ContentValues();
        values.put("time", item.time);
        values.put("coreVersion", item.coreVersion);
        values.put("srcLang", item.srcLang);
        values.put("dstLang", item.dstLang);
        values.put("origText", item.origText);
        values.put("translatedText", item.translatedText);
        values.put("useBaseDict", item.useBaseDict);
        values.put("useSameSound", item.useSameSound);
        values.put("isCq", item.isCq);
        values.put("wordPickerName", item.wordPickerName);
        try (SQLiteDatabase db = getWritableDatabase()) {
            long res = db.insert(TABLE_NAME, "", values);
            if (res == -1) throw new SQLException("Database error: failed to insert");
        } catch (SQLException e) {
            Log.e(TAG, e.toString());
        }
    }

    public List<HistoryItem> getAll() {
        List<HistoryItem> items = new ArrayList<>();
        String query = "select * from history;";
        try (SQLiteDatabase db = getReadableDatabase()) {
            Cursor cursor = db.rawQuery(query, null);
            while (cursor.moveToNext()) {
                try {
                    HistoryItem item = new HistoryItem();
                    item.time = cursor.getLong(cursor.getColumnIndexOrThrow("time"));
                    item.coreVersion = cursor.getString(cursor.getColumnIndexOrThrow("coreVersion"));
                    item.srcLang = cursor.getString(cursor.getColumnIndexOrThrow("srcLang"));
                    item.dstLang = cursor.getString(cursor.getColumnIndexOrThrow("dstLang"));
                    item.origText = cursor.getString(cursor.getColumnIndexOrThrow("origText"));
                    item.translatedText = cursor.getString(cursor.getColumnIndexOrThrow("translatedText"));
                    item.useBaseDict = cursor.getInt(cursor.getColumnIndexOrThrow("useBaseDict")) > 0;
                    item.useSameSound = cursor.getInt(cursor.getColumnIndexOrThrow("useSameSound")) > 0;
                    item.isCq = cursor.getInt(cursor.getColumnIndexOrThrow("isCq")) > 0;
                    item.wordPickerName = cursor.getString(cursor.getColumnIndexOrThrow("wordPickerName"));

                    items.add(item);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, e.toString());
                }
            }
            cursor.close();
        }

        Collections.sort(items);
        return items;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
