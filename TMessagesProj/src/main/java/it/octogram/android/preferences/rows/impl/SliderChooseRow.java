package it.octogram.android.preferences.rows.impl;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import it.octogram.android.ConfigProperty;
import it.octogram.android.preferences.PreferenceType;
import it.octogram.android.preferences.rows.BaseRow;
import it.octogram.android.preferences.rows.ToggleableBaseRowBuilder;

public class SliderChooseRow extends BaseRow {

    private final List<Pair<Integer, String>> options;
    private final ConfigProperty<Integer> preferenceValue;
    private final ConfigProperty<String> currentValue;

    protected SliderChooseRow(List<Pair<Integer, String>> options, ConfigProperty<String> currentValue, ConfigProperty<Integer> preferenceValue) {
        super(PreferenceType.SLIDER_CHOOSE);
        this.options = options;
        this.preferenceValue = preferenceValue;
        this.currentValue = currentValue;
    }

    public List<Pair<Integer, String>> getOptions() {
        return options;
    }

    public ConfigProperty<String> getCurrentValue() {
        return currentValue;
    }

    public int getIntValue() {
        for (int index = 0; index < options.size(); index++) {
            Pair<Integer, String> pair = options.get(index);
            if (currentValue.getValue().equals(pair.second)) {
                return index;
            }
        }
        return -1;
    }

    public String[] getValues() {
        List<String> titleArray = new ArrayList<>();
        for (int index = 0; index < options.size(); index++) {
            Pair<Integer, String> pair = options.get(index);
            titleArray.add(pair.second);
        }
        return titleArray.toArray(new String[0]);
    }

    public ArrayList<Integer> getIds() {
        ArrayList<Integer> idArray = new ArrayList<>();
        for (int index = 0; index < options.size(); index++) {
            Pair<Integer, String> pair = options.get(index);
            idArray.add(pair.first);
        }
        return idArray;
    }

    public ConfigProperty<Integer> getPreferenceValue() {
        return preferenceValue;
    }

    public static class SliderChooseRowBuilder extends ToggleableBaseRowBuilder<SliderChooseRow, Integer> {
        private final List<Pair<Integer, String>> options = new ArrayList<>();
        private ConfigProperty<String> currentValue;

        public SliderChooseRowBuilder options(List<Pair<Integer, String>> options) {
            this.options.addAll(options);
            return this;
        }

        public SliderChooseRowBuilder currentValue(ConfigProperty<String> currentValue) {
            this.currentValue = currentValue;
            return this;
        }

        public SliderChooseRow build() {
            return new SliderChooseRow(options, currentValue, preferenceValue);
        }
    }
}
