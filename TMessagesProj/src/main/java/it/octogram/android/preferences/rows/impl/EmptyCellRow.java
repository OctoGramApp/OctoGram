/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

package it.octogram.android.preferences.rows.impl;

import it.octogram.android.preferences.PreferenceType;
import it.octogram.android.preferences.rows.BaseRow;

public class EmptyCellRow extends BaseRow {
    public EmptyCellRow() {
        super(PreferenceType.EMPTY_CELL);
    }
}
