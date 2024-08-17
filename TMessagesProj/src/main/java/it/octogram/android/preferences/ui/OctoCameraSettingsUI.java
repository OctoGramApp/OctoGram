/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

package it.octogram.android.preferences.ui;

import android.content.Context;
import android.util.Size;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import it.octogram.android.CameraType;
import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;
import it.octogram.android.camerax.CameraXUtils;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.rows.impl.CustomCellRow;
import it.octogram.android.preferences.rows.impl.ListRow;
import it.octogram.android.preferences.rows.impl.SwitchRow;
import it.octogram.android.preferences.ui.custom.CameraTypeSelector;
import it.octogram.android.utils.PopupChoiceDialogOption;

public class OctoCameraSettingsUI implements PreferencesEntry {

//    public static String getCameraInfo(boolean isSettingsTitle) {
//        return switch (CameraType.Companion.fromInt(OctoConfig.INSTANCE.cameraType.getValue())) {
//            case SYSTEM_CAMERA -> isSettingsTitle ?
//                    LocaleController.getString(R.string.SystemCameraDesc) :
//                    LocaleController.getString(R.string.CameraTypeSystem);
//            case CAMERA_X -> isSettingsTitle ?
//                    LocaleController.getString(R.string.CameraXDesc) :
//                    LocaleController.getString(R.string.CameraTypeX);
//            case CAMERA_2 -> isSettingsTitle ?
//                    LocaleController.getString(R.string.Camera2Desc) :
//                    LocaleController.getString(R.string.CameraType2);
//            case TELEGRAM -> isSettingsTitle ?
//                    LocaleController.getString(R.string.DefaultCameraDesc) :
//                    LocaleController.getString(R.string.CameraTypeDefault);
//        };
//    }

    public static List<PopupChoiceDialogOption> getCameraResOptions() {
        var availableSizes = CameraXUtils.getAvailableVideoSizes();
        var sortedHeights = availableSizes
                .values().stream()
                .sorted(Comparator.comparingInt(Size::getWidth).reversed())
                .map(Size::getHeight);
        return sortedHeights
                .distinct()
                .map(height -> new PopupChoiceDialogOption()
                        .setId(height)
                        .setItemTitle(height + "p")
                )
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public OctoPreferences getPreferences(PreferencesFragment fragment, Context context) {
        ConfigProperty<Boolean> cameraXEnabled = new ConfigProperty<>(null, OctoConfig.INSTANCE.cameraType.getValue() == CameraType.CAMERA_X.getValue());

        return OctoPreferences.builder(LocaleController.formatString("OctoCameraSettings", R.string.OctoCameraSettings))
                .sticker(context, OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME, StickerUi.CAMERA, true, LocaleController.formatString("OctoCameraSettingsHeader", R.string.OctoCameraSettingsHeader))
                .category("Camera Type", category -> {
                    category.row(new CustomCellRow.CustomCellRowBuilder()
                            .layout(new CameraTypeSelector(context) {
                                @Override
                                protected void onSelectedCamera(int cameraSelected) {
                                    if (cameraSelected == OctoConfig.INSTANCE.cameraType.getValue()) {
                                        return;
                                    }

                                    OctoConfig.INSTANCE.cameraType.updateValue(cameraSelected);
                                    cameraXEnabled.updateValue(cameraSelected == CameraType.CAMERA_X.getValue());
                                    fragment.reloadUIAfterValueUpdate();
                                }
                            })
                            .postNotificationName(NotificationCenter.mainUserInfoChanged, NotificationCenter.reloadInterface)
                            .build());
//                    category.row(new FooterRow.FooterRowBuilder()
//                            .onClick(fragment::reloadUIAfterValueUpdate)
//                            .title(String.format("%s", OctoUtils.getUrlNoUnderlineText(new SpannableString(OctoUtils.fromHtml((getCameraInfo(true)))))))
//                            .postNotificationName(NotificationCenter.mainUserInfoChanged, NotificationCenter.reloadInterface)
//                            .build());
                })
                .category("Camera Option", cameraXEnabled, category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.cameraXZeroShutter)
                            .title(LocaleController.getString("ZeroShutter", R.string.ZeroShutter))
                            .description(LocaleController.getString("ZeroShutter_Desc", R.string.ZeroShutter_Desc))
                            .showIf(cameraXEnabled)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.cameraXPerformanceMode)
                            .title(LocaleController.getString("PerformanceMode", R.string.PerformanceMode))
                            .description(LocaleController.getString("PerformanceMode_Desc", R.string.PerformanceMode_Desc))
                            .showIf(cameraXEnabled)
                            .build());

                    var resolutionOptions = getCameraResOptions();
                    if (!resolutionOptions.isEmpty()) {
                        category.row(new ListRow.ListRowBuilder()
                                .options(resolutionOptions)
                                .currentValue(OctoConfig.INSTANCE.cameraXResolution)
                                .showIf(cameraXEnabled)
                                .title(LocaleController.getString("CurrentCameraXResolution", R.string.CurrentCameraXResolution))
                                .build());
                    }
                })
                .category(LocaleController.getString("TranslatorCategoryOptions", R.string.TranslatorCategoryOptions), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.startWithRearCamera)
                            .title(LocaleController.formatString("StartWithRearCamera", R.string.StartWithRearCamera))
                            .description(LocaleController.formatString("StartWithRearCamera_Desc", R.string.StartWithRearCamera_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.disableCameraPreview)
                            .title(LocaleController.formatString("DisableCameraPreview", R.string.DisableCameraPreview))
                            .description(LocaleController.formatString("DisableCameraPreview_Desc", R.string.DisableCameraPreview_Desc))
                            .build());
                })
                .build();
    }
}
