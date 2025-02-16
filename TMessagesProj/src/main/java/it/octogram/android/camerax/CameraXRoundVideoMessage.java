/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */
package it.octogram.android.camerax;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceOrientedMeteringPointFactory;
import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.Components.InstantCameraView;

import java.util.Optional;

import it.octogram.android.logs.OctoLogging;

@RequiresApi(21)
public final class CameraXRoundVideoMessage {
    private static final String TAG = "InstantCamera";

    private record CameraConfig(CameraXController controller, CameraXController.CameraLifecycle lifecycle) {}

    private CameraConfig cameraConfig;

    private enum CameraState {
        READY,
        CONFIGURING,
        ERROR
    }

    public boolean isCameraReady(InstantCameraView instantCameraView) {
        if (cameraConfig == null) return false;

        CameraState state = getCameraState(instantCameraView);
        return state == CameraState.READY;
    }

    private CameraState getCameraState(InstantCameraView view) {
        if (view.cameraReady && cameraConfig != null && cameraConfig.controller().isInitied() && view.cameraThread != null) {
            return CameraState.READY;
        }
        return CameraState.CONFIGURING;
    }

    public Optional<CameraXController> getController() {
        return Optional.ofNullable(cameraConfig)
                .map(CameraConfig::controller);
    }

    public boolean isCameraXInitied() {
        return cameraConfig != null && cameraConfig.controller().isInitied();
    }

    public CameraXController.CameraLifecycle getLifecycle() {
        return cameraConfig != null ? cameraConfig.lifecycle() : null;
    }

    public void configureCameraView(InstantCameraView instantCameraView, SurfaceTexture surfaceTexture) {
        AndroidUtilities.runOnUIThread(() -> {
            try {
                performCameraConfiguration(instantCameraView, surfaceTexture);
            } catch (Exception e) {
                OctoLogging.e("Camera configuration failed: " + e.getMessage());
            }
        });
    }

    private void performCameraConfiguration(InstantCameraView instantCameraView, SurfaceTexture surfaceTexture) {
        if (isCameraReady(instantCameraView)) {
            OctoLogging.e("Camera already ready, skipping reconfiguration");
            return;
        }

        if (instantCameraView.cameraThread == null) {
            OctoLogging.d(TAG, "Creating camera thread");
            return;
        }

        OctoLogging.d(TAG, "Configuring camera");
        configureSurfaceTexture(instantCameraView, surfaceTexture);

        var factory = createMeteringPointFactory(instantCameraView);
        configureSurfaceProvider(instantCameraView, surfaceTexture, factory);

        if (cameraConfig != null && cameraConfig.lifecycle() != null) {
            cameraConfig.lifecycle().start();
        }
    }

    private void configureSurfaceTexture(InstantCameraView view, SurfaceTexture texture) {
        var size = view.previewSize[0];
        texture.setDefaultBufferSize(size.getWidth(), size.getHeight());
    }

    private MeteringPointFactory createMeteringPointFactory(InstantCameraView view) {
        var size = view.previewSize[0];
        return new SurfaceOrientedMeteringPointFactory(size.getWidth(), size.getHeight());
    }

    private void configureSurfaceProvider(@NonNull InstantCameraView instantCameraView, @NonNull SurfaceTexture surfaceTexture, @NonNull MeteringPointFactory factory) {
        OctoLogging.d(TAG, "Configuring surface provider");

        Preview.SurfaceProvider surfaceProvider = request -> {
            OctoLogging.d(TAG, "Providing surface");
            Surface surface = new Surface(surfaceTexture);
            request.provideSurface(surface, ContextCompat.getMainExecutor(instantCameraView.getContext()), result -> OctoLogging.d(TAG, "Surface provided"));
        };

        updateFlashStatus(instantCameraView);

        var lifecycle = new CameraXController.CameraLifecycle();
        var controller = new CameraXController(lifecycle, factory, surfaceProvider);

        controller.setStableFPSPreviewOnly(true);
        initializeCamera(instantCameraView, controller);

        this.cameraConfig = new CameraConfig(controller, lifecycle);
    }

    private void initializeCamera(InstantCameraView view, CameraXController controller) {
        controller.initCamera(
                view.getContext(),
                view.isFrontface,
                () -> {
                    OctoLogging.d(TAG, "Camera ready");
                    if (view.cameraThread != null) {
                        OctoLogging.d(TAG, "Setting orientation");
                        view.cameraThread.setOrientation();
                    }
                }
        );
    }

    public void toggleCamera(InstantCameraView instantCameraView) {
        OctoLogging.d(TAG, "Toggling camera");
        instantCameraView.saveLastCameraBitmap();

        instantCameraView.isFrontface = !instantCameraView.isFrontface;
        instantCameraView.cameraReady = false;
        Bitmap bitmap = instantCameraView.lastBitmap;

        if (bitmap != null) {
            OctoLogging.d(TAG, "Showing flicker stub");
            instantCameraView.needDrawFlickerStub = false;
            instantCameraView.textureOverlayView.setImageBitmap(bitmap);
            instantCameraView.textureOverlayView.setAlpha(1f);
        }

        instantCameraView.cameraThread.reinitForNewCamera();
        updateFlashStatus(instantCameraView);
    }

    public void stopCamera(InstantCameraView instantCameraView) {
        try {
            updateFlashStatus(instantCameraView);

            getController().ifPresent(controller -> {
                OctoLogging.d(TAG, "Stopping camera");
                controller.stopVideoRecording(true);
                controller.closeCamera();
                if (cameraConfig != null && cameraConfig.lifecycle() != null) {
                    cameraConfig.lifecycle().stop();
                }
                cameraConfig = null;
            });
        } catch (Exception e) {
            OctoLogging.e("Error stopping camera: " + e.getMessage());
        }
    }

    public void updateFlashStatus(InstantCameraView instantCameraView) {
        OctoLogging.d(TAG, "Updating flash status");
        instantCameraView.updateFlash();
    }

    public void setZoom(float zoom) {
        OctoLogging.d(TAG, "Applying zoom: " + zoom);
        getController().ifPresent(controller -> controller.setZoom(zoom));
    }
}