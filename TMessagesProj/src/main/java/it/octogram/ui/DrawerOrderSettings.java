package it.octogram.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;

import java.util.ArrayList;
import java.util.Objects;

import it.octogram.android.MenuOrderController;
import it.octogram.ui.Cells.AddItem;
import it.octogram.ui.Cells.HintHeader;
import it.octogram.ui.Cells.SwapOrder;

public class DrawerOrderSettings extends BaseSettingsActivity {
    private ItemTouchHelper itemTouchHelper;

    private int headerHintRow;
    private int headerSuggestedOptionsRow;
    private int headerMenuRow;
    private int menuHintsStartRow;
    private int menuHintsEndRow;
    private int hintsDividerRow;
    private int menuItemsStartRow;
    private int menuItemsEndRow;
    private int menuItemsDividerRow;


    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onMenuItemClick(int id) {
        super.onMenuItemClick(id);
        if (id == 1) {
            updateListAnimated(() -> MenuOrderController.addItem(MenuOrderController.DIVIDER_ITEM));
            if (MenuOrderController.IsDefaultPosition()) {
                menuItem.hideSubItem(2);
            } else {
                menuItem.showSubItem(2);
            }
            reloadMainInfo();
        } else if (id == 2) {
            updateListAnimated(MenuOrderController::resetToDefaultPosition);
            menuItem.hideSubItem(2);
            reloadMainInfo();
        }
    }

    @Override
    protected ActionBarMenuItem createMenuItem() {
        ActionBarMenu menu = actionBar.createMenu();
        ActionBarMenuItem menuItem = menu.addItem(0, R.drawable.ic_ab_other);
        menuItem.setContentDescription(LocaleController.getString("AccDescrMoreOptions", R.string.AccDescrMoreOptions));
        menuItem.addSubItem(1, R.drawable.msg_newfilter, LocaleController.getString("AddDivider", R.string.AddDivider));
        menuItem.addSubItem(2, R.drawable.msg_reset, LocaleController.getString("ResetItemsOrder", R.string.ResetItemsOrder));
        if (MenuOrderController.IsDefaultPosition()) {
            menuItem.hideSubItem(2);
        } else {
            menuItem.showSubItem(2);
        }
        return menuItem;
    }

    @Override
    public View createView(Context context) {
        View res = super.createView(context);
        itemTouchHelper = new ItemTouchHelper(new TouchHelperCallback());
        itemTouchHelper.attachToRecyclerView(listView);
        return res;
    }

    @Override
    protected String getActionBarTitle() {
        return LocaleController.getString("MenuItems", R.string.MenuItems);
    }

    protected void updateRowsId() {
        super.updateRowsId();
        headerSuggestedOptionsRow = -1;
        hintsDividerRow = -1;

        int size_hints = MenuOrderController.sizeHints();
        headerHintRow = rowCount++;

        if (size_hints > 0) {
            headerSuggestedOptionsRow = rowCount++;
            menuHintsStartRow = rowCount;
            rowCount += size_hints;
            menuHintsEndRow = rowCount;
            hintsDividerRow = rowCount++;
        }

        headerMenuRow = rowCount++;
        menuItemsStartRow = rowCount;
        rowCount += MenuOrderController.sizeAvailable();
        menuItemsEndRow = rowCount;
        menuItemsDividerRow = rowCount++;
    }

    @Override
    protected BaseListAdapter createAdapter() {
        return new ListAdapter();
    }

    private class ListAdapter extends BaseListAdapter {

