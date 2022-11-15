package com.trashsoftware.ducksontranslator;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.trashsoftware.ducksontranslator.db.HistoryAccess;
import com.trashsoftware.ducksontranslator.db.HistoryItem;
import com.trashsoftware.ducksontranslator.dialogs.ChangelogDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import trashsoftware.duckSonTranslator.DuckSonTranslator;
import trashsoftware.duckSonTranslator.wordPickerChsGeg.PickerFactory;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MAIN_ACTIVITY";
    final List<WordPickerItem> pickerList = List.of(
            new WordPickerItem(this, PickerFactory.COMBINED_CHAR, true),
            new WordPickerItem(this, PickerFactory.COMMON_PREFIX_CHAR),
            new WordPickerItem(this, PickerFactory.INVERSE_FREQ_CHAR),
            new WordPickerItem(this, PickerFactory.RANDOM_CHAR)
    );
    DuckSonTranslator translator;
    private HistoryAccess historyAccess;
    private TextInputEditText editTextUp, editTextDown;
    private Spinner lang1Spinner, lang2Spinner;
    private ArrayAdapter<LanguageItem> lang1Adapter, lang2Adapter;
    private boolean autoDetect = false;
    private NavigationView navigationView;
    private MenuItem appVersionItem, coreVersionItem;
    private DrawerLayout mainDrawer;
    private ScrollView mainScrollView;
    private MaterialButtonToggleGroup dialectGroup;
    private MaterialButton cqToggle, mandarinToggle;
    private SwitchMaterial useBaseDictSwitch;
    //    private MaterialButton homophoneYesToggle, homophoneNoToggle;
    private SwitchMaterial homophoneSwitch;
    private Spinner wordPickerSpinner;
    private ArrayAdapter<WordPickerItem> wordPickerAdapter;
    ActivityResultLauncher<Intent> historyResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        HistoryItem item = (HistoryItem) data.getSerializableExtra("historyItem");
                        if (item != null) {
                            mainDrawer.closeDrawer(navigationView);

                            translator.setUseBaseDict(item.isUseBaseDict());
                            translator.setUseSameSoundChar(item.isUseSameSound());
                            translator.setChongqingMode(item.isCq());
                            translator.setChsGegPicker(PickerFactory.valueOf(item.getWordPickerName()));

                            applyOptionsToUI();

                            selectByValue(item.getSrcLang(), lang1Spinner);
                            selectByValue(item.getDstLang(), lang2Spinner);

                            editTextUp.setText(item.getOrigText());
                            translate();
                        }
                    }
                }
            }
    );
    private SharedPreferences translatorPref;
    private SharedPreferences versionPref;

    public static String getLangName(Context context, String langId) {
        switch (langId) {
            case "chs":
                return context.getString(R.string.chinese);
            case "geg":
                return context.getString(R.string.geglish);
            default:
                return "";
        }
    }

    public static int getWordPickerShownName(PickerFactory factory) {
        switch (factory) {
            case COMMON_PREFIX_CHAR:
                return R.string.word_picker_substring;
            case COMBINED_CHAR:
                return R.string.word_picker_combined;
            case RANDOM_CHAR:
                return R.string.word_picker_random;
            case INVERSE_FREQ_CHAR:
                return R.string.word_picker_freq;
            default:
                throw new RuntimeException();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        translatorPref = getSharedPreferences("translatorPref", 0);
        versionPref = getSharedPreferences("versionPref", 0);

        mainDrawer = findViewById(R.id.mainDrawer);
        mainScrollView = findViewById(R.id.main_scroller);

        editTextUp = findViewById(R.id.textBoxUp);
        editTextDown = findViewById(R.id.textBoxDown);

        setScrollListeners();

        navigationView = findViewById(R.id.mainNavigation);

        appVersionItem = navigationView.getMenu().findItem(R.id.appVersionItem);
        coreVersionItem = navigationView.getMenu().findItem(R.id.coreVersionItem);

        View header = navigationView.getHeaderView(0);

        // 方言
        dialectGroup = header.findViewById(R.id.dialectToggleGroup);
        cqToggle = header.findViewById(R.id.dialectCqBtn);
        mandarinToggle = header.findViewById(R.id.dialectMandarinBtn);

        // 使用小词典
        useBaseDictSwitch = header.findViewById(R.id.baseDictSwitch);

        // 同音字
        homophoneSwitch = header.findViewById(R.id.homophoneSwitch);

        try {
            long t0 = System.currentTimeMillis();
            translator = new DuckSonTranslator();
            Log.v(TAG, "Translator launch time: " + (System.currentTimeMillis() - t0));
            restoreSettings();
            appVersionItem.setTitle(String.format(getString(R.string.app_version), BuildConfig.VERSION_NAME));
            coreVersionItem.setTitle(String.format(getString(R.string.core_version),
                    translator.getCoreVersion() + "." + translator.getDictionaryVersion()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        initToggleSelection();

        wordPickerSpinner = header.findViewById(R.id.wordPickerSpinner);
        initWordPickerSpinner();

        // 语言设置
        lang1Spinner = findViewById(R.id.lang1Spinner);
        lang2Spinner = findViewById(R.id.lang2Spinner);

        List<LanguageItem> lang1List = new ArrayList<>(List.of(
                new LanguageItem(this, "", R.string.auto_detect, true),
                new LanguageItem(this, "chs", R.string.chinese, false),
                new LanguageItem(this, "geg", R.string.geglish, false)
        ));
        List<LanguageItem> lang2List = new ArrayList<>(List.of(
                new LanguageItem(this, "chs", R.string.chinese, false),
                new LanguageItem(this, "geg", R.string.geglish, false)
        ));

        lang1Adapter = new ArrayAdapter<>(this, R.layout.lang_spinner_item, lang1List);
        lang1Adapter.setDropDownViewResource(R.layout.lang_spinner_dropdiwn_item);
        lang1Spinner.setAdapter(lang1Adapter);

        lang2Adapter = new ArrayAdapter<>(this, R.layout.lang_spinner_item, lang2List);
        lang2Adapter.setDropDownViewResource(R.layout.lang_spinner_dropdiwn_item);
        lang2Spinner.setAdapter(lang2Adapter);

        addSpinnerChangeListener();
        addTextChangeListener();

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.app_bar_name);

        ActionBarDrawerToggle actionBarDrawerToggle =
                new ActionBarDrawerToggle(this, mainDrawer, R.string.open, R.string.close);

        actionBarDrawerToggle.syncState();

        mainDrawer.addDrawerListener(actionBarDrawerToggle);

        useBaseDictSwitch.setOnCheckedChangeListener((v, checked) -> {
            translator.setUseBaseDict(checked);
            savePref();
        });

        homophoneSwitch.setOnCheckedChangeListener((v, checked) -> {
            translator.setUseSameSoundChar(checked);
            cqToggle.setEnabled(checked);
            mandarinToggle.setEnabled(checked);
            savePref();
        });

        cqToggle.setOnClickListener(v -> {
            translator.setChongqingMode(true);
            savePref();
        });
        mandarinToggle.setOnClickListener(v -> {
            translator.setChongqingMode(false);
            savePref();
        });

        historyAccess = HistoryAccess.getInstance(this);

        checkIsFirstOpen();
    }

    private void checkIsFirstOpen() {
        int lastOpenVersion = versionPref.getInt("lastOpenVersion", 0);
        int currentVersion = BuildConfig.VERSION_CODE;
        Log.i(TAG, String.format("Current version %d, last open version %d",
                currentVersion, lastOpenVersion));
//        showUpdates();
        if (currentVersion != lastOpenVersion) {
            showUpdates();
            SharedPreferences.Editor editor = versionPref.edit();
            editor.putInt("lastOpenVersion", currentVersion);
            editor.apply();
        }
    }

    private void showUpdates() {
        ChangelogDialog dialog = new ChangelogDialog(this);

        dialog.show();
    }

    private void restoreSettings() {
        translator.setUseBaseDict(translatorPref.getBoolean("useBaseDict", true));
        translator.setChongqingMode(translatorPref.getBoolean("cqMode", true));
        translator.setUseSameSoundChar(translatorPref.getBoolean("useSameSoundChar", true));
        if (!translator.isUseSameSoundChar()) {
            cqToggle.setEnabled(false);
            mandarinToggle.setEnabled(false);
        }

        translator.setChsGegPicker(pickerList.get(translatorPref.getInt("wordPickerIndex", 0)).pickerFactory);
    }

    private void applyOptionsToUI() {
        initToggleSelection();

        PickerFactory current = translator.getChsGegPicker().getFactory();
        for (int i = 0; i < wordPickerAdapter.getCount(); i++) {
            if (wordPickerAdapter.getItem(i).pickerFactory == current) {
                wordPickerSpinner.setSelection(i);
                break;
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setScrollListeners() {
        // 让焦点在text时scrollview不滑动，焦点不在时才滑动
        mainScrollView.setOnTouchListener((v, event) -> {
            editTextUp.getParent().requestDisallowInterceptTouchEvent(false);
            editTextDown.getParent().requestDisallowInterceptTouchEvent(false);
            return false;
        });

        editTextUp.setOnTouchListener((v, event) -> {
            editTextUp.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        editTextDown.setOnTouchListener((v, event) -> {
            editTextDown.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });
    }

    private void savePref() {
        SharedPreferences.Editor editor = translatorPref.edit();
        editor.putBoolean("useBaseDict", translator.isUseBaseDict());
        editor.putBoolean("cqMode", translator.isChongqingMode());
        editor.putBoolean("useSameSoundChar", translator.isUseSameSoundChar());
        editor.putInt("wordPickerIndex", wordPickerSpinner.getSelectedItemPosition());
        editor.apply();
    }

    private void initWordPickerSpinner() {
        // 借一下spinner_item不过分吧？
        wordPickerAdapter = new ArrayAdapter<>(this, R.layout.lang_spinner_item, pickerList);
        wordPickerAdapter.setDropDownViewResource(R.layout.lang_spinner_dropdiwn_item);
        wordPickerSpinner.setAdapter(wordPickerAdapter);

        PickerFactory current = translator.getChsGegPicker().getFactory();
        for (int i = 0; i < wordPickerAdapter.getCount(); i++) {
            if (wordPickerAdapter.getItem(i).pickerFactory == current) {
                wordPickerSpinner.setSelection(i);
                break;
            }
        }

        wordPickerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                WordPickerItem selected = wordPickerAdapter.getItem(position);
                if (selected != null) {
                    translator.setChsGegPicker(selected.pickerFactory);
                    savePref();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initToggleSelection() {
        if (translator.isChongqingMode()) {
            dialectGroup.check(R.id.dialectCqBtn);
        } else {
            dialectGroup.check(R.id.dialectMandarinBtn);
        }

        useBaseDictSwitch.setChecked(translator.isUseBaseDict());
        homophoneSwitch.setChecked(translator.isUseSameSoundChar());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (navigationView.isShown()) {
                    mainDrawer.closeDrawer(navigationView);
                } else {
                    mainDrawer.openDrawer(navigationView);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addSpinnerChangeListener() {
        lang1Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                LanguageItem sl = lang1Adapter.getItem(i);
                autoDetect = sl.isAutoDetect;
                updateLangSpinners(editTextUp.getText().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void addTextChangeListener() {
        editTextUp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                updateLangSpinners(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void updateLangSpinners(String editorText) {
        if (editorText.length() > 0) {
            if (autoDetect) {
                String lang = translator.autoDetectLanguage(editorText);
                autoChangeDstLang(lang);
                lang1Adapter.getItem(0).langId = lang;
                lang1Adapter.notifyDataSetChanged();
            }
        }
    }

    private void autoChangeDstLang(String srcLangCode) {
        if ("chs".equals(srcLangCode)) {
            lang2Spinner.setSelection(1);
        } else if ("geg".equals(srcLangCode)) {
            lang2Spinner.setSelection(0);
        }
    }

    public void translateAction(View view) {
        translate();
    }

    private void translate() {
        LanguageItem src = (LanguageItem) lang1Spinner.getSelectedItem();
        LanguageItem dst = (LanguageItem) lang2Spinner.getSelectedItem();

        String input = Objects.requireNonNull(editTextUp.getText()).toString();
        if (input.trim().isEmpty()) {
            editTextDown.setText("");
            return;
        }

        String srcLang;
        if (src.isAutoDetect) {
            if (src.langId.length() > 0) {
                srcLang = src.langId;
            } else {
                srcLang = translator.autoDetectLanguage(input);
                src.langId = srcLang;  // 顺便给它设置了
            }
        } else {
            srcLang = src.langId;
        }
        String dstLang = dst.langId;
        editTextDown.setTextColor(Color.GRAY);
        editTextDown.setText(R.string.translating);

        Thread thread = new Thread(() -> {
            String output;
            long t0 = System.currentTimeMillis();
            if ("chs".equals(srcLang) && "geg".equals(dstLang)) {
                output = translator.chsToGeglish(input);
            } else if ("geg".equals(srcLang) && "chs".equals(dstLang)) {
                output = translator.geglishToChs(input);
            } else {
                output = input;
            }
            Log.v(TAG, "Translation time: " + (System.currentTimeMillis() - t0));

            historyAccess.insert(HistoryItem.createFromTranslator(
                    srcLang,
                    input,
                    dstLang,
                    output,
                    translator
            ));
            runOnUiThread(() -> {
                editTextDown.setTextColor(Color.BLACK);
                editTextDown.setText(output);
            });
        });
        thread.start();
    }

    public void swapLanguageAction(View view) {
        Editable editable = editTextDown.getText();
        if (editable == null) return;
        String down = editable.toString();
        LanguageItem src = (LanguageItem) lang1Spinner.getSelectedItem();
        LanguageItem dst = (LanguageItem) lang2Spinner.getSelectedItem();

        String srcLang = src.langId;
        if (srcLang.isEmpty()) {
            srcLang = dst.langId.equals("chs") ? "geg" : "chs";
        }

        editTextUp.setText(down);

        selectByValue(dst.langId, lang1Spinner);
        selectByValue(srcLang, lang2Spinner);
        translate();
    }

    public void copyDownTextAction(View view) {
        Editable editable = editTextDown.getText();
        if (editable == null) {
            Toast.makeText(this, R.string.nothing_to_copy, Toast.LENGTH_SHORT).show();
            return;
        }
        String down = editable.toString();
        if (down.trim().isEmpty()) {
            Toast.makeText(this, R.string.nothing_to_copy, Toast.LENGTH_SHORT).show();
            return;
        }

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(down, down);
        clipboard.setPrimaryClip(clipData);

        Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
    }

    public void historyAction(MenuItem view) {
        Intent intent = new Intent(this, HistoryActivity.class);
        historyResultLauncher.launch(intent);
    }

    public void changelogAction(MenuItem view) {
        showUpdates();
    }

    private void selectByValue(String langCode, Spinner spinner) {
        for (int i = 0; i < spinner.getAdapter().getCount(); i++) {
            LanguageItem item = (LanguageItem) spinner.getAdapter().getItem(i);
            if (item.langId.equals(langCode)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    public static class LanguageItem {
        public final int resId;
        public final boolean isAutoDetect;
        final Context context;
        String langId;

        LanguageItem(Context context, String langId, int resId, boolean isAutoDetect) {
            this.context = context;
            this.langId = langId;
            this.resId = resId;
            this.isAutoDetect = isAutoDetect;
        }

        @NonNull
        @Override
        public String toString() {
            String base = context.getString(resId);
            if (isAutoDetect) {
                String langName = getLangName(context, langId);
                if (langName.length() > 0) return base + "(" + langName + ")";
                else return base;
            } else {
                return base;
            }
        }
    }

    public static class WordPickerItem {
        final Context context;
        final int resId;
        final PickerFactory pickerFactory;
        final boolean recommended;

        WordPickerItem(Context context, PickerFactory pickerFactory) {
            this(context, pickerFactory, false);
        }

        WordPickerItem(Context context, PickerFactory pickerFactory, boolean recommended) {
            this.context = context;
            this.resId = getWordPickerShownName(pickerFactory);
            this.pickerFactory = pickerFactory;
            this.recommended = recommended;
        }

        @NonNull
        @Override
        public String toString() {
            String s = context.getString(resId);
            if (recommended) {
                return s + '(' + context.getString(R.string.recommended) + ')';
            } else {
                return s;
            }
        }
    }
}