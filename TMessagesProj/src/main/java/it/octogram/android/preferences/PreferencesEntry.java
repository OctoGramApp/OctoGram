/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

package it.octogram.android.preferences;

import android.content.Context;

import it.octogram.android.preferences.fragment.PreferencesFragment;

public interface PreferencesEntry {

    OctoPreferences getPreferences(PreferencesFragment fragment, Context context);

}
