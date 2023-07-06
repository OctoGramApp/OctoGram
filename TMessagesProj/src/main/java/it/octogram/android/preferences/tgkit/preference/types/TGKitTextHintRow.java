package it.octogram.android.preferences.tgkit.preference.types;

import it.octogram.android.preferences.tgkit.preference.TGKitPreference;

public class TGKitTextHintRow extends TGKitPreference {
    public boolean divider;

    public TGKitTextHintRow(boolean divider) {
        this.divider = divider;
    }

    @Override
    public TGPType getType() {
        return TGPType.HINT;
    }
}
