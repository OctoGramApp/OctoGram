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
