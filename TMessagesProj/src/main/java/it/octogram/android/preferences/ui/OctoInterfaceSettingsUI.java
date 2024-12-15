/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

package it.octogram.android.preferences.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Parcelable;
import android.text.SpannableString;

import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.SimpleTextView;
import org.telegram.ui.LaunchActivity;

import java.text.MessageFormat;
import java.util.List;

import it.octogram.android.ActionBarCenteredTitle;
import it.octogram.android.ConfigProperty;
import it.octogram.android.IconsUIType;
import it.octogram.android.InterfaceCheckboxUI;
import it.octogram.android.InterfaceSliderUI;
import it.octogram.android.InterfaceSwitchUI;
import it.octogram.android.OctoConfig;
import it.octogram.android.TabMode;
import it.octogram.android.TabStyle;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.rows.impl.CustomCellRow;
import it.octogram.android.preferences.rows.impl.FooterInformativeRow;
import it.octogram.android.preferences.rows.impl.ListRow;
import it.octogram.android.preferences.rows.impl.SwitchRow;
import it.octogram.android.preferences.ui.custom.FolderTypeSelector;
import it.octogram.android.preferences.ui.custom.IconsSelector;
import it.octogram.android.utils.MessageStringHelper;
import it.octogram.android.utils.PopupChoiceDialogOption;

public class OctoInterfaceSettingsUI implements PreferencesEntry {
    private IconsSelector iconsSelector;
    private FolderTypeSelector folderTypeSelector;

    private final ConfigProperty<Boolean> isUsingDefault = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> isUsingSolar = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> isUsingM3 = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> canShowMemeModeRow = new ConfigProperty<>(null, false);

    private boolean wasCentered = false;
    private boolean wasCenteredAtBeginning = false;
    private float _centeredMeasure = -1;

