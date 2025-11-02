package com.trashsoftware.ducksontranslator.widgets;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.trashsoftware.ducksontranslator.R;
import com.trashsoftware.ducksontranslator.fragments.MainDictionaryFragment;

import java.util.ArrayList;
import java.util.List;

import trashsoftware.duckSonTranslator.words.WordResult;
import trashsoftware.duckSonTranslator.words.WordResultType;

public class DictAdapter extends RecyclerView.Adapter<DictVH> {

    static final int TYPE_DIVIDER = 0;
    static final int TYPE_ITEM = 1;

    private final List<WordResultWrapper> list = new ArrayList<>();
    MainDictionaryFragment fragment;
    View emptyPlaceholder;

    public DictAdapter(MainDictionaryFragment fragment, View emptyPlaceholder) {
        this.fragment = fragment;
        this.emptyPlaceholder = emptyPlaceholder;
    }

    public void refreshContent(List<WordResult> results) {
        List<WordResultWrapper> wrappers = makeWrapperList(results);
        emptyPlaceholder.setVisibility(wrappers.isEmpty() ? View.VISIBLE : View.GONE);
        int origSize = list.size();

        for (int i = 0; i < wrappers.size(); i++) {
            WordResultWrapper wr = wrappers.get(i);
            if (i < origSize) {
                list.set(i, wr);
                notifyItemChanged(i);
            } else {
                list.add(wr);
                notifyItemInserted(i);
            }
        }

        while (list.size() > wrappers.size()) {
            list.remove(list.size() - 1);
            notifyItemRemoved(list.size());
        }

        notifyItemRangeChanged(0, origSize);
    }

    @NonNull
    @Override
    public DictVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_DIVIDER) {
            return new DictVH.DividerVH(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recycler_type_divider, parent, false));
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.dict_item_view, parent, false);

            return new DictVH.ItemHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull DictVH holder, int position) {
        WordResultWrapper item = list.get(position);
        if (holder instanceof DictVH.ItemHolder) {
            assert item.wordResult != null;
            ((DictVH.ItemHolder) holder).setContent(item.wordResult, fragment.getContext());
        } else if (holder instanceof DictVH.DividerVH) {
            ((DictVH.DividerVH) holder).setContent(item.type);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (list.get(position).wordResult == null) {
            return TYPE_DIVIDER;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private static List<WordResultWrapper> makeWrapperList(List<WordResult> wordResults) {
        List<WordResultWrapper> wrappers = new ArrayList<>();
        WordResultType lastType = null;
        for (WordResult wr : wordResults) {
            if (wr.getType() != lastType) {
                lastType = wr.getType();
                if (wr.getType() != WordResultType.PINYIN) {
                    wrappers.add(new WordResultWrapper(null, wr.getType()));
                }
            }
            wrappers.add(new WordResultWrapper(wr, wr.getType()));
        }
        return wrappers;
    }

    /**
     * @param wordResult if this is null, then this item will be a separator
     */
    private record WordResultWrapper(@Nullable WordResult wordResult, WordResultType type) {
    }
}
