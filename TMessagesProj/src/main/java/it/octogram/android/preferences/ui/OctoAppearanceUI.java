/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;

import java.util.List;

import it.octogram.android.ActionBarTitleOption;
import it.octogram.android.ConfigProperty;
import it.octogram.android.CustomEmojiController;
import it.octogram.android.ExpandableRowsIds;
import it.octogram.android.OctoConfig;
import it.octogram.android.ShortcutsPosition;
import it.octogram.android.StickerUi;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.rows.impl.ExpandableRows;
import it.octogram.android.preferences.rows.impl.FooterInformativeRow;
import it.octogram.android.preferences.rows.impl.ListRow;
import it.octogram.android.preferences.rows.impl.ShadowRow;
import it.octogram.android.preferences.rows.impl.SwitchRow;
import it.octogram.android.preferences.rows.impl.TextDetailRow;
import it.octogram.android.preferences.rows.impl.TextIconRow;
import it.octogram.android.preferences.ui.custom.CustomActionBarTitleBottomSheet;
import it.octogram.android.utils.ExpandableRowsOption;
import it.octogram.android.utils.OctoUtils;
import it.octogram.android.utils.PopupChoiceDialogOption;

public class OctoAppearanceUI implements PreferencesEntry {
    @Override
    public OctoPreferences getPreferences(PreferencesFragment fragment, Context context) {
        ConfigProperty<Boolean> showCustomTitleRow = new ConfigProperty<>(null, OctoConfig.INSTANCE.actionBarTitleOption.getValue() == ActionBarTitleOption.CUSTOM.getValue());

        ConfigProperty<Boolean> areShortcutsEnabled = new ConfigProperty<>(null, false);
        ConfigProperty<Boolean> showTooManyOptionsAlert = new ConfigProperty<>(null, false);

        Runnable updateShortcutsState = () -> {
            areShortcutsEnabled.updateValue(areShortcutsEnabled());
            showTooManyOptionsAlert.updateValue(getEnabledShortcutsCount() > 2 && OctoConfig.INSTANCE.shortcutsPosition.getValue() == ShortcutsPosition.CHAT_INFO.getId());
        };
        updateShortcutsState.run();

        return OctoPreferences.builder(LocaleController.getString(R.string.Appearance))
                .sticker(context, OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME, StickerUi.APPEARANCE, true, LocaleController.getString(R.string.OctoAppearanceSettingsHeader))
                .row(new TextDetailRow.TextDetailRowBuilder()
                        .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoChatsSettingsUI())))
                        .icon(R.drawable.msg_groups)
                        .title(getString(R.string.ChatTitle))
                        .description(getString(R.string.Chat_Desc))
                        .build()
                )
                .row(new TextDetailRow.TextDetailRowBuilder()
                        .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoDrawerSettingsUI())))
                        .icon(R.drawable.msg_map_type)
                        .title(getString(R.string.DrawerTitle))
                        .description(getString(R.string.Drawer_Desc))
                        .build()
                )
                .row(new TextDetailRow.TextDetailRowBuilder()
                        .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoInterfaceSettingsUI())))
                        .icon(R.drawable.media_draw)
                        .title(getString(R.string.AppTitleSettings))
                        .description(getString(R.string.AppTitle_Desc))
                        .build()
                )
                .row(new ShadowRow())
                .category(getString(R.string.ActionsHeader), category -> {
                    category.row(new ExpandableRows.ExpandableRowsBuilder()
                            .setId(ExpandableRowsIds.CONTEXT_MENU_ELEMENTS.getId())
                            .setIcon(R.drawable.msg_list)
                            .setMainTitle(getString(R.string.ContextElements))
                            .addRow(new ExpandableRowsOption()
                                    .setOptionTitle(getString(R.string.ClearFromCache))
                                    .setProperty(OctoConfig.INSTANCE.contextClearFromCache)
                            )
                            .addRow(new ExpandableRowsOption()
                                    .setOptionTitle(getString(R.string.CopyPhoto))
                                    .setProperty(OctoConfig.INSTANCE.contextCopyPhoto)
                            )
                            .addRow(new ExpandableRowsOption()
                                    .setOptionTitle(getString(R.string.AddToSavedMessages))
                                    .setProperty(OctoConfig.INSTANCE.contextSaveMessage)
                            )
                            .addRow(new ExpandableRowsOption()
                                    .setOptionTitle(getString(R.string.ReportChat))
                                    .setProperty(OctoConfig.INSTANCE.contextReportMessage)
                            )
                            .addRow(new ExpandableRowsOption()
                                    .setOptionTitle(getString(R.string.MessageDetails))
                                    .setProperty(OctoConfig.INSTANCE.contextMessageDetails)
                            )
                            .addRow(new ExpandableRowsOption()
                                    .setOptionTitle(getString(R.string.NoQuoteForward))
                                    .setProperty(OctoConfig.INSTANCE.contextNoQuoteForward)
                            )
                            .addRow(new ExpandableRowsOption()
                                    .setOptionTitle(getString(R.string.CustomF_ReplyPvt))
                                    .setProperty(OctoConfig.INSTANCE.contextReplyPrivateChat)
                            )
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build()
                    );
                    category.row(new ExpandableRows.ExpandableRowsBuilder()
                            .setId(ExpandableRowsIds.ADMIN_SHORTCUTS.getId())
                            .setIcon(R.drawable.msg_admins)
                            .setMainTitle(getString(R.string.AdminShortcuts))
                            .setOnSingleStateChange(updateShortcutsState)
                            .addRow(new ExpandableRowsOption()
                                    .setOptionTitle(getString(R.string.ChannelAdministrators))
                                    .setProperty(OctoConfig.INSTANCE.shortcutsAdministrators)
                            )
                            .addRow(new ExpandableRowsOption()
                                    .setOptionTitle(getString(R.string.EventLog))
                                    .setProperty(OctoConfig.INSTANCE.shortcutsRecentActions)
                            )
                            .addRow(new ExpandableRowsOption()
                                    .setOptionTitle(getString(R.string.Statistics))
                                    .setProperty(OctoConfig.INSTANCE.shortcutsStatistics)
                            )
                            .addRow(new ExpandableRowsOption()
                                    .setOptionTitle(getString(R.string.ChannelPermissions))
                                    .setProperty(OctoConfig.INSTANCE.shortcutsPermissions)
                            )
                            .addRow(new ExpandableRowsOption()
                                    .setOptionTitle(getString(R.string.InviteLinks))
                                    .setProperty(OctoConfig.INSTANCE.shortcutsInviteLinks)
                            )
                            .addRow(new ExpandableRowsOption()
                                    .setOptionTitle(getString(R.string.GroupMembers))
                                    .setProperty(OctoConfig.INSTANCE.shortcutsMembers)
                            )
                            .build()
                    );
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.shortcutsPosition)
                            .onSelected(updateShortcutsState)
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(ShortcutsPosition.THREE_DOTS.getId())
                                            .setItemTitle(getString(R.string.AdminShortcutsPositionThreeDots))
                                            .setItemDescription(getString(R.string.AdminShortcutsPositionThreeDots_Desc)),
                                    new PopupChoiceDialogOption()
                                            .setId(ShortcutsPosition.CHAT_INFO.getId())
                                            .setItemTitle(getString(R.string.AdminShortcutsPositionChatInfo))
                                            .setItemDescription(getString(R.string.AdminShortcutsPositionChatInfo_Desc)+" "+getString(R.string.AdminShortcutsPositionChatInfo_Alert)),
                                    new PopupChoiceDialogOption()
                                            .setId(ShortcutsPosition.PROFILE_DOTS.getId())
                                            .setItemTitle(getString(R.string.AdminShortcutsPositionChatThreeDots))
                                            .setItemDescription(getString(R.string.AdminShortcutsPositionChatThreeDots_Desc))
                            ))
                            .showIf(areShortcutsEnabled)
                            .title(getString(R.string.AdminShortcutsPosition))
                            .build());
                })
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(LocaleController.getString(R.string.AdminShortcutsPositionChatInfo_Alert))
                        .showIf(showTooManyOptionsAlert)
                        .build())
                .category(getString(R.string.FontEmojisHeader), category -> {
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new EmojiPackSettings()))
                            .value(CustomEmojiController.getSelectedPackName())
                            .icon(OctoUtils.getPetIconFixed())
                            .title(getString(R.string.EmojiSets))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> {
                                AndroidUtilities.clearTypefaceCache();
                                Parcelable recyclerViewState = null;
                                if (fragment.getListView().getLayoutManager() != null)
                                    recyclerViewState = fragment.getListView().getLayoutManager().onSaveInstanceState();
                                fragment.getParentLayout().rebuildAllFragmentViews(true, true);
                                fragment.getListView().getLayoutManager().onRestoreInstanceState(recyclerViewState);
                            })
                            .icon(R.drawable.msg_text_outlined)
                            .preferenceValue(OctoConfig.INSTANCE.useSystemFont)
                            .title(getString(R.string.UseSystemFont))
                            .requiresRestart(true)
                            .build());
                })
                .category(LocaleController.getString(R.string.InterfaceHeader), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.showUserIconsInChatsList)
                            .title(getString(R.string.ShowUserIconsInChatsList))
                            .description(getString(R.string.ShowUserIconsInChatsList_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hideStories)
                            .requiresRestart(true)
                            .title(getString(R.string.HideStories))
                            .description(getString(R.string.HideStories_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.alwaysShowDownloads)
                            .requiresRestart(true)
                            .title(getString(R.string.AlwaysShowDownloads))
                            .description(getString(R.string.AlwaysShowDownloads_Desc))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.actionBarTitleOption)
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(ActionBarTitleOption.EMPTY.getValue())
                                            .setItemTitle(getString(R.string.ActionBarTitleCustomEmpty)),
                                    new PopupChoiceDialogOption()
                                            .setId(ActionBarTitleOption.APP_NAME.getValue())
                                            .setItemTitle(getString(R.string.BuildAppName)),
                                    new PopupChoiceDialogOption()
                                            .setId(ActionBarTitleOption.ACCOUNT_NAME.getValue())
                                            .setItemTitle(getString(R.string.ActionBarTitleAccountName)),
                                    new PopupChoiceDialogOption()
                                            .setId(ActionBarTitleOption.ACCOUNT_USERNAME.getValue())
                                            .setItemTitle(getString(R.string.ActionBarTitleAccountUsername)),
                                    new PopupChoiceDialogOption()
                                            .setId(ActionBarTitleOption.FOLDER_NAME.getValue())
                                            .setItemTitle(getString(R.string.ActionBarTitleFolderName)),
                                    new PopupChoiceDialogOption()
                                            .setId(ActionBarTitleOption.CUSTOM.getValue())
                                            .setItemTitle(getString(R.string.ActionBarTitleCustom))
                            ))
                            .onSelected(() -> {
                                showCustomTitleRow.setValue(OctoConfig.INSTANCE.actionBarTitleOption.getValue() == ActionBarTitleOption.CUSTOM.getValue());
                                fragment.rebuildAllFragmentsWithLast();
                            })
                            .title(getString(R.string.ActionBarTitle))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> editCustomName(fragment, context))
                            .value(getCustomNameStatus())
                            .showIf(showCustomTitleRow)
                            .title(getString(R.string.ActionBarTitleCustom))
                            .build()
                    );
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> {
                                Parcelable recyclerViewState = null;
                                RecyclerView.LayoutManager layoutManager = fragment.getListView().getLayoutManager();
                                if (layoutManager != null)
                                    recyclerViewState = layoutManager.onSaveInstanceState();
                                fragment.getParentLayout().rebuildAllFragmentViews(false, false);
                                if (layoutManager != null && recyclerViewState != null)
                                    layoutManager.onRestoreInstanceState(recyclerViewState);
                            })
                            .preferenceValue(OctoConfig.INSTANCE.disableDividers)
                            .title(LocaleController.getString(R.string.HideDividers))
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

    private int getEnabledShortcutsCount() {
        int i = 0;
        if (OctoConfig.INSTANCE.shortcutsAdministrators.getValue()) i++;
        if (OctoConfig.INSTANCE.shortcutsRecentActions.getValue()) i++;
        if (OctoConfig.INSTANCE.shortcutsStatistics.getValue()) i++;
        if (OctoConfig.INSTANCE.shortcutsPermissions.getValue()) i++;
        if (OctoConfig.INSTANCE.shortcutsInviteLinks.getValue()) i++;
        if (OctoConfig.INSTANCE.shortcutsMembers.getValue()) i++;
        return i;
    }

    private boolean areShortcutsEnabled() {
        return getEnabledShortcutsCount() > 0;
    }

    private void editCustomName(PreferencesFragment fragment, Context context) {
        var bottomSheet = new CustomActionBarTitleBottomSheet(context, new CustomActionBarTitleBottomSheet.CustomActionBarTitleCallback() {
            @Override
            public void didRenameSuccessfully(String customName) {
                OctoConfig.INSTANCE.actionBarCustomTitle.updateValue(customName);
                OctoConfig.INSTANCE.actionBarTitleOption.updateValue(ActionBarTitleOption.CUSTOM.getValue());
                fragment.rebuildAllFragmentsWithLast();
            }

            @Override
            public void didReset() {
                OctoConfig.INSTANCE.actionBarCustomTitle.updateValue("Home");
                OctoConfig.INSTANCE.actionBarTitleOption.updateValue(ActionBarTitleOption.EMPTY.getValue());
                fragment.rebuildAllFragmentsWithLast();
            }
        });
        bottomSheet.show();
    }

    private String getCustomNameStatus() {
        String customName = OctoConfig.INSTANCE.actionBarCustomTitle.getValue();
        if (TextUtils.isEmpty(customName)) {
            customName = getString(R.string.ActionBarTitleCustomEmpty);
        }

        return customName;
    }
}
