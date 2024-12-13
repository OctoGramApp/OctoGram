/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

package it.octogram.android.preferences.rows.cells;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;

public class StickerCell extends LinearLayout {
    public StickerCell(Context context) {
        super(context);
        setGravity(Gravity.CENTER_HORIZONTAL);
        setOrientation(VERTICAL);
    }
}
