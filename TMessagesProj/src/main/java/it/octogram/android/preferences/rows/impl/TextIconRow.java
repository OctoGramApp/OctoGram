package it.octogram.android.preferences.rows.impl;

import android.app.Activity;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Cells.TextCell;

import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.preferences.PreferenceType;
import it.octogram.android.preferences.rows.BaseRow;
import it.octogram.android.preferences.rows.Clickable;
import it.octogram.android.preferences.rows.ToggleableBaseRowBuilder;

public class TextIconRow extends BaseRow implements Clickable {

    private int icon = -1;
    private final Runnable onClick;
    private final ConfigProperty<Boolean> preference;
    private final String value;

    private TextIconRow(String title,
                        ConfigProperty<Boolean> hidden,
                        boolean divider,
                        int icon,
                        @Nullable String value,
                        boolean requiresRestart,
                        ConfigProperty<Boolean> preferenceValue,
                        Runnable onClick) {
        super(title, null, requiresRestart, hidden, divider, PreferenceType.TEXT_ICON);
        this.icon = icon;
        this.onClick = onClick;
        this.preference = preferenceValue;
        this.value = value;
    }

    public void bindCell(TextCell cell) {
        if (preference != null) {
            if (icon != -1) {
                cell.setTextAndCheckAndIcon(getTitle(), preference.getValue(), icon, hasDivider());
            } else {
                cell.setTextAndCheck(getTitle(), preference.getValue(), hasDivider());
            }
        } else {
            if (icon != -1 && value != null) {
                cell.setTextAndValueAndIcon(getTitle(), value, icon, hasDivider());
            } else if (value != null) {
                cell.setTextAndValue(getTitle(), value, hasDivider());
            } else if (icon != -1) {
                cell.setTextAndIcon(getTitle(), icon, hasDivider());
            } else {
                cell.setText(getTitle(), hasDivider());
            }
        }
    }

    public ConfigProperty<Boolean> getPreference() {
        return preference;
    }

    @Override
    public boolean onClick(BaseFragment fragment, Activity activity, View view, int position, float x, float y) {
        if (onClick != null)
            onClick.run();

        if (preference == null)
            return false;

        OctoConfig.INSTANCE.toggleBooleanSetting(preference);
        FrameLayout cell = (FrameLayout) view;
        TextCell textCell = (TextCell) cell.getChildAt(0);
        textCell.setChecked(preference.getValue());
        return true;
    }

    public static class TextIconRowBuilder extends ToggleableBaseRowBuilder<TextIconRow, Boolean> {
        private Runnable onClick;
        private int icon = -1;
        private String value;

        public TextIconRowBuilder onClick(Runnable onClick) {
            this.onClick = onClick;
            return this;
        }

        public TextIconRowBuilder icon(int icon) {
            this.icon = icon;
            return this;
        }

        public TextIconRowBuilder value(String value) {
            this.value = value;
            return this;
        }

        public TextIconRow build() {
            return new TextIconRow(title, showIf, divider, icon, value, requiresRestart, preferenceValue, onClick);
        }
    }

}
