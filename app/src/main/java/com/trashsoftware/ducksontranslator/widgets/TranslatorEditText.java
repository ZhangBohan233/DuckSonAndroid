package com.trashsoftware.ducksontranslator.widgets;

import android.content.Context;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.trashsoftware.ducksontranslator.model.MainViewModel;
import com.trashsoftware.ducksontranslator.util.Util;

import java.util.ArrayList;
import java.util.List;

public class TranslatorEditText extends MaterialAutoCompleteTextView {

    private final MainViewModel viewModel;
    boolean notShownToastInThisRound = true;

    public TranslatorEditText(@NonNull Context context) {
        super(context);

        viewModel = MainViewModel.getInstance();
    }

    public TranslatorEditText(@NonNull Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        viewModel = MainViewModel.getInstance();
    }

    public boolean isHighlightAble(String correspondingOrigText) {
        return Util.equals(getText(), correspondingOrigText);
    }

    public void setToastReady() {
        notShownToastInThisRound = true;
    }

    public void clearHighlights() {
        Editable et = getText();
        if (et != null) {
            for (BackgroundColorSpan span : viewModel.transUpTextSpans) {
                et.removeSpan(span);
            }
            viewModel.transUpTextSpans.clear();
        }
    }

    public void highlightText(@NonNull List<int[]> ranges) {
//        System.out.println("high " + trashsoftware.duckSonTranslator.dict.Util.listOfArrayToString(ranges));
        Editable text = getText();
        if (text == null) return;
        for (int[] range : ranges) {
            BackgroundColorSpan span = new BackgroundColorSpan(getHighlightColor());
            viewModel.transUpTextSpans.add(span);
            text.setSpan(span, range[0], range[1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        setSelection(ranges.get(0)[0]);  // 光标位置影响scroll，通过这个方式来滚动到正确位置
    }
}
