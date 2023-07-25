package it.octogram.android.preferences.rows.impl;

import android.app.Activity;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.util.Consumer;

import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Cells.TextCell;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

import it.octogram.android.config.ConfigProperty;
import it.octogram.android.preferences.PreferenceType;
import it.octogram.android.preferences.rows.BaseRow;
import it.octogram.android.preferences.rows.BaseRowBuilder;
import it.octogram.android.preferences.rows.Clickable;

public class TextIconRow extends BaseRow implements Clickable {

    private int icon = -1;
    private final Runnable onClick;
    private final String value;

    private TextIconRow(String title, ConfigProperty<Boolean> hidden, boolean divider, int icon, @Nullable String value, Runnable onClick) {
        super(title, null, false, hidden, divider, PreferenceType.TEXT_ICON);
        this.icon = icon;
        this.onClick = onClick;
        this.value = value;
    }

    public void bindCell(TextCell cell) {
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

    @Override
    public void onClick(BaseFragment fragment, Activity activity, View view, int position, float x, float y) {
        if (onClick != null)
            onClick.run();
    }

    public static class TextIconRowBuilder extends BaseRowBuilder<TextIconRow> {
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
            return new TextIconRow(title, showIf, divider, icon, value, onClick);
        }
    }

}
