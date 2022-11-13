package com.trashsoftware.ducksontranslator.db;

import java.io.Serializable;

import trashsoftware.duckSonTranslator.DuckSonTranslator;

public class HistoryItem implements Comparable<HistoryItem>, Serializable {
    long time;
    String coreVersion;
    String origText;
    String translatedText;
    String srcLang;
    String dstLang;
    boolean useBaseDict;
    boolean useSameSound;
    boolean isCq;
    String wordPickerName;

    transient private boolean expanded;  // 不存的属性，用于UI
    transient private boolean selected;

    public static HistoryItem createFromTranslator(String srcLang,
                                                   String origText,
                                                   String dstLang,
                                                   String translatedText,
                                                   DuckSonTranslator translator) {
        HistoryItem item = new HistoryItem();
        item.time = System.currentTimeMillis();
        item.coreVersion = translator.getCoreVersion() + "." + translator.getDictionaryVersion();
        item.origText = origText;
        item.translatedText = translatedText;
        item.srcLang = srcLang;
        item.dstLang = dstLang;
        item.useBaseDict = translator.isUseBaseDict();
        item.useSameSound = translator.isUseSameSoundChar();
        item.isCq = translator.isChongqingMode();
        item.wordPickerName = translator.getChsGegPicker().getFactory().name();
        return item;
    }

    @Override
    public int compareTo(HistoryItem o) {
        return -Long.compare(this.time, o.time);
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setSelected(boolean selected) {
        System.out.println(selected);
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public long getTime() {
        return time;
    }

    public String getCoreVersion() {
        return coreVersion;
    }

    public String getDstLang() {
        return dstLang;
    }

    public String getOrigText() {
        return origText;
    }

    public String getSrcLang() {
        return srcLang;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public String getWordPickerName() {
        return wordPickerName;
    }

    public boolean isCq() {
        return isCq;
    }

    public boolean isUseBaseDict() {
        return useBaseDict;
    }

    public boolean isUseSameSound() {
        return useSameSound;
    }
}
