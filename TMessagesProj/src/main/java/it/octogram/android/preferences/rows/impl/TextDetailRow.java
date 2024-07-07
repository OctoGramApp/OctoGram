package it.octogram.android.preferences.rows.impl;

import android.app.Activity;
import android.view.View;

import androidx.annotation.Nullable;

import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;

import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.preferences.PreferenceType;
import it.octogram.android.preferences.rows.BaseRow;
import it.octogram.android.preferences.rows.BaseRowBuilder;
import it.octogram.android.preferences.rows.Clickable;

public class TextDetailRow extends BaseRow implements Clickable {

    private final int icon;
    private final Runnable onClick;
    private final String newID;
    private boolean wasNewBadgeVisible = false;

    private TextDetailRow(@Nullable String title, @Nullable String summary, boolean requiresRestart, ConfigProperty<Boolean> showIf, boolean showIfReverse, boolean divider, int icon, Runnable onClick, String newID) {
        super(title, summary, requiresRestart, showIf, showIfReverse, divider, PreferenceType.TEXT_DETAIL);
        this.icon = icon;
        this.onClick = onClick;
        this.newID = newID;
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
        String title = getTitle();
        wasNewBadgeVisible = false;

        if (newID != null && OctoConfig.INSTANCE.isNewIdVisible(newID)) {
            wasNewBadgeVisible = true;
            return TextCell.applyNewSpan(title);
        }

        return title;
    }

    public static class TextDetailRowBuilder extends BaseRowBuilder<TextDetailRow> {
        private Runnable onClick;
        private int icon = -1;
        private String newID = null;

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

        public TextDetailRow build() {
            return new TextDetailRow(title, description, requiresRestart, showIf, showIfReverse, divider, icon, onClick, newID);
        }
    }
}
