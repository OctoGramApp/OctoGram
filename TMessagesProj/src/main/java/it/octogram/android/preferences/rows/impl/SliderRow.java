/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

package it.octogram.android.preferences.rows.impl;

import it.octogram.android.ConfigProperty;
import it.octogram.android.preferences.PreferenceType;
import it.octogram.android.preferences.rows.BaseRow;
import it.octogram.android.preferences.rows.ToggleableBaseRowBuilder;

public class SliderRow extends BaseRow {

    private final int min;
    private final int max;
    private final ConfigProperty<Integer> preferenceValue;
    private final Runnable runnable;

    protected SliderRow(int min, int max, ConfigProperty<Integer> preferenceValue, Runnable runnable, ConfigProperty<Boolean> showIf, boolean showIfReverse) {
        super(null, null, false, showIf, showIfReverse, PreferenceType.SLIDER);
        this.min = min;
        this.max = max;
        this.preferenceValue = preferenceValue;
        this.runnable = runnable;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }
    public Runnable getRunnable() {
        return runnable;
    }

    public ConfigProperty<Integer> getPreferenceValue() {
        return preferenceValue;
    }

    public static class SliderRowBuilder extends ToggleableBaseRowBuilder<SliderRow, Integer> {
        private Runnable runnable;
        private int min = 0;
        private int max = 0;

        public SliderRowBuilder min(int val) {
            min = val;
            return this;
        }

        public SliderRowBuilder max(int val) {
            max = val;
            return this;
        }

        public SliderRowBuilder onSelected(Runnable runnable) {
            this.runnable = runnable;
            return this;
        }

        public SliderRow build() {
            return new SliderRow(min, max, preferenceValue, runnable, showIf, showIfReverse);
        }
    }
}
