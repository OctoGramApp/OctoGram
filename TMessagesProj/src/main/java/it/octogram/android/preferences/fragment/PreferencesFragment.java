/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.fragment;

import static android.widget.LinearLayout.VERTICAL;
import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;
import static org.telegram.ui.Components.LayoutHelper.createLinear;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Parcelable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
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
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.CheckBox2;
import org.telegram.ui.Components.ColoredImageSpan;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.FireworksOverlay;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ListView.AdapterWithDiffUtils;
import org.telegram.ui.Components.Premium.PremiumFeatureBottomSheet;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.ShareAlert;
import org.telegram.ui.Components.SlideChooseView;
import org.telegram.ui.Components.Switch;
import org.telegram.ui.Components.UndoView;
import org.telegram.ui.PremiumPreviewFragment;
import org.telegram.ui.Stories.recorder.ButtonWithCounterView;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import it.octogram.android.ConfigProperty;
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
import it.octogram.android.preferences.rows.impl.FooterInformativeRow;
import it.octogram.android.preferences.rows.impl.HeaderRow;
import it.octogram.android.preferences.rows.impl.ListRow;
import it.octogram.android.preferences.rows.impl.SliderChooseRow;
import it.octogram.android.preferences.rows.impl.SliderRow;
import it.octogram.android.preferences.rows.impl.StickerHeaderRow;
import it.octogram.android.preferences.rows.impl.SwitchRow;
import it.octogram.android.preferences.rows.impl.TextDetailRow;
import it.octogram.android.preferences.rows.impl.TextIconRow;
import it.octogram.android.utils.ExpandableRowsOption;
import it.octogram.android.utils.FingerprintUtils;
import it.octogram.android.utils.OctoUtils;

public class PreferencesFragment extends BaseFragment {

    private OctoPreferences preferences;
    private final PreferencesEntry entry;
    private String focusOnKey;

    private ListAdapter listAdapter;
    private RecyclerListView listView;
    private Context context;
    /** @noinspection deprecation*/
    private UndoView restartTooltip;
    private FireworksOverlay fireworksOverlay;
    private LinearLayoutManager linearLayoutManager;

    private boolean hasUnlockedWithAuth = false;

    private boolean isSelectingItems = false;
    private FrameLayout buttonContainer;

    private static final ArrayList<Integer> expandedRowIds = new ArrayList<>();
    private static final Map<Integer, ExpandableRowIndex> expandableIndexes = new HashMap<>();
    private final ArrayList<BaseRow> oldItems = new ArrayList<>();
    private final ArrayList<BaseRow> currentShownItems = new ArrayList<>();
    private final List<BaseRow> reorderedPreferences = new ArrayList<>();

    private final HashSet<PreferenceType> typesWithDividerSupport = new HashSet<>();
    private final HashSet<Integer> typesWithCopySupport = new HashSet<>();
    private final HashSet<Integer> visibleTypesDuringCopy = new HashSet<>();
    {
        typesWithDividerSupport.add(PreferenceType.SWITCH);
        typesWithDividerSupport.add(PreferenceType.CHECKBOX);
        typesWithDividerSupport.add(PreferenceType.LIST);
        typesWithDividerSupport.add(PreferenceType.TEXT_DETAIL);
        typesWithDividerSupport.add(PreferenceType.TEXT_ICON);
        typesWithDividerSupport.add(PreferenceType.EXPANDABLE_ROWS);
        typesWithDividerSupport.add(PreferenceType.EXPANDABLE_ROWS_CHILD);

        typesWithCopySupport.add(PreferenceType.SWITCH.getAdapterType());
        typesWithCopySupport.add(PreferenceType.LIST.getAdapterType());
        typesWithCopySupport.add(PreferenceType.EXPANDABLE_ROWS_CHILD.getAdapterType());
        typesWithCopySupport.add(PreferenceType.SLIDER.getAdapterType());
        typesWithCopySupport.add(PreferenceType.SLIDER_CHOOSE.getAdapterType());
        typesWithCopySupport.add(PreferenceType.TEXT_ICON.getAdapterType());
        typesWithCopySupport.add(PreferenceType.TEXT_DETAIL.getAdapterType());
        typesWithCopySupport.add(PreferenceType.CUSTOM.getAdapterType());

        visibleTypesDuringCopy.addAll(typesWithCopySupport);
        visibleTypesDuringCopy.add(PreferenceType.HEADER.getAdapterType());
        visibleTypesDuringCopy.add(PreferenceType.SHADOW.getAdapterType());
        visibleTypesDuringCopy.add(PreferenceType.EXPANDABLE_ROWS.getAdapterType());
    }

