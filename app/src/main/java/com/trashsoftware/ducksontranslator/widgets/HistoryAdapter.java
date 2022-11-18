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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryVH> {
    public static final int NONE_SELECTED = 0;
    public static final int SOME_BUT_NOT_ALL_SELECTED = 4;
    public static final int ALL_SELECTED = 8;

    private final HistoryAccess historyAccess;
    private final HistoryActivity context;
    private final List<HistoryItem> allHistory = new ArrayList<>();
    private boolean selecting;

    public HistoryAdapter(HistoryActivity context) {
        this.context = context;

        historyAccess = HistoryAccess.getInstance(context);
        pullHistoryFromDb();
    }

    private static boolean isDifferentDay(LocalDate a, LocalDate b) {
        return !a.equals(b);
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
        int origSize = getItemCount();
        if (historyAccess.patchDelete(selected)) {
            for (int i = affectedIndex.size() - 1; i >= 0; i--) {
                int removeIndex = affectedIndex.get(i);
                allHistory.remove(removeIndex);
                notifyItemRemoved(removeIndex);
            }
            notifyItemChanged(allHistory.size());  // 通知最后的计数器变了
            notifyItemRangeChanged(0, origSize);
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

    @NonNull
    @Override
    public HistoryVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == HistoryVH.NORMAL) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.history_item_view, parent, false);

            return new HistoryItemVH(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.history_item_footer, parent, false);
            return new HistoryFooterVH(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position < allHistory.size()) {
            return HistoryVH.NORMAL;
        } else {
            return HistoryVH.FOOTER;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryVH holder, int position) {
//        System.out.println("bind" + position);
        int type = getItemViewType(position);

        if (type == HistoryVH.NORMAL) {
            HistoryItemVH itemVH = (HistoryItemVH) holder;
            HistoryItem item = allHistory.get(position);

            boolean isDivider;
            if (position == 0) {
                isDivider = true;
            } else {
                HistoryItem last = allHistory.get(position - 1);
                isDivider = isDifferentDay(item.getDate(), last.getDate());
            }

            itemVH.setItem(context, this, item, isDivider);
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
        } else if (type == HistoryVH.FOOTER) {
            HistoryFooterVH footerVH = (HistoryFooterVH) holder;
            footerVH.setValue(context, allHistory.size());
        }
    }

    @Override
    public int getItemCount() {
        return allHistory.size() + 1;
    }
}
