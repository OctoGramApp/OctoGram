/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui;

import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.BulletinFactory;

import java.util.List;
import java.util.Locale;

import it.octogram.android.AutoDownloadUpdate;
import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;
import it.octogram.android.app.OctoPreferences;
import it.octogram.android.app.PreferencesEntry;
import it.octogram.android.app.fragment.PreferencesFragment;
import it.octogram.android.app.rows.impl.CustomCellRow;
import it.octogram.android.app.rows.impl.FooterInformativeRow;
import it.octogram.android.app.rows.impl.ListRow;
import it.octogram.android.app.rows.impl.ShadowRow;
import it.octogram.android.app.rows.impl.SwitchRow;
import it.octogram.android.app.ui.cells.CheckForUpdatesButtonCell;
import it.octogram.android.utils.OctoLogging;
import it.octogram.android.utils.UpdatesManager;
import it.octogram.android.utils.appearance.PopupChoiceDialogOption;
import it.octogram.android.utils.deeplink.DeepLinkDef;

public class OctoUpdatesUI implements PreferencesEntry {
    private CheckForUpdatesButtonCell checkCell;
    private PreferencesFragment fragment;
    private final static String TAG = "OctoUpdatesUI";

    @NonNull
    @Override
    public OctoPreferences getPreferences(@NonNull PreferencesFragment fragment, @NonNull Context context) {
        this.fragment = fragment;

        TLRPC.Chat pbetaChatInstance = UpdatesManager.INSTANCE.getPrivateBetaChatInstance();
        ConfigProperty<Boolean> isPbetaUser = new ConfigProperty<>(null, pbetaChatInstance != null);

        if (pbetaChatInstance == null && OctoConfig.INSTANCE.receivePBetaUpdates.getValue()) {
            OctoLogging.d(TAG, String.format(Locale.US, "%s: %s LINE: 62", isPbetaUser.getValue(), OctoConfig.INSTANCE.receivePBetaUpdates.getValue()));
            OctoConfig.INSTANCE.receivePBetaUpdates.updateValue(false);
        }

        OctoLogging.d(TAG, String.format(Locale.US, "%s %s: LINE:66", OctoConfig.INSTANCE.receivePBetaUpdates.getValue(), isPbetaUser.getValue()));

        OctoPreferences build = OctoPreferences.builder(getString(R.string.Updates))
                .deepLink(DeepLinkDef.UPDATE)
                .sticker(context, OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME, StickerUi.UPDATES, true, getString(R.string.UpdatesSettingsHeader))
                .row(new CustomCellRow.CustomCellRowBuilder()
                        .layout(checkCell = new CheckForUpdatesButtonCell(context))
                        .avoidReDraw(true)
                        .build())
                .row(new ShadowRow())
                .category(R.string.UpdatesOptions, category -> {
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.autoDownloadUpdatesStatus)
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(AutoDownloadUpdate.ALWAYS.getValue())
                                            .setItemTitle(getString(R.string.UpdatesSettingsAutoDownloadAlways))
                                            .setItemDescription(getString(R.string.UpdatesSettingsAutoDownloadAlwaysDesc)),
                                    new PopupChoiceDialogOption()
                                            .setId(AutoDownloadUpdate.ONLY_ON_WIFI.getValue())
                                            .setItemTitle(getString(R.string.UpdatesSettingsAutoDownloadWifi)),
                                    new PopupChoiceDialogOption()
                                            .setId(AutoDownloadUpdate.NEVER.getValue())
                                            .setItemTitle(getString(R.string.UpdatesSettingsAutoDownloadNever))
                            ))
                            .title(getString(R.string.UpdatesSettingsAutoDownload))
                            .build()
                    );
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.autoCheckUpdateStatus)
                            .title(getString(R.string.UpdatesSettingsAuto))
                            .showIf(OctoConfig.INSTANCE.receivePBetaUpdates, true)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> UpdatesManager.INSTANCE.checkForUpdates())
                            .preferenceValue(OctoConfig.INSTANCE.preferBetaVersion)
                            .title(getString(R.string.UpdatesSettingsBeta))
                            .showIf(OctoConfig.INSTANCE.receivePBetaUpdates, true)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> UpdatesManager.INSTANCE.checkForUpdates())
                            .preferenceValue(OctoConfig.INSTANCE.receivePBetaUpdates)
                            .title(getString(R.string.UpdatesSettingsPBeta))
                            .showIf(isPbetaUser)
                            .build());
                })
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(getString(R.string.UpdatesSettingsAutoDescription))
                        .showIf(isPbetaUser, true)
                        .build())
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(formatString(R.string.UpdatesSettingsPbetaDescription, getString(R.string.UpdatesSettingsAuto).toLowerCase()))
                        .showIf(isPbetaUser)
                        .build())
                .build();
        preUpdateUI();
        return build;
    }

    private void preUpdateUI() {
        UpdatesManager.INSTANCE.addCallback(new UpdatesManager.UpdatesManagerCallback() {
            @Override
            public void checkingForUpdates() {
                AndroidUtilities.runOnUIThread(() -> checkCell.updateState(CheckForUpdatesButtonCell.CheckCellState.CHECKING_UPDATES));
            }

            @Override
            public void onNoUpdateAvailable() {
                AndroidUtilities.runOnUIThread(() -> {
                    checkCell.updateState(CheckForUpdatesButtonCell.CheckCellState.NO_UPDATE_AVAILABLE);
                    BulletinFactory.of(fragment).createSimpleBulletin(R.raw.done, getString(R.string.UpdatesSettingsCheckUpdated)).show();
                });
            }

            @Override
            public void onUpdateAvailable(TLRPC.TL_help_appUpdate update) {
                AndroidUtilities.runOnUIThread(() -> checkCell.updateState(CheckForUpdatesButtonCell.CheckCellState.UPDATE_NEED_DOWNLOAD));
            }

            @Override
            public void onUpdateDownloading(float percent) {
                AndroidUtilities.runOnUIThread(() -> checkCell.updateState(CheckForUpdatesButtonCell.CheckCellState.UPDATE_IS_DOWNLOADING, percent));
            }

            @Override
            public void onUpdateReady() {
                AndroidUtilities.runOnUIThread(() -> checkCell.updateState(CheckForUpdatesButtonCell.CheckCellState.UPDATE_IS_READY));
            }
        });
    }
}
