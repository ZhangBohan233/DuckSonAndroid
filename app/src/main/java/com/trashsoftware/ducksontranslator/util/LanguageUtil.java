package com.trashsoftware.ducksontranslator.util;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.os.LocaleList;
import android.util.DisplayMetrics;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class LanguageUtil {
    public static final String TAG = "LanguageUtil";

    public static final SimpleDateFormat GEGLISH_DATE =
            new SimpleDateFormat("yyyy 'year' MM 'month' dd 'day'");
    public static final SimpleDateFormat GEGLISH_DATE_TIME =
            new SimpleDateFormat("yyyy 'year' MM 'month' dd 'day' HH:mm:ss");
    public static final SimpleDateFormat TIME_HH_MM_SS =
            new SimpleDateFormat("HH:mm:ss");
    public static final SimpleDateFormat TIME_HH_MM =
            new SimpleDateFormat("HH:mm");
    private volatile static LanguageUtil instance;

    private LanguageCode languageCode;
    private Locale locale;

    public static LanguageUtil getInstance() {
        if (instance == null) {
            synchronized (LanguageUtil.class){
                if (instance == null){
                    instance = new LanguageUtil();
                }
            }
        }

        return instance;
    }

    private void checkIfLocaleSet() {
        if (locale == null || languageCode == null) {
            Log.e(TAG, "Locale not set");
            locale = Locale.getDefault();
            languageCode = LanguageCode.DEFAULT;
        }
    }

    public DateFormat getDateInstance() {
        checkIfLocaleSet();
        if ("bg".equals(locale.getLanguage())) {
            return GEGLISH_DATE;
        } else {
            return SimpleDateFormat.getDateInstance(DateFormat.DEFAULT, locale);
        }
    }

    public DateFormat getDateTimeInstance() {
        checkIfLocaleSet();
        if ("bg".equals(locale.getLanguage())) {
            return GEGLISH_DATE_TIME;
        } else {
            return SimpleDateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, locale);
        }
    }

    public DateTimeFormatter getDateFormatter() {
        checkIfLocaleSet();
        if ("bg".equals(locale.getLanguage())) {
            return DateTimeFormatter.ofPattern(GEGLISH_DATE.toPattern());
        } else {
            return DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);
        }
    }

    public Context applyLanguage(Context context, LanguageCode language) {
        this.languageCode = language;
        if (language == LanguageCode.DEFAULT) {
            locale = Locale.getDefault();
        } else {
            locale = new Locale(language.systemLanCode);
        }

        Configuration configuration = context.getResources().getConfiguration();

        configuration.setLocale(locale);
        return context.createConfigurationContext(configuration);
    }
}
