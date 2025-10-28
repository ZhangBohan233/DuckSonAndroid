package com.trashsoftware.ducksontranslator

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.navigation.NavigationView
import com.trashsoftware.ducksontranslator.db.HistoryAccess
import com.trashsoftware.ducksontranslator.db.HistoryItem
import com.trashsoftware.ducksontranslator.dialogs.ChangelogDialog
import com.trashsoftware.ducksontranslator.fragments.MainDictionaryFragment
import com.trashsoftware.ducksontranslator.fragments.MainEncryptionFragment
import com.trashsoftware.ducksontranslator.fragments.MainTranslateFragment
import com.trashsoftware.ducksontranslator.model.MainViewModel
import com.trashsoftware.ducksontranslator.util.LanguageCode
import com.trashsoftware.ducksontranslator.util.LanguageUtil
import trashsoftware.duckSonTranslator.wordPickers.PickerFactory

class MainActivity : AppCompatActivity() {
    private var viewModel: MainViewModel? = null

    private var mainNavView: BottomNavigationView? = null

    private var navigationView: NavigationView? = null
    private var appVersionItem: MenuItem? = null
    private var coreVersionItem: MenuItem? = null
    private var mainDrawer: DrawerLayout? = null
    private var navController: NavController? = null

    private var translateFragment: MainTranslateFragment? = null
    private var dictionaryFragment: MainDictionaryFragment? = null
    private var encryptionFragment: MainEncryptionFragment? = null

    private var useBaseDictSwitch: MaterialCheckBox? = null
    private var homophoneSwitch: MaterialCheckBox? = null
    private var cqModeSwitch: MaterialCheckBox? = null

    // Result getter of history view
    var historyResultLauncher: ActivityResultLauncher<Intent?> =
        registerForActivityResult<Intent?, ActivityResult?>(
            StartActivityForResult() as ActivityResultContract<Intent?, ActivityResult?>,
            ActivityResultCallback { result: ActivityResult? ->
                if (result!!.resultCode == RESULT_OK) {
                    val data = result.data
                    if (data != null) {
                        val item = data.getSerializableExtra("historyItem") as HistoryItem?
                        if (item != null) {
                            mainDrawer!!.closeDrawer(navigationView!!)

                            viewModel!!.recoverToHistory(item)
                            applyOptionsToUI()

                            switchToTranslateFragment()
                            translateFragment!!.reTranslate()
                        }
                    }
                }
            }
        )
    private var translatorPref: SharedPreferences? = null
    var settingsResultLauncher: ActivityResultLauncher<Intent?> =
        registerForActivityResult<Intent?, ActivityResult?>(
            StartActivityForResult() as ActivityResultContract<Intent?, ActivityResult?>,
            ActivityResultCallback { result: ActivityResult? ->
//                System.out.println(PreferenceManager.getDefaultSharedPreferences(this).getAll());
                if (result!!.resultCode == SettingsActivity.RESULT_RELOAD) {
                    recreate()
                } else if (result.resultCode == RESULT_OK) {
                    restoreSettings()
                }
            }
        )
    private var versionPref: SharedPreferences? = null

    fun setTranslateFragment(translateFragment: MainTranslateFragment) {
        this.translateFragment = translateFragment
    }

    fun setDictionaryFragment(dictionaryFragment: MainDictionaryFragment) {
        this.dictionaryFragment = dictionaryFragment
    }

    fun setEncryptionFragment(encryptionFragment: MainEncryptionFragment) {
        this.encryptionFragment = encryptionFragment
    }

    private fun switchToTranslateFragment() {
        navController!!.navigate(R.id.bot_navigation_trans)
    }

