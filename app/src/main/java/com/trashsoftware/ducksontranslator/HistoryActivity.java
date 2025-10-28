package com.trashsoftware.ducksontranslator;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.window.OnBackInvokedDispatcher;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.color.MaterialColors;
import com.trashsoftware.ducksontranslator.widgets.HistoryAdapter;

public class HistoryActivity extends AppCompatActivity {

//    TextView emptyPlaceholder;
    RecyclerView historyRecyclerView;
    Button deleteButton, manageButton;
    MaterialCheckBox selectAllBox;
    ConstraintLayout bottomBar;
    HistoryAdapter adapter;
    private boolean programmingChangeSelectAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Toolbar toolbar = findViewById(R.id.history_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        Window window = getWindow();
        int color = MaterialColors.getColor(this, com.google.android.material.R.attr.colorPrimarySurface, Color.BLACK);
        window.setStatusBarColor(color);

        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, window.getDecorView());
        boolean isLight = MaterialColors.isColorLight(color);
        controller.setAppearanceLightStatusBars(!isLight);

        manageButton = findViewById(R.id.manage_history_button);
        deleteButton = findViewById(R.id.delete_selected_history_btn);
        selectAllBox = findViewById(R.id.select_all_box);
        bottomBar = findViewById(R.id.history_bottom_bar);
        bottomBar.setVisibility(View.GONE);

        historyRecyclerView = findViewById(R.id.historyRecyclerView);
//        emptyPlaceholder = findViewById(R.id.empty_history_text);
        initRecyclerView();

        selectAllBox.setOnCheckedChangeListener((view, isChecked) -> {
            // 不用selectionChange，因为可能要代码改，不想触发这个
            if (!programmingChangeSelectAll) {
                adapter.selectOrDeselectAll(isChecked);
            }
        });

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (adapter.isSelecting()) {
                    adapter.setSelecting(false);
                    return;
                }

                this.remove();
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return false;
    }

    public void setBottomBarVisible(boolean visible) {
        bottomBar.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setDeleteButtonEnabled(boolean enabled) {
        deleteButton.setEnabled(enabled);
    }

    public void setSelectAllSelected(boolean selected) {
        programmingChangeSelectAll = true;
        selectAllBox.setChecked(selected);
        programmingChangeSelectAll = false;
    }

    public void setManageButton(boolean newSelectingStatus) {
        manageButton.setText(newSelectingStatus ? R.string.cancel : R.string.manage);
    }

    public void onManageClicked(View view) {
        adapter.setSelecting(!adapter.isSelecting());
    }

    public void onDeleteClicked(View view) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.please_confirm)
                .setMessage(R.string.delete_confirm)
                .setIcon(R.drawable.ic_launcher_icon)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    if (adapter.deleteSelectedItems()) {
                        Toast.makeText(this, R.string.delete_success, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, R.string.delete_failed, Toast.LENGTH_SHORT).show();
                    }
                    adapter.setSelecting(false);
                }).setNegativeButton(R.string.cancel, (dialog, which) -> {
                    // nothing happens
                }).show();
    }

    @Deprecated
    public void onClearHistoryClicked(MenuItem view) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.please_confirm)
                .setMessage(R.string.clear_confirm)
                .setIcon(R.drawable.ic_launcher_icon)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    if (adapter.clearItems()) {
                        Toast.makeText(this, R.string.clear_success, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, R.string.clear_failed, Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton(R.string.cancel, (dialog, which) -> {
                    // nothing happens
                }).show();
    }

    private void initRecyclerView() {
        adapter = new HistoryAdapter(this);
        historyRecyclerView.addItemDecoration(
                new DividerItemDecoration(historyRecyclerView.getContext(),
                        DividerItemDecoration.VERTICAL));

        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyRecyclerView.setAdapter(adapter);
    }
}