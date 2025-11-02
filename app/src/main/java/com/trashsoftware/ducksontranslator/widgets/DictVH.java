package com.trashsoftware.ducksontranslator.widgets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.textview.MaterialTextView;
import com.trashsoftware.ducksontranslator.R;

import java.util.Objects;

import trashsoftware.duckSonTranslator.words.WordResult;
import trashsoftware.duckSonTranslator.words.WordResultType;

public abstract class DictVH extends RecyclerView.ViewHolder {

    public DictVH(@NonNull View itemView) {
        super(itemView);

    }

    public static class ItemHolder extends DictVH {

        final MaterialCardView itemCard;
        final MaterialTextView srcText;
        final LinearLayout container;
//        final ConstraintLayout homophoneDivider;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);

            itemCard = itemView.findViewById(R.id.dict_item_card);
            srcText = itemView.findViewById(R.id.dict_item_src_word);
            container = itemView.findViewById(R.id.dict_item_content);
        }

        void setContent(WordResult wordResult, Context context) {
            int cardBack;
            WordResultType wrt = wordResult.getType();
            cardBack = switch (wrt) {
                case PINYIN -> com.google.android.material.R.attr.colorPrimaryInverse;
                case EXACT -> com.google.android.material.R.attr.colorPrimaryContainer;
                case PREFIX, SUBSTRING, ROUGH ->
                        com.google.android.material.R.attr.colorSecondaryContainer;
                default -> com.google.android.material.R.attr.colorTertiaryContainer;
            };
            itemCard.setCardBackgroundColor(MaterialColors.getColor(context, cardBack, 0));

            container.removeAllViews();
            srcText.setText(wordResult.getDst());

            int textColor;
            if (wrt == WordResultType.PINYIN) {
                textColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurface, 0);
            } else {
                textColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurfaceVariant, 0);
            }
            srcText.setTextColor(textColor);

            for (var posDes : wordResult.getPosDescription().entrySet()) {
                View holder = LayoutInflater.from(context).inflate(R.layout.dict_item_des, container, false);
                MaterialTextView posText = holder.findViewById(R.id.dict_item_part_of_speech);
                MaterialTextView desText = holder.findViewById(R.id.dict_item_dst_des);

                if (wrt == WordResultType.PINYIN) {
                    posText.setText(getPinyinType(context, posDes.getKey()));
                } else {
                    posText.setText(context.getString(R.string.part_of_speech, posDes.getKey()));
                }
                posText.setTextColor(textColor);
                desText.setTextColor(textColor);

                String des = String.join("; ", posDes.getValue());
                desText.setText(des);

                container.addView(holder);
            }
        }

        private String getPinyinType(Context context, String pos) {
            if ("pinyin".equals(pos)) return context.getString(R.string.pinyin);
            else if ("cqPin".equals(pos)) return context.getString(R.string.cqPin);
            else return pos;
        }
    }

    public static class DividerVH extends DictVH {

        TextView sepText;

        public DividerVH(@NonNull View itemView) {
            super(itemView);

            sepText = itemView.findViewById(R.id.recycler_divider_text);
        }

        public void setContent(WordResultType wordResultType) {
            int resId = switch (wordResultType) {
                case PINYIN -> 0;  // Should be unreachable if no bug
                case EXACT -> R.string.exact_word_divider;
                case PREFIX -> R.string.prefix_word_divider;
                case SUBSTRING -> R.string.substring_word_divider;
                case ROUGH -> R.string.rough_word_divider;
                case HOMOPHONE -> R.string.same_sound_divider;
            };
            if (resId != 0) {
                sepText.setText(resId);
            }
        }
    }
}
