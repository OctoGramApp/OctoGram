/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */

package it.octogram.android.preferences.rows;

import it.octogram.android.ConfigProperty;

public abstract class ToggleableBaseRowBuilder<T, G> extends BaseRowBuilder<T> {

    protected ConfigProperty<G> preferenceValue;

    public ToggleableBaseRowBuilder<T, G> preferenceValue(ConfigProperty<G> val) {
        preferenceValue = val;
        return this;
    }
}
