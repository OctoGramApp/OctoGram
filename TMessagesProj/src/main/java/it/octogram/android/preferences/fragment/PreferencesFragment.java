/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

package it.octogram.android.preferences.fragment;

import static android.widget.LinearLayout.VERTICAL;
import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.ui.Components.LayoutHelper.createLinear;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.INavigationLayout;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.AnimatedTextView;
import org.telegram.ui.Components.CheckBox2;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.FireworksOverlay;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ListView.AdapterWithDiffUtils;
import org.telegram.ui.Components.Premium.PremiumFeatureBottomSheet;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SlideChooseView;
import org.telegram.ui.Components.Switch;
import org.telegram.ui.Components.UndoView;
import org.telegram.ui.PremiumPreviewFragment;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import it.octogram.android.OctoConfig;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferenceType;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.rows.BaseRow;
import it.octogram.android.preferences.rows.Clickable;
import it.octogram.android.preferences.rows.cells.SliderCell;
import it.octogram.android.preferences.rows.impl.CheckboxRow;
import it.octogram.android.preferences.rows.impl.CustomCellRow;
import it.octogram.android.preferences.rows.impl.ExpandableRows;
import it.octogram.android.preferences.rows.impl.ExpandableRowsChild;
import it.octogram.android.preferences.rows.impl.ListRow;
import it.octogram.android.preferences.rows.impl.SliderChooseRow;
import it.octogram.android.preferences.rows.impl.SliderRow;
import it.octogram.android.preferences.rows.impl.StickerHeaderRow;
import it.octogram.android.preferences.rows.impl.SwitchRow;
import it.octogram.android.preferences.rows.impl.TextDetailRow;
import it.octogram.android.preferences.rows.impl.TextIconRow;
import it.octogram.android.utils.ExpandableRowsOption;
import it.octogram.android.utils.OctoUtils;

public class PreferencesFragment extends BaseFragment {

    private OctoPreferences preferences;
    private final PreferencesEntry entry;

    private ListAdapter listAdapter;
    private RecyclerListView listView;
    private Context context;
    private UndoView restartTooltip;
    private FireworksOverlay fireworksOverlay;

    private static final ArrayList<Integer> expandedRowIds = new ArrayList<>();
    private static final Map<Integer, ExpandableRowIndex> expandableIndexes = new HashMap<>();
    private final ArrayList<BaseRow> oldItems = new ArrayList<>();
    private final ArrayList<BaseRow> currentShownItems = new ArrayList<>();
    private final List<BaseRow> reorderedPreferences = new ArrayList<>();

    private final HashSet<PreferenceType> typesWithDividerSupport = new HashSet<>();
    {
        typesWithDividerSupport.add(PreferenceType.SWITCH);
        typesWithDividerSupport.add(PreferenceType.CHECKBOX);
        typesWithDividerSupport.add(PreferenceType.LIST);
        typesWithDividerSupport.add(PreferenceType.TEXT_DETAIL);
        typesWithDividerSupport.add(PreferenceType.TEXT_ICON);
        typesWithDividerSupport.add(PreferenceType.EXPANDABLE_ROWS);
    }

    public PreferencesFragment(PreferencesEntry entry) {
        this.entry = entry;
    }

    private void updatePreferences() {
        OctoPreferences preferences = entry.getPreferences(this, context);

        for (BaseRow baseRow : preferences.preferences()) {
            reorderedPreferences.add(baseRow);

            if (baseRow instanceof ExpandableRows expandableRows) {
                for (ExpandableRowsOption item : expandableRows.getItemsList()) {
                    reorderedPreferences.add(new ExpandableRowsChild(item, expandableRows.getId()));
                }
            }
        }

        if (OctoConfig.INSTANCE.disableDividers.getValue()) {
            for (BaseRow baseRow : reorderedPreferences) {
                baseRow.setDivider(false);
            }
        }

        this.preferences = preferences;
    }

