package com.trashsoftware.ducksontranslator.widgets;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trashsoftware.ducksontranslator.R;
import com.trashsoftware.ducksontranslator.fragments.MainDictionaryFragment;

import java.util.ArrayList;
import java.util.List;

import trashsoftware.duckSonTranslator.words.WordResult;

public class DictAdapter extends RecyclerView.Adapter<DictVH> {

    private final List<WordResult> list = new ArrayList<>();
    MainDictionaryFragment fragment;
    View emptyPlaceholder;

    public DictAdapter(MainDictionaryFragment fragment, View emptyPlaceholder) {
        this.fragment = fragment;
        this.emptyPlaceholder = emptyPlaceholder;
    }

    public void refreshContent(List<WordResult> results) {
        emptyPlaceholder.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
        int origSize = list.size();

        for (int i = 0; i < results.size(); i++) {
            WordResult wr = results.get(i);
            if (i < origSize) {
                list.set(i, wr);
                notifyItemChanged(i);
            } else {
                list.add(wr);
                notifyItemInserted(i);
            }
        }

        while (list.size() > results.size()) {
            list.remove(list.size() - 1);
            notifyItemRemoved(list.size());
        }

        notifyItemRangeChanged(0, origSize);
    }

    @NonNull
    @Override
    public DictVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dict_item_view, parent, false);

        return new DictVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DictVH holder, int position) {
        WordResult item = list.get(position);

        boolean isSameSoundDivider = false;
        if (item.isFromSameSound()) {
            if (position == 0) isSameSoundDivider = true;
            else {
                WordResult last = list.get(position - 1);
                if (!last.isFromSameSound()) isSameSoundDivider = true;
            }
        }

        holder.setContent(item, fragment.getContext(), isSameSoundDivider);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
