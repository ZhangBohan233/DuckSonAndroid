package com.trashsoftware.ducksontranslator.widgets;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.trashsoftware.ducksontranslator.MainActivity;
import com.trashsoftware.ducksontranslator.R;
import com.trashsoftware.ducksontranslator.db.HistoryItem;

import java.text.SimpleDateFormat;
import java.util.Date;

import trashsoftware.duckSonTranslator.wordPickerChsGeg.PickerFactory;

public class HistoryItemVH extends HistoryVH {

    private final ConstraintLayout expandPart;
    private final TextView srcLangText, dstLangText, origText, translatedText;
    private final TextView baseDict, homo, dialect, picker, date;
    private final Button trans;
    private final MaterialCheckBox checkBox;
    HistoryItem item;

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
    }

    public int getItemType() {
        return HistoryAdapter.ITEM_NORMAL;
    }

    public void setItem(Context context, HistoryAdapter parent, HistoryItem item) {
        this.item = item;

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
        picker.setText(MainActivity.getWordPickerShownName(pf));

        date.setText(SimpleDateFormat.getDateTimeInstance().format(new Date(item.getTime())));

        trans.setOnClickListener(view -> parent.translateAgain(this));

        expandPart.setVisibility(item.isExpanded() ? View.VISIBLE : View.GONE);
        checkBox.setVisibility(parent.isSelecting() ? View.VISIBLE : View.GONE);
        checkBox.setChecked(item.isSelected());

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setSelected(isChecked);
            parent.updateUiWhenSelectionChanged();
        });

        if (item.isExpanded()) {
            origText.setMaxLines(Integer.MAX_VALUE);
            translatedText.setMaxLines(Integer.MAX_VALUE);
        } else {
            origText.setMaxLines(3);
            translatedText.setMaxLines(3);
        }
    }

    private void setYesNo(TextView view, boolean b) {
        view.setText(b ? R.string.yes : R.string.no);
    }
}
