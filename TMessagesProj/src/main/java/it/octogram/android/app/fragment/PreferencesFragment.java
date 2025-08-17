/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.fragment;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
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
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.INavigationLayout;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.CircularProgressDrawable;
import org.telegram.ui.Components.ColoredImageSpan;
import org.telegram.ui.Components.CrossfadeDrawable;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.FireworksOverlay;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.Premium.PremiumFeatureBottomSheet;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.ShareAlert;
import org.telegram.ui.Components.UndoView;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.PremiumPreviewFragment;
import org.telegram.ui.Stories.recorder.ButtonWithCounterView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.app.OctoPreferences;
import it.octogram.android.app.PreferenceType;
import it.octogram.android.app.PreferencesEntry;
import it.octogram.android.app.rows.BaseRow;
import it.octogram.android.app.rows.Clickable;
import it.octogram.android.app.rows.impl.CustomCellRow;
import it.octogram.android.app.rows.impl.ExpandableRows;
import it.octogram.android.app.rows.impl.ExpandableRowsChild;
import it.octogram.android.app.rows.impl.HeaderRow;
import it.octogram.android.app.rows.impl.ListRow;
import it.octogram.android.app.rows.impl.SliderChooseRow;
import it.octogram.android.app.rows.impl.SliderRow;
import it.octogram.android.app.rows.impl.SwitchRow;
import it.octogram.android.app.rows.impl.TextDetailRow;
import it.octogram.android.app.rows.impl.TextIconRow;
import it.octogram.android.app.ui.components.ExpandableRowIndex;
import it.octogram.android.app.ui.components.SwitchCell;
import it.octogram.android.utils.account.FingerprintUtils;
import it.octogram.android.utils.config.ExpandableRowsOption;
import it.octogram.android.utils.config.ImportSettingsScanHelper;

public class PreferencesFragment extends BaseFragment {

    private static final ArrayList<Integer> expandedRowIds = new ArrayList<>();
    private static final Map<Integer, ExpandableRowIndex> expandableIndexes = new HashMap<>();
    private final PreferencesEntry entry;
    private final ArrayList<BaseRow> oldItems = new ArrayList<>();
    private final ArrayList<BaseRow> currentShownItems = new ArrayList<>();
    private final List<BaseRow> reorderedPreferences = new ArrayList<>();
    private final HashSet<PreferenceType> typesWithDividerSupport = new HashSet<>();
    private final HashSet<Integer> typesWithCopySupport = new HashSet<>();
    private final HashSet<Integer> visibleTypesDuringCopy = new HashSet<>();
    private OctoPreferences preferences;
    private String focusOnKey;
    private ListAdapter listAdapter;
    private RecyclerListView listView;
    private Context context;
    /**
     * @noinspection deprecation
     */
    private UndoView restartTooltip;
    private FireworksOverlay fireworksOverlay;
    private LinearLayoutManager linearLayoutManager;
    private boolean hasUnlockedWithAuth = false;
    private boolean isSelectingItems = false;
    private FrameLayout buttonContainer;
    private ActionBarMenuItem doneButton;
    private boolean _isSelectingItems = false;
    private boolean _isDoneButtonVisible = false;

