/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */
package it.octogram.android.preferences.tgkit.preference.types;

import android.app.Activity;
import android.app.Dialog;
import android.util.Pair;
import android.view.View;

import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.AlertsCreator;

import java.util.ArrayList;
import java.util.List;

import it.octogram.android.preferences.tgkit.preference.TGKitPreference;
import kotlin.Triple;

public class TGKitListPreference extends TGKitPreference {

    private boolean divider = false;
    private TGTLContract contract;

    public TGKitListPreference(String title, TGTLContract contract, boolean divider) {
        this.title = title;
        this.contract = contract;
        this.divider = divider;
    }

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

    public boolean getDivider() {
        return divider;
    }

    public void setDivider(boolean divider) {
        this.divider = divider;
    }

    public TGTLContract getContract() {
        return contract;
    }

    public void setContract(TGTLContract contract) {
        this.contract = contract;
    }

    public interface TGTLContract {
        String getValue();

        void setValue(int id);

        List<Pair<Integer, String>> getOptions();

        List<Triple<Integer, String, Integer>> getOptionsIcons();

        boolean hasIcons();
    }

    public interface TempInterface {
        void update();
    }
}