    public PreferencesFragment(PreferencesEntry entry) {
        this(entry, null);
    }

    public PreferencesFragment(PreferencesEntry entry, String focusOnKey) {
        this.entry = entry;
        this.focusOnKey = focusOnKey;
    }

    public boolean isLockedContent() {
        return entry.isLockedContent();
    }

    public void updatePreferences() {
        OctoPreferences preferences = entry.getPreferences(this, context);
        reorderedPreferences.clear();

        for (BaseRow baseRow : preferences.preferences()) {
            reorderedPreferences.add(baseRow);

            if (baseRow instanceof ExpandableRows expandableRows) {
                for (ExpandableRowsOption item : expandableRows.getItemsList()) {
                    reorderedPreferences.add(new ExpandableRowsChild(item, expandableRows));
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
            menuItem.setContentDescription(getString(R.string.AccDescrMoreOptions));

            for (OctoPreferences.OctoContextMenuElement element : preferences.elements()) {
                ActionBarMenuSubItem item = menuItem.addSubItem(++i, element.icon, element.title);
                if (element.danger) {
                    item.setIconColor(Theme.getColor(Theme.key_text_RedRegular));
                    item.setTextColor(Theme.getColor(Theme.key_text_RedBold));
                    item.setSelectorColor(Theme.multAlpha(Theme.getColor(Theme.key_text_RedRegular), .12f));
                }
            }
        }
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        super.onActivityResultFragment(requestCode, resultCode, data);
        entry.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        Bulletin.addDelegate(this, new Bulletin.Delegate() {
            @Override
            public int getBottomOffset(int tag) {
                if (isSelectingItems) {
                    return dp(48 + 10 + 10);
                }

                return Bulletin.Delegate.super.getBottomOffset(tag);
            }
        });
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        Bulletin.removeDelegate(this);
    }

    @Override
    public boolean canBeginSlide() {
        return !isSelectingItems;
    }

    @Override
    public boolean onBackPressed() {
        if (isSelectingItems) {
            isSelectingItems = false;
            updateIsSelectingItems();
            return false;
        }

        return true;
    }

    @Override
    public View createView(Context context) {
        this.context = context;

        currentShownItems.clear();
        oldItems.clear();
        reorderedPreferences.clear();
        expandableIndexes.clear();
        expandedRowIds.clear();

        updatePreferences();

        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(preferences.title());
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }

        if (!preferences.deepLink().isEmpty()) {
            actionBar.setLongClickable(true);
            actionBar.setOnLongClickListener(v -> {
                if (!isSelectingItems) {
                    isSelectingItems = true;
                    updateIsSelectingItems();
                }

                return true;
            });
        }

        actionBar.setAllowOverlayTitle(true);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    if (isSelectingItems) {
                        isSelectingItems = false;
                        updateIsSelectingItems();
                        return;
                    }

                    finishFragment();
                } else if (id > 0 && !isSelectingItems) {
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
        linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);

        DefaultItemAnimator itemAnimator = getDefaultItemAnimator();
        listView.setItemAnimator(itemAnimator);

        listView.setVerticalScrollBarEnabled(false);
        listView.setLayoutManager(linearLayoutManager);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setAdapter(listAdapter);

        //noinspection deprecation
        restartTooltip = new UndoView(context);
        frameLayout.addView(restartTooltip, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.LEFT, 8, 0, 8, 8));

