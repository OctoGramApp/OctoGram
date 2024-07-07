/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

package it.octogram.android.preferences.ui;

import android.content.Context;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

import java.util.List;

import it.octogram.android.ActionBarTitleOption;
import it.octogram.android.ConfigProperty;
import it.octogram.android.CustomEmojiController;
import it.octogram.android.NewFeaturesBadgeId;
import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.rows.impl.ListRow;
import it.octogram.android.preferences.rows.impl.ShadowRow;
import it.octogram.android.preferences.rows.impl.SwitchRow;
import it.octogram.android.preferences.rows.impl.TextDetailRow;
import it.octogram.android.preferences.rows.impl.TextIconRow;
import it.octogram.android.preferences.ui.custom.CustomActionBarTitleBottomSheet;
import it.octogram.android.utils.PopupChoiceDialogOption;

/** @noinspection deprecation*/
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
                        .title(LocaleController.getString("ChatTitle", R.string.ChatTitle))
                        .description(LocaleController.getString("Chat_Desc", R.string.Chat_Desc))
                        .build())
                .row(new TextDetailRow.TextDetailRowBuilder()
                        .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoDrawerSettingsUI())))
                        .icon(R.drawable.msg_map_type)
                        .isNew(NewFeaturesBadgeId.DRAWER_BADGE.getId())
                        .title(LocaleController.getString("DrawerTitle", R.string.DrawerTitle))
                        .description(LocaleController.getString("Drawer_Desc", R.string.Drawer_Desc))
                        .build())
                .row(new TextDetailRow.TextDetailRowBuilder()
                        .onClick(() -> fragment.presentFragment(new PreferencesFragment(new ContextElementsSettingsUI())))
                        .icon(R.drawable.msg_list)
                        .title(LocaleController.getString("ContextElements", R.string.ContextElements))
                        .description(LocaleController.getString("ContextElements_Desc", R.string.ContextElements_Desc))
                        .build())
                .row(new ShadowRow())
                .category(LocaleController.getString("FontEmojisHeader", R.string.FontEmojisHeader), category -> {
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new EmojiPackSettings()))
                            .value(CustomEmojiController.getSelectedPackName())
                            .icon(R.drawable.msg_emoji_cat)
                            .title(LocaleController.getString("EmojiSets", R.string.EmojiSets))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> {
                                AndroidUtilities.clearTypefaceCache();
                                fragment.rebuildAllFragmentsWithLast();
                            })
                            .icon(R.drawable.msg_text_outlined)
                            .preferenceValue(OctoConfig.INSTANCE.useSystemFont)
                            .title(LocaleController.getString("UseSystemFont", R.string.UseSystemFont))
                            .requiresRestart(true)
                            .build());
                })
                .category(LocaleController.formatString("InterfaceHeader", R.string.InterfaceHeader), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.showUserIconsInChatsList)
                            .title(LocaleController.getString("ShowUserIconsInChatsList", R.string.ShowUserIconsInChatsList))
                            .description(LocaleController.getString("ShowUserIconsInChatsList_Desc", R.string.ShowUserIconsInChatsList_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hideStories)
                            .requiresRestart(true)
                            .title(LocaleController.getString("HideStories", R.string.HideStories))
                            .description(LocaleController.getString("HideStories_Desc", R.string.HideStories_Desc))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.actionBarTitleOption)
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(ActionBarTitleOption.EMPTY.getValue())
                                            .setItemTitle(LocaleController.getString("ActionBarTitleCustomEmpty", R.string.ActionBarTitleCustomEmpty)),
                                    new PopupChoiceDialogOption()
                                            .setId(ActionBarTitleOption.APP_NAME.getValue())
                                            .setItemTitle(LocaleController.getString("BuildAppName", R.string.BuildAppName)),
                                    new PopupChoiceDialogOption()
                                            .setId(ActionBarTitleOption.ACCOUNT_NAME.getValue())
                                            .setItemTitle(LocaleController.getString("ActionBarTitleAccountName", R.string.ActionBarTitleAccountName)),
                                    new PopupChoiceDialogOption()
                                            .setId(ActionBarTitleOption.ACCOUNT_USERNAME.getValue())
                                            .setItemTitle(LocaleController.getString("ActionBarTitleAccountUsername", R.string.ActionBarTitleAccountUsername)),
                                    new PopupChoiceDialogOption()
                                            .setId(ActionBarTitleOption.CUSTOM.getValue())
                                            .setItemTitle(LocaleController.getString("ActionBarTitleCustom", R.string.ActionBarTitleCustom))
                            ))
                            .onSelected(() -> showCustomTitleRow.setValue(OctoConfig.INSTANCE.actionBarTitleOption.getValue() == ActionBarTitleOption.CUSTOM.getValue()))
                            .title(LocaleController.getString("ActionBarTitle", R.string.ActionBarTitle))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> editCustomName(fragment, context))
                            .value(getCustomNameStatus())
                            .showIf(showCustomTitleRow)
                            .title(LocaleController.getString("ActionBarTitleCustom", R.string.ActionBarTitleCustom))
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
                .category(LocaleController.getString("LocalOther", R.string.LocalOther), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.forcePacmanAnimation)
                            .title(LocaleController.getString("ForcePacmanAnimation", R.string.ForcePacmanAnimation))
                            .description(LocaleController.getString("ForcePacmanAnimation_Desc", R.string.ForcePacmanAnimation_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.showSnowflakes)
                            .title(LocaleController.getString("ShowSnowflakes", R.string.ShowSnowflakes))
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
            customName = LocaleController.getString("ActionBarTitleCustomEmpty", R.string.ActionBarTitleCustomEmpty);
        }

        return customName;
    }
}
