package it.octogram.android.preferences.tgkit.preference.types;

import androidx.annotation.Nullable;

import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;

import it.octogram.android.preferences.tgkit.preference.TGKitPreference;

public class TGKitTextDetailRow extends TGKitPreference {
    public String detail;
    public boolean divider;
    private int icon = -1;

    public TGKitTextDetailRow(String title, String detail, boolean divider, @Nullable TGTIListener listener) {
        this.title = title;
        this.detail = detail;
        this.divider = divider;
        this.listener = listener;
    }

    public TGKitTextDetailRow(String title, String detail, boolean divider, int icon, @Nullable TGTIListener listener) {
        this.title = title;
        this.detail = detail;
        this.divider = divider;
        this.icon = icon;
        this.listener = listener;
    }

    @Nullable
    public TGTIListener listener;

    @Override
    public TGPType getType() {
        return TGPType.TEXT_DETAIL;
    }

    public void bindCell(TextDetailSettingsCell cell) {
        if (icon != -1) {
            cell.setTextAndValueAndIcon(title, detail, icon, divider);
        } else {
            cell.setTextAndValue(title, detail, divider);
        }
    }

    public void setDivider(boolean divider) {
        this.divider = divider;
    }

    public interface TGTIListener {
        void onClick(BaseFragment bf);
    }


}
