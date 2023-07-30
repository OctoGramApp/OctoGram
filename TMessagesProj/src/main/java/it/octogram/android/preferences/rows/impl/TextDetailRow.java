package it.octogram.android.preferences.rows.impl;

import android.app.Activity;
import android.view.View;

import androidx.annotation.Nullable;

import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Cells.TextDetailSettingsCell;

import it.octogram.android.ConfigProperty;
import it.octogram.android.preferences.PreferenceType;
import it.octogram.android.preferences.rows.BaseRow;
import it.octogram.android.preferences.rows.Clickable;
import it.octogram.android.preferences.rows.BaseRowBuilder;

public class TextDetailRow extends BaseRow implements Clickable {

    private int icon = -1;
    private final Runnable onClick;

    private TextDetailRow(@Nullable String title, @Nullable String summary, boolean requiresRestart, ConfigProperty<Boolean> showIf, boolean divider, int icon, Runnable onClick) {
        super(title, summary, requiresRestart, showIf, divider, PreferenceType.TEXT_DETAIL);
        this.icon = icon;
        this.onClick = onClick;
    }

    public void bindCell(TextDetailSettingsCell cell) {
        if (icon != -1) {
            cell.setTextAndValueAndIcon(getTitle(), getSummary(), icon, hasDivider());
        } else {
            cell.setTextAndValue(getTitle(), getSummary(), hasDivider());
        }
    }

    @Override
    public void onClick(BaseFragment fragment, Activity activity, View view, int position, float x, float y) {
        if (onClick != null)
            onClick.run();
    }

    public static class TextDetailRowBuilder extends BaseRowBuilder<TextDetailRow> {
        private Runnable onClick;
        private int icon = -1;

        public TextDetailRowBuilder onClick(Runnable onClick) {
            this.onClick = onClick;
            return this;
        }

        public TextDetailRowBuilder icon(int icon) {
            this.icon = icon;
            return this;
        }

        public TextDetailRow build() {
            return new TextDetailRow(title, description, requiresRestart, showIf, divider, icon, onClick);
        }
    }
}
