/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

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
    private final Runnable onUpdate;
    private final Runnable onTouchEnd;

    protected SliderChooseRow(List<Pair<Integer, String>> options, ConfigProperty<Integer> currentValue, ConfigProperty<Boolean> showIf, boolean showIfReverse, Runnable onUpdate, Runnable onTouchEnd) {
        super(null, null, false, showIf, showIfReverse, PreferenceType.SLIDER_CHOOSE);
        this.options = options;
        this.preferenceValue = currentValue;
        this.onUpdate = onUpdate;
        this.onTouchEnd = onTouchEnd;
    }

    public List<Pair<Integer, String>> getOptions() {
        return options;
    }

    public ConfigProperty<Integer> getPreferenceValue() {
        return preferenceValue;
    }

    public Runnable getOnUpdate() {
        return onUpdate;
    }

    public Runnable getOnTouchEnd() {
        return onTouchEnd;
    }

    public int getIntValue() {
        for (int index = 0; index < options.size(); index++) {
            Pair<Integer, String> pair = options.get(index);
            if (preferenceValue.getValue().equals(pair.first)) {
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


    public static class SliderChooseRowBuilder extends ToggleableBaseRowBuilder<SliderChooseRow, Integer> {
        private final List<Pair<Integer, String>> options = new ArrayList<>();
        private Runnable onUpdate;
        private Runnable onTouchEnd;

        public SliderChooseRowBuilder options(List<Pair<Integer, String>> options) {
            this.options.addAll(options);
            return this;
        }

        public SliderChooseRowBuilder onUpdate(Runnable onUpdate) {
            this.onUpdate = onUpdate;
            return this;
        }

        public SliderChooseRowBuilder onTouchEnd(Runnable onTouchEnd) {
            this.onTouchEnd = onTouchEnd;
            return this;
        }

        public SliderChooseRow build() {
            return new SliderChooseRow(options, preferenceValue, showIf, showIfReverse, onUpdate, onTouchEnd);
        }
    }
}
