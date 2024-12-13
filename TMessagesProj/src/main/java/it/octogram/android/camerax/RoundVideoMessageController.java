/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */
package it.octogram.android.camerax;

import android.graphics.SurfaceTexture;
import android.view.Surface;

import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceOrientedMeteringPointFactory;
import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.Components.InstantCameraView;

import it.octogram.android.logs.OctoLogging;

public class RoundVideoMessageController {
    public CameraXController.CameraLifecycle cameraLifecycle = new CameraXController.CameraLifecycle();
    public CameraXController cameraXController;

    public void configureCameraView(InstantCameraView instantCameraView, final SurfaceTexture surfaceTexture) {
        AndroidUtilities.runOnUIThread(() -> {
            if (isCameraReady(instantCameraView)) {                 
                OctoLogging.e("Camera already ready, skipping reconfiguration");
                return;
            }

            if (instantCameraView.cameraThread == null) {
                OctoLogging.d("InstantCamera", "Creating camera thread");
                return;
            }

            OctoLogging.d("InstantCamera", "Configuring camera");
            resetZoomSlider(instantCameraView);

            OctoLogging.d("InstantCamera create camera session");

            OctoLogging.d("InstantCamera", "Setting default buffer size");
            surfaceTexture.setDefaultBufferSize(
                    instantCameraView.previewSize[0].getWidth(),
                    instantCameraView.previewSize[0].getHeight()
            );

            OctoLogging.d("InstantCamera", "Creating metering point factory");
            MeteringPointFactory factory = new SurfaceOrientedMeteringPointFactory(
                    (float) instantCameraView.previewSize[0].getWidth(),
                    (float) instantCameraView.previewSize[0].getHeight()
            );

            OctoLogging.d("InstantCamera", "Configuring surface provider");
            configureSurfaceProvider(instantCameraView, surfaceTexture, factory);
            cameraLifecycle.start();
        });
    }

    private void configureSurfaceProvider(InstantCameraView instantCameraView, SurfaceTexture surfaceTexture, MeteringPointFactory factory) {
        OctoLogging.d("InstantCamera", "Configuring surface provider");
        Preview.SurfaceProvider surfaceProvider = request -> {
            OctoLogging.d("InstantCamera", "Providing surface");
            Surface surface = new Surface(surfaceTexture);
            request.provideSurface(surface, ContextCompat.getMainExecutor(instantCameraView.getContext()), result -> {
            });
        };

        updateFlashStatus(instantCameraView);

        cameraXController = new CameraXController(cameraLifecycle, factory, surfaceProvider);
        cameraXController.setStableFPSPreviewOnly(true);
        cameraXController.initCamera(instantCameraView.getContext(), instantCameraView.isFrontface, () -> {
            OctoLogging.d("InstantCamera", "Camera ready");
            if (instantCameraView.cameraThread != null) {
                OctoLogging.d("InstantCamera", "Setting orientation");
                instantCameraView.cameraThread.setOrientation();
            }
        });
    }

    public void toggleCamera(InstantCameraView instantCameraView) {
        OctoLogging.d("InstantCamera", "Toggling camera");
        instantCameraView.saveLastCameraBitmap();
        resetZoomSlider(instantCameraView);

        instantCameraView.isFrontface = !instantCameraView.isFrontface;
        instantCameraView.cameraReady = false;

        if (instantCameraView.lastBitmap != null) {
            OctoLogging.d("InstantCamera", "Showing flicker stub");
            instantCameraView.needDrawFlickerStub = false;
            instantCameraView.textureOverlayView.setImageBitmap(instantCameraView.lastBitmap);
            instantCameraView.textureOverlayView.setAlpha(1f);
        }

        instantCameraView.cameraThread.reinitForNewCamera();
        updateFlashStatus(instantCameraView);
    }

    public void stopCamera(InstantCameraView instantCameraView) {
        try {
            if (instantCameraView.zoomSlider != null) {
                OctoLogging.d("InstantCamera", "Hiding zoom slider");
                instantCameraView.zoomSlider.setAlpha(0f);
            }

            updateFlashStatus(instantCameraView);

            if (cameraXController != null) {
                OctoLogging.d("InstantCamera", "Stopping camera");
                cameraXController.stopVideoRecording(true);
                cameraXController.closeCamera();
                cameraXController = null;
                cameraLifecycle.stop();
            }
        } catch (Exception e) {
            OctoLogging.e("Error stopping camera: " + e.getMessage());
        }
    }

    public boolean isCameraReady(InstantCameraView instantCameraView) {
        return instantCameraView.cameraReady &&
                cameraXController != null &&
                cameraXController.isInitied() &&
                instantCameraView.cameraThread != null;
    }

    private void resetZoomSlider(InstantCameraView instantCameraView) {
        if (instantCameraView.zoomSlider == null) {
            OctoLogging.d("InstantCamera", "Zoom slider not found");
            return;
        }
        OctoLogging.d("InstantCamera", "Resetting zoom slider");
        instantCameraView.zoomSlider.setSliderValue(0F, false);
        instantCameraView.zoomSlider.setAlpha(1.0f);
    }

    public void updateFlashStatus(InstantCameraView instantCameraView) {
        OctoLogging.d("InstantCamera", "Updating flash status");
        instantCameraView.updateFlash();
    }

    public void applyZoom(float zoom) {
        OctoLogging.d("InstantCamera", "Applying zoom: " + zoom);
        if (cameraXController != null) {
            cameraXController.setZoom(zoom);
        }
    }
}
