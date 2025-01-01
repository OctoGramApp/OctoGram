/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui.custom;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import it.octogram.android.EventType;

@SuppressLint("ViewConstructor")
public class ThemeSelectorCell extends FrameLayout {
    private int noTheme = 0;
    private int timedTheme = 0;
    private int valentineTheme = 0;
    private int halloweenTheme = 0;
    private int holidayTheme = 0;
    private int lunarNewYearTheme = 0;
    private final Map<Integer, Integer> map;

    private int rowCount = 0;
    private int prevSelectedPosition = -1;

    private final ListAdapter listAdapter;
    private final LinearLayoutManager layoutManager;
    private final RecyclerListView recyclerView;
    private final LinearSmoothScroller scroller;

    public ThemeSelectorCell(Context context, int selectedDefault) {
        super(context);
        map = new HashMap<>();
        listAdapter = new ListAdapter(context);
        recyclerView = new RecyclerListView(getContext());
        recyclerView.setAdapter(listAdapter);
        recyclerView.setClipChildren(false);
        recyclerView.setClipToPadding(false);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(null);
        recyclerView.setNestedScrollingEnabled(false);
        scroller = new LinearSmoothScroller(getContext()) {
            @Override
            protected int calculateTimeForScrolling(int dx) {
                return super.calculateTimeForScrolling(dx) * 6;
            }
        };
        recyclerView.setLayoutManager(layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setOnItemClickListener((view, position) -> {
            ThemeDrawer currItem = (ThemeDrawer) view;
            if (currItem.isSelected) {
                return;
            }
            listAdapter.setSelectedItem(position);
            onSelectedEvent(currItem.eventId);
            for (int i = 0; i < recyclerView.getChildCount(); i++) {
                ThemeDrawer child = (ThemeDrawer) recyclerView.getChildAt(i);
                if (child != view) {
                    child.cancelAnimation();
                }
            }
            recyclerView.post(() -> {
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    final int targetPosition = position > prevSelectedPosition
                            ? Math.min(position + 1, rowCount - 1)
                            : Math.max(position - 1, 0);
                    scroller.setTargetPosition(targetPosition);
                    layoutManager.startSmoothScroll(scroller);
                }
                prevSelectedPosition = position;
            });
            currItem.playEmojiAnimation();
        });
        recyclerView.setFocusable(false);
        recyclerView.setPadding(dp(12), 0, dp(12), 0);
        addView(recyclerView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 150, Gravity.START, 0, 8, 0, 8));
        updateRowsId();
        recyclerView.post(() -> {
            //noinspection ConstantConditions
            int selectedPosition = map.getOrDefault(selectedDefault, 0);
            prevSelectedPosition = selectedPosition;
            listAdapter.setSelectedItem(selectedPosition);
            if (selectedPosition > 0 && selectedPosition < rowCount / 2) {
                selectedPosition -= 1;
            }
            int finalSelectedPosition = Math.min(selectedPosition, rowCount - 1);
            layoutManager.scrollToPositionWithOffset(finalSelectedPosition, 0);
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateRowsId() {
        map.clear();
        rowCount = 0;
        noTheme = rowCount++;
        map.put(EventType.NONE.getValue(), noTheme);
        timedTheme = rowCount++;
        map.put(EventType.DEFAULT.getValue(), timedTheme);
        valentineTheme = rowCount++;
        map.put(EventType.VALENTINE.getValue(), valentineTheme);
        halloweenTheme = rowCount++;
        map.put(EventType.HALLOWEEN.getValue(), halloweenTheme);
        holidayTheme = rowCount++;
        map.put(EventType.HOLIDAY.getValue(), holidayTheme);
        lunarNewYearTheme = rowCount++;
        map.put(EventType.LUNAR_NEW_YEAR.getValue(), lunarNewYearTheme);
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final Context mContext;
        private int selectedItemPosition = -1;
        private int oldSelectedItem = -1;
        private WeakReference<ThemeDrawer> selectedViewRef;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RecyclerListView.Holder(new ThemeDrawer(mContext));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ThemeDrawer drawerCell2 = (ThemeDrawer) holder.itemView;
            boolean animated = drawerCell2.canBeAnimate() && oldSelectedItem != -1;
            if (position == noTheme) {
                drawerCell2.setEvent(
                        EventType.NONE.getValue(),
                        R.raw.cross,
                        new int[]{
                                R.drawable.msg_block,
                                R.drawable.msg_block,
                                R.drawable.msg_block,
                                R.drawable.msg_block,
                                R.drawable.msg_block,
                        }
                );
            } else if (position == timedTheme) {
                drawerCell2.setEvent(
                        EventType.DEFAULT.getValue(),
                        R.raw.automatic,
                        new int[]{
                                R.drawable.msg_groups,
                                R.drawable.msg_contacts,
                                R.drawable.msg_calls,
                                R.drawable.msg_saved,
                                R.drawable.msg_settings,
                        }
                );
            } else if (position == valentineTheme) {
                drawerCell2.setEvent(
                        EventType.VALENTINE.getValue(),
                        R.raw.valentine,
                        new int[]{
                                R.drawable.msg_groups_14,
                                R.drawable.msg_contacts_14,
                                R.drawable.msg_calls_14,
                                R.drawable.msg_saved_14,
                                R.drawable.msg_settings_14,
                        }
                );
            } else if (position == halloweenTheme) {
                drawerCell2.setEvent(
                        EventType.HALLOWEEN.getValue(),
                        R.raw.halloween,
                        new int[]{
                                R.drawable.msg_groups_hw,
                                R.drawable.msg_contacts_hw,
                                R.drawable.msg_calls_hw,
                                R.drawable.msg_saved_hw,
                                R.drawable.msg_settings_hw,
                        }
                );
            } else if (position == holidayTheme) {
                drawerCell2.setEvent(
                        EventType.HOLIDAY.getValue(),
                        R.raw.christmas,
                        new int[]{
                                R.drawable.msg_groups_ny,
                                R.drawable.msg_contacts_ny,
                                R.drawable.msg_calls_ny,
                                R.drawable.msg_saved_ny,
                                R.drawable.msg_settings_ny,
                        }
                );
            } else if (position == lunarNewYearTheme) {
                drawerCell2.setEvent(
                        EventType.LUNAR_NEW_YEAR.getValue(),
                        R.raw.lunar_new_year,
                        new int[]{
                                R.drawable.menu_groups_cn,
                                R.drawable.menu_contacts_cn,
                                R.drawable.menu_calls_cn,
                                R.drawable.menu_bookmarks_cn,
                                R.drawable.menu_settings_cn,
                        }
                );
            }
            drawerCell2.setSelected(position == selectedItemPosition, animated);
            if (position == selectedItemPosition) {
                selectedViewRef = new WeakReference<>(drawerCell2);
            }
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        public void setSelectedItem(int position) {
            if (selectedItemPosition == position) {
                return;
            }
            if (selectedItemPosition >= 0) {
                notifyItemChanged(selectedItemPosition);
                ThemeDrawer view = selectedViewRef == null ? null : selectedViewRef.get();
                if (view != null) {
                    view.setSelected(false);
                }
            }
            oldSelectedItem = selectedItemPosition;
            selectedItemPosition = position;
            notifyItemChanged(selectedItemPosition);
        }
    }

    protected void onSelectedEvent(int eventSelected) {
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawLine(dp(8), getMeasuredHeight() - 1, getMeasuredWidth() - dp(8), getMeasuredHeight() - 1, Theme.dividerPaint);
    }
}