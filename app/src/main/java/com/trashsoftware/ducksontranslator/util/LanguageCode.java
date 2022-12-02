package com.trashsoftware.ducksontranslator.util;

import java.util.Locale;

public enum LanguageCode {
    DEFAULT(""),
    CHS("zh"),
    ENG("en"),
    GEG("bg");

    public final String systemLanCode;

    LanguageCode(String systemLanCode) {
        this.systemLanCode = systemLanCode;
    }

    public static LanguageCode fromLocale(Locale locale) {
        String s = locale.getCountry();
        for (LanguageCode lc : values()) {
            if (lc.systemLanCode.equals(s)) return lc;
        }
        return ENG;
    }

    public static LanguageCode fromLanCodeName(String s) {
        try {
            return valueOf(s);
        } catch (IllegalArgumentException e) {
            return DEFAULT;
        }
    }
}
