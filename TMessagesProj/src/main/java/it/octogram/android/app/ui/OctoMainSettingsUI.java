/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.Components.ChatAttachAlert;
import org.telegram.ui.Components.ChatAttachAlertDocumentLayout;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.LaunchActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;

import it.octogram.android.ConfigProperty;
import it.octogram.android.NewFeaturesBadgeIds;
import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;
import it.octogram.android.StoreUtils;
import it.octogram.android.app.OctoPreferences;
import it.octogram.android.app.PreferencesEntry;
import it.octogram.android.app.fragment.PreferencesFragment;
import it.octogram.android.app.rows.impl.TextDetailRow;
import it.octogram.android.app.rows.impl.TextIconRow;
import it.octogram.android.app.ui.bottomsheets.ExportDoneReadyBottomSheet;
import it.octogram.android.utils.AppRestartHelper;
import it.octogram.android.utils.OctoLogging;
import it.octogram.android.utils.OctoUtils;
import it.octogram.android.utils.chat.FileShareHelper;
import it.octogram.android.utils.deeplink.DeepLinkDef;

/**
 * @noinspection SequencedCollectionMethodCanBeUsed
 */
public class OctoMainSettingsUI implements PreferencesEntry, ChatAttachAlertDocumentLayout.DocumentSelectActivityDelegate {
    private PreferencesFragment fragment;
    private ChatAttachAlert chatAttachAlert;

