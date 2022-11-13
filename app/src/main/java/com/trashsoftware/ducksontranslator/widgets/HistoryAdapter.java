package com.trashsoftware.ducksontranslator.widgets;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trashsoftware.ducksontranslator.HistoryActivity;
import com.trashsoftware.ducksontranslator.R;
import com.trashsoftware.ducksontranslator.db.HistoryAccess;
import com.trashsoftware.ducksontranslator.db.HistoryItem;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryItemVH> {
    public static final int NONE_SELECTED = 0;
    public static final int SOME_BUT_NOT_ALL_SELECTED = 4;
    public static final int ALL_SELECTED = 8;

    private final HistoryAccess historyAccess;
    private final HistoryActivity context;
    private final List<HistoryItem> allHistory = new ArrayList<>();
    private View placeHolder;
    private boolean selecting;

    public HistoryAdapter(HistoryActivity context, View placeHolder) {
        this.context = context;
        this.placeHolder = placeHolder;

        historyAccess = HistoryAccess.getInstance(context);
        pullHistoryFromDb();
    }

    public void selectOrDeselectAll(boolean select) {
        for (int i = 0; i < getItemCount(); i++) {
            allHistory.get(i).setSelected(select);
            notifyItemChanged(i);
        }
        updateUiWhenSelectionChanged();
    }

    public boolean deleteSelectedItems() {
        List<HistoryItem> selected = new ArrayList<>();
        List<Integer> affectedIndex = new ArrayList<>();
        for (int i = 0; i < allHistory.size(); i++) {
            HistoryItem item = allHistory.get(i);
            if (item.isSelected()) {
                affectedIndex.add(i);
                selected.add(item);
                item.setSelected(false);
            }
        }
        int origSize = selected.size();
        if (historyAccess.patchDelete(selected)) {
            for (int i = affectedIndex.size() - 1; i >= 0; i--) {
                int removeIndex = affectedIndex.get(i);
                allHistory.remove(removeIndex);
                notifyItemRemoved(removeIndex);
            }
            notifyItemRangeChanged(0, origSize);
            detectSizeChange();
            return true;
        } else {
            pullHistoryFromDb();
            notifyDataSetChanged();
            return false;
        }
    }

    private void pullHistoryFromDb() {
        allHistory.clear();
        allHistory.addAll(historyAccess.getAll());
        detectSizeChange();
    }

    void updateUiWhenSelectionChanged() {
        int selectionSta = selectionStatus();

        if (selectionSta == NONE_SELECTED) {
            context.setDeleteButtonEnabled(false);
            context.setSelectAllSelected(false);
        } else {
            context.setDeleteButtonEnabled(true);
            context.setSelectAllSelected(selectionSta != SOME_BUT_NOT_ALL_SELECTED);
        }
    }

    private int selectionStatus() {
        int selectedCount = 0;

        for (HistoryItem item : allHistory) {
            if (item.isSelected()) {
                selectedCount++;
            }
        }
        if (selectedCount == 0) return NONE_SELECTED;
        if (selectedCount == allHistory.size()) return ALL_SELECTED;
        return SOME_BUT_NOT_ALL_SELECTED;
    }

    void translateAgain(HistoryItemVH holder) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("historyItem", holder.item);
        context.setResult(Activity.RESULT_OK, resultIntent);
        context.finish();
    }

    @Deprecated
    public boolean clearItems() {
        if (historyAccess.deleteAll()) {
            int size = allHistory.size();
            allHistory.clear();
            notifyItemRangeChanged(0, size);
            detectSizeChange();
            return true;
        } else {
            pullHistoryFromDb();
            notifyDataSetChanged();
            return false;
        }
    }

    @Deprecated
    public boolean deleteItem(HistoryItemVH holder) {
        if (historyAccess.delete(holder.item)) {
            int index = holder.getAdapterPosition();
            allHistory.remove(index);
            notifyItemRemoved(index);
            notifyItemRangeChanged(index, allHistory.size());
            detectSizeChange();
            return true;
        } else {
            pullHistoryFromDb();
            return false;
        }
    }

    public boolean isSelecting() {
        return selecting;
    }

    public void setSelecting(boolean selecting) {
        this.selecting = selecting;

        context.setBottomBarVisible(selecting);
        context.setManageButton(selecting);

        for (int i = 0; i < getItemCount(); i++) {
            allHistory.get(i).setSelected(false);
            notifyItemChanged(i);
        }
        updateUiWhenSelectionChanged();
    }

    private void detectSizeChange() {
        if (allHistory.size() == 0) {
            placeHolder.setVisibility(View.VISIBLE);
        } else {
            placeHolder.setVisibility(View.GONE);
        }
    }

    @NonNull
    @Override
    public HistoryItemVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_item_view, parent, false);

        return new HistoryItemVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryItemVH holder, int position) {
//        System.out.println("bind" + position);
        HistoryItem item = allHistory.get(position);
        holder.setItem(context, this, item);
        holder.itemView.setOnClickListener(v -> {
            item.setExpanded(!item.isExpanded());
            notifyItemChanged(position);
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (!selecting) {
                setSelecting(true);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return allHistory.size();
    }
}
