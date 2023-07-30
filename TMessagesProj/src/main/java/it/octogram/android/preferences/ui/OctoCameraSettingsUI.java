/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */

package it.octogram.android.preferences.ui;

import android.content.Context;

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
        return OctoPreferences.builder("Camera Settings")
                .sticker(context, R.raw.utyan_camera, true, "Here you can customize the camera settings within the app.")
                .category("CameraX", category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.cameraXEnabled)
                            .title("Enable CameraX")
                            .description("Use CameraX instead of the legacy camera API")
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.cameraXZeroShutter)
                            .title("Zero shutter lag")
                            .description("Enable zero shutter lag for CameraX")
                            .showIf(OctoConfig.INSTANCE.cameraXEnabled)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.cameraXPerfOverQuality)
                            .title("Performance mode")
                            .description("Enable performance mode for CameraX. This will reduce the quality of the photos taken.")
                            .showIf(OctoConfig.INSTANCE.cameraXEnabled)
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .value(OctoConfig.INSTANCE.cameraXResolution.getValue() + "p")
                            .title("Current CameraX Resolution")
                            .showIf(OctoConfig.INSTANCE.cameraXEnabled)
                            .build());
                })
                .build();
    }

}