    @NonNull
    @Override
    public OctoPreferences getPreferences(@NonNull PreferencesFragment fragment, @NonNull Context context) {
        this.fragment = fragment;

        ConfigProperty<Boolean> logsOnlyPbeta = new ConfigProperty<>(null, BuildVars.DEBUG_PRIVATE_VERSION);
        ConfigProperty<Boolean> canShowUpdatesPolicy = new ConfigProperty<>(null, !StoreUtils.isDownloadedFromAnyStore() || StoreUtils.isFromPlayStore());

        return OctoPreferences.builder(getString(R.string.OctoGramSettings))
                .deepLink(DeepLinkDef.OCTOSETTINGS)
                .addContextMenuItem(new OctoPreferences.OctoContextMenuElement(R.drawable.msg_saved, getString(R.string.Export), () -> openExportSettingsProcedure(fragment, context)))
                .addContextMenuItem(new OctoPreferences.OctoContextMenuElement(R.drawable.msg_customize, getString(R.string.ImportReady), () -> openImportSettingsProcedure(fragment, context)))
                .addContextMenuItem(new OctoPreferences.OctoContextMenuElement(R.drawable.msg_reset, getString(R.string.ResetSettings), () -> openResetSettingsProcedure(fragment, context)).asDanger())
                .sticker(context, OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME, StickerUi.MAIN_SETTINGS, true, getString(R.string.OctoMainSettingsHeader))
                .category(getString(R.string.Settings), category -> {
                    if (BuildConfig.DEBUG) {
                        category.row(new TextDetailRow.TextDetailRowBuilder()
                                .onClick(() -> {
                                    try {
                                        throw new IllegalArgumentException("Inner cause");
                                    } catch (Exception e) {
                                        throw new RuntimeException("Test crash", e);
                                    }
                                })
                                .icon(R.drawable.msg_cancel)
                                .title("Crash the app")
                                .description("Yup, this is literally a crash button")
                                .build());
                        category.row(new TextDetailRow.TextDetailRowBuilder()
                                .onClick(() -> {
                                    OctoConfig.INSTANCE.updateSignalingCommitID.updateValue("z");
                                    OctoConfig.INSTANCE.updateSignalingChangelog.updateValue("update test");
                                    AppRestartHelper.triggerRebirth(context, new Intent(context, LaunchActivity.class));
                                })
                                .icon(R.drawable.round_update_white_28)
                                .title("Test app signaling")
                                .description("Check update message")
                                .build());
                    }
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoGeneralSettingsUI())))
                            .icon(R.drawable.msg_media)
                            .title(getString(R.string.General))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoAppearanceUI())))
                            .icon(R.drawable.settings_appearance)
                            .isNew(NewFeaturesBadgeIds.APPEARANCE)
                            .title(getString(R.string.Appearance))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoChatsUI())))
                            .icon(R.drawable.msg2_discussion)
                            .isNew(NewFeaturesBadgeIds.CHATS)
                            .title(getString(R.string.ChatTitle))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoCameraSettingsUI())))
                            .icon(R.drawable.msg_camera)
                            .title(getString(R.string.ChatCamera))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoPrivacySettingsUI())))
                            .icon(R.drawable.menu_privacy)
                            .isNew(NewFeaturesBadgeIds.PRIVACY_MAIN)
                            .title(getString(R.string.PrivacySettings))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoExperimentsUI())))
                            .icon(R.drawable.outline_science_white)
                            .title(getString(R.string.Experiments))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> {
                                if (StoreUtils.isFromPlayStore()) {
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Utilities.uriParseSafe("https://play.google.com/store/apps/details?id=" + ApplicationLoader.applicationContext.getPackageName()));
                                    context.startActivity(browserIntent);
                                } else {
                                    fragment.presentFragment(new PreferencesFragment(new OctoUpdatesUI()));
                                }
                            })
                            .icon(R.drawable.round_update_white_28)
                            .showIf(canShowUpdatesPolicy)
                            .title(getString(R.string.Updates))
                            .build());
                })
                .category(getString(R.string.OctoMainSettingsInfo), category -> {
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new OctoDcStatusActivity()))
                            .icon(R.drawable.datacenter_status)
                            .title(getString(R.string.DatacenterStatus))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoInfoSettingsUI())))
                            .icon(R.drawable.msg_info)
                            .title(getString(R.string.OctoInfoSettingsHeader))
                            .build());
                })
                .build();
    }

    private void openExportSettingsProcedure(PreferencesFragment fragment, Context context) {
        var bottomSheet = new ExportDoneReadyBottomSheet(context, fragment);
        bottomSheet.show();
    }

    private void openImportSettingsProcedure(PreferencesFragment fragment, Context context) {
        chatAttachAlert = new ChatAttachAlert(context, fragment, false, false);
        chatAttachAlert.setExportPicker();
        chatAttachAlert.init();
        chatAttachAlert.show();
        chatAttachAlert.setDocumentsDelegate(this);
    }

    public void startDocumentSelectActivity() {
        try {
            Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
            photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
            photoPickerIntent.setType("application/*");
            fragment.startActivityForResult(photoPickerIntent, 21);
        } catch (Exception e) {
            OctoLogging.e(e);
        }
    }

    private void startImportActivity(File firstFile) {
        if (firstFile.getName().endsWith(OctoConfig.OCTOEXPORT_EXTENSION) && OctoConfig.isValidExport(firstFile)) {
            OctoImportSettingsUI ui = new OctoImportSettingsUI();
            ui.setData(null, firstFile);
            fragment.presentFragment(ui);
        } else {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(fragment.getParentActivity());
            alertDialogBuilder.setTitle(getString(R.string.ImportReadyImportFailedZeroTitle));
            alertDialogBuilder.setMessage(getString(R.string.ImportReadyImportFailedDataCaption));
            alertDialogBuilder.setPositiveButton("OK", null);
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

            if (firstFile.getAbsolutePath().startsWith(AndroidUtilities.getCacheDir().getAbsolutePath())) {
                if (!firstFile.delete()) {
                    OctoLogging.e("Failed to delete file: " + firstFile.getAbsolutePath());
                }
            }
        }
    }

    @Override
    public void didSelectFiles(ArrayList<String> files, String caption, ArrayList<MessageObject> fMessages, boolean notify, int scheduleDate, long effectId, boolean invertMedia, long payStars) {
        File firstFile = new File(files.get(0));

        if (chatAttachAlert != null) {
            chatAttachAlert = null;
        }

        if (!firstFile.exists() || !firstFile.isFile()) {
            return;
        }

        startImportActivity(firstFile);
    }

    static int saveDataStateCache = ExportDoneReadyBottomSheet.SaveDataState.SAVE_EVERYTHING;

    public static void setSaveDataStateCache(int saveDataStateCache1) {
        saveDataStateCache = saveDataStateCache1;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ExportDoneReadyBottomSheet.CREATE_FILE_REQ && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    OutputStream outputStream = ApplicationLoader.applicationContext.getContentResolver().openOutputStream(uri);
                    if (outputStream != null) {
                        outputStream.write(ExportDoneReadyBottomSheet.createOctoExport(saveDataStateCache).toString().getBytes());
                        outputStream.close();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if (requestCode == 21 && data != null) {
            if (chatAttachAlert != null) {
                chatAttachAlert.dismiss();
                chatAttachAlert = null;
            }

            Uri uri = data.getData();
            if (uri != null) {
                File cacheDir = AndroidUtilities.getCacheDir();
                String tempFile = OctoUtils.generateRandomString().replace("-", "") + OctoConfig.OCTOEXPORT_EXTENSION;
                File file = new File(cacheDir.getPath(), tempFile);
                try {
                    final InputStream inputStream = ApplicationLoader.applicationContext.getContentResolver().openInputStream(uri);
                    if (inputStream != null) {
                        OutputStream outputStream = new FileOutputStream(file);
                        final byte[] buffer = new byte[4 * 1024];
                        int read;
                        while ((read = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, read);
                        }
                        inputStream.close();
                        outputStream.flush();
                        outputStream.close();
                        startImportActivity(file);
                    }
                } catch (IOException e) {
                    OctoLogging.e(e);
                }
            }
        }
    }

    private void openResetSettingsProcedure(BaseFragment fragment, Context context) {
        boolean[] saveBackup = {false};

        FrameLayout frameLayout = new FrameLayout(context);
        CheckBoxCell checkbox = new CheckBoxCell(context, 1, null);
        checkbox.setBackground(Theme.getSelectorDrawable(false));
        checkbox.setText(getString(R.string.ResetSettingsBackup), "", false, false);
        checkbox.setPadding(LocaleController.isRTL ? dp(16) : dp(8), 0, LocaleController.isRTL ? dp(8) : dp(16), 0);
        frameLayout.addView(checkbox, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.TOP | Gravity.LEFT, 0, 0, 0, 0));
        checkbox.setOnClickListener(v -> {
            CheckBoxCell cell1 = (CheckBoxCell) v;
            saveBackup[0] = !saveBackup[0];
            cell1.setChecked(saveBackup[0], true);
        });

        AlertDialog.Builder warningBuilder = new AlertDialog.Builder(context);
        warningBuilder.setTitle(getString(R.string.ResetSettingsTitle));
        warningBuilder.setMessage(getString(R.string.ResetSettingsDescription));
        warningBuilder.setView(frameLayout).setCustomViewOffset(9);
        warningBuilder.setPositiveButton(getString(R.string.ResetSettingsButton), (dialog1, which1) -> {
            AlertDialog progressDialog = new AlertDialog(LaunchActivity.instance, AlertDialog.ALERT_TYPE_SPINNER);
            progressDialog.setCanCancel(false);
            progressDialog.show();

            if (saveBackup[0]) {
                FileShareHelper.FileShareData data = new FileShareHelper.FileShareData();
                data.fileName = "default-config";
                data.fileExtension = OctoConfig.OCTOEXPORT_EXTENSION;
                data.fileContent = ExportDoneReadyBottomSheet.createOctoExport(ExportDoneReadyBottomSheet.SaveDataState.SAVE_EVERYTHING);
                data.fragment = fragment;
                data.shareToSavedMessages = true;
                data.caption = getString(R.string.ExportDataShareFileComment);
                data.delegate = new FileShareHelper.FileShareData.FileShareDelegate() {
                    @Override
                    public void onSuccess() {
                        completeReset(context);
                    }

                    @Override
                    public void onFailed() {
                        progressDialog.dismiss();
                        warningBuilder.getDismissRunnable().run();

                        AlertDialog.Builder errorBuilder = new AlertDialog.Builder(context);
                        errorBuilder.setTitle(getString(R.string.Warning));
                        errorBuilder.setPositiveButton(getString(R.string.OK), null);
                        errorBuilder.setMessage(getString(R.string.ResetSettingsBackupFailed));
                        AlertDialog alertDialog = errorBuilder.create();
                        alertDialog.show();
                    }
                };
                FileShareHelper.init(data);
                return;
            }

            completeReset(context);
        });
        warningBuilder.setNeutralButton(getString(R.string.Cancel), null);
        AlertDialog dialog = warningBuilder.create();

        dialog.setOnShowListener(dialog1 -> {
            TextView positiveButton = (TextView) dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            String defaultText = (String) positiveButton.getText();
            positiveButton.setEnabled(false);

            new CountDownTimer(5000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    int currentSeconds = (int) millisUntilFinished / 1000 + 1;
                    positiveButton.setText(String.format(Locale.getDefault(), "%s (%d)", defaultText, currentSeconds));
                }

                @Override
                public void onFinish() {
                    positiveButton.setText(defaultText);
                    positiveButton.setEnabled(true);
                }
            }.start();
        });

        dialog.show();
        dialog.redPositive();
    }

    private static void completeReset(Context context) {
        OctoConfig.INSTANCE.resetConfig();
        AppRestartHelper.triggerRebirth(context, new Intent(context, LaunchActivity.class));
    }
}
