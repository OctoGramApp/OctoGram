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

public class SwitchRow extends BaseRow implements Clickable {

    private final ConfigProperty<Boolean> preferenceValue;

    private SwitchRow(@Nullable String title, @Nullable String summary, boolean requiresRestart, ConfigProperty<Boolean> showIf, boolean divider, ConfigProperty<Boolean> preferenceValue) {
        super(title, summary, requiresRestart, showIf, divider, PreferenceType.SWITCH);
        this.preferenceValue = preferenceValue;
    }

    public boolean getPreferenceValue() {
        return preferenceValue.getValue();
    }

    @Override
    public void onClick(BaseFragment fragment, Activity activity, View view, int position, float x, float y) {
        OctoConfig.INSTANCE.toggleBooleanSetting(preferenceValue);

        TextCheckCell cell = (TextCheckCell) view;
        cell.setChecked(preferenceValue.getValue());
    }

    public static class SwitchRowBuilder extends ToggleableBaseRowBuilder<SwitchRow, Boolean> {
        public SwitchRow build() {
            return new SwitchRow(title, description, requiresRestart, showIf, divider, preferenceValue);
        }
    }

}
