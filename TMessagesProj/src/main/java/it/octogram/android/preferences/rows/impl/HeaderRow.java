package it.octogram.android.preferences.rows.impl;

import it.octogram.android.ConfigProperty;
import it.octogram.android.preferences.PreferenceType;
import it.octogram.android.preferences.rows.BaseRow;

public class HeaderRow extends BaseRow {

    public HeaderRow(CharSequence title) {
        super(title, PreferenceType.HEADER);
    }

    public HeaderRow(CharSequence title, ConfigProperty<Boolean> showIf) {
        super(title, null, false, showIf, false, PreferenceType.HEADER);
    }

}
