package com.trashsoftware.ducksontranslator.widgets;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    private final List<Wrapper> allHistory = new ArrayList<>();
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
            Wrapper wrapper = allHistory.get(i);
            if (wrapper.item != null) {
                wrapper.item.setSelected(select);
                notifyItemChanged(i);
            }
        }
        updateUiWhenSelectionChanged();
    }

    public boolean deleteSelectedItems() {
        List<HistoryItem> selected = new ArrayList<>();
        List<Integer> affectedIndex = new ArrayList<>();
        for (int i = 0; i < allHistory.size(); i++) {
            Wrapper wrapper = allHistory.get(i);
            if (wrapper.item != null) {
                if (wrapper.item.isSelected()) {
                    affectedIndex.add(i);
                    selected.add(wrapper.item);
                    wrapper.item.setSelected(false);
                }
            }
        }
        int origSize = getItemCount();
        if (historyAccess.patchDelete(selected)) {
            for (int i = affectedIndex.size() - 1; i >= 0; i--) {
                int removeIndex = affectedIndex.get(i);
                allHistory.remove(removeIndex);
                notifyItemRemoved(removeIndex);
            }
            // 这里会有一个问题：某一天的东西删完了，这一天的日期还在
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

        LocalDate lastDate = null;
        for (HistoryItem historyItem : historyAccess.getAll()) {
            LocalDate date = historyItem.getDate();
            if (lastDate == null || isDifferentDay(lastDate, date)) {
                lastDate = date;
                Wrapper dateWrapper = new Wrapper(null, HistoryVH.DATE_SPLITTER);
                dateWrapper.date = date;
                allHistory.add(dateWrapper);
            }

            allHistory.add(new Wrapper(historyItem, HistoryVH.NORMAL));
        }
        allHistory.add(new Wrapper(null, HistoryVH.FOOTER));
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
        int valid = 0;

        for (Wrapper wrapper : allHistory) {
            if (wrapper.item != null) {
                valid++;
                if (wrapper.item.isSelected()) {
                    selectedCount++;
                }
            }
        }
        if (selectedCount == 0) return NONE_SELECTED;
        if (selectedCount == valid) return ALL_SELECTED;
        return SOME_BUT_NOT_ALL_SELECTED;
    }

    void translateAgain(HistoryItemVH holder) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("historyItem", holder.item);
        context.setResult(Activity.RESULT_OK, resultIntent);
        context.finish();
    }

//    @Deprecated
//    public boolean clearItems() {
//        if (historyAccess.deleteAll()) {
//            int size = allHistory.size();
//            allHistory.clear();
//            notifyItemRangeChanged(0, size);
//            return true;
//        } else {
//            pullHistoryFromDb();
//            notifyDataSetChanged();
//            return false;
//        }
//    }

//    @Deprecated
//    public boolean deleteItem(HistoryItemVH holder) {
//        if (historyAccess.delete(holder.item)) {
//            int index = holder.getAdapterPosition();
//            allHistory.remove(index);
//            notifyItemRemoved(index);
//            notifyItemRangeChanged(index, allHistory.size());
//            return true;
//        } else {
//            pullHistoryFromDb();
//            return false;
//        }
//    }

    public boolean isSelecting() {
        return selecting;
    }

    public void setSelecting(boolean selecting) {
        this.selecting = selecting;

        context.setBottomBarVisible(selecting);
        context.setManageButton(selecting);

        for (int i = 0; i < allHistory.size(); i++) {
            Wrapper wrapper = allHistory.get(i);
            if (wrapper.item != null) {
                wrapper.item.setSelected(false);
            }
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
        } else if (viewType == HistoryVH.FOOTER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.history_item_footer, parent, false);
            return new HistoryFooterVH(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_date_divider, parent, false);
            return new HistoryDateVH(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return allHistory.get(position).type;
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryVH holder, int position) {
//        System.out.println("bind" + position);
        int type = getItemViewType(position);

        if (type == HistoryVH.NORMAL) {
            HistoryItemVH itemVH = (HistoryItemVH) holder;
            HistoryItem item = allHistory.get(position).item;
            assert item != null;

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
        } else if (type == HistoryVH.FOOTER) {
            HistoryFooterVH footerVH = (HistoryFooterVH) holder;
            footerVH.setValue(context, (int) allHistory.stream().filter(wrapper -> wrapper.item != null).count());
        } else if (type == HistoryVH.DATE_SPLITTER) {
            HistoryDateVH dateVH = (HistoryDateVH) holder;
            Wrapper wrapper = allHistory.get(position);
            assert wrapper.date != null;
            dateVH.setItem(context, wrapper.date);
        }
    }

    @Override
    public int getItemCount() {
        return allHistory.size();
    }

    public static class Wrapper {
        @Nullable public final HistoryItem item;
        public final int type;
        @Nullable public LocalDate date;

        Wrapper(@Nullable HistoryItem item, int type) {
            this.item = item;
            this.type = type;
        }
    }
}
