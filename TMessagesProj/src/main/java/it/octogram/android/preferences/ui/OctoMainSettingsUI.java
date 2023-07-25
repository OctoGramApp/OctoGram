/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */

package it.octogram.android.preferences.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.BulletinFactory;

import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.rows.impl.FooterRow;
import it.octogram.android.preferences.rows.impl.TextDetailRow;
import it.octogram.android.preferences.rows.impl.TextIconRow;

public class OctoMainSettingsUI implements PreferencesEntry {

    @Override
    public OctoPreferences getPreferences(BaseFragment fragment, Context context) {
        String footer = AndroidUtilities.replaceTags(LocaleController.formatString("OctoMainSettingsFooter", R.string.OctoMainSettingsFooter, BuildConfig.BUILD_VERSION_STRING)).toString();
        String comingSoon = AndroidUtilities.replaceTags(LocaleController.formatString("FeatureCurrentlyUnavailable", R.string.FeatureCurrentlyUnavailable)).toString();
        return OctoPreferences.builder("OctoGram Settings")
                .sticker(context, R.raw.utyan_robot, true, "Welcome to the OctoGram Settings! Here you can customize your experience with the app.")
                .category(LocaleController.formatString("Settings", R.string.Settings), category -> {
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> {
                                fragment.presentFragment(new PreferencesFragment(context, new OctoGeneralSettingsUI()));
                            })
                            .icon(R.drawable.msg_media)
                            .title(LocaleController.formatString("General", R.string.General))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> {
                                BulletinFactory.of(fragment).createErrorBulletin(comingSoon, fragment.getResourceProvider()).show();
                            })
                            .icon(R.drawable.msg_translate)
                            .title(LocaleController.formatString("Translator", R.string.Translator))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> {
                                BulletinFactory.of(fragment).createErrorBulletin(comingSoon, fragment.getResourceProvider()).show();
                            })
                            .icon(R.drawable.msg_colors)
                            .title(LocaleController.formatString("Appearance", R.string.Appearance))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> {
                                BulletinFactory.of(fragment).createErrorBulletin(comingSoon, fragment.getResourceProvider()).show();
                            })
                            .icon(R.drawable.msg_camera)
                            .title(LocaleController.formatString("ChatCamera", R.string.ChatCamera))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> {
                                BulletinFactory.of(fragment).createErrorBulletin(comingSoon, fragment.getResourceProvider()).show();
                            })
                            .icon(R.drawable.msg_photo_rotate)
                            .title(LocaleController.formatString("Updates", R.string.Updates))
                            .build());
                })
                .category(LocaleController.formatString("OctoMainSettingsInfoCategory", R.string.OctoMainSettingsInfoCategory), category -> {
                    category.row(new TextDetailRow.TextDetailRowBuilder()
                            .onClick(() -> {
                                MessagesController.getInstance(fragment.getCurrentAccount()).openByUserName("OctoGramApp", fragment, 1);
                            })
                            .icon(R.drawable.msg_channel)
                            .title(LocaleController.formatString("OfficialChannel", R.string.OfficialChannel))
                            .description(LocaleController.formatString("OfficialChannel_Desc", R.string.OfficialChannel_Desc))
                            .build());
                    category.row(new TextDetailRow.TextDetailRowBuilder()
                            .onClick(() -> {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/OctoGramApp/OctoGram"));
                                fragment.getParentActivity().startActivity(browserIntent);
                            })
                            .icon(R.drawable.msg_warning)
                            .title(LocaleController.formatString("ContributeOcto", R.string.ContributeOcto))
                            .description(LocaleController.formatString("ContributeOcto_Desc", R.string.ContributeOcto_Desc))
                            .build());
                    category.row(new TextDetailRow.TextDetailRowBuilder()
                            .onClick(() -> {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://crowdin.com/project/octogram"));
                                fragment.getParentActivity().startActivity(browserIntent);
                            })
                            .icon(R.drawable.msg_translate)
                            .title(LocaleController.formatString("TranslateOcto", R.string.TranslateOcto))
                            .description(LocaleController.formatString("TranslateOcto_Desc", R.string.TranslateOcto_Desc))
                            .build());
                })
                .row(new FooterRow.FooterRowBuilder().title(footer).build())
                .build();
    }
}
