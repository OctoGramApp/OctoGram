package it.octogram.android.preferences.ui.custom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Vibrator;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.FileProvider;
import androidx.core.graphics.ColorUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CodepointsLengthInputFilter;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RLottieImageView;
import org.telegram.ui.LaunchActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.utils.ImportSettingsScanHelper;

public class ExportDoneReadyBottomSheet extends BottomSheet {
    private Activity originalActivity;
    private final EditTextBoldCursor editText;
    private static final ImportSettingsScanHelper settingsScan = new ImportSettingsScanHelper();

    public ExportDoneReadyBottomSheet(Context context) {
        super(context, true);
        setApplyBottomPadding(false);
        setApplyTopPadding(false);

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        RLottieImageView imageView = new RLottieImageView(context);
        imageView.setAutoRepeat(true);
        imageView.setAnimation(R.raw.saved_folders, AndroidUtilities.dp(130), AndroidUtilities.dp(130));
        imageView.playAnimation();
        linearLayout.addView(imageView, LayoutHelper.createLinear(144, 144, Gravity.CENTER_HORIZONTAL, 0, 16, 0, 16));

        TextView textView = new TextView(context);
        textView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        textView.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setText(LocaleController.getString("ExportDataReady", R.string.ExportDataReady));
        textView.setPadding(AndroidUtilities.dp(30), 0, AndroidUtilities.dp(30), 0);
        linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        textView = new TextView(context);
        textView.setTextColor(Theme.getColor(Theme.key_dialogTextGray3));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setText(LocaleController.getString("ExportDataDescription", R.string.ExportDataDescription));
        textView.setPadding(AndroidUtilities.dp(30), AndroidUtilities.dp(10), AndroidUtilities.dp(30), AndroidUtilities.dp(21));
        linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        editText = new EditTextBoldCursor(context);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        editText.setHintTextColor(getThemedColor(Theme.key_windowBackgroundWhiteHintText));
        editText.setTextColor(getThemedColor(Theme.key_windowBackgroundWhiteBlackText));
        editText.setBackgroundDrawable(null);
        editText.setLineColors(getThemedColor(Theme.key_windowBackgroundWhiteInputField), getThemedColor(Theme.key_windowBackgroundWhiteInputFieldActivated), getThemedColor(Theme.key_text_RedRegular));
        editText.setMaxLines(1);
        editText.setLines(1);
        editText.setPadding(0, 0, 0, 0);
        editText.setSingleLine(true);
        editText.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setHint(LocaleController.getString("ExportDataFilename", R.string.ExportDataFilename));
        editText.setCursorColor(getThemedColor(Theme.key_windowBackgroundWhiteBlackText));
        editText.setCursorSize(AndroidUtilities.dp(20));
        editText.setCursorWidth(1.5f);
        InputFilter[] inputFilters = new InputFilter[1];
        inputFilters[0] = new CodepointsLengthInputFilter(40) {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (source != null && source.length() > 0 && TextUtils.indexOf(source, '\n') == source.length() - 1) {
                    shareExport(editText.getText().toString().trim());
                    return "";
                }
                CharSequence result = super.filter(source, start, end, dest, dstart, dend);
                if (result != null && source != null && result.length() != source.length()) {
                    Vibrator v = (Vibrator) originalActivity.getSystemService(Context.VIBRATOR_SERVICE);
                    if (v != null) {
                        v.vibrate(200);
                    }
                    AndroidUtilities.shakeView(editText);
                }
                return result;
            }
        };
        editText.setFilters(inputFilters);
        editText.setOnEditorActionListener((currentTextView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                shareExport(editText.getText().toString().trim());
                return true;
            }
            return false;
        });
        linearLayout.addView(editText, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 36, Gravity.LEFT | Gravity.TOP, 17, 15, 17, 0));

        TextView buttonTextView = new TextView(context);
        buttonTextView.setPadding(AndroidUtilities.dp(34), 0, AndroidUtilities.dp(34), 0);
        buttonTextView.setGravity(Gravity.CENTER);
        buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        buttonTextView.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
        buttonTextView.setText(LocaleController.getString("ExportDataShare", R.string.ExportDataShare));
        buttonTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        buttonTextView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(6), Theme.getColor(Theme.key_featuredStickers_addButton), ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_windowBackgroundWhite), 120)));
        buttonTextView.setOnClickListener(view -> {
            shareExport(editText.getText().toString().trim());
        });
        linearLayout.addView(buttonTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, 0, 16, 15, 16, 8));

        setCustomView(linearLayout);
    }

    public void setOriginalActivity(Activity originalActivity) {
        this.originalActivity = originalActivity;
    }

    private void shareExport(String fileNameText) {
        if (fileNameText.contains("/") || fileNameText.length() > 40) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(originalActivity);
            alertDialogBuilder.setTitle(LocaleController.getString("ExportDataShareFailedTitle", R.string.ImportReadyImportFailedZeroTitle));
            alertDialogBuilder.setMessage(LocaleController.getString("ExportDataShareFailedCustomFileName", R.string.ImportReadyImportFailedZeroCaption));
            alertDialogBuilder.setPositiveButton("OK", null);
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return;
        }

        dismiss();

        try {
            JSONObject mainObject = createOctoExport();

            File cacheDir = AndroidUtilities.getCacheDir();
            File cacheFile;
            if (fileNameText.isEmpty()) {
                cacheFile = File.createTempFile("temp", ".octoexport", cacheDir);
            } else {
                cacheFile = new File(cacheDir.getPath(), fileNameText + ".octoexport");
                if (cacheFile.exists()) {
                    cacheFile.delete();
                }
            }

            FileOutputStream fos = new FileOutputStream(cacheFile);
            fos.write(mainObject.toString(4).getBytes());
            fos.close();

            Uri uri = FileProvider.getUriForFile(ApplicationLoader.applicationContext, ApplicationLoader.getApplicationId() + ".provider", cacheFile);

            Intent intent = new Intent(getContext(), LaunchActivity.class);
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/json");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            getContext().startActivity(intent);
        } catch (JSONException e) {
            Log.e(getClass().getName(), "Error sharing settings export", e);
        } catch (IOException e) {
            Log.e(getClass().getName(), "Error generating settings export", e);
        }
    }

    private JSONObject createOctoExport() {
        JSONObject mainObject = new JSONObject();

        for (Field field : OctoConfig.INSTANCE.getClass().getDeclaredFields()) {
            if (field.getType().equals(ConfigProperty.class)) {
                try {
                    ConfigProperty<?> configProperty = (ConfigProperty<?>) field.get(OctoConfig.INSTANCE);
                    String fieldName = field.getName();

                    if (settingsScan.excludedOptions.contains(fieldName)) {
                        continue;
                    }

                    Object fieldValue = null;
                    if (configProperty != null) {
                        fieldValue = configProperty.getValue();
                    }
                    mainObject.put(fieldName, fieldValue);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    Log.e(getClass().getName(), "Error getting settings export", e);
                }
            }
        }

        return mainObject;
    }
}
