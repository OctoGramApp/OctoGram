/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */
package it.octogram.android.preferences;

import org.telegram.ui.ActionBar.BaseFragment;

import java.util.List;

import it.octogram.android.preferences.tgkit.preference.OctoPreferences;
import it.octogram.android.preferences.tgkit.preference.TGKitCategory;
import it.octogram.android.preferences.tgkit.preference.TGKitPreference;
import it.octogram.android.preferences.tgkit.preference.types.TGKitListPreference;
import it.octogram.android.preferences.tgkit.preference.types.TGKitSettingsCellRow;
import it.octogram.android.preferences.tgkit.preference.types.TGKitSwitchPreference;
import it.octogram.android.preferences.tgkit.preference.types.TGKitTextDetailRow;

public interface BasePreferencesEntry {

    default OctoPreferences getProcessedPrefs(BaseFragment bf) {
        OctoPreferences preferences = getPreferences(bf);
        List<TGKitPreference> preferenceList = preferences.getPreferences();

        for (TGKitPreference preference : preferenceList) {
            int lastIndex = preferenceList.size() - 1;
            for (int index = 0; index < preferenceList.size(); index++) {
                boolean isNotLast = index != lastIndex;
                if (preference instanceof TGKitListPreference) {
                    ((TGKitListPreference) preference).setDivider(isNotLast);
                } else if (preference instanceof TGKitSettingsCellRow) {
                    ((TGKitSettingsCellRow) preference).setDivider(isNotLast);
                } else if (preference instanceof TGKitTextDetailRow) {
                    ((TGKitTextDetailRow) preference).setDivider(isNotLast);
                } else if (preference instanceof TGKitSwitchPreference) {
                    ((TGKitSwitchPreference) preference).setDivider(isNotLast);
                }
            }
        }

        return preferences;
    }

    OctoPreferences getPreferences(BaseFragment bf);

}
