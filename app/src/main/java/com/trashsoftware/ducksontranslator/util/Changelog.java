package com.trashsoftware.ducksontranslator.util;

public class Changelog {

    private String versionName;
    private String dateStr;
    private String content;

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setDateStr(String dateStr) {
        this.dateStr = dateStr;
    }

    public String getContent() {
        return content;
    }

    public String getDateStr() {
        return dateStr;
    }

    public String getVersionName() {
        return versionName;
    }
}
