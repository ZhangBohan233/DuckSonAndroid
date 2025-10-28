package com.trashsoftware.ducksontranslator.widgets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.trashsoftware.ducksontranslator.R;

import trashsoftware.duckSonTranslator.words.WordResult;

public abstract class DictVH extends RecyclerView.ViewHolder {

    public DictVH(@NonNull View itemView) {
        super(itemView);

    }

    public static class ItemHolder extends DictVH {

        final TextView srcText;
        final LinearLayout container;
//        final ConstraintLayout homophoneDivider;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);

            srcText = itemView.findViewById(R.id.dict_item_src_word);
            container = itemView.findViewById(R.id.dict_item_content);
//            homophoneDivider = itemView.findViewById(R.id.dict_item_homophone_divider);
        }

        void setContent(WordResult wordResult, Context context) {
//            homophoneDivider.setVisibility(isHomophoneDivider ? View.VISIBLE : View.GONE);

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

        public DividerVH(@NonNull View itemView) {
            super(itemView);

            TextView sepText = itemView.findViewById(R.id.recycler_divider_text);

            sepText.setText(R.string.same_sound_divider);
        }
    }
}