        listView.setOnItemClickListener((view, position, x, y) -> {
            BaseRow row = currentShownItems.get(position);

            if (isSelectingItems && !(row instanceof ExpandableRows)) {
                if (canCopyItem(row)) {
                    String link = String.format(Locale.US, "tg://%s?t=%s", preferences.deepLink(), getItemLink(row));
                    showDialog(new ShareAlert(context, null, link, false, link, false, true));
                }

                return;
            }

            if (row instanceof ExpandableRows expandableRow) {
                SwitchCell switchCell = (SwitchCell) view;

                if (isExpandableRowsLocked(expandableRow)) {
                    FingerprintUtils.checkFingerprint(context, FingerprintUtils.FingerprintAction.EXPAND_SETTINGS, true, () -> {
                        hasUnlockedWithAuth = true;
                        listView.clickItem(view, position);
                    });
                    return;
                }

                if (expandableRow.isMainSwitchHidden() || isSelectingItems || (LocaleController.isRTL ? x > dp(19 + 37 + 19) : x < view.getMeasuredWidth() - dp(19 + 37 + 19))) {
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

                    if (expandableRow.getOnSingleStateChange() != null) {
                        expandableRow.getOnSingleStateChange().run();
                    }

                    switchCell.reload();
                    reloadSecondaryCellFromMain(switchCell);
                }
            } else if (row instanceof ExpandableRowsChild expandableRow) {
                SwitchCell switchCell = (SwitchCell) view;

                ExpandableRowsOption singleItem = expandableRow.getItem();
                if (singleItem.onClick != null && !singleItem.onClick.get()) {
                    return;
                }

                singleItem.property.updateValue(!singleItem.property.getValue());

                switchCell.reload();
                reloadMainCellFromSecondary(switchCell);

                if (singleItem.onPostUpdate != null) {
                    singleItem.onPostUpdate.run();
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

    private boolean _isSelectingItems = false;
    private void updateIsSelectingItems() {
        if (isSelectingItems == _isSelectingItems) {
            return;
        }
        _isSelectingItems = isSelectingItems;

        if (isSelectingItems) {
            boolean isThereCopyNeedItem = false;
            for (BaseRow row : currentShownItems) {
                if (canCopyItem(row)) {
                    isThereCopyNeedItem = true;
                    break;
                }
            }
            if (!isThereCopyNeedItem) {
                isSelectingItems = false;
                _isSelectingItems = false;
                var _deepLink = String.format(Locale.US, "tg://%s", preferences.deepLink());
                AndroidUtilities.setLightStatusBar(getParentActivity().getWindow(), isLightStatusBar());
                showDialog(new ShareAlert(context, null, _deepLink, false, _deepLink, false, true));
                return;
            }
        }

        if (buttonContainer == null) {
            buttonContainer = new FrameLayout(context);
            buttonContainer.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray, getResourceProvider()));

            View buttonShadow = new View(context);
            buttonShadow.setBackgroundColor(Theme.getColor(Theme.key_dialogGrayLine, getResourceProvider()));
            buttonContainer.addView(buttonShadow, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 1f / AndroidUtilities.density, Gravity.FILL_HORIZONTAL | Gravity.TOP));

            TextView textView = new TextView(context);
            textView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack, getResourceProvider()));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            textView.setAlpha(0.7f);
            textView.setText(getString(R.string.SelectElement_Desc));
            buttonContainer.addView(textView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 25, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 1f / AndroidUtilities.density + 7, 0, 5));

