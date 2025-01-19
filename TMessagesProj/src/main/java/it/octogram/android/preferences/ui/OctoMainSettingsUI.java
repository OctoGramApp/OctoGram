/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.provider.DocumentsContract;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.AlertDialog;
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
import java.util.UUID;

import it.octogram.android.ConfigProperty;
import it.octogram.android.NewFeaturesBadgeId;
import it.octogram.android.OctoConfig;
import it.octogram.android.logs.OctoLogging;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.rows.impl.FooterRow;
import it.octogram.android.preferences.rows.impl.TextDetailRow;
import it.octogram.android.preferences.rows.impl.TextIconRow;
import it.octogram.android.preferences.ui.custom.ExportDoneReadyBottomSheet;
import it.octogram.android.preferences.ui.custom.ImportSettingsBottomSheet;
import it.octogram.android.utils.AppRestartHelper;
import it.octogram.android.utils.LogsMigrator;
import it.octogram.android.utils.OctoUtils;

public class OctoMainSettingsUI implements PreferencesEntry, ChatAttachAlertDocumentLayout.DocumentSelectActivityDelegate {
    private final ConfigProperty<Boolean> logsOnlyPbeta = new ConfigProperty<>(null, false);
    private void updateConfigs() {
        logsOnlyPbeta.updateValue(BuildVars.DEBUG_PRIVATE_VERSION);
    }

    private PreferencesFragment fragment;
    private ChatAttachAlert chatAttachAlert;

