package com.trashsoftware.ducksontranslator.widgets;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.trashsoftware.ducksontranslator.MainActivity;
import com.trashsoftware.ducksontranslator.R;
import com.trashsoftware.ducksontranslator.db.HistoryItem;
import com.trashsoftware.ducksontranslator.util.LanguageUtil;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

import trashsoftware.duckSonTranslator.wordPickers.PickerFactory;

public class HistoryItemVH extends HistoryVH {

    private final ConstraintLayout dateDividerPart;
    private final ConstraintLayout expandPart;
    private final TextView srcLangText, dstLangText, origText, translatedText;
    private final TextView baseDict, homo, dialect, picker, date;
    private final TextView dateDividerText;
    private final Button trans;
    private final MaterialCheckBox checkBox;
    private final AlignedText fullTextContainer;
    HistoryItem item;
    Date itemDate;

    public HistoryItemVH(@NonNull View itemView) {
        super(itemView);

        dateDividerPart = itemView.findViewById(R.id.item_date_divider);
        expandPart = itemView.findViewById(R.id.expand_part);
        checkBox = itemView.findViewById(R.id.history_item_box);
        dateDividerText = itemView.findViewById(R.id.item_date_divider_text);

        srcLangText = itemView.findViewById(R.id.src_lang);
        dstLangText = itemView.findViewById(R.id.dst_lang);
        origText = itemView.findViewById(R.id.src_text);
        translatedText = itemView.findViewById(R.id.dst_text);

        baseDict = itemView.findViewById(R.id.base_dict_selection);
        homo = itemView.findViewById(R.id.same_sound_selection);
        dialect = itemView.findViewById(R.id.dialect_selection);
        picker = itemView.findViewById(R.id.word_picker_selection);
        date = itemView.findViewById(R.id.date_selection);

        trans = itemView.findViewById(R.id.translate_again_btn);
        fullTextContainer = itemView.findViewById(R.id.full_text_container);
    }

    @Override
    public int getViewType() {
        return NORMAL;
    }

    private void setupDateDivider(Context context) {
        LocalDate today = LocalDate.now();

        LocalDate itemD = item.getDate();
        if (itemD.equals(today)) {
            dateDividerText.setText(R.string.today);
            return;
        }
        LocalDate yesterday = today.minusDays(1);
        if (itemD.equals(yesterday)) {
            dateDividerText.setText(R.string.yesterday);
            return;
        }
        LocalDate beforeYesterday = yesterday.minusDays(1);
        if (itemD.equals(beforeYesterday)) {
            dateDividerText.setText(R.string.day_before_yesterday);
            return;
        }

        dateDividerText.setText(dateText(context, false));
    }

    private String dateText(Context context, boolean showTime) {
        if (showTime) {
            return LanguageUtil.getInstance().getDateTimeInstance().format(itemDate);
        } else {
            return LanguageUtil.getInstance().getDateInstance().format(itemDate);
        }
    }

    public void setItem(Context context, HistoryAdapter parent, HistoryItem item,
                        boolean isDateDivider) {
        this.item = item;

        itemDate = new Date(item.getTime());

        if (isDateDivider) {
            dateDividerPart.setVisibility(View.VISIBLE);
            setupDateDivider(context);
        } else {
            dateDividerPart.setVisibility(View.GONE);
        }

        srcLangText.setText(MainActivity.getLangName(context, item.getSrcLang()));
        dstLangText.setText(MainActivity.getLangName(context, item.getDstLang()));
        origText.setText(item.getOrigText());
        translatedText.setText(item.getTranslatedText());

        setYesNo(baseDict, item.isUseBaseDict());
        setYesNo(homo, item.isUseSameSound());

        if (item.isCq()) {
            dialect.setText(R.string.cqDialect);
        } else {
            dialect.setText(R.string.mandarin);
        }

        String pickerDb = item.getWordPickerName();
        PickerFactory pf = PickerFactory.valueOf(pickerDb);
        picker.setText(MainActivity.getWordPickerShownName(context, pf));

        date.setText(dateText(context, true));

        trans.setOnClickListener(view -> parent.translateAgain(this));

        expandPart.setVisibility(item.isExpanded() ? View.VISIBLE : View.GONE);
        checkBox.setVisibility(parent.isSelecting() ? View.VISIBLE : View.GONE);
        checkBox.setChecked(item.isSelected());

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setSelected(isChecked);
            parent.updateUiWhenSelectionChanged();
        });

        if (item.isExpanded()) {
            origText.setVisibility(View.INVISIBLE);  // 不用gone, 因为要固定箭头的位置
            translatedText.setVisibility(View.INVISIBLE);
            fullTextContainer.setVisibility(View.VISIBLE);

            fullTextContainer.setText(item.getOrigText(), item.getTranslatedText());
            fullTextContainer.drawParagraphs();

//            origText.setMaxLines(Integer.MAX_VALUE);
//            translatedText.setMaxLines(Integer.MAX_VALUE);
        } else {
            origText.setVisibility(View.VISIBLE);
            translatedText.setVisibility(View.VISIBLE);
            fullTextContainer.setVisibility(View.GONE);
//            origText.setMaxLines(3);
//            translatedText.setMaxLines(3);
        }
    }

    private void setYesNo(TextView view, boolean b) {
        view.setText(b ? R.string.yes : R.string.no);
    }
}
