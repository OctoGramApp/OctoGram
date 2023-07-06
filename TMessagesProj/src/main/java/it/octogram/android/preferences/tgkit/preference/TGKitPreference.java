package it.octogram.android.preferences.tgkit.preference;

import it.octogram.android.preferences.tgkit.preference.types.TGPType;

abstract public class TGKitPreference {
    public String title;

    abstract public TGPType getType();
}
