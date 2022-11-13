package com.trashsoftware.ducksontranslator.widgets;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class HistoryVH extends RecyclerView.ViewHolder {

    public HistoryVH(@NonNull View itemView) {
        super(itemView);
    }

    public abstract int getItemType();
}
