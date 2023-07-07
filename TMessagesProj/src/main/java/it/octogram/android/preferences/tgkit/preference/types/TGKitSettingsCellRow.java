/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */
package it.octogram.android.preferences.tgkit.preference.types;

import androidx.annotation.Nullable;

import org.telegram.ui.ActionBar.Theme;

import it.octogram.android.preferences.tgkit.preference.TGKitPreference;

public class TGKitSettingsCellRow extends TGKitPreference {
    public boolean divider = false;
    public int textColor = Theme.getColor(Theme.key_windowBackgroundWhiteBlackText);

    @Nullable
    public TGKitTextIconRow.TGTIListener listener;

    @Override
    public TGPType getType() {
        return TGPType.TEXT_ICON;
    }

    public void setDivider(boolean divider) {
        this.divider = divider;
    }
}
