/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */

package it.octogram.android.preferences.fragment;

import static androidx.recyclerview.widget.LinearLayoutManager.VERTICAL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.*;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.Premium.PremiumFeatureBottomSheet;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SlideChooseView;
import org.telegram.ui.Components.UndoView;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import it.octogram.android.OctoConfig;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferenceType;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.rows.BaseRow;
import it.octogram.android.preferences.rows.Clickable;
import it.octogram.android.preferences.rows.cells.SliderCell;
import it.octogram.android.preferences.rows.impl.CheckboxRow;
import it.octogram.android.preferences.rows.impl.CustomCellRow;
import it.octogram.android.preferences.rows.impl.ListRow;
import it.octogram.android.preferences.rows.impl.SliderChooseRow;
import it.octogram.android.preferences.rows.impl.SliderRow;
import it.octogram.android.preferences.rows.impl.StickerHeaderRow;
import it.octogram.android.preferences.rows.impl.SwitchRow;
import it.octogram.android.preferences.rows.impl.TextDetailRow;
import it.octogram.android.preferences.rows.impl.TextIconRow;
import org.telegram.ui.PremiumPreviewFragment;

/*
 * This library is *heavily* inspired by CatoGramX's preferences library.
 */
public class PreferencesFragment extends BaseFragment {

    private final Object PARTIAL = new Object();

    private OctoPreferences preferences;
    private final SparseArray<BaseRow> positions = new SparseArray<>();
    private final PreferencesEntry entry;

    private ListAdapter listAdapter;
    private RecyclerListView listView;
    private int rowCount;
    private Context context;

    public PreferencesFragment(PreferencesEntry entry) {
        this.entry = entry;
    }

    private void updatePreferences() {
        OctoPreferences preferences = entry.getPreferences(this, context);
        List<BaseRow> preferenceList = preferences.getPreferences();

        if (OctoConfig.INSTANCE.disableDividers.getValue()) {
            for (BaseRow baseRow : preferenceList) {
                baseRow.setDivider(false);
            }
        }

        this.preferences = preferences;
    }

    public void updateRows() {
        positions.clear();
        rowCount = 0;
        for (BaseRow category : preferences.getPreferences()) {
            if (category.getShowIfPreferenceValue() != null && !category.getShowIfPreferenceValue().getValue()) {
                positions.put(-1, category);
                category.setRow(-1);
                continue;
            }
            positions.put(rowCount++, category);
            category.setRow(rowCount);
        }
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        return true;
    }

    @Override
    public View createView(Context context) {
        this.context = context;
        updatePreferences();
        updateRows();

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(preferences.getTitle());
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        actionBar.setAllowOverlayTitle(true);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        listAdapter = new ListAdapter(context);

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        listView = new RecyclerListView(context);
        listView.setVerticalScrollBarEnabled(false);
        listView.setLayoutManager(new LinearLayoutManager(context, VERTICAL, false));
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setAdapter(listAdapter);

        UndoView restartTooltip = new UndoView(context);
        frameLayout.addView(restartTooltip, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.LEFT, 8, 0, 8, 8));

