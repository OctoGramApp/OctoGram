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
import android.os.Handler;
import android.os.Looper;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.camera2.interop.Camera2CameraInfo;
import androidx.camera.camera2.interop.ExperimentalCamera2Interop;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ZoomState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.VideoCapture;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.SharedConfig;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import it.octogram.android.CameraXResolution;
import it.octogram.android.OctoConfig;
import it.octogram.android.utils.OctoLogging;

public class CameraXUtils {
    private static final String TAG = "CameraXUtils";
    private static volatile Map<Quality, Size> qualityToSize;
    private static volatile Exception qualityException;
    private static int cameraResolution = -1;

    public static boolean isCameraXSupported() {
        return SharedConfig.getDevicePerformanceClass() >= SharedConfig.PERFORMANCE_CLASS_AVERAGE;
    }

    public static int getDefault() {
        return (isCameraXSupported() && SharedConfig.getDevicePerformanceClass() == SharedConfig.PERFORMANCE_CLASS_HIGH) ? 1 : 0;
    }

    public static boolean isWideAngleAvailable(ProcessCameraProvider provider) {
        OctoLogging.d(TAG, "isWideAngleAvailable" + getWideCameraId(provider));
        return getWideCameraId(provider) != null;
    }

    @OptIn(markerClass = ExperimentalCamera2Interop.class)
    @NonNull
    public static CameraSelector getDefaultWideAngleCamera(@NonNull ProcessCameraProvider provider) {
        OctoLogging.d(TAG, "getDefaultWideAngleCamera invoked.");

        String wideCameraId = getWideCameraId(provider);
        OctoLogging.d(TAG, "Wide camera ID retrieved: " + (wideCameraId != null ? wideCameraId : "null"));

        if (wideCameraId != null) {
            return new CameraSelector.Builder()
                    .addCameraFilter(cameraInfoList -> {
                        OctoLogging.d(TAG, "Applying CameraIdFilter for wide-angle camera.");
                        for (CameraInfo cameraInfo : cameraInfoList) {
                            String id = Camera2CameraInfo.from(cameraInfo).getCameraId();
                            if (id.equals(wideCameraId)) {
                                OctoLogging.d(TAG, "Wide-angle camera found with ID: " + id);
                                return Collections.singletonList(cameraInfo);
                            }
                            OctoLogging.d(TAG, "Camera ID " + id + " is not the wide-angle camera.");
                        }
                        OctoLogging.d(TAG, "No wide-angle camera found in the filter (should not happen if getWideCameraId is correct).");
                        return Collections.emptyList();
                    })
                    .build();
        } else {
            throw new IllegalArgumentException("This device doesn't support wide camera! "
                    + "isWideAngleAvailable should be checked first before calling "
                    + "getDefaultWideAngleCamera.");
        }
    }

    public static Map<Quality, Size> getAvailableVideoSizes() throws IllegalStateException {
        if (qualityException != null) {
            throw new IllegalStateException("CameraX sizes failed to load!", qualityException);
        }
        return qualityToSize == null ? new HashMap<>() : qualityToSize;
    }


    public static void loadCameraXSizes() {
        OctoLogging.d(TAG, "loadCameraXSizes invoked.");

        if (qualityToSize != null) {
            OctoLogging.d(TAG, "CameraX sizes already loaded.");
            return;
        }
        if (qualityException != null) {
            OctoLogging.d(TAG, "CameraX sizes loading skipped due to previous exception.");
            return;
        }

        Context context = ApplicationLoader.applicationContext;
        ListenableFuture<ProcessCameraProvider> providerFuture = ProcessCameraProvider.getInstance(context);

        OctoLogging.d(TAG, "Fetching ProcessCameraProvider instance.");

        Executor backgroundExecutor = Executors.newSingleThreadExecutor();
        Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        providerFuture.addListener(() -> {
            try {
                ProcessCameraProvider provider = providerFuture.get();
                OctoLogging.d(TAG, "Camera provider obtained successfully.");

                backgroundExecutor.execute(() -> {
                    try {
                        CameraSelector cameraSelector = new CameraSelector.Builder().build();
                        OctoLogging.d(TAG, "CameraSelector built successfully on background thread.");

                        Map<Quality, Size> loadedSizes = getAvailableVideoSizes(cameraSelector, provider);
                        OctoLogging.d(TAG, "Video sizes loaded successfully on background thread: " + loadedSizes);

                        mainThreadHandler.post(() -> {
                            qualityToSize = loadedSizes;
                            loadSuggestedResolution();
                            OctoLogging.d(TAG, "Updated qualityToSize and loaded suggested resolution on main thread.");
                        });

                    } catch (Exception e) {
                        mainThreadHandler.post(() -> {
                            qualityException = e;
                            OctoLogging.e(TAG, "Unexpected exception occurred while loading camera sizes in background.", e);
                        });
                    }
                });

            } catch (ExecutionException | InterruptedException e) {
                qualityException = e;
                OctoLogging.e(TAG, "Exception getting CameraProvider.", e);
            }
        }, ContextCompat.getMainExecutor(context));

        OctoLogging.d(TAG, "Added listener for ProcessCameraProvider.");
    }

    @SuppressLint("RestrictedApi")
    private static Map<Quality, Size> getAvailableVideoSizes(CameraSelector cameraSelector, ProcessCameraProvider provider) {
        return new HashMap<>(provider.getAvailableCameraInfos().stream()
                .filter(cameraInfo -> cameraSelector.filter(List.of(cameraInfo)).contains(cameraInfo))
                .findFirst()
                .map(cameraInfo -> QualitySelector.getQualityToResolutionMap(
                        Recorder.getVideoCapabilities(cameraInfo),
                        VideoCapture.withOutput(new Recorder.Builder().build()).getDynamicRange())
                )
                .orElse(new HashMap<>()));
    }

