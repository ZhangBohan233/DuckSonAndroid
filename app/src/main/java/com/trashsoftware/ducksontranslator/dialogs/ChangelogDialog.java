package com.trashsoftware.ducksontranslator.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.textview.MaterialTextView;
import com.trashsoftware.ducksontranslator.R;
import com.trashsoftware.ducksontranslator.util.Changelog;
import com.trashsoftware.ducksontranslator.util.ChangelogReader;

import java.io.IOException;

public class ChangelogDialog extends Dialog {

    final LinearLayout container;
    final Button fullChangelogBtn, closeBtn;
    ChangelogReader changelogReader;
    private boolean showFull = false;

    public ChangelogDialog(@NonNull Context context) {
        super(context);

        setContentView(R.layout.changelog_dialog);
        getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        container = findViewById(R.id.changelog_content_container);
        fullChangelogBtn = findViewById(R.id.full_changelog_btn);
        closeBtn = findViewById(R.id.changelog_close_btn);

        fullChangelogBtn.setOnClickListener(this::onFullChangelogClicked);
        closeBtn.setOnClickListener(this::onCloseClicked);

        try {
            changelogReader = ChangelogReader.getInstance(context);
        } catch (IOException e) {
            Log.e("ChangelogDialog", "Failed to load changelog", e);
        }

        fillContainer();
    }

    private void fillContainer() {
        container.removeAllViews();
        Changelog[] shows;
        if (showFull) {
            shows = changelogReader.getAllChangelogs();
        } else {
            shows = new Changelog[]{changelogReader.getMostRecent()};
        }
        for (Changelog changelog : shows) {
            MaterialTextView versionName = new MaterialTextView(getContext());
            versionName.setText(changelog.getVersionName());
            versionName.setTextAppearance(getContext(), com.google.android.material.R.style.TextAppearance_Material3_TitleMedium);

            TextView versionDate = new TextView(getContext());
            versionDate.setText(changelog.getDateStr());

            TextView content = new TextView(getContext());
            content.setText(changelog.getContent());

            container.addView(versionName);
            container.addView(versionDate);
            container.addView(content);
        }
    }

    public void onFullChangelogClicked(View view) {
        showFull = !showFull;
        fullChangelogBtn.setText(showFull ? R.string.fold_changelog : R.string.full_changelog);
        fillContainer();
    }

    public void onCloseClicked(View view) {
        dismiss();
    }
}
