/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

package it.octogram.android.preferences.ui;

import android.content.Context;
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
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.LaunchActivity;

import java.io.File;
import java.util.List;

import it.octogram.android.AutoDownloadUpdate;
import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.rows.impl.CustomCellRow;
import it.octogram.android.preferences.rows.impl.FooterInformativeRow;
import it.octogram.android.preferences.rows.impl.ListRow;
import it.octogram.android.preferences.rows.impl.ShadowRow;
import it.octogram.android.preferences.rows.impl.SwitchRow;
import it.octogram.android.preferences.rows.impl.TextIconRow;
import it.octogram.android.preferences.ui.components.CustomUpdatesCheckCell;
import it.octogram.android.utils.PopupChoiceDialogOption;
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
                .sticker(context, OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME, StickerUi.UPDATES, true, LocaleController.formatString(R.string.UpdatesSettingsHeader))
                .row(new CustomCellRow.CustomCellRowBuilder()
                        .layout(checkCell = new CustomUpdatesCheckCell(context, () -> checkForUpdates(fragment, context)))
                        .build())
                .row(new ShadowRow())
                .category("Options", category -> {
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.autoDownloadUpdatesStatus)
                            .onSelected(this::checkAutoDownloadState)
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(AutoDownloadUpdate.ALWAYS.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.UpdatesSettingsAutoDownloadAlways))
                                            .setItemDescription(LocaleController.formatString(R.string.UpdatesSettingsAutoDownloadAlwaysDesc)),
                                    new PopupChoiceDialogOption()
                                            .setId(AutoDownloadUpdate.ONLY_ON_WIFI.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.UpdatesSettingsAutoDownloadWifi)),
                                    new PopupChoiceDialogOption()
                                            .setId(AutoDownloadUpdate.NEVER.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.UpdatesSettingsAutoDownloadNever))
                            ))
                            .title(LocaleController.getString(R.string.UpdatesSettingsAutoDownload))
                            .build()
                    );
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.autoCheckUpdateStatus)
                            .title(LocaleController.formatString(R.string.UpdatesSettingsAuto))
                            .showIf(OctoConfig.INSTANCE.receivePBetaUpdates, true)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> checkForUpdates(fragment, context, true))
                            .preferenceValue(OctoConfig.INSTANCE.preferBetaVersion)
                            .title(LocaleController.formatString(R.string.UpdatesSettingsBeta))
                            .showIf(OctoConfig.INSTANCE.receivePBetaUpdates, true)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> checkForUpdates(fragment, context, true))
                            .preferenceValue(OctoConfig.INSTANCE.receivePBetaUpdates)
                            .title(LocaleController.formatString(R.string.UpdatesSettingsPBeta))
                            .showIf(isPbetaUser)
                            .build());
                })
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(LocaleController.formatString(R.string.UpdatesSettingsAutoDescription))
                        .showIf(isPbetaUser, true)
                        .build())
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(LocaleController.formatString(R.string.UpdatesSettingsPbetaDescription, LocaleController.getString(R.string.UpdatesSettingsAuto)))
                        .showIf(isPbetaUser)
                        .build())
                .category(LocaleController.formatString(R.string.UpdatesSettingsLinks), category -> {
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> MessagesController.getInstance(fragment.getCurrentAccount()).openByUserName("OctoGramApp", fragment, 1))
                            .value("@OctoGramApp")
                            .icon(R.drawable.msg_channel)
                            .title(LocaleController.formatString(R.string.OfficialChannel))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> Browser.openUrl(LaunchActivity.instance, Uri.parse(String.format("https://github.com/OctoGramApp/OctoGram/tree/%s", BuildConfig.GIT_COMMIT_HASH))))
                            .value(BuildConfig.GIT_COMMIT_HASH)
                            .icon(R.drawable.outline_source_white_28)
                            .title(LocaleController.formatString(R.string.SourceCode))
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
                AndroidUtilities.runOnUIThread(() -> checkCell.updateState(CustomUpdatesCheckCell.CheckCellState.UPDATE_IS_READY));
            } else {
                boolean isDownloading = FileLoader.getInstance(0).isLoadingFile(fileName);
                if (isDownloading) {
                    Float p = ImageLoader.getInstance().getFileProgress(fileName);
                    AndroidUtilities.runOnUIThread(() -> checkCell.updateState(CustomUpdatesCheckCell.CheckCellState.UPDATE_IS_DOWNLOADING, p != null ? p : 0.0f));
                } else {
                    AndroidUtilities.runOnUIThread(() -> checkCell.updateState(CustomUpdatesCheckCell.CheckCellState.UPDATE_NEED_DOWNLOAD));
                }
            }
        }
    }

    private void checkAutoDownloadState() {
        if (UpdatesManager.canAutoDownloadUpdates() && SharedConfig.isAppUpdateAvailable()) {
            preUpdateUI();

            if (checkCell.getCurrentState() == CustomUpdatesCheckCell.CheckCellState.UPDATE_NEED_DOWNLOAD) {
                LaunchActivity.instance.handleNewUpdate(SharedConfig.pendingAppUpdate, true);
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
                    UpdatesManager.installUpdate();
            }

            return;
        }

        AndroidUtilities.runOnUIThread(() -> checkCell.updateState(CustomUpdatesCheckCell.CheckCellState.CHECKING_UPDATES));

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
                    alertDialogBuilder.setTitle(LocaleController.getString(R.string.AppName));
                    alertDialogBuilder.setMessage(LocaleController.getString(R.string.UpdatesSettingsCheckFailed));
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
