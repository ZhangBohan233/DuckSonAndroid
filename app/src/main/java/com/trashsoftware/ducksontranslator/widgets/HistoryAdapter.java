package com.trashsoftware.ducksontranslator.widgets;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trashsoftware.ducksontranslator.HistoryActivity;
import com.trashsoftware.ducksontranslator.R;
import com.trashsoftware.ducksontranslator.db.HistoryAccess;
import com.trashsoftware.ducksontranslator.db.HistoryItem;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryVH> {
    public static final int ITEM_NORMAL = 0;
    public static final int ITEM_FOOTER = 1;

    public static final int NONE_SELECTED = 0;
    public static final int SOME_BUT_NOT_ALL_SELECTED = 4;
    public static final int ALL_SELECTED = 8;

    private final HistoryAccess historyAccess;
    private final HistoryActivity context;
    private final List<HistoryItem> allHistory = new ArrayList<>();
    private final View placeHolder;
    private boolean selecting;
    private boolean showLoadMore;

    public HistoryAdapter(HistoryActivity context, View placeHolder) {
        this.context = context;
        this.placeHolder = placeHolder;

        historyAccess = HistoryAccess.getInstance(context);
        pullHistoryFromDb(HistoryAccess.EACH_TIME_LOAD);

        showLoadMore = allHistory.size() >= HistoryAccess.EACH_TIME_LOAD &&
                allHistory.size() % HistoryAccess.EACH_TIME_LOAD == 0;
    }

    public void selectOrDeselectAll(boolean select) {
        for (int i = 0; i < allHistory.size(); i++) {
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
        pullHistoryFromDb(allHistory.size());
    }

    private void pullHistoryFromDb(int targetSize) {
        int origSize = allHistory.size();
        int modifyIndex = historyAccess.fillListToSize(allHistory, targetSize);
        for (int i = modifyIndex; i < origSize; i++) {
            notifyItemChanged(i);
        }
        notifyItemRangeChanged(modifyIndex, origSize);
//        allHistory.clear();
//        allHistory.addAll(historyAccess.getAll());
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

    public boolean loadMore() {
        int curSize = allHistory.size();
        int tarSize = curSize + HistoryAccess.EACH_TIME_LOAD;
        int beginMod = historyAccess.fillListToSize(allHistory, tarSize);
        for (int i = beginMod; i < curSize; i++) {
            notifyItemChanged(i);
        }
        for (int i = curSize; i < allHistory.size(); i++) {
            notifyItemInserted(i);
        }
        detectSizeChange();
        notifyItemRangeChanged(beginMod, allHistory.size());
        return allHistory.size() == tarSize;
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

        for (int i = 0; i < allHistory.size(); i++) {
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
    public HistoryVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_NORMAL) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.history_item_view, parent, false);

            return new HistoryItemVH(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.history_footer, parent, false);
            return new HistoryFooterVH(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == allHistory.size()) {
            return ITEM_FOOTER;
        } else {
            return ITEM_NORMAL;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryVH holder, int position) {
        int itemType = getItemViewType(position);

        if (itemType == ITEM_NORMAL) {
            HistoryItemVH itemVH = (HistoryItemVH) holder;
            HistoryItem item = allHistory.get(position);
            itemVH.setItem(context, this, item);
            itemVH.itemView.setOnClickListener(v -> {
                item.setExpanded(!item.isExpanded());
                notifyItemChanged(position);
            });
            itemVH.itemView.setOnLongClickListener(v -> {
                if (!selecting) {
                    setSelecting(true);
                    return true;
                }
                return false;
            });
        } else if (itemType == ITEM_FOOTER) {
            HistoryFooterVH footerVH = (HistoryFooterVH) holder;
            footerVH.loadMoreButton.setOnClickListener(v -> {
                showLoadMore = loadMore();
                notifyItemChanged(getItemCount() - 1);
            });
            footerVH.setVisible(showLoadMore);
        }
    }

    @Override
    public int getItemCount() {
        return allHistory.size() + (showLoadMore ? 1 : 0);
    }
}
