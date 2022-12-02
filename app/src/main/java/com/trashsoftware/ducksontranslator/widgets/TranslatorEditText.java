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

import java.util.ArrayList;
import java.util.List;

public class TranslatorEditText extends MaterialAutoCompleteTextView {

    final List<BackgroundColorSpan> spans = new ArrayList<>();

    // 在用户主动修改上方文本之后，翻译高亮将不可用
    private boolean highlightAble = false;
    boolean notShownToastInThisRound = true;

    public TranslatorEditText(@NonNull Context context) {
        super(context);
    }

    public TranslatorEditText(@NonNull Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void textContentChanged() {
        highlightAble = false;
    }

    public boolean isHighlightAble() {
        return highlightAble;
    }

    public void enableHighlighting() {
        highlightAble = true;
        notShownToastInThisRound = true;
    }

    public void clearHighlights() {
        Editable et = getText();
        if (et != null) {
            for (BackgroundColorSpan span : spans) {
                et.removeSpan(span);
            }
            spans.clear();
        }
    }

    public void highlightText(@NonNull List<int[]> ranges) {
        Editable text = getText();
        if (text == null) return;
        for (int[] range : ranges) {
            BackgroundColorSpan span = new BackgroundColorSpan(getHighlightColor());
            spans.add(span);
            text.setSpan(span, range[0], range[1], Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        setSelection(ranges.get(0)[0]);  // 光标位置影响scroll，通过这个方式来滚动到正确位置
    }
}
