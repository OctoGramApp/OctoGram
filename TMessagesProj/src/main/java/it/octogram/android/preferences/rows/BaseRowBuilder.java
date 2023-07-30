/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */

package it.octogram.android.preferences.rows;

import it.octogram.android.ConfigProperty;

public abstract class BaseRowBuilder<T> {

    protected String title;
    protected String description;
    protected boolean requiresRestart = false;
    protected ConfigProperty<Boolean> showIf;
    protected boolean divider = true;

    public BaseRowBuilder<T> title(String val) {
        title = val;
        return this;
    }

    public BaseRowBuilder<T> description(String val) {
        description = val;
        return this;
    }

    public BaseRowBuilder<T> requiresRestart(boolean val) {
        requiresRestart = val;
        return this;
    }

    public BaseRowBuilder<T> showIf(ConfigProperty<Boolean> val) {
        showIf = val;
        return this;
    }

    public BaseRowBuilder<T> divider(boolean val) {
        divider = val;
        return this;
    }

    public abstract T build();

}
