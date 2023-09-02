/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */

package it.octogram.android.preferences.ui;

import android.content.Context;

import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.rows.impl.*;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.INavigationLayout;
import org.telegram.ui.ActionBar.Theme;

import it.octogram.android.CustomEmojiController;
import it.octogram.android.OctoConfig;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.ui.custom.ThemeSelectorCell;

public class OctoAppearanceUI implements PreferencesEntry {

    @Override
    public OctoPreferences getPreferences(BaseFragment fragment, Context context) {
        return OctoPreferences.builder(LocaleController.formatString("Appearance", R.string.Appearance))
                .sticker(context, R.raw.utyan_appearance, true, LocaleController.formatString("OctoAppearanceSettingsHeader", R.string.OctoAppearanceSettingsHeader))
                .category(LocaleController.getString("FontEmojisHeader", R.string.FontEmojisHeader), category -> {
                    category.row(new CustomCellRow.CustomCellRowBuilder()
                            .layout(new ThemeSelectorCell(context, OctoConfig.INSTANCE.eventType.getValue()) {
                                @Override
                                protected void onSelectedEvent(int eventSelected) {
                                    super.onSelectedEvent(eventSelected);
                                    OctoConfig.INSTANCE.updateIntegerSetting(OctoConfig.INSTANCE.eventType, eventSelected);

                                    Theme.lastHolidayCheckTime = 0;
                                    Theme.dialogs_holidayDrawable = null;

                                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.mainUserInfoChanged);
                                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.reloadInterface);
                                }
                            })
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new EmojiPackSettings()))
                            .value(CustomEmojiController.getSelectedPackName())
                            .icon(R.drawable.msg_emoji_cat)
                            .title(LocaleController.getString("EmojiSets", R.string.EmojiSets))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .icon(R.drawable.msg_text_outlined)
                            .preferenceValue(OctoConfig.INSTANCE.useSystemFont)
                            .title(LocaleController.getString("UseSystemFont", R.string.UseSystemFont))
                            .requiresRestart(true)
                            .build());
                })
                .row(new TextDetailRow.TextDetailRowBuilder()
                        .onClick(() -> fragment.presentFragment(new PreferencesFragment(new DrawerElementsSettingsUI())))
                        .icon(R.drawable.msg_message)
                        .title(LocaleController.getString("DrawerElements", R.string.DrawerElements))
                        .description(LocaleController.getString("DrawerElements_Desc", R.string.DrawerElements_Desc))
                        .build())
//                .row(new TextDetailRow.TextDetailRowBuilder()
//                        .onClick(() -> fragment.presentFragment(new DatacenterActivity()))
//                        .icon(R.drawable.msg_menu_stories)
//                        .title(LocaleController.getString("ContextElements", R.string.ContextElements))
//                        .description(LocaleController.getString("ContextElements_Desc", R.string.ContextElements_Desc))
//                        .build())
                .row(new ShadowRow())
                .category(LocaleController.formatString("FormattingHeader", R.string.FormattingHeader), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> {
                                LocaleController.getInstance().recreateFormatters();
                                fragment.getParentLayout().rebuildAllFragmentViews(false, false);
                                return true;
                            })
                            .preferenceValue(OctoConfig.INSTANCE.formatTimeWithSeconds)
                            .title(LocaleController.formatString("FormatTimeWithSeconds", R.string.FormatTimeWithSeconds))
                            .description(LocaleController.formatString("FormatTimeWithSeconds_Desc", R.string.FormatTimeWithSeconds_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.numberRounding)
                            .title(LocaleController.formatString("NumberRounding", R.string.NumberRounding))
                            .description(LocaleController.formatString("NumberRounding_Desc", R.string.NumberRounding_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> {
                                fragment.getParentLayout().rebuildAllFragmentViews(false, false);
                                return true;
                            })
                            .preferenceValue(OctoConfig.INSTANCE.pencilIconForEditedMessages)
                            .title(LocaleController.formatString("PencilIconForEdited", R.string.PencilIconForEdited))
                            .description(LocaleController.formatString("PencilIconForEdited_Desc", R.string.PencilIconForEdited_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> {
                                fragment.getParentLayout().rebuildFragments(INavigationLayout.REBUILD_FLAG_REBUILD_LAST);
                                return true;
                            })
                            .preferenceValue(OctoConfig.INSTANCE.disableDividers)
                            .title(LocaleController.formatString("HideDividers", R.string.HideDividers))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                })
                .category(LocaleController.getString("HeaderHeader", R.string.HeaderHeader), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.showNameInActionBar)
                            .title(LocaleController.getString("ShowNameActionBar", R.string.ShowNameActionBar))
                            .description(LocaleController.getString("ShowNameActionBar_Desc", R.string.ShowNameActionBar_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.searchIconInHeader)
                            .title(LocaleController.getString("SearchIconInHeader", R.string.SearchIconInHeader))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.slidingTitle)
                            .title(LocaleController.getString("SlidingTitle", R.string.SlidingTitle))
                            .description(LocaleController.getString("SlidingTitle_Desc", R.string.SlidingTitle_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hideStories)
                            .requiresRestart(true)
                            .title(LocaleController.getString("HideStories", R.string.HideStories))
                            .description(LocaleController.getString("HideStories_Desc", R.string.HideStories_Desc))
                            .build());
                })
                .category(LocaleController.formatString("BlurHeader", R.string.BlurHeader), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.forceChatBlurEffect)
                            .title(LocaleController.getString("ForceChatBlurEffect", R.string.ForceChatBlurEffect))
                            .build());
                    category.row(new HeaderRow(LocaleController.getString("ForceChatBlurEffectName", R.string.ForceChatBlurEffectName), OctoConfig.INSTANCE.forceChatBlurEffect));
                    category.row(new SliderRow.SliderRowBuilder()
                            .min(0)
                            .max(255)
                            .preferenceValue(OctoConfig.INSTANCE.blurEffectStrength)
                            .showIf(OctoConfig.INSTANCE.forceChatBlurEffect)
                            .build());
                })
                .row(new HeaderRow(LocaleController.getString("StickersSizeHeader", R.string.StickersSizeHeader)))
                .row(new SliderRow.SliderRowBuilder()
                        .min(2)
                        .max(20)
                        .preferenceValue(OctoConfig.INSTANCE.maxStickerSize)
                        .build())
                .row(new ShadowRow())
                .category(LocaleController.getString("ArchiveHeader", R.string.ArchiveHeader), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.forcePacmanAnimation)
                            .title(LocaleController.getString("ForcePacmanAnimation", R.string.ForcePacmanAnimation))
                            .description(LocaleController.getString("ForcePacmanAnimation_Desc", R.string.ForcePacmanAnimation_Desc))
                            .build());
                })
                .category(LocaleController.getString("LocalOther", R.string.LocalOther), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.showSnowflakes)
                            .title(LocaleController.getString("ShowSnowflakes", R.string.ShowSnowflakes))
                            .requiresRestart(true)
                            .build());
                })
                .build();
    }

}
