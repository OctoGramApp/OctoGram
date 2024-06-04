/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

package it.octogram.android.preferences.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.LaunchActivity;

import java.io.File;

import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.rows.impl.CustomCellRow;
import it.octogram.android.preferences.rows.impl.FooterInformativeRow;
import it.octogram.android.preferences.rows.impl.SwitchRow;
import it.octogram.android.preferences.rows.impl.TextIconRow;
import it.octogram.android.preferences.ui.components.CustomUpdatesCheckCell;
import it.octogram.android.utils.UpdatesManager;

public class OctoUpdatesUI implements PreferencesEntry {
    private CustomUpdatesCheckCell checkCell;

    @Override
    public OctoPreferences getPreferences(PreferencesFragment fragment, Context context) {
        TLRPC.Chat pbetaChatInstance = UpdatesManager.getPrivateBetaChatInstance();
        ConfigProperty<Boolean> isPbetaUser = new ConfigProperty<>(null, pbetaChatInstance != null);

        if (pbetaChatInstance == null && OctoConfig.INSTANCE.receivePBetaUpdates.getValue()) {
            OctoConfig.INSTANCE.receivePBetaUpdates.updateValue(false);
        }

        OctoPreferences build = OctoPreferences.builder(LocaleController.formatString("Updates", R.string.Updates))
                .sticker(context, R.raw.updates, true, LocaleController.formatString("UpdatesSettingsHeader", R.string.UpdatesSettingsHeader))
                .row(new CustomCellRow.CustomCellRowBuilder()
                        .layout(checkCell = new CustomUpdatesCheckCell(context, () -> checkForUpdates(fragment, context)))
                        .build())
                .row(new SwitchRow.SwitchRowBuilder()
                        .preferenceValue(OctoConfig.INSTANCE.autoCheckUpdates)
                        .title(LocaleController.formatString("UpdatesSettingsAuto", R.string.UpdatesSettingsAuto))
                        .showIf(OctoConfig.INSTANCE.receivePBetaUpdates, true)
                        .build())
                .row(new SwitchRow.SwitchRowBuilder()
                        .onClick(() -> {
                            AndroidUtilities.runOnUIThread(() -> checkForUpdates(fragment, context, true), 300);
                            return true;
                        })
                        .preferenceValue(OctoConfig.INSTANCE.preferBetaVersion)
                        .title(LocaleController.formatString("UpdatesSettingsBeta", R.string.UpdatesSettingsBeta))
                        .showIf(OctoConfig.INSTANCE.receivePBetaUpdates, true)
                        .build())
                .row(new SwitchRow.SwitchRowBuilder()
                        .onClick(() -> {
                            AndroidUtilities.runOnUIThread(() -> checkForUpdates(fragment, context, true), 300);
                            return true;
                        })
                        .preferenceValue(OctoConfig.INSTANCE.receivePBetaUpdates)
                        .title(LocaleController.formatString("UpdatesSettingsPBeta", R.string.UpdatesSettingsPBeta))
                        .showIf(isPbetaUser)
                        .build())
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(LocaleController.formatString("UpdatesSettingsAutoDescription", R.string.UpdatesSettingsAutoDescription))
                        .showIf(isPbetaUser, true)
                        .build())
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(LocaleController.formatString("UpdatesSettingsPbetaDescription", R.string.UpdatesSettingsPbetaDescription, LocaleController.getString(R.string.UpdatesSettingsAuto)))
                        .showIf(isPbetaUser)
                        .build())
                .category(LocaleController.formatString("UpdatesSettingsLinks", R.string.UpdatesSettingsLinks), category -> {
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> MessagesController.getInstance(fragment.getCurrentAccount()).openByUserName("OctoGramApp", fragment, 1))
                            .value("@OctoGramApp")
                            .icon(R.drawable.msg_channel)
                            .title(LocaleController.formatString("OfficialChannel", R.string.OfficialChannel))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("https://github.com/OctoGramApp/OctoGram/tree/%s", BuildConfig.GIT_COMMIT_HASH)));
                                fragment.getParentActivity().startActivity(browserIntent);
                            })
                            .value(BuildConfig.GIT_COMMIT_HASH)
                            .icon(R.drawable.outline_source_white_28)
                            .title(LocaleController.formatString("SourceCode", R.string.SourceCode))
                            .build());
                })
                .build();
        preUpdateUI();
        return build;
    }

    private void preUpdateUI() {
        if (SharedConfig.isAppUpdateAvailable()) {
            String fileName = FileLoader.getAttachFileName(SharedConfig.pendingAppUpdate.document);
            File path = FileLoader.getInstance(0).getPathToAttach(SharedConfig.pendingAppUpdate.document, true);
            if (path.exists()) {
                checkCell.updateState(CustomUpdatesCheckCell.CheckCellState.UPDATE_IS_READY);
            } else {
                boolean isDownloading = FileLoader.getInstance(0).isLoadingFile(fileName);
                if (isDownloading) {
                    Float p = ImageLoader.getInstance().getFileProgress(fileName);
                    checkCell.updateState(CustomUpdatesCheckCell.CheckCellState.UPDATE_IS_DOWNLOADING, p != null ? p : 0.0f);
                } else {
                    checkCell.updateState(CustomUpdatesCheckCell.CheckCellState.UPDATE_NEED_DOWNLOAD);
                }
            }
        }
    }

    private void checkForUpdates(PreferencesFragment fragment, Context context, boolean ignoreTelegramCache) {
        if (SharedConfig.isAppUpdateAvailable() && !ignoreTelegramCache) {
            preUpdateUI();

            int currentState = checkCell.getCurrentState();
            switch (currentState) {
                case CustomUpdatesCheckCell.CheckCellState.UPDATE_NEED_DOWNLOAD:
                    LaunchActivity.instance.handleNewUpdate(SharedConfig.pendingAppUpdate, true);
                break;
                case CustomUpdatesCheckCell.CheckCellState.UPDATE_IS_READY:
                    AndroidUtilities.openForView(SharedConfig.pendingAppUpdate.document, true, fragment.getParentActivity());
            }

            return;
        }

        checkCell.updateState(CustomUpdatesCheckCell.CheckCellState.CHECKING_UPDATES);

        UpdatesManager.isUpdateAvailable(new UpdatesManager.UpdatesManagerCheckInterface() {
            @Override
            public void onThereIsUpdate(JSONObject updateData) {
                if (updateData == null) {
                    return;
                }

                AndroidUtilities.runOnUIThread(() -> UpdatesManager.getTLRPCUpdateFromObject(updateData, update -> {
                    LaunchActivity.instance.handleNewUpdate(update, true);
                    preUpdateUI();
                }));
            }

            @Override
            public void onThereIsUpdate(TLRPC.TL_help_appUpdate update) {
                AndroidUtilities.runOnUIThread(() -> {
                    LaunchActivity.instance.handleNewUpdate(update, true);
                    preUpdateUI();
                });
            }

            @Override
            public void onNoUpdate() {
                AndroidUtilities.runOnUIThread(() -> {
                    LaunchActivity.instance.resetUpdateInstance();
                    checkCell.updateState(CustomUpdatesCheckCell.CheckCellState.NO_UPDATE_AVAILABLE);
                    BulletinFactory.of(fragment).createSimpleBulletin(R.raw.done, LocaleController.formatString("UpdatesSettingsCheckUpdated", R.string.UpdatesSettingsCheckUpdated)).show();
                });
            }

            @Override
            public void onError() {
                AndroidUtilities.runOnUIThread(() -> {
                    checkCell.updateState(CustomUpdatesCheckCell.CheckCellState.NO_UPDATE_AVAILABLE);
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    alertDialogBuilder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                    alertDialogBuilder.setMessage(LocaleController.getString("UpdatesSettingsCheckFailed", R.string.UpdatesSettingsCheckFailed));
                    alertDialogBuilder.setPositiveButton("OK", null);
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                });
            }
        });
    }

    private void checkForUpdates(PreferencesFragment fragment, Context context) {
        checkForUpdates(fragment, context, false);
    }
}
