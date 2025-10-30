package com.trashsoftware.ducksontranslator;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.window.OnBackInvokedDispatcher;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.color.MaterialColors;
import com.trashsoftware.ducksontranslator.fragments.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {
    public static final int RESULT_RELOAD = 1;

    private boolean needReload = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);

//        Window window = getWindow();
//        int color = MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimarySurface, Color.BLACK);
//        window.setStatusBarColor(color);
//
//        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, window.getDecorView());
//        boolean isLight = MaterialColors.isColorLight(color);
//        controller.setAppearanceLightStatusBars(!isLight);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                int resultCode = Activity.RESULT_OK;
                if (needReload) {
                    resultCode = RESULT_RELOAD;
                }
                Intent resultIntent = new Intent();
                setResult(resultCode, resultIntent);
                finish();

                this.remove();
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

    }

    public void setNeedReload(boolean reload) {
        this.needReload = reload;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return false;
    }
}
