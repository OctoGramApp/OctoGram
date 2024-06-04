package it.octogram.android.preferences.rows.impl;

import android.app.Activity;
import android.view.View;

import androidx.annotation.Nullable;

import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Cells.CheckBoxCell;

import java.util.function.Supplier;

import it.octogram.android.ConfigProperty;
import it.octogram.android.preferences.PreferenceType;
import it.octogram.android.preferences.rows.BaseRow;
import it.octogram.android.preferences.rows.Clickable;
import it.octogram.android.preferences.rows.ToggleableBaseRowBuilder;

public class CheckboxRow extends BaseRow implements Clickable {

    private final ConfigProperty<Boolean> preferenceValue;
    private final Supplier<Boolean> supplierClickable;

    private CheckboxRow(@Nullable String title, @Nullable String summary, boolean requiresRestart, ConfigProperty<Boolean> showIf, boolean showIfReverse, boolean divider, ConfigProperty<Boolean> preferenceValue, Supplier<Boolean> supplierClickable, boolean premium, int... posts) {
        super(title, summary, requiresRestart, showIf, showIfReverse, divider, PreferenceType.CHECKBOX, premium, posts);
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
        preferenceValue.updateValue(!preferenceValue.getValue());

        CheckBoxCell cell = (CheckBoxCell) view;
        cell.setChecked(preferenceValue.getValue(), true);
        return true;
    }

    public static class CheckboxRowBuilder extends ToggleableBaseRowBuilder<CheckboxRow, Boolean> {
        private Supplier<Boolean> supplierClickable;

        public CheckboxRowBuilder onClick(Supplier<Boolean> clickable) {
            this.supplierClickable = clickable;
            return this;
        }

        public CheckboxRow build() {
            return new CheckboxRow(title, description, requiresRestart, showIf, showIfReverse, divider, preferenceValue, supplierClickable, premium, postNotificationName);
        }
    }

}
