/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui.custom;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Vibrator;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.CodepointsLengthInputFilter;
import org.telegram.ui.Components.ItemOptions;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.OutlineEditText;
import org.telegram.ui.Components.StickerImageView;
import org.telegram.ui.LaunchActivity;

import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;
import it.octogram.android.ai.helper.CustomModelsHelper;
import it.octogram.android.preferences.ui.OctoMainSettingsUI;
import it.octogram.android.utils.chat.FileShareHelper;
import it.octogram.android.utils.config.ImportSettingsScanHelper;

public class ExportDoneReadyBottomSheet extends BottomSheet {
    public static final int CREATE_FILE_REQ = 300;
    private final BaseFragment baseFragment;
    private final OutlineEditText inputElement;
    private final ImageView configButton;

    public interface SaveDataState {
        int SAVE_EVERYTHING = 0;
        int SAVE_SETTINGS = 1;
        int SAVE_MODELS = 2;
    }
    private int saveDataState = SaveDataState.SAVE_EVERYTHING;

    public ExportDoneReadyBottomSheet(Context context, BaseFragment baseFragment) {
        super(context, true);
        setApplyBottomPadding(false);
        setApplyTopPadding(false);

        this.baseFragment = baseFragment;

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        StickerImageView imageView = new StickerImageView(getContext(), UserConfig.selectedAccount);
        imageView.setStickerPackName(OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME);
        imageView.setStickerNum(StickerUi.IMPORT_SETTINGS.getValue());
        imageView.getImageReceiver().setAutoRepeat(1);
        linearLayout.addView(imageView, LayoutHelper.createLinear(144, 144, Gravity.CENTER_HORIZONTAL, 0, 16, 0, 16));

        TextView textView = new TextView(context);
        textView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        textView.setTypeface(AndroidUtilities.bold());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setText(getString(R.string.ExportDataReady));
        textView.setPadding(dp(30), 0, dp(30), 0);
        linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        textView = new TextView(context);
        textView.setTextColor(Theme.getColor(Theme.key_dialogTextGray3));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setText(getString(R.string.ExportDataDescription));
        textView.setPadding(dp(30), dp(10), dp(30), dp(21));
        linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        inputElement = new OutlineEditText(context);
        inputElement.getEditText().setMinHeight(AndroidUtilities.dp(58));
        inputElement.getEditText().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        inputElement.getEditText().setMaxLines(1);
        inputElement.setHint(getString(R.string.ExportDataFilename));

        InputFilter[] inputFilters = new InputFilter[1];
        inputFilters[0] = new CodepointsLengthInputFilter(40) {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dStart, int dEnd) {
                if (source != null && !TextUtils.isEmpty(source) && TextUtils.indexOf(source, '\n') == source.length() - 1) {
                    return "";
                }
                CharSequence result = super.filter(source, start, end, dest, dStart, dEnd);
                if (result != null && source != null && result.length() != source.length()) {
                    shakeEditText();
                }
                return result;
            }
        };
        inputElement.getEditText().setFilters(inputFilters);
        inputElement.getEditText().setOnEditorActionListener((currentTextView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                AndroidUtilities.hideKeyboard(inputElement.getEditText());
                return true;
            }
            return false;
        });
        linearLayout.addView(inputElement, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 58, Gravity.LEFT | Gravity.TOP, 17, 7, 17, 7));

        FrameLayout buttonView = new FrameLayout(context);
        buttonView.setBackgroundColor(getThemedColor(Theme.key_dialogBackground));

        TextView buttonTextView = new TextView(context);
        buttonTextView.setPadding(dp(34), 0, dp(34), 0);
        buttonTextView.setGravity(Gravity.CENTER);
        buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        buttonTextView.setTypeface(AndroidUtilities.bold());
        buttonTextView.setText(getString(R.string.ExportDataShare_SavedMessages));
        buttonTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        buttonTextView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(dp(6), Theme.getColor(Theme.key_featuredStickers_addButton), ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_windowBackgroundWhite), 120)));
        buttonTextView.setOnClickListener(view -> shareExport(true));
        buttonView.addView(buttonTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 16, 16, 16+48+8, 16));

        configButton = new ImageView(context);
        configButton.setScaleType(ImageView.ScaleType.CENTER);
        configButton.setImageResource(R.drawable.msg_download_settings);
        configButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_featuredStickers_buttonText), PorterDuff.Mode.MULTIPLY));
        configButton.setBackground(Theme.AdaptiveRipple.filledRect(Theme.getColor(Theme.key_featuredStickers_addButton), 6));
        configButton.setOnClickListener(v -> openConfig());
        buttonView.addView(configButton, LayoutHelper.createFrame(48, 48, Gravity.BOTTOM | Gravity.RIGHT, 0, 16, 16, 16));

        linearLayout.addView(buttonView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL));

        setCustomView(linearLayout);
    }

    private void shakeEditText() {
        if (inputElement != null) {
            AndroidUtilities.shakeView(inputElement);
            Vibrator v = (Vibrator) LaunchActivity.instance.getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) {
                v.vibrate(200);
            }
        }
    }

    private void openConfig() {
        ItemOptions options = ItemOptions.makeOptions(containerView, null, configButton, true);
        options.setGravity(Gravity.RIGHT);
        options.forceBottom(true);

        if (!CustomModelsHelper.getModelsList().isEmpty()) {
            TextView[] mainSubTextView = {null};

            final ItemOptions whatToSaveScrollback = options.makeSwipeback();
            Runnable fillOptions = () -> {
                if (whatToSaveScrollback.getLinearLayout() != null) {
                    whatToSaveScrollback.getLinearLayout().removeAllViews();
                }
                if (whatToSaveScrollback.getLastLayout() != null) {
                    whatToSaveScrollback.getLastLayout().removeAllViews();
                }
                whatToSaveScrollback.add(R.drawable.ic_ab_back, getString(R.string.Back), options::closeSwipeback);
                for (int i = 0; i <= SaveDataState.SAVE_MODELS; i++) {
                    int finalI = i;
                    whatToSaveScrollback.addChecked(saveDataState == i, getString(getCorrespondingSaveDataString(i)), () -> {
                        saveDataState = finalI;
                        if (mainSubTextView[0] != null) {
                            mainSubTextView[0].setText(getString(getCorrespondingSaveDataString(finalI)));
                            options.closeSwipeback();
                        } else {
                            options.dismiss();
                        }
                    });
                }
            };
            fillOptions.run();

            options.add(getString(R.string.ExportDataSettings_SaveData), getString(getCorrespondingSaveDataString(saveDataState)), () -> {
                fillOptions.run();
                options.openSwipeback(whatToSaveScrollback);
            });

            View lastChild = options.getLastLayout().getItemAt(options.getLastLayout().getItemsCount() - 1);
            if (lastChild instanceof ActionBarMenuSubItem) {
                TextView subtextView = ((ActionBarMenuSubItem) lastChild).subtextView;
                if (subtextView != null) {
                    mainSubTextView[0] = subtextView;
                    subtextView.setPadding(0, 0, 0, 0);
                }
            }

            options.addGap();
        }

        options.add(R.drawable.msg_share, getString(R.string.ExportDataShare), () -> shareExport(false));
        options.add(R.drawable.media_download, getString(R.string.ExportDataSave), this::saveFileLocally);
        if (LocaleController.isRTL) {
            options.setGravity(Gravity.LEFT);
        }
        options.setDimAlpha(0);
        options.show();
    }

    private int getCorrespondingSaveDataString(int state) {
        return switch(state) {
            case SaveDataState.SAVE_SETTINGS -> R.string.ExportDataSettings_JustSettings;
            case SaveDataState.SAVE_MODELS -> R.string.ExportDataSettings_JustAiModels;
            default -> R.string.ExportDataSettings_Everything;
        };
    }

    public void shareExport(boolean shareToSavedMessages) {
        String fileNameText = inputElement.getEditText().getText().toString().trim();
        if (fileNameText.isEmpty()) {
            fileNameText = "default-config";
        }

        FileShareHelper.FileShareData data = new FileShareHelper.FileShareData();
        data.fileName = fileNameText;
        data.fileExtension = OctoConfig.OCTOEXPORT_EXTENSION;
        data.fileContent = createOctoExport(saveDataState);
        data.fragment = baseFragment;
        data.caption = getString(R.string.ExportDataShareFileComment);
        data.delegate = new FileShareHelper.FileShareData.FileShareDelegate() {
            @Override
            public void onChatSelectSheetOpen() {
                dismiss();
            }

            @Override
            public void onSuccess() {
                AndroidUtilities.runOnUIThread(() -> BulletinFactory.of(baseFragment).createSimpleBulletin(R.raw.forward, getString(R.string.ExportDataShareDone)).show());
            }

            @Override
            public void onInvalidName() {
                shakeEditText();
            }
        };
        data.shareToSavedMessages = shareToSavedMessages;
        FileShareHelper.init(data);
    }

    private void saveFileLocally() {
        String fileNameText = inputElement.getEditText().getText().toString().trim();
        if (fileNameText.isEmpty()) {
            fileNameText = "default-config";
        }

        if (FileShareHelper.isInvalidName(fileNameText)) {
            shakeEditText();
            return;
        }

        dismiss();
        OctoMainSettingsUI.setSaveDataStateCache(saveDataState);

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, fileNameText+OctoConfig.OCTOEXPORT_EXTENSION);
        baseFragment.startActivityForResult(intent, CREATE_FILE_REQ);
    }

    public static JSONObject createOctoExport(int saveDataState) {
        JSONObject mainObject = new JSONObject();

        if (saveDataState <= 1) {
            for (ImportSettingsScanHelper.SettingsScanCategory category : ImportSettingsScanHelper.INSTANCE.categories) {
                for (ImportSettingsScanHelper.SettingsScanOption option : category.options) {
                    if (ImportSettingsScanHelper.INSTANCE.excludedOptions.contains(option.property.getKey())) {
                        continue;
                    }

                    try {
                        mainObject.put(option.property.getKey(), option.property.getValue());
                    } catch (JSONException ignored) {}
                }
            }
        }

        if (saveDataState == SaveDataState.SAVE_MODELS || saveDataState == SaveDataState.SAVE_EVERYTHING) {
            JSONArray array = new JSONArray();
            for (CustomModelsHelper.CustomModel model : CustomModelsHelper.getModelsList().values()) {
                array.put(model.convertAsObject());
            }
            try {
                mainObject.put("ai_models", array);
            } catch (JSONException ignored) {}
        }

        return mainObject;
    }
}