    @Override
    public OctoPreferences getPreferences(PreferencesFragment fragment, Context context) {
        updateConfig();

        wasCentered = isTitleCentered();
        wasCenteredAtBeginning = wasCentered;

        return OctoPreferences.builder(LocaleController.getString(R.string.AppTitleSettings))
                .category(LocaleController.getString(R.string.ImproveInterface), category -> {
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.uiTitleCenteredState)
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(ActionBarCenteredTitle.ALWAYS.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.ImproveInterfaceTitleCenteredAlways)),
                                    new PopupChoiceDialogOption()
                                            .setId(ActionBarCenteredTitle.JUST_IN_CHATS.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.ImproveInterfaceTitleCenteredChats)),
                                    new PopupChoiceDialogOption()
                                            .setId(ActionBarCenteredTitle.JUST_IN_SETTINGS.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.ImproveInterfaceTitleCenteredSettings)),
                                    new PopupChoiceDialogOption()
                                            .setId(ActionBarCenteredTitle.NEVER.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.ImproveInterfaceTitleCenteredNever))
                            ))
                            .onSelected(() -> animateActionBarUpdate(fragment))
                            .title(LocaleController.getString(R.string.ImproveInterfaceTitleCentered))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.uiImmersivePopups)
                            .title(LocaleController.getString(R.string.ImproveInterfaceImmersivePopups))
                            .description(LocaleController.getString(R.string.ImproveInterfaceImmersivePopups_Desc))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(InterfaceSwitchUI.DEFAULT.getValue())
                                            .setItemSwitchIconUI(InterfaceSwitchUI.DEFAULT)
                                            .setItemTitle(LocaleController.getString(R.string.ImproveInterfaceSwitchDefault)),
                                    new PopupChoiceDialogOption()
                                            .setId(InterfaceSwitchUI.ONEUIOLD.getValue())
                                            .setItemSwitchIconUI(InterfaceSwitchUI.ONEUIOLD)
                                            .setItemTitle(LocaleController.getString(R.string.ImproveInterfaceSwitchOneUIOld)),
                                    new PopupChoiceDialogOption()
                                            .setId(InterfaceSwitchUI.ONEUINEW.getValue())
                                            .setItemSwitchIconUI(InterfaceSwitchUI.ONEUINEW)
                                            .setItemTitle(LocaleController.getString(R.string.ImproveInterfaceSwitchOneUINew)),
                                    new PopupChoiceDialogOption()
                                            .setId(InterfaceSwitchUI.GOOGLE.getValue())
                                            .setItemSwitchIconUI(InterfaceSwitchUI.GOOGLE)
                                            .setItemTitle(LocaleController.getString(R.string.ImproveInterfaceSwitchGoogle))
                            ))
                            .onSelected(() -> reloadUI(fragment))
                            .currentValue(OctoConfig.INSTANCE.interfaceSwitchUI)
                            .title(LocaleController.getString(R.string.ImproveInterfaceSwitch))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(InterfaceCheckboxUI.DEFAULT.getValue())
                                            .setItemCheckboxIconUI(InterfaceCheckboxUI.DEFAULT)
                                            .setItemTitle(LocaleController.getString(R.string.ImproveInterfaceCheckboxDefault)),
                                    new PopupChoiceDialogOption()
                                            .setId(InterfaceCheckboxUI.ROUNDED.getValue())
                                            .setItemCheckboxIconUI(InterfaceCheckboxUI.ROUNDED)
                                            .setItemTitle(LocaleController.getString(R.string.ImproveInterfaceCheckboxRounded)),
                                    new PopupChoiceDialogOption()
                                            .setId(InterfaceCheckboxUI.TRANSPARENT_UNCHECKED.getValue())
                                            .setItemCheckboxIconUI(InterfaceCheckboxUI.TRANSPARENT_UNCHECKED)
                                            .setItemTitle(LocaleController.getString(R.string.ImproveInterfaceCheckboxSemiTransparent)),
                                    new PopupChoiceDialogOption()
                                            .setId(InterfaceCheckboxUI.ALWAYS_TRANSPARENT.getValue())
                                            .setItemCheckboxIconUI(InterfaceCheckboxUI.ALWAYS_TRANSPARENT)
                                            .setItemTitle(LocaleController.getString(R.string.ImproveInterfaceCheckboxAlwaysTransparent1))
                            ))
                            .currentValue(OctoConfig.INSTANCE.interfaceCheckboxUI)
                            .title(LocaleController.getString(R.string.ImproveInterfaceCheckbox))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(InterfaceSliderUI.DEFAULT.getValue())
                                            .setItemSliderIconUI(InterfaceSliderUI.DEFAULT)
                                            .setItemTitle(LocaleController.getString(R.string.ImproveInterfaceSliderDefault)),
                                    new PopupChoiceDialogOption()
                                            .setId(InterfaceSliderUI.MODERN.getValue())
                                            .setItemSliderIconUI(InterfaceSliderUI.MODERN)
                                            .setItemTitle(LocaleController.getString(R.string.ImproveInterfaceSliderModern)),
                                    new PopupChoiceDialogOption()
                                            .setId(InterfaceSliderUI.ANDROID.getValue())
                                            .setItemSliderIconUI(InterfaceSliderUI.ANDROID)
                                            .setItemTitle(LocaleController.getString(R.string.ImproveInterfaceSliderAndroid))
                            ))
                            .currentValue(OctoConfig.INSTANCE.interfaceSliderUI)
                            .title(LocaleController.getString(R.string.ImproveInterfaceSlider))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.useSquaredFab)
                            .title(R.string.SquaredFab)
                            .requiresRestart(true)
                            .description(R.string.SquaredFab_Desc)
                            .build());
                })
                .category(R.string.ManageFolders, category -> {
                    category.row(new CustomCellRow.CustomCellRowBuilder()
                            .layout(folderTypeSelector = new FolderTypeSelector(context))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.tabMode)
                            .options(List.of(
                                    new PopupChoiceDialogOption().setId(TabMode.TEXT.getValue()).setItemTitle(R.string.FolderTypeDefault).setTabModeIconUI(TabMode.TEXT),
                                    new PopupChoiceDialogOption().setId(TabMode.ICON.getValue()).setItemTitle(R.string.FolderTypeIconsOnly).setTabModeIconUI(TabMode.ICON),
                                    new PopupChoiceDialogOption().setId(TabMode.MIXED.getValue()).setItemTitle(R.string.FolderTypeTextAndIcons).setTabModeIconUI(TabMode.MIXED)
                            ))
                            .onSelected(() -> {
                                AccountInstance.getInstance(UserConfig.selectedAccount).getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
                                folderTypeSelector.fillTabs();
                            })
                            .title(R.string.FolderType)
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.tabStyle)
                            .options(List.of(
                                    new PopupChoiceDialogOption().setId(TabStyle.DEFAULT.getValue()).setItemTitle(R.string.FolderStyleDefault).setTabStyleIconUI(TabStyle.DEFAULT),
                                    new PopupChoiceDialogOption().setId(TabStyle.ROUNDED.getValue()).setItemTitle(R.string.FolderStyleRounded).setTabStyleIconUI(TabStyle.ROUNDED),
                                    new PopupChoiceDialogOption().setId(TabStyle.FLOATING.getValue()).setItemTitle(R.string.FolderStyleFloating).setTabStyleIconUI(TabStyle.FLOATING),
                                    new PopupChoiceDialogOption().setId(TabStyle.TEXT_ONLY.getValue()).setItemTitle(R.string.FolderStyleTextOnly).setTabStyleIconUI(TabStyle.TEXT_ONLY),
                                    new PopupChoiceDialogOption().setId(TabStyle.CHIPS.getValue()).setItemTitle(R.string.FolderStyleChips).setTabStyleIconUI(TabStyle.CHIPS),
                                    new PopupChoiceDialogOption().setId(TabStyle.PILLS.getValue()).setItemTitle(R.string.FolderStylePills).setTabStyleIconUI(TabStyle.PILLS),
                                    new PopupChoiceDialogOption().setId(TabStyle.FULL.getValue()).setItemTitle(R.string.FolderStyleFull).setTabStyleIconUI(TabStyle.FULL)
                            ))
                            .onSelected(() -> {
                                AccountInstance.getInstance(UserConfig.selectedAccount).getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
                                folderTypeSelector.fillTabs();
                            })
                            .title(R.string.FolderStyle)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> {
                                AccountInstance.getInstance(UserConfig.selectedAccount).getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
                                folderTypeSelector.fillTabs();
                            })
                            .preferenceValue(OctoConfig.INSTANCE.hideUnreadCounterOnFolder)
                            .title(R.string.HideUnreadCounter)
                            .description(R.string.HideUnreadCounter_Desc)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> AccountInstance.getInstance(UserConfig.selectedAccount).getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated))
                            .preferenceValue(OctoConfig.INSTANCE.showFoldersMessagesCounter)
                            .title(R.string.ShowMessagesCounter)
                            .description(R.string.ShowMessagesCounter_Desc)
                            .showIf(OctoConfig.INSTANCE.hideUnreadCounterOnFolder, true)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> AccountInstance.getInstance(UserConfig.selectedAccount).getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated))
                            .preferenceValue(OctoConfig.INSTANCE.includeMutedChatsInCounter)
                            .title(R.string.IncludeMutedChats)
                            .description(R.string.IncludeMutedChats_Desc)
                            .showIf(OctoConfig.INSTANCE.hideUnreadCounterOnFolder, true)
                            .build());
                })
                .category(LocaleController.getString(R.string.ImproveIcons), category -> {
                    category.row(new CustomCellRow.CustomCellRowBuilder()
                            .layout(iconsSelector = new IconsSelector(context) {
                                @Override
                                protected void onSelectedIcons() {
                                    updateConfig();
                                    fragment.reloadUIAfterValueUpdate();
                                    fragment.smoothScrollToEnd();
                                    fragment.showRestartTooltip();
                                }
                            })
                            .postNotificationName(NotificationCenter.mainUserInfoChanged, NotificationCenter.reloadInterface)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> iconsSelector.iconsPreviewCell.animateUpdate())
                            .preferenceValue(OctoConfig.INSTANCE.uiRandomMemeIcons)
                            .showIf(canShowMemeModeRow)
                            .title("Meme Mode")
                            .requiresRestart(true)
                            .description(LocaleController.getString(R.string.ImproveIconsMeme_Desc))
                            .build());
                })
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(LocaleController.getString(R.string.ImproveIconsDefault_Desc))
                        .showIf(isUsingDefault)
                        .build())
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(composeIconsDescription(true))
                        .showIf(isUsingSolar)
                        .build())
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(composeIconsDescription(false))
                        .showIf(isUsingM3)
                        .build())
                .build();
    }

    private CharSequence composeIconsDescription(boolean isSolar) {
        return MessageStringHelper.getUrlNoUnderlineText(
                new SpannableString(
                        MessageStringHelper.fromHtml(
                                isSolar ?
                                        LocaleController.formatString(
                                                R.string.ImproveIconsSolar_Desc,
                                                "<a href='tg://resolve?domain=TierOhneNation'>@TierOhneNation</a>",
                                                "<a href='tg://resolve?domain=design480'>@Design480</a>"
                                        ) :
                                        LocaleController.formatString(
                                                R.string.ImproveIconsMaterialDesign3_Desc,
                                                MessageFormat.format("<a href=''https://m3.material.io/styles/icons''>{0}</a>", LocaleController.getString(R.string.ImproveIconsMaterialDesign3_DescHere))
                                        )
                        )
                )
        );
    }

    private void updateConfig() {
        int currentState = OctoConfig.INSTANCE.uiIconsType.getValue();
        boolean canUseMemeMode = currentState != IconsUIType.DEFAULT.getValue() && IconsSelector.canUseMemeMode();

        isUsingDefault.setValue(currentState == IconsUIType.DEFAULT.getValue());
        isUsingSolar.setValue(currentState == IconsUIType.SOLAR.getValue());
        isUsingM3.setValue(currentState == IconsUIType.MATERIAL_DESIGN_3.getValue());
        canShowMemeModeRow.setValue(canUseMemeMode);
    }

    private void animateActionBarUpdate(PreferencesFragment fragment) {
        boolean centered = isTitleCentered();
        ActionBar actionBar = fragment.getActionBar();

        if (wasCentered == centered) {
            return;
        }

        if (actionBar != null) {
            SimpleTextView titleTextView = actionBar.getTitleTextView();

            if (_centeredMeasure == -1) {
                _centeredMeasure = actionBar.getMeasuredWidth() / 2f - titleTextView.getTextWidth() / 2f - AndroidUtilities.dp((AndroidUtilities.isTablet() ? 80 : 72));
            }

            titleTextView.animate().translationX(_centeredMeasure * (centered ? 1 : 0) - (wasCenteredAtBeginning ? Math.abs(_centeredMeasure) : 0)).setDuration(150).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    wasCentered = centered;

                    reloadUI(fragment);
                    LaunchActivity.makeRipple(centered ? (actionBar.getMeasuredWidth() / 2f) : 0, 0, centered ? 0.9f : 0.1f);
                }
            }).start();
        } else {
            reloadUI(fragment, true);
        }
    }

    /**
     * @noinspection deprecation
     */
    public void reloadUI(PreferencesFragment fragment, boolean reloadLast) {
        Parcelable recyclerViewState = null;
        RecyclerView.LayoutManager layoutManager = fragment.getListView().getLayoutManager();
        if (layoutManager != null)
            recyclerViewState = layoutManager.onSaveInstanceState();
        fragment.getParentLayout().rebuildAllFragmentViews(reloadLast, reloadLast);
        if (layoutManager != null && recyclerViewState != null)
            layoutManager.onRestoreInstanceState(recyclerViewState);
    }

    public void reloadUI(PreferencesFragment fragment) {
        reloadUI(fragment, false);
    }

    private boolean isTitleCentered() {
        int centeredState = OctoConfig.INSTANCE.uiTitleCenteredState.getValue();
        return centeredState == ActionBarCenteredTitle.ALWAYS.getValue() || centeredState == ActionBarCenteredTitle.JUST_IN_SETTINGS.getValue();
    }
}
