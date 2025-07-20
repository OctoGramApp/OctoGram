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
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Range;
import android.view.Surface;
import android.view.WindowManager;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.annotation.RestrictTo;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalZeroShutterLag;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.impl.utils.Exif;
import androidx.camera.core.internal.compat.workaround.ExifRotationAvailability;
import androidx.camera.extensions.ExtensionMode;
import androidx.camera.extensions.ExtensionsManager;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.FallbackStrategy;
import androidx.camera.video.FileOutputOptions;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import com.google.common.util.concurrent.ListenableFuture;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.camera.Size;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import it.octogram.android.CameraPreview;
import it.octogram.android.OctoConfig;
import it.octogram.android.utils.OctoLogging;
import it.octogram.android.utils.media.JpegImageUtils;

public class CameraXController {

    protected static final String TAG = "CameraXController";
    private static Camera camera;
    private final CameraLifecycle lifecycle;
    private final MeteringPointFactory meteringPointFactory;
    private final Preview.SurfaceProvider surfaceProvider;
    public float oldZoomSelection = OctoConfig.INSTANCE.cameraPreview.getValue() != CameraPreview.BOTTOM_BAR ? 0F : 5F;
    private boolean isFrontface;
    private boolean isInitiated = false;
    private ProcessCameraProvider provider;
    private CameraSelector cameraSelector;
    private CameraXView.VideoSavedCallback videoSavedCallback;
    private boolean abandonCurrentVideo = false;
    private ImageCapture iCapture;
    private Preview previewUseCase;
    private VideoCapture<Recorder> vCapture;
    private Recording recording;
    private ExtensionsManager extensionsManager;
    private boolean stableFPSPreviewOnly = false;
    private boolean noSupportedSurfaceCombinationWorkaround = false;
    private int selectedEffect = EffectFacing.CAMERA_NONE;

    public CameraXController(CameraLifecycle lifecycle, MeteringPointFactory factory, Preview.SurfaceProvider surfaceProvider) {
        this.lifecycle = lifecycle;
        this.meteringPointFactory = factory;
        this.surfaceProvider = surfaceProvider;
    }

