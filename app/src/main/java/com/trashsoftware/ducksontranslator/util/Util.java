package com.trashsoftware.ducksontranslator.util;

public class Util {

    public static boolean equals(CharSequence cs1, CharSequence cs2) {
        if (cs1 == null) return cs2 == null;
        if (cs2 == null) return false;
        int len = cs1.length();
        if (len != cs2.length()) return false;

        for (int i = 0; i < len; i++) {
            if (cs1.charAt(i) != cs2.charAt(i)) return false;
        }
        return true;
    }

    public static <T> boolean arrayContains(T[] array, Object item) {
        for (T t : array) {
            if (t == item) return true;
        }
        return false;
    }
}
