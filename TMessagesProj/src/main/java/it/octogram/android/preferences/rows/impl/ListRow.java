package it.octogram.android.preferences.rows.impl;

import android.app.Activity;
import android.app.Dialog;
import android.util.Pair;
import android.view.View;

import androidx.annotation.Nullable;

import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.AlertsCreator;

import java.util.ArrayList;
import java.util.List;

import it.octogram.android.OctoConfig;
import it.octogram.android.ConfigProperty;
import it.octogram.android.preferences.PreferenceType;
import it.octogram.android.preferences.rows.BaseRow;
import it.octogram.android.preferences.rows.BaseRowBuilder;
import it.octogram.android.preferences.rows.Clickable;
import kotlin.Triple;

public class ListRow extends BaseRow implements Clickable {

    private final List<Pair<Integer, String>> options;
    private final List<Triple<Integer, String, Integer>> optionsIcons;
    private final ConfigProperty<String> currentValue;

    public ListRow(@Nullable String title,
                   boolean divider,
                   boolean requiresRestart,
                   ConfigProperty<Boolean> showIf,
                   List<Pair<Integer, String>> options,
                   @Nullable List<Triple<Integer, String, Integer>> optionsIcons,
                   ConfigProperty<String> currentValue) {
        super(title, null, requiresRestart, showIf, divider, PreferenceType.LIST);
        this.options = options;
        this.optionsIcons = optionsIcons;
        this.currentValue = currentValue;
    }

    @Override
    public boolean onClick(BaseFragment fragment, Activity activity, View view, int position, float x, float y) {
        int selected = 0;
        List<String> titleArray = new ArrayList<>();
        List<Integer> idArray = new ArrayList<>();

        if (!optionsIcons.isEmpty()) {
            for (int index = 0; index < optionsIcons.size(); index++) {
                Triple<Integer, String, Integer> triple = optionsIcons.get(index);
                titleArray.add(triple.component2());
                idArray.add(triple.component1());

                if (currentValue.getValue().equals(triple.component2())) {
                    selected = index;
                }
            }
        } else {
            for (int index = 0; index < options.size(); index++) {
                Pair<Integer, String> pair = options.get(index);
                titleArray.add(pair.second);
                idArray.add(pair.first);

                if (currentValue.getValue().equals(pair.second)) {
                    selected = index;
                }
            }
        }

        Dialog dialog = AlertsCreator.createSingleChoiceDialog(
                activity,
                titleArray.toArray(new String[0]),
                getTitle(),
                selected,
                (dialogInterface, sel) -> {
                    int id = idArray.get(sel);
                    OctoConfig.INSTANCE.updateStringSetting(currentValue, options.get(id).second);
                    if (view instanceof TextSettingsCell) {
                        ((TextSettingsCell) view).setTextAndValue(getTitle(), currentValue.getValue(), hasDivider());
                    }
                }
        );

        fragment.setVisibleDialog(dialog);

        dialog.show();
        return true;
    }

    public ConfigProperty<String> getCurrentValue() {
        return currentValue;
    }

    public static class ListRowBuilder extends BaseRowBuilder<ListRow> {

        private final List<Pair<Integer, String>> options = new ArrayList<>();
        private final List<Triple<Integer, String, Integer>> optionsIcons = new ArrayList<>();
        private ConfigProperty<String> currentValue;

        public ListRowBuilder options(List<Pair<Integer, String>> opt) {
            options.addAll(opt);
            return this;
        }

        public ListRowBuilder optionsIcons(List<Triple<Integer, String, Integer>> opt) {
            optionsIcons.addAll(opt);
            return this;
        }

        public ListRowBuilder currentValue(ConfigProperty<String> currentValue) {
            this.currentValue = currentValue;
            return this;
        }

        @Override
        public ListRow build() {
            return new ListRow(title, divider, requiresRestart, showIf, options, optionsIcons, currentValue);
        }
    }
}