        listView.setOnItemClickListener((view, position, x, y) -> {
            BaseRow row = positions.get(position);
            if (row instanceof Clickable) {
                if (row.isPremium() && !UserConfig.getInstance(UserConfig.selectedAccount).isPremium()) {
                    showDialog(new PremiumFeatureBottomSheet(this, PremiumPreviewFragment.PREMIUM_FEATURE_ADVANCED_CHAT_MANAGEMENT, false));
                    return;
                }

                boolean success = ((Clickable) row).onClick(this, getParentActivity(), view, position, x, y);
                if (success) {
                    if (row.doesRequireRestart()) {
                        restartTooltip.showWithAction(0, UndoView.ACTION_NEED_RESTART, null, null);
                    }

                    if (row.getPostNotificationName() != null) {
                        Arrays.stream(row.getPostNotificationName()).forEach(NotificationCenter.getGlobalInstance()::postNotificationName);
                    }
                }
            }

            List<BaseRow> toShow = preferences.getPreferences().stream()
                    .filter(fi -> fi.getShowIfPreferenceValue() != null)
                    .filter(BaseRow::isCurrentlyHidden)
                    .filter(fi -> fi.getShowIfPreferenceValue().getValue())
                    .collect(Collectors.toList());
            if (!toShow.isEmpty()) {
                updateUI(toShow, true);
            }

            List<BaseRow> toHide = preferences.getPreferences().stream()
                    .filter(fi -> fi.getShowIfPreferenceValue() != null)
                    .filter(fi -> !fi.isCurrentlyHidden())
                    .filter(fi -> !fi.getShowIfPreferenceValue().getValue())
                    .collect(Collectors.toList());
            if (!toHide.isEmpty()) {
                updateUI(toHide, false);
            }
        });
        return fragmentView;
    }

    private void updateUI(List<BaseRow> rows, boolean show) {
        rows.forEach(row -> row.setCurrentlyHidden(!show));
        if (show) {
            updateRows();
        }
        int row = rows.get(0).getRow() - 1;
        int size = rows.size();
        if (show) {
            listAdapter.notifyItemRangeInserted(row, size);
        } else {
            listAdapter.notifyItemRangeRemoved(row, size);
        }
        listAdapter.notifyItemRangeChanged(row, size, PARTIAL);
        if (!show) {
            updateRows();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume() {
        super.onResume();
        updateRows();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    public RecyclerListView getListView() {
        return listView;
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private final Context context;

        protected ListAdapter(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            PreferenceType type = PreferenceType.fromAdapterType(viewType);
            if (type == null) {
                throw new RuntimeException("No type found for " + viewType);
            }

            switch (type) {
                case CUSTOM:
                case TEXT_ICON:
                    view = new FrameLayout(context);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case SHADOW:
                    view = new ShadowSectionCell(context);
                    break;
                case EMPTY_CELL:
                case HEADER:
                    view = new HeaderCell(context);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case SWITCH:
                    view = new TextCheckCell(context);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case TEXT_DETAIL:
                    view = new TextDetailSettingsCell(context);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case SLIDER:
                    view = new SliderCell(context);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case LIST:
                    view = new TextSettingsCell(context);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case SLIDER_CHOOSE:
                    view = new SlideChooseView(context);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case FOOTER:
                    TextInfoPrivacyCell cell = new TextInfoPrivacyCell(context, 10);
                    cell.getTextView().setGravity(Gravity.CENTER_HORIZONTAL);
                    cell.getTextView().setTextColor(getThemedColor(Theme.key_windowBackgroundWhiteGrayText3));
                    cell.getTextView().setMovementMethod(null);
                    cell.getTextView().setPadding(0, AndroidUtilities.dp(14), 0, AndroidUtilities.dp(14));
                    view = cell;
                    view.setBackground(Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, getThemedColor(Theme.key_windowBackgroundGrayShadow)));
                    break;
                case FOOTER_INFORMATIVE:
                    view = new TextInfoPrivacyCell(context);
                    view.setBackground(Theme.getThemedDrawable(context, R.drawable.greydivider, getThemedColor(Theme.key_windowBackgroundGrayShadow)));
                    break;
                case STICKER_HEADER:
                    LinearLayout layout = new LinearLayout(context);
                    layout.setGravity(Gravity.CENTER_HORIZONTAL);
                    layout.setOrientation(VERTICAL);
                    view = layout;
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case CHECKBOX:
                    CheckBoxCell checkBoxCell = new CheckBoxCell(context, 4, 21, getResourceProvider());
                    checkBoxCell.getCheckBoxRound().setDrawBackgroundAsArc(14);
                    checkBoxCell.getCheckBoxRound().setColor(Theme.key_switch2TrackChecked, Theme.key_radioBackground, Theme.key_checkboxCheck);
                    checkBoxCell.setEnabled(true);
                    view = checkBoxCell;
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                default:
                    throw new RuntimeException("No view found for " + type);
            }

            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            PreferenceType type = PreferenceType.fromAdapterType(holder.getItemViewType());
            if (type == null) {
                throw new RuntimeException("No type found for " + holder.getItemViewType());
            }

            switch (type) {
                case CUSTOM:
                    FrameLayout frameLayout = ((FrameLayout) holder.itemView);
                    // remove the current view if it exists
                    if (frameLayout.getChildCount() > 0) {
                        frameLayout.removeAllViews();
                    }
                    // add custom view
                    View view = ((CustomCellRow) positions.get(position)).getLayout();
                    frameLayout.addView(view);
                    break;
                case SHADOW:
                    holder.itemView.setBackground(Theme.getThemedDrawable(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case EMPTY_CELL:
                case HEADER:
                    ((HeaderCell) holder.itemView).setText(positions.get(position).getTitle());
                    break;
                case SWITCH:
                    TextCheckCell checkCell = (TextCheckCell) holder.itemView;
                    SwitchRow switchRow = (SwitchRow) positions.get(position);
                    if (switchRow.getSummary() != null) {
                        checkCell.setTextAndValueAndCheck(switchRow.getTitle(), switchRow.getSummary(), switchRow.getPreferenceValue(), true, switchRow.hasDivider());
                    } else {
                        checkCell.setTextAndCheck(switchRow.getTitle(), switchRow.getPreferenceValue(), switchRow.hasDivider());
                    }
                    checkCell.setCheckBoxIcon(switchRow.isPremium() ? R.drawable.permission_locked : 0);
                    break;
                case TEXT_DETAIL:
                    TextDetailRow textDetailRow = (TextDetailRow) positions.get(position);
                    textDetailRow.bindCell((TextDetailSettingsCell) holder.itemView);
                    TextDetailSettingsCell settingsCell = (TextDetailSettingsCell) holder.itemView;
                    settingsCell.setMultilineDetail(true);
                    textDetailRow.bindCell(settingsCell);
                    break;
                case TEXT_ICON:
                    FrameLayout l = ((FrameLayout) holder.itemView);
                    // remove the current view if it exists
                    if (l.getChildCount() > 0) {
                        l.removeAllViews();
                    }
                    // add custom view
                    TextIconRow textIconRow = (TextIconRow) positions.get(position);
                    TextCell v = new TextCell(context, 23, false, textIconRow.getPreference() != null, getResourceProvider());
                    l.addView(v);

                    textIconRow.bindCell(v);
                    break;
                case SLIDER:
                    ((SliderCell) holder.itemView).setSliderRow((SliderRow) positions.get(position));
                    break;
                case LIST:
                    TextSettingsCell listCell = (TextSettingsCell) holder.itemView;
                    ListRow listRow = (ListRow) positions.get(position);
                    listCell.setTextAndValue(listRow.getTitle(), listRow.getTextFromInteger(listRow.getCurrentValue().getValue()), true, listRow.hasDivider());
                    break;
                case SLIDER_CHOOSE:
                    SlideChooseView slideChooseView = (SlideChooseView) holder.itemView;
                    SliderChooseRow sliderRow = (SliderChooseRow) positions.get(position);
                    slideChooseView.setCallback(index -> {
                        int id = sliderRow.getIds().get(index);
                        OctoConfig.INSTANCE.updateIntegerSetting(sliderRow.getPreferenceValue(), sliderRow.getOptions().get(id).first);
                    });
                    slideChooseView.setOptions(sliderRow.getIntValue(), sliderRow.getValues());
                    break;
                case FOOTER_INFORMATIVE:
                case FOOTER:
                    ((TextInfoPrivacyCell) holder.itemView).setText(positions.get(position).getTitle());
                    break;
                case STICKER_HEADER:
                    StickerHeaderRow pref = (StickerHeaderRow) positions.get(position);
                    LinearLayout linearLayout = (LinearLayout) holder.itemView;

                    linearLayout.removeAllViews();
                    if (((View) pref.getStickerView()).getParent() != null) {
                        ((ViewGroup) ((View) pref.getStickerView()).getParent()).removeView((View) pref.getStickerView());
                    }

                    linearLayout.addView((View) pref.getStickerView(), LayoutHelper.createLinear(120, 120, Gravity.CENTER_HORIZONTAL, 0, 20, 0, 0));

                    if (pref.getSummary() != null) {
                        TextView textView = new TextView(context);
                        textView.setGravity(Gravity.CENTER_HORIZONTAL);
                        textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                        textView.setLinkTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkText));
                        textView.setHighlightColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkSelection));
                        textView.setText(pref.getSummary());
                        textView.setMovementMethod(new AndroidUtilities.LinkMovementMethodMy());
                        linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 36, 26, 36, 0));
                    }
                    break;
                case CHECKBOX:
                    CheckBoxCell checkboxCell = (CheckBoxCell) holder.itemView;
                    CheckboxRow checkboxRow = (CheckboxRow) positions.get(position);
                    checkboxCell.setText(checkboxRow.getTitle(), checkboxRow.getSummary(), checkboxRow.getPreferenceValue(), !OctoConfig.INSTANCE.disableDividers.getValue(), true);
                    checkboxCell.setIcon(checkboxRow.isPremium() ? R.drawable.permission_locked : 0);
                    break;
                default:
                    throw new RuntimeException("No view found for " + type);
            }
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return positions.get(holder.getAdapterPosition()).getType().isEnabled();
        }

        @Override
        public int getItemViewType(int position) {
            return positions.get(position).getType().getAdapterType();
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
    }


}
