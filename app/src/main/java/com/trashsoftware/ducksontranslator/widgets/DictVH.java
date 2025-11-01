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
import com.trashsoftware.ducksontranslator.R;

import trashsoftware.duckSonTranslator.words.WordResult;
import trashsoftware.duckSonTranslator.words.WordResultType;

public abstract class DictVH extends RecyclerView.ViewHolder {

    public DictVH(@NonNull View itemView) {
        super(itemView);

    }

    public static class ItemHolder extends DictVH {

        final MaterialCardView itemCard;
        final TextView srcText;
        final LinearLayout container;
//        final ConstraintLayout homophoneDivider;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);

            itemCard = itemView.findViewById(R.id.dict_item_card);
            srcText = itemView.findViewById(R.id.dict_item_src_word);
            container = itemView.findViewById(R.id.dict_item_content);
        }

        void setContent(WordResult wordResult, Context context) {
//            int cardBack = switch (wordResult.getType()) {
//                case EXACT -> com.google.android.material.R.attr.colorSurfaceContainer;
//                case PREFIX -> com.google.android.material.R.attr.colorSurfaceContainerHighest;
//                case SUBSTRING -> com.google.android.material.R.attr.colorSurfaceBright;
//                default -> androidx.cardview.R.color.cardview_light_background;
//            };
//            itemCard.setCardBackgroundColor(cardBack);

            container.removeAllViews();
            srcText.setText(wordResult.getDst());

            for (var posDes : wordResult.getPosDescription().entrySet()) {
                View holder = LayoutInflater.from(context).inflate(R.layout.dict_item_des, container, false);
                TextView posText = holder.findViewById(R.id.dict_item_part_of_speech);
                TextView desText = holder.findViewById(R.id.dict_item_dst_des);

                posText.setText(context.getString(R.string.part_of_speech, posDes.getKey()));

                String des = String.join("; ", posDes.getValue());
                desText.setText(des);

                container.addView(holder);
            }
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
                case EXACT -> R.string.exact_word_divider;
                case PREFIX -> R.string.prefix_word_divider;
                case SUBSTRING -> R.string.substring_word_divider;
                case ROUGH -> R.string.rough_word_divider;
                case HOMOPHONE -> R.string.same_sound_divider;
            };
            sepText.setText(resId);
        }
    }
}
