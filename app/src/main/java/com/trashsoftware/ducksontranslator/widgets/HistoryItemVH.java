package com.trashsoftware.ducksontranslator.widgets;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.card.MaterialCardView;
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

    protected final MaterialCardView cardView;
    private final ConstraintLayout expandPart, previewPart;
    private final TextView srcLangText, dstLangText, origText, translatedText;
    private final TextView baseDict, homo, dialect, picker, date;
    private final TextView previewInfo, previewTime;
    private final Button trans;
    private final MaterialCheckBox checkBox;
    private final AlignedText fullTextContainer;
    HistoryItem item;
    Date itemDate;

    public HistoryItemVH(@NonNull View itemView) {
        super(itemView);

        cardView = itemView.findViewById(R.id.history_item_card);
        expandPart = itemView.findViewById(R.id.expand_part);
        previewPart = itemView.findViewById(R.id.history_preview_fold);
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

        previewInfo = itemView.findViewById(R.id.preview_info_text);
        previewTime = itemView.findViewById(R.id.preview_time_text);
    }

    @Override
    public int getViewType() {
        return NORMAL;
    }

    public void setItem(Context context, HistoryAdapter parent, HistoryItem item) {
        this.item = item;

        itemDate = new Date(item.getTime());

        String src = MainActivity.getLangName(context, item.getSrcLang());
        String dst = MainActivity.getLangName(context, item.getDstLang());

        srcLangText.setText(src);
        dstLangText.setText(dst);
        origText.setText(item.getOrigText());
        translatedText.setText(item.getTranslatedText());

        setYesNo(baseDict, item.isUseBaseDict());
        setYesNo(homo, item.isUseSameSound());

        String dialectName;

        if (item.isCq()) {
            dialectName = context.getString(R.string.cqDialect);
        } else {
            dialectName = context.getString(R.string.mandarin);
        }
        dialect.setText(dialectName);

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

        // 预览部分
        previewInfo.setText(
                String.format("%s - %s", src, dst)
        );

        previewTime.setText(HistoryDateVH.timeText(itemDate, false));

        if (item.isExpanded()) {
            origText.setVisibility(View.INVISIBLE);  // 不用gone, 因为要固定箭头的位置
            translatedText.setVisibility(View.INVISIBLE);

            previewPart.setVisibility(View.GONE);

            fullTextContainer.setVisibility(View.VISIBLE);

            fullTextContainer.setText(item.getOrigText(), item.getTranslatedText());
            fullTextContainer.drawParagraphs();
        } else {
            origText.setVisibility(View.VISIBLE);
            translatedText.setVisibility(View.VISIBLE);
            previewPart.setVisibility(View.VISIBLE);
            fullTextContainer.setVisibility(View.GONE);
        }
    }

    private void setYesNo(TextView view, boolean b) {
        view.setText(b ? R.string.yes : R.string.no);
    }
}
