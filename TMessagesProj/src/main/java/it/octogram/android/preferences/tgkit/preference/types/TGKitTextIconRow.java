package it.octogram.android.preferences.tgkit.preference.types;

import androidx.annotation.Nullable;

import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Cells.TextCell;

import it.octogram.android.preferences.tgkit.preference.TGKitPreference;

public class TGKitTextIconRow extends TGKitPreference {
    private boolean divider = false;
    private int icon = -1;

    public TGKitTextIconRow(String title, boolean divider, @Nullable TGTIListener listener) {
        this.title = title;
        this.divider = divider;
        this.listener = listener;
    }

    public TGKitTextIconRow(String title, boolean divider, int icon, @Nullable TGTIListener listener) {
        this.title = title;
        this.divider = divider;
        this.icon = icon;
        this.listener = listener;
    }

    @Nullable
    public String value = null;

    public TGKitTextIconRow setValue(String value) {
        this.value = value;
        return this;
    }

    @Nullable
    public TGTIListener listener;

    public void bindCell(TextCell cell) {
        if (icon != -1 && value != null) {
            cell.setTextAndValueAndIcon(title, value, icon, divider);
        } else if (value != null) {
            cell.setTextAndValue(title, value, divider);
        } else if (icon != -1) {
            cell.setTextAndIcon(title, icon, divider);
        } else {
            cell.setText(title, divider);
        }
    }

    @Override
    public TGPType getType() {
        return TGPType.TEXT_ICON;
    }

    public interface TGTIListener {
        void onClick(BaseFragment bf);
    }
}
