package com.trashsoftware.ducksontranslator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.navigation.NavigationView;
import com.trashsoftware.ducksontranslator.db.HistoryAccess;
import com.trashsoftware.ducksontranslator.db.HistoryItem;
import com.trashsoftware.ducksontranslator.dialogs.ChangelogDialog;
import com.trashsoftware.ducksontranslator.fragments.MainDictionaryFragment;
import com.trashsoftware.ducksontranslator.fragments.MainEncryptionFragment;
import com.trashsoftware.ducksontranslator.fragments.MainTranslateFragment;
import com.trashsoftware.ducksontranslator.model.MainViewModel;
import com.trashsoftware.ducksontranslator.util.LanguageCode;
import com.trashsoftware.ducksontranslator.util.LanguageUtil;

import trashsoftware.duckSonTranslator.translators.DuckSonTranslator;
import trashsoftware.duckSonTranslator.wordPickers.PickerFactory;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MAIN_ACTIVITY";
    private static final String DEFAULT_PICKER = PickerFactory.COMBINED_CHAR.name();
    private MainViewModel viewModel;

    private BottomNavigationView mainNavView;

    private NavigationView navigationView;
    private MenuItem appVersionItem, coreVersionItem;
    private DrawerLayout mainDrawer;
    private NavController navController;

    private MainTranslateFragment translateFragment;
    private MainDictionaryFragment dictionaryFragment;
    private MainEncryptionFragment encryptionFragment;

    private MaterialSwitch useBaseDictSwitch, homophoneSwitch, cqModeSwitch;
    // Result getter of history view
    ActivityResultLauncher<Intent> historyResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        HistoryItem item = (HistoryItem) data.getSerializableExtra("historyItem");
                        if (item != null) {
                            mainDrawer.closeDrawer(navigationView);

                            viewModel.recoverToHistory(item);
                            applyOptionsToUI();

                            switchToTranslateFragment();
                            translateFragment.reTranslate();
                        }
                    }
                }
            }
    );
    private SharedPreferences translatorPref;
    ActivityResultLauncher<Intent> settingsResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
