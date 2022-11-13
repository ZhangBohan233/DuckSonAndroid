package com.trashsoftware.ducksontranslator.widgets;

import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.trashsoftware.ducksontranslator.R;

public class HistoryFooterVH extends HistoryVH {
    final Button loadMoreButton;

    public HistoryFooterVH(@NonNull View itemView) {
        super(itemView);

        loadMoreButton = itemView.findViewById(R.id.load_more_btn);
    }

    public void setVisible(boolean visible) {
        itemView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemType() {
        return HistoryAdapter.ITEM_FOOTER;
    }
}
