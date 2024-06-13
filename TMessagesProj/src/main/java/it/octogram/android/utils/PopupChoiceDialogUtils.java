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
import android.widget.LinearLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.RadioColorCell;
import org.telegram.ui.Components.LayoutHelper;

import java.util.List;

public class PopupChoiceDialogUtils {

    public static Dialog createChoiceDialog(Activity parentActivity, List<PopupChoiceDialogOption> options, final String title, final int selected, final DialogInterface.OnClickListener listener) {
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
                    throw new UnsupportedOperationException();
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
        builder.show();
        return builder.create();
    }

    private static boolean hasEveryOptionIcon(List<PopupChoiceDialogOption> options) {
        for (PopupChoiceDialogOption option : options) {
            if (option.itemIcon == 0) {
                return false;
            }
        }
        return true;
    }
}
