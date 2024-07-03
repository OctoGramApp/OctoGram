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

public class SwitchRowCollapsible extends BaseRow implements Clickable {

    private final ConfigProperty<Boolean> preferenceValue;
    private final Supplier<Boolean> supplierClickable;
    private final Runnable runnablePostUpdate;
    private final boolean isCollapsable;

    private SwitchRowCollapsible(@Nullable String title, @Nullable String summary, boolean requiresRestart, ConfigProperty<Boolean> showIf, boolean showIfReverse, boolean divider, ConfigProperty<Boolean> preferenceValue, Supplier<Boolean> supplierClickable, Runnable runnablePostUpdate, boolean premium, boolean autoShowPremiumAlert, boolean isCollapsable, int... posts) {
        super(title, summary, requiresRestart, showIf, showIfReverse, divider, PreferenceType.SWITCH, premium, autoShowPremiumAlert, posts);
        this.preferenceValue = preferenceValue;
        this.supplierClickable = supplierClickable;
        this.runnablePostUpdate = runnablePostUpdate;
        this.isCollapsable = isCollapsable;
    }

    public boolean getPreferenceValue() {
        return preferenceValue.getValue();
    }

    public boolean isCollapsable() {
        return isCollapsable;
    }

    @Override
    public boolean onClick(BaseFragment fragment, Activity activity, View view, int position, float x, float y) {
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

    public static class SwitchRowBuilder extends ToggleableBaseRowBuilder<SwitchRowCollapsible, Boolean> {
        private Supplier<Boolean> supplierClickable;
        private Runnable runnablePostUpdate;
        private boolean isCollapsable;

        public SwitchRowBuilder onClick(Supplier<Boolean> clickable) {
            this.supplierClickable = clickable;
            return this;
        }

        public SwitchRowBuilder onPostUpdate(Runnable runnable) {
            this.runnablePostUpdate = runnable;
            return this;
        }

        public SwitchRowBuilder setCollapsable(boolean isCollapsable) {
            this.isCollapsable = isCollapsable;
            return this;
        }

        public SwitchRowCollapsible build() {
            return new SwitchRowCollapsible(title, description, requiresRestart, showIf, showIfReverse, divider, preferenceValue, supplierClickable, runnablePostUpdate, premium, autoShowPremiumAlert, isCollapsable, postNotificationName);
        }
    }

}
