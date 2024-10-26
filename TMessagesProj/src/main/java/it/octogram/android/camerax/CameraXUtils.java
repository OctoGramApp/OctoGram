/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

package it.octogram.android.camerax;

import static android.hardware.camera2.CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS;
import static android.hardware.camera2.CameraMetadata.LENS_FACING_BACK;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.util.Log;
import android.util.Size;

import androidx.annotation.OptIn;
import androidx.camera.camera2.interop.Camera2CameraInfo;
import androidx.camera.camera2.interop.ExperimentalCamera2Interop;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ZoomState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.SharedConfig;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import it.octogram.android.CameraXResolution;
import it.octogram.android.OctoConfig;

public class CameraXUtils {

    private static Map<Quality, Size> qualityToSize;
    private static Exception qualityException;
    private static int cameraResolution = -1;

    public static boolean isCameraXSupported() {
        return SharedConfig.getDevicePerformanceClass() >= SharedConfig.PERFORMANCE_CLASS_AVERAGE;
    }

    public static int getDefault() {
        return (isCameraXSupported() && SharedConfig.getDevicePerformanceClass() == SharedConfig.PERFORMANCE_CLASS_HIGH) ? 1 : 0;
    }

    public static boolean isWideAngleAvailable(ProcessCameraProvider provider) {
        return getWideCameraId(provider) != null;
    }

    @OptIn(markerClass = ExperimentalCamera2Interop.class)
    public static CameraSelector getDefaultWideAngleCamera(ProcessCameraProvider provider) {
        String wideCamera = getWideCameraId(provider);
        if (wideCamera != null) {
            return new CameraSelector.Builder().addCameraFilter(cameraInfo -> {
                List<CameraInfo> cameraFiltered = new ArrayList<>();
                for (int i = 0; i < cameraInfo.size(); i++) {
                    CameraInfo c = cameraInfo.get(i);
                    String id = Camera2CameraInfo.from(c).getCameraId();
                    if (id.equals(wideCamera)) {
                        cameraFiltered.add(c);
                    }
                }
                return cameraFiltered;
            }).build();
        }
        throw new IllegalArgumentException("This device doesn't support wide camera! "
                + "isWideAngleAvailable should be checked first before calling "
                + "getDefaultWideAngleCamera.");
    }

    public static Map<Quality, Size> getAvailableVideoSizes() throws IllegalStateException {
        if (qualityException != null) {
            throw new IllegalStateException("CameraX sizes failed to load!", qualityException);
        }
        return qualityToSize == null ? new HashMap<>() : qualityToSize;
    }

    public static void loadCameraXSizes() {
        if (qualityToSize != null || qualityException != null) {
            return;
        }
        Context context = ApplicationLoader.applicationContext;
        ListenableFuture<ProcessCameraProvider> providerFtr = ProcessCameraProvider.getInstance(context);
        providerFtr.addListener(() -> {
            ProcessCameraProvider provider = null;
            try {
                CameraSelector.Builder cameraBuilder = new CameraSelector.Builder();
                provider = providerFtr.get();
                CameraSelector camera = cameraBuilder.build();
                qualityToSize = getAvailableVideoSizes(camera, provider);
                loadSuggestedResolution();
            } catch (Exception e) {
                qualityException = e;
            } finally {
                if (provider != null) {
                    provider.unbindAll();
                }
            }
        }, ContextCompat.getMainExecutor(context));
    }

    private static Map<Quality, Size> getAvailableVideoSizes(CameraSelector cameraSelector, ProcessCameraProvider provider) {
        //noinspection deprecation
        return cameraSelector.filter(provider.getAvailableCameraInfos()).stream()
                .findFirst()
                .map(camInfo ->
                                QualitySelector.getSupportedQualities(camInfo).stream().collect(
                                Collectors.toMap(
                                        Function.identity(),
                                        quality -> Optional.ofNullable(QualitySelector.getResolution(camInfo, quality))
                                                .orElse(new Size(0, 0))
                                )
                        )
                ).orElse(new HashMap<>());
    }

