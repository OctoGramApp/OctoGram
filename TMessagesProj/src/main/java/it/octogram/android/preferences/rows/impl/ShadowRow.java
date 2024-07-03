package it.octogram.android.preferences.rows.impl;

import it.octogram.android.ConfigProperty;
import it.octogram.android.preferences.PreferenceType;
import it.octogram.android.preferences.rows.BaseRow;

public class ShadowRow extends BaseRow {

    public ShadowRow() {
        super(PreferenceType.SHADOW);
    }

    public ShadowRow(ConfigProperty<Boolean> showIf) {
        super(PreferenceType.SHADOW, showIf);
    }

}
