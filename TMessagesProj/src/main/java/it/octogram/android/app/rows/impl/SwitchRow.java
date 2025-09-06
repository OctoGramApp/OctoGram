/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.rows.impl;

import android.app.Activity;
import android.view.View;

import androidx.annotation.Nullable;

import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Cells.TextCheckCell;

import java.util.function.Supplier;

import it.octogram.android.ConfigProperty;
import it.octogram.android.app.PreferenceType;
import it.octogram.android.app.rows.BaseRow;
import it.octogram.android.app.rows.Clickable;
import it.octogram.android.app.rows.ToggleableBaseRowBuilder;

public class SwitchRow extends BaseRow implements Clickable {

    private final ConfigProperty<Boolean> preferenceValue;
    private final Supplier<Boolean> supplierClickable;
    private final Runnable runnablePostUpdate;
    private final boolean locked;
    private final Runnable lockedAction;
    private final boolean isMainPageAction;

    private CachedState cachedState;

    private SwitchRow(@Nullable CharSequence title, @Nullable String summary, boolean requiresRestart, ConfigProperty<Boolean> showIf, boolean showIfReverse, boolean divider, ConfigProperty<Boolean> preferenceValue, Supplier<Boolean> supplierClickable, Runnable runnablePostUpdate, boolean premium, boolean autoShowPremiumAlert, boolean locked, Runnable lockedAction, boolean isMainPageAction, int... posts) {
        super(title, summary, requiresRestart, showIf, showIfReverse, divider, isMainPageAction ? PreferenceType.MAIN_PAGE_SWITCH : PreferenceType.SWITCH, premium, autoShowPremiumAlert, posts);
        this.preferenceValue = preferenceValue;
        this.supplierClickable = supplierClickable;
        this.runnablePostUpdate = runnablePostUpdate;
        this.locked = locked;
        this.lockedAction = lockedAction;
        this.isMainPageAction = isMainPageAction;
    }

    public ConfigProperty<Boolean> getPreferenceValueConfig() {
        return preferenceValue;
    }

    public boolean getPreferenceValue() {
        return preferenceValue.getValue();
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isMainPageAction() {
        return isMainPageAction;
    }

    public CachedState getCachedState() {
        if (cachedState == null) {
            cachedState = new CachedState();
        }
        return cachedState;
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
        private boolean isMainPageAction;

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

        public SwitchRowBuilder isMainPageAction(boolean isMainPageAction) {
            this.isMainPageAction = isMainPageAction;
            return this;
        }

        public SwitchRow build() {
            return new SwitchRow(title, description, requiresRestart, showIf, showIfReverse, divider, preferenceValue, supplierClickable, runnablePostUpdate, premium, autoShowPremiumAlert, locked, lockedAction, isMainPageAction, postNotificationName);
        }
    }

    public class CachedState {
        private boolean hasInitData = false;
        private boolean lastState;

        public boolean isHasInitData() {
            return hasInitData;
        }

        public void setHasInitData(boolean hasInitData) {
            this.hasInitData = hasInitData;
        }

        public boolean getLastState() {
            return lastState;
        }

        public void setLastState(boolean lastState) {
            this.lastState = lastState;
        }
    }
}