/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Parcelable;
import android.text.SpannableString;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
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
import it.octogram.android.StickerUi;
import it.octogram.android.TabMode;
import it.octogram.android.TabStyle;
import it.octogram.android.app.OctoPreferences;
import it.octogram.android.app.PreferencesEntry;
import it.octogram.android.app.fragment.PreferencesFragment;
import it.octogram.android.app.rows.impl.CustomCellRow;
import it.octogram.android.app.rows.impl.FooterInformativeRow;
import it.octogram.android.app.rows.impl.ListRow;
import it.octogram.android.app.rows.impl.SwitchRow;
import it.octogram.android.app.rows.impl.TextIconRow;
import it.octogram.android.app.ui.cells.FolderTypeSelectorCell;
import it.octogram.android.app.ui.cells.IconsSelectorCell;
import it.octogram.android.app.ui.cells.InterfaceUIPreviewCell;
import it.octogram.android.app.ui.components.DrawerPreviewCell;
import it.octogram.android.utils.appearance.MessageStringHelper;
import it.octogram.android.utils.appearance.PopupChoiceDialogOption;
import it.octogram.android.utils.deeplink.DeepLinkDef;

public class OctoAppearanceUI implements PreferencesEntry {
    private IconsSelectorCell iconsSelectorCell;
    private PreferencesFragment fragment;

    private final ConfigProperty<Boolean> isUsingDefault = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> isUsingSolar = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> isUsingM3 = new ConfigProperty<>(null, false);

    private DrawerPreviewCell drawerPreviewCell;
    private FolderTypeSelectorCell folderTypeSelectorCell;
    private InterfaceUIPreviewCell switchesPreview;

    private boolean wasCentered = false;
    private boolean wasCenteredAtBeginning = false;
    private float _centeredMeasure = -1;

