/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.rows;

import it.octogram.android.ConfigProperty;

public abstract class ToggleableBaseRowBuilder<T, G> extends BaseRowBuilder<T> {

    protected ConfigProperty<G> preferenceValue;
    protected boolean premium;
    protected boolean autoShowPremiumAlert = true;

    public ToggleableBaseRowBuilder<T, G> preferenceValue(ConfigProperty<G> val) {
        preferenceValue = val;
        return this;
    }

    public ToggleableBaseRowBuilder<T, G> premium(boolean val) {
        premium = val;
        return this;
    }

    public ToggleableBaseRowBuilder<T, G> autoShowPremiumAlert(boolean val) {
        autoShowPremiumAlert = val;
        return this;
    }
}
