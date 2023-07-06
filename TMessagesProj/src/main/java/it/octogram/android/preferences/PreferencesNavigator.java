package it.octogram.android.preferences;

import it.octogram.android.preferences.tgkit.TGKitSettingsFragment;

public class PreferencesNavigator {

    public static TGKitSettingsFragment navigateToPreferences() {
        return new TGKitSettingsFragment(new TestPreferences());
    }

}
