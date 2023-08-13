package it.octogram.android.preferences.rows.impl;

import android.app.Activity;
import android.view.View;

import androidx.annotation.Nullable;

import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Cells.TextCheckCell;

import it.octogram.android.OctoConfig;
import it.octogram.android.ConfigProperty;
import it.octogram.android.preferences.PreferenceType;
import it.octogram.android.preferences.rows.BaseRow;
import it.octogram.android.preferences.rows.Clickable;
import it.octogram.android.preferences.rows.ToggleableBaseRowBuilder;

import java.util.function.Supplier;

public class SwitchRow extends BaseRow implements Clickable {

    private final ConfigProperty<Boolean> preferenceValue;
    private final Supplier<Boolean> supplierClickable;

    private SwitchRow(@Nullable String title, @Nullable String summary, boolean requiresRestart, ConfigProperty<Boolean> showIf, boolean divider, ConfigProperty<Boolean> preferenceValue, Supplier<Boolean> supplierClickable, int... posts) {
        super(title, summary, requiresRestart, showIf, divider, PreferenceType.SWITCH, posts);
        this.preferenceValue = preferenceValue;
        this.supplierClickable = supplierClickable;
    }

    public boolean getPreferenceValue() {
        return preferenceValue.getValue();
    }

    @Override
    public boolean onClick(BaseFragment fragment, Activity activity, View view, int position, float x, float y) {
        if (supplierClickable != null && !supplierClickable.get()) {
            return false;
        }
        OctoConfig.INSTANCE.toggleBooleanSetting(preferenceValue);

        TextCheckCell cell = (TextCheckCell) view;
        cell.setChecked(preferenceValue.getValue());
        return true;
    }

    public static class SwitchRowBuilder extends ToggleableBaseRowBuilder<SwitchRow, Boolean> {
        private Supplier<Boolean> supplierClickable;

        public SwitchRowBuilder onClick(Supplier<Boolean> clickable) {
            this.supplierClickable = clickable;
            return this;
        }

        public SwitchRow build() {
            return new SwitchRow(title, description, requiresRestart, showIf, divider, preferenceValue, supplierClickable, postNotificationName);
        }
    }

}
