/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.rows.impl;

import it.octogram.android.ConfigProperty;
import it.octogram.android.app.PreferenceType;
import it.octogram.android.app.rows.BaseRow;

public class ShadowRow extends BaseRow {
    public ShadowRow() {
        super(PreferenceType.SHADOW);
    }

    public ShadowRow(ConfigProperty<Boolean> showIf) {
        super(PreferenceType.SHADOW, showIf);
    }

    public ShadowRow(ConfigProperty<Boolean> showIf, boolean showIfReverse) {
        super(PreferenceType.SHADOW, showIf, showIfReverse);
    }
}
