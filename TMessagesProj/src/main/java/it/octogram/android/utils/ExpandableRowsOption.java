/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

package it.octogram.android.utils;

import it.octogram.android.ConfigProperty;

public class ExpandableRowsOption {
    public ConfigProperty<Boolean> property;
    public String optionTitle;
    public Runnable onClick;

    public ExpandableRowsOption setProperty(ConfigProperty<Boolean> property) {
        this.property = property;
        return this;
    }

    public ExpandableRowsOption setOptionTitle(String optionTitle) {
        this.optionTitle = optionTitle;
        return this;
    }

    public ExpandableRowsOption setOnClick(Runnable onClick) {
        this.onClick = onClick;
        return this;
    }
}
