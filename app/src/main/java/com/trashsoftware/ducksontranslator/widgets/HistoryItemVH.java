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

    private final ConstraintLayout expandPart;
    private final TextView srcLangText, dstLangText, origText, translatedText;
    private final TextView baseDict, homo, dialect, picker, date;
    private final Button trans;
    private final MaterialCheckBox checkBox;
    private final AlignedText fullTextContainer;
    HistoryItem item;
    Date itemDate;

    public HistoryItemVH(@NonNull View itemView) {
        super(itemView);

        expandPart = itemView.findViewById(R.id.expand_part);
        checkBox = itemView.findViewById(R.id.history_item_box);

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

    public void setItem(Context context, HistoryAdapter parent, HistoryItem item) {
        this.item = item;

        itemDate = new Date(item.getTime());

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

        date.setText(HistoryDateVH.dateText(itemDate, true));

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
        } else {
            origText.setVisibility(View.VISIBLE);
            translatedText.setVisibility(View.VISIBLE);
            fullTextContainer.setVisibility(View.GONE);
        }
    }

    private void setYesNo(TextView view, boolean b) {
        view.setText(b ? R.string.yes : R.string.no);
    }
}
