/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.text.SpannableString;
import android.util.Size;

import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import it.octogram.android.CameraPreview;
import it.octogram.android.CameraType;
import it.octogram.android.CameraXResolution;
import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;
import it.octogram.android.camerax.CameraXController;
import it.octogram.android.camerax.CameraXUtils;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.rows.impl.CustomCellRow;
import it.octogram.android.preferences.rows.impl.FooterInformativeRow;
import it.octogram.android.preferences.rows.impl.ListRow;
import it.octogram.android.preferences.rows.impl.SwitchRow;
import it.octogram.android.preferences.ui.custom.CameraTypeSelector;
import it.octogram.android.utils.MessageStringHelper;
import it.octogram.android.utils.PopupChoiceDialogOption;

public class OctoCameraSettingsUI implements PreferencesEntry {
    private final ConfigProperty<Boolean> usingSystemCamera = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> usingCameraX = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> usingCamera2 = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> usingTelegramCamera = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> isZSLAvailable = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> isCameraXSupportedAndZSLSupported = new ConfigProperty<>(null, false);

    public static List<PopupChoiceDialogOption> getCameraResOptions() {
        var availableSizes = CameraXUtils.getAvailableVideoSizes();
        var sortedHeights = availableSizes
                .values().stream()
                .sorted(Comparator.comparingInt(Size::getWidth).reversed())
                .map(Size::getHeight);
        return sortedHeights
                .distinct()
                .map(height -> new PopupChoiceDialogOption()
                        .setId(CameraXResolution.INSTANCE.getCameraXResolution(height))
                        .setItemTitle(height + "p")
                )
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static CharSequence getCameraDesc(CameraType cameraType) {
        String advise;
        switch (cameraType) {
            case SYSTEM_CAMERA -> advise = getString(R.string.SystemCameraDesc);
            case CAMERA_X -> advise = getString(R.string.CameraXDesc);
            case CAMERA_2 -> advise = getString(R.string.Camera2Desc);
            default -> advise = getString(R.string.DefaultCameraDesc);
        }
        var htmlParsed = new SpannableString(MessageStringHelper.fromHtml(advise));
        return MessageStringHelper.getUrlNoUnderlineText(htmlParsed);
    }

    @Override
    public OctoPreferences getPreferences(PreferencesFragment fragment, Context context) {
        updateConfigs();
        return OctoPreferences.builder(getString(R.string.OctoCameraSettings))
                .sticker(context, OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME, StickerUi.CAMERA, true, getString(R.string.OctoCameraSettingsHeader))
                .category(R.string.CameraType, category -> category.row(new CustomCellRow.CustomCellRowBuilder()
                        .layout(new CameraTypeSelector(context) {
                            @Override
                            protected void onSelectedCamera(int cameraSelected) {
                                if (cameraSelected == OctoConfig.INSTANCE.cameraType.getValue()) {
                                    return;
                                }

                                OctoConfig.INSTANCE.cameraType.updateValue(cameraSelected);
                                updateConfigs();
                                fragment.reloadUIAfterValueUpdate();
                            }
                        })
                        .postNotificationName(NotificationCenter.mainUserInfoChanged, NotificationCenter.reloadInterface)
                        .build()))
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(getCameraDesc(CameraType.TELEGRAM))
                        .showIf(usingTelegramCamera)
                        .build())
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(getCameraDesc(CameraType.CAMERA_2))
                        .showIf(usingCamera2)
                        .build())
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(getCameraDesc(CameraType.CAMERA_X))
                        .showIf(usingCameraX)
                        .build())
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(getCameraDesc(CameraType.SYSTEM_CAMERA))
                        .showIf(usingSystemCamera)
                        .build())
                .category(getString(R.string.CameraOption), usingCameraX, category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.cameraXZeroShutter)
                            .title(getString(R.string.ZeroShutter))
                            .description(getString(R.string.ZeroShutter_Desc))
                            .showIf(isCameraXSupportedAndZSLSupported)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.cameraXPerformanceMode)
                            .title(getString(R.string.PerformanceMode))
                            .description(getString(R.string.PerformanceMode_Desc))
                            .showIf(usingCameraX)
                            .build());

                    var resolutionOptions = getCameraResOptions();
                    if (!resolutionOptions.isEmpty()) {
                        // TODO: Fix capture resolution
                        category.row(new ListRow.ListRowBuilder()
                                .options(resolutionOptions)
                                .currentValue(OctoConfig.INSTANCE.cameraXResolution)
                                .showIf(usingCameraX)
                                .title(getString(R.string.CurrentCameraXResolution))
                                .build());
                    }
                })
                .category(getString(R.string.TranslatorCategoryOptions), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.startWithRearCamera)
                            .title(getString(R.string.StartWithRearCamera))
                            .description(getString(R.string.StartWithRearCamera_Desc))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.cameraPreview)
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(CameraPreview.DEFAULT)
                                            .setItemTitle(R.string.CameraButtonPosition_Default),
                                    new PopupChoiceDialogOption()
                                            .setId(CameraPreview.BOTTOM_BAR)
                                            .setItemTitle(R.string.CameraButtonPosition_BottomBar))
                            )
                            .title(R.string.CameraButtonPosition)
                            .description(R.string.CameraButtonPosition_Desc)
                            .build());
                })
                .build();
    }

    private void updateConfigs() {
        int cameraType = OctoConfig.INSTANCE.cameraType.getValue();

        usingSystemCamera.setValue(cameraType == CameraType.SYSTEM_CAMERA.getValue());
        usingCameraX.setValue(cameraType == CameraType.CAMERA_X.getValue());
        usingCamera2.setValue(cameraType == CameraType.CAMERA_2.getValue());
        usingTelegramCamera.setValue(cameraType == CameraType.TELEGRAM.getValue());

        isZSLAvailable.setValue(CameraXController.isZSLSupported());
        isCameraXSupportedAndZSLSupported.setValue(usingCameraX.getValue() ? isZSLAvailable.getValue() : isCameraXSupportedAndZSLSupported.getValue());

        if (!isZSLAvailable.getValue()) {
            OctoConfig.INSTANCE.cameraXZeroShutter.updateValue(false);
        }
    }
}
