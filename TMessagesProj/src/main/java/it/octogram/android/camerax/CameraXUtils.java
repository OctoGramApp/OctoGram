/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.camerax;

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
import org.telegram.messenger.SharedConfig;

import java.util.ArrayList;
import java.util.Arrays;
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
import it.octogram.android.logs.OctoLogging;

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
        OctoLogging.d("CameraXUtils", "isWideAngleAvailable" + getWideCameraId(provider));
        return getWideCameraId(provider) != null;
    }

    @OptIn(markerClass = ExperimentalCamera2Interop.class)
    public static CameraSelector getDefaultWideAngleCamera(ProcessCameraProvider provider) {
        final String tag = "CameraUtils";
        OctoLogging.d(tag, "getDefaultWideAngleCamera invoked.");

        String wideCamera = getWideCameraId(provider);
        OctoLogging.d(tag, "Wide camera ID retrieved: " + (wideCamera != null ? wideCamera : "null"));

        if (wideCamera != null) {
            return new CameraSelector.Builder().addCameraFilter(cameraInfo -> {
                List<CameraInfo> cameraFiltered = new ArrayList<>();
                OctoLogging.d(tag, "Applying filter to available cameras.");

                for (int i = 0; i < cameraInfo.size(); i++) {
                    CameraInfo c = cameraInfo.get(i);
                    String id = Camera2CameraInfo.from(c).getCameraId();
                    OctoLogging.d(tag, "Checking camera with ID: " + id);

                    if (id.equals(wideCamera)) {
                        OctoLogging.d(tag, "Wide-angle camera found: " + id);
                        cameraFiltered.add(c);
                    }
                }

                OctoLogging.d(tag, "Number of filtered cameras: " + cameraFiltered.size());
                return cameraFiltered;
            }).build();
        }

        String errorMessage = "This device doesn't support a wide-angle camera! "
                + "Ensure isWideAngleAvailable is checked before calling getDefaultWideAngleCamera.";
        OctoLogging.d(tag, errorMessage);
        throw new IllegalArgumentException(errorMessage);
    }

    public static Map<Quality, Size> getAvailableVideoSizes() throws IllegalStateException {
        if (qualityException != null) {
            throw new IllegalStateException("CameraX sizes failed to load!", qualityException);
        }
        return qualityToSize == null ? new HashMap<>() : qualityToSize;
    }

    public static void loadCameraXSizes() {
        final String tag = "CameraUtils";
        OctoLogging.d(tag, "loadCameraXSizes invoked.");

        if (qualityToSize != null || qualityException != null) {
            OctoLogging.d(tag, "Sizes already loaded or an exception occurred previously.");
            return;
        }

        Context context = ApplicationLoader.applicationContext;
        ListenableFuture<ProcessCameraProvider> providerFtr = ProcessCameraProvider.getInstance(context);

        OctoLogging.d(tag, "Fetching ProcessCameraProvider instance.");

        providerFtr.addListener(() -> {
            ProcessCameraProvider provider = null;
            try {
                OctoLogging.d(tag, "Camera provider future listener invoked.");

                CameraSelector.Builder cameraBuilder = new CameraSelector.Builder();
                provider = providerFtr.get();
                OctoLogging.d(tag, "Camera provider obtained successfully.");

                CameraSelector camera = cameraBuilder.build();
                OctoLogging.d(tag, "CameraSelector built successfully.");

                qualityToSize = getAvailableVideoSizes(camera, provider);
                OctoLogging.d(tag, "Video sizes loaded successfully: " + qualityToSize);

                loadSuggestedResolution();
                OctoLogging.d(tag, "Suggested resolutions loaded.");
            } catch (Exception e) {
                qualityException = e;
                OctoLogging.d(tag, "Exception occurred while loading camera sizes: " + e.getMessage());
            } finally {
                if (provider != null) {
                    provider.unbindAll();
                    OctoLogging.d(tag, "Camera provider unbound.");
                }
            }
        }, ContextCompat.getMainExecutor(context));

        OctoLogging.d(tag, "Added listener for ProcessCameraProvider.");
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
            OctoLogging.d("CameraXUtils", String.format(Locale.ROOT,"getResolutionFromConfig: %d, enumToValue: %d", current, CameraXResolution.INSTANCE.enumToValue(CameraXResolution.HD)));
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
        final String tag = "CameraUtils";
        OctoLogging.d(tag, "getWideCameraId invoked.");

        float lowestAngledCamera = Integer.MAX_VALUE;
        List<CameraInfo> cameraInfoList = provider.getAvailableCameraInfos();
        OctoLogging.d(tag, "Number of available camera infos: " + cameraInfoList.size());

        String cameraId = null;
        int availableBackCamera = 0;
        boolean foundWideAngleOnPrimaryCamera = false;

        for (int i = 0; i < cameraInfoList.size(); i++) {
            CameraInfo cameraInfo = cameraInfoList.get(i);
            String id = Camera2CameraInfo.from(cameraInfo).getCameraId();
            OctoLogging.d(tag, "Processing camera ID: " + id);
            try {
                CameraCharacteristics cameraCharacteristics = Camera2CameraInfo.from(cameraInfo).getCameraCharacteristicsMap().get(id);
                if (cameraCharacteristics != null) {
                    Integer lensFacing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                    if (lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                        availableBackCamera++;
                        OctoLogging.d(tag, "Found a back-facing camera. Total: " + availableBackCamera);

                        ZoomState zoomState = cameraInfo.getZoomState().getValue();
                        if (zoomState != null) {
                            OctoLogging.d(tag, "Zoom state for camera ID " + id + ": MinZoomRatio = " + zoomState.getMinZoomRatio());
                            if (zoomState.getMinZoomRatio() < 1.0F && zoomState.getMinZoomRatio() > 0) {
                                foundWideAngleOnPrimaryCamera = true;
                                OctoLogging.d(tag, "Wide-angle capability detected on primary back camera: " + id);
                            }
                        }

                        float[] listLensAngle = cameraCharacteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
                        if (listLensAngle != null && listLensAngle.length > 0) {
                            OctoLogging.d(tag, "Focal lengths for camera ID " + id + ": " + Arrays.toString(listLensAngle));
                            if (listLensAngle[0] < 3.0f && listLensAngle[0] < lowestAngledCamera) {
                                lowestAngledCamera = listLensAngle[0];
                                cameraId = id;
                                OctoLogging.d(tag, "New lowest angled camera ID: " + cameraId + " with angle " + lowestAngledCamera);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                OctoLogging.e(tag, "Error processing camera ID: " + id, e);
            }
        }

        OctoLogging.d(tag, "Total back-facing cameras found: " + availableBackCamera);
        if (availableBackCamera >= 2 && !foundWideAngleOnPrimaryCamera) {
            OctoLogging.d(tag, "Returning wide-angle camera ID: " + cameraId);
            return cameraId;
        } else {
            OctoLogging.d(tag, "No wide-angle camera available. Returning null.");
            return null;
        }
    }
}
