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
    private final ConfigProperty<String> preferenceValue;

    protected SliderChooseRow(List<Pair<Integer, String>> options, ConfigProperty<String> currentValue, ConfigProperty<Boolean> showIf) {
        super(null, null, false, showIf, PreferenceType.SLIDER_CHOOSE);
        this.options = options;
        this.preferenceValue = currentValue;
    }

    public List<Pair<Integer, String>> getOptions() {
        return options;
    }

    public ConfigProperty<String> getPreferenceValue() {
        return preferenceValue;
    }

    public int getIntValue() {
        for (int index = 0; index < options.size(); index++) {
            Pair<Integer, String> pair = options.get(index);
            if (preferenceValue.getValue().equals(pair.second)) {
                return index;
            }
        }
        return -1;
    }

    public String[] getValues() {
        List<String> titleArray = new ArrayList<>();
        options.forEach(pair -> titleArray.add(pair.second));
        return titleArray.toArray(new String[0]);
    }

    public ArrayList<Integer> getIds() {
        ArrayList<Integer> idArray = new ArrayList<>();
        options.forEach(pair -> idArray.add(pair.first));
        return idArray;
    }

    public static class SliderChooseRowBuilder extends ToggleableBaseRowBuilder<SliderChooseRow, String> {
        private final List<Pair<Integer, String>> options = new ArrayList<>();

        public SliderChooseRowBuilder options(List<Pair<Integer, String>> options) {
            this.options.addAll(options);
            return this;
        }

        public SliderChooseRow build() {
            return new SliderChooseRow(options, preferenceValue, showIf);
        }
    }
}
