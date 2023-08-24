/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */

package it.octogram.android.preferences.ui;

import android.content.Context;
import android.media.AudioFormat;
import android.util.Pair;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.ArrayList;

import it.octogram.android.OctoConfig;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.rows.impl.HeaderRow;
import it.octogram.android.preferences.rows.impl.ListRow;
import it.octogram.android.preferences.rows.impl.SliderChooseRow;
import it.octogram.android.preferences.rows.impl.SwitchRow;
import it.octogram.android.preferences.ui.custom.AllowExperimentalBottomSheet;

public class OctoExperimentsUI implements PreferencesEntry {

    @Override
    public OctoPreferences getPreferences(BaseFragment fragment, Context context) {
        if (OctoConfig.INSTANCE.experimentsEnabled.getValue()) {
            if (!OctoConfig.INSTANCE.alternativeNavigation.getValue() &&
                    !OctoConfig.INSTANCE.uploadBoost.getValue() &&
                    !OctoConfig.INSTANCE.downloadBoost.getValue() &&
                    !OctoConfig.INSTANCE.mediaInGroupCall.getValue() &&
                    OctoConfig.INSTANCE.gcOutputType.getValue() == AudioFormat.CHANNEL_OUT_MONO &&
                    OctoConfig.INSTANCE.maxRecentStickers.getValue() == 20 &&
                    OctoConfig.INSTANCE.photoResolution.getValue() == OctoConfig.PhotoResolution.DEFAULT) {
                OctoConfig.INSTANCE.toggleBooleanSetting(OctoConfig.INSTANCE.experimentsEnabled);
            }
        }
        return OctoPreferences.builder(LocaleController.getString("Experiments", R.string.Experiments))
                .sticker(context, R.raw.utyan_experiments, true, LocaleController.formatString("OctoExperimentsSettingsHeader", R.string.OctoExperimentsSettingsHeader))
                .category(LocaleController.getString("ExperimentalSettings", R.string.ExperimentalSettings), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkExperimentsEnabled(fragment, context))
                            .preferenceValue(OctoConfig.INSTANCE.alternativeNavigation)
                            .title(LocaleController.getString("AlternativeNavigation", R.string.AlternativeNavigation))
                            .description(LocaleController.getString("AlternativeNavigation_Desc", R.string.AlternativeNavigation_Desc))
                            .requiresRestart(true)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkExperimentsEnabled(fragment, context))
                            .preferenceValue(OctoConfig.INSTANCE.mediaInGroupCall)
                            .title(LocaleController.getString(R.string.MediaStream))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .onClick(() -> checkExperimentsEnabled(fragment, context))
                            .options(new ArrayList<>() {{
                                add(new Pair<>(0, LocaleController.getString(R.string.AudioTypeMono)));
                                add(new Pair<>(1, LocaleController.getString(R.string.AudioTypeStereo)));
                            }})
                            .currentValue(OctoConfig.INSTANCE.gcOutputType)
                            .title(LocaleController.getString(R.string.AudioStereo))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .onClick(() -> checkExperimentsEnabled(fragment, context))
                            .options(new ArrayList<>() {{
                                add(new Pair<>(OctoConfig.PhotoResolution.LOW, LocaleController.getString(R.string.ResolutionLow)));
                                add(new Pair<>(OctoConfig.PhotoResolution.DEFAULT, LocaleController.getString(R.string.ResolutionMedium)));
                                add(new Pair<>(OctoConfig.PhotoResolution.HIGH, LocaleController.getString(R.string.ResolutionHigh)));
                            }})
                            .currentValue(OctoConfig.INSTANCE.photoResolution)
                            .title(LocaleController.getString("PhotoResolution", R.string.PhotoResolution))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.maxRecentStickers)
                            .options(new ArrayList<>() {{
                                add(new Pair<>(0, LocaleController.getString("MaxStickerSizeDefault", R.string.MaxStickerSizeDefault)));
                                add(new Pair<>(1, "30"));
                                add(new Pair<>(2, "40"));
                                add(new Pair<>(3, "50"));
                                add(new Pair<>(4, "80"));
                                add(new Pair<>(5, "100"));
                                add(new Pair<>(6, "120"));
                                add(new Pair<>(7, "150"));
                                add(new Pair<>(8, "180"));
                                add(new Pair<>(9, "200"));
                            }})
                            .title(LocaleController.getString("MaxRecentStickers", R.string.MaxRecentStickers))
                            .build());
                })
                .category("Upload & Download Boost", category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkExperimentsEnabled(fragment, context))
                            .preferenceValue(OctoConfig.INSTANCE.uploadBoost)
                            .title(LocaleController.getString("UploadBoost", R.string.UploadBoost))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkExperimentsEnabled(fragment, context))
                            .preferenceValue(OctoConfig.INSTANCE.downloadBoost)
                            .title(LocaleController.getString("DownloadBoost", R.string.DownloadBoost))
                            .build());
                    category.row(new HeaderRow(LocaleController.getString("DownloadBoostType", R.string.DownloadBoostType), OctoConfig.INSTANCE.downloadBoost));
                    category.row(new SliderChooseRow.SliderChooseRowBuilder()
                            .options(new ArrayList<>() {{
                                add(new Pair<>(0, LocaleController.getString("Default", R.string.Default)));
                                add(new Pair<>(1, LocaleController.getString("Fast", R.string.Fast)));
                                add(new Pair<>(2, LocaleController.getString("Extreme", R.string.Extreme)));
                            }})
                            .preferenceValue(OctoConfig.INSTANCE.downloadBoostValue)
                            .showIf(OctoConfig.INSTANCE.downloadBoost)
                            .build());
                })
                .build();
    }

    private boolean checkExperimentsEnabled(BaseFragment fragment, Context context) {
        if (OctoConfig.INSTANCE.experimentsEnabled.getValue())
            return true;

        // create OK/Cancel dialog
        /*AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(LocaleController.getString("OctoExperimentsDialogTitle", R.string.OctoExperimentsDialogTitle));
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), (dialog, which) -> {
            OctoConfig.INSTANCE.toggleBooleanSetting(OctoConfig.INSTANCE.experimentsEnabled);
        });
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), (dialog, which) -> {
            dialog.dismiss();
        });
        builder.setMessage(LocaleController.getString("OctoExperimentsDialogMessage", R.string.OctoExperimentsDialogMessage));
        fragment.showDialog(builder.create());
        */
        new AllowExperimentalBottomSheet(context).show();

        return OctoConfig.INSTANCE.experimentsEnabled.getValue();
    }

}
