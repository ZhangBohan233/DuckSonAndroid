package com.trashsoftware.ducksontranslator.fragments;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.trashsoftware.ducksontranslator.R;
import com.trashsoftware.ducksontranslator.SettingsActivity;

import java.util.Locale;
import java.util.Objects;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {
    public static final String TAG = "SettingsFragment";

    ListPreference languageList;

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.pref_settings, rootKey);

        languageList = findPreference("language_list");

        initView();

//        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    private void initView() {
        String[] languages = getResources().getStringArray(R.array.language_list);
        Locale systemLocale = Locale.getDefault();
        String sysDefault = getString(R.string.language_system, systemLocale.getDisplayLanguage());
        languages[0] = sysDefault;
        languageList.setEntries(languages);

        languageList.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
        if (preference.getKey().equals("language_list")) {
            Log.i(TAG, "Language changed");
            ((SettingsActivity) requireActivity()).setNeedReload(true);
        }
        return true;
    }
}