//                System.out.println(PreferenceManager.getDefaultSharedPreferences(this).getAll());
                if (result.getResultCode() == SettingsActivity.RESULT_RELOAD) {
                    recreate();
                } else if (result.getResultCode() == RESULT_OK) {
                    restoreSettings();
                }
            }
    );
    private SharedPreferences versionPref;

    public static String getLangName(Context context, String langId) {
        return switch (langId) {
            case "chs" -> context.getString(R.string.chinese);
            case "geg" -> context.getString(R.string.geglish);
            case "chi" -> context.getString(R.string.chinglish);
            default -> "";
        };
    }

    public static String getWordPickerShownName(Context context, PickerFactory pf) {
        String[] names = context.getResources().getStringArray(R.array.word_picker_list);
        String[] values = context.getResources().getStringArray(R.array.word_picker_list_values);
        for (int i = 0; i < names.length; i++) {
            if (pf.name().equals(values[i])) {
                return names[i];
            }
        }
        return pf.name();
    }

    public void setTranslateFragment(MainTranslateFragment translateFragment) {
        this.translateFragment = translateFragment;
    }

    public void setDictionaryFragment(MainDictionaryFragment dictionaryFragment) {
        this.dictionaryFragment = dictionaryFragment;
    }

    public void setEncryptionFragment(MainEncryptionFragment encryptionFragment) {
        this.encryptionFragment = encryptionFragment;
    }

    private void switchToTranslateFragment() {
        navController.navigate(R.id.bot_navigation_trans);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
//        super.attachBaseContext(newBase);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(newBase);
        String localeCode = pref.getString("language_list", "");
        LanguageCode langCode = LanguageCode.fromLanCodeName(localeCode);
        Log.d(TAG, "Switched to " + langCode);
        super.attachBaseContext(LanguageUtil.getInstance().applyLanguage(newBase, langCode));
//        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = MainViewModel.getInstance();

        mainNavView = findViewById(R.id.main_nav_bot_bar);

        FragmentManager fragmentManager = getSupportFragmentManager();
        NavHostFragment navContainerView = (NavHostFragment) fragmentManager
                .findFragmentById(R.id.main_nav_container);
        assert navContainerView != null;
        navController = navContainerView.getNavController();

        // 启动
        NavigationUI.setupWithNavController(mainNavView, navController);

        translatorPref = getSharedPreferences("translatorPref", 0);
        versionPref = getSharedPreferences("versionPref", 0);

        mainDrawer = findViewById(R.id.mainDrawer);
        navigationView = findViewById(R.id.mainNavigation);

        appVersionItem = navigationView.getMenu().findItem(R.id.appVersionItem);
        coreVersionItem = navigationView.getMenu().findItem(R.id.coreVersionItem);

        setStatusBarColor(this,
                this,
                mainDrawer,
                getWindow());

        View header = navigationView.getHeaderView(0);

        // 方言
        cqModeSwitch = header.findViewById(R.id.cqModeSwitch);

        // 使用小词典
        useBaseDictSwitch = header.findViewById(R.id.baseDictSwitch);

        // 同音字
        homophoneSwitch = header.findViewById(R.id.homophoneSwitch);

        DuckSonTranslator translator = viewModel.getTranslator();  // 初始化
        String coreVersion = viewModel.getShownCoreVersion();

        restoreSettings();
        appVersionItem.setTitle(String.format(getString(R.string.app_version), BuildConfig.VERSION_NAME));
        coreVersionItem.setTitle(String.format(getString(R.string.core_version), coreVersion));

        initToggleSelection();

        MaterialToolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        ActionBarDrawerToggle actionBarDrawerToggle =
                new ActionBarDrawerToggle(this, mainDrawer, R.string.open, R.string.close);

        actionBarDrawerToggle.syncState();

        mainDrawer.addDrawerListener(actionBarDrawerToggle);

        useBaseDictSwitch.setOnCheckedChangeListener((v, checked) -> {
            translator.getOptions().setUseBaseDict(checked);
            savePref();
        });

        cqModeSwitch.setOnCheckedChangeListener((v, checked) -> {
            translator.getOptions().setChongqingMode(checked);
            savePref();
        });

        homophoneSwitch.setOnCheckedChangeListener((v, checked) -> {
            translator.getOptions().setUseSameSoundChar(checked);
            cqModeSwitch.setEnabled(checked);
            savePref();
        });

        HistoryAccess historyAccess = HistoryAccess.getInstance(this);

        checkIsFirstOpen();
    }

    @SuppressLint("ResourceAsColor")
    public static void setStatusBarColor(
            Context ctx,
            AppCompatActivity activity,
            View rootLayout,
            Window window) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {  // Android 15+
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());

                int top = insets.top;
                int bottom = insets.bottom;

                // Add top/bottom margin so content doesn’t go under system bars
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                params.topMargin = top;
                params.bottomMargin = bottom;
                v.setLayoutParams(params);

                // Create a fake status bar view
                View statusBarView = new View(ctx);
                statusBarView.setLayoutParams(
                        new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                top
                        )
                );
                statusBarView.setBackgroundColor(
                        ContextCompat.getColor(ctx, R.color.md_theme_surface)
                );

                activity.addContentView(statusBarView, statusBarView.getLayoutParams());

                return WindowInsetsCompat.CONSUMED;
            });
        } else {
            // For Android 14 and below
            window.setStatusBarColor(ContextCompat.getColor(ctx, R.color.md_theme_surface));
        }
    }

    public void translateAction(View view) {
        translateFragment.translate();
    }

    public void swapLanguageAction(View view) {
        translateFragment.swapLanguage();
    }

    public void clearUpTextAction(View view) {
        translateFragment.clearUpText();
    }

    public void copyDownTextAction(View view) {
        translateFragment.copyDownText();
    }

    public void searchDictAction(View view) {
        dictionaryFragment.search();
    }

    public void expandCollapseKeyFieldAction(View view) {
        encryptionFragment.expandCollapseKeyField();
    }

    public void expandCollapseEncryptFieldAction(View view) {
        encryptionFragment.expandCollapseEncryptField();
    }

    public void generateRSAKeysAction(View view) {
        encryptionFragment.generateRSAKeys();
    }

    public void encryptAction(View view) {
        encryptionFragment.encryptDecrypt();
    }

    public void copyPublicKeyAction(View view) {
        encryptionFragment.copyPublicKey();
    }

    public void sharePublicKeyAction(View view) {
        encryptionFragment.sharePublicKey();
    }

    public void applyPrivateKeyAction(View view) {
        encryptionFragment.applyPrivateKey();
    }

    public void clearEncryptInputAction(View view) {
        encryptionFragment.clearEncryptInput();
    }

    public void clearKeyInputAction(View view) {
        encryptionFragment.clearKeyInput();
    }

    public void copyEncryptOutputAction(View view) {
        encryptionFragment.copyEncryptOutput();
    }

    public void shareEncryptOutputAction(View view) {
        encryptionFragment.shareEncryptOutput();
    }

    public void expandEncryptInstructionAction(View view) {
        encryptionFragment.expandCollapseInstruction(true);
    }

    public void collapseEncryptInstructionAction(View view) {
        encryptionFragment.expandCollapseInstruction(false);
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
        DuckSonTranslator translator = viewModel.getTranslator();
        translator.getOptions().setUseBaseDict(translatorPref.getBoolean("useBaseDict", true));
        translator.getOptions().setChongqingMode(translatorPref.getBoolean("cqMode", true));
        translator.getOptions().setUseSameSoundChar(translatorPref.getBoolean("useSameSoundChar", true));
        if (!translator.getOptions().isUseSameSoundChar()) {
            cqModeSwitch.setEnabled(false);
        }

        String picker = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("word_picker", DEFAULT_PICKER);
        PickerFactory pf;
        try {
            pf = PickerFactory.valueOf(picker);
        } catch (EnumConstantNotPresentException e) {
            pf = PickerFactory.valueOf(DEFAULT_PICKER);
        }
        translator.getOptions().setPickerFactory(pf);
    }

    private void applyOptionsToUI() {
        initToggleSelection();
    }

    private void savePref() {
        SharedPreferences.Editor editor = translatorPref.edit();
        editor.putBoolean("useBaseDict", viewModel.getTranslator().getOptions().isUseBaseDict());
        editor.putBoolean("cqMode", viewModel.getTranslator().getOptions().isChongqingMode());
        editor.putBoolean("useSameSoundChar", viewModel.getTranslator().getOptions().isUseSameSoundChar());
//        editor.putInt("wordPickerIndex", wordPickerSpinner.getSelectedItemPosition());
        editor.apply();
    }

    private void initToggleSelection() {
        DuckSonTranslator translator = viewModel.getTranslator();
        cqModeSwitch.setChecked(translator.getOptions().isChongqingMode());

        useBaseDictSwitch.setChecked(translator.getOptions().isUseBaseDict());
        homophoneSwitch.setChecked(translator.getOptions().isUseSameSoundChar());
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

    public void historyAction(MenuItem view) {
        Intent intent = new Intent(this, HistoryActivity.class);
        historyResultLauncher.launch(intent);
    }

    public void settingsAction(MenuItem view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        settingsResultLauncher.launch(intent);
    }

    public void changelogAction(MenuItem view) {
        showUpdates();
    }
}