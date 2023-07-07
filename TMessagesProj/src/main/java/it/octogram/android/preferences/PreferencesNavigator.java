/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */
package it.octogram.android.preferences;

import it.octogram.android.preferences.tgkit.TGKitSettingsFragment;

public class PreferencesNavigator {

    public static TGKitSettingsFragment navigateToPreferences() {
        return new TGKitSettingsFragment(new TestPreferences());
    }

}
