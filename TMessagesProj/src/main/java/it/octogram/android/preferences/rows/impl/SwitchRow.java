/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.rows.impl;

import android.app.Activity;
import android.view.View;

import androidx.annotation.Nullable;

import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Cells.TextCheckCell;

import java.util.function.Supplier;

import it.octogram.android.ConfigProperty;
import it.octogram.android.preferences.PreferenceType;
import it.octogram.android.preferences.rows.BaseRow;
import it.octogram.android.preferences.rows.Clickable;
import it.octogram.android.preferences.rows.ToggleableBaseRowBuilder;

public class SwitchRow extends BaseRow implements Clickable {

    private final ConfigProperty<Boolean> preferenceValue;
    private final Supplier<Boolean> supplierClickable;
    private final Runnable runnablePostUpdate;
    private final boolean locked;
    private final Runnable lockedAction;

    private SwitchRow(@Nullable CharSequence title, @Nullable String summary, boolean requiresRestart, ConfigProperty<Boolean> showIf, boolean showIfReverse, boolean divider, ConfigProperty<Boolean> preferenceValue, Supplier<Boolean> supplierClickable, Runnable runnablePostUpdate, boolean premium, boolean autoShowPremiumAlert, boolean locked, Runnable lockedAction, int... posts) {
        super(title, summary, requiresRestart, showIf, showIfReverse, divider, PreferenceType.SWITCH, premium, autoShowPremiumAlert, posts);
        this.preferenceValue = preferenceValue;
        this.supplierClickable = supplierClickable;
        this.runnablePostUpdate = runnablePostUpdate;
        this.locked = locked;
        this.lockedAction = lockedAction;
    }

    public boolean getPreferenceValue() {
        return preferenceValue.getValue();
    }

    public boolean isLocked() {
        return locked;
    }

    @Override
    public boolean onClick(BaseFragment fragment, Activity activity, View view, int position, float x, float y) {
        if (locked) {
            if (lockedAction != null) {
                lockedAction.run();
            }
            return false;
        }

        if (supplierClickable != null && !supplierClickable.get()) {
            return false;
        }

        preferenceValue.updateValue(!preferenceValue.getValue());

        if (runnablePostUpdate != null) {
            runnablePostUpdate.run();
        }

        TextCheckCell cell = (TextCheckCell) view;
        cell.setChecked(preferenceValue.getValue());
        return true;
    }

    public static class SwitchRowBuilder extends ToggleableBaseRowBuilder<SwitchRow, Boolean> {
        private Supplier<Boolean> supplierClickable;
        private Runnable runnablePostUpdate;
        private boolean locked;
        private Runnable lockedAction;

        public SwitchRowBuilder onClick(Supplier<Boolean> clickable) {
            this.supplierClickable = clickable;
            return this;
        }

        public SwitchRowBuilder onPostUpdate(Runnable runnable) {
            this.runnablePostUpdate = runnable;
            return this;
        }

        public SwitchRowBuilder setLocked(boolean locked) {
            this.locked = locked;
            return this;
        }

        public SwitchRowBuilder onLockedAction(Runnable lockedAction) {
            this.lockedAction = lockedAction;
            return this;
        }

        public SwitchRow build() {
            return new SwitchRow(title, description, requiresRestart, showIf, showIfReverse, divider, preferenceValue, supplierClickable, runnablePostUpdate, premium, autoShowPremiumAlert, locked, lockedAction, postNotificationName);
        }
    }
}