            ButtonWithCounterView button = new ButtonWithCounterView(context, getResourceProvider());
            final SpannableStringBuilder sb = new SpannableStringBuilder("G ");
            final ColoredImageSpan span = new ColoredImageSpan(R.drawable.msg_share);
            sb.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.append(new SpannableStringBuilder(getString(R.string.SelectElement_CopyPage)));
            button.setText(sb, false);
            buttonContainer.addView(button, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM, 10, 0, 10, 10));
            button.setOnClickListener(v -> {
                isSelectingItems = false;
                updateIsSelectingItems();
                var _deepLink = String.format(Locale.US, "tg://%s", preferences.deepLink());
                showDialog(new ShareAlert(context, null, _deepLink, false, _deepLink, false, true));
            });
        }

        if (((FrameLayout) fragmentView).indexOfChild(buttonContainer) == -1) {
            ((FrameLayout) fragmentView).addView(buttonContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48 + 25 + 10 + 10, Gravity.FILL_HORIZONTAL | Gravity.BOTTOM));
        }

        float destination = isSelectingItems ? 100f : 0f;
        ValueAnimator animator = ValueAnimator.ofFloat(isSelectingItems ? 0f : 100f, destination);
        animator.setDuration(250);
        animator.addUpdateListener(animation -> {
            float animatedValue = (float) animation.getAnimatedValue() / 100f;

            actionBar.setBackgroundColor(ColorUtils.blendARGB(Theme.getColor(Theme.key_actionBarDefault), Theme.getColor(Theme.key_windowBackgroundWhite), animatedValue));
            actionBar.setTitleColor(ColorUtils.blendARGB(Theme.getColor(Theme.key_actionBarDefaultTitle), Theme.getColor(Theme.key_windowBackgroundWhiteBlackText), animatedValue));
            actionBar.setItemsColor(ColorUtils.blendARGB(Theme.getColor(Theme.key_actionBarDefaultTitle), Theme.getColor(Theme.key_windowBackgroundWhiteBlackText), animatedValue), false);
            buttonContainer.setTranslationY((1f - animatedValue) * dp(48 + 25 + 10 + 10));

            if (actionBar.menu != null) {
                actionBar.menu.setAlpha(1f - animatedValue);
            }

            if (animatedValue == (destination / 100f)) {
                AndroidUtilities.setLightStatusBar(getParentActivity().getWindow(), isLightStatusBar());
            }
        });
        animator.start();

        buttonContainer.setEnabled(isSelectingItems);
        if (actionBar.menu != null) {
            actionBar.menu.setEnabled(!isSelectingItems);
        }

        actionBar.setTitleAnimated(isSelectingItems ? getString(R.string.SelectElement) : preferences.title(), true, 250);
        listView.setPadding(0, 0, 0, isSelectingItems ? dp(48 + 25 + 10 + 10) : 0);

        reloadUIAfterValueUpdate();
    }

    @Override
    public boolean isLightStatusBar() {
        if (getLastStoryViewer() != null && getLastStoryViewer().isShown()) {
            return false;
        }
        if (hasForceLightStatusBar() && !Theme.getCurrentTheme().isDark()) {
            return true;
        }
        int key = Theme.key_actionBarDefault;
        if (isSelectingItems) {
            key = Theme.key_windowBackgroundWhite;
        }
        return ColorUtils.calculateLuminance(Theme.getColor(key)) > 0.7f;
    }

    private boolean canCopyItem(BaseRow row) {
        return !getItemLink(row).isEmpty();
    }

    private boolean canShowItemDuringCopy(BaseRow row) {
        if (!visibleTypesDuringCopy.contains(row.viewType)) {
            return false;
        } else if (row instanceof ExpandableRows expandableRows) {
            for (ExpandableRowsOption option : expandableRows.getItemsList()) {
                if (!getItemLink(option).isEmpty()) {
                    return true;
                }
            }
            return false;
        }

        if (typesWithCopySupport.contains(row.viewType)) {
            return !getItemLink(row).isEmpty();
        }

        return true;
    }

    private String getItemLink(BaseRow row) {
        ConfigProperty<?> data = null;
        String finalLink = null;

        if (row instanceof SwitchRow r2) {
            data = r2.getPreferenceValueConfig();
        } else if (row instanceof ListRow r2) {
            data = r2.getCurrentValue();
        } else if (row instanceof ExpandableRowsChild r2) {
            data = r2.getItem().property;
        } else if (row instanceof SliderRow r2) {
            data = r2.getPreferenceValue();
        } else if (row instanceof SliderChooseRow r2) {
            data = r2.getPreferenceValue();
        } else if (row instanceof TextIconRow r2) {
            if (r2.getPropertySelectionTag() != null) {
                finalLink = r2.getPropertySelectionTag();
            } else {
                data = r2.getPreference();
            }
        } else if (row instanceof TextDetailRow r2) {
            finalLink = r2.getPropertySelectionTag();
        } else if (row instanceof CustomCellRow r2) {
            finalLink = r2.getPropertySelectionTag();
        }

        String copiedLink = "";
        if (data != null && data.getKey() != null && !data.getKey().isEmpty()) {
            copiedLink = data.getKey();
        } else if (finalLink != null && !finalLink.isEmpty()) {
            copiedLink = finalLink;
        }

        return copiedLink;
    }

    private String getItemLink(ExpandableRowsOption option) {
        ConfigProperty<Boolean> data = option.property;
        String copiedLink = "";
        if (data != null && data.getKey() != null && !data.getKey().isEmpty()) {
            copiedLink = data.getKey();
        }

        return copiedLink;
    }

    private boolean isExpandableRowsLocked(ExpandableRows expandableRow) {
        return expandableRow.isLocked() && FingerprintUtils.hasFingerprintCached() && !hasUnlockedWithAuth;
    }

    public void showRestartTooltip() {
        //noinspection deprecation
        restartTooltip.showWithAction(0, UndoView.ACTION_NEED_RESTART, null, null);
    }

    public void smoothScrollToEnd() {
        listView.smoothScrollToPosition(currentShownItems.size() - 1);
    }

    public void notifyItemChanged(int... types) {
        for (int i = 0; i < listAdapter.getItemCount(); i++) {
            int itemType = listAdapter.getItemViewType(i);
            for (int type : types) {
                if (itemType == type) {
                    listAdapter.notifyItemChanged(i);
                    break;
                }
            }
        }
    }

    public void reloadUIAfterValueUpdate() {
        oldItems.clear();
        oldItems.addAll(currentShownItems);
        currentShownItems.clear();

        int focusElement = -1;
        BaseRow skipElement = null;

        //ArrayList<Integer> lockedExpandableRowsToIgnore = new ArrayList<>();
        for (int i = 0; i < reorderedPreferences.size(); i++) {
            BaseRow category = reorderedPreferences.get(i);

            if (skipElement == category) {
                skipElement = null;
                continue;
            }

            boolean isFocused = focusOnKey != null && getItemLink(category).equalsIgnoreCase(focusOnKey) && focusElement == -1;

            // expand ExpandableRows if the focused element is part of the items
            if (focusOnKey != null && category instanceof ExpandableRows expandableRows && !expandedRowIds.contains(expandableRows.getId())) {
                boolean containsFocused = false;
                for (ExpandableRowsOption option : expandableRows.getItemsList()) {
                    if (getItemLink(option).equalsIgnoreCase(focusOnKey)) {
                        containsFocused = true;
                    }
                }
                if (containsFocused) {
                    if (isExpandableRowsLocked(expandableRows)) {
                        isFocused = true;
                    } else {
                        expandedRowIds.add(expandableRows.getId());
                    }
                }
            }

            if (!canShowItem(category)) {
                continue;
            }

            if (!category.hasDivider() && !OctoConfig.INSTANCE.disableDividers.getValue()) {
                category.setDivider(true);
            }

            BaseRow nextElement = getNextVisibleElement(i, category);

            if (nextElement != null) {
                boolean nextElementSupportDivider = typesWithDividerSupport.contains(nextElement.getType());
                if (!nextElementSupportDivider && nextElement.getType() == PreferenceType.HEADER && !((HeaderRow) nextElement).getUseHeaderStyle()) {
                    nextElementSupportDivider = true;
                }

                // disable divider if next element doesn't support it
                category.setDivider(nextElementSupportDivider);
            }

            if (category.getType() == PreferenceType.SHADOW && nextElement != null && (nextElement.getType() == PreferenceType.FOOTER || nextElement.getType() == PreferenceType.FOOTER_INFORMATIVE || nextElement.getType() == PreferenceType.SHADOW)) {
                // hide shadow if shadow+footer, shadow+shadow
                continue;
            }

            if (category.getType() == PreferenceType.HEADER && nextElement != null && nextElement.getType() == PreferenceType.SHADOW) {
                // hide case: header+shadow
                skipElement = nextElement;
                continue;
            }

            if (currentShownItems.isEmpty() && (category.getType() == PreferenceType.EMPTY_CELL || category.getType() == PreferenceType.SHADOW)) {
                // hide empty cell and hide shadow if empty cell+shadow
                if (nextElement != null && nextElement.getType() == PreferenceType.SHADOW) {
                    skipElement = nextElement;
                }
                continue;
            }

            currentShownItems.add(category);

            // identify focused element position
            if (isFocused) {
                focusElement = currentShownItems.size() - 1;
            }
        }

        listAdapter.setItems(oldItems, currentShownItems);

        if (focusOnKey != null) {
            focusOnKey = null;
        }

        if (focusElement != -1) {
            linearLayoutManager.scrollToPositionWithOffset(focusElement, dp(60));
            int finalFocusElement = focusElement;
            RecyclerListView.IntReturnCallback callback = () -> finalFocusElement;
            listView.highlightRow(callback);
        }
    }

    private BaseRow getNextVisibleElement(int i, BaseRow element) {
        if (i != reorderedPreferences.size() - 1) {
            for (int j = i + 1; j < reorderedPreferences.size(); j++) {
                BaseRow row = reorderedPreferences.get(j);
                if (element != row && canShowItem(row)) {
                    return row;
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
        if (isSelectingItems && !canShowItemDuringCopy(item)) {
            return false;
        }

        if (item instanceof ExpandableRowsChild expandableRow && !expandedRowIds.contains(expandableRow.getRefersToId())) {
            return false;
        }

        if (item.getShowIfPreferenceValue() != null) {
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

                if (index.getOnSingleStateChange() != null) {
                    index.getOnSingleStateChange().run();
                }

                break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public RecyclerListView getListView() {
        return listView;
    }

    public boolean hasUnlockedWithAuth() {
        return hasUnlockedWithAuth;
    }

    private class ListAdapter extends AdapterWithDiffUtils {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final Context context = parent.getContext();
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
                    view = new SlideChooseView(context) {
                        @SuppressLint("ClickableViewAccessibility")
                        @Override
                        public boolean onTouchEvent(MotionEvent event) {
                            if (isSelectingItems) {
                                return false;
                            }

                            return super.onTouchEvent(event);
                        }

                        @Override
                        public boolean isSlidable() {
                            return !isSelectingItems && super.isSlidable();
                        }
                    };
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case FOOTER:
                    TextInfoPrivacyCell cell = new TextInfoPrivacyCell(context, 10);
                    cell.getTextView().setGravity(Gravity.CENTER_HORIZONTAL);
                    cell.getTextView().setTextColor(getThemedColor(Theme.key_windowBackgroundWhiteGrayText3));
                    cell.getTextView().setMovementMethod(null);
                    cell.getTextView().setPadding(0, dp(14), 0, dp(14));
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
                    //view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
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

            PreferenceType type = PreferenceType.fromAdapterType(holder.getItemViewType());
            if (type == null) {
                throw new RuntimeException("No type found for " + holder.getItemViewType());
            }

            ArrayList<View> additionalItems = new ArrayList<>();

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
                        customView.setEnabled(false);
                    }

                    break;
                case SHADOW:
                    holder.itemView.setBackground(Theme.getThemedDrawable(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case EMPTY_CELL:
                case HEADER:
                    HeaderRow headerRow = (HeaderRow) currentShownItems.get(position);
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    headerCell.setText(headerRow.getTitle());
                    if (!headerRow.getUseHeaderStyle()) {
                        headerCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                        headerCell.setTextSize(16);
                        headerCell.getTextView().setTypeface(null);
                        headerCell.setTopMargin(10);
                    }
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
                    additionalItems.add(checkCell.getCheckBox());
                    break;
                case TEXT_DETAIL:
                    TextDetailRow textDetailRow = (TextDetailRow) currentShownItems.get(position);
                    textDetailRow.bindCell((TextDetailSettingsCell) holder.itemView);
                    TextDetailSettingsCell settingsCell = (TextDetailSettingsCell) holder.itemView;
                    settingsCell.setMultilineDetail(true);
                    textDetailRow.bindCell(settingsCell);
                    if (!settingsCell.isMultiline()) {
                        additionalItems.add(settingsCell.getValueTextView());
                    }
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

                    if (textIconRow.isBlue()) {
                        v.setBackground(Theme.createSelectorWithBackgroundDrawable(Theme.getColor(Theme.key_windowBackgroundWhite), Theme.getColor(Theme.key_listSelector)));
                        v.setColors(Theme.key_windowBackgroundWhiteBlueText4, Theme.key_windowBackgroundWhiteBlueText4);
                    }

                    textIconRow.bindCell(v);
                    break;
                case SLIDER:
                    ((SliderCell) holder.itemView).setSliderRow((SliderRow) currentShownItems.get(position));
                    ((SliderCell) holder.itemView).setSlideable(!isSelectingItems);
                    break;
                case LIST:
                    TextSettingsCell listCell = (TextSettingsCell) holder.itemView;
                    ListRow listRow = (ListRow) currentShownItems.get(position);
                    listCell.setTextAndValue(listRow.getTitle(), listRow.getTextFromInteger(listRow.getCurrentValue().getValue()), true, listRow.hasDivider());
                    additionalItems.add(listCell.getValueTextView());
                    break;
                case SLIDER_CHOOSE:
                    SlideChooseView slideChooseView = (SlideChooseView) holder.itemView;
                    SliderChooseRow sliderRow = (SliderChooseRow) currentShownItems.get(position);
                    slideChooseView.setCallback(new SlideChooseView.Callback() {
                        @Override
                        public void onOptionSelected(int index) {
                            sliderRow.getPreferenceValue().updateValue(sliderRow.getOptions().get(index).first);

                            if (sliderRow.getOnUpdate() != null) {
                                sliderRow.getOnUpdate().run();
                            }
                        }

                        @Override
                        public void onTouchEnd() {
                            if (sliderRow.getOnTouchEnd() != null) {
                                sliderRow.getOnTouchEnd().run();
                            }
                        }
                    });
                    slideChooseView.setOptions(sliderRow.getIntValue(), sliderRow.getValues());
                    break;
                case FOOTER_INFORMATIVE:
                    ((TextInfoPrivacyCell) holder.itemView).setText(((FooterInformativeRow) currentShownItems.get(position)).getDynamicTitle());
                    break;
                case FOOTER:
                    ((TextInfoPrivacyCell) holder.itemView).setText(currentShownItems.get(position).getTitle());
                    break;
                case STICKER_HEADER:
                    StickerHeaderRow pref = (StickerHeaderRow) currentShownItems.get(position);
                    LinearLayout linearLayout = (LinearLayout) holder.itemView;

                    if (linearLayout.getChildCount() == 0) {
                        if (!pref.getUseOctoAnimation()) {
                            View stickerView = (View) pref.getStickerView();
                            linearLayout.addView(stickerView, createLinear(104, 104, Gravity.CENTER_HORIZONTAL, 0, 14, 0, 0));
                        }

                        TextView messageTextView = null;
                        if (pref.getSummary() != null) {
                            messageTextView = new TextView(context);
                            messageTextView.setTextColor(Theme.getColor(Theme.key_chats_message));
                            messageTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                            messageTextView.setGravity(Gravity.CENTER);
                            messageTextView.setText(pref.getSummary());
                            messageTextView.setMovementMethod(new AndroidUtilities.LinkMovementMethodMy());

                            if (!pref.getUseOctoAnimation()) {
                                linearLayout.addView(messageTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT, 52, 25, 52, 18));
                            }
                        }

                        if (pref.getUseOctoAnimation()) {
                            OctoAnimationFragment octoFragment = new OctoAnimationFragment(context, messageTextView);
                            octoFragment.setEasterEggCallback(PreferencesFragment.this::animateFireworksOverlay);
                            linearLayout.addView(octoFragment, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, OctoAnimationFragment.sz, Gravity.CENTER_HORIZONTAL));
                        }
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
                        expandableIndexes.put(expandableRows.getId(), new ExpandableRowIndex(switchCell, expandableRows.getOnSingleStateChange()));
                    }

                    switchCell.setAsSwitch(expandableRows);
                    switchCell.setIsSelectingItems(isSelectingItems);
                    if (expandableRows.isMainSwitchHidden()) {
                        switchCell.switchView.setVisibility(View.GONE);
                    } else {
                        additionalItems.add(switchCell.switchView);
                    }
                    break;
                case EXPANDABLE_ROWS_CHILD:
                    SwitchCell switchCellChild = (SwitchCell) holder.itemView;
                    ExpandableRowsChild expandableRowsChild = (ExpandableRowsChild) currentShownItems.get(position);

                    ExpandableRowIndex index = expandableIndexes.get(expandableRowsChild.getRefersToId());
                    if (index != null) {
                        index.addSecondaryCell(switchCellChild);
                    }

                    switchCellChild.setAsCheckbox(expandableRowsChild);
                    switchCellChild.setIsSelectingItems(isSelectingItems);
                    additionalItems.add(switchCellChild.checkBoxView);
                    break;
                default:
                    throw new RuntimeException("No view found for " + type);
            }

            for (View view : additionalItems) {
                if (view != null) {
                    view.setVisibility(isSelectingItems ? View.INVISIBLE : View.VISIBLE);
                }
            }

            additionalItems.clear();
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

    private class SwitchCell extends FrameLayout {

        private final ImageView imageView;
        private final AvatarDrawable avatarDrawable;
        private final BackupImageView backupImageView;
        private final TextView textView;
        private final AnimatedTextView countTextView;
        private final ImageView arrowView;
        private final Switch switchView;
        private final CheckBox2 checkBoxView;
        private final LinearLayout textViewLayout;

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

            textViewLayout = new LinearLayout(context);
            textViewLayout.setOrientation(LinearLayout.HORIZONTAL);
            textViewLayout.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);

            avatarDrawable = new AvatarDrawable();
            avatarDrawable.setTextSize(dp(12));

            backupImageView = new BackupImageView(context);
            backupImageView.setRoundRadius(dp(18));

            boolean isRTL = LocaleController.isRTL;
            textViewLayout.addView(backupImageView, createLinear(36, 36, Gravity.CENTER_VERTICAL, 0, 0, 0, 0));
            textViewLayout.addView(textView, createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL));
            textViewLayout.addView(countTextView, createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0, Gravity.CENTER_VERTICAL, isRTL ? 6 : 0, 0, isRTL ? 0 : 6, 0));
            textViewLayout.addView(arrowView, createLinear(16, 16, 0, Gravity.CENTER_VERTICAL, isRTL ? 0 : 2, 0, 0, 0));

            addView(textViewLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT), 73, 0, 8, 0));

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
        private ExpandableRowsChild _item;
        private boolean isSelectingItems = false;
        private boolean hasAddedUserData = false;
        private boolean _isLocked = false;

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
            backupImageView.setVisibility(GONE);
            switchView.setChecked(selectedOptions > 0, true);

            boolean currentExpanded = expandedRowIds.contains(expandableRows.getId());
            if (isExpanded != currentExpanded) {
                isExpanded = currentExpanded;
                arrowView.animate().rotation(isExpanded ? 180 : 0).setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT).setDuration(240).start();
            }

            countTextView.setText(MessageFormat.format(" {0}/{1}", selectedOptions, expandableRows.getItemsList().size()));

            ((MarginLayoutParams) textViewLayout.getLayoutParams()).rightMargin = dp((LocaleController.isRTL ? 64 : 75) + 4);

            needLine = !expandableRows.getItemsList().isEmpty() && !expandableRows.isMainSwitchHidden();
            needDivider = expandableRows.hasDivider();
            setWillNotDraw(false);

            boolean isLocked = isExpandableRowsLocked(expandableRows);
            if (_isLocked != isLocked) {
                _isLocked = isLocked;
                arrowView.setScaleX(isLocked ? 0.8f : 1f);
                arrowView.setScaleY(isLocked ? 0.8f : 1f);
                arrowView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(isLocked ? Theme.key_stickers_menu : Theme.key_windowBackgroundWhiteBlackText), PorterDuff.Mode.MULTIPLY));
                arrowView.setImageResource(isLocked ? R.drawable.other_lockedfolders2 : R.drawable.arrow_more);
                arrowView.setTranslationX(isLocked ? dp(2) : 0);
            }
        }

        public void setAsCheckbox(ExpandableRowsChild expandableRow) {
            isSwitch = false;
            _item = expandableRow;

            imageView.setVisibility(GONE);
            switchView.setVisibility(GONE);
            countTextView.setVisibility(GONE);
            arrowView.setVisibility(GONE);
            checkBoxView.setVisibility(VISIBLE);
            checkBoxView.setChecked(expandableRow.getItem().property.getValue(), true);

            if (expandableRow.getItem().hasAccount()) {
                if (!hasAddedUserData) {
                    int account = expandableRow.getItem().accountId;
                    TLRPC.User user = UserConfig.getInstance(account).getCurrentUser();
                    avatarDrawable.setInfo(account, user);
                    textView.setText(ContactsController.formatName(user.first_name, user.last_name));
                    backupImageView.getImageReceiver().setCurrentAccount(account);
                    backupImageView.setForUserOrChat(user, avatarDrawable);
                    backupImageView.setVisibility(VISIBLE);
                    hasAddedUserData = true;

                    CharSequence text = user.first_name;
                    try {
                        text = Emoji.replaceEmoji(text, textView.getPaint().getFontMetricsInt(), false);
                    } catch (Exception ignore) {}
                    textView.setText(text);

                    backupImageView.setTranslationX(dp(41) * (LocaleController.isRTL ? -2.2f : 1));
                    textView.setTranslationX(dp(55) * (LocaleController.isRTL ? -2.2f : 1));
                }
            } else {
                backupImageView.setVisibility(GONE);
                textView.setText(expandableRow.getItem().optionTitle);
                textView.setTranslationX(dp(41) * (LocaleController.isRTL ? -2.2f : 1));
            }

            needLine = false;
            needDivider = expandableRow.hasDivider();
            setWillNotDraw(!expandableRow.hasDivider());
        }

        public void reload() {
            if (isSwitch) {
                setAsSwitch(_expandableRows);
            } else {
                setAsCheckbox(_item);
            }
        }

        public void setIsSelectingItems(boolean isSelectingItems) {
            this.isSelectingItems = isSelectingItems;
            invalidate();
        }

        @Override
        protected void onDraw(@NonNull Canvas canvas) {
            super.onDraw(canvas);
            if (LocaleController.isRTL) {
                if (needLine && !isSelectingItems) {
                    float x = dp(19 + 37 + 19);
                    canvas.drawRect(x - dp(0.66f), (getMeasuredHeight() - dp(20)) / 2f, x, (getMeasuredHeight() + dp(20)) / 2f, Theme.dividerPaint);
                }
                if (needDivider && !OctoConfig.INSTANCE.disableDividers.getValue()) {
                    canvas.drawLine(getMeasuredWidth() - dp(64) + ((hasAddedUserData ? backupImageView : textView).getTranslationX() < 0 ? dp(-32) : 0), getMeasuredHeight() - 1, 0, getMeasuredHeight() - 1, Theme.dividerPaint);
                }
            } else {
                if (needLine && !isSelectingItems) {
                    float x = getMeasuredWidth() - dp(19 + 37 + 19);
                    canvas.drawRect(x - dp(0.66f), (getMeasuredHeight() - dp(20)) / 2f, x, (getMeasuredHeight() + dp(20)) / 2f, Theme.dividerPaint);
                }
                if (needDivider && !OctoConfig.INSTANCE.disableDividers.getValue()) {
                    canvas.drawLine(dp(64) + (hasAddedUserData ? backupImageView : textView).getTranslationX(), getMeasuredHeight() - 1, getMeasuredWidth(), getMeasuredHeight() - 1, Theme.dividerPaint);
                }
            }
        }
    }

    private static class ExpandableRowIndex {
        private final SwitchCell mainCell;
        private final Runnable onSingleStateChange;
        private final ArrayList<SwitchCell> secondaryCell = new ArrayList<>();

        public ExpandableRowIndex(SwitchCell mainCell, Runnable onSingleStateChange) {
            this.mainCell = mainCell;
            this.onSingleStateChange = onSingleStateChange;
        }

        public Runnable getOnSingleStateChange() {
            return onSingleStateChange;
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
}
