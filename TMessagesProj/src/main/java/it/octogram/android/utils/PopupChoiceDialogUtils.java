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
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.widget.LinearLayout;

import org.apache.commons.lang3.NotImplementedException;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.RadioColorCell;
import org.telegram.ui.Components.CheckBox2;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.Switch;

import java.util.List;

public class PopupChoiceDialogUtils {
    public static Dialog createChoiceDialog(Activity parentActivity, List<PopupChoiceDialogOption> options, final CharSequence title, final int selected, final DialogInterface.OnClickListener listener) {
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
                    LinearLayout tempLayout = new LinearLayout(builder.getContext());
                    tempLayout.setOrientation(LinearLayout.HORIZONTAL);

                    if (option.itemSwitchIconUI != null) {
                        Switch switchView = getSwitch(builder.getContext(), option.itemSwitchIconUI.getValue());
                        Switch checkedView = getSwitch(builder.getContext(), option.itemSwitchIconUI.getValue());
                        checkedView.setChecked(true, false);

                        tempLayout.addView(switchView, LayoutHelper.createFrame(37, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL | Gravity.LEFT, 0, 0, 10, 0));
                        tempLayout.addView(checkedView, LayoutHelper.createFrame(37, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL | Gravity.RIGHT));
                    } else if (option.itemCheckboxIconUI != null) {
                        CheckBox2 checkBoxView = getCheckbox(builder.getContext(), option.itemCheckboxIconUI.getValue());
                        CheckBox2 checkedView = getCheckbox(builder.getContext(), option.itemCheckboxIconUI.getValue());
                        checkedView.setChecked(true, false);

                        tempLayout.addView(checkBoxView, LayoutHelper.createFrame(23, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL | Gravity.LEFT, 0, 0, 10, 0));
                        tempLayout.addView(checkedView, LayoutHelper.createFrame(23, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL | Gravity.RIGHT));
                    }

                    cell.addView(tempLayout, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 50, Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT)));
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

        return builder.create();
    }

    private static Switch getSwitch(Context context, int switchIconUI) {
        Switch switchView = new Switch(context, switchIconUI);
        switchView.setColors(Theme.key_switchTrack, Theme.key_switchTrackChecked, Theme.key_windowBackgroundWhite, Theme.key_windowBackgroundWhite);
        switchView.setEnabled(false);
        return switchView;
    }

    private static CheckBox2 getCheckbox(Context context, int checkBoxIconUI) {
        CheckBox2 checkBoxView = new CheckBox2(context, 21, checkBoxIconUI);
        checkBoxView.setColor(Theme.key_radioBackgroundChecked, Theme.key_checkboxDisabled, Theme.key_checkboxCheck);
        checkBoxView.setDrawUnchecked(true);
        checkBoxView.setDrawBackgroundAsArc(10);
        return checkBoxView;
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