    private void loadContextMenu() {
        if (!preferences.elements().isEmpty()) {
            ActionBarMenu menu = actionBar.createMenu();

            int i = -1;
            ActionBarMenuItem menuItem = menu.addItem(++i, R.drawable.ic_ab_other);
            menuItem.setContentDescription(LocaleController.getString(R.string.AccDescrMoreOptions));

            for (OctoPreferences.OctoContextMenuElement element : preferences.elements()) {
                menuItem.addSubItem(++i, element.icon, element.title);
            }
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

        currentShownItems.clear();
        oldItems.clear();
        reorderedPreferences.clear();

        updatePreferences();

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(preferences.title());
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        actionBar.setAllowOverlayTitle(true);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id > 0) {
                    OctoPreferences.OctoContextMenuElement element = preferences.elements().get(id - 1);
                    if (element != null) {
                        element.run.run();
                    }
                }
            }
        });

        loadContextMenu();

        listAdapter = new ListAdapter();

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        listView = new RecyclerListView(context);

        DefaultItemAnimator itemAnimator = getDefaultItemAnimator();
        listView.setItemAnimator(itemAnimator);

        listView.setVerticalScrollBarEnabled(false);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setAdapter(listAdapter);

        restartTooltip = new UndoView(context);
        frameLayout.addView(restartTooltip, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.LEFT, 8, 0, 8, 8));

        listView.setOnItemClickListener((view, position, x, y) -> {
            BaseRow row = currentShownItems.get(position);
            if (row instanceof ExpandableRows expandableRow) {
                SwitchCell switchCell = (SwitchCell) view;
                if (LocaleController.isRTL ? x > dp(19 + 37 + 19) : x < view.getMeasuredWidth() - dp(19 + 37 + 19)) {
                    int expRowID = expandableRow.getId();
                    if (expandedRowIds.contains(expRowID)) {
                        expandedRowIds.removeIf(z -> z == expRowID);
                    } else {
                        expandedRowIds.add(expRowID);
                    }

                    switchCell.reload();
                } else {
                    boolean newState = !switchCell.switchView.isChecked();
                    for (ExpandableRowsOption item : expandableRow.getItemsList()) {
                        item.property.updateValue(newState);
                    }
                    switchCell.reload();
                    reloadSecondaryCellFromMain(switchCell);
                }
            } else if (row instanceof ExpandableRowsChild expandableRow) {
                SwitchCell switchCell = (SwitchCell) view;

                ExpandableRowsOption singleItem = expandableRow.getItem();
                singleItem.property.updateValue(!singleItem.property.getValue());

                switchCell.reload();
                reloadMainCellFromSecondary(switchCell);

                if (singleItem.onClick != null) {
                    singleItem.onClick.run();
                }
            } else if (row instanceof Clickable) {
                boolean isProceedingForPremiumAlert = false;
                if (row.isPremium() && !UserConfig.getInstance(UserConfig.selectedAccount).isPremium()) {
                    if (row.getAutoShowPremiumAlert()) {
                        showDialog(new PremiumFeatureBottomSheet(this, PremiumPreviewFragment.PREMIUM_FEATURE_ADVANCED_CHAT_MANAGEMENT, false));
                        return;
                    } else {
                        isProceedingForPremiumAlert = true;
                    }
                }

                boolean success;
                if (row instanceof ListRow) {
                    success = ((ListRow) row).onCustomClick(this, getParentActivity(), view, position, x, y, this::reloadUIAfterValueUpdate);
                } else {
                    success = ((Clickable) row).onClick(this, getParentActivity(), view, position, x, y);
                }
                if (success && !isProceedingForPremiumAlert) {
                    if (row.doesRequireRestart()) {
                        showRestartTooltip();
                    }

                    if (row.getPostNotificationName() != null) {
                        Arrays.stream(row.getPostNotificationName()).forEach(NotificationCenter.getGlobalInstance()::postNotificationName);
                    }
                }
            }

            reloadUIAfterValueUpdate();
        });

        reloadUIAfterValueUpdate();

        return fragmentView;
    }

    @NonNull
    private DefaultItemAnimator getDefaultItemAnimator() {
        DefaultItemAnimator itemAnimator = new DefaultItemAnimator() {
            @Override
            protected void onMoveAnimationUpdate(RecyclerView.ViewHolder holder) {
                super.onMoveAnimationUpdate(holder);
                fragmentView.invalidate();
            }
        };
        itemAnimator.setSupportsChangeAnimations(false);
        itemAnimator.setDelayAnimations(false);
        itemAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        itemAnimator.setDurations(350);
        return itemAnimator;
    }

    public void showRestartTooltip() {
        restartTooltip.showWithAction(0, UndoView.ACTION_NEED_RESTART, null, null);
    }

    public void smoothScrollToEnd() {
        listView.smoothScrollToPosition(currentShownItems.size() - 1);
    }

    public void reloadUIAfterValueUpdate() {
        oldItems.clear();
        oldItems.addAll(currentShownItems);
        currentShownItems.clear();

        for (int i = 0; i < reorderedPreferences.size(); i++) {
            BaseRow category = reorderedPreferences.get(i);

            if (!category.hasDivider() && !OctoConfig.INSTANCE.disableDividers.getValue()) {
                category.setDivider(true);
            }

            BaseRow nextElement = getNextVisibleElement(i, category);

            if (category.getType() == PreferenceType.SHADOW && nextElement != null && (nextElement.getType() == PreferenceType.FOOTER || nextElement.getType() == PreferenceType.FOOTER_INFORMATIVE)) {
                continue;
            }

            if (nextElement == null || !typesWithDividerSupport.contains(nextElement.getType())) {
                category.setDivider(false);
            }

            if (canShowItem(category)) {
                currentShownItems.add(category);
            }
        }

        listAdapter.setItems(oldItems, currentShownItems);
    }

    private BaseRow getNextVisibleElement(int i, BaseRow element) {
        if (i != reorderedPreferences.size() - 1) {
            boolean found = false;
            for (BaseRow category : reorderedPreferences) {
                if (category == element) {
                    found = true;
                } else if (found && canShowItem(category)) {
                    return category;
                }
            }
        }

        return null;
    }

    private void animateFireworksOverlay() {
        if (fireworksOverlay == null) {
            fireworksOverlay = new FireworksOverlay(getContext());
            ((FrameLayout) fragmentView).addView(fireworksOverlay, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        }

        if (!fireworksOverlay.isStarted()) {
            fireworksOverlay.start();
        }
    }

    private boolean canShowItem(BaseRow item) {
        if (item instanceof ExpandableRowsChild expandableRow) {
            return expandedRowIds.contains(expandableRow.getRefersToId());
        } else if (item.getShowIfPreferenceValue() != null) {
            return item.getShowIfReverse() != item.getShowIfPreferenceValue().getValue();
        }

        return true;
    }

    private void reloadSecondaryCellFromMain(SwitchCell mainCell) {
        for (ExpandableRowIndex index : expandableIndexes.values()) {
            if (index.mainCell == mainCell) {
                for (SwitchCell secondaryCell : index.secondaryCell) {
                    secondaryCell.reload();
                }
                break;
            }
        }
    }

    private void reloadMainCellFromSecondary(SwitchCell secondaryCell) {
        for (ExpandableRowIndex index : expandableIndexes.values()) {
            if (index.secondaryCell.contains(secondaryCell)) {
                index.mainCell.reload();
                break;
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume() {
        super.onResume();
    }

    public RecyclerListView getListView() {
        return listView;
    }

    private class ListAdapter extends AdapterWithDiffUtils {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final Context context = parent.getContext();
            View view;
            PreferenceType type = PreferenceType.Companion.fromAdapterType(viewType);
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
                case EXPANDABLE_ROWS:
                case EXPANDABLE_ROWS_CHILD:
                    view = new SwitchCell(context);
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
            if (position < 0 || position >= currentShownItems.size()) {
                return;
            }

            PreferenceType type = PreferenceType.Companion.fromAdapterType(holder.getItemViewType());
            if (type == null) {
                throw new RuntimeException("No type found for " + holder.getItemViewType());
            }

            switch (type) {
                case CUSTOM:
                    FrameLayout frameLayout = (FrameLayout) holder.itemView;
                    // remove the current view if it exists
                    if (frameLayout.getChildCount() > 0) {
                        frameLayout.removeAllViews();
                    }

                    // add custom view
                    CustomCellRow row = (CustomCellRow) currentShownItems.get(position);
                    if (row.getLayout() != null) {
                        View customView = row.getLayout();
                        ViewGroup parent = (ViewGroup) customView.getParent();
                        if (parent != null) {
                            parent.removeView(customView);
                        }
                        frameLayout.addView(customView);
                    }

                    break;
                case SHADOW:
                    holder.itemView.setBackground(Theme.getThemedDrawable(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case EMPTY_CELL:
                case HEADER:
                    ((HeaderCell) holder.itemView).setText(currentShownItems.get(position).getTitle());
                    break;
                case SWITCH:
                    TextCheckCell checkCell = (TextCheckCell) holder.itemView;
                    SwitchRow switchRow = (SwitchRow) currentShownItems.get(position);
                    if (switchRow.getSummary() != null) {
                        checkCell.setTextAndValueAndCheck(OctoUtils.safeToString(switchRow.getTitle()), switchRow.getSummary(), switchRow.getPreferenceValue(), true, switchRow.hasDivider());
                    } else {
                        checkCell.setTextAndCheck(switchRow.getTitle(), switchRow.getPreferenceValue(), switchRow.hasDivider());
                    }
                    checkCell.setCheckBoxIcon(switchRow.isPremium() || switchRow.isLocked() ? R.drawable.permission_locked : 0);
                    break;
                case TEXT_DETAIL:
                    TextDetailRow textDetailRow = (TextDetailRow) currentShownItems.get(position);
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
                    TextIconRow textIconRow = (TextIconRow) currentShownItems.get(position);
                    TextCell v = new TextCell(context, 23, false, textIconRow.getPreference() != null, getResourceProvider());
                    l.addView(v);

                    textIconRow.bindCell(v);
                    break;
                case SLIDER:
                    ((SliderCell) holder.itemView).setSliderRow((SliderRow) currentShownItems.get(position));
                    break;
                case LIST:
                    TextSettingsCell listCell = (TextSettingsCell) holder.itemView;
                    ListRow listRow = (ListRow) currentShownItems.get(position);
                    listCell.setTextAndValue(listRow.getTitle(), listRow.getTextFromInteger(listRow.getCurrentValue().getValue()), true, listRow.hasDivider());
                    break;
                case SLIDER_CHOOSE:
                    SlideChooseView slideChooseView = (SlideChooseView) holder.itemView;
                    SliderChooseRow sliderRow = (SliderChooseRow) currentShownItems.get(position);
                    slideChooseView.setCallback(index -> {
                        int id = sliderRow.getIds().get(index);
                        sliderRow.getPreferenceValue().updateValue(sliderRow.getOptions().get(id).first);
                    });
                    slideChooseView.setOptions(sliderRow.getIntValue(), sliderRow.getValues());
                    break;
                case FOOTER_INFORMATIVE:
                case FOOTER:
                    ((TextInfoPrivacyCell) holder.itemView).setText(currentShownItems.get(position).getTitle());
                    break;
                case STICKER_HEADER:
                    StickerHeaderRow pref = (StickerHeaderRow) currentShownItems.get(position);
                    LinearLayout linearLayout = (LinearLayout) holder.itemView;

                    if (linearLayout.getChildCount() > 0) {
                        return;
                    }

                    if (!pref.getUseOctoAnimation()) {
                        linearLayout.addView((View) pref.getStickerView(), createLinear(120, 120, Gravity.CENTER_HORIZONTAL, 0, 20, 0, 0));
                    }

                    TextView textView = null;
                    if (pref.getSummary() != null) {
                        textView = new TextView(context);
                        textView.setGravity(Gravity.CENTER_HORIZONTAL);
                        textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                        textView.setLinkTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkText));
                        textView.setHighlightColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkSelection));
                        textView.setText(pref.getSummary());
                        textView.setMovementMethod(new AndroidUtilities.LinkMovementMethodMy());

                        if (!pref.getUseOctoAnimation()) {
                            linearLayout.addView(textView, createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 36, 26, 36, 0));
                        }
                    }

                    if (pref.getUseOctoAnimation()) {
                        OctoAnimationFragment octoFragment = new OctoAnimationFragment(context, textView);
                        octoFragment.setEasterEggCallback(PreferencesFragment.this::animateFireworksOverlay);
                        linearLayout.addView(octoFragment, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, OctoAnimationFragment.sz, Gravity.CENTER_HORIZONTAL));
                    }

                    break;
                case CHECKBOX:
                    CheckBoxCell checkboxCell = (CheckBoxCell) holder.itemView;
                    CheckboxRow checkboxRow = (CheckboxRow) currentShownItems.get(position);
                    checkboxCell.setText(checkboxRow.getTitle(), checkboxRow.getSummary(), checkboxRow.getPreferenceValue(), checkboxRow.hasDivider(), true);
                    checkboxCell.setIcon(checkboxRow.isPremium() ? R.drawable.permission_locked : 0);
                    break;
                case EXPANDABLE_ROWS:
                    SwitchCell switchCell = (SwitchCell) holder.itemView;
                    ExpandableRows expandableRows = (ExpandableRows) currentShownItems.get(position);

                    if (expandableIndexes.get(expandableRows.getId()) == null) {
                        expandableIndexes.put(expandableRows.getId(), new ExpandableRowIndex(switchCell));
                    }

                    switchCell.setAsSwitch(expandableRows);
                    break;
                case EXPANDABLE_ROWS_CHILD:
                    SwitchCell switchCellChild = (SwitchCell) holder.itemView;
                    ExpandableRowsChild expandableRowsChild = (ExpandableRowsChild) currentShownItems.get(position);

                    ExpandableRowIndex index = expandableIndexes.get(expandableRowsChild.getRefersToId());
                    if (index != null) {
                        index.addSecondaryCell(switchCellChild);
                    }

                    boolean showDivider = position + 1 < currentShownItems.size();
                    switchCellChild.setAsCheckbox(expandableRowsChild.getItem(), showDivider);

                    break;
                default:
                    throw new RuntimeException("No view found for " + type);
            }
        }

        @Override
        public int getItemCount() {
            return currentShownItems.size();
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return currentShownItems.get(holder.getAdapterPosition()).getType().isEnabled();
        }

        @Override
        public int getItemViewType(int position) {
            return currentShownItems.get(position).getType().getAdapterType();
        }
    }

    private static class SwitchCell extends FrameLayout {

        private final ImageView imageView;
        private final TextView textView;
        private final AnimatedTextView countTextView;
        private final ImageView arrowView;
        private final Switch switchView;
        private final CheckBox2 checkBoxView;

        private boolean needDivider, needLine;

        public SwitchCell(Context context) {
            super(context);

            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
            setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

            imageView = new ImageView(context);
            imageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon), PorterDuff.Mode.MULTIPLY));
            imageView.setVisibility(View.GONE);
            addView(imageView, LayoutHelper.createFrame(24, 24, Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT), 20, 0, 20, 0));

            textView = new AppCompatTextView(context) {
                @Override
                protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                    if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST) {
                        widthMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec) - dp(52), MeasureSpec.AT_MOST);
                    }
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                }
            };
            textView.setLines(1);
            textView.setSingleLine(true);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            textView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
            textView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);

            countTextView = new AnimatedTextView(context, false, true, true);
            countTextView.setAnimationProperties(.35f, 0, 200, CubicBezierInterpolator.EASE_OUT_QUINT);
            countTextView.setTypeface(AndroidUtilities.bold());
            countTextView.setTextSize(dp(14));
            countTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            countTextView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);

            arrowView = new ImageView(context);
            arrowView.setVisibility(GONE);
            arrowView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText), PorterDuff.Mode.MULTIPLY));
            arrowView.setImageResource(R.drawable.arrow_more);

            LinearLayout textViewLayout = new LinearLayout(context);
            textViewLayout.setOrientation(LinearLayout.HORIZONTAL);
            textViewLayout.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);

            boolean isRTL = LocaleController.isRTL;
            textViewLayout.addView(textView, createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL));
            textViewLayout.addView(countTextView, createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0, Gravity.CENTER_VERTICAL, isRTL ? 6 : 0, 0, isRTL ? 0 : 6, 0));
            textViewLayout.addView(arrowView, createLinear(16, 16, 0, Gravity.CENTER_VERTICAL, isRTL ? 0 : 2, 0, 0, 0));

            addView(textViewLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT), 64, 0, 8, 0));

            switchView = new Switch(context);
            switchView.setVisibility(GONE);
            switchView.setColors(Theme.key_switchTrack, Theme.key_switchTrackChecked, Theme.key_windowBackgroundWhite, Theme.key_windowBackgroundWhite);
            switchView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
            addView(switchView, LayoutHelper.createFrame(37, 50, Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT), 19, 0, 19, 0));

            checkBoxView = new CheckBox2(context, 21);
            checkBoxView.setColor(Theme.key_radioBackgroundChecked, Theme.key_checkboxDisabled, Theme.key_checkboxCheck);
            checkBoxView.setDrawUnchecked(true);
            checkBoxView.setChecked(true, false);
            checkBoxView.setDrawBackgroundAsArc(10);
            checkBoxView.setVisibility(GONE);
            checkBoxView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
            addView(checkBoxView, LayoutHelper.createFrame(21, 21, Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT), LocaleController.isRTL ? 0 : 64, 0, LocaleController.isRTL ? 64 : 0, 0));
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(
                    MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(dp(50), MeasureSpec.EXACTLY)
            );
        }

        private boolean isExpanded;

        private boolean isSwitch;
        private ExpandableRows _expandableRows;
        private ExpandableRowsOption _item;
        private boolean _divider;

        public void setAsSwitch(ExpandableRows expandableRows) {
            isSwitch = true;
            _expandableRows = expandableRows;

            int selectedOptions = 0;
            for (ExpandableRowsOption item : expandableRows.getItemsList()) {
                if (item.property.getValue()) {
                    selectedOptions++;
                }
            }

            checkBoxView.setVisibility(GONE);
            imageView.setVisibility(VISIBLE);
            imageView.setImageResource(expandableRows.getIcon());
            textView.setText(expandableRows.getMainItemTitle());
            countTextView.setVisibility(VISIBLE);
            arrowView.setVisibility(VISIBLE);
            textView.setTranslationX(0);
            switchView.setVisibility(VISIBLE);
            switchView.setChecked(selectedOptions > 0, true);

            boolean currentExpanded = expandedRowIds.contains(expandableRows.getId());
            if (isExpanded != currentExpanded) {
                isExpanded = currentExpanded;
                arrowView.animate().rotation(isExpanded ? 180 : 0).setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT).setDuration(240).start();
            }

            countTextView.setText(MessageFormat.format(" {0}/{1}", selectedOptions, expandableRows.getItemsList().size()));

            needLine = !expandableRows.getItemsList().isEmpty();
            setWillNotDraw(!needLine);
        }

        public void setAsCheckbox(ExpandableRowsOption item, boolean divider) {
            isSwitch = false;
            _item = item;
            _divider = divider;

            checkBoxView.setVisibility(VISIBLE);
            checkBoxView.setChecked(item.property.getValue(), true);
            imageView.setVisibility(GONE);
            switchView.setVisibility(GONE);
            countTextView.setVisibility(GONE);
            arrowView.setVisibility(GONE);
            textView.setText(item.optionTitle);
            textView.setTranslationX(dp(41) * (LocaleController.isRTL ? -2.2f : 1));

            needLine = false;
            needDivider = divider;
            setWillNotDraw(!divider);
        }

        public void reload() {
            if (isSwitch) {
                setAsSwitch(_expandableRows);
            } else {
                setAsCheckbox(_item, _divider);
            }
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);
            if (LocaleController.isRTL) {
                if (needLine) {
                    float x = dp(19 + 37 + 19);
                    canvas.drawRect(x - dp(0.66f), (getMeasuredHeight() - dp(20)) / 2f, x, (getMeasuredHeight() + dp(20)) / 2f, Theme.dividerPaint);
                }
                if (needDivider && !OctoConfig.INSTANCE.disableDividers.getValue()) {
                    canvas.drawLine(getMeasuredWidth() - dp(64) + (textView.getTranslationX() < 0 ? dp(-32) : 0), getMeasuredHeight() - 1, 0, getMeasuredHeight() - 1, Theme.dividerPaint);
                }
            } else {
                if (needLine) {
                    float x = getMeasuredWidth() - dp(19 + 37 + 19);
                    canvas.drawRect(x - dp(0.66f), (getMeasuredHeight() - dp(20)) / 2f, x, (getMeasuredHeight() + dp(20)) / 2f, Theme.dividerPaint);
                }
                if (needDivider && !OctoConfig.INSTANCE.disableDividers.getValue()) {
                    canvas.drawLine(dp(64) + textView.getTranslationX(), getMeasuredHeight() - 1, getMeasuredWidth(), getMeasuredHeight() - 1, Theme.dividerPaint);
                }
            }
        }
    }

    private static class ExpandableRowIndex {
        private final SwitchCell mainCell;
        private final ArrayList<SwitchCell> secondaryCell = new ArrayList<>();

        public ExpandableRowIndex(SwitchCell mainCell) {
            this.mainCell = mainCell;
        }

        public void addSecondaryCell(SwitchCell cell) {
            if (secondaryCell.contains(cell)) {
                return;
            }

            secondaryCell.add(cell);
        }
    }
    public void rebuildAllFragmentsWithLast() {
        Parcelable recyclerViewState = null;
        if (listView.getLayoutManager() != null) {
            recyclerViewState = listView.getLayoutManager().onSaveInstanceState();
        }
        parentLayout.rebuildFragments(INavigationLayout.REBUILD_FLAG_REBUILD_LAST);
        listView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
    }

    public void reloadInterface() {
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.reloadInterface);
    }

    public void reloadMainInfo() {
        getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
    }

    public void reloadDialogs() {
        getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
    }
}
