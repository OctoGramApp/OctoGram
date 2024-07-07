package it.octogram.android.preferences.rows.impl;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;

import androidx.annotation.Nullable;

import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Cells.TextSettingsCell;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import it.octogram.android.ConfigProperty;
import it.octogram.android.preferences.PreferenceType;
import it.octogram.android.preferences.rows.BaseRow;
import it.octogram.android.preferences.rows.BaseRowBuilder;
import it.octogram.android.preferences.rows.Clickable;
import it.octogram.android.utils.PopupChoiceDialogOption;
import it.octogram.android.utils.PopupChoiceDialogUtils;

public class ListRow extends BaseRow implements Clickable {

    private List<PopupChoiceDialogOption> options;
    private final Supplier<List<PopupChoiceDialogOption>> supplierOptions;
    private final ConfigProperty<Integer> currentValue;

    private final Supplier<Boolean> supplierClickable;
    private final Runnable supplierClickableSelected;

    public ListRow(@Nullable String title,
                   boolean divider,
                   boolean requiresRestart,
                   ConfigProperty<Boolean> showIf,
                   boolean showIfReverse,
                   List<PopupChoiceDialogOption> options,
                   Supplier<List<PopupChoiceDialogOption>> supplierOptions,
                   ConfigProperty<Integer> currentValue,
                   Supplier<Boolean> supplierClickable,
                   Runnable supplierClickableSelected
               ) {
        super(title, null, requiresRestart, showIf, showIfReverse, divider, PreferenceType.LIST);
        this.options = options;
        this.supplierOptions = supplierOptions;
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

        if (supplierOptions != null) {
            options = supplierOptions.get();
        }

        Dialog dialog = PopupChoiceDialogUtils.createChoiceDialog(
                activity,
                options,
                getTitle(),
                getSelectedOption() != null ? getSelectedOption().id : 0,
                (dialogInterface, sel) -> {
                    PopupChoiceDialogOption currentOption = getOptionFromId(sel);
                    if (currentOption == null) {
                        return;
                    }

                    currentValue.updateValue(currentOption.id);
                    if (supplierClickableSelected != null) {
                        supplierClickableSelected.run();
                    }

                    reloadPreferencesUI.run();
                    if (view instanceof TextSettingsCell) {
                        ((TextSettingsCell) view).setTextAndValue(getTitle(), currentOption.itemTitle, true, hasDivider());
                    }
                }
        );

        fragment.setVisibleDialog(dialog);
        dialog.show();
        return true;
    }

    private PopupChoiceDialogOption getOptionFromId(int id) {
        for (int i = 0; i < options.size(); i++) {
            PopupChoiceDialogOption option = options.get(i);
            if (option.id == id) {
                return option;
            }
        }
        return null;
    }

    private PopupChoiceDialogOption getSelectedOption() {
        for (int i = 0; i < options.size(); i++) {
            PopupChoiceDialogOption option = options.get(i);
            if (currentValue.getValue() == option.id) {
                return option;
            }
        }
        return null;
    }

    public String getTextFromInteger(int id) {
        for (int i = 0; i < options.size(); i++) {
            PopupChoiceDialogOption option = options.get(i);

            if (option.id == id) {
                return option.itemTitle;
            }
        }
        return null;
    }

    public ConfigProperty<Integer> getCurrentValue() {
        return currentValue;
    }

    public static class ListRowBuilder extends BaseRowBuilder<ListRow> {

        private final List<PopupChoiceDialogOption> options = new ArrayList<>();
        private Supplier<List<PopupChoiceDialogOption>> supplierOptions = null;
        private Supplier<Boolean> supplierClickable;
        private Runnable supplierClickableSelected;
        private ConfigProperty<Integer> currentValue;

        public ListRowBuilder options(List<PopupChoiceDialogOption> opt) {
            options.addAll(opt);
            return this;
        }

        public ListRowBuilder supplierOptions(Supplier<List<PopupChoiceDialogOption>> supplierOptions) {
            this.supplierOptions = supplierOptions;

            if (supplierOptions != null) {
                options.addAll(supplierOptions.get());
            }

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
            return new ListRow(title, divider, requiresRestart, showIf, showIfReverse, options, supplierOptions, currentValue, supplierClickable, supplierClickableSelected);
        }
    }
}
