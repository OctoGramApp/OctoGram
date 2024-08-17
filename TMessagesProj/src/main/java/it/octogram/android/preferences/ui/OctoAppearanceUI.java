/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
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
import it.octogram.android.NewFeaturesBadgeId;
import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.rows.impl.ExpandableRows;
import it.octogram.android.preferences.rows.impl.ListRow;
import it.octogram.android.preferences.rows.impl.ShadowRow;
import it.octogram.android.preferences.rows.impl.SwitchRow;
import it.octogram.android.preferences.rows.impl.TextDetailRow;
import it.octogram.android.preferences.rows.impl.TextIconRow;
import it.octogram.android.preferences.ui.custom.CustomActionBarTitleBottomSheet;
import it.octogram.android.utils.ExpandableRowsOption;
import it.octogram.android.utils.PopupChoiceDialogOption;

/**
 * @noinspection deprecation
 */
public class OctoAppearanceUI implements PreferencesEntry {
    @Override
    public OctoPreferences getPreferences(PreferencesFragment fragment, Context context) {
        ConfigProperty<Boolean> showCustomTitleRow = new ConfigProperty<>(null, OctoConfig.INSTANCE.actionBarTitleOption.getValue() == ActionBarTitleOption.CUSTOM.getValue());

        return OctoPreferences.builder(LocaleController.formatString(R.string.Appearance))
                .sticker(context, OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME, StickerUi.APPEARANCE, true, LocaleController.formatString("OctoAppearanceSettingsHeader", R.string.OctoAppearanceSettingsHeader))
                .row(new TextDetailRow.TextDetailRowBuilder()
                        .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoChatsSettingsUI())))
                        .icon(R.drawable.msg_groups)
                        .isNew(NewFeaturesBadgeId.CHATS_BADGE.getId())
                        .title(getString("ChatTitle", R.string.ChatTitle))
                        .description(getString("Chat_Desc", R.string.Chat_Desc))
                        .build()
                )
                .row(new TextDetailRow.TextDetailRowBuilder()
                        .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoDrawerSettingsUI())))
                        .icon(R.drawable.msg_map_type)
                        .isNew(NewFeaturesBadgeId.DRAWER_BADGE.getId())
                        .title(getString("DrawerTitle", R.string.DrawerTitle))
                        .description(getString("Drawer_Desc", R.string.Drawer_Desc))
                        .build()
                )
                .row(new ExpandableRows.ExpandableRowsBuilder()
                        .setId(ExpandableRowsIds.CONTEXT_MENU_ELEMENTS.getId())
                        .setIcon(R.drawable.msg_list)
                        .setMainTitle(getString("ContextElements", R.string.ContextElements))
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
                        .postNotificationName(NotificationCenter.reloadInterface)
                        .build()
                )
                .row(new ShadowRow())
                .category(getString("FontEmojisHeader", R.string.FontEmojisHeader), category -> {
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new EmojiPackSettings()))
                            .value(CustomEmojiController.getSelectedPackName())
                            .icon(R.drawable.msg_emoji_cat)
                            .title(getString("EmojiSets", R.string.EmojiSets))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> {
                                AndroidUtilities.clearTypefaceCache();
                                fragment.rebuildAllFragmentsWithLast();
                            })
                            .icon(R.drawable.msg_text_outlined)
                            .preferenceValue(OctoConfig.INSTANCE.useSystemFont)
                            .title(getString("UseSystemFont", R.string.UseSystemFont))
                            .requiresRestart(true)
                            .build());
                })
                .category(LocaleController.formatString("InterfaceHeader", R.string.InterfaceHeader), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.showUserIconsInChatsList)
                            .title(getString("ShowUserIconsInChatsList", R.string.ShowUserIconsInChatsList))
                            .description(getString("ShowUserIconsInChatsList_Desc", R.string.ShowUserIconsInChatsList_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hideStories)
                            .requiresRestart(true)
                            .title(getString("HideStories", R.string.HideStories))
                            .description(getString("HideStories_Desc", R.string.HideStories_Desc))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.actionBarTitleOption)
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(ActionBarTitleOption.EMPTY.getValue())
                                            .setItemTitle(getString("ActionBarTitleCustomEmpty", R.string.ActionBarTitleCustomEmpty)),
                                    new PopupChoiceDialogOption()
                                            .setId(ActionBarTitleOption.APP_NAME.getValue())
                                            .setItemTitle(getString("BuildAppName", R.string.BuildAppName)),
                                    new PopupChoiceDialogOption()
                                            .setId(ActionBarTitleOption.ACCOUNT_NAME.getValue())
                                            .setItemTitle(getString("ActionBarTitleAccountName", R.string.ActionBarTitleAccountName)),
                                    new PopupChoiceDialogOption()
                                            .setId(ActionBarTitleOption.ACCOUNT_USERNAME.getValue())
                                            .setItemTitle(getString("ActionBarTitleAccountUsername", R.string.ActionBarTitleAccountUsername)),
                                    new PopupChoiceDialogOption()
                                            .setId(ActionBarTitleOption.CUSTOM.getValue())
                                            .setItemTitle(getString("ActionBarTitleCustom", R.string.ActionBarTitleCustom))
                            ))
                            .onSelected(() -> showCustomTitleRow.setValue(OctoConfig.INSTANCE.actionBarTitleOption.getValue() == ActionBarTitleOption.CUSTOM.getValue()))
                            .title(getString("ActionBarTitle", R.string.ActionBarTitle))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> editCustomName(fragment, context))
                            .value(getCustomNameStatus())
                            .showIf(showCustomTitleRow)
                            .title(getString("ActionBarTitleCustom", R.string.ActionBarTitleCustom))
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
                            .title(LocaleController.formatString("HideDividers", R.string.HideDividers))
                            .build());
                })
                .category(getString("LocalOther", R.string.LocalOther), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.forcePacmanAnimation)
                            .title(getString("ForcePacmanAnimation", R.string.ForcePacmanAnimation))
                            .description(getString("ForcePacmanAnimation_Desc", R.string.ForcePacmanAnimation_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.showSnowflakes)
                            .title(getString("ShowSnowflakes", R.string.ShowSnowflakes))
                            .requiresRestart(true)
                            .build());
                })
                .build();
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
            customName = getString("ActionBarTitleCustomEmpty", R.string.ActionBarTitleCustomEmpty);
        }

        return customName;
    }
}
