/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */

package it.octogram.android.preferences.ui;

import android.content.Context;
import android.util.Pair;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;

import java.util.ArrayList;

import it.octogram.android.CustomEmojiController;
import it.octogram.android.OctoConfig;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.rows.impl.CheckboxRow;
import it.octogram.android.preferences.rows.impl.CustomCellRow;
import it.octogram.android.preferences.rows.impl.HeaderRow;
import it.octogram.android.preferences.rows.impl.ListRow;
import it.octogram.android.preferences.rows.impl.SliderRow;
import it.octogram.android.preferences.rows.impl.SwitchRow;
import it.octogram.android.preferences.rows.impl.TextIconRow;
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
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.useSystemFont)
                            .title(LocaleController.getString("UseSystemFont", R.string.UseSystemFont))
                            .requiresRestart(true)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.showSnowflakes)
                            .title(LocaleController.getString("ShowSnowflakes", R.string.ShowSnowflakes))
                            .requiresRestart(true)
                            .build());
                    category.row(new HeaderRow(LocaleController.getString("StickersSizeHeader", R.string.StickersSizeHeader)));
                    category.row(new SliderRow.SliderRowBuilder()
                            .min(2)
                            .max(20)
                            .preferenceValue(OctoConfig.INSTANCE.maxStickerSize)
                            .build());
                })
                .category(LocaleController.formatString("FormattingHeader", R.string.FormattingHeader), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> {
                                LocaleController.getInstance().recreateFormatters();
                                fragment.getParentLayout().rebuildAllFragmentViews(false, false);
                                return true;
                            })
                            .preferenceValue(OctoConfig.INSTANCE.formatTimeWithSeconds)
                            .title(LocaleController.formatString("FormatTimeWithSeconds", R.string.FormatTimeWithSeconds))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.numberRounding)
                            .title(LocaleController.formatString("NumberRounding", R.string.NumberRounding))
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
                .category(LocaleController.getString("ArchiveHeader", R.string.ArchiveHeader), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.forcePacmanAnimation)
                            .title(LocaleController.getString("ForcePacmanAnimation", R.string.ForcePacmanAnimation))
                            .description(LocaleController.getString("ForcePacmanAnimation_Desc", R.string.ForcePacmanAnimation_Desc))
                            .build());
                })
                .category(LocaleController.getString(R.string.FoldersType), category -> category.row(new ListRow.ListRowBuilder()
                        .options(new ArrayList<>() {{
                            add(new Pair<>(0, LocaleController.getString(R.string.FoldersTypeIcons)));
                            add(new Pair<>(1, LocaleController.getString(R.string.FoldersTypeIconsTitles)));
                            add(new Pair<>(2, LocaleController.getString(R.string.FoldersTypeTitles)));
                        }})
                        .currentValue(OctoConfig.INSTANCE.tabMode)
                        .title(LocaleController.getString(R.string.FoldersType))
                        .build()))
                .category(LocaleController.getString("ArchiveHeader", R.string.ArchiveHeader), category -> category.row(new SwitchRow.SwitchRowBuilder()
                        .preferenceValue(OctoConfig.INSTANCE.forcePacmanAnimation)
                        .title(LocaleController.getString("ForcePacmanAnimation", R.string.ForcePacmanAnimation))
                        .description(LocaleController.getString("ForcePacmanAnimation_Desc", R.string.ForcePacmanAnimation_Desc))
                        .build()))
                .category(LocaleController.getString("DrawerElementsHeader", R.string.DrawerElementsHeader), category -> {
                    category.row(new CheckboxRow.CheckboxRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.changeStatus)
                            .title(LocaleController.getString(R.string.SetEmojiStatus))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                    category.row(new CheckboxRow.CheckboxRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.myStories)
                            .title(LocaleController.getString(R.string.ProfileMyStories))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                    category.row(new CheckboxRow.CheckboxRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.newGroup)
                            .title(LocaleController.getString(R.string.NewGroup))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                    category.row(new CheckboxRow.CheckboxRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.newChannel)
                            .title(LocaleController.getString(R.string.NewChannel))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                    category.row(new CheckboxRow.CheckboxRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.contacts)
                            .title(LocaleController.getString(R.string.Contacts))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                    category.row(new CheckboxRow.CheckboxRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.calls)
                            .title(LocaleController.getString(R.string.Calls))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                    category.row(new CheckboxRow.CheckboxRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.peopleNearby)
                            .title(LocaleController.getString(R.string.PeopleNearby))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                    category.row(new CheckboxRow.CheckboxRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.savedMessages)
                            .title(LocaleController.getString(R.string.SavedMessages))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                    category.row(new CheckboxRow.CheckboxRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.settings)
                            .title(LocaleController.getString(R.string.Settings))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                    category.row(new CheckboxRow.CheckboxRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.octogramSettings)
                            .title("OctoGram Settings")
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                    category.row(new CheckboxRow.CheckboxRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.datacenterInfo)
                            .title(LocaleController.getString(R.string.DatacenterStatus))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                    category.row(new CheckboxRow.CheckboxRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.inviteFriends)
                            .title(LocaleController.getString(R.string.InviteFriends))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                    category.row(new CheckboxRow.CheckboxRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.telegramFeatures)
                            .title(LocaleController.getString(R.string.TelegramFeatures))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                })
                .build();
    }

}
