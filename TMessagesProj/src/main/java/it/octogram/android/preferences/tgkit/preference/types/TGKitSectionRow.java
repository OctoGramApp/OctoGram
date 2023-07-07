/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */
package it.octogram.android.preferences.tgkit.preference.types;

import it.octogram.android.preferences.tgkit.preference.TGKitPreference;

public class TGKitSectionRow extends TGKitPreference {
    @Override
    public TGPType getType() {
        return TGPType.SECTION;
    }
}
