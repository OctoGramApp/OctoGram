/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui.custom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import androidx.collection.LongSparseArray;
import androidx.core.graphics.ColorUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.CodepointsLengthInputFilter;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ShareAlert;
import org.telegram.ui.Components.StickerImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Objects;

import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;
import it.octogram.android.logs.OctoLogging;
import it.octogram.android.utils.ImportSettingsScanHelper;

public class ExportDoneReadyBottomSheet extends BottomSheet implements NotificationCenter.NotificationCenterDelegate {
    public static final int CREATE_FILE_REQ = 300;

    private final Activity originalActivity;
    private final BaseFragment baseFragment;
    private final EditTextBoldCursor editText;
    private static final ImportSettingsScanHelper settingsScan = new ImportSettingsScanHelper();

    private String sharingFullLocation;
    private String sharingFileName;
    private LongSparseArray<TLRPC.Dialog> sharingDids = new LongSparseArray<>();

    private Runnable onCompletedRunnable;
    private Runnable onFailedRunnable;

    public ExportDoneReadyBottomSheet(Context context, Activity originalActivity, BaseFragment baseFragment) {
        super(context, true);
        setApplyBottomPadding(false);
        setApplyTopPadding(false);

        this.originalActivity = originalActivity;
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
        textView.setText(LocaleController.getString(R.string.ExportDataReady));
        textView.setPadding(AndroidUtilities.dp(30), 0, AndroidUtilities.dp(30), 0);
        linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        textView = new TextView(context);
        textView.setTextColor(Theme.getColor(Theme.key_dialogTextGray3));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        textView.setText(LocaleController.getString(R.string.ExportDataDescription));
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
        editText.setHint(LocaleController.getString(R.string.ExportDataFilename));
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
        buttonTextView.setTypeface(AndroidUtilities.bold());
        buttonTextView.setText(LocaleController.getString(R.string.ExportDataShare));
        buttonTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        buttonTextView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(6), Theme.getColor(Theme.key_featuredStickers_addButton), ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_windowBackgroundWhite), 120)));
        buttonTextView.setOnClickListener(view -> shareExport(editText.getText().toString().trim()));
        linearLayout.addView(buttonTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, 0, 16, 15, 16, 8));

        TextView saveTextView = new TextView(context);
        saveTextView.setGravity(Gravity.CENTER);
        saveTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        saveTextView.setText(LocaleController.getString(R.string.ExportDataSave));
        saveTextView.setTextColor(Theme.getColor(Theme.key_dialogTextGray3));
        saveTextView.setOnClickListener(view -> {
            dismiss();
            saveFileLocally(editText.getText().toString().trim());
        });
        linearLayout.addView(saveTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, 0, 16, 0, 16, 8));

        setCustomView(linearLayout);
    }

    public void setOnCompletedRunnable(Runnable onCompletedRunnable) {
        this.onCompletedRunnable = onCompletedRunnable;
    }

    public void setOnFailedRunnable(Runnable onFailedRunnable) {
        this.onFailedRunnable = onFailedRunnable;
    }

    public void shareExport(String fileNameText) {
        if (isFileNameInvalid(fileNameText)) {
            return;
        }

        if (baseFragment != null) {
            dismiss();
        }

        try {
            JSONObject mainObject = createOctoExport();

            File cacheDir = AndroidUtilities.getCacheDir();
            if (fileNameText.isEmpty()) {
                fileNameText = "my-octogram-export";
            }

            File cacheFile = new File(cacheDir.getPath(), fileNameText + ".octoexport");
            if (cacheFile.exists()) {
                cacheFile.delete();
            }

            FileOutputStream fos = new FileOutputStream(cacheFile);
            if (BuildConfig.DEBUG) {
                fos.write(mainObject.toString(4).getBytes());
            } else {
                fos.write(mainObject.toString().getBytes());
            }
            fos.close();

            sharingFileName = cacheFile.getName();
            sharingFullLocation = cacheFile.getAbsolutePath();
            FileLoader instance = FileLoader.getInstance(UserConfig.selectedAccount);

            if (baseFragment == null) {
                long userID = UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId();
                TLRPC.Dialog dialog = MessagesController.getInstance(UserConfig.selectedAccount).getDialog(userID);
                sharingDids.append(userID, dialog);
                initUpdateReceiver();
                instance.uploadFile(cacheFile.getPath(), false, true, ConnectionsManager.FileTypeFile);
            } else {
                ShareAlert shAlert = new ShareAlert(baseFragment.getParentActivity(), null, null, false, null, false) {

                    @Override
                    protected void onSend(LongSparseArray<TLRPC.Dialog> dids, int count, TLRPC.TL_forumTopic topic) {
                        sharingDids = dids;

                        initUpdateReceiver();
                        instance.uploadFile(cacheFile.getPath(), false, true, ConnectionsManager.FileTypeFile);
                    }
                };
                baseFragment.showDialog(shAlert);
            }
        } catch (JSONException e) {
            OctoLogging.e(getClass().getName(), "Error sharing settings export", e);
        } catch (IOException e) {
            OctoLogging.e(getClass().getName(), "Error generating settings export", e);
        }
    }

    private void saveFileLocally(String fileNameText) {
        if (isFileNameInvalid(fileNameText)) {
            return;
        }

        if (fileNameText.isEmpty()) {
            fileNameText = "my-octogram-export";
        }

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, fileNameText+".octoexport");
        baseFragment.startActivityForResult(intent, CREATE_FILE_REQ);
    }

    private boolean isFileNameInvalid(String fileNameText) {
        if (fileNameText.contains("/") || fileNameText.length() > 40) {
            if (baseFragment != null) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(originalActivity);
                alertDialogBuilder.setTitle(LocaleController.getString(R.string.ImportReadyImportFailedZeroTitle));
                alertDialogBuilder.setMessage(LocaleController.getString(R.string.ImportReadyImportFailedZeroCaption));
                alertDialogBuilder.setPositiveButton("OK", null);
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
            return true;
        }

        return false;
    }

    public static JSONObject createOctoExport() {
        JSONObject mainObject = new JSONObject();

        for (Field field : OctoConfig.INSTANCE.getClass().getDeclaredFields()) {
            if (field.getType().equals(ConfigProperty.class)) {
                try {
                    ConfigProperty<?> configProperty = (ConfigProperty<?>) field.get(OctoConfig.INSTANCE);

                    if (configProperty != null) {
                        if (settingsScan.excludedOptions.contains(configProperty.getKey())) {
                            continue;
                        }

                        mainObject.put(configProperty.getKey(), configProperty.getValue());
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return mainObject;
    }

    private void initUpdateReceiver() {
        NotificationCenter.getInstance(UserConfig.selectedAccount).addObserver(this, NotificationCenter.fileUploaded);
        NotificationCenter.getInstance(UserConfig.selectedAccount).addObserver(this, NotificationCenter.fileUploadFailed);
    }

    private void stopUpdateReceiver() {
        NotificationCenter.getInstance(UserConfig.selectedAccount).removeObserver(this, NotificationCenter.fileUploaded);
        NotificationCenter.getInstance(UserConfig.selectedAccount).removeObserver(this, NotificationCenter.fileUploadFailed);
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.fileUploaded) {
            String location = (String) args[0];
            TLRPC.InputFile inputFile = (TLRPC.InputFile) args[1];

            if (inputFile == null) {
                return;
            }

            if (!Objects.equals(location, sharingFullLocation)) {
                return;
            }

            stopUpdateReceiver();

            if (baseFragment != null) {
                AndroidUtilities.runOnUIThread(() -> BulletinFactory.of(baseFragment).createSimpleBulletin(R.raw.forward, LocaleController.getString(R.string.ExportDataShareDone)).show());
            }

            TLRPC.TL_documentAttributeFilename attr = new TLRPC.TL_documentAttributeFilename();
            attr.file_name = sharingFileName;

            TLRPC.TL_inputMediaUploadedDocument inputMediaDocument = new TLRPC.TL_inputMediaUploadedDocument();
            inputMediaDocument.file = inputFile;
            inputMediaDocument.attributes.add(attr);
            inputMediaDocument.mime_type = OctoConfig.EXPORT_BACKUP_MIME_TYPE;

            StringBuilder baseInfo = new StringBuilder();
            baseInfo.append(LocaleController.getInstance().getFormatterFull().format(System.currentTimeMillis()));
            baseInfo.append("\n");
            baseInfo.append(LocaleController.getString(R.string.ExportDataShareFileComment));

            for (int i = 0; i < sharingDids.size(); i++) {
                TLRPC.TL_messages_sendMedia req = new TLRPC.TL_messages_sendMedia();
                req.peer = MessagesController.getInstance(currentAccount).getInputPeer(sharingDids.keyAt(i));
                req.random_id = SendMessagesHelper.getInstance(currentAccount).getNextRandomId();
                req.message = baseInfo.toString();
                req.silent = false;
                req.invert_media = true;
                req.media = inputMediaDocument;
                ConnectionsManager.getInstance(currentAccount).sendRequest(req, null);
            }

            if (onCompletedRunnable != null) {
                onCompletedRunnable.run();
            }
        } else if (id == NotificationCenter.fileUploadFailed) {
            String location = (String) args[0];

            if (!Objects.equals(location, sharingFullLocation)) {
                return;
            }

            stopUpdateReceiver();

            if (onFailedRunnable != null) {
                onFailedRunnable.run();
            }
        }
    }
}
