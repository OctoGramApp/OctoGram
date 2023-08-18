/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */

package it.octogram.android.preferences.ui;

import android.content.Context;
import it.octogram.android.OctoConfig;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.rows.impl.*;
import it.octogram.android.preferences.ui.custom.ThemeSelectorCell;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.INavigationLayout;

public class OctoAppearanceUI implements PreferencesEntry {

    @Override
    public OctoPreferences getPreferences(BaseFragment fragment, Context context) {
        return OctoPreferences.builder(LocaleController.formatString("Appearance", R.string.Appearance))
                .sticker(context, R.raw.utyan_appearance, true, LocaleController.formatString("OctoAppearanceSettingsHeader", R.string.OctoAppearanceSettingsHeader))
                .category("Sticker Size", category -> {
                    category.row(
                            new SliderRow.SliderRowBuilder()
                                    .min(2)
                                    .max(20)
                                    .preferenceValue(OctoConfig.INSTANCE.maxStickerSize)
                                    //.postNotificationName(NotificationCenter.reloadInterface, NotificationCenter.updateInterfaces)
                                    .build()
                    );
                    /*category.row(
                            new CustomCellRow.CustomCellRowBuilder().layout(new StickerSizePreviewMessages(context, INavigationLayout.newLayout(context))).build()
                    );*/

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
                            .postNotificationName(NotificationCenter.storiesUpdated, NotificationCenter.storiesEnabledUpdate, NotificationCenter.reloadInterface, NotificationCenter.updateInterfaces)
                            .title("Hide stories")
                            .build());
                })
                .category("Icon sets", category -> {
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
                    category.row(new FooterInformativeRow.FooterInformativeRowBuilder()
                            .title("You can force telegram to change the emoji event type")
                            .build());
                })
                .category(LocaleController.getString("FontEmojisHeader", R.string.FontEmojisHeader), category -> {
                    category.row(new TextDetailRow.TextDetailRowBuilder()
                            .icon(R.drawable.msg_emoji_cat)
                            .title(LocaleController.getString("EmojiSets", R.string.EmojiSets))
                            .description(LocaleController.getString("FeatureCurrentlyUnavailable", R.string.FeatureCurrentlyUnavailable))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.useSystemFont)
                            .title(LocaleController.getString("UseSystemFont", R.string.UseSystemFont))
                            .requiresRestart(true)
                            .build());
                })
                .category(LocaleController.getString("ArchiveHeader", R.string.ArchiveHeader), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.forcePacmanAnimation)
                            .title(LocaleController.getString("ForcePacmanAnimation", R.string.ForcePacmanAnimation))
                            .description(LocaleController.getString("ForcePacmanAnimation_Desc", R.string.ForcePacmanAnimation_Desc))
                            .build());
                })
                .build();
    }

}
