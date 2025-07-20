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
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;

import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.app.PreferenceType;
import it.octogram.android.app.rows.BaseRow;
import it.octogram.android.app.rows.BaseRowBuilder;
import it.octogram.android.app.rows.Clickable;

public class TextDetailRow extends BaseRow implements Clickable {

    private final int icon;
    private final Runnable onClick;
    private final String newID;
    private final String propertySelectionTag;
    private boolean wasNewBadgeVisible = false;

    private TextDetailRow(@Nullable CharSequence title, @Nullable String summary, boolean requiresRestart, String propertySelectionTag, ConfigProperty<Boolean> showIf, boolean showIfReverse, boolean divider, int icon, Runnable onClick, String newID) {
        super(title, summary, requiresRestart, showIf, showIfReverse, divider, PreferenceType.TEXT_DETAIL);
        this.icon = icon;
        this.onClick = onClick;
        this.newID = newID;
        this.propertySelectionTag = propertySelectionTag;
    }

    public void bindCell(TextDetailSettingsCell cell) {
        if (icon != -1) {
            cell.setTextAndValueAndIcon(rebindTitle(), getSummary(), icon, hasDivider());
        } else {
            cell.setTextAndValue(rebindTitle(), getSummary(), hasDivider());
        }
    }

    @Override
    public boolean onClick(BaseFragment fragment, Activity activity, View view, int position, float x, float y) {
        if (onClick != null) {
            onClick.run();
        }

        if (newID != null && OctoConfig.INSTANCE.isNewIdVisible(newID)) {
            OctoConfig.INSTANCE.hideNewId(newID);

            if (wasNewBadgeVisible && view instanceof TextDetailSettingsCell vt) {
                bindCell(vt);
            }

            wasNewBadgeVisible = false;
        }

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

    public String getPropertySelectionTag() {
        return propertySelectionTag;
    }

    public static class TextDetailRowBuilder extends BaseRowBuilder<TextDetailRow> {
        private Runnable onClick;
        private int icon = -1;
        private String newID = null;
        private String propertySelectionTag = null;

        public TextDetailRowBuilder onClick(Runnable onClick) {
            this.onClick = onClick;
            return this;
        }

        public TextDetailRowBuilder icon(int icon) {
            this.icon = icon;
            return this;
        }

        public TextDetailRowBuilder isNew(String newID) {
            this.newID = newID;
            return this;
        }

        public TextDetailRowBuilder propertySelectionTag(String propertySelectionTag) {
            this.propertySelectionTag = propertySelectionTag;
            return this;
        }

        public TextDetailRow build() {
            return new TextDetailRow(title, description, requiresRestart, propertySelectionTag, showIf, showIfReverse, divider, icon, onClick, newID);
        }
    }
}
