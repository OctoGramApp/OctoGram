/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */

package it.octogram.android.preferences.ui;

import android.content.Context;

import it.octogram.android.preferences.rows.impl.*;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;

import it.octogram.android.OctoConfig;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;

public class OctoAppearanceUI implements PreferencesEntry {

    @Override
    public OctoPreferences getPreferences(BaseFragment fragment, Context context) {
        return OctoPreferences.builder(LocaleController.formatString("Appearance", R.string.Appearance))
                .sticker(context, R.raw.utyan_appearance, true, LocaleController.formatString("OctoAppearanceSettingsHeader", R.string.OctoAppearanceSettingsHeader))
                .category("Blur", category -> {
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
                .category("Formatting", category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.formatTimeWithSeconds)
                            .title("Format time with seconds")
                            .requiresRestart(true)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.numberRounding)
                            .title("Number rounding")
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.pencilIconForEditedMessages)
                            .title("Pencil icon for edited messages")
                            .description("This will replace the text \"Edited\" with a pencil icon")
                            .build());
                })
                .category("Header", category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.showNameInActionBar)
                            .title(LocaleController.getString("ShowNameActionBar", R.string.ShowNameActionBar))
                            .description(LocaleController.getString("ShowNameActionBar_Desc", R.string.ShowNameActionBar_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.searchIconInHeader)
                            .title("Search icon in header")
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.slidingTitle)
                            .title("Sliding title")
                            .description("The title will slide on screen when it's too long")
                            .build());
                })
                .category("Fonts & Emojis", category -> {
                    category.row(new TextDetailRow.TextDetailRowBuilder()
                            .icon(R.drawable.msg_emoji_cat)
                            .title("Emoji sets")
                            .description("Feature coming soon")
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.useSystemFont)
                            .title("Use system font")
                            .build());
                })
                .category("Archive", category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.forcePacmanAnimation)
                            .title("Force pacman animation")
                            .description("Force pacman animation when archiving a chat")
                            .build());
                })
                .build();
    }

}