    override fun attachBaseContext(newBase: Context) {
//        super.attachBaseContext(newBase);
        val pref = PreferenceManager.getDefaultSharedPreferences(newBase)
        val localeCode: String = pref.getString("language_list", "")!!
        val langCode = LanguageCode.fromLanCodeName(localeCode)
        Log.d(TAG, "Switched to " + langCode)
        super.attachBaseContext(LanguageUtil.getInstance().applyLanguage(newBase, langCode))
        //        super.attachBaseContext(newBase);
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = MainViewModel.getInstance()

        mainNavView = findViewById<BottomNavigationView>(R.id.main_nav_bot_bar)

        val fragmentManager = getSupportFragmentManager()
        val navContainerView = fragmentManager
            .findFragmentById(R.id.main_nav_container) as NavHostFragment?
        navController = navContainerView!!.navController

        // 启动
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        setupWithNavController(mainNavView!!, navController!!)

        translatorPref = getSharedPreferences("translatorPref", 0)
        versionPref = getSharedPreferences("versionPref", 0)

        mainDrawer = findViewById<DrawerLayout>(R.id.mainDrawer)
        navigationView = findViewById<NavigationView>(R.id.mainNavigation)

        appVersionItem = navigationView!!.getMenu().findItem(R.id.appVersionItem)
        coreVersionItem = navigationView!!.getMenu().findItem(R.id.coreVersionItem)

        val header = navigationView!!.getHeaderView(0)

        // 方言
        cqModeSwitch = header.findViewById<MaterialCheckBox>(R.id.cqModeSwitch)

        // 使用小词典
        useBaseDictSwitch = header.findViewById<MaterialCheckBox>(R.id.baseDictSwitch)

        // 同音字
        homophoneSwitch = header.findViewById<MaterialCheckBox>(R.id.homophoneSwitch)

        val translator = viewModel!!.getTranslator() // 初始化
        val coreVersion = viewModel!!.getShownCoreVersion()

        restoreSettings()
        appVersionItem!!.setTitle(
            String.format(
                getString(R.string.app_version),
                BuildConfig.VERSION_NAME
            )
        )
        coreVersionItem!!.setTitle(String.format(getString(R.string.core_version), coreVersion))

        initToggleSelection()

        val toolbar = findViewById<Toolbar?>(R.id.main_toolbar)
        setSupportActionBar(toolbar)

        val actionBar: ActionBar? = checkNotNull(getSupportActionBar())
        actionBar!!.setDisplayHomeAsUpEnabled(true)

        val actionBarDrawerToggle =
            ActionBarDrawerToggle(this, mainDrawer, R.string.open, R.string.close)

        actionBarDrawerToggle.syncState()

        mainDrawer!!.addDrawerListener(actionBarDrawerToggle)

        useBaseDictSwitch!!.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { v: CompoundButton?, checked: Boolean ->
            translator.getOptions().setUseBaseDict(checked)
            savePref()
        })

