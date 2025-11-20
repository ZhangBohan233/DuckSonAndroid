package com.trashsoftware.ducksontranslator.widgets;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.trashsoftware.ducksontranslator.R;
import com.trashsoftware.ducksontranslator.util.LanguageUtil;

import java.time.LocalDate;
import java.util.Date;

public class HistoryDateVH extends HistoryVH {

    TextView dateDividerText;

    public HistoryDateVH(@NonNull View itemView) {
        super(itemView);

        dateDividerText = itemView.findViewById(R.id.item_date_divider_text);
    }

    public void setItem(Context context, LocalDate localDate) {
        setupDateDivider(context, localDate);
    }

    private void setupDateDivider(Context context, LocalDate localDate) {
        LocalDate today = LocalDate.now();

        if (localDate.equals(today)) {
            dateDividerText.setText(R.string.today);
            return;
        }
        LocalDate yesterday = today.minusDays(1);
        if (localDate.equals(yesterday)) {
            dateDividerText.setText(R.string.yesterday);
            return;
        }
        LocalDate beforeYesterday = yesterday.minusDays(1);
        if (localDate.equals(beforeYesterday)) {
            dateDividerText.setText(R.string.day_before_yesterday);
            return;
        }

        dateDividerText.setText(dateText(localDate));
    }

    public static String dateText(Date itemDate, boolean showTime) {
        if (showTime) {
            return LanguageUtil.getInstance().getDateTimeInstance().format(itemDate);
        } else {
            return LanguageUtil.getInstance().getDateInstance().format(itemDate);
        }
    }

    public static String dateText(LocalDate localDate) {
        return localDate.format(LanguageUtil.getInstance().getDateFormatter());
    }

    public static String timeText(Date itemDate, boolean showSeconds) {
        if (showSeconds) return LanguageUtil.TIME_HH_MM_SS.format(itemDate);
        else return LanguageUtil.TIME_HH_MM.format(itemDate);
    }

    @Override
    public int getViewType() {
        return DATE_SPLITTER;
    }
}
