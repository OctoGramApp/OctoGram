/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */

package it.octogram.android.preferences.ui;

import android.content.Context;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;

import it.octogram.android.OctoConfig;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.rows.impl.SwitchRow;
import it.octogram.android.preferences.rows.impl.TextIconRow;

public class OctoCameraSettingsUI implements PreferencesEntry {

    @Override
    public OctoPreferences getPreferences(BaseFragment fragment, Context context) {
        return OctoPreferences.builder(LocaleController.formatString("OctoCameraSettings", R.string.OctoCameraSettings))
                .sticker(context, R.raw.utyan_camera, true, LocaleController.formatString("OctoCameraSettingsHeader", R.string.OctoCameraSettingsHeader))
                .category("CameraX", category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.cameraXEnabled)
                            .title(LocaleController.getString("UseCameraX", R.string.UseCameraX))
                            .description(LocaleController.getString("UseCameraX_Desc", R.string.UseCameraX_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.cameraXZeroShutter)
                            .title(LocaleController.getString("ZeroShutter", R.string.ZeroShutter))
                            .description(LocaleController.getString("ZeroShutter_Desc", R.string.ZeroShutter_Desc))
                            .showIf(OctoConfig.INSTANCE.cameraXEnabled)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.cameraXPerfOverQuality)
                            .title(LocaleController.getString("PerformanceMode", R.string.PerformanceMode))
                            .description(LocaleController.getString("PerformanceMode_Desc", R.string.PerformanceMode_Desc))
                            .showIf(OctoConfig.INSTANCE.cameraXEnabled)
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .value(OctoConfig.INSTANCE.cameraXResolution.getValue() + "p")
                            .title(LocaleController.getString("CurrentCameraXResolution", R.string.CurrentCameraXResolution))
                            .showIf(OctoConfig.INSTANCE.cameraXEnabled)
                            .build());
                })
                .build();
    }

}