        cqModeSwitch!!.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { v: CompoundButton?, checked: Boolean ->
            translator.getOptions().setChongqingMode(checked)
            savePref()
        })

        homophoneSwitch!!.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { v: CompoundButton?, checked: Boolean ->
            translator.getOptions().setUseSameSoundChar(checked)
            cqModeSwitch!!.setEnabled(checked)
            savePref()
        })

        val historyAccess = HistoryAccess.getInstance(this)

        checkIsFirstOpen()
    }

    fun translateAction(view: View?) {
        translateFragment!!.translate()
    }

    fun swapLanguageAction(view: View?) {
        translateFragment!!.swapLanguage()
    }

    fun clearUpTextAction(view: View?) {
        translateFragment!!.clearUpText()
    }

    fun copyDownTextAction(view: View?) {
        translateFragment!!.copyDownText()
    }

    fun searchDictAction(view: View?) {
        dictionaryFragment!!.search()
    }

    fun expandCollapseKeyFieldAction(view: View?) {
        encryptionFragment!!.expandCollapseKeyField()
    }

    fun expandCollapseEncryptFieldAction(view: View?) {
        encryptionFragment!!.expandCollapseEncryptField()
    }

    fun generateRSAKeysAction(view: View?) {
        encryptionFragment!!.generateRSAKeys()
    }

    fun encryptAction(view: View?) {
        encryptionFragment!!.encryptDecrypt()
    }

    fun copyPublicKeyAction(view: View?) {
        encryptionFragment!!.copyPublicKey()
    }

    fun sharePublicKeyAction(view: View?) {
        encryptionFragment!!.sharePublicKey()
    }

    fun applyPrivateKeyAction(view: View?) {
        encryptionFragment!!.applyPrivateKey()
    }

    fun copyPrivateKeyAction(view: View?) {
        encryptionFragment!!.copyPrivateKey()
    }

    fun clearEncryptInputAction(view: View?) {
        encryptionFragment!!.clearEncryptInput()
    }

    fun clearKeyInputAction(view: View?) {
        encryptionFragment!!.clearKeyInput()
    }

    fun copyEncryptOutputAction(view: View?) {
        encryptionFragment!!.copyEncryptOutput()
    }

    fun shareEncryptOutputAction(view: View?) {
        encryptionFragment!!.shareEncryptOutput()
    }

    private fun checkIsFirstOpen() {
        val lastOpenVersion = versionPref!!.getInt("lastOpenVersion", 0)
        val currentVersion = BuildConfig.VERSION_CODE
        Log.i(
            TAG, String.format(
                "Current version %d, last open version %d",
                currentVersion, lastOpenVersion
            )
        )
        //        showUpdates();
        if (currentVersion != lastOpenVersion) {
            showUpdates()
            val editor = versionPref!!.edit()
            editor.putInt("lastOpenVersion", currentVersion)
            editor.apply()
        }
    }

    private fun showUpdates() {
        val dialog = ChangelogDialog(this)

        dialog.show()
    }

    private fun restoreSettings() {
        val translator = viewModel!!.getTranslator()
        translator.getOptions().setUseBaseDict(translatorPref!!.getBoolean("useBaseDict", true))
        translator.getOptions().setChongqingMode(translatorPref!!.getBoolean("cqMode", true))
        translator.getOptions()
            .setUseSameSoundChar(translatorPref!!.getBoolean("useSameSoundChar", true))
        if (!translator.getOptions().isUseSameSoundChar()) {
            cqModeSwitch!!.setEnabled(false)
        }

        val picker: String = PreferenceManager.getDefaultSharedPreferences(this)
            .getString("word_picker", DEFAULT_PICKER)!!
        var pf: PickerFactory?
        try {
            pf = PickerFactory.valueOf(picker)
        } catch (e: EnumConstantNotPresentException) {
            pf = PickerFactory.valueOf(DEFAULT_PICKER)
        }
        translator.getOptions().setPickerFactory(pf)
    }

    private fun applyOptionsToUI() {
        initToggleSelection()
    }

    private fun savePref() {
        val editor = translatorPref!!.edit()
        editor.putBoolean("useBaseDict", viewModel!!.getTranslator().getOptions().isUseBaseDict())
        editor.putBoolean("cqMode", viewModel!!.getTranslator().getOptions().isChongqingMode())
        editor.putBoolean(
            "useSameSoundChar",
            viewModel!!.getTranslator().getOptions().isUseSameSoundChar()
        )
        //        editor.putInt("wordPickerIndex", wordPickerSpinner.getSelectedItemPosition());
        editor.apply()
    }

    private fun initToggleSelection() {
        val translator = viewModel!!.getTranslator()
        cqModeSwitch!!.setChecked(translator.getOptions().isChongqingMode())

        useBaseDictSwitch!!.setChecked(translator.getOptions().isUseBaseDict())
        homophoneSwitch!!.setChecked(translator.getOptions().isUseSameSoundChar())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> if (navigationView!!.isShown()) {
                mainDrawer!!.closeDrawer(navigationView!!)
            } else {
                mainDrawer!!.openDrawer(navigationView!!)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun historyAction(view: MenuItem?) {
        val intent = Intent(this, HistoryActivity::class.java)
        historyResultLauncher.launch(intent)
    }

    fun settingsAction(view: MenuItem?) {
        val intent = Intent(this, SettingsActivity::class.java)
        settingsResultLauncher.launch(intent)
    }

    fun changelogAction(view: MenuItem?) {
        showUpdates()
    }

    companion object {
        private const val TAG = "MAIN_ACTIVITY"
        private val DEFAULT_PICKER = PickerFactory.COMBINED_CHAR.name
        @JvmStatic
        fun getLangName(context: Context, langId: String): String {
            when (langId) {
                "chs" -> return context.getString(R.string.chinese)
                "geg" -> return context.getString(R.string.geglish)
                "chi" -> return context.getString(R.string.chinglish)
                else -> return ""
            }
        }

        @JvmStatic
        fun getWordPickerShownName(context: Context, pf: PickerFactory): String? {
            val names = context.getResources().getStringArray(R.array.word_picker_list)
            val values = context.getResources().getStringArray(R.array.word_picker_list_values)
            for (i in names.indices) {
                if (pf.name == values[i]) {
                    return names[i]
                }
            }
            return pf.name
        }
    }
}