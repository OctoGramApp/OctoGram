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
    private final String propertySelectionTag;
    private String value;
    private final String newID;
    private boolean wasNewBadgeVisible = false;
    private final OnDynamicDataUpdate dynamicDataUpdate;
    private final boolean isBlue;

    public interface OnDynamicDataUpdate {
        String getTitle();
        String getValue();
    }

    private TextIconRow(CharSequence title,
                        ConfigProperty<Boolean> showIf,
                        boolean showIfReverse,
                        boolean divider,
                        int icon,
                        @Nullable String value,
                        boolean requiresRestart,
                        ConfigProperty<Boolean> preferenceValue,
                        String propertySelectionTag,
                        Runnable onClick,
                        String newID,
                        OnDynamicDataUpdate dynamicDataUpdate,
                        boolean isBlue) {
        super(title, null, requiresRestart, showIf, showIfReverse, divider, PreferenceType.TEXT_ICON);
        this.icon = icon;
        this.onClick = onClick;
        this.preference = preferenceValue;
        this.propertySelectionTag = propertySelectionTag;
        this.value = value;
        this.newID = newID;
        this.dynamicDataUpdate = dynamicDataUpdate;
        this.isBlue = isBlue;
    }

    public void bindCell(TextCell cell) {
        if (dynamicDataUpdate != null) {
            setTitle(dynamicDataUpdate.getTitle());
            value = dynamicDataUpdate.getValue();
        }

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

    public boolean isBlue() {
        return isBlue;
    }

    public ConfigProperty<Boolean> getPreference() {
        return preference;
    }

    public String getPropertySelectionTag() {
        return propertySelectionTag;
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
        private OnDynamicDataUpdate dynamicDataUpdate;
        private String propertySelectionTag;
        private boolean isBlue;

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

        public TextIconRowBuilder propertySelectionTag(String propertySelectionTag) {
            this.propertySelectionTag = propertySelectionTag;
            return this;
        }

        public TextIconRowBuilder setDynamicDataUpdate(OnDynamicDataUpdate dynamicDataUpdate) {
            this.dynamicDataUpdate = dynamicDataUpdate;
            return this;
        }

        public TextIconRowBuilder isBlue(boolean isBlue) {
            this.isBlue = isBlue;
            return this;
        }

        public TextIconRow build() {
            return new TextIconRow(title, showIf, showIfReverse, divider, icon, value, requiresRestart, preferenceValue, propertySelectionTag, onClick, newID, dynamicDataUpdate, isBlue);
        }
    }

}
