package com.trashsoftware.ducksontranslator.widgets;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.trashsoftware.ducksontranslator.R;

import java.util.ArrayList;
import java.util.List;

import trashsoftware.duckSonTranslator.result.ResultToken;
import trashsoftware.duckSonTranslator.result.TranslationResult;

public class ResultText extends androidx.appcompat.widget.AppCompatTextView {

    private ViewGroup.LayoutParams focusedParams;
    private ViewGroup.LayoutParams normalParams;
    private TranslationResult translationResult;
    private View focusIndicator;
    private TranslatorEditText srcField;

    public ResultText(@NonNull Context context) {
        super(context);

        createParams();
    }

    public ResultText(@NonNull Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        createParams();
    }

    private void createParams() {
        focusedParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(3));
        normalParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(1));
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);

        super.onSelectionChanged(selStart, selEnd);
        if (translationResult == null || srcField == null) return;
        if (srcField.isHighlightAble()) {

//        System.out.println("Select " + selStart + " " + selEnd);

            List<ResultToken> selectedTokens = translationResult.findTokensInRange(selStart, selEnd);
            List<int[]> indexRanges = TranslationResult.rangeOf(selectedTokens);

//        System.out.println("Result " + selectedTokens);
            srcField.clearHighlights();
            if (selStart != selEnd && !indexRanges.isEmpty()) {
                srcField.highlightText(analyzeRangesWithSpace(indexRanges));
            }
        } else if (selStart != selEnd && srcField.notShownToastInThisRound) {
            Toast.makeText(getContext(), R.string.text_has_changed, Toast.LENGTH_SHORT).show();
            srcField.notShownToastInThisRound = false;
        }
    }

    private List<int[]> analyzeRangesWithSpace(List<int[]> ranges) {
        List<int[]> results = new ArrayList<>();

        int[] first = ranges.get(0);
        int curBegin = first[0];
        int curEnd = first[1];
        for (int[] rge : ranges.subList(1, ranges.size())) {
            // 看看两段高亮之间是不是只有空格或是折行
            boolean isContinuous = true;
            for (int i = curEnd; i < rge[0]; i++) {
                char gap = translationResult.getOriginalText().charAt(i);
                if (gap != ' ' && gap != '\n') {
                    isContinuous = false;
                    break;
                }
            }
            if (!isContinuous) {
                results.add(new int[]{curBegin, curEnd});
                curBegin = rge[0];
            }
            curEnd = rge[1];
        }
        results.add(new int[]{curBegin, curEnd});
        return results;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getContext().getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);

        if (focusIndicator != null) {
            ViewGroup.LayoutParams layoutParams = focusIndicator.getLayoutParams();
            if (focused) {
                focusIndicator.setBackgroundColor(getContext().getColor(R.color.teal_200));
                layoutParams.height = dpToPx(3);
            } else {
                focusIndicator.setBackgroundColor(getContext().getColor(R.color.not_important_black));
                layoutParams.height = dpToPx(1);
            }
            focusIndicator.setLayoutParams(layoutParams);
        }
    }

    public void setSrcField(TranslatorEditText srcField) {
        this.srcField = srcField;
    }

    public void setFocusIndicator(View focusIndicator) {
        this.focusIndicator = focusIndicator;
    }

    public void setTranslationResult(TranslationResult translationResult) {
        this.translationResult = translationResult;
    }
}
