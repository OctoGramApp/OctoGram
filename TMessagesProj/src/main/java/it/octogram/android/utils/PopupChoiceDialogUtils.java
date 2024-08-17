/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

package it.octogram.android.utils;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.Gravity;
import android.widget.LinearLayout;

import org.apache.commons.lang3.NotImplementedException;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.RadioColorCell;
import org.telegram.ui.Components.CheckBox2;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.Switch;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PopupChoiceDialogUtils {
    private static final ArrayList<Switch> dialogSwitches = new ArrayList<>();
    private static final ArrayList<CheckBox2> dialogCheckBoxes = new ArrayList<>();
    private static Timer timer;

    public static Dialog createChoiceDialog(Activity parentActivity, List<PopupChoiceDialogOption> options, final String title, final int selected, final DialogInterface.OnClickListener listener) {
        clearState();

        AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
        builder.setTitle(title);

        boolean hasEveryOptionIcon = hasEveryOptionIcon(options);

        final LinearLayout linearLayout = new LinearLayout(parentActivity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        builder.setView(linearLayout);

        for (PopupChoiceDialogOption option : options) {
            if (hasEveryOptionIcon) {
                if (option.itemDescription != null) {
                    // as soon as it will be needed
                    // create a custom AlertDialogCell instance and integrate text2
                    throw new NotImplementedException();
                }

                AlertDialog.AlertDialogCell cell = new AlertDialog.AlertDialogCell(parentActivity.getBaseContext(), null);
                cell.setTextAndIcon(option.itemTitle, option.itemIcon);
                cell.setTag(option.id);
                linearLayout.addView(cell);
                cell.setOnClickListener(v -> {
                    int sel = (Integer) v.getTag();
                    builder.getDismissRunnable().run();
                    listener.onClick(null, sel);
                });

                if (option.itemIcon == 0) {
                    if (option.itemSwitchIconUI != null) {
                        Switch switchView = new Switch(builder.getContext(), option.itemSwitchIconUI.getValue());
                        switchView.setColors(Theme.key_switchTrack, Theme.key_switchTrackChecked, Theme.key_windowBackgroundWhite, Theme.key_windowBackgroundWhite);
                        switchView.setEnabled(false);
                        cell.addView(switchView, LayoutHelper.createFrame(37, 50, Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT)));
                        dialogSwitches.add(switchView);
                    } else if (option.itemCheckboxIconUI != null) {
                        CheckBox2 checkBoxView = new CheckBox2(builder.getContext(), 21, option.itemCheckboxIconUI.getValue());
                        checkBoxView.setColor(Theme.key_radioBackgroundChecked, Theme.key_checkboxDisabled, Theme.key_checkboxCheck);
                        checkBoxView.setDrawUnchecked(true);
                        checkBoxView.setDrawBackgroundAsArc(10);
                        cell.addView(checkBoxView, LayoutHelper.createFrame(37, 50, Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT)));
                        dialogCheckBoxes.add(checkBoxView);
                    }
                }

                if (!option.clickable) {
                    cell.setClickable(false);
                    cell.setEnabled(false);
                    cell.setAlpha(0.5f);
                }
            } else {
                RadioColorCell cell = new RadioColorCell(parentActivity);
                cell.setPadding(dp(4), 0, dp(4), 0);
                cell.setTag(option.id);
                cell.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector), Theme.RIPPLE_MASK_ALL));
                cell.setCheckColor(Theme.getColor(Theme.key_radioBackground), Theme.getColor(Theme.key_dialogRadioBackgroundChecked));

                if (option.itemDescription != null) {
                    cell.setTextAndText2AndValue(option.itemTitle, option.itemDescription, selected == option.id);
                } else {
                    cell.setTextAndValue(option.itemTitle, selected == option.id);
                }

                linearLayout.addView(cell);
                cell.setOnClickListener(v -> {
                    int sel = (Integer) v.getTag();
                    builder.getDismissRunnable().run();
                    listener.onClick(null, sel);
                });

                if (!option.clickable) {
                    cell.setClickable(false);
                    cell.setEnabled(false);
                    cell.setAlpha(0.5f);
                }
            }
        }

        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);

        if (!dialogSwitches.isEmpty() || !dialogCheckBoxes.isEmpty()) {
            builder.setOnDismissListener((e) -> clearState());

            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    AndroidUtilities.runOnUIThread(() -> {
                        for (Switch switchView : dialogSwitches) {
                            switchView.setChecked(true, true);
                        }
                        for (CheckBox2 checkBoxView : dialogCheckBoxes) {
                            checkBoxView.setChecked(true, true);
                        }
                        clearState();
                    });
                }
            }, 500);
        }

        return builder.create();
    }

    private static void clearState() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        dialogSwitches.clear();
        dialogCheckBoxes.clear();
    }

    private static boolean hasEveryOptionIcon(List<PopupChoiceDialogOption> options) {
        for (PopupChoiceDialogOption option : options) {
            if (option.itemIcon == 0 && option.itemSwitchIconUI == null && option.itemCheckboxIconUI == null) {
                return false;
            }
        }
        return true;
    }
}
