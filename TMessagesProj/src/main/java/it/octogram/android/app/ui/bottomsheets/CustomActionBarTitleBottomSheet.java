/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui.bottomsheets;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.os.Vibrator;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CodepointsLengthInputFilter;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.OutlineEditText;
import org.telegram.ui.Components.StickerImageView;

import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;

public class CustomActionBarTitleBottomSheet extends BottomSheet {
    private final OutlineEditText editText;
    private final CustomActionBarTitleCallback callback;

    public CustomActionBarTitleBottomSheet(Context context, CustomActionBarTitleCallback callback) {
        super(context, true);
        setApplyBottomPadding(false);
        setApplyTopPadding(false);

        this.callback = callback;

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        StickerImageView imageView = new StickerImageView(getContext(), UserConfig.selectedAccount);
        imageView.setStickerPackName(OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME);
        imageView.setStickerNum(StickerUi.HEADER_CUSTOM_TITLE.getValue());
        imageView.getImageReceiver().setAutoRepeat(0);
        linearLayout.addView(imageView, LayoutHelper.createLinear(144, 144, Gravity.CENTER_HORIZONTAL, 0, 16, 0, 16));

        TextView textView = new TextView(context);
        textView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        textView.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setText(getString(R.string.ActionBarTitleCustom));
        textView.setPadding(dp(30), 0, dp(30), 0);
        linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        String appName = getString(R.string.BuildAppName);
        textView = new TextView(context);
        textView.setTextColor(Theme.getColor(Theme.key_dialogTextGray3));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setText(formatString(R.string.ActionBarTitleCustomDescription, appName));
        textView.setPadding(dp(30), dp(10), dp(30), dp(21));
        linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        editText = new OutlineEditText(context);
        editText.getEditText().setMinHeight(AndroidUtilities.dp(58));
        editText.getEditText().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        editText.getEditText().setMaxLines(1);
        editText.getEditText().setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.getEditText().setHint(appName);
        editText.setHint(getString(R.string.ActionBarTitleCustom));
        editText.getEditText().setText(OctoConfig.INSTANCE.actionBarCustomTitle.getValue());

        InputFilter[] inputFilters = new InputFilter[1];
        inputFilters[0] = new CodepointsLengthInputFilter(40) {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dStart, int dEnd) {
                if (source != null && !TextUtils.isEmpty(source) && TextUtils.indexOf(source, '\n') == source.length() - 1) {
                    setCustomTitle(editText.getEditText().getText().toString().trim());
                    return "";
                }
                CharSequence result = super.filter(source, start, end, dest, dStart, dEnd);
                if (result != null && source != null && result.length() != source.length()) {
                    Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                    if (v != null) {
                        v.vibrate(200);
                    }
                    AndroidUtilities.shakeView(editText);
                }
                return result;
            }
        };
        editText.getEditText().setFilters(inputFilters);
        editText.getEditText().setOnEditorActionListener((currentTextView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                setCustomTitle(editText.getEditText().getText().toString().trim());
                return true;
            }
            return false;
        });
        linearLayout.addView(editText, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 58, Gravity.LEFT | Gravity.TOP, 17, 7, 17, 7));

        TextView buttonTextView = new TextView(context);
        buttonTextView.setPadding(dp(34), 0, dp(34), 0);
        buttonTextView.setGravity(Gravity.CENTER);
        buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        buttonTextView.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
        buttonTextView.setText(getString(R.string.ActionBarTitleCustomSet));
        buttonTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        buttonTextView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(dp(6), Theme.getColor(Theme.key_featuredStickers_addButton), ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_windowBackgroundWhite), 120)));
        buttonTextView.setOnClickListener(view -> setCustomTitle(editText.getEditText().getText().toString().trim()));
        linearLayout.addView(buttonTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, 0, 16, 15, 16, 8));

        textView = new TextView(context);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setText(getString(R.string.UseCustomDeviceNameRenameDefault));
        textView.setTextColor(Theme.getColor(Theme.key_dialogTextGray3));
        textView.setOnClickListener(view -> setCustomTitle(""));
        linearLayout.addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, 0, 16, 0, 16, 0));

        setCustomView(linearLayout);
    }

    private void setCustomTitle(String customActionBarTitle) {
        AndroidUtilities.hideKeyboard(editText.getEditText());
        dismiss();

        if (TextUtils.isEmpty(customActionBarTitle)) {
            callback.didReset();
        } else {
            callback.didRenameSuccessfully(customActionBarTitle);
        }
    }

    public interface CustomActionBarTitleCallback {
        void didRenameSuccessfully(String customName);

        void didReset();
    }
}
