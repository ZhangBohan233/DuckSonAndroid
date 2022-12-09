package com.trashsoftware.ducksontranslator.model;

import android.text.style.BackgroundColorSpan;
import android.util.Log;

import com.trashsoftware.ducksontranslator.db.HistoryItem;
import com.trashsoftware.ducksontranslator.fragments.MainTranslateFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import trashsoftware.duckSonTranslator.TranslatorOptions;
import trashsoftware.duckSonTranslator.result.TranslationResult;
import trashsoftware.duckSonTranslator.translators.DuckSonTranslator;
import trashsoftware.duckSonTranslator.wordPickers.PickerFactory;
import trashsoftware.duckSonTranslator.words.DuckSonDictionary;
import trashsoftware.duckSonTranslator.words.WordResult;

public class MainViewModel {
    public static final String TAG = "MAIN_VIEW_MODEL";
    private static MainViewModel instance;
    public boolean dictSearched;
    public int dictLangSpinnerIndex;
    public List<WordResult> wordResults;
    public int transSrcLangSpinnerIndex;
    public int transDstLangSpinnerIndex;
    public List<BackgroundColorSpan> transUpTextSpans = new ArrayList<>();
    public String plainOrigText;
    private DuckSonTranslator translator;
    private DuckSonDictionary dictionary;
    private String shownCoreVersion;
    private TranslationResult translationResult;

    public static MainViewModel getInstance() {
        if (instance == null) {
            instance = new MainViewModel();
        }
        return instance;
    }

    public void recoverToHistory(HistoryItem item) {
        translator.getOptions().setUseBaseDict(item.isUseBaseDict());
        translator.getOptions().setUseSameSoundChar(item.isUseSameSound());
        translator.getOptions().setChongqingMode(item.isCq());
        translator.getOptions().setPickerFactory(PickerFactory.valueOf(item.getWordPickerName()));

        plainOrigText = item.getOrigText();

        transSrcLangSpinnerIndex = MainTranslateFragment.srcLangIndex(item.getSrcLang());
        transDstLangSpinnerIndex = MainTranslateFragment.dstLangIndex(item.getDstLang());
    }

    public TranslationResult getTranslationResult() {
        return translationResult;
    }

    public void setTranslationResult(TranslationResult translationResult) {
        this.translationResult = translationResult;
    }

    public DuckSonTranslator getTranslator() {
        if (translator == null) {
            long t0 = System.currentTimeMillis();
            try {
                translator = new DuckSonTranslator();
                shownCoreVersion = translator.getCoreVersion() + "." + translator.getDictionaryVersion();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.v(TAG, "Translator launch time: " + (System.currentTimeMillis() - t0));
        }
        return translator;
    }

    public DuckSonDictionary getDictionary() {
        if (dictionary == null) {
            try {
                long t0 = System.currentTimeMillis();
                dictionary = new DuckSonDictionary(TranslatorOptions.getInstance());
                Log.v(TAG, "Dictionary launch time: " + (System.currentTimeMillis() - t0));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return dictionary;
    }

    public String getShownCoreVersion() {
        return shownCoreVersion;
    }
}
