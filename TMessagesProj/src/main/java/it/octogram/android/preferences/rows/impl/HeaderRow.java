package it.octogram.android.preferences.rows.impl;

import it.octogram.android.ConfigProperty;
import it.octogram.android.preferences.PreferenceType;
import it.octogram.android.preferences.rows.BaseRow;

public class HeaderRow extends BaseRow {

    public HeaderRow(String title) {
        super(title, PreferenceType.HEADER);
    }

    public HeaderRow(String title, ConfigProperty<Boolean> showIf) {
        super(title, null, false, showIf, PreferenceType.HEADER);
    }

}
