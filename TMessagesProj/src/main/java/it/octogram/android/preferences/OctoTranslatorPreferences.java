/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */

package it.octogram.android.preferences;

import android.content.Context;

import java.util.List;

import it.octogram.android.preferences.tgkit.preference.OctoPreferences;

public class OctoTranslatorPreferences implements BasePreferencesEntry {

    @Override
    public OctoPreferences getPreferences(Context context) {
        return OctoPreferences.builder("Translator Settings")
                .sticker(context, "GoldUtya", 29, true, "Welcome to the OctoGram Translator Settings! Here you can customize your translating experience.")
                .category("Translate messages", List.of(

                ))
                .build();
    }

}
