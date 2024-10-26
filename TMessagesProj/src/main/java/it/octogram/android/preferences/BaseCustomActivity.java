package it.octogram.android.preferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.INavigationLayout;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.Cells.CreationTextCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ManageChatTextCell;
import org.telegram.ui.Cells.ManageChatUserCell;
import org.telegram.ui.Cells.RadioCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextCheckCell2;
import org.telegram.ui.Cells.TextCheckbox2Cell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextRadioCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Cells.UserCell2;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.FlickerLoadingView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.List;

import it.octogram.android.ViewType;

public class BaseCustomActivity extends BaseFragment {
    protected static final Object PARTIAL = new Object();
    protected int rowCount;
    protected BaseCustomViewAdapter listAdapter;
    protected Context context;
    protected RecyclerListView listView;
    protected ActionBarMenuItem menuItem;
    protected EmptyTextProgressView emptyView;
    private BaseFragment parentFragment;

    protected String getActionBarTitle() {
        return null;
    }

    protected ActionBarMenuItem createMenuItem() {
        return null;
    }

    protected boolean haveEmptyView() {
        return false;
    }

    
    @Override
    public View createView(Context context) {
        this.context = context;
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(getActionBarTitle());
        actionBar.setAllowOverlayTitle(true);
        menuItem = createMenuItem();
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    if (onBackPressed()) {
                        finishFragment();
                    }
                } else {
                    onMenuItemClick(id);
                }
            }
        });

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        listView = new RecyclerListView(context);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setVerticalScrollBarEnabled(false);
        DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        itemAnimator.setDelayAnimations(false);
        listView.setItemAnimator(itemAnimator);
        listView.setAdapter(listAdapter = createAdapter());
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setOnItemClickListener(this::onItemClick);
        listView.setOnItemLongClickListener(this::onItemLongClick);

        if (haveEmptyView()) {
            emptyView = new EmptyTextProgressView(context);
            emptyView.setText(LocaleController.getString(R.string.NoResult));
            emptyView.showTextView();
            emptyView.setShowAtCenter(true);
            frameLayout.addView(emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
            listView.setEmptyView(emptyView);
            listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
                    }
                }
            });
        }

        parentFragment = parentLayout.getLastFragment();
        return fragmentView;
    }

    protected void hideLastFragment() {
        hideLastFragment(parentFragment, parentLayout);
    }

    protected void showLastFragment() {
        showLastFragment(parentFragment, parentLayout);
    }

    public static void hideLastFragment(BaseFragment parentFragment, INavigationLayout parentLayout) {
        if (parentFragment != null && parentLayout.getFragmentStack().contains(parentFragment)) {
            parentLayout.removeFragmentFromStack(parentFragment);
        }
    }

    public static void showLastFragment(BaseFragment parentFragment, INavigationLayout parentLayout) {
        if (parentFragment != null && !parentLayout.getFragmentStack().contains(parentFragment)) {
            int position = parentLayout.getFragmentStack().size() - 1;
            parentFragment.rebuild();
            parentLayout.addFragmentToStack(parentFragment, position);
        }
    }

    protected void onItemClick(View view, int position, float x, float y) {

    }

    protected boolean onItemLongClick(View view, int position, float x, float y) {
        return false;
    }

    protected void onMenuItemClick(int id) {

    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume() {
        super.onResume();
        updateRowsId();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    protected void updateRowsId() {
        rowCount = 0;
    }

    protected BaseCustomViewAdapter createAdapter() {
        return null;
    }

    public class BaseCustomViewAdapter extends RecyclerListView.SelectionAdapter {
        @Override
        public int getItemCount() {
            return rowCount;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List payloads) {
            Object payload = payloads.isEmpty() ? null : payloads.get(0);
            onBindViewHolder(holder, position, PARTIAL.equals(payload));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            onBindViewHolder(holder, position, false);
        }

        protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, boolean partial) {

        }

        protected ViewType getViewType(int position) {
            return null;
        }

        protected boolean isEnabled(ViewType viewType, int position) {
            return false;
        }

        @NonNull
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            boolean canSetBackground = true;
            ViewType type = ViewType.Companion.fromInt(viewType);
            switch (type) {
                case SHADOW:
                    view = new ShadowSectionCell(context);
                    break;
                case HEADER:
                case HEADER_NO_SHADOW:
                    view = new HeaderCell(context);
                    break;
                case CHECKBOX:
                    view = new TextCheckbox2Cell(context);
                    break;
                case TEXT_RADIO:
                    view = new TextRadioCell(context);
                    break;
                case SWITCH:
                    view = new TextCheckCell(context);
                    break;
                case TEXT_CELL:
                    view = new TextCell(context);
                    break;
                case TEXT_HINT:
                    view = new TextInfoPrivacyCell(context);
                    break;
                case TEXT_HINT_WITH_PADDING:
                    TextInfoPrivacyCell textInfoPrivacyCell = new TextInfoPrivacyCell(context);
                    textInfoPrivacyCell.setBottomPadding(16);
                    view = textInfoPrivacyCell;
                    break;
                case SETTINGS:
                    view = new TextSettingsCell(context);
                    break;
                case DETAILED_SETTINGS:
                    view = new TextDetailSettingsCell(context);
                    break;
                case ADD_EXCEPTION:
                    view = new ManageChatTextCell(context);
                    break;
                case RADIO:
                    view = new RadioCell(context);
                    break;
                case MANAGE_CHAT:
                    view = new ManageChatUserCell(context, 7, 6, true);
                    break;
                case CHAT:
                    view = new UserCell2(context, 4, 0);
                    break;
                case PLACEHOLDER:
                    view = new FlickerLoadingView(parent.getContext());
                    break;
                case CREATION_TEXT_CELL:
                    CreationTextCell creationTextCell = new CreationTextCell(context);
                    creationTextCell.startPadding = 61;
                    view = creationTextCell;
                    break;
                case TEXT_CHECK_CELL2:
                    view = new TextCheckCell2(context);
                    break;
                case CHECKBOX_CELL:
                    CheckBoxCell checkBoxCell = new CheckBoxCell(context, 4, 21, getResourceProvider());
                    checkBoxCell.getCheckBoxRound().setDrawBackgroundAsArc(14);
                    checkBoxCell.getCheckBoxRound().setColor(Theme.key_switch2TrackChecked, Theme.key_radioBackground, Theme.key_checkboxCheck);
                    checkBoxCell.setEnabled(true);
                    view = checkBoxCell;
                    break;
                default:
                    view = onCreateViewHolder(type);
                    canSetBackground = false;
                    if (view == null)
                        throw new IllegalArgumentException("Unknown viewType: " + viewType);
            }
            switch (ViewType.Companion.fromInt(viewType)) {
                case HEADER_NO_SHADOW:
                case TEXT_HINT:
                case TEXT_HINT_WITH_PADDING:
                    break;
                default:
                    if (canSetBackground)
                        view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        protected View onCreateViewHolder(ViewType viewType) {
            return null;
        }

        @Override
        public int getItemViewType(int position) {
            return getViewType(position).toInt();
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return isEnabled(ViewType.Companion.fromInt(holder.getItemViewType()), holder.getAdapterPosition());
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void notifyDataSetChanged() {
            if (listView.isComputingLayout()) {
                listView.post(this::notifyDataSetChanged);
                return;
            }
            super.notifyDataSetChanged();
        }

        @Override
        public void notifyItemChanged(int position) {
            if (listView.isComputingLayout()) {
                listView.post(() -> notifyItemChanged(position));
                return;
            }
            super.notifyItemChanged(position);
        }

        @Override
        public void notifyItemChanged(int position, @Nullable Object payload) {
            if (listView.isComputingLayout()) {
                listView.post(() -> notifyItemChanged(position, payload));
                return;
            }
            super.notifyItemChanged(position, payload);
        }

        @Override
        public void notifyItemRangeChanged(int positionStart, int itemCount) {
            if (listView.isComputingLayout()) {
                listView.post(() -> notifyItemRangeChanged(positionStart, itemCount));
                return;
            }
            super.notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void notifyItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            if (listView.isComputingLayout()) {
                listView.post(() -> notifyItemRangeChanged(positionStart, itemCount, payload));
                return;
            }
            super.notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void notifyItemInserted(int position) {
            if (listView.isComputingLayout()) {
                listView.post(() -> notifyItemInserted(position));
                return;
            }
            super.notifyItemInserted(position);
        }

        @Override
        public void notifyItemMoved(int fromPosition, int toPosition) {
            if (listView.isComputingLayout()) {
                listView.post(() -> notifyItemMoved(fromPosition, toPosition));
                return;
            }
            super.notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void notifyItemRangeInserted(int positionStart, int itemCount) {
            if (listView.isComputingLayout()) {
                listView.post(() -> notifyItemRangeInserted(positionStart, itemCount));
                return;
            }
            super.notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void notifyItemRangeRemoved(int positionStart, int itemCount) {
            if (listView.isComputingLayout()) {
                listView.post(() -> notifyItemRangeRemoved(positionStart, itemCount));
                return;
            }
            super.notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void notifyItemRemoved(int position) {
            if (listView.isComputingLayout()) {
                listView.post(() -> notifyItemRemoved(position));
                return;
            }
            super.notifyItemRemoved(position);
        }


        public void swapElements(int adapterPosition, int adapterPosition1) {
        }
    }


    protected void reloadInterface() {
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.reloadInterface);
    }

    protected void reloadMainInfo() {
        getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
    }

    protected void reloadDialogs() {
        getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
    }

    protected void rebuildAllFragmentsWithLast() {
        Parcelable recyclerViewState = null;
        if (listView.getLayoutManager() != null) {
            recyclerViewState = listView.getLayoutManager().onSaveInstanceState();
        }
        parentLayout.rebuildFragments(INavigationLayout.REBUILD_FLAG_REBUILD_LAST);
        listView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
    }
}