package com.trashsoftware.ducksontranslator.widgets;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class HistoryVH extends RecyclerView.ViewHolder {
    public static final int NORMAL = 1;
    public static final int FOOTER = 2;

    public HistoryVH(@NonNull View itemView) {
        super(itemView);
    }

    public abstract int getViewType();
}
