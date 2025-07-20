/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui;

import static org.telegram.messenger.LocaleController.getString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.text.TextUtils;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.Objects;

import it.octogram.android.OctoConfig;
import it.octogram.android.app.ui.components.AddItem;
import it.octogram.android.app.ui.components.HintHeader;
import it.octogram.android.app.ui.components.SwapOrder;
import it.octogram.android.utils.OctoLogging;
import it.octogram.android.utils.config.DrawerOrderController;

public class OctoAppearanceDrawerOrderUI extends BaseFragment {

    private final static String TAG = "DrawerOrderSettings";
    private ItemTouchHelper itemTouchHelper;
    private RecyclerListView listView;
    private ListAdapter listAdapter;
    private ActionBarMenuItem menuItem;
    private Context context;
    private int headerHintRow;
    private int headerSuggestedOptionsRow;
    private int headerMenuRow;
    private int menuHintsStartRow;
    private int menuHintsEndRow;
    private int hintsDividerRow;
    private int menuItemsStartRow;
    private int menuItemsEndRow;
    private int menuItemsDividerRow;
    private int rowCount;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        updateRowsId();
        return true;
    }

    protected void onMenuItemClick(int id) {
        if (menuItem == null) {
            menuItem = createMenuItem();
        }

        if (id == SubItem.ADD_DIVIDER.ordinal()) {
            if (!DrawerOrderController.isDefaultPosition()) {
                menuItem.showSubItem(SubItem.RESET_ORDER.ordinal());
            } else {
                menuItem.hideSubItem(SubItem.RESET_ORDER.ordinal());
            }
            updateListAnimated(() -> DrawerOrderController.addItem(DrawerOrderController.DIVIDER_ITEM));
        } else if (id == SubItem.RESET_ORDER.ordinal()) {
            updateListAnimated(DrawerOrderController::resetToDefaultPosition);
            menuItem.hideSubItem(SubItem.RESET_ORDER.ordinal());
        }
        reloadMainInfo();
    }

    protected ActionBarMenuItem createMenuItem() {
        ActionBarMenu menu = actionBar.createMenu();
        ActionBarMenuItem menuItem = menu.addItem(SubItem.TREE_DOTS.ordinal(), R.drawable.ic_ab_other);
        menuItem.setContentDescription(getString(R.string.AccDescrMoreOptions));
        menuItem.addSubItem(SubItem.ADD_DIVIDER.ordinal(), R.drawable.msg_new_filter, getString(R.string.AddDivider));
        menuItem.addSubItem(SubItem.RESET_ORDER.ordinal(), R.drawable.msg_reset, getString(R.string.ResetItemsOrder));

        if (!DrawerOrderController.isDefaultPosition()) {
            menuItem.showSubItem(SubItem.RESET_ORDER.ordinal());
        } else {
            menuItem.hideSubItem(SubItem.RESET_ORDER.ordinal());
        }
        return menuItem;
    }

    @Override
    public View createView(Context context) {
        this.context = context;
        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundGray));

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(getString(R.string.MenuItems));
        actionBar.setBackgroundColor(getThemedColor(Theme.key_actionBarDefault));
        actionBar.setItemsColor(getThemedColor(Theme.key_actionBarDefaultIcon), false);
        actionBar.setItemsBackgroundColor(getThemedColor(Theme.key_actionBarDefaultSelector), false);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (
                        id == SubItem.TREE_DOTS.ordinal() ||
                                id == SubItem.RESET_ORDER.ordinal() ||
                                id == SubItem.ADD_DIVIDER.ordinal()
                ) {
                    onMenuItemClick(id);
                }
            }
        });

        menuItem = createMenuItem();

        listAdapter = new ListAdapter();
        listView = new RecyclerListView(context);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setAdapter(listAdapter);
        listView.setVerticalScrollBarEnabled(false);

        DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        itemAnimator.setDelayAnimations(false);
        listView.setItemAnimator(itemAnimator);

        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        itemTouchHelper = new ItemTouchHelper(new TouchHelperCallback());
        itemTouchHelper.attachToRecyclerView(listView);

        fragmentView = frameLayout;
        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null && listView != null) {
            listView.post(() -> listAdapter.notifyItemRangeChanged(0, listAdapter.getItemCount()));
        }
    }

    @Override
    public void clearViews() {
        super.clearViews();
        if (listView != null) {
            listView.setAdapter(null);
            listView = null;
        }
        listAdapter = null;
    }

    protected void updateRowsId() {
        rowCount = 0;
        headerSuggestedOptionsRow = -1;
        hintsDividerRow = -1;

        int size_hints = DrawerOrderController.sizeHints();
        headerHintRow = rowCount++;

        if (size_hints > 0) {
            headerSuggestedOptionsRow = rowCount++;
            menuHintsStartRow = rowCount;
            rowCount += size_hints;
            menuHintsEndRow = rowCount;
            hintsDividerRow = rowCount++;
        }

        if (!TextUtils.isEmpty(OctoConfig.INSTANCE.drawerItems.getValue())) {
            headerMenuRow = rowCount++;
        }
        menuItemsStartRow = rowCount;
        rowCount += DrawerOrderController.sizeAvailable();
        menuItemsEndRow = rowCount;
        menuItemsDividerRow = rowCount++;
    }

    public void updateListAnimated(@Nullable Runnable callback) {
        if (listAdapter == null) {
            updateRowsId();
            return;
        }

        DiffCallback diffCallback = new DiffCallback();
        diffCallback.oldRowCount = rowCount;
        diffCallback.fillPositions(diffCallback.oldPositionToItem);
        diffCallback.oldMenuHints.clear();
        diffCallback.oldMenuItems.clear();

        diffCallback.oldMenuHints.add(null);
        for (int i = 0; i < DrawerOrderController.sizeHints(); i++) {
            diffCallback.oldMenuHints.add(DrawerOrderController.getSingleNotAvailableMenuItem(i));
        }
        diffCallback.oldMenuHints.add(null);
        for (int i = 0; i < DrawerOrderController.sizeAvailable(); i++) {
            diffCallback.oldMenuItems.add(DrawerOrderController.getSingleAvailableMenuItem(i));
        }

        diffCallback.oldMenuHintsStartRow = headerSuggestedOptionsRow;
        diffCallback.oldMenuHintsEndRow = hintsDividerRow;
        diffCallback.oldMenuItemsStartRow = menuItemsStartRow;
        diffCallback.oldMenuItemsEndRow = menuItemsEndRow;

        if (callback != null) {
            callback.run();
        }

        updateRowsId();
        diffCallback.fillPositions(diffCallback.newPositionToItem);

        try {
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
            diffResult.dispatchUpdatesTo(listAdapter);
        } catch (Exception e) {
            OctoLogging.e(TAG, e);
            if (listAdapter != null && listView != null) {
                listView.post(() -> listAdapter.notifyItemRangeChanged(0, listAdapter.getItemCount()));
            }
        }

        if (listView != null) {
            AndroidUtilities.updateVisibleRows(listView);
        }
    }

    private void reloadMainInfo() {
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.reloadInterface);
    }


    enum SubItem {
        TREE_DOTS,
        ADD_DIVIDER,
        RESET_ORDER,
    }

    enum ViewType {
        HINT_HEADER, HEADER, MENU_ITEM, SUGGESTED_OPTIONS, SHADOW;

        static ViewType fromInt(int i) {
            return values()[i];
        }
    }

    private class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public int getItemViewType(int position) {
            if (position == hintsDividerRow || position == menuItemsDividerRow) {
                return ViewType.SHADOW.ordinal();
            } else if (position == headerHintRow) {
                return ViewType.HINT_HEADER.ordinal();
            } else if (position == headerSuggestedOptionsRow || position == headerMenuRow) {
                return ViewType.HEADER.ordinal();
            } else if (position >= menuItemsStartRow && position < menuItemsEndRow) {
                return ViewType.MENU_ITEM.ordinal();
            } else if (position >= menuHintsStartRow && position < menuHintsEndRow) {
                return ViewType.SUGGESTED_OPTIONS.ordinal();
            }
            return -1;
        }

        @SuppressLint("ClickableViewAccessibility")
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (ViewType.fromInt(viewType)) {
                case HINT_HEADER -> {
                    view = new HintHeader(context, getString(R.string.MenuItemsOrderDesc));
                    view.setBackground(Theme.getThemedDrawable(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                }
                case MENU_ITEM -> {
                    SwapOrder swapOrderCell = new SwapOrder(context);
                    swapOrderCell.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
                    swapOrderCell.setOnReorderButtonTouchListener((View v, MotionEvent event) -> {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            itemTouchHelper.startDrag(listView.getChildViewHolder(swapOrderCell));
                        }
                        return false;
                    });
                    swapOrderCell.setOnDeleteClick(v -> {
                        int index = listView.getChildViewHolder(swapOrderCell).getAdapterPosition();
                        int position = index - menuItemsStartRow;
                        if (DrawerOrderController.isAvailable(swapOrderCell.menuId, position)) {
                            updateListAnimated(() -> DrawerOrderController.removeItem(position));

                            if (menuItem != null) {
                                if (DrawerOrderController.isDefaultPosition()) {
                                    menuItem.hideSubItem(SubItem.RESET_ORDER.ordinal());
                                } else {
                                    menuItem.showSubItem(SubItem.RESET_ORDER.ordinal());
                                }
                            }
                            reloadMainInfo();
                        }
                    });
                    view = swapOrderCell;
                }
                case SUGGESTED_OPTIONS -> {
                    AddItem addItemCell = new AddItem(context);
                    addItemCell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    addItemCell.setAddOnClickListener(v -> {
                        if (!DrawerOrderController.isAvailable(addItemCell.menuId)) {
                            int index = DrawerOrderController.getPositionOf(addItemCell.menuId);
                            updateListAnimated(() -> {
                                if (index != -1) {
                                    DrawerOrderController.addItem(addItemCell.menuId);
                                }
                            });
                            if (DrawerOrderController.isDefaultPosition()) {
                                menuItem.hideSubItem(SubItem.RESET_ORDER.ordinal());
                            } else {
                                menuItem.showSubItem(SubItem.RESET_ORDER.ordinal());
                            }
                            reloadMainInfo();
                        }
                    });
                    view = addItemCell;
                }
                case SHADOW -> view = new ShadowSectionCell(context);
                case HEADER -> {
                    view = new HeaderCell(context);
                    view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
                }
                default -> view = new View(context);
            }
            return new RecyclerView.ViewHolder(view) {
            };
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            switch (ViewType.fromInt(holder.getItemViewType())) {
                case ViewType.HINT_HEADER -> {
                }
                case ViewType.SHADOW ->
                        holder.itemView.setBackground(Theme.getThemedDrawable(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                case ViewType.HEADER -> {
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == headerSuggestedOptionsRow) {
                        headerCell.setText(getString(R.string.RecommendedItems));
                    } else if (position == headerMenuRow) {
                        headerCell.setText(getString(R.string.MenuItems));
                    }
                }
                case ViewType.MENU_ITEM -> {
                    SwapOrder swapOrderCell = (SwapOrder) holder.itemView;
                    DrawerOrderController.EditableMenuItem menuItem = DrawerOrderController.getSingleAvailableMenuItem(position - menuItemsStartRow);
                    if (menuItem != null) {
                        swapOrderCell.setData(menuItem.text, menuItem.isDefault, menuItem.isPremium, menuItem.id, position != menuItemsEndRow - 1);
                    }
                }
                case ViewType.SUGGESTED_OPTIONS -> {
                    AddItem addItemCell = (AddItem) holder.itemView;
                    DrawerOrderController.EditableMenuItem notData = DrawerOrderController.getSingleNotAvailableMenuItem(position - menuHintsStartRow);
                    if (notData != null) {
                        addItemCell.setData(notData.text, notData.id, notData.isPremium, true);
                    }
                }
                default -> throw new IllegalArgumentException("Invalid view type");
            }
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        public void swapElements(int fromIndex, int toIndex) {
            int idx1 = fromIndex - menuItemsStartRow;
            int idx2 = toIndex - menuItemsStartRow;
            int count = menuItemsEndRow - menuItemsStartRow;
            if (idx1 < 0 || idx2 < 0 || idx1 >= count || idx2 >= count) {
                return;
            }
            DrawerOrderController.changePosition(idx1, idx2);
            notifyItemMoved(fromIndex, toIndex);
            if (DrawerOrderController.isDefaultPosition()) {
                menuItem.hideSubItem(SubItem.RESET_ORDER.ordinal());
            } else {
                menuItem.showSubItem(SubItem.RESET_ORDER.ordinal());
            }
            reloadMainInfo();
        }
    }

    public class TouchHelperCallback extends ItemTouchHelper.Callback {

        @Override
        public boolean isLongPressDragEnabled() {
            return super.isLongPressDragEnabled();
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            if (viewHolder.getItemViewType() != ViewType.MENU_ITEM.ordinal()) {
                return makeMovementFlags(0, 0);
            }
            return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
            if (source.getItemViewType() != target.getItemViewType()) {
                return false;
            }
            listAdapter.swapElements(source.getAdapterPosition(), target.getAdapterPosition());
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

    private class DiffCallback extends DiffUtil.Callback {
        int oldRowCount;
        SparseIntArray oldPositionToItem = new SparseIntArray();
        SparseIntArray newPositionToItem = new SparseIntArray();
        ArrayList<DrawerOrderController.EditableMenuItem> oldMenuHints = new ArrayList<>();
        ArrayList<DrawerOrderController.EditableMenuItem> oldMenuItems = new ArrayList<>();
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
                    DrawerOrderController.EditableMenuItem oldItem = oldMenuHints.get(oldItemPosition - oldMenuHintsStartRow);
                    DrawerOrderController.EditableMenuItem newItem = DrawerOrderController.getSingleNotAvailableMenuItem(newItemPosition - menuHintsStartRow);
                    if (oldItem == null || newItem == null) {
                        return false;
                    }
                    return Objects.equals(oldItem.id, newItem.id);
                }
            }
            if (newItemPosition >= menuItemsStartRow && newItemPosition < menuItemsEndRow) {
                if (oldItemPosition >= oldMenuItemsStartRow && oldItemPosition < oldMenuItemsEndRow) {
                    DrawerOrderController.EditableMenuItem oldItem = oldMenuItems.get(oldItemPosition - oldMenuItemsStartRow);
                    DrawerOrderController.EditableMenuItem newItem = DrawerOrderController.getSingleAvailableMenuItem(newItemPosition - menuItemsStartRow);
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
            if (DrawerOrderController.sizeHints() > 0) {
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