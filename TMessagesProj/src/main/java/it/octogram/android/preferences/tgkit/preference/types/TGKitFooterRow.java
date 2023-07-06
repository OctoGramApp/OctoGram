package it.octogram.android.preferences.tgkit.preference.types;

import androidx.annotation.Nullable;

import org.telegram.ui.ActionBar.BaseFragment;

import it.octogram.android.preferences.tgkit.preference.TGKitPreference;

public class TGKitFooterRow extends TGKitPreference {

    public TGKitFooterRow(String text, @Nullable TGTIListener listener) {
        this.title = text;
        this.listener = listener;
    }

    @Nullable
    public TGTIListener listener;

    @Override
    public TGPType getType() {
        return TGPType.FOOTER;
    }

    public interface TGTIListener {
        void onClick(BaseFragment bf);
    }

}
