/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Adapters.FiltersView;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ListView.AdapterWithDiffUtils;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SearchViewPager;
import org.telegram.ui.Components.ShareAlert;
import org.telegram.ui.LaunchActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.octogram.android.OctoConfig;
import it.octogram.android.app.ui.cells.ChatSettingsPreviewsCell;
import it.octogram.android.utils.AppRestartHelper;
import it.octogram.android.utils.config.SearchOptionsOrderController;
import it.octogram.android.utils.deeplink.DeepLinkDef;

public class OctoGeneralSearchOrderUI extends BaseFragment {

    private RecyclerListView listView;
    private ListAdapter adapter;
    private ItemTouchHelper itemTouchHelper;

    private boolean edited = false;
    private Context context;

    private final List<Integer> availableOptions = new ArrayList<>();
    {
        availableOptions.addAll(SearchOptionsOrderController.getCurrentOrder());
    }

    public class ItemCell extends FrameLayout {

        private final SimpleTextView textView;
        private final ImageView moveImageView;

        public ItemCell(Context context) {
            super(context);
            setWillNotDraw(false);

            moveImageView = new ImageView(context);
            moveImageView.setFocusable(false);
            moveImageView.setScaleType(ImageView.ScaleType.CENTER);
            moveImageView.setImageResource(R.drawable.list_reorder);
            moveImageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_stickers_menu), PorterDuff.Mode.MULTIPLY));
            moveImageView.setContentDescription(LocaleController.getString(R.string.FilterReorder));
            moveImageView.setClickable(true);
            addView(moveImageView, LayoutHelper.createFrame(48, 48, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL, 7, 0, 6, 0));

            textView = new SimpleTextView(context);
            textView.setPadding(0, dp(4), 0, dp(4));
            textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            textView.setTextSize(16);
            textView.setMaxLines(1);
            textView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
            textView.setEmojiColor(Theme.getColor(Theme.key_featuredStickers_addButton, resourceProvider));
            addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 80 : 64, 10, LocaleController.isRTL ? 64 : 80, 0));
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(dp(50), MeasureSpec.EXACTLY));
        }
        
        public void setId(int itemId) {
            textView.setText(getItemName(itemId));
        }
        
        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            if (!OctoConfig.INSTANCE.disableDividers.getValue()) {
                canvas.drawLine(LocaleController.isRTL ? 0 : dp(62), getMeasuredHeight() - 1, getMeasuredWidth() - (LocaleController.isRTL ? dp(62) : 0), getMeasuredHeight() - 1, Theme.dividerPaint);
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        public void setOnReorderButtonTouchListener(OnTouchListener listener) {
            moveImageView.setOnTouchListener(listener);
        }
    }
    
    public static String getItemName(int itemId) {
        if (itemId == SearchViewPager.ViewPagerAdapter.DIALOGS_TYPE) {
            return getString(R.string.SearchAllChatsShort);
        } else if (itemId == SearchViewPager.ViewPagerAdapter.CHANNELS_TYPE) {
            return getString(R.string.ChannelsTab);
        } else if (itemId == SearchViewPager.ViewPagerAdapter.BOTS_TYPE) {
            return getString(R.string.AppsTab);
        } else if (itemId == SearchViewPager.ViewPagerAdapter.POSTS_TYPE) {
            return getString(R.string.SearchPosts);
        } else if (itemId == SearchViewPager.ViewPagerAdapter.DOWNLOADS_TYPE) {
            return getString(R.string.DownloadsTabs);
        } else if (itemId == SearchViewPager.ViewPagerAdapter.PUBLIC_POSTS_TYPE) {
            return getString(R.string.PublicPostsTabs);
        } else if (itemId <= -1 && FiltersView.filters.length > (-itemId - 1)) {
            return FiltersView.filters[-itemId - 1].getTitle();
        }
        return "-";
    }

    private final ArrayList<ItemInner> oldItems = new ArrayList<>();
    private final ArrayList<ItemInner> items = new ArrayList<>();

    private int filtersSectionStart = -1, filtersSectionEnd = -1;

    private void updateRows(boolean animated) {
        oldItems.clear();
        oldItems.addAll(items);
        items.clear();

        items.add(ItemInner.asMiniHeader(getString(R.string.SearchItems_items)));
        items.add(ItemInner.asPreview());
        filtersSectionStart = items.size();
        for (int option : availableOptions) {
            items.add(ItemInner.asItem(option));
        }
        filtersSectionEnd = items.size();

        items.add(ItemInner.asInfo(getString(R.string.SearchItems_Customize)));

        if (adapter != null) {
            if (animated) {
                adapter.setItems(oldItems, items);
            } else {
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public View createView(Context context) {
        this.context = context;

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(getString(R.string.SearchItems));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == 1) {
                    edited = SearchOptionsOrderController.isUsingCustomVersion();
                    SearchOptionsOrderController.resetOrdering();
                    availableOptions.clear();
                    availableOptions.addAll(SearchOptionsOrderController.getCurrentOrder());
                    updateRows(true);
                }
            }
        });

        actionBar.setLongClickable(true);
        actionBar.setOnLongClickListener(v -> {
            String link = String.format(Locale.US, "tg://%s", DeepLinkDef.GENERAL_SEARCHORDER);
            showDialog(new ShareAlert(context, null, link, false, link, false, true));
            return true;
        });

        ActionBarMenu menu = actionBar.createMenu();
        ActionBarMenuItem menuItem = menu.addItem(OctoAppearanceDrawerOrderUI.SubItem.TREE_DOTS.ordinal(), R.drawable.ic_ab_other);
        menuItem.setContentDescription(getString(R.string.AccDescrMoreOptions));
        menuItem.addSubItem(1, R.drawable.msg_reset, getString(R.string.ResetItemsOrder));

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        listView = new RecyclerListView(context) {
            @Override
            protected void dispatchDraw(Canvas canvas) {
                drawSectionBackground(canvas, filtersSectionStart, filtersSectionEnd - 1, Theme.getColor(Theme.key_windowBackgroundWhite));
                super.dispatchDraw(canvas);
            }
        };
        DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setDurations(350);
        itemAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        itemAnimator.setDelayAnimations(false);
        itemAnimator.setSupportsChangeAnimations(false);
        listView.setItemAnimator(itemAnimator);
        ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setVerticalScrollBarEnabled(false);
        itemTouchHelper = new ItemTouchHelper(new TouchHelperCallback());
        itemTouchHelper.attachToRecyclerView(listView);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setAdapter(adapter = new ListAdapter(context));

        updateRows(false);

        return fragmentView;
    }

    @Override
    public boolean onBackPressed() {
        if (edited) {
            showRestartDialog();
            return false;
        }

        return super.onBackPressed();
    }

    @Override
    public boolean canBeginSlide() {
        if (edited) {
            showRestartDialog();
            return false;
        }

        return super.canBeginSlide();
    }


    public void showRestartDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(getString(R.string.AppName));
        alertDialogBuilder.setMessage(getString(R.string.RestartAppToApplyChanges));
        alertDialogBuilder.setPositiveButton(getString(R.string.RestartAppToApplyChangesButton), (v, d) -> {
            v.dismiss();
            AppRestartHelper.triggerRebirth(context, new Intent(context, LaunchActivity.class));
        });
        alertDialogBuilder.setNegativeButton(getString(R.string.Cancel), null);
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
        dialog.redPositive();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private static final int VIEW_TYPE_MINIHEADER = 1;
    private static final int VIEW_TYPE_ITEM = 2;
    private static final int VIEW_TYPE_PREVIEW = 3;
    private static final int VIEW_TYPE_INFO = 4;

    private static class ItemInner extends AdapterWithDiffUtils.Item {
        public ItemInner(int viewType) {
            super(viewType, false);
        }

        CharSequence text;
        int itemId;

        public static ItemInner asPreview() {
            return new ItemInner(VIEW_TYPE_PREVIEW);
        }

        public static ItemInner asMiniHeader(String text) {
            ItemInner item = new ItemInner(VIEW_TYPE_MINIHEADER);
            item.text = text;
            return item;
        }

        public static ItemInner asInfo(String text) {
            ItemInner item = new ItemInner(VIEW_TYPE_INFO);
            item.text = text;
            return item;
        }

        public static ItemInner asItem(int id) {
            ItemInner item = new ItemInner(VIEW_TYPE_ITEM);
            item.itemId = id;
            return item;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof ItemInner other)) {
                return false;
            }
            if (other.viewType != viewType) {
                return false;
            }
            if (viewType == VIEW_TYPE_MINIHEADER || viewType == VIEW_TYPE_INFO) {
                if (!TextUtils.equals(text, other.text)) {
                    return false;
                }
            }
            if (viewType == VIEW_TYPE_ITEM) {
                return itemId == other.itemId;
            }
            return true;
        }
    }

    private class ListAdapter extends AdapterWithDiffUtils {

        private final Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == VIEW_TYPE_ITEM;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = null;
            switch (viewType) {
                case VIEW_TYPE_MINIHEADER:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_ITEM:
                    ItemCell cell = new ItemCell(mContext);
                    cell.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    cell.setOnReorderButtonTouchListener((v, event) -> {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            itemTouchHelper.startDrag(listView.getChildViewHolder(cell));
                        }
                        return false;
                    });
                    view = cell;
                    break;
                case VIEW_TYPE_INFO:
                    view = new TextInfoPrivacyCell(mContext);
                    view.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider, getThemedColor(Theme.key_windowBackgroundGrayShadow)));
                    break;
                case VIEW_TYPE_PREVIEW:
                    view = new ChatSettingsPreviewsCell(mContext, ChatSettingsPreviewsCell.PreviewType.SEARCH_ORDER);
                    break;
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ItemInner item = items.get(position);
            if (item == null) {
                return;
            }
            switch (holder.getItemViewType()) {
                case VIEW_TYPE_MINIHEADER: {
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    headerCell.setText(item.text);
                    break;
                }
                case VIEW_TYPE_ITEM: {
                    ItemCell cell = (ItemCell) holder.itemView;
                    cell.setId(item.itemId);
                    break;
                }
                case VIEW_TYPE_PREVIEW: {
                    ChatSettingsPreviewsCell cell = (ChatSettingsPreviewsCell) holder.itemView;
                    cell.invalidate();
                    break;
                }
                case VIEW_TYPE_INFO:
                    ((TextInfoPrivacyCell) holder.itemView).setText(item.text);
                    break;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position < 0 || position >= items.size()) {
                return VIEW_TYPE_MINIHEADER;
            }
            ItemInner item = items.get(position);
            return item.viewType;
        }

        public void swapElements(int fromPosition, int toPosition) {
            if (fromPosition < filtersSectionStart || toPosition < filtersSectionStart) {
                return;
            }


            int currentOption = availableOptions.get(fromPosition - 2);
            availableOptions.set(fromPosition - 2, availableOptions.get(toPosition - 2));
            availableOptions.set(toPosition - 2, currentOption);

            updateRows(true);
        }
    }

    public class TouchHelperCallback extends ItemTouchHelper.Callback {
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            if (viewHolder.getItemViewType() != VIEW_TYPE_ITEM) {
                return makeMovementFlags(0, 0);
            }
            return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
            if (source.getItemViewType() != target.getItemViewType()) {
                return false;
            }
            adapter.swapElements(source.getAdapterPosition(), target.getAdapterPosition());
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
            } else {
                SearchOptionsOrderController.saveCurrentOrder(availableOptions);
                adapter.notifyItemChanged(1);
                edited = true;
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
}