    public static boolean hasGoodCamera(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    @OptIn(markerClass = ExperimentalZeroShutterLag.class)
    public static boolean isZSLSupported() {
        if (ForceZslSupport.isForcedZslDevice()) return true;
        if (camera == null) return false;
        return camera.getCameraInfo().isZslSupported();
    }

    @OptIn(markerClass = ExperimentalZeroShutterLag.class)
    private ImageCapture.Builder getCaptureModeBuilder() {
        var iCaptureBuilder = new ImageCapture.Builder();

        int captureMode;
        if (OctoConfig.INSTANCE.cameraXZeroShutter.getValue()) {
            captureMode = ImageCapture.CAPTURE_MODE_ZERO_SHUTTER_LAG;
        } else if (OctoConfig.INSTANCE.cameraXPerformanceMode.getValue()) {
            captureMode = ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY;
        } else {
            captureMode = ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY;
        }

        iCaptureBuilder.setCaptureMode(captureMode);
        iCaptureBuilder.setTargetAspectRatio(AspectRatio.RATIO_16_9);

        return iCaptureBuilder;
    }

    public void setTorchEnabled(boolean b) {
        if (camera != null) {
            camera.getCameraControl().enableTorch(b);
        }
    }

    public boolean isInitied() {
        return isInitiated;
    }

    public void setFrontFace(boolean isFrontFace) {
        this.isFrontface = isFrontFace;
    }

    public boolean isFrontface() {
        return isFrontface;
    }

    public void setStableFPSPreviewOnly(boolean isEnabled) {
        stableFPSPreviewOnly = isEnabled;
    }

    public void initCamera(Context context, boolean isInitialFrontface, Runnable onPreInit) {
        this.isFrontface = isInitialFrontface;
        var providerFtr = ProcessCameraProvider.getInstance(context);
        providerFtr.addListener(
                () -> {
                    try {
                        provider = providerFtr.get();
                        ListenableFuture<ExtensionsManager> extensionFuture = ExtensionsManager.getInstanceAsync(context, provider);
                        extensionFuture.addListener(() -> {
                            try {
                                extensionsManager = extensionFuture.get();
                                bindUseCases();
                                lifecycle.start();
                                onPreInit.run();
                                isInitiated = true;
                            } catch (ExecutionException | InterruptedException e) {
                                OctoLogging.e("Error initializing ExtensionsManager: " + e.getMessage(), e);
                            }
                        }, ContextCompat.getMainExecutor(context));
                    } catch (ExecutionException | InterruptedException e) {
                        OctoLogging.e("Error retrieving provider: " + e.getMessage(), e);
                    }
                }, ContextCompat.getMainExecutor(context)
        );
    }

    public int getCameraEffect() {
        return selectedEffect;
    }

    public void setCameraEffect(@EffectFacing int effect) {
        selectedEffect = effect;
        bindUseCases();
    }

    public void switchCamera() {
        isFrontface = !isFrontface;
        bindUseCases();
    }

    public void closeCamera() {
        lifecycle.stop();
    }

    public boolean hasFrontFaceCamera() {
        if (provider == null) {
            return false;
        }
        try {
            return provider.hasCamera(new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build());
        } catch (CameraInfoUnavailableException e) {
            return false;
        }
    }

    private int getNextFlashMode(int legacyMode) {
        return switch (legacyMode) {
            case ImageCapture.FLASH_MODE_AUTO -> ImageCapture.FLASH_MODE_ON;
            case ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_OFF;
            default -> ImageCapture.FLASH_MODE_AUTO;
        };
    }

    public int setNextFlashMode() {
        int next = getNextFlashMode(iCapture.getFlashMode());
        iCapture.setFlashMode(next);
        return next;
    }

    public int getCurrentFlashMode() {
        return iCapture.getFlashMode();
    }

    public boolean isFlashAvailable() {
        return camera.getCameraInfo().hasFlashUnit();
    }

    public boolean isModeSupported(@ExtensionMode.Mode int mode) {
        if (mode == ExtensionMode.NONE) {
            return true;
        }

        var validModes = new int[]{
                ExtensionMode.HDR,
                ExtensionMode.NIGHT,
                ExtensionMode.AUTO,
                ExtensionMode.BOKEH,
                ExtensionMode.FACE_RETOUCH
        };

        for (int validMode : validModes) {
            if (extensionsManager != null && mode == validMode) {
                try {
                    return extensionsManager.isExtensionAvailable(cameraSelector, mode);
                } catch (Exception e) {
                    OctoLogging.d("CameraX-Extensions", String.format(Locale.US, "Error: %s", e.getMessage()), e);
                    OctoLogging.e(MessageFormat.format("CameraX-Extensions: {0}", e.getMessage()), e);
                }
            }
        }

        throw new IllegalArgumentException("Unexpected mode: " + mode);
    }

    public boolean isAvailableWideMode() {
        return provider != null && CameraXUtils.isWideAngleAvailable(provider);
    }

    public android.util.Size getVideoBestSize() {
        int w, h;
        var size = CameraXUtils.getPreviewBestSize();
        w = size.getWidth();
        h = size.getHeight();
        if ((getDisplayOrientation() == 0 || getDisplayOrientation() == 180) && getDeviceDefaultOrientation() == Configuration.ORIENTATION_PORTRAIT) {
            return new android.util.Size(h, w);
        } else {
            return new android.util.Size(w, h);
        }
    }

    @SuppressLint("SwitchIntDef")
    public CameraSelector getCameraSelectorForEffect(CameraSelector cameraSelector, @EffectFacing int selectedEffect) {
        return extensionsManager.getExtensionEnabledCameraSelector(cameraSelector, switch (selectedEffect) {
            case EffectFacing.CAMERA_NIGHT -> ExtensionMode.NIGHT;
            case EffectFacing.CAMERA_HDR -> ExtensionMode.HDR;
            case EffectFacing.CAMERA_AUTO -> ExtensionMode.AUTO;
            case EffectFacing.CAMERA_BOKEH -> ExtensionMode.BOKEH;
            case EffectFacing.CAMERA_FACE_RETOUCH -> ExtensionMode.FACE_RETOUCH;
            default -> ExtensionMode.NONE;
        });
    }

    public void bindUseCases() {
        if (provider == null) return;

        var targetSize = getVideoBestSize();
        var previewBuilder = new Preview.Builder().setTargetResolution(targetSize);

        cameraSelector = !isFrontface && selectedEffect == EffectFacing.CAMERA_WIDE
                ? CameraXUtils.getDefaultWideAngleCamera(provider)
                : (isFrontface ? CameraSelector.DEFAULT_FRONT_CAMERA : CameraSelector.DEFAULT_BACK_CAMERA);

        if (!isFrontface) {
            cameraSelector = getCameraSelectorForEffect(cameraSelector, selectedEffect);
        }

        var quality = CameraXUtils.getVideoQuality();
        var selector = QualitySelector.from(quality, FallbackStrategy.higherQualityOrLowerThan(quality));
        vCapture = VideoCapture.withOutput(new Recorder.Builder().setQualitySelector(selector).build());

        var iCaptureBuilder = getCaptureModeBuilder();

        provider.unbindAll();
        previewUseCase = previewBuilder.build();
        previewUseCase.setSurfaceProvider(surfaceProvider);

        if (lifecycle.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) return;

        try {
            if (stableFPSPreviewOnly) {
                camera = provider.bindToLifecycle(lifecycle, cameraSelector, previewUseCase, vCapture);
            } else {
                iCapture = iCaptureBuilder.build();
                camera = provider.bindToLifecycle(lifecycle, cameraSelector, previewUseCase, vCapture, iCapture);
                noSupportedSurfaceCombinationWorkaround = false;
            }
        } catch (java.lang.IllegalArgumentException e) {
            noSupportedSurfaceCombinationWorkaround = true;
            try {
                camera = provider.bindToLifecycle(lifecycle, cameraSelector, previewUseCase, iCapture);
            } catch (java.lang.IllegalArgumentException ignored) {
                camera = null;
            }
        }

        if (camera != null) {
            camera.getCameraControl().setLinearZoom(oldZoomSelection);
        }
    }

    public float getMaxZoom() {
        return camera.getCameraInfo().getZoomState().getValue() != null ? camera.getCameraInfo().getZoomState().getValue().getMaxZoomRatio() : 0.0f;
    }

    public float getMinZoom() {
        return camera.getCameraInfo().getZoomState().getValue() != null ? camera.getCameraInfo().getZoomState().getValue().getMinZoomRatio() : 0.0f;
    }

    public void setZoom(float value) {
        oldZoomSelection = value;
        camera.getCameraControl().setLinearZoom(value);
    }

    public float resetZoom() {
        if (camera == null) return 0.0f;

        camera.getCameraControl().setZoomRatio(0f);
        var zoomState = camera.getCameraInfo().getZoomState().getValue();
        return zoomState != null ? (oldZoomSelection = zoomState.getLinearZoom()) : 0.0f;
    }

    public boolean isExposureCompensationSupported() {
        if (camera == null) return false;
        return camera.getCameraInfo().getExposureState().isExposureCompensationSupported();
    }

    public void setExposureCompensation(float value) {
        if (camera == null) return;
        if (!camera.getCameraInfo().getExposureState().isExposureCompensationSupported()) return;
        Range<Integer> evRange = camera.getCameraInfo().getExposureState().getExposureCompensationRange();
        int index = (int) (mix(evRange.getLower().floatValue(), evRange.getUpper().floatValue(), value) + 0.5f);
        camera.getCameraControl().setExposureCompensationIndex(index);
    }

    public void setTargetOrientation(int rotation) {
        if (previewUseCase != null) {
            previewUseCase.setTargetRotation(rotation);
        }
        if (iCapture != null) {
            iCapture.setTargetRotation(rotation);
        }
        if (vCapture != null) {
            vCapture.setTargetRotation(rotation);
        }
    }

    public void setWorldCaptureOrientation(int rotation) {
        if (iCapture != null) {
            iCapture.setTargetRotation(rotation);
        }
        if (vCapture != null) {
            vCapture.setTargetRotation(rotation);
        }
    }

    public void focusToPoint(int x, int y) {
        MeteringPoint point = meteringPointFactory.createPoint(x, y);

        FocusMeteringAction action = new FocusMeteringAction
                .Builder(point, FocusMeteringAction.FLAG_AE | FocusMeteringAction.FLAG_AF | FocusMeteringAction.FLAG_AWB)
                //.disableAutoCancel()
                .build();

        camera.getCameraControl().startFocusAndMetering(action);
    }

    public void recordVideo(final File path, boolean mirror, CameraXView.VideoSavedCallback onStop) {
        if (noSupportedSurfaceCombinationWorkaround) {
            provider.unbindAll();
            provider.bindToLifecycle(lifecycle, cameraSelector, previewUseCase, vCapture);
        }
        videoSavedCallback = onStop;
        FileOutputOptions fileOpt = new FileOutputOptions
                .Builder(path)
                .build();

        if (iCapture.getFlashMode() == ImageCapture.FLASH_MODE_ON) {
            camera.getCameraControl().enableTorch(true);
        }
        recording = vCapture.getOutput()
                .prepareRecording(ApplicationLoader.applicationContext, fileOpt)
                .withAudioEnabled()
                .start(AsyncTask.THREAD_POOL_EXECUTOR, videoRecordEvent -> {
                    if (videoRecordEvent instanceof VideoRecordEvent.Finalize finalize) {
                        if (finalize.hasError()) {
                            if (noSupportedSurfaceCombinationWorkaround) {
                                AndroidUtilities.runOnUIThread(() -> {
                                    provider.unbindAll();
                                    provider.bindToLifecycle(lifecycle, cameraSelector, previewUseCase, iCapture);
                                });
                            }
                            OctoLogging.e(finalize.getCause());
                        } else {
                            if (noSupportedSurfaceCombinationWorkaround) {
                                AndroidUtilities.runOnUIThread(() -> {
                                    provider.unbindAll();
                                    provider.bindToLifecycle(lifecycle, cameraSelector, previewUseCase, iCapture);
                                });
                            }

                            if (abandonCurrentVideo) {
                                abandonCurrentVideo = false;
                            } else {
                                finishRecordingVideo(path, mirror);
                                if (iCapture.getFlashMode() == ImageCapture.FLASH_MODE_ON) {
                                    camera.getCameraControl().enableTorch(false);
                                }
                            }
                        }
                    }
                });
    }

    private void finishRecordingVideo(final File path, boolean mirror) {
        long duration = 0;
        try (MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever()) {
            mediaMetadataRetriever.setDataSource(path.getAbsolutePath());
            String d = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (d != null) {
                duration = (int) Math.ceil(Long.parseLong(d) / 1000.0f);
            }
        } catch (Exception e) {
            OctoLogging.e(e);
        }

        Bitmap bitmap = SendMessagesHelper.createVideoThumbnail(path.getAbsolutePath(), MediaStore.Video.Thumbnails.MINI_KIND);
        if (mirror) {
            Bitmap b = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(b);
            canvas.scale(-1, 1, b.getWidth() >> 1, b.getHeight() >> 1);
            canvas.drawBitmap(bitmap, 0, 0, null);
            bitmap.recycle();
            bitmap = b;
        }
        String fileName = Integer.MIN_VALUE + "_" + SharedConfig.getLastLocalId() + ".jpg";
        final File cacheFile = new File(FileLoader.getDirectory(FileLoader.MEDIA_DIR_CACHE), fileName);
        try {
            FileOutputStream stream = new FileOutputStream(cacheFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 87, stream);
        } catch (Throwable e) {
            OctoLogging.e(e);
        }
        SharedConfig.saveConfig();
        final long durationFinal = duration;
        final Bitmap bitmapFinal = bitmap;
        AndroidUtilities.runOnUIThread(() -> {
            if (videoSavedCallback != null) {
                String cachePath = cacheFile.getAbsolutePath();
                if (bitmapFinal != null) {
                    ImageLoader.getInstance().putImageToCache(new BitmapDrawable(ApplicationLoader.applicationContext.getResources(), bitmapFinal), Utilities.MD5(cachePath), false);
                }
                videoSavedCallback.onFinishVideoRecording(cachePath, durationFinal);
                videoSavedCallback = null;
            }
        });
    }

    public void stopVideoRecording(final boolean abandon) {
        abandonCurrentVideo = abandon;
        if (recording != null) {
            recording.stop();
        }
    }

    public void takePicture(final File file, Runnable onTake) {
        if (stableFPSPreviewOnly) return;
        iCapture.takePicture(AsyncTask.THREAD_POOL_EXECUTOR, new ImageCapture.OnImageCapturedCallback() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                int orientation = image.getImageInfo().getRotationDegrees();
                try {

                    FileOutputStream output = new FileOutputStream(file);

                    int flipState = 0;
                    if (isFrontface && (orientation == 90 || orientation == 270)) {
                        flipState = JpegImageUtils.FLIP_Y;
                    } else if (isFrontface && (orientation == 0 || orientation == 180)) {
                        flipState = JpegImageUtils.FLIP_X;
                    }

                    byte[] jpegByteArray = JpegImageUtils.imageToJpegByteArray(image, flipState);
                    output.write(jpegByteArray);
                    output.close();
                    Exif exif = Exif.createFromFile(file);
                    exif.attachTimestamp();

                    if (new ExifRotationAvailability().shouldUseExifOrientation(image)) {
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        buffer.rewind();
                        byte[] data = new byte[buffer.capacity()];
                        buffer.get(data);
                        InputStream inputStream = new ByteArrayInputStream(data);
                        Exif originalExif = Exif.createFromInputStream(inputStream);
                        exif.setOrientation(originalExif.getOrientation());
                    } else {
                        exif.rotate(orientation);
                    }
                    exif.save();
                } catch (JpegImageUtils.CodecFailedException | IOException |
                         IllegalStateException e) {
                    OctoLogging.e("Error occurred: " + e.getMessage(), e);
                }
                image.close();
                AndroidUtilities.runOnUIThread(onTake);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                OctoLogging.e(exception);
            }
        });
    }

    @SuppressLint("RestrictedApi")
    public Size getPreviewSize() {
        Size size = new Size(0, 0);
        if (previewUseCase != null) {
            android.util.Size s = previewUseCase.getAttachedSurfaceResolution();
            if (s != null) {
                size = new Size(s.getWidth(), s.getHeight());
            }
        }
        return size;
    }

    public int getDisplayOrientation() {
        WindowManager mgr = (WindowManager) ApplicationLoader.applicationContext.getSystemService(Context.WINDOW_SERVICE);
        int rotation = mgr.getDefaultDisplay().getRotation();
        return switch (rotation) {
            case Surface.ROTATION_90 -> 90;
            case Surface.ROTATION_180 -> 180;
            case Surface.ROTATION_270 -> 270;
            case Surface.ROTATION_0 -> 0;
            default -> throw new IllegalStateException("Unexpected value: " + rotation);
        };
    }

    private int getDeviceDefaultOrientation() {
        WindowManager windowManager = (WindowManager) (ApplicationLoader.applicationContext.getSystemService(Context.WINDOW_SERVICE));
        Configuration config = ApplicationLoader.applicationContext.getResources().getConfiguration();
        int rotation = windowManager.getDefaultDisplay().getRotation();

        if (((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && config.orientation == Configuration.ORIENTATION_LANDSCAPE) ||
                ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && config.orientation == Configuration.ORIENTATION_PORTRAIT)) {
            return Configuration.ORIENTATION_LANDSCAPE;
        } else {
            return Configuration.ORIENTATION_PORTRAIT;
        }
    }

    private float mix(Float x, Float y, Float f) {
        return x * (1 - f) + y * f;
    }

    @IntDef({
            EffectFacing.CAMERA_NONE,
            EffectFacing.CAMERA_AUTO,
            EffectFacing.CAMERA_HDR,
            EffectFacing.CAMERA_NIGHT,
            EffectFacing.CAMERA_WIDE,
            EffectFacing.CAMERA_BOKEH,
            EffectFacing.CAMERA_FACE_RETOUCH
    })
    @Retention(RetentionPolicy.SOURCE)
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public @interface EffectFacing {
        int CAMERA_NONE = 0,
                CAMERA_NIGHT = 1,
                CAMERA_HDR = 2,
                CAMERA_AUTO = 3,
                CAMERA_WIDE = 4,
                CAMERA_BOKEH = 5,
                CAMERA_FACE_RETOUCH = 6;
    }

    public static class CameraLifecycle implements LifecycleOwner {

        private final LifecycleRegistry lifecycleRegistry;

        public CameraLifecycle() {
            lifecycleRegistry = new LifecycleRegistry(this);
            lifecycleRegistry.setCurrentState(Lifecycle.State.CREATED);
        }

        public void start() {
            try {
                lifecycleRegistry.setCurrentState(Lifecycle.State.RESUMED);
            } catch (IllegalStateException ignored) {
            }
        }

        public void stop() {
            try {
                lifecycleRegistry.setCurrentState(Lifecycle.State.DESTROYED);
            } catch (IllegalStateException ignored) {
            }
        }

        @NonNull
        public Lifecycle getLifecycle() {
            return lifecycleRegistry;
        }
    }

    static class ForceZslSupport {
        private static final String TAG = "ForceZslSupport";

        public static boolean isForcedZslDevice() {
            var supportedDevices = new String[]{
                    // Pixel 6 Series
                    "oriole", "raven", "bluejay",
                    // Pixel 7 Series
                    "cheetah", "lynx", "panther",
                    // Pixel 8 Series
                    "shiba", "akita", "husky",
                    // Pixel 9 Series
                    "tokay", "tegu", "caiman", "komodo",
            };

            boolean isSupported = Arrays.asList(supportedDevices).contains(Build.DEVICE.toLowerCase(Locale.US).trim());
            OctoLogging.d(TAG, String.format(Locale.US, "Device %s ZSL support forced: %s (forced for: %s)", Build.DEVICE, isSupported, Arrays.asList(supportedDevices)));
            OctoLogging.d(TAG, String.format(Locale.US, "Device %s ZSL support forced: %s", Build.DEVICE, isSupported));
            return isSupported;
        }
    }
}
