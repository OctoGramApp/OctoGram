/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui.components;

import java.util.ArrayList;

public class ExpandableRowIndex {
    private final SwitchCell mainCell;
    private final Runnable onSingleStateChange;
    private final ArrayList<SwitchCell> secondaryCell = new ArrayList<>();

    public ExpandableRowIndex(SwitchCell mainCell, Runnable onSingleStateChange) {
        this.mainCell = mainCell;
        this.onSingleStateChange = onSingleStateChange;
    }

    public Runnable getOnSingleStateChange() {
        return onSingleStateChange;
    }

    public ArrayList<SwitchCell> getSecondaryCell() {
        return secondaryCell;
    }

    public SwitchCell getMainCell() {
        return mainCell;
    }

    public void addSecondaryCell(SwitchCell cell) {
        if (secondaryCell.contains(cell)) {
            return;
        }

        secondaryCell.add(cell);
    }
}