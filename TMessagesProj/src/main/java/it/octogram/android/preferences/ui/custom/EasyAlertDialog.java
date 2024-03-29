package it.octogram.android.preferences.ui.custom;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.widget.LinearLayout;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.RadioColorCell;

import java.util.ArrayList;

public class EasyAlertDialog {

    public static void show(ArrayList<? extends CharSequence> entries, String title, int checkedIndex, Context context, OnItemClickListener listener) {
        show(entries, title, checkedIndex, context, listener, null);
    }

    public static void show(ArrayList<? extends CharSequence> entries, String title, int checkedIndex, Context context, OnItemClickListener listener, Theme.ResourcesProvider resourcesProvider) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, resourcesProvider);
        builder.setTitle(title);
        LinearLayout linearLayout = createLinearLayout(context);
        builder.setView(linearLayout);

        for (int i = 0; i < entries.size(); i++) {
            addRadioColorCell(context, linearLayout, entries.get(i), checkedIndex == i, i, builder, listener);
        }

        builder.setNegativeButton(LocaleController.getString(R.string.Cancel), null);
        builder.show();
    }

    private static LinearLayout createLinearLayout(Context context) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        return linearLayout;
    }

    private static void addRadioColorCell(Context context, LinearLayout linearLayout, CharSequence entry, boolean isChecked, int index, AlertDialog.Builder builder, OnItemClickListener listener) {
        RadioColorCell cell = new RadioColorCell(context);
        cell.setPadding(dp(4), 0, dp(4), 0);
        cell.setTag(index);
        cell.setCheckColor(Theme.getColor(Theme.key_radioBackground), Theme.getColor(Theme.key_dialogRadioBackgroundChecked));
        cell.setTextAndValue(entry, isChecked);
        linearLayout.addView(cell);
        cell.setOnClickListener(v -> {
            int which = (int) v.getTag();
            builder.getDismissRunnable().run();
            listener.onClick(which);
        });
    }

    public interface OnItemClickListener {
        void onClick(int i);
    }
}

