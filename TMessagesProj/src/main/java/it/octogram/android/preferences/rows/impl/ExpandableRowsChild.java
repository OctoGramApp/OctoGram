/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.rows.impl;

import android.app.Activity;
import android.view.View;

import org.telegram.ui.ActionBar.BaseFragment;

import it.octogram.android.preferences.PreferenceType;
import it.octogram.android.preferences.rows.BaseRow;
import it.octogram.android.preferences.rows.Clickable;
import it.octogram.android.utils.ExpandableRowsOption;

public class ExpandableRowsChild extends BaseRow implements Clickable {
    private final ExpandableRowsOption item;
    private final int refersToId;

    public ExpandableRowsChild(ExpandableRowsOption item, int refersToId) {
        super(PreferenceType.EXPANDABLE_ROWS_CHILD);
        this.item = item;
        this.refersToId = refersToId;
    }

    public ExpandableRowsOption getItem() {
        return item;
    }

    public int getRefersToId() {
        return refersToId;
    }

    @Override
    public boolean onClick(BaseFragment fragment, Activity activity, View view, int position, float x, float y) {
        return false;
    }
}
