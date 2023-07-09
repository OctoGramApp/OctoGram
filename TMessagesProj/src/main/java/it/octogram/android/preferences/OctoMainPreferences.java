/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */
package it.octogram.android.preferences;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;

import java.util.ArrayList;

import it.octogram.android.preferences.tgkit.preference.OctoPreferences;
import it.octogram.android.preferences.tgkit.preference.types.TGKitFooterRow;
import it.octogram.android.preferences.tgkit.preference.types.TGKitTextDetailRow;
import it.octogram.android.preferences.tgkit.preference.types.TGKitTextIconRow;

public class OctoMainPreferences implements BasePreferencesEntry {

    @Override
    public OctoPreferences getPreferences(Context context) {
        String footer = "OctoGram v" + BuildConfig.BUILD_VERSION_STRING + ". Thank you for your interest in the project :)";
        return OctoPreferences.builder("OctoGram Settings")
                .sticker(context, "UtyaDuck", 31, true, "Welcome to the OctoGram Settings! Here you can customize your experience with the app.")
                .category("Settings", new ArrayList<>() {
                    {
                        add(new TGKitTextIconRow("General", true, R.drawable.msg_media, bf1 -> {
                            bf1.presentFragment(PreferencesNavigator.navigateToGeneralPreferences(context));
                        }));
                        add(new TGKitTextIconRow("Translator", true, R.drawable.msg_translate, bf1 -> {
                            bf1.presentFragment(PreferencesNavigator.navigateToTranslatorPreferences(context));
                        }));
                        add(new TGKitTextIconRow("Appearance", true, R.drawable.msg_colors, null));
                        add(new TGKitTextIconRow("Updates", true, R.drawable.msg_photo_rotate, null));
                        add(new TGKitTextIconRow("Experiments", true, R.drawable.msg_colors, null));
                    }
                })
                .category("Info", new ArrayList<>() {
                    {
                        add(new TGKitTextDetailRow("Official Channel", "Stay informed about new updates", true, R.drawable.msg_channel, bf1 -> {
                            MessagesController.getInstance(bf1.getCurrentAccount()).openByUserName("OctoGramApp", bf1, 1);
                        }));
                        add(new TGKitTextDetailRow("Contribute to OctoGram", "Help us creating a better app if you are a developer", true, R.drawable.msg_warning, bf1 -> {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/OctoGramApp/OctoGram"));
                            bf1.getParentActivity().startActivity(browserIntent);
                        }));
                        add(new TGKitTextDetailRow("Translate OctoGram", "Help us translating OctoGram in your language", true, R.drawable.msg_translate, bf1 -> {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://crowdin.com/project/octogram"));
                            bf1.getParentActivity().startActivity(browserIntent);
                        }));
                    }
                })
                .add(new TGKitFooterRow(footer, null))
                .build();
    }
}
