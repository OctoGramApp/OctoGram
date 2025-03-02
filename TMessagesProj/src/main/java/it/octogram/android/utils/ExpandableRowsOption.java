/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils;

import java.util.function.Supplier;

import it.octogram.android.ConfigProperty;

public class ExpandableRowsOption {
    public ConfigProperty<Boolean> property;
    public String optionTitle;
    public Supplier<Boolean> onClick;
    public Runnable onPostUpdate;
    public int accountId = -1;

    public ExpandableRowsOption property(ConfigProperty<Boolean> property) {
        this.property = property;
        return this;
    }

    public ExpandableRowsOption optionTitle(String optionTitle) {
        this.optionTitle = optionTitle;
        return this;
    }

    public ExpandableRowsOption accountId(int accountId) {
        this.accountId = accountId;
        return this;
    }

    public ExpandableRowsOption onClick(Supplier<Boolean> onClick) {
        this.onClick = onClick;
        return this;
    }

    public ExpandableRowsOption onPostUpdate(Runnable onPostUpdate) {
        this.onPostUpdate = onPostUpdate;
        return this;
    }

    public boolean hasAccount() {
        return accountId != -1;
    }
}