        @Override
        protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, boolean partial) {
            switch (ViewType.fromInt(holder.getItemViewType())) {
                case SHADOW:
                    holder.itemView.setBackground(Theme.getThemedDrawable(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case HEADER:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == headerSuggestedOptionsRow) {
                        headerCell.setText(LocaleController.getString("RecommendedItems", R.string.RecommendedItems));
                    } else if (position == headerMenuRow) {
                        headerCell.setText(LocaleController.getString("MenuItems", R.string.MenuItems));
                    }
                    break;
                case MENU_ITEM:
                    SwapOrder swapOrderCell = (SwapOrder) holder.itemView;
                    MenuOrderController.EditableMenuItem data = MenuOrderController.getSingleAvailableMenuItem(position - menuItemsStartRow);
                    if (data != null) {
                        swapOrderCell.setData(data.text, data.isDefault, data.isPremium, data.id, true);
                    }
                    break;
                case SUGGESTED_OPTIONS:
                    AddItem addItemCell = (AddItem) holder.itemView;
                    MenuOrderController.EditableMenuItem notData = MenuOrderController.getSingleNotAvailableMenuItem(position - menuHintsStartRow);
                    if (notData != null) {
                        addItemCell.setData(notData.text, notData.id, notData.isPremium, true);
                    }
                    break;
            }
        }

        @Override
        protected boolean isEnabled(ViewType viewType, int position) {
            return viewType == ViewType.MENU_ITEM;
        }

        @SuppressLint({"ClickableViewAccessibility", "NotifyDataSetChanged"})
        @Override
        protected View onCreateViewHolder(ViewType viewType) {
            View view = null;
            switch (viewType) {
                case HINT_HEADER:
                    view = new HintHeader(context, R.raw.filters, LocaleController.formatString("MenuItemsOrderDesc", R.string.MenuItemsOrderDesc));
                    view.setBackground(Theme.getThemedDrawable(context, R.drawable.greydivider_top, Theme.key_windowBackgroundGrayShadow));
                    break;
                case MENU_ITEM:
                    SwapOrder swapOrderCell = new SwapOrder(context);
                    swapOrderCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    swapOrderCell.setOnReorderButtonTouchListener((v, event) -> {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            itemTouchHelper.startDrag(listView.getChildViewHolder(swapOrderCell));
                        }
                        return false;
                    });
                    swapOrderCell.setOnDeleteClick(v -> {
                        int index = listView.getChildViewHolder(swapOrderCell).getAdapterPosition();
                        int position = index - menuItemsStartRow;
                        if (MenuOrderController.isAvailable(swapOrderCell.menuId, position)) {
                            updateListAnimated(() -> MenuOrderController.removeItem(position));
                            if (MenuOrderController.IsDefaultPosition()) {
                                menuItem.hideSubItem(2);
                            } else {
                                menuItem.showSubItem(2);
                            }
                            reloadMainInfo();
                        }
                    });
                    view = swapOrderCell;
                    break;
                case SUGGESTED_OPTIONS:
                    AddItem addItemCell = new AddItem(context);
                    addItemCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    addItemCell.setAddOnClickListener(v -> {
                        if (!MenuOrderController.isAvailable(addItemCell.menuId)) {
                            int index = MenuOrderController.getPositionOf(addItemCell.menuId);
                            updateListAnimated(() -> {
                                if (index != -1) {
                                    MenuOrderController.addItem(addItemCell.menuId);
                                }
                            });
                            if (MenuOrderController.IsDefaultPosition()) {
                                menuItem.hideSubItem(2);
                            } else {
                                menuItem.showSubItem(2);
                            }
                            reloadMainInfo();
                        }
                    });
                    view = addItemCell;
                    break;
            }
            return view;
        }

        @Override
        public ViewType getViewType(int position) {
            if (position == hintsDividerRow || position == menuItemsDividerRow) {
                return ViewType.SHADOW;
            } else if (position == headerHintRow) {
                return ViewType.HINT_HEADER;
            } else if (position == headerSuggestedOptionsRow || position == headerMenuRow) {
                return ViewType.HEADER;
            } else if (position >= menuItemsStartRow && position < menuItemsEndRow) {
                return ViewType.MENU_ITEM;
            } else if (position >= menuHintsStartRow && position < menuHintsEndRow) {
                return ViewType.SUGGESTED_OPTIONS;
            }
            throw new IllegalArgumentException("Invalid position");
        }