    @NonNull
    @Override
    public OctoPreferences getPreferences(@NonNull PreferencesFragment fragment, @NonNull Context context) {
        wasCentered = isTitleCentered();
        wasCenteredAtBeginning = wasCentered;
        this.fragment = fragment;
        updateConfig();

        return OctoPreferences.builder(getString(R.string.Appearance))
                .deepLink(DeepLinkDef.APPEARANCE)
                .sticker(context, OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME, StickerUi.APPEARANCE, true, getString(R.string.OctoAppearanceSettingsHeader))
                .category(getString(R.string.ImproveInterface), category -> {
                    category.row(new CustomCellRow.CustomCellRowBuilder()
                            .layout(switchesPreview = new InterfaceUIPreviewCell(context))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.uiTitleCenteredState)
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(ActionBarCenteredTitle.ALWAYS.getValue())
                                            .setItemTitle(getString(R.string.ImproveInterfaceTitleCenteredAlways)),
                                    new PopupChoiceDialogOption()
                                            .setId(ActionBarCenteredTitle.JUST_IN_CHATS.getValue())
                                            .setItemTitle(getString(R.string.ImproveInterfaceTitleCenteredChats)),
                                    new PopupChoiceDialogOption()
                                            .setId(ActionBarCenteredTitle.JUST_IN_SETTINGS.getValue())
                                            .setItemTitle(getString(R.string.ImproveInterfaceTitleCenteredSettings)),
                                    new PopupChoiceDialogOption()
                                            .setId(ActionBarCenteredTitle.NEVER.getValue())
                                            .setItemTitle(getString(R.string.ImproveInterfaceTitleCenteredNever))
                            ))
                            .onSelected(() -> animateActionBarUpdate(fragment))
                            .title(getString(R.string.ImproveInterfaceTitleCentered))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(InterfaceSwitchUI.DEFAULT.getValue())
                                            .setItemSwitchIconUI(InterfaceSwitchUI.DEFAULT)
                                            .setItemTitle(getString(R.string.ImproveInterfaceSwitchDefault)),
                                    new PopupChoiceDialogOption()
                                            .setId(InterfaceSwitchUI.ONEUIOLD.getValue())
                                            .setItemSwitchIconUI(InterfaceSwitchUI.ONEUIOLD)
                                            .setItemTitle(getString(R.string.ImproveInterfaceSwitchOneUIOld)),
                                    new PopupChoiceDialogOption()
                                            .setId(InterfaceSwitchUI.ONEUINEW.getValue())
                                            .setItemSwitchIconUI(InterfaceSwitchUI.ONEUINEW)
                                            .setItemTitle(getString(R.string.ImproveInterfaceSwitchOneUINew)),
                                    new PopupChoiceDialogOption()
                                            .setId(InterfaceSwitchUI.GOOGLE.getValue())
                                            .setItemSwitchIconUI(InterfaceSwitchUI.GOOGLE)
                                            .setItemTitle(getString(R.string.ImproveInterfaceSwitchGoogle)),
                                    new PopupChoiceDialogOption()
                                            .setId(InterfaceSwitchUI.GOOGLE_NEW.getValue())
                                            .setItemSwitchIconUI(InterfaceSwitchUI.GOOGLE_NEW)
                                            .setItemTitle(getString(R.string.ImproveInterfaceSwitchGoogleNew))
                            ))
                            .onSelected(() -> {
                                reloadUI(fragment);
                                AndroidUtilities.runOnUIThread(() -> switchesPreview.animateUpdate(), 100);
                            })
                            .currentValue(OctoConfig.INSTANCE.interfaceSwitchUI)
                            .title(getString(R.string.ImproveInterfaceSwitch))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(InterfaceCheckboxUI.DEFAULT.getValue())
                                            .setItemCheckboxIconUI(InterfaceCheckboxUI.DEFAULT)
                                            .setItemTitle(getString(R.string.ImproveInterfaceCheckboxDefault)),
                                    new PopupChoiceDialogOption()
                                            .setId(InterfaceCheckboxUI.ROUNDED.getValue())
                                            .setItemCheckboxIconUI(InterfaceCheckboxUI.ROUNDED)
                                            .setItemTitle(getString(R.string.ImproveInterfaceCheckboxRounded)),
                                    new PopupChoiceDialogOption()
                                            .setId(InterfaceCheckboxUI.TRANSPARENT_UNCHECKED.getValue())
                                            .setItemCheckboxIconUI(InterfaceCheckboxUI.TRANSPARENT_UNCHECKED)
                                            .setItemTitle(getString(R.string.ImproveInterfaceCheckboxSemiTransparent)),
                                    new PopupChoiceDialogOption()
                                            .setId(InterfaceCheckboxUI.ALWAYS_TRANSPARENT.getValue())
                                            .setItemCheckboxIconUI(InterfaceCheckboxUI.ALWAYS_TRANSPARENT)
                                            .setItemTitle(getString(R.string.ImproveInterfaceCheckboxAlwaysTransparent1))
                            ))
                            .onSelected(() -> AndroidUtilities.runOnUIThread(() -> switchesPreview.animateUpdate(), 100))
                            .currentValue(OctoConfig.INSTANCE.interfaceCheckboxUI)
                            .title(getString(R.string.ImproveInterfaceCheckbox))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(InterfaceSliderUI.DEFAULT.getValue())
                                            .setItemSliderIconUI(InterfaceSliderUI.DEFAULT)
                                            .setItemTitle(getString(R.string.ImproveInterfaceSliderDefault)),
                                    new PopupChoiceDialogOption()
                                            .setId(InterfaceSliderUI.MODERN.getValue())
                                            .setItemSliderIconUI(InterfaceSliderUI.MODERN)
                                            .setItemTitle(getString(R.string.ImproveInterfaceSliderModern)),
                                    new PopupChoiceDialogOption()
                                            .setId(InterfaceSliderUI.ANDROID.getValue())
                                            .setItemSliderIconUI(InterfaceSliderUI.ANDROID)
                                            .setItemTitle(getString(R.string.ImproveInterfaceSliderAndroid))
                            ))
                            .onSelected(() -> AndroidUtilities.runOnUIThread(() -> switchesPreview.animateUpdate(), 100))
                            .currentValue(OctoConfig.INSTANCE.interfaceSliderUI)
                            .title(getString(R.string.ImproveInterfaceSlider))
                            .build());
                })
                .category(R.string.DrawerTitle, category -> {
                    drawerPreviewCell = new OctoAppearanceDrawerSettingsUI().fillMainCategory(context, fragment, category, true);
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .isBlue(true)
                            .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoAppearanceDrawerSettingsUI())))
                            .icon(R.drawable.media_draw)
                            .title(getString(R.string.DrawerTitle_More))
                            .build());
                })
                .category(getString(R.string.ImproveIcons), category -> {
                    category.row(new CustomCellRow.CustomCellRowBuilder()
                            .layout(iconsSelectorCell = new IconsSelectorCell(context) {
                                @Override
                                protected void onSelectedIcons() {
                                    updateConfig();
                                    fragment.reloadUIAfterValueUpdate();
                                    fragment.showRestartTooltip();
                                }
                            })
                            .postNotificationName(NotificationCenter.mainUserInfoChanged, NotificationCenter.reloadInterface)
                            .build());
                })
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(getString(R.string.ImproveIconsDefault_Desc))
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
                .category(R.string.ManageFolders, category -> {
                    category.row(new CustomCellRow.CustomCellRowBuilder()
                            .layout(folderTypeSelectorCell = new FolderTypeSelectorCell(context))
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
                                folderTypeSelectorCell.fillTabs();
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
                                folderTypeSelectorCell.fillTabs();
                            })
                            .title(R.string.FolderStyle)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> {
                                AccountInstance.getInstance(UserConfig.selectedAccount).getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
                                folderTypeSelectorCell.fillTabs();
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
                .category(getString(R.string.LocalOther), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.forcePacmanAnimation)
                            .title(getString(R.string.ForcePacmanAnimation))
                            .description(getString(R.string.ForcePacmanAnimation_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.showSnowflakes)
                            .title(getString(R.string.ShowSnowflakes))
                            .requiresRestart(true)
                            .build());
                })
                .build();
    }

    private CharSequence composeIconsDescription(boolean isSolar) {
        return MessageStringHelper.getUrlNoUnderlineText(
                new SpannableString(
                        MessageStringHelper.fromHtml(
                                isSolar ?
                                        formatString(
                                                R.string.ImproveIconsSolar_Desc,
                                                "<a href='tg://resolve?domain=TierOhneNation'>@TierOhneNation</a>",
                                                "<a href='tg://resolve?domain=design480'>@Design480</a>"
                                        ) :
                                        formatString(
                                                R.string.ImproveIconsMaterialDesign3_Desc,
                                                MessageFormat.format("<a href=''https://m3.material.io/styles/icons''>{0}</a>", getString(R.string.ImproveIconsMaterialDesign3_DescHere))
                                        )
                        )
                )
        );
    }

    private void updateConfig() {
        int currentState = OctoConfig.INSTANCE.uiIconsType.getValue();

        isUsingDefault.setValue(currentState == IconsUIType.DEFAULT.getValue());
        isUsingSolar.setValue(currentState == IconsUIType.SOLAR.getValue());
        isUsingM3.setValue(currentState == IconsUIType.MATERIAL_DESIGN_3.getValue());
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
                _centeredMeasure = actionBar.getMeasuredWidth() / 2f - titleTextView.getTextWidth() / 2f - dp((AndroidUtilities.isTablet() ? 80 : 72));
            }

            titleTextView.animate().translationX(_centeredMeasure * (centered ? 1 : 0) - (wasCenteredAtBeginning ? Math.abs(_centeredMeasure) : 0)).setDuration(150).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    wasCentered = centered;

                    reloadUI(fragment);
                    LaunchActivity.makeRipple(centered ? (actionBar.getMeasuredWidth() / 2f) : 0, 0, centered ? 1.3f : 0.1f);
                }
            }).start();
        } else {
            reloadUI(fragment, true);
        }
    }

    private boolean isTitleCentered() {
        int centeredState = OctoConfig.INSTANCE.uiTitleCenteredState.getValue();
        return centeredState == ActionBarCenteredTitle.ALWAYS.getValue() || centeredState == ActionBarCenteredTitle.JUST_IN_SETTINGS.getValue();
    }

    private boolean isFirstDraw = true;
    @Override
    public void onBecomeFullyVisible() {
        if (isFirstDraw) {
            isFirstDraw = false;
            return;
        }

        AndroidUtilities.runOnUIThread(() -> {
            fragment.reloadUIAfterValueUpdate();
            drawerPreviewCell.reloadInstance();
            drawerPreviewCell.updateMiniIcon();
        });
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
}