    {
        typesWithDividerSupport.add(PreferenceType.SWITCH);
        typesWithDividerSupport.add(PreferenceType.CHECKBOX);
        typesWithDividerSupport.add(PreferenceType.LIST);
        typesWithDividerSupport.add(PreferenceType.TEXT_DETAIL);
        typesWithDividerSupport.add(PreferenceType.TEXT_ICON);
        typesWithDividerSupport.add(PreferenceType.EXPANDABLE_ROWS);
        typesWithDividerSupport.add(PreferenceType.EXPANDABLE_ROWS_CHILD);
        typesWithDividerSupport.add(PreferenceType.CUSTOM_AI_MODEL);

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

    public void insertRow(BaseRow baseRow, BaseRow refer, boolean insertBefore) {
        if (reorderedPreferences.contains(refer)) {
            int index = reorderedPreferences.indexOf(refer);
            reorderedPreferences.add(index + (insertBefore ? 0 : 1), baseRow);
            AndroidUtilities.runOnUIThread(this::reloadUIAfterValueUpdate, 100);
        }
    }

    public void deleteRow(BaseRow baseRow) {
        int index = reorderedPreferences.indexOf(baseRow);
        if (index != -1) {
            reorderedPreferences.remove(baseRow);
            if (baseRow instanceof ExpandableRows expandableRows) {
                reorderedPreferences.removeIf(x -> x instanceof ExpandableRowsChild r2 && r2.getRefersToId() == expandableRows.getId());
            }
            reloadUIAfterValueUpdate();
        }
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
        try {
            entry.onFragmentCreate();
        } catch (Exception ignored) {}
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        Bulletin.removeDelegate(this);
        try {
            entry.onFragmentDestroy();
        } catch (Exception ignored) {}
    }

    @Override
    public boolean canBeginSlide() {
        return !isSelectingItems && entry.canBeginSlide();
    }

    @Override
    public boolean onBackPressed() {
        if (isSelectingItems) {
            isSelectingItems = false;
            updateIsSelectingItems();
            return false;
        }

        return entry.canBeginSlide();
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

        if (preferences.deepLink() != null && !preferences.deepLink().isEmpty() && !preferences.hasSaveButton()) {
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

                    if (!canBeginSlide()) {
                        return;
                    }

                    finishFragment();
                } else if (id > 0 && !isSelectingItems) {
                    if (id == 1 && preferences.hasSaveButton() && doneButton != null) {
                        entry.onSaveButtonPressed();
                        return;
                    }

                    OctoPreferences.OctoContextMenuElement element = preferences.elements().get(id - 1);
                    if (element != null) {
                        element.run.run();
                    }
                }
            }
        });

        if (preferences.hasSaveButton()) {
            Drawable checkmark = ContextCompat.getDrawable(context, R.drawable.ic_ab_done);
            if (checkmark != null) {
                checkmark.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_actionBarDefaultIcon), PorterDuff.Mode.MULTIPLY));
                Drawable doneButtonDrawable = new CrossfadeDrawable(checkmark, new CircularProgressDrawable(Theme.getColor(Theme.key_actionBarDefaultIcon)));
                doneButton = actionBar.createMenu().addItemWithWidth(1, doneButtonDrawable, dp(56), getString(R.string.Done));
                updateDoneButtonVisibility(false, false);
            }
        } else {
            loadContextMenu();
        }

        listAdapter = new ListAdapter(PreferencesFragment.this);

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

    @Override
    public void onBecomeFullyVisible() {
        super.onBecomeFullyVisible();
        entry.onBecomeFullyVisible();
    }

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

        float destination = isSelectingItems ? 1f : 0f;
        ValueAnimator animator = ValueAnimator.ofFloat(isSelectingItems ? 0f : 1f, destination);
        animator.setDuration(250);
        animator.addUpdateListener(animation -> {
            float animatedValue = (float) animation.getAnimatedValue();

            actionBar.setBackgroundColor(ColorUtils.blendARGB(Theme.getColor(Theme.key_actionBarDefault), Theme.getColor(Theme.key_windowBackgroundWhite), animatedValue));
            actionBar.setTitleColor(ColorUtils.blendARGB(Theme.getColor(Theme.key_actionBarDefaultTitle), Theme.getColor(Theme.key_windowBackgroundWhiteBlackText), animatedValue));
            actionBar.setItemsColor(ColorUtils.blendARGB(Theme.getColor(Theme.key_actionBarDefaultTitle), Theme.getColor(Theme.key_windowBackgroundWhiteBlackText), animatedValue), false);
            buttonContainer.setTranslationY((1f - animatedValue) * dp(48 + 25 + 10 + 10));

            if (actionBar.menu != null) {
                actionBar.menu.setAlpha(1f - animatedValue);
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                AndroidUtilities.setLightStatusBar(getParentActivity().getWindow(), isLightStatusBar());
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {
                onAnimationEnd(animation);
            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {

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

    public void updateDoneButtonVisibility(boolean hasChanges, boolean animated) {
        if (preferences == null || !preferences.hasSaveButton() || doneButton == null) {
            return;
        }

        if (animated && _isDoneButtonVisible == hasChanges) {
            return;
        }
        _isDoneButtonVisible = hasChanges;

        doneButton.setEnabled(hasChanges);
        if (animated) {
            doneButton.animate().alpha(hasChanges ? 1.0f : 0.0f).scaleX(hasChanges ? 1.0f : 0.0f).scaleY(hasChanges ? 1.0f : 0.0f).setDuration(180).start();
        } else {
            doneButton.setAlpha(hasChanges ? 1.0f : 0.0f);
            doneButton.setScaleX(hasChanges ? 1.0f : 0.0f);
            doneButton.setScaleY(hasChanges ? 1.0f : 0.0f);
        }
    }

    public void reloadUIAfterValueUpdate() {
        oldItems.clear();
        oldItems.addAll(currentShownItems);
        currentShownItems.clear();

        int focusElement = -1;
        boolean gotFocus = false;
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

            if (isFocused) {
                gotFocus = true;
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

        if (focusOnKey != null && !focusOnKey.isEmpty() && !gotFocus) {
            ImportSettingsScanHelper.SettingsScanCategory foundCategory = null;
            ImportSettingsScanHelper.SettingsScanOption foundOption = null;
            for (ImportSettingsScanHelper.SettingsScanCategory category : ImportSettingsScanHelper.INSTANCE.categories) {
                if (foundCategory != null) {
                    break;
                }

                for (ImportSettingsScanHelper.SettingsScanOption option : category.options) {
                    if (!option.isTitle && option.property != null && option.property.getKey() != null && option.property.getKey().equalsIgnoreCase(focusOnKey.toLowerCase())) {
                        foundCategory = category;
                        foundOption = option;
                        break;
                    }
                }
            }
            if (foundCategory != null) {
                String finalFocusOnKey = focusOnKey;
                ImportSettingsScanHelper.SettingsScanCategory finalCategory = foundCategory;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(getString(R.string.OptionMoved));
                builder.setMessage(formatString(R.string.OptionMoved_Text, foundOption.getName(), foundCategory.getName()));
                builder.setPositiveButton(getString(R.string.Open), (dialog, which) -> {
                    dialog.dismiss();
                    LaunchActivity.instance.presentFragment(finalCategory.onGetFragment.onCall(finalFocusOnKey));
                });
                builder.setNegativeButton(getString(R.string.Close), null);
                builder.show();
            }
        }

        if (focusElement != -1) {
            linearLayoutManager.scrollToPositionWithOffset(focusElement, dp(60));
            int finalFocusElement = focusElement;
            RecyclerListView.IntReturnCallback callback = () -> finalFocusElement;
            listView.highlightRow(callback);
        }

        if (focusOnKey != null) {
            focusOnKey = null;
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

    void animateFireworksOverlay() {
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
            if (index.getMainCell() == mainCell) {
                for (SwitchCell secondaryCell : index.getSecondaryCell()) {
                    secondaryCell.reload();
                }
                break;
            }
        }
    }

    private void reloadMainCellFromSecondary(SwitchCell secondaryCell) {
        for (ExpandableRowIndex index : expandableIndexes.values()) {
            if (index.getSecondaryCell().contains(secondaryCell)) {
                index.getMainCell().reload();

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

    public boolean isSelectingItems() {
        return isSelectingItems;
    }

    public Map<Integer, ExpandableRowIndex> getExpandableIndexes() {
        return expandableIndexes;
    }

    public ArrayList<Integer> getExpandedRowIds() {
        return expandedRowIds;
    }

    public FireworksOverlay getFireworksOverlay() {
        return fireworksOverlay;
    }

    public FrameLayout getFragmentView() {
        return (FrameLayout) fragmentView;
    }

    public Context getContext() {
        return context;
    }

    public LinearLayoutManager getLinearLayoutManager() {
        return linearLayoutManager;
    }

    public boolean isExpandableRowsLockedInternal(ExpandableRows expandableRow) {
        return isExpandableRowsLocked(expandableRow);
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