package com.trashsoftware.ducksontranslator.widgets;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AlignedText extends LinearLayout {
    private static final int PARAGRAPH_GAP = 6;
    LayoutParams leftParam =
            new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.42f);
    LayoutParams midParam =
            new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.10f);
    LayoutParams rightParam =
            new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.48f);
    LayoutParams blockParams =
            new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    private String[] leftParagraphs;
    private String[] rightParagraphs;

    public AlignedText(Context context) {
        super(context);
    }

    public AlignedText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void setText(String leftText, String rightText) {
        this.leftParagraphs = leftText.split("\n");
        this.rightParagraphs = rightText.split("\n");
        if (leftParagraphs.length != rightParagraphs.length) {
            throw new RuntimeException("Not compatible left and right: " +
                    leftParagraphs.length + ", " + rightParagraphs.length);
        }
    }

    public void drawParagraphs() {
        removeAllViews();
        for (int i = 0; i < leftParagraphs.length; i++) {
            String leftPara = leftParagraphs[i];
            String rightPara = rightParagraphs[i];

            TextView leftText = new TextView(getContext());
            leftText.setTextColor(Color.BLACK);
            leftText.setText(leftPara);

            TextView rightText = new TextView(getContext());
            rightText.setTextColor(Color.BLACK);
            rightText.setText(rightPara);

            LinearLayout left = new LinearLayout(getContext());
            left.setLayoutParams(leftParam);
            left.addView(leftText);

            LinearLayout middle = new LinearLayout(getContext());
            middle.setLayoutParams(midParam);

            LinearLayout right = new LinearLayout(getContext());
            right.setLayoutParams(rightParam);
            right.addView(rightText);

            LinearLayout block = new LinearLayout(getContext());
            blockParams.setMargins(0, 0, 0, PARAGRAPH_GAP);
            block.setOrientation(HORIZONTAL);
            block.setLayoutParams(blockParams);
            block.addView(left);
            block.addView(middle);
            block.addView(right);

            addView(block);
        }
    }

//    public int drawNextParagraph() {
//        String para = paragraphs[curPara++];
//        TextView textView = new TextView(getContext());
//        textView.setText(para);
//        textView.setTextColor(Color.BLACK);
//
//        textView.post(() -> {
//            int lineCount = textView.getLineCount();
//            // Use lineCount here
//        });
//    }
}
