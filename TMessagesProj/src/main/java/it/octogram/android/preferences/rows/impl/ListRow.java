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
import java.util.function.Supplier;

import it.octogram.android.ConfigProperty;
import it.octogram.android.preferences.PreferenceType;
import it.octogram.android.preferences.rows.BaseRow;
import it.octogram.android.preferences.rows.BaseRowBuilder;
import it.octogram.android.preferences.rows.Clickable;
import it.octogram.android.utils.PopupChoiceDialogUtils;
import kotlin.Triple;

public class ListRow extends BaseRow implements Clickable {

    private final List<Pair<Integer, String>> options;
    private final List<Triple<Integer, String, Integer>> optionsIcons;
    private final ConfigProperty<Integer> currentValue;

    private final Supplier<Boolean> supplierClickable;
    private final Runnable supplierClickableSelected;

    public ListRow(@Nullable String title,
                   boolean divider,
                   boolean requiresRestart,
                   ConfigProperty<Boolean> showIf,
                   boolean showIfReverse,
                   List<Pair<Integer, String>> options,
                   @Nullable List<Triple<Integer, String, Integer>> optionsIcons,
                   ConfigProperty<Integer> currentValue,
                   Supplier<Boolean> supplierClickable,
                   Runnable supplierClickableSelected
               ) {
        super(title, null, requiresRestart, showIf, showIfReverse, divider, PreferenceType.LIST);
        this.options = options;
        this.optionsIcons = optionsIcons;
        this.currentValue = currentValue;
        this.supplierClickable = supplierClickable;
        this.supplierClickableSelected = supplierClickableSelected;
    }

    @Override
    public boolean onClick(BaseFragment fragment, Activity activity, View view, int position, float x, float y) {
        // onClick is useless and it isn't used as PreferencesFragment uses onCustomClick wsith Runnable reloadPreferencesUI
        return false;
    }

    public boolean onCustomClick(BaseFragment fragment, Activity activity, View view, int position, float x, float y, Runnable reloadPreferencesUI) {
        if (supplierClickable != null && !supplierClickable.get()) {
            return false;
        }

        int selected = 0;
        List<String> titleArray = new ArrayList<>();
        List<Integer> idArray = new ArrayList<>();
        Dialog dialog;

        if (!optionsIcons.isEmpty()) {
            List<Integer> icons = new ArrayList<>();
            for (int index = 0; index < optionsIcons.size(); index++) {
                Triple<Integer, String, Integer> triple = optionsIcons.get(index);
                titleArray.add(triple.component2());
                idArray.add(triple.component1());
                icons.add(triple.component3());

                if (currentValue.getValue().equals(triple.component1())) {
                    selected = index;
                }
            }
            dialog = PopupChoiceDialogUtils.createSingleChoiceIconsDialog(
                    activity,
                    titleArray.toArray(new String[0]),
                    icons.stream().mapToInt(i -> i).toArray(),
                    getTitle(),
                    selected,
                    (dialogInterface, sel) -> {
                        int id = idArray.get(sel);
                        currentValue.updateValue(optionsIcons.get(id).component1());
                        if (supplierClickableSelected != null) {
                            supplierClickableSelected.run();
                        }
                        reloadPreferencesUI.run();
                        if (view instanceof TextSettingsCell) {
                            ((TextSettingsCell) view).setTextAndValue(getTitle(), getTextFromInteger(currentValue.getValue()), true, hasDivider());
                        }
                    }
            );
        } else {
            for (int index = 0; index < options.size(); index++) {
                Pair<Integer, String> pair = options.get(index);
                titleArray.add(pair.second);
                idArray.add(pair.first);

                if (currentValue.getValue().equals(pair.first)) {
                    selected = index;
                }
            }
            dialog = AlertsCreator.createSingleChoiceDialog(
                    activity,
                    titleArray.toArray(new String[0]),
                    getTitle(),
                    selected,
                    (dialogInterface, sel) -> {
                        int id = idArray.get(sel);
                        currentValue.updateValue(options.get(id).first);
                        if (supplierClickableSelected != null) {
                            supplierClickableSelected.run();
                        }
                        reloadPreferencesUI.run();
                        if (view instanceof TextSettingsCell) {
                            ((TextSettingsCell) view).setTextAndValue(getTitle(), getTextFromInteger(currentValue.getValue()), true, hasDivider());
                        }
                    }
            );
        }
        fragment.setVisibleDialog(dialog);
        dialog.show();
        return true;
    }

    public String getTextFromInteger(int integer) {
        List<String> titleArray = new ArrayList<>();
        if (!options.isEmpty()) {
            options.forEach(pair -> titleArray.add(pair.second));
            for (int index = 0; index < titleArray.size(); index++) {
                if (options.get(index).first.equals(integer)) {
                    return titleArray.get(index);
                }
            }
        } else {
            optionsIcons.forEach(triple -> titleArray.add(triple.component2()));
            for (int index = 0; index < titleArray.size(); index++) {
                if (optionsIcons.get(index).component1().equals(integer)) {
                    return titleArray.get(index);
                }
            }
        }
        return "";
    }

    public ConfigProperty<Integer> getCurrentValue() {
        return currentValue;
    }

    public static class ListRowBuilder extends BaseRowBuilder<ListRow> {

        private final List<Pair<Integer, String>> options = new ArrayList<>();
        private final List<Triple<Integer, String, Integer>> optionsIcons = new ArrayList<>();
        private Supplier<Boolean> supplierClickable;
        private Runnable supplierClickableSelected;
        private ConfigProperty<Integer> currentValue;

        public ListRowBuilder options(List<Pair<Integer, String>> opt) {
            options.addAll(opt);
            return this;
        }

        public ListRowBuilder optionsIcons(List<Triple<Integer, String, Integer>> opt) {
            optionsIcons.addAll(opt);
            return this;
        }

        public ListRowBuilder currentValue(ConfigProperty<Integer> currentValue) {
            this.currentValue = currentValue;
            return this;
        }

        public ListRowBuilder onClick(Supplier<Boolean> supplierClickable) {
            this.supplierClickable = supplierClickable;
            return this;
        }

        public ListRowBuilder onSelected(Runnable supplierClickableSelected) {
            this.supplierClickableSelected = supplierClickableSelected;
            return this;
        }

        @Override
        public ListRow build() {
            return new ListRow(title, divider, requiresRestart, showIf, showIfReverse, options, optionsIcons, currentValue, supplierClickable, supplierClickableSelected);
        }
    }
}
