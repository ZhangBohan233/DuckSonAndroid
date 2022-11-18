package com.trashsoftware.ducksontranslator;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.trashsoftware.ducksontranslator.util.AssetsReader;

import java.io.IOException;

public class AboutActivity extends AppCompatActivity {
    public static final String TAG = "AboutActivity";

    TextView emailView, closedBetaUsersView, appVersionView, coreVersionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        emailView = findViewById(R.id.email_content);
        closedBetaUsersView = findViewById(R.id.cb_users);
        appVersionView = findViewById(R.id.app_version_content);
        coreVersionView = findViewById(R.id.core_version_content);

        appVersionView.setText(MainActivity.getAppVersion());
        coreVersionView.setText(MainActivity.getCoreVersion());

        Toolbar toolbar = findViewById(R.id.about_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        fillTexts();
    }

    private void fillTexts() {
        String[] emails = getResources().getStringArray(R.array.contact_emails);
        String emailText = String.join("\n", emails);
        emailView.setText(emailText);

        try {
            String usersList = AssetsReader.readAssetsLinesAsString(this,
                    "closed_beta_user_list.txt");
            closedBetaUsersView.setText(usersList);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }
}