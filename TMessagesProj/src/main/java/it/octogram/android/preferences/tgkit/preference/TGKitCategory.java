/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */
package it.octogram.android.preferences.tgkit.preference;

import java.util.List;

public class TGKitCategory {
    public String name;
    public List<TGKitPreference> preferences;

    public TGKitCategory(String name, List<TGKitPreference> preferences) {
        this.name = name;
        this.preferences = preferences;
    }

    public List<TGKitPreference> getPreferences() {
        return preferences;
    }
}