    @Override
    public OctoPreferences getPreferences(PreferencesFragment fragment, Context context) {
        updateConfigs();
        this.fragment = fragment;
        var footer = AndroidUtilities.replaceTags(formatString(R.string.OctoMainSettingsFooter, BuildConfig.BUILD_VERSION_STRING));
        return OctoPreferences.builder(getString(R.string.OctoGramSettings))
                .octoAnimation(getString(R.string.OctoMainSettingsHeader))
                .category(getString(R.string.Settings), category -> {
                    if ("it.octogram.android.beta".equals(ApplicationLoader.applicationContext.getPackageName())) {
                        category.row(new TextDetailRow.TextDetailRowBuilder()
                                .onClick(() -> {
                                    throw new RuntimeException("Test crash");
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
                            .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoTranslatorUI())))
                            .icon(R.drawable.msg_translate)
                            .title(getString(R.string.Translator))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoAppearanceUI())))
                            .icon(R.drawable.settings_appearance)
                            .isNew(NewFeaturesBadgeId.APPEARANCE_BADGE.getId())
                            .title(getString(R.string.Appearance))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoCameraSettingsUI())))
                            .icon(R.drawable.msg_camera)
                            .title(getString(R.string.ChatCamera))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoExperimentsUI())))
                            .icon(R.drawable.outline_science_white)
                            .title(getString(R.string.Experiments))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoUpdatesUI())))
                            .icon(R.drawable.round_update_white_28)
                            .title(getString(R.string.Updates))
                            .build());
                })
                .category(getString(R.string.OctoMainSettingsManageCategory), category -> {
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> openExportSettingsProcedure(fragment, context))
                            .icon(R.drawable.msg_saved)
                            .title(getString(R.string.Export))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> openImportSettingsProcedure(fragment, context))
                            .icon(R.drawable.msg_customize)
                            .title(getString(R.string.ImportReady))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> openResetSettingsProcedure(context))
                            .icon(R.drawable.msg_reset)
                            .title(getString(R.string.ResetSettings))
                            .build());
                })
                .category(getString(R.string.OctoMainSettingsInfoCategory), category -> {
                    category.row(new TextDetailRow.TextDetailRowBuilder()
                            .onClick(() -> fragment.presentFragment(new DatacenterActivity()))
                            .icon(R.drawable.datacenter_status)
                            .title(getString(R.string.DatacenterStatus))
                            .description(getString(R.string.DatacenterStatus_Desc))
                            .build());
                    category.row(new TextDetailRow.TextDetailRowBuilder()
                            .onClick(() -> {
                                LogsMigrator.migrateOldLogs();
                                fragment.presentFragment(new CrashesActivity());
                            })
                            .icon(R.drawable.msg_secret_hw)
                            .title(getString(R.string.CrashHistory))
                            .description(getString(R.string.CrashHistory_Desc))
                            .build());
                    category.row(new TextDetailRow.TextDetailRowBuilder()
                            .onClick(() -> fragment.presentFragment(new OctoLogsActivity()))
                            .icon(R.drawable.msg_log)
                            .title(LocaleController.getString(R.string.CrashHistory)+ " (PBETA)")
                            .description(LocaleController.getString(R.string.CrashHistory_Desc))
                            .showIf(logsOnlyPbeta)
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> Browser.openUrl(LaunchActivity.instance, Uri.parse(String.format("https://%s/privacy", OctoUtils.getDomain()))))
                            .icon(R.drawable.msg2_policy)
                            .title(getString(R.string.OctoPrivacyPolicy))
                            .build());
                    category.row(new TextDetailRow.TextDetailRowBuilder()
                            .onClick(() -> Browser.openUrl(LaunchActivity.instance, Uri.parse(String.format("https://%s/translate", OctoUtils.getDomain()))))
                            .icon(R.drawable.msg_translate)
                            .title(getString(R.string.TranslateOcto))
                            .description(getString(R.string.TranslateOcto_Desc))
                            .build());
                })
                .row(new FooterRow.FooterRowBuilder().title(footer).build())
                .build();
    }

    private void openExportSettingsProcedure(PreferencesFragment fragment, Context context) {
        var bottomSheet = new ExportDoneReadyBottomSheet(context, fragment.getParentActivity(), fragment);
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
        if (firstFile.getName().endsWith(".octoexport") && OctoConfig.isValidExport(firstFile)) {
            ImportSettingsBottomSheet sheet = new ImportSettingsBottomSheet(fragment, firstFile);
            sheet.setOriginalActivity(fragment.getParentActivity());
            sheet.show();
        } else {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(fragment.getParentActivity());
            alertDialogBuilder.setTitle(LocaleController.getString(R.string.ImportReadyImportFailedZeroTitle));
            alertDialogBuilder.setMessage(LocaleController.getString(R.string.ImportReadyImportFailedDataCaption));
            alertDialogBuilder.setPositiveButton("OK", null);
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

            if (firstFile.getAbsolutePath().startsWith(AndroidUtilities.getCacheDir().getAbsolutePath())) {
                firstFile.delete();
            }
        }
    }

    @Override
    public void didSelectFiles(ArrayList<String> files, String caption, ArrayList<MessageObject> fMessages, boolean notify, int scheduleDate, long effectId, boolean invertMedia) {
        File firstFile = new File(files.get(0));

        if (chatAttachAlert != null) {
            chatAttachAlert = null;
        }

        if (!firstFile.exists() || !firstFile.isFile()) {
            return;
        }

        startImportActivity(firstFile);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ExportDoneReadyBottomSheet.CREATE_FILE_REQ && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    OutputStream outputStream = ApplicationLoader.applicationContext.getContentResolver().openOutputStream(uri);
                    if (outputStream != null) {
                        outputStream.write(ExportDoneReadyBottomSheet.createOctoExport().toString().getBytes());
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
                String tempFile = UUID.randomUUID().toString().replace("-", "") + ".octoexport";
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

    private void openResetSettingsProcedure(Context context) {
        openResetSettingsProcedure(context, false);
    }

    public static void openResetSettingsProcedure(Context context, boolean resetExperimentalSettings) {
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
        warningBuilder.setMessage(getString(resetExperimentalSettings ? R.string.ResetSettingsDescriptionExperimental : R.string.ResetSettingsDescription));
        warningBuilder.setView(frameLayout).setCustomViewOffset(9);
        warningBuilder.setPositiveButton(getString(R.string.ResetSettingsButton), (dialog1, which1) -> {
            AlertDialog progressDialog = new AlertDialog(LaunchActivity.instance, AlertDialog.ALERT_TYPE_SPINNER);
            progressDialog.setCanCancel(false);
            progressDialog.show();

            if (saveBackup[0]) {
                ExportDoneReadyBottomSheet instance = new ExportDoneReadyBottomSheet(context, null, null);
                instance.setOnCompletedRunnable(() -> completeReset(context, resetExperimentalSettings));
                instance.setOnFailedRunnable(() -> {
                    progressDialog.dismiss();
                    warningBuilder.getDismissRunnable().run();

                    AlertDialog.Builder errorBuilder = new AlertDialog.Builder(context);
                    errorBuilder.setTitle(LocaleController.getString(R.string.Warning));
                    errorBuilder.setPositiveButton(LocaleController.getString(R.string.OK), null);
                    errorBuilder.setMessage(LocaleController.getString(R.string.ResetSettingsBackupFailed));
                    AlertDialog alertDialog = errorBuilder.create();
                    alertDialog.show();
                });
                instance.shareExport("");

                return;
            }

            completeReset(context, resetExperimentalSettings);
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

    private static void completeReset(Context context, boolean resetExperimentalSettings) {
        if (resetExperimentalSettings) {
            OctoExperimentsUI.resetSettings();
        } else {
            OctoConfig.INSTANCE.resetConfig();
        }

        AppRestartHelper.triggerRebirth(context, new Intent(context, LaunchActivity.class));
    }
}
