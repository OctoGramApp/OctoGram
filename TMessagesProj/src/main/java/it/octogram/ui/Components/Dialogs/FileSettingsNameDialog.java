package it.octogram.ui.Components.Dialogs;

import android.app.Activity;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;

import java.text.DateFormat;
import java.util.Date;

public class FileSettingsNameDialog {
    private boolean enterEventSent;

    public FileSettingsNameDialog(Activity activity, DialogListener dialogListener) {
        final EditTextBoldCursor editText = new EditTextBoldCursor(activity);
        editText.setBackground(Theme.createEditTextDrawable(activity, true));
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, null);
        builder.setTitle(LocaleController.getString("BackupName", R.string.BackupName));
        builder.setCheckFocusable(false);
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), (dialog, which) -> AndroidUtilities.hideKeyboard(editText));
        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        builder.setView(linearLayout);

        editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        editText.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        editText.setMaxLines(1);
        editText.setLines(1);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        editText.setGravity(Gravity.LEFT | Gravity.TOP);
        editText.setSingleLine(true);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setHint(LocaleController.getString("Settings", R.string.Settings));
        editText.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        editText.setCursorColor(Theme.getColor(Theme.key_dialogTextBlue));
        editText.setCursorSize(AndroidUtilities.dp(20));
        editText.setCursorWidth(1.5f);
        editText.setPadding(0, AndroidUtilities.dp(4), 0, 0);
        linearLayout.addView(editText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 36, Gravity.TOP | Gravity.LEFT, 24, 6, 24, 0));
        editText.setOnEditorActionListener((textView, i, keyEvent) -> {
            AndroidUtilities.hideKeyboard(textView);
            builder.create().getButton(AlertDialog.BUTTON_POSITIVE).callOnClick();
            return false;
        });
        editText.addTextChangedListener(new TextWatcher() {
            boolean ignoreTextChange;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (ignoreTextChange) {
                    return;
                }
                if (s.length() > 40) {
                    ignoreTextChange = true;
                    s.delete(40, s.length());
                    AndroidUtilities.shakeView(editText);
                    editText.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                    ignoreTextChange = false;
                }
            }
        });
        builder.setPositiveButton(LocaleController.getString("Save", R.string.Save), (dialog, which) -> {
            AndroidUtilities.hideKeyboard(editText);
            String nameFile = LocaleController.getString("Settings", R.string.Settings);
            if (!TextUtils.isEmpty(editText.getText().toString().trim())) {
                nameFile = editText.getText().toString().trim();
            }
            DateFormat formatter = DateFormat.getDateTimeInstance();
            nameFile = nameFile.replace("%date%", formatter.format(new Date()));
            dialogListener.onConfirm(nameFile);
            builder.getDismissRunnable().run();
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(dialog -> makeFocusable(null, alertDialog, editText, true));
        alertDialog.setOnDismissListener(dialog -> AndroidUtilities.hideKeyboard(editText));
        alertDialog.show();
        editText.requestFocus();
        TextView button = (TextView) alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        if (button != null) {
            button.setTextColor(Theme.getColor(Theme.key_dialogTextRed));
        }
    }

    protected void makeFocusable(BottomSheet bottomSheet, AlertDialog alertDialog, EditTextBoldCursor editText, boolean showKeyboard) {
        if (!enterEventSent) {
            enterEventSent = true;
            if (bottomSheet != null) {
                bottomSheet.setFocusable(true);
            } else if (alertDialog != null) {
                alertDialog.setFocusable(true);
            }
            if (showKeyboard) {
                AndroidUtilities.runOnUIThread(() -> {
                    editText.requestFocus();
                    AndroidUtilities.showKeyboard(editText);
                }, 100);
            }
        }
    }

    public interface DialogListener {
        void onConfirm(String fileName);
    }
}
