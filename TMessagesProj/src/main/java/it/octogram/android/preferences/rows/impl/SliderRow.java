package it.octogram.android.preferences.rows.impl;

import it.octogram.android.ConfigProperty;
import it.octogram.android.preferences.PreferenceType;
import it.octogram.android.preferences.rows.BaseRow;
import it.octogram.android.preferences.rows.ToggleableBaseRowBuilder;

public class SliderRow extends BaseRow {

    private final int min;
    private final int max;
    private final ConfigProperty<Integer> preferenceValue;

    protected SliderRow(int min, int max, ConfigProperty<Integer> preferenceValue, ConfigProperty<Boolean> showIf, boolean showIfReverse) {
        super(null, null, false, showIf, showIfReverse, PreferenceType.SLIDER);
        this.min = min;
        this.max = max;
        this.preferenceValue = preferenceValue;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public ConfigProperty<Integer> getPreferenceValue() {
        return preferenceValue;
    }

    public static class SliderRowBuilder extends ToggleableBaseRowBuilder<SliderRow, Integer> {
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

        public SliderRow build() {
            return new SliderRow(min, max, preferenceValue, showIf, showIfReverse);
        }
    }
}