    public static void loadSuggestedResolution() {
        int suggestedRes = getSuggestedResolution(false);
        Map<Quality, Size> sizes = getAvailableVideoSizes();

        int min = sizes.values().stream()
                .mapToInt(Size::getHeight)
                .min().orElse(0);

        int max = sizes.values().stream()
                .mapToInt(Size::getHeight)
                .max().orElse(0);

        getAvailableVideoSizes().values().stream()
                .sorted(Comparator.comparingInt(Size::getHeight).reversed())
                .mapToInt(Size::getHeight)
                .filter(height -> height <= suggestedRes)
                .findFirst()
                .ifPresent(height -> {
                    cameraResolution = height;
                    int current = getResolutionFromConfig();
                    if (current == -1 || current > max || current < min) {
                        OctoConfig.INSTANCE.cameraXResolution.updateValue(height);
                    }
                });
    }

    public static int getCameraResolution() {
        return cameraResolution;
    }

    private static int getResolutionFromConfig() {
        int current = OctoConfig.INSTANCE.cameraXResolution.getValue();
        if (BuildVars.DEBUG_PRIVATE_VERSION) {
            Log.d("CameraXUtils", String.format(Locale.ROOT,"getResolutionFromConfig: %d, enumToValue: %d", current, CameraXResolution.INSTANCE.enumToValue(CameraXResolution.HD)));
        }
        if (current == CameraXResolution.INSTANCE.enumToValue(CameraXResolution.SD)) {
            return 480;
        } else if (current == CameraXResolution.INSTANCE.enumToValue(CameraXResolution.HD)) {
            return 720;
        } else if (current == CameraXResolution.INSTANCE.enumToValue(CameraXResolution.FHD)) {
            return 1080;
        } else if (current == CameraXResolution.INSTANCE.enumToValue(CameraXResolution.UHD)) {
            return 2160;
        } else {
            return -1;
        }
    }
    public static Size getPreviewBestSize() {
        int suggestedRes = getSuggestedResolution(true);
        return getAvailableVideoSizes().values().stream()
                .filter(size -> size.getHeight() <= getResolutionFromConfig() && size.getHeight() <= suggestedRes)
                .max(Comparator.comparingInt(Size::getHeight))
                .orElse(new Size(0, 0));
    }

    public static Quality getVideoQuality() {
        return getAvailableVideoSizes().entrySet().stream()
                .filter(entry -> entry.getValue().getHeight() == OctoConfig.INSTANCE.cameraXResolution.getValue())
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(Quality.HIGHEST);
    }

    private static int getSuggestedResolution(boolean isPreview) {
        return switch (SharedConfig.getDevicePerformanceClass()) {
            case SharedConfig.PERFORMANCE_CLASS_LOW -> 720;
            case SharedConfig.PERFORMANCE_CLASS_AVERAGE -> 1080;
            case SharedConfig.PERFORMANCE_CLASS_HIGH -> 2160;
            default -> OctoConfig.INSTANCE.cameraXZeroShutter.getValue() && isPreview ? 1080 : 2160;
        };
    }

    @OptIn(markerClass = ExperimentalCamera2Interop.class)
    @SuppressLint("RestrictedApi")
    public static String getWideCameraId(ProcessCameraProvider provider) {
        float lowestAngledCamera = Integer.MAX_VALUE;
        List<CameraInfo> cameraInfoList = provider.getAvailableCameraInfos();
        String cameraId = null;
        int availableBackCamera = 0;
        boolean foundWideAngleOnPrimaryCamera = false;
        for (int i = 0; i < cameraInfoList.size(); i++) {
            CameraInfo cameraInfo = cameraInfoList.get(i);
            String id = Camera2CameraInfo.from(cameraInfo).getCameraId();
            try {
                CameraCharacteristics cameraCharacteristics = Camera2CameraInfo.from(cameraInfo).getCameraCharacteristicsMap().get(id);
                if (cameraCharacteristics != null) {
                    var lensFacing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                    if (lensFacing != null && lensFacing == LENS_FACING_BACK) {
                        availableBackCamera++;
                        ZoomState zoomState = cameraInfo.getZoomState().getValue();
                        if (zoomState != null && zoomState.getMinZoomRatio() < 1.0F && zoomState.getMinZoomRatio() > 0) {
                            foundWideAngleOnPrimaryCamera = true;
                        }
                        float[] listLensAngle = cameraCharacteristics.get(LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
                        if (listLensAngle != null && listLensAngle.length > 0) {
                            if (listLensAngle[0] < 3.0f && listLensAngle[0] < lowestAngledCamera) {
                                lowestAngledCamera = listLensAngle[0];
                                cameraId = id;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
        return availableBackCamera >= 2 && !foundWideAngleOnPrimaryCamera ? cameraId : null;
    }
}
