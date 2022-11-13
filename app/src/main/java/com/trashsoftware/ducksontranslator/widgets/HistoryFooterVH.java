package com.trashsoftware.ducksontranslator.widgets;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.trashsoftware.ducksontranslator.R;

import java.text.NumberFormat;
import java.util.Locale;

public class HistoryFooterVH extends HistoryVH {
    final TextView itemsCount;
    final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());

    public HistoryFooterVH(@NonNull View itemView) {
        super(itemView);

        itemsCount = itemView.findViewById(R.id.history_count_text);
    }

    public void setValue(Context context, int value) {
        if (value == 0) {
            itemsCount.setText(R.string.empty_history);
        } else {
            String num = numberFormat.format(value);
            String formatted = context.getString(R.string.history_items_count, num);
            itemsCount.setText(formatted);
        }
    }

    @Override
    public int getViewType() {
        return FOOTER;
    }
}
