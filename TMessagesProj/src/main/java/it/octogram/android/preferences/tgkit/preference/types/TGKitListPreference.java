package it.octogram.android.preferences.tgkit.preference.types;

import android.app.Activity;
import android.app.Dialog;
import android.util.Pair;
import android.view.View;

import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.AlertsCreator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import it.octogram.android.preferences.tgkit.preference.TGKitPreference;
import kotlin.Triple;

public class TGKitListPreference extends TGKitPreference {

    private boolean divider = false;
    private TGTLContract contract;

    @Override
    public TGPType getType() {
        return TGPType.LIST;
    }

    public void callAction(BaseFragment fragment, Activity activity, View view, float x, float y, TempInterface temp) {
        int selected = 0;
        List<String> titleArray = new ArrayList<>();
        List<Integer> idArray = new ArrayList<>();

        if (contract.hasIcons()) {
            List<Triple<Integer, String, Integer>> optionsIcons = contract.getOptionsIcons();
            for (int index = 0; index < optionsIcons.size(); index++) {
                Triple<Integer, String, Integer> triple = optionsIcons.get(index);
                titleArray.add(triple.component2());
                idArray.add(triple.component1());

                if (contract.getValue().equals(triple.component2())) {
                    selected = index;
                }
            }
        } else {
            List<Pair<Integer, String>> options = contract.getOptions();
            for (int index = 0; index < options.size(); index++) {
                Pair<Integer, String> pair = options.get(index);
                titleArray.add(pair.second);
                idArray.add(pair.first);

                if (contract.getValue().equals(pair.second)) {
                    selected = index;
                }
            }
        }

        Dialog dialog = AlertsCreator.createSingleChoiceDialog(
                activity,
                titleArray.toArray(new String[0]),
                title,
                selected,
                (dialogInterface, sel) -> {
                    contract.setValue(idArray.get(sel));
                    temp.update();
                }
        );

        fragment.setVisibleDialog(dialog);

        dialog.show();
    }

    public void contractIcons(List<Triple<Integer, String, Integer>> options, String value, Function<Integer, Void> valueSetter) {
        contract = new TGTLContract() {
            @Override
            public void setValue(int id) {
                valueSetter.apply(id);
            }

            @Override
            public String getValue() {
                return value;
            }

            @Override
            public List<Pair<Integer, String>> getOptions() {
                return new ArrayList<>();
            }

            @Override
            public List<Triple<Integer, String, Integer>> getOptionsIcons() {
                return options;
            }

            @Override
            public boolean hasIcons() {
                return true;
            }
        };

    }

    public void contract(List<Pair<Integer, String>> options, String value, Function<Integer, Void> valueSetter) {
        contract = new TGTLContract() {
            @Override
            public void setValue(int id) {
                valueSetter.apply(id);
            }

            @Override
            public String getValue() {
                return value;
            }

            @Override
            public List<Pair<Integer, String>> getOptions() {
                return options;
            }

            @Override
            public List<Triple<Integer, String, Integer>> getOptionsIcons() {
                return new ArrayList<>();
            }

            @Override
            public boolean hasIcons() {
                return false;
            }
        };

    }


    public void setDivider(boolean divider) {
        this.divider = divider;
    }

    public void setContract(TGTLContract contract) {
        this.contract = contract;
    }

    public boolean getDivider() {
        return divider;
    }

    public TGTLContract getContract() {
        return contract;
    }

    public interface TGTLContract {
        void setValue(int id);

        String getValue();

        List<Pair<Integer, String>> getOptions();

        List<Triple<Integer, String, Integer>> getOptionsIcons();

        boolean hasIcons();
    }

    public interface TempInterface {
        void update();
    }
}