        public void swapElements(int fromIndex, int toIndex) {
            int idx1 = fromIndex - menuItemsStartRow;
            int idx2 = toIndex - menuItemsStartRow;
            int count = menuItemsEndRow - menuItemsStartRow;
            if (idx1 < 0 || idx2 < 0 || idx1 >= count || idx2 >= count) {
                return;
            }
            MenuOrderController.changePosition(idx1, idx2);
            notifyItemMoved(fromIndex, toIndex);
            if (MenuOrderController.IsDefaultPosition()) {
                menuItem.hideSubItem(2);
            } else {
                menuItem.showSubItem(2);
            }
            reloadMainInfo();
        }
    }

    public class TouchHelperCallback extends ItemTouchHelper.Callback {

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            if (viewHolder.getItemViewType() != ViewType.MENU_ITEM.toInt()) {
                return makeMovementFlags(0, 0);
            }
            return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
            if (source.getItemViewType() != target.getItemViewType()) {
                return false;
            }
            ((ListAdapter) listAdapter).swapElements(source.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                listView.cancelClickRunnables(false);
                viewHolder.itemView.setPressed(true);
            }
            super.onSelectedChanged(viewHolder, actionState);
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        }

        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            viewHolder.itemView.setPressed(false);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateListAnimated(Runnable callback) {
        if (listAdapter == null) {
            updateRowsId();
            return;
        }
        DiffCallback diffCallback = new DiffCallback();
        diffCallback.oldRowCount = rowCount;
        diffCallback.fillPositions(diffCallback.oldPositionToItem);
        diffCallback.oldMenuHints.clear();
        diffCallback.oldMenuItems.clear();
        diffCallback.oldMenuHints.add(null); // header
        for (int i = 0; i < MenuOrderController.sizeHints(); i++) {
            diffCallback.oldMenuHints.add(MenuOrderController.getSingleNotAvailableMenuItem(i));
        }
        diffCallback.oldMenuHints.add(null); // divider
        for (int i = 0; i < MenuOrderController.sizeAvailable(); i++) {
            diffCallback.oldMenuItems.add(MenuOrderController.getSingleAvailableMenuItem(i));
        }
        diffCallback.oldMenuHintsStartRow = headerSuggestedOptionsRow;
        diffCallback.oldMenuHintsEndRow = hintsDividerRow;
        diffCallback.oldMenuItemsStartRow = menuItemsStartRow;
        diffCallback.oldMenuItemsEndRow = menuItemsEndRow;
        callback.run();
        updateRowsId();
        diffCallback.fillPositions(diffCallback.newPositionToItem);
        try {
            DiffUtil.calculateDiff(diffCallback).dispatchUpdatesTo(listAdapter);
        } catch (Exception e) {
            FileLog.e(e);
            listAdapter.notifyDataSetChanged();
        }
        AndroidUtilities.updateVisibleRows(listView);
    }

    private class DiffCallback extends DiffUtil.Callback {
        int oldRowCount;
        SparseIntArray oldPositionToItem = new SparseIntArray();
        SparseIntArray newPositionToItem = new SparseIntArray();
        ArrayList<MenuOrderController.EditableMenuItem> oldMenuHints = new ArrayList<>();
        ArrayList<MenuOrderController.EditableMenuItem> oldMenuItems = new ArrayList<>();
        int oldMenuHintsStartRow;
        int oldMenuHintsEndRow;
        int oldMenuItemsStartRow;
        int oldMenuItemsEndRow;

        @Override
        public int getOldListSize() {
            return oldRowCount;
        }

        @Override
        public int getNewListSize() {
            return rowCount;
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            if (newItemPosition >= menuHintsStartRow && newItemPosition < menuHintsEndRow) {
                if (oldItemPosition >= oldMenuHintsStartRow && oldItemPosition < oldMenuHintsEndRow) {
                    MenuOrderController.EditableMenuItem oldItem = oldMenuHints.get(oldItemPosition - oldMenuHintsStartRow);
                    MenuOrderController.EditableMenuItem newItem = MenuOrderController.getSingleNotAvailableMenuItem(newItemPosition - menuHintsStartRow);
                    if (oldItem == null || newItem == null) {
                        return false;
                    }
                    return Objects.equals(oldItem.id, newItem.id);
                }
            }
            if (newItemPosition >= menuItemsStartRow && newItemPosition < menuItemsEndRow) {
                if (oldItemPosition >= oldMenuItemsStartRow && oldItemPosition < oldMenuItemsEndRow) {
                    MenuOrderController.EditableMenuItem oldItem = oldMenuItems.get(oldItemPosition - oldMenuItemsStartRow);
                    MenuOrderController.EditableMenuItem newItem = MenuOrderController.getSingleAvailableMenuItem(newItemPosition - menuItemsStartRow);
                    if (oldItem == null || newItem == null) {
                        return false;
                    }
                    return Objects.equals(oldItem.id, newItem.id);
                }
            }
            int oldIndex = oldPositionToItem.get(oldItemPosition, -1);
            int newIndex = newPositionToItem.get(newItemPosition, -1);
            return oldIndex == newIndex && oldIndex >= 0;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return areItemsTheSame(oldItemPosition, newItemPosition);
        }

        public void fillPositions(SparseIntArray sparseIntArray) {
            sparseIntArray.clear();
            int pointer = 0;

            put(++pointer, headerHintRow, sparseIntArray);
            if (MenuOrderController.sizeHints() > 0) {
                put(++pointer, headerSuggestedOptionsRow, sparseIntArray);
                put(++pointer, hintsDividerRow, sparseIntArray);
            }
            put(++pointer, headerMenuRow, sparseIntArray);
            put(++pointer, menuItemsDividerRow, sparseIntArray);
        }

        private void put(int id, int position, SparseIntArray sparseIntArray) {
            if (position >= 0) {
                sparseIntArray.put(position, id);
            }
        }
    }
}