    public static void loadSuggestedResolution() {
        OctoLogging.d(TAG, "loadSuggestedResolution: Starting to load suggested resolution.");
        int suggestedRes = getSuggestedResolution(false);
        OctoLogging.d(TAG, "loadSuggestedResolution: Suggested resolution from getSuggestedResolution: " + suggestedRes);
        Collection<Size> sizes = getAvailableVideoSizes().values();

        if (sizes.isEmpty()) {
            OctoLogging.w(TAG, "loadSuggestedResolution: No available video sizes found. Cannot set resolution.");
            return;
        }
        OctoLogging.d(TAG, "loadSuggestedResolution: Available video sizes found: " + sizes);

        int minHeight = Integer.MAX_VALUE;
        int maxHeight = Integer.MIN_VALUE;
        Size bestSize = null;

        for (Size size : sizes) {
            int height = size.getHeight();
            if (height <= suggestedRes) {
                if (bestSize == null || height > bestSize.getHeight()) {
                    bestSize = size;
                }
            }
            minHeight = Math.min(minHeight, height);
            maxHeight = Math.max(maxHeight, height);
        }
        OctoLogging.d(TAG, "loadSuggestedResolution: Minimum available height: " + minHeight + ", Maximum available height: " + maxHeight);

        if (bestSize != null) {
            int height = bestSize.getHeight();
            OctoLogging.d(TAG, "loadSuggestedResolution: Best resolution size found matching suggested resolution: " + bestSize + ", Height: " + height);
            cameraResolution = height;
            OctoLogging.d(TAG, "loadSuggestedResolution: Setting cameraResolution to: " + height);
            int current = getResolutionFromConfig();
            OctoLogging.d(TAG, "loadSuggestedResolution: Current resolution from config: " + current);
            if (current == -1 || current > maxHeight || current < minHeight) {
                OctoLogging.d(TAG, "loadSuggestedResolution: Current config resolution is invalid or not set. Updating OctoConfig to: " + height);
                OctoConfig.INSTANCE.cameraXResolution.updateValue(height);
            } else {
                OctoLogging.d(TAG, "loadSuggestedResolution: Current config resolution is valid and within range, no update needed.");
            }
        } else {
            OctoLogging.w(TAG, "loadSuggestedResolution: No suitable resolution found within suggested resolution limit (" + suggestedRes + ").");
        }
        OctoLogging.d(TAG, "loadSuggestedResolution: Finished loading suggested resolution.");
    }

    public static int getCameraResolution() {
        return cameraResolution;
    }

    private static int getResolutionFromConfig() {
        int current = OctoConfig.INSTANCE.cameraXResolution.getValue();
        if (BuildVars.DEBUG_PRIVATE_VERSION) {
            OctoLogging.e(TAG, String.format(Locale.US, "getResolutionFromConfig: %d, enumToValue: %d", current, CameraXResolution.INSTANCE.enumToValue(CameraXResolution.HD)));
        }
        if (current == CameraXResolution.INSTANCE.enumToValue(CameraXResolution.SD)) {
            return 480;
        } else if (current == CameraXResolution.INSTANCE.enumToValue(CameraXResolution.HD)) {
            return 720;
        } else if (current == CameraXResolution.INSTANCE.enumToValue(CameraXResolution.FHD)) {
            return 1080;
        } else if (current == CameraXResolution.INSTANCE.enumToValue(CameraXResolution.UHD)) {
            return 2160;
        } else if (current == CameraXResolution.INSTANCE.enumToValue(CameraXResolution.MAX)) {
            return 4096;
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
            case SharedConfig.PERFORMANCE_CLASS_HIGH -> isPreview ? 2160 : 4096;
            default -> OctoConfig.INSTANCE.cameraXZeroShutter.getValue() && isPreview ? 1080 : 2160;
        };
    }

    @OptIn(markerClass = ExperimentalCamera2Interop.class)
    @SuppressLint("RestrictedApi")
    public static String getWideCameraId(@NonNull ProcessCameraProvider provider) {
        float minFocalLength = Float.MAX_VALUE;
        String wideCameraId = null;
        int availableBackCameraCount = 0;
        boolean primaryCameraHasWideZoom = false;

        List<CameraInfo> cameraInfoList = provider.getAvailableCameraInfos();

        for (CameraInfo cameraInfo : cameraInfoList) {
            Camera2CameraInfo camera2CameraInfo = Camera2CameraInfo.from(cameraInfo);
            String cameraId = camera2CameraInfo.getCameraId();

            try {
                CameraCharacteristics cameraCharacteristics = Camera2CameraInfo.from(cameraInfo).getCameraCharacteristicsMap().get(cameraId);
                if (cameraCharacteristics != null) {
                    Integer lensFacing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                    if (lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                        availableBackCameraCount++;

                        if (availableBackCameraCount == 1) {
                            ZoomState zoomState = cameraInfo.getZoomState().getValue();
                            if (zoomState != null && zoomState.getMinZoomRatio() < 1.0F && zoomState.getMinZoomRatio() > 0) {
                                primaryCameraHasWideZoom = true;
                            }
                        }

                        float[] focalLengths = cameraCharacteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
                        if (focalLengths != null && focalLengths.length > 0) {
                            float focalLength = focalLengths[0];
                            if (focalLength < 3.0f && focalLength < minFocalLength) {
                                minFocalLength = focalLength;
                                wideCameraId = cameraId;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                OctoLogging.e(TAG, "Error processing camera info for ID: " + cameraId, e);
            }
        }

        return (availableBackCameraCount >= 2 && !primaryCameraHasWideZoom) ? wideCameraId : null;
    }
}