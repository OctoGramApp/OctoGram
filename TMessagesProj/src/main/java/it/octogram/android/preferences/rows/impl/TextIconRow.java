/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

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

    private final int icon;
    private final Runnable onClick;
    private final ConfigProperty<Boolean> preference;
    private final String value;
    private final String newID;
    private boolean wasNewBadgeVisible = false;

    private TextIconRow(CharSequence title,
                        ConfigProperty<Boolean> showIf,
                        boolean showIfReverse,
                        boolean divider,
                        int icon,
                        @Nullable String value,
                        boolean requiresRestart,
                        ConfigProperty<Boolean> preferenceValue,
                        Runnable onClick,
                        String newID) {
        super(title, null, requiresRestart, showIf, showIfReverse, divider, PreferenceType.TEXT_ICON);
        this.icon = icon;
        this.onClick = onClick;
        this.preference = preferenceValue;
        this.value = value;
        this.newID = newID;
    }

    public void bindCell(TextCell cell) {
        if (preference != null) {
            if (icon != -1) {
                cell.setTextAndCheckAndIcon(rebindTitle(), preference.getValue(), icon, hasDivider());
            } else {
                cell.setTextAndCheck(rebindTitle(), preference.getValue(), hasDivider());
            }
        } else {
            if (icon != -1 && value != null) {
                cell.setTextAndValueAndIcon(rebindTitle(), value, icon, hasDivider());
            } else if (value != null) {
                cell.setTextAndValue(rebindTitle(), value, hasDivider());
            } else if (icon != -1) {
                cell.setTextAndIcon(rebindTitle(), icon, hasDivider());
            } else {
                cell.setText(rebindTitle(), hasDivider());
            }
        }
    }

    public ConfigProperty<Boolean> getPreference() {
        return preference;
    }

    @Override
    public boolean onClick(BaseFragment fragment, Activity activity, View view, int position, float x, float y) {
        if (onClick != null) {
            onClick.run();
        }

        if (newID != null && OctoConfig.INSTANCE.isNewIdVisible(newID)) {
            OctoConfig.INSTANCE.hideNewId(newID);

            if (wasNewBadgeVisible && view instanceof TextCell vt) {
                bindCell(vt);
            }

            wasNewBadgeVisible = false;
        }

        if (preference == null) {
            return false;
        }

        preference.updateValue(!preference.getValue());
        FrameLayout cell = (FrameLayout) view;
        TextCell textCell = (TextCell) cell.getChildAt(0);
        textCell.setChecked(preference.getValue());
        return true;
    }

    private CharSequence rebindTitle() {
        CharSequence title = getTitle();
        wasNewBadgeVisible = false;

        if (newID != null && OctoConfig.INSTANCE.isNewIdVisible(newID)) {
            wasNewBadgeVisible = true;
            return TextCell.applyNewSpan(title);
        }

        return title;
    }

    public static class TextIconRowBuilder extends ToggleableBaseRowBuilder<TextIconRow, Boolean> {
        private Runnable onClick;
        private int icon = -1;
        private String value;
        private String newID = null;

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

        public TextIconRowBuilder isNew(String newID) {
            this.newID = newID;
            return this;
        }

        public TextIconRow build() {
            return new TextIconRow(title, showIf, showIfReverse, divider, icon, value, requiresRestart, preferenceValue, onClick, newID);
        }
    }

}
