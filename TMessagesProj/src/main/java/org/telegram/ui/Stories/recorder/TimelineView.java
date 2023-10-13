package org.telegram.ui.Stories.recorder;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.dpf2;
import static org.telegram.messenger.AndroidUtilities.lerp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedFloat;
import org.telegram.ui.Components.BlurringShader;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.ItemOptions;
import org.telegram.ui.Components.Scroller;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class TimelineView extends View {

    // milliseconds when timeline goes out of the box
    public static final long MAX_SCROLL_DURATION = 120 * 1000L;
    // minimal allowed duration to select
    public static final long MIN_SELECT_DURATION = 1 * 1000L;
    // maximum allowed duration to select
    public static final long MAX_SELECT_DURATION = (long) (59 * 1000L);

    interface TimelineDelegate {
        void onProgressDragChange(boolean dragging);
        void onProgressChange(long progress, boolean fast);

        void onVideoLeftChange(float left);
        void onVideoRightChange(float right);

        void onAudioOffsetChange(long offset);
        void onAudioLeftChange(float left);
        void onAudioRightChange(float right);

        void onAudioVolumeChange(float volume);
        void onAudioRemove();
    }

    private TimelineDelegate delegate;

    private long progress;
    private long scroll;

    private boolean hasVideo;
    private String videoPath;
    private long videoDuration;
    private float videoLeft;
    private float videoRight;
    private VideoThumbsLoader thumbs;

    private boolean hasAudio;
    private String audioPath;
    private boolean audioSelected;
    private long audioOffset;
    private long audioDuration;
    private float audioLeft;
    private float audioRight;
    private boolean waveformIsLoaded;
    private float audioVolume;
    private AudioWaveformLoader waveform;

    private long getBaseDuration() {
        if (hasVideo) {
            return videoDuration;
        }
        if (hasAudio) {
            return audioDuration;
        }
        return Math.max(1, audioDuration);
    }

    private final AnimatedFloat audioT = new AnimatedFloat(this, 0, 360, CubicBezierInterpolator.EASE_OUT_QUINT);
    private final AnimatedFloat audioSelectedT = new AnimatedFloat(this, 360, CubicBezierInterpolator.EASE_OUT_QUINT);

    private final AnimatedFloat waveformLoaded = new AnimatedFloat(this, 0, 600, CubicBezierInterpolator.EASE_OUT_QUINT);
    private final AnimatedFloat waveformMax = new AnimatedFloat(this, 0, 360, CubicBezierInterpolator.EASE_OUT_QUINT);

    private final BlurringShader.BlurManager blurManager;
    private final BlurringShader.StoryBlurDrawer backgroundBlur;
    private final BlurringShader.StoryBlurDrawer audioBlur;
    private final BlurringShader.StoryBlurDrawer audioWaveformBlur;

    private final RectF videoBounds = new RectF();
    private final Paint videoFramePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Path videoClipPath = new Path();
    private final Path selectedVideoClipPath = new Path();

    private final Paint regionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint regionCutPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint regionHandlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressWhitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF audioBounds = new RectF();
    private final Path audioClipPath = new Path();
    private final Paint waveformPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final WaveformPath waveformPath = new WaveformPath();

    private final Paint audioDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Drawable audioIcon;
    private final TextPaint audioAuthorPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private StaticLayout audioAuthor;
    private float audioAuthorWidth, audioAuthorLeft;
    private final TextPaint audioTitlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private StaticLayout audioTitle;
    private float audioTitleWidth, audioTitleLeft;

    private final LinearGradient ellipsizeGradient = new LinearGradient(0, 0, 16, 0, new int[] { 0x00ffffff, 0xffffffff }, new float[] { 0, 1 }, Shader.TileMode.CLAMP);
    private final Matrix ellipsizeMatrix = new Matrix();
    private final Paint ellipsizePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Scroller scroller = new Scroller(getContext());

    private final ViewGroup container;
    private final View previewContainer;
    private final Theme.ResourcesProvider resourcesProvider;

    private final Runnable onLongPress;

    public TimelineView(Context context, ViewGroup container, View previewContainer, Theme.ResourcesProvider resourcesProvider, BlurringShader.BlurManager blurManager) {
        super(context);

        this.container = container;
        this.previewContainer = previewContainer;
        this.resourcesProvider = resourcesProvider;

        audioDotPaint.setColor(0x7fffffff);
        audioAuthorPaint.setTextSize(dp(12));
        audioAuthorPaint.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
        audioAuthorPaint.setColor(0xffffffff);
        audioTitlePaint.setTextSize(dp(12));
        audioTitlePaint.setColor(0xffffffff);
        waveformPaint.setColor(0x40ffffff);

        ellipsizePaint.setShader(ellipsizeGradient);
        ellipsizePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

        regionPaint.setColor(0xffffffff);
        regionPaint.setShadowLayer(dp(1), 0, dp(1), 0x1a000000);
        regionCutPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        regionHandlePaint.setColor(0xff000000);
        progressWhitePaint.setColor(0xffffffff);
        progressShadowPaint.setColor(0x26000000);

        audioIcon = getContext().getResources().getDrawable(R.drawable.filled_widget_music).mutate();
        audioIcon.setColorFilter(new PorterDuffColorFilter(0xffffffff, PorterDuff.Mode.SRC_IN));

        this.blurManager = blurManager;
        backgroundBlur =    new BlurringShader.StoryBlurDrawer(blurManager, this, BlurringShader.StoryBlurDrawer.BLUR_TYPE_BACKGROUND);
        audioBlur =         new BlurringShader.StoryBlurDrawer(blurManager, this, BlurringShader.StoryBlurDrawer.BLUR_TYPE_AUDIO_BACKGROUND);
        audioWaveformBlur = new BlurringShader.StoryBlurDrawer(blurManager, this, BlurringShader.StoryBlurDrawer.BLUR_TYPE_AUDIO_WAVEFORM_BACKGROUND);

        onLongPress = () -> {
            if (!pressVideo && hasAudio) {
                VolumeSliderView slider =
                    new VolumeSliderView(getContext())
                        .setVolume(audioVolume)
                        .setOnValueChange(volume -> {
                            audioVolume = volume;
                            if (delegate != null) {
                                delegate.onAudioVolumeChange(volume);
                            }
                        });
                final long videoScrollDuration = Math.min(getBaseDuration(), MAX_SCROLL_DURATION);
                float uiRight = Math.min(w - px - ph, px + ph + (audioOffset - scroll + lerp(audioRight, 1, audioSelectedT.get()) * audioDuration) / (float) videoScrollDuration * sw);
                ItemOptions itemOptions = ItemOptions.makeOptions(container, resourcesProvider, this)
                    .addView(slider)
                    .addSpaceGap()
                    .add(R.drawable.msg_delete, LocaleController.getString(R.string.StoryAudioRemove), () -> {
                        if (delegate != null) {
                            delegate.onAudioRemove();
                        }
                    })
                    .setGravity(Gravity.RIGHT)
                    .forceTop(true)
                    .translate(-(w - uiRight) + dp(18), dp(4) + (!hasVideo ? dp(audioSelected ? 35 : 40) : 0))
                    .show();
                itemOptions.setBlurBackground(blurManager, -previewContainer.getX(), -previewContainer.getY());

                try {
                    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS, HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
                } catch (Exception e) {}
            }
        };
    }

    public void setDelegate(TimelineDelegate delegate) {
        this.delegate = delegate;
    }

    public void setVideo(String videoPath, long videoDuration) {
        if (TextUtils.equals(this.videoPath, videoPath)) {
            return;
        }
        if (thumbs != null) {
            thumbs.destroy();
            thumbs = null;
        }
        if (videoPath != null) {
            scroll = 0;
            this.videoPath = videoPath;
            this.videoDuration = videoDuration;
            setupVideoThumbs();
        } else {
            this.videoPath = null;
            this.videoDuration = 1;
            scroll = 0;
        }
        hasVideo = this.videoPath != null;
        progress = 0;
        invalidate();
    }

    private void setupVideoThumbs() {
        if (getMeasuredWidth() <= 0 || this.thumbs != null) {
            return;
        }
        this.thumbs = new VideoThumbsLoader(videoPath, getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), dp(38));
        if (this.thumbs.getDuration() > 0) {
            videoDuration = this.thumbs.getDuration();
        }
    }

    private final AnimatedFloat loopProgress = new AnimatedFloat(0, this, 0, 340, CubicBezierInterpolator.EASE_OUT_QUINT);
    private long loopProgressFrom = -1;
    public void setProgress(long progress) {
        if (
            hasVideo && progress < this.progress && progress <= videoDuration * videoLeft + 240 && this.progress + 240 >= videoDuration * videoRight ||
            hasAudio && progress < this.progress && progress <= audioDuration * audioLeft + 240 && this.progress + 240 >= audioDuration * audioRight
        ) {
            loopProgressFrom = -1;
            loopProgress.set(1, true);
        }
        this.progress = progress;
        invalidate();
    }

    public void setVideoLeft(float left) {
        videoLeft = left;
        invalidate();
    }

    public void setVideoRight(float right) {
        videoRight = right;
        invalidate();
    }

    public void setAudio(String audioPath, String audioAuthorText, String audioTitleText, long duration, long offset, float left, float right, float volume, boolean animated) {
        if (!TextUtils.equals(this.audioPath, audioPath)) {
            if (waveform != null) {
                waveform.destroy();
                waveform = null;
                waveformIsLoaded = false;
                waveformLoaded.set(0, true);
            }
            this.audioPath = audioPath;
            setupAudioWaveform();
        }
        this.audioPath = audioPath;
        hasAudio = !TextUtils.isEmpty(audioPath);
        if (!hasAudio) {
            audioSelected = false;
            audioAuthorText = null;
            audioTitleText = null;
        }
        if (TextUtils.isEmpty(audioAuthorText)) {
            audioAuthorText = null;
        }
        if (TextUtils.isEmpty(audioTitleText)) {
            audioTitleText = null;
        }
        if (hasAudio) {
            audioDuration = duration;
            audioOffset = offset - (long) (left * duration);
            audioLeft = left;
            audioRight = right;
            audioVolume = volume;
            if (audioAuthorText != null) {
                audioAuthor = new StaticLayout(audioAuthorText, audioAuthorPaint, 99999, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false);
                audioAuthorWidth = audioAuthor.getLineCount() > 0 ? audioAuthor.getLineWidth(0) : 0;
                audioAuthorLeft  = audioAuthor.getLineCount() > 0 ? audioAuthor.getLineLeft(0) : 0;
            } else {
                audioAuthorWidth = 0;
                audioAuthor = null;
            }
            if (audioTitleText != null) {
                audioTitle = new StaticLayout(audioTitleText, audioTitlePaint, 99999, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false);
                audioTitleWidth = audioTitle.getLineCount() > 0 ? audioTitle.getLineWidth(0) : 0;
                audioTitleLeft  = audioTitle.getLineCount() > 0 ? audioTitle.getLineLeft(0) : 0;
            } else {
                audioTitleWidth = 0;
                audioTitle = null;
            }
        }
        if (!animated) {
            audioT.set(hasAudio, true);
        }
        invalidate();
    }

    private void setupAudioWaveform() {
        if (getMeasuredWidth() <= 0 || this.waveform != null) {
            return;
        }
        this.waveform = new AudioWaveformLoader(audioPath, getMeasuredWidth() - getPaddingLeft() - getPaddingRight());
        waveformIsLoaded = false;
        waveformLoaded.set(0, true);
        waveformMax.set(1, true);
    }

    private static final int HANDLE_PROGRESS = 0;
    private static final int HANDLE_VIDEO_SCROLL = 1;
    private static final int HANDLE_VIDEO_LEFT = 2;
    private static final int HANDLE_VIDEO_RIGHT = 3;
    private static final int HANDLE_VIDEO_REGION = 4;
    private static final int HANDLE_AUDIO_SCROLL = 5;
    private static final int HANDLE_AUDIO_LEFT = 6;
    private static final int HANDLE_AUDIO_RIGHT = 7;
    private static final int HANDLE_AUDIO_REGION = 8;

    private int detectHandle(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        final long scrollWidth = Math.min(getBaseDuration(), MAX_SCROLL_DURATION);
        final float progressT = (Utilities.clamp(progress, getBaseDuration(), 0) + (!hasVideo ? audioOffset : 0) - scroll) / (float) scrollWidth;
        final float progressX = px + ph + sw * progressT;
        if (x >= progressX - dp(12) && x <= progressX + dp(12)) {
            return HANDLE_PROGRESS;
        }

        final boolean isInVideo = y > h - py - getVideoHeight() - dp(2);

        if (isInVideo) {
            final float leftX = px + ph + (videoLeft * videoDuration - scroll) / (float) scrollWidth * sw;
            final float rightX = px + ph + (videoRight * videoDuration - scroll) / (float) scrollWidth * sw;

            if (x >= leftX - dp(10 + 5) && x <= leftX + dp(5)) {
                return HANDLE_VIDEO_LEFT;
            } else if (x >= rightX - dp(5) && x <= rightX + dp(10 + 5)) {
                return HANDLE_VIDEO_RIGHT;
            } else if (x >= leftX && x <= rightX && (videoLeft > 0.01f || videoRight < .99f)) {
                return HANDLE_VIDEO_REGION;
            }
        } else if (hasAudio) {
            float leftX = px + ph + (audioOffset + audioLeft * audioDuration - scroll) / (float) scrollWidth * sw;
            float rightX = px + ph + (audioOffset + audioRight * audioDuration - scroll) / (float) scrollWidth * sw;

            if (audioSelected || !hasVideo) {
                if (x >= leftX - dp(10 + 5) && x <= leftX + dp(5)) {
                    return HANDLE_AUDIO_LEFT;
                } else if (x >= rightX - dp(5) && x <= rightX + dp(10 + 5)) {
                    return HANDLE_AUDIO_RIGHT;
                } else if (x >= leftX && x <= rightX) {
                    final float maxDuration = Math.min(MAX_SCROLL_DURATION, getBaseDuration());
                    final float minLeft = Math.max(0, scroll - audioOffset) / (float) audioDuration;
                    final float maxRight = Math.min(1, Math.max(0, scroll - audioOffset + maxDuration) / (float) audioDuration);
                    if (!hasVideo) {
                        return HANDLE_AUDIO_REGION;
                    } else {
                        return HANDLE_AUDIO_SCROLL;
                    }
                }
                leftX = px + ph + (audioOffset - scroll) / (float) scrollWidth * sw;
                rightX = px + ph + (audioOffset + audioDuration - scroll) / (float) scrollWidth * sw;
            }
            if (x >= leftX && x <= rightX) {
                return HANDLE_AUDIO_SCROLL;
            }
        }

        if (videoDuration > MAX_SCROLL_DURATION && isInVideo) {
            return HANDLE_VIDEO_SCROLL;
        }

        return -1;
    }

    public boolean onBackPressed() {
        if (audioSelected) {
            audioSelected = false;
            return true;
        }
        return false;
    }

    public boolean isDragging() {
        return dragged;
    }

    private Runnable askExactSeek;
    private boolean setProgressAt(float x, boolean fast) {
        if (!hasVideo && !hasAudio) {
            return false;
        }

        final long scrollWidth = Math.min(getBaseDuration(), MAX_SCROLL_DURATION);
        final float t = (x - px - ph) / sw;
        long progress = (long) Utilities.clamp(t * scrollWidth + (!hasVideo ? -audioOffset : 0) + scroll, hasVideo ? videoDuration : audioDuration, 0);
        if (hasVideo && (progress / (float) videoDuration < videoLeft || progress / (float) videoDuration > videoRight)) {
            return false;
        }
        if (hasAudio && !hasVideo && (progress / (float) audioDuration < audioLeft || progress / (float) audioDuration > audioRight)) {
            return false;
        }
        this.progress = progress;
        invalidate();
        if (delegate != null) {
            delegate.onProgressChange(progress, fast);
        }
        if (askExactSeek != null) {
            AndroidUtilities.cancelRunOnUIThread(askExactSeek);
            askExactSeek = null;
        }
        if (fast) {
            AndroidUtilities.runOnUIThread(askExactSeek = () -> {
                if (delegate != null) {
                    delegate.onProgressChange(progress, false);
                }
            }, 150);
        }
        return true;
    }

    private float getVideoHeight() {
        if (!hasVideo)
            return 0;
        float audioSelected = this.audioSelectedT.set(this.audioSelected);
        return lerp(dp(28), dp(38), 1f - audioSelected);
    }

    private float getAudioHeight() {
        float audioSelected = this.audioSelectedT.set(this.audioSelected);
        return lerp(dp(28), dp(38), audioSelected);
    }

    private long lastTime;
    private long pressTime;
    private float lastX;
    private int pressHandle = -1;
    private boolean pressVideo = true;
    private boolean draggingProgress, dragged;
    private boolean hadDragChange;
    private VelocityTracker velocityTracker;
    private boolean scrollingVideo = true;
    private boolean scrolling = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!hasVideo && !hasAudio) {
            return false;
        }

        if (hasVideo && !hasAudio && event.getAction() == MotionEvent.ACTION_DOWN && event.getY() < h - py - getVideoHeight() - py) {
            return false;
        }

        if (hasAudio && !hasVideo && event.getAction() == MotionEvent.ACTION_DOWN && event.getY() < h - py - getAudioHeight() - py) {
            return false;
        }

        final long now = System.currentTimeMillis();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (askExactSeek != null) {
                AndroidUtilities.cancelRunOnUIThread(askExactSeek);
                askExactSeek = null;
            }
            scroller.abortAnimation();
            pressHandle = detectHandle(event);
            pressVideo = !hasAudio || event.getY() > h - py - getVideoHeight() - (hasVideo ? dp(4) : 0);
            pressTime = System.currentTimeMillis();
            draggingProgress = pressHandle == HANDLE_PROGRESS || pressHandle == -1 || pressHandle == HANDLE_VIDEO_SCROLL;
            hadDragChange = false;
            if (pressHandle == HANDLE_VIDEO_SCROLL || pressHandle == HANDLE_AUDIO_SCROLL || pressHandle == HANDLE_AUDIO_REGION) {
                velocityTracker = VelocityTracker.obtain();
            } else if (velocityTracker != null) {
                velocityTracker.recycle();
                velocityTracker = null;
            }
            dragged = false;
            lastX = event.getX();
            AndroidUtilities.cancelRunOnUIThread(this.onLongPress);
            AndroidUtilities.runOnUIThread(this.onLongPress, ViewConfiguration.getLongPressTimeout());
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            final float Δx = event.getX() - lastX;
            final boolean allowDrag = dragged || Math.abs(Δx) > AndroidUtilities.touchSlop;
            if (allowDrag) {
                final long videoScrollDuration = Math.min(getBaseDuration(), MAX_SCROLL_DURATION);
                if (pressHandle == HANDLE_VIDEO_SCROLL) {
                    scroll = (long) Utilities.clamp(scroll - Δx / sw * videoScrollDuration, videoDuration - videoScrollDuration, 0);
                    invalidate();
                    dragged = true;
                    draggingProgress = false;
                } else if (pressHandle == HANDLE_VIDEO_LEFT || pressHandle == HANDLE_VIDEO_RIGHT || pressHandle == HANDLE_VIDEO_REGION) {
                    float d = Δx / sw * (videoScrollDuration / (float) videoDuration);
                    if (pressHandle == HANDLE_VIDEO_LEFT) {
                        videoLeft = Utilities.clamp(videoLeft + d, videoRight - MIN_SELECT_DURATION / (float) videoDuration, 0);
                        if (delegate != null) {
                            delegate.onVideoLeftChange(videoLeft);
                        }
                        if (videoRight - videoLeft > MAX_SELECT_DURATION / (float) videoDuration) {
                            videoRight = Math.min(1, videoLeft + MAX_SELECT_DURATION / (float) videoDuration);
                            if (delegate != null) {
                                delegate.onVideoRightChange(videoRight);
                            }
                        }
                    } else if (pressHandle == HANDLE_VIDEO_RIGHT) {
                        videoRight = Utilities.clamp(videoRight + d, 1, videoLeft + MIN_SELECT_DURATION / (float) videoDuration);
                        if (delegate != null) {
                            delegate.onVideoRightChange(videoRight);
                        }
                        if (videoRight - videoLeft > MAX_SELECT_DURATION / (float) videoDuration) {
                            videoLeft = Math.max(0, videoRight - MAX_SELECT_DURATION / (float) videoDuration);
                            if (delegate != null) {
                                delegate.onVideoLeftChange(videoLeft);
                            }
                        }
                    } else if (pressHandle == HANDLE_VIDEO_REGION) {
                        if (d > 0) {
                            d = Math.min(1 - videoRight, d);
                        } else {
                            d = Math.max(-videoLeft, d);
                        }
                        videoLeft += d;
                        videoRight += d;
                        if (delegate != null) {
                            delegate.onVideoLeftChange(videoLeft);
                            delegate.onVideoRightChange(videoRight);
                        }
                    }
                    if (progress / (float) videoDuration < videoLeft || progress / (float) videoDuration > videoRight) {
                        progress = (long) (videoLeft * videoDuration);
                        if (delegate != null) {
                            delegate.onProgressChange(progress, false);
                        }
                    }
                    invalidate();
                    dragged = true;
                    draggingProgress = false;
                } else if (pressHandle == HANDLE_AUDIO_LEFT || pressHandle == HANDLE_AUDIO_RIGHT || pressHandle == HANDLE_AUDIO_REGION) {
                    float d = Δx / sw * (videoScrollDuration / (float) audioDuration);
                    if (pressHandle == HANDLE_AUDIO_LEFT) {
                        float maxValue = audioRight - minAudioSelect() / (float) audioDuration;
                        float minValue = Math.max(0, scroll - audioOffset) / (float) audioDuration;
                        if (!hasVideo) {
                            minValue = Math.max(minValue, audioRight - MAX_SELECT_DURATION / (float) audioDuration);
                            if (!hadDragChange && d < 0 && audioLeft <= (audioRight - MAX_SELECT_DURATION / (float) audioDuration)) {
                                pressHandle = HANDLE_AUDIO_REGION;
                            }
                        } else {
                            minValue = Math.max(minValue, (videoLeft * videoDuration + scroll - audioOffset) / (float) audioDuration);
                        }
                        float wasAudioLeft = audioLeft;
                        audioLeft = Utilities.clamp(audioLeft + d, maxValue, minValue);
                        if (Math.abs(wasAudioLeft - audioLeft) > 0.01f) {
                            hadDragChange = true;
                        }
                        if (delegate != null) {
                            delegate.onAudioOffsetChange(audioOffset + (long) (audioLeft * audioDuration));
                        }
                        if (delegate != null) {
                            delegate.onAudioLeftChange(audioLeft);
                        }
                    } else if (pressHandle == HANDLE_AUDIO_RIGHT) {
                        float maxValue = Math.min(1, Math.max(0, scroll - audioOffset + videoScrollDuration) / (float) audioDuration);
                        float minValue = audioLeft + minAudioSelect() / (float) audioDuration;
                        if (!hasVideo) {
                            maxValue = Math.min(maxValue, audioLeft + MAX_SELECT_DURATION / (float) audioDuration);
                            if (!hadDragChange && d > 0 && audioRight >= (audioLeft + MAX_SELECT_DURATION / (float) audioDuration)) {
                                pressHandle = HANDLE_AUDIO_REGION;
                            }
                        } else {
                            maxValue = Math.min(maxValue, (videoRight * videoDuration + scroll - audioOffset) / (float) audioDuration);
                        }
                        float wasAudioRight = audioRight;
                        audioRight = Utilities.clamp(audioRight + d, maxValue, minValue);
                        if (Math.abs(wasAudioRight - audioRight) > 0.01f) {
                            hadDragChange = true;
                        }
                        if (delegate != null) {
                            delegate.onAudioRightChange(audioRight);
                        }
                    }
                    if (pressHandle == HANDLE_AUDIO_REGION) {
                        float minLeft = Math.max(0, scroll - audioOffset) / (float) audioDuration;
                        float maxRight = Math.min(1, Math.max(0, scroll - audioOffset + videoScrollDuration) / (float) audioDuration);
                        if (d > 0) {
                            d = Math.min(maxRight - audioRight, d);
                        } else {
                            d = Math.max(minLeft - audioLeft, d);
                        }
                        audioLeft += d;
                        audioRight += d;

                        if (delegate != null) {
                            delegate.onAudioLeftChange(audioLeft);
                            delegate.onAudioOffsetChange(audioOffset + (long) (audioLeft * audioDuration));
                            delegate.onAudioRightChange(audioRight);
                        }
                        if (delegate != null) {
                            delegate.onProgressDragChange(true);
                        }
                    }
                    if (!hasVideo && (progress / (float) audioDuration < audioLeft || progress / (float) audioDuration > audioRight)) {
                        progress = (long) (audioLeft * audioDuration);
                        if (delegate != null) {
                            delegate.onProgressDragChange(true);
                            delegate.onProgressChange(progress, false);
                        }
                    }
                    invalidate();
                    dragged = true;
                    draggingProgress = false;
                } else if (pressHandle == HANDLE_AUDIO_SCROLL) {
                    float d = Δx / sw * videoScrollDuration;
                    moveAudioOffset(d);
                    dragged = true;
                    draggingProgress = false;
                } else if (draggingProgress) {
                    setProgressAt(event.getX(), now - lastTime < 350);
                    if (!dragged && delegate != null) {
                        delegate.onProgressDragChange(true);
                    }
                    dragged = true;
                }
                lastX = event.getX();
            }
            if (dragged) {
                AndroidUtilities.cancelRunOnUIThread(this.onLongPress);
            }
            if ((pressHandle == HANDLE_VIDEO_SCROLL || pressHandle == HANDLE_AUDIO_SCROLL || pressHandle == HANDLE_AUDIO_REGION) && velocityTracker != null) {
                velocityTracker.addMovement(event);
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            AndroidUtilities.cancelRunOnUIThread(this.onLongPress);
            scroller.abortAnimation();
            boolean scrollStopped = true;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (System.currentTimeMillis() - pressTime <= ViewConfiguration.getTapTimeout() && !dragged) {
                    if (!pressVideo && !audioSelected) {
                        audioSelected = true;
                        invalidate();
                    } else if (pressVideo && audioSelected) {
                        audioSelected = false;
                        invalidate();
                    } else {
                        long wasProgress = progress;
                        if (setProgressAt(event.getX(), false) && Math.abs(progress - wasProgress) > 400) {
                            loopProgressFrom = wasProgress;
                            loopProgress.set(1, true);
                            invalidate();
                        }
                    }
                } else if (pressHandle == HANDLE_VIDEO_SCROLL && velocityTracker != null) {
                    velocityTracker.computeCurrentVelocity(1000);
                    final int velocity = (int) velocityTracker.getXVelocity();
                    scrollingVideo = true;
                    if (Math.abs(velocity) > dp(100)) {
                        final long videoScrollDuration = Math.min(videoDuration, MAX_SCROLL_DURATION);
                        final int scrollX = (int) (px + scroll / (float) videoScrollDuration * sw);
                        final int maxScrollX = (int) (px + (videoDuration - videoScrollDuration) / (float) videoScrollDuration * sw);
                        scrolling = true;
                        scroller.fling(wasScrollX = scrollX, 0, -velocity, 0, px, maxScrollX, 0, 0);
                        scrollStopped = false;
                    }
                } else if ((pressHandle == HANDLE_AUDIO_SCROLL || pressHandle == HANDLE_AUDIO_REGION && !dragged) && hasVideo && audioSelected && velocityTracker != null) {
                    velocityTracker.computeCurrentVelocity(1000);
                    final int velocity = (int) velocityTracker.getXVelocity();
                    scrollingVideo = false;
                    if (Math.abs(velocity) > dp(100)) {
                        final long videoScrollDuration = Math.min(getBaseDuration(), MAX_SCROLL_DURATION);
                        final int scrollX = (int) (px + ph + audioOffset / (float) videoScrollDuration * sw);
                        final long mx = (long) Math.max(getBaseDuration(), audioDuration);
                        final long mn = (long) Math.min(getBaseDuration(), audioDuration);
                        scrolling = true;
                        scroller.fling(wasScrollX = scrollX, 0, velocity, 0, (int) (px + ph + (long) ((videoLeft * videoDuration) - (1 * audioDuration)) / (float) videoScrollDuration * sw), (int) (px + ph + (long) ((videoRight * videoDuration) - (0 * audioDuration)) / (float) videoScrollDuration * sw), 0, 0);
                        scrollStopped = false;
                    }
                }
            }
            if (askExactSeek != null) {
                AndroidUtilities.cancelRunOnUIThread(askExactSeek);
                askExactSeek = null;
            }
            if (dragged && scrollStopped && delegate != null) {
                delegate.onProgressDragChange(false);
            }
            dragged = false;
            draggingProgress = false;
            pressTime = -1;
            pressHandle = -1;
            if (velocityTracker != null) {
                velocityTracker.recycle();
                velocityTracker = null;
            }
        }
        lastTime = System.currentTimeMillis();
        return true;
    }

    private long minAudioSelect() {
        return (long) Math.max(MIN_SELECT_DURATION, Math.min(videoDuration, MAX_SELECT_DURATION) * 0.15f);
    }

    private void moveAudioOffset(final float d) {
        final long videoScrollDuration = Math.min(getBaseDuration(), MAX_SCROLL_DURATION);
        if (!hasVideo) {
            audioOffset = Utilities.clamp(audioOffset + (long) d, 0, (long) -(audioDuration - Math.min(getBaseDuration(), MAX_SCROLL_DURATION)));
        } else if (audioSelected) {
            long mx = (long) ((videoRight * videoDuration) - (audioRight * audioDuration));
            long mn = (long) ((videoLeft * videoDuration) - (audioLeft * audioDuration));
            final float wasDuration = Math.min(audioRight - audioLeft, (videoRight - videoLeft) * videoDuration / (float) audioDuration);
            if (audioOffset + (long) d > mx) {
                audioRight = Utilities.clamp((videoRight * videoDuration - audioOffset - (long) d) / (float) audioDuration, 1, wasDuration);
                audioLeft = Utilities.clamp(audioRight - wasDuration, 1, 0);
                long mmx = (long) ((videoRight * videoDuration) - (audioRight * audioDuration));
                long mmn = (long) ((videoLeft * videoDuration) - (audioLeft * audioDuration));
                if (mmx < mmn) {
                    long t = mmx;
                    mmx = mmn;
                    mmn = t;
                }
                audioOffset = Utilities.clamp(audioOffset + (long) d, mmx, mmn);
                if (delegate != null) {
                    delegate.onAudioLeftChange(audioLeft);
                    delegate.onAudioRightChange(audioRight);
                }
            } else if (audioOffset + (long) d < mn) {
                audioLeft = Utilities.clamp((videoLeft * videoDuration - audioOffset - (long) d) / (float) audioDuration, 1 - wasDuration, 0);
                audioRight = Utilities.clamp(audioLeft + wasDuration, 1, 0);
                long mmx = (long) ((videoRight * videoDuration) - (audioRight * audioDuration));
                long mmn = (long) ((videoLeft * videoDuration) - (audioLeft * audioDuration));
                if (mmx < mmn) {
                    long t = mmx;
                    mmx = mmn;
                    mmn = t;
                }
                audioOffset = Utilities.clamp(audioOffset + (long) d, mmx, mmn);
                if (delegate != null) {
                    delegate.onAudioLeftChange(audioLeft);
                    delegate.onAudioRightChange(audioRight);
                }
            } else {
                audioOffset += (long) d;
            }
        } else {
            audioOffset = Utilities.clamp(audioOffset + (long) d, (long) (getBaseDuration() - audioDuration * audioRight), (long) (-audioLeft * audioDuration));
        }
        if (!hasVideo && (progress / (float) audioDuration < audioLeft || progress / (float) audioDuration > audioRight)) {
            progress = (long) (audioLeft * audioDuration);
            if (delegate != null) {
                delegate.onProgressChange(progress, false);
            }
        }
        invalidate();
        if (delegate != null) {
            delegate.onAudioOffsetChange(audioOffset + (long) (audioLeft * audioDuration));
        }
        if (!dragged && delegate != null) {
            delegate.onProgressDragChange(true);

            if (hasVideo) {
                long progressToStart = Utilities.clamp(audioOffset + (long) (audioLeft * audioDuration), (long) (videoRight * videoDuration), (long) (videoLeft * videoDuration));
                if (Math.abs(progress - progressToStart) > 400) {
                    loopProgressFrom = progress;
                    loopProgress.set(1, true);
                }
                delegate.onProgressChange(progress = progressToStart, false);
            }
        } else if ((dragged || scrolling) && hasVideo) {
            progress = Utilities.clamp(audioOffset + (long) (audioLeft * audioDuration), (long) (videoRight * videoDuration), (long) (videoLeft * videoDuration));
            if (delegate != null) {
                delegate.onProgressChange(progress, false);
            }
        }
    }

    private int wasScrollX;
    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            int scrollX = scroller.getCurrX();
            final long videoScrollDuration = Math.min(getBaseDuration(), MAX_SCROLL_DURATION);
            if (scrollingVideo) {
                long wasScroll = scroll;
                scroll = (long) Math.max(0, ((scrollX - px - ph) / (float) sw * videoScrollDuration));
//                videoLeft = Utilities.clamp(videoLeft + (scroll - wasScroll) / (float) videoDuration, 1, 0);
//                videoRight = Utilities.clamp(videoRight + (scroll - wasScroll) / (float) videoDuration, 1, 0);
//                if (delegate != null) {
//                    delegate.onVideoLeftChange(videoLeft);
//                    delegate.onVideoRightChange(videoRight);
//                }
            } else {
                final float d = ((scrollX - px - ph) / (float) sw * videoScrollDuration) - ((wasScrollX - px - ph) / (float) sw * videoScrollDuration);
                moveAudioOffset(d);
            }
            invalidate();
            wasScrollX = scrollX;
        } else if (scrolling) {
            scrolling = false;
            if (delegate != null) {
                delegate.onProgressDragChange(false);
            }
        }
    }

    class WaveformPath extends Path {

        private int lastWaveformCount;
        private float lastAnimatedLoaded;
        private long lastScrollDuration;
        private float lastAudioHeight;
        private float lastMaxBar;
        private float lastAudioSelected;
        private float lastBottom;
        private float lastStart;
        private float lastLeft;
        private float lastRight;

        public void check(
            float start, float left, float right,
            float audioSelected,
            float animatedLoaded,
            long scrollDuration,
            float audioHeight,
            float maxBar,
            float bottom
        ) {
            if (waveform == null) {
                rewind();
                return;
            }
            if (lastWaveformCount != waveform.getCount() ||
                Math.abs(lastAnimatedLoaded - animatedLoaded) > 0.01f ||
                lastScrollDuration != scrollDuration ||
                Math.abs(lastAudioHeight - audioHeight) > 1f ||
                Math.abs(lastMaxBar - maxBar) > 0.01f ||
                Math.abs(lastAudioSelected - audioSelected) > 0.1f ||
                Math.abs(lastBottom - bottom) > 1f ||
                Math.abs(lastStart - start) > 1f ||
                Math.abs(lastLeft - left) > 1f ||
                Math.abs(lastRight - right) > 1f
            ) {
                lastWaveformCount = waveform.getCount();
                layout(
                    lastStart = start, lastLeft = left, lastRight = right,
                    lastAudioSelected = audioSelected,
                    lastAnimatedLoaded = animatedLoaded,
                    lastScrollDuration = scrollDuration,
                    lastMaxBar = maxBar,
                    lastAudioHeight = audioHeight,
                    lastBottom = bottom
                );
            }
        }

        private void layout(
            float start, float left, float right,
            float audioSelected,
            float animatedLoaded,
            long scrollDuration,
            float maxBar,
            float audioHeight,
            float bottom
        ) {
            waveformPath.rewind();
            final float barWidth = Math.round(dpf2(3.3333f));
            int from = Math.max(0, (int) ((left - ph - start) / barWidth));
            int to = Math.min(waveform.getCount() - 1, (int) Math.ceil((right + ph - start) / barWidth));
            for (int i = from; i <= to; ++i) {
                float x = start + i * barWidth + dp(2);
                float h = maxBar <= 0 ? 0 : waveform.getBar(i) / (float) maxBar * audioHeight * .6f;
                if (i < animatedLoaded && i + 1 > animatedLoaded) {
                    h *= (animatedLoaded - i);
                } else if (i > animatedLoaded) {
                    h = 0;
                }
                if (x < left || x > right) {
                    h *= audioSelected;
                    if (h <= 0) {
                        continue;
                    }
                }
                h = Math.max(h, lerp(dpf2(0.66f), dpf2(1.5f), audioSelected));
                AndroidUtilities.rectTmp.set(
                        x,
                        lerp(bottom - h, bottom - (audioHeight + h) / 2f, audioSelected),
                        x + dpf2(1.66f),
                        lerp(bottom, bottom - (audioHeight - h) / 2f, audioSelected)
                );
                waveformPath.addRoundRect(AndroidUtilities.rectTmp, waveformRadii, Path.Direction.CW);
            }
        }
    }

    final float[] selectedVideoRadii = new float[8];
    final float[] waveformRadii = new float[8];

    @Override
    protected void dispatchDraw(Canvas canvas) {
        final Paint blurPaint = backgroundBlur.getPaint(1f);

        final long scrollDuration = Math.min(getBaseDuration(), MAX_SCROLL_DURATION);
        float videoHeight = 0;
        float videoT = hasVideo ? 1 : 0;

        // draw video thumbs
        if (hasVideo) {
            canvas.save();
            videoHeight = getVideoHeight();
            final float videoStartX = (videoDuration <= 0 ? 0 : px + ph - scroll / (float) scrollDuration * sw) - ph;
            final float videoEndX = (videoDuration <= 0 ? 0 : px + ph + (videoDuration - scroll) / (float) scrollDuration * sw) + ph;
            videoBounds.set(videoStartX, h - py - videoHeight, videoEndX, h - py);
            videoClipPath.rewind();
            videoClipPath.addRoundRect(videoBounds, dp(8), dp(8), Path.Direction.CW);
            canvas.clipPath(videoClipPath);
            if (thumbs != null) {
                float x = videoStartX;
                final int frameWidth = thumbs.getFrameWidth();
                final int fromFrame = (int) Math.max(0, Math.floor((videoStartX - px) / frameWidth));
                final int toFrame = (int) Math.min(thumbs.count, Math.ceil(((videoEndX - videoStartX) - px) / frameWidth) + 1);

                final int y = (int) (h - py - videoHeight);

                boolean allLoaded = thumbs.frames.size() >= toFrame;
                boolean fullyCovered = allLoaded;
                if (fullyCovered) {
                    for (int i = fromFrame; i < Math.min(thumbs.frames.size(), toFrame); ++i) {
                        VideoThumbsLoader.BitmapFrame frame = thumbs.frames.get(i);
                        if (frame.bitmap == null) {
                            fullyCovered = false;
                            break;
                        }
                    }
                }

                if (!fullyCovered) {
                    if (blurPaint == null) {
                        canvas.drawColor(0x40000000);
                    } else {
                        canvas.drawRect(videoBounds, blurPaint);
                        canvas.drawColor(0x33000000);
                    }
                }

                for (int i = fromFrame; i < Math.min(thumbs.frames.size(), toFrame); ++i) {
                    VideoThumbsLoader.BitmapFrame frame = thumbs.frames.get(i);
                    if (frame.bitmap != null) {
                        videoFramePaint.setAlpha((int) (0xFF * frame.getAlpha()));
                        canvas.drawBitmap(frame.bitmap, x, y - (int) ((frame.bitmap.getHeight() - videoHeight) / 2f), videoFramePaint);
                    }
                    x += frameWidth;
                }

                if (!allLoaded) {
                    thumbs.load();
                }
            }
            selectedVideoClipPath.rewind();

            AndroidUtilities.rectTmp.set(
                px + ph + (videoLeft * videoDuration - scroll) / (float) scrollDuration * sw - (videoLeft <= 0 ? ph : 0),
                h - py - videoHeight,
                px + ph + (videoRight * videoDuration - scroll) / (float) scrollDuration * sw + (videoRight >= 1 ? ph : 0),
                h - py
            );
            selectedVideoClipPath.addRoundRect(
                AndroidUtilities.rectTmp,
                selectedVideoRadii,
                Path.Direction.CW
            );
            canvas.clipPath(selectedVideoClipPath, Region.Op.DIFFERENCE);
            canvas.drawColor(0x50000000);
            canvas.restore();
        }

        // draw audio
        float audioT = this.audioT.set(hasAudio);
        float audioSelected = this.audioSelectedT.set(hasAudio && this.audioSelected);
        final float p = dp(4);
        final float audioHeight = getAudioHeight() * audioT;
        if (audioT > 0) {
            final Paint audioBlurPaint = audioBlur.getPaint(audioT);
            canvas.save();
            float left, right;
            if (hasVideo) {
                left = px + ph + (audioOffset - scroll + lerp(audioLeft, 0, audioSelected) * audioDuration) / (float) scrollDuration * sw;
                right = px + ph + (audioOffset - scroll + lerp(audioRight, 1, audioSelected) * audioDuration) / (float) scrollDuration * sw;
            } else {
                left = px + ph + (audioOffset - scroll) / (float) scrollDuration * sw;
                right = px + ph + (audioOffset - scroll + audioDuration) / (float) scrollDuration * sw;
            }

            final float bottom = h - py - videoHeight - p * videoT;
            audioBounds.set(left - ph, bottom - audioHeight, right + ph, bottom);
            audioClipPath.rewind();
            audioClipPath.addRoundRect(audioBounds, dp(8), dp(8), Path.Direction.CW);
            canvas.clipPath(audioClipPath);

            if (audioBlurPaint == null) {
                canvas.drawColor(Theme.multAlpha(0x40000000, audioT));
            } else {
                canvas.drawRect(audioBounds, audioBlurPaint);
                canvas.drawColor(Theme.multAlpha(0x33000000, audioT));
            }

            if (waveform != null && audioBlurPaint != null) {
                Paint paint = audioWaveformBlur.getPaint(.4f * audioT);
                if (paint == null) {
                    paint = waveformPaint;
                    paint.setAlpha((int) (0x40 * audioT));
                }
                final float maxBar = waveformMax.set(waveform.getMaxBar(), !waveformIsLoaded);
                waveformIsLoaded = waveform.getLoadedCount() > 0;
                final float animatedLoaded = waveformLoaded.set(waveform.getLoadedCount());
                final float start = px + ph + (audioOffset - scroll) / (float) scrollDuration * sw;
                waveformPath.check(start, left, right, audioSelected, animatedLoaded, scrollDuration, audioHeight, maxBar, bottom);
                canvas.drawPath(waveformPath, paint);
            }

            if (audioSelected < 1) {
                final float tleft = px + ph + (audioOffset - scroll + audioLeft * audioDuration) / (float) scrollDuration * sw;
                final float tright = px + ph + (audioOffset - scroll + audioRight * audioDuration) / (float) scrollDuration * sw;

                final float textCx = (Math.max(px, tleft) + Math.min(w - px, tright)) / 2f;
                final float textCy = bottom - audioHeight + dp(28 / 2);
                final float textMaxWidth = Math.max(0, Math.min(w - px, tright) - Math.max(px, tleft) - dp(24));
                float textWidth = dpf2(13) + (audioAuthor == null && audioTitle == null ? 0 : dpf2(3.11f) + audioAuthorWidth + dpf2(3.66f + 2 + 4) + audioTitleWidth);
                final boolean fit = textWidth < textMaxWidth;

                float x = textCx - Math.min(textWidth, textMaxWidth) / 2f;
                audioIcon.setBounds((int) x, (int) (textCy - dp(13) / 2f), (int) (x + dp(13)), (int) (textCy + dp(13) / 2f));
                audioIcon.setAlpha((int) (0xFF * (1f - audioSelected)));
                audioIcon.draw(canvas);
                x += dpf2(13 + 3.11f);
                canvas.saveLayerAlpha(0, 0, w, h, 0xFF, Canvas.ALL_SAVE_FLAG);
                final float x2 = Math.min(tright, w) - dp(12);
                canvas.clipRect(x, 0, x2, h);
                if (audioAuthor != null) {
                    canvas.save();
                    canvas.translate(x - audioAuthorLeft, textCy - audioAuthor.getHeight() / 2f);
                    audioAuthorPaint.setAlpha((int) (0xFF * (1f - audioSelected) * audioT));
                    audioAuthor.draw(canvas);
                    canvas.restore();
                    x += audioAuthorWidth;
                }
                if (audioAuthor != null && audioTitle != null) {
                    x += dpf2(3.66f);
                    int wasAlpha = audioDotPaint.getAlpha();
                    audioDotPaint.setAlpha((int) (wasAlpha * (1f - audioSelected)));
                    canvas.drawCircle(x + dp(1), textCy, dp(1), audioDotPaint);
                    audioDotPaint.setAlpha(wasAlpha);
                    x += dpf2(2);
                    x += dpf2(4);
                }
                if (audioTitle != null) {
                    canvas.save();
                    canvas.translate(x - audioTitleLeft, textCy - audioTitle.getHeight() / 2f);
                    audioTitlePaint.setAlpha((int) (0xFF * (1f - audioSelected) * audioT));
                    audioTitle.draw(canvas);
                    canvas.restore();
                }
                if (!fit) {
                    ellipsizeMatrix.reset();
                    ellipsizeMatrix.postScale(dpf2(8) / 16, 1);
                    ellipsizeMatrix.postTranslate(x2 - dp(8), 0);
                    ellipsizeGradient.setLocalMatrix(ellipsizeMatrix);
                    canvas.drawRect(x2 - dp(8), bottom - audioHeight, x2, bottom, ellipsizePaint);
                }
                canvas.restore();
            }
            canvas.restore();
        }

        // draw region
        final float regionTop = lerp(h - py - videoHeight, h - py - videoHeight - p * videoT - audioHeight, hasVideo ? audioSelected : 1);
        final float regionBottom = lerp(h - py, h - py - videoHeight - p * videoT, audioSelected);
        final float left = lerp(videoLeft * videoDuration, audioOffset + audioLeft * audioDuration, hasVideo ? audioSelected : 1);
        final float right = lerp(videoRight * videoDuration, audioOffset + audioRight * audioDuration, hasVideo ? audioSelected : 1);
        float leftX = px + ph + (left - scroll) / (float) scrollDuration * sw;
        float rightX = px + ph + (right - scroll) / (float) scrollDuration * sw;
        float progressAlpha = (hasAudio && !hasVideo ? audioT : videoT);
        if (audioT > 0. || videoT > 0.) {
            drawRegion(canvas, blurPaint, regionTop, regionBottom, leftX, rightX, (hasVideo ? 1 : lerp(.6f, 1f, audioSelected) * audioT) * progressAlpha);
            if (hasVideo && hasAudio && audioSelected > 0) {
                drawRegion(
                    canvas,
                    blurPaint,
                    h - py - videoHeight,
                    h - py,
                    ph + px + (videoLeft * videoDuration - scroll) / (float) scrollDuration * sw,
                    ph + px + (videoRight * videoDuration - scroll) / (float) scrollDuration * sw,
                    .8f
                );
            }

            // draw progress
            float loopT = loopProgress.set(0);
            final float y1 = h - py - videoHeight - (audioHeight + p * videoT) * audioT - dpf2(4.3f);
            final float y2 = h - py + dpf2(4.3f);
            if (loopT > 0) {
                drawProgress(canvas, y1, y2, loopProgressFrom != -1 ? loopProgressFrom : (long) (hasVideo ? videoDuration * videoRight : audioDuration * audioRight), loopT * progressAlpha);
            }
            drawProgress(canvas, y1, y2, progress, (1f - loopT) * progressAlpha);
        }

        if (dragged) {
            long Δd = (long) (dp(32) / (float) sw * scrollDuration * (1f / (1000f / AndroidUtilities.screenRefreshRate)));
            if (pressHandle == HANDLE_VIDEO_REGION) {
                int direction = 0;
                if (videoLeft < (scroll / (float) videoDuration)) {
                    direction = -1;
                } else if (videoRight > ((scroll + scrollDuration) / (float) videoDuration)) {
                    direction = +1;
                }
                long wasScroll = scroll;
                scroll = Utilities.clamp(scroll + direction * Δd, videoDuration - scrollDuration, 0);
                progress += direction * Δd;
                float d = (scroll - wasScroll) / (float) videoDuration;
                if (d > 0) {
                    d = Math.min(1 - videoRight, d);
                } else {
                    d = Math.max(0 - videoLeft, d);
                }
                videoLeft = Utilities.clamp(videoLeft + d, 1, 0);
                videoRight = Utilities.clamp(videoRight + d, 1, 0);
                if (delegate != null) {
                    delegate.onVideoLeftChange(videoLeft);
                    delegate.onVideoRightChange(videoRight);
                }
                invalidate();
            } else if (pressHandle == HANDLE_AUDIO_REGION) {
                int direction = 0;
                if (audioLeft < ((-audioOffset + 100) / (float) audioDuration)) {
                    direction = -1;
                } else if (audioRight >= ((-audioOffset + scrollDuration - 100) / (float) audioDuration)) {
                    direction = +1;
                }
                if (direction != 0) {
                    long wasOffset = audioOffset;
                    if (this.audioSelected && hasVideo) {
                        audioOffset = Utilities.clamp(audioOffset - direction * Δd, (long) ((videoRight * videoDuration) - (audioLeft * audioDuration)), (long) ((videoLeft * videoDuration) - (audioRight * audioDuration)));
                    } else {
                        audioOffset = Utilities.clamp(audioOffset - direction * Δd, 0, (long) -(audioDuration - Math.min(getBaseDuration(), MAX_SCROLL_DURATION)));
                    }
                    float d = -(audioOffset - wasOffset) / (float) audioDuration;
                    if (d > 0) {
                        d = Math.min(1 - audioRight, d);
                    } else {
                        d = Math.max(0 - audioLeft, d);
                    }
                    if (!hasVideo) {
                        progress = (long) Utilities.clamp(progress + d * audioDuration, audioDuration, 0);
                    }
                    audioLeft = Utilities.clamp(audioLeft + d, 1, 0);
                    audioRight = Utilities.clamp(audioRight + d, 1, 0);
                    if (delegate != null) {
                        delegate.onAudioLeftChange(audioLeft);
                        delegate.onAudioRightChange(audioRight);
                        delegate.onProgressChange(progress, false);
                    }
                    invalidate();
                }
            }
        }
    }

    private void drawRegion(Canvas canvas, Paint blurPaint, float top, float bottom, float left, float right, float alpha) {
        if (alpha <= 0) {
            return;
        }

        AndroidUtilities.rectTmp.set(left - dp(10), top, right + dp(10), bottom);
        canvas.saveLayerAlpha(0, 0, w, h, 0xFF, Canvas.ALL_SAVE_FLAG);
        regionPaint.setAlpha((int) (0xFF * alpha));
        canvas.drawRoundRect(AndroidUtilities.rectTmp, dp(6), dp(6), regionPaint);
        AndroidUtilities.rectTmp.inset(dp(10), dp(2));
        canvas.drawRect(AndroidUtilities.rectTmp, regionCutPaint);

        final float hw = dp(2), hh = dp(10);
        Paint handlePaint = blurPaint != null ? blurPaint : regionHandlePaint;
        regionHandlePaint.setAlpha(0xFF);
        handlePaint.setAlpha((int) (0xFF * alpha));
        AndroidUtilities.rectTmp.set(
                left - (dp(10) - hw) / 2f,
                (top + bottom - hh) / 2f,
                left - (dp(10) + hw) / 2f,
                (top + bottom + hh) / 2f
        );
        canvas.drawRoundRect(AndroidUtilities.rectTmp, dp(1), dp(1), handlePaint);
        if (blurPaint != null) {
            regionHandlePaint.setAlpha((int) (0x30 * alpha));
            canvas.drawRoundRect(AndroidUtilities.rectTmp, dp(1), dp(1), regionHandlePaint);
        }
        AndroidUtilities.rectTmp.set(
                right + (dp(10) - hw) / 2f,
                (top + bottom - hh) / 2f,
                right + (dp(10) + hw) / 2f,
                (top + bottom + hh) / 2f
        );
        canvas.drawRoundRect(AndroidUtilities.rectTmp, dp(1), dp(1), handlePaint);
        if (blurPaint != null) {
            regionHandlePaint.setAlpha((int) (0x30 * alpha));
            canvas.drawRoundRect(AndroidUtilities.rectTmp, dp(1), dp(1), regionHandlePaint);
        }

        canvas.restore();
    }

    private void drawProgress(Canvas canvas, float y1, float y2, long progress, float scale) {
        final long scrollDuration = Math.min(getBaseDuration(), MAX_SCROLL_DURATION);

        final float progressT = (Utilities.clamp(progress, getBaseDuration(), 0) + (!hasVideo ? audioOffset : 0) - scroll) / (float) scrollDuration;
        final float progressX = px + ph + sw * progressT;

        float yd = (y1 + y2) / 2;
        y1 += yd / 2f * (1f - scale);
        y2 -= yd / 2f * (1f - scale);
        progressShadowPaint.setAlpha((int) (0x26 * scale));
        progressWhitePaint.setAlpha((int) (0xFF * scale));

        AndroidUtilities.rectTmp.set(progressX - dpf2(1.5f), y1, progressX + dpf2(1.5f), y2);
        AndroidUtilities.rectTmp.inset(-dpf2(0.66f), -dpf2(0.66f));
        canvas.drawRoundRect(AndroidUtilities.rectTmp, dp(6), dp(6), progressShadowPaint);
        AndroidUtilities.rectTmp.set(progressX - dpf2(1.5f), y1, progressX + dpf2(1.5f), y2);
        canvas.drawRoundRect(AndroidUtilities.rectTmp, dp(6), dp(6), progressWhitePaint);
    }

    private int sw;
    private int w, h, ph, px, py;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        audioAuthorPaint.setTextSize(dp(12));
        audioTitlePaint.setTextSize(dp(12));
        waveformRadii[0] = waveformRadii[1] = waveformRadii[2] = waveformRadii[3] = dp(2);
        waveformRadii[4] = waveformRadii[5] = waveformRadii[6] = waveformRadii[7] = 0;
        setPadding(px = dp(12), py = dp(5), dp(12), dp(5));
        setMeasuredDimension(w = MeasureSpec.getSize(widthMeasureSpec), h = dp(80));
        ph = dp(10);
        sw = w - 2 * ph - 2 * px;
        if (videoPath != null && this.thumbs == null) {
            setupVideoThumbs();
        }
        if (audioPath != null && this.waveform == null) {
            setupAudioWaveform();
        }
    }

    private class VideoThumbsLoader {

        private long duration;
        private final long frameIterator;
        private final int count;

        private final ArrayList<BitmapFrame> frames = new ArrayList<>();
        private MediaMetadataRetriever metadataRetriever;

        private final int frameWidth;
        private final int frameHeight;

        private boolean destroyed;

        public VideoThumbsLoader(String path, int uiWidth, int uiHeight) {
            metadataRetriever = new MediaMetadataRetriever();
            long duration = MAX_SCROLL_DURATION;
            int width = 0;
            int height = 0;
            try {
                metadataRetriever.setDataSource(path);
                String value;
                value = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                if (value != null) {
                    this.duration = duration = Long.parseLong(value);
                }
                value = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                if (value != null) {
                    width = Integer.parseInt(value);
                }
                value = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                if (value != null) {
                    height = Integer.parseInt(value);
                }
                value = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
                if (value != null) {
                    int orientation = Integer.parseInt(value);
                    if (orientation == 90 || orientation == 270) {
                        int temp = width;
                        width = height;
                        height = temp;
                    }
                }
            } catch (Exception e) {
                metadataRetriever = null;
                FileLog.e(e);
            }
            float aspectRatio = 1;
            if (width != 0 && height != 0) {
                aspectRatio = width / (float) height;
            }
            aspectRatio = Utilities.clamp(aspectRatio, 4 / 3f, 9f / 16f);
            frameHeight = Math.max(1, uiHeight);
            frameWidth = Math.max(1, (int) Math.ceil(uiHeight * aspectRatio));
            final float uiScrollWidth = Math.max(duration, MAX_SCROLL_DURATION) / (float) MAX_SCROLL_DURATION * uiWidth;
            count = (int) Math.ceil(uiScrollWidth / frameWidth);
            frameIterator = (long) (duration / (float) count);
            nextFrame = -frameIterator;
            load();
        }

        public int getFrameWidth() {
            return frameWidth;
        }

        public long getDuration() {
            return duration;
        }

        public BitmapFrame getFrameAt(int index) {
            if (index < 0 || index >= frames.size()) {
                return null;
            }
            return frames.get(index);
        }

        public int getLoadedCount() {
            return frames.size();
        }

        public int getCount() {
            return count;
        }

        private long nextFrame;
        private boolean loading = false;

        // (ui) load() -> (themeQueue) retrieveFrame() -> (ui) receiveFrame()
        public void load() {
            if (loading || metadataRetriever == null || frames.size() >= count) {
                return;
            }
            loading = true;
            nextFrame += frameIterator;
            Utilities.themeQueue.cancelRunnable(this::retrieveFrame);
            Utilities.themeQueue.postRunnable(this::retrieveFrame);
        }

        private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        private void retrieveFrame() {
            if (metadataRetriever == null) {
                return;
            }

            Bitmap bitmap = null;
            try {
                bitmap = metadataRetriever.getFrameAtTime(nextFrame * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                if (bitmap != null) {
                    Bitmap scaledBitmap = Bitmap.createBitmap(frameWidth, frameHeight, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(scaledBitmap);
                    float scale = Math.max((float) frameWidth / bitmap.getWidth(), (float) frameHeight / bitmap.getHeight());
                    Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
                    Rect dest = new Rect(
                        (int) ((scaledBitmap.getWidth() -  bitmap.getWidth()  * scale) / 2f),
                        (int) ((scaledBitmap.getHeight() - bitmap.getHeight() * scale) / 2f),
                        (int) ((scaledBitmap.getWidth() +  bitmap.getWidth()  * scale) / 2f),
                        (int) ((scaledBitmap.getHeight() + bitmap.getHeight() * scale) / 2f)
                    );
                    canvas.drawBitmap(bitmap, src, dest, bitmapPaint);
                    bitmap.recycle();
                    bitmap = scaledBitmap;
                }
            } catch (Exception e) {
                FileLog.e(e);
            }

            final Bitmap finalBitmap = bitmap;
            AndroidUtilities.runOnUIThread(() -> receiveFrame(finalBitmap));
        };

        private void receiveFrame(Bitmap bitmap) {
            if (!loading || destroyed) {
                return;
            }
            frames.add(new BitmapFrame(bitmap));
            loading = false;
            invalidate();
        }

        public void destroy() {
            destroyed = true;
            Utilities.themeQueue.cancelRunnable(this::retrieveFrame);
            for (BitmapFrame frame : frames) {
                if (frame.bitmap != null) {
                    frame.bitmap.recycle();
                }
            }
            frames.clear();
            if (metadataRetriever != null) {
                try {
                    metadataRetriever.release();
                } catch (Exception e) {
                    metadataRetriever = null;
                    FileLog.e(e);
                }
            }
        }

        public class BitmapFrame {
            public Bitmap bitmap;
            private final AnimatedFloat alpha = new AnimatedFloat(0, TimelineView.this, 0, 240, CubicBezierInterpolator.EASE_OUT_QUINT);

            public BitmapFrame(Bitmap bitmap) {
                this.bitmap = bitmap;
            }

            public float getAlpha() {
                return alpha.set(1);
            }
        }
    }

    private class AudioWaveformLoader {

        private final int count;
        private int loaded = 0;
        private final short[] data;
        private short max;

        private final MediaExtractor extractor;
        private MediaFormat inputFormat;
        private long duration;

        private final Object lock = new Object();
        private boolean stop = false;

        private FfmpegAudioWaveformLoader waveformLoader;

        public AudioWaveformLoader(String path, int uiWidth) {

            extractor = new MediaExtractor();
            String mime = null;
            try {
                extractor.setDataSource(path);

                int numTracks = extractor.getTrackCount();
                for (int i = 0; i < numTracks; ++i) {
                    MediaFormat format = extractor.getTrackFormat(i);
                    mime = format.getString(MediaFormat.KEY_MIME);
                    if (mime != null && mime.startsWith("audio/")) {
                        extractor.selectTrack(i);
                        inputFormat = format;
                        break;
                    }
                }

                if (inputFormat != null) {
                    duration = inputFormat.getLong(MediaFormat.KEY_DURATION) / 1_000_000L;
                }
            } catch (Exception e) {
                FileLog.e(e);
            }

            final float videoScrollWidth = Math.min(hasVideo ? videoDuration : duration * 1000, MAX_SCROLL_DURATION);
            final float uiScrollWidth = (duration * 1000) / videoScrollWidth * uiWidth;
            final int sampleWidth = Math.round(dpf2(3.3333f));
            count = Math.round(uiScrollWidth / sampleWidth);
            data = new short[count];

            if (duration > 0 && inputFormat != null) {
                if ("audio/mpeg".equals(mime) || "audio/mp3".equals(mime)) {
                    waveformLoader = new FfmpegAudioWaveformLoader(path, count, this::receiveData);
                } else {
                    Utilities.phoneBookQueue.postRunnable(this::run);
                }
            }
        }

        private void run() {
            try {
                final int sampleRate = inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                final int skip = 4;
                final int barWidth = (int) Math.round(duration * sampleRate / (float) count / (1 + skip));
                final long chunkTime = 2500;
                String mime = inputFormat.getString(MediaFormat.KEY_MIME);
                final MediaCodec decoder = MediaCodec.createDecoderByType(mime);
                if (decoder == null) {
                    return;
                }
                decoder.configure(inputFormat, null, null, 0);
                decoder.start();

                ByteBuffer[] inputBuffers = decoder.getInputBuffers();
                ByteBuffer[] outputBuffers = decoder.getOutputBuffers();
                int outputBufferIndex = -1;

                int chunkindex = 0;
                short[] chunk = new short[32];

                int index = 0, count = 0;
                short peak = 0;
                boolean end = false;

                do {
                    MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                    int inputBufferIndex = decoder.dequeueInputBuffer(chunkTime);
                    if (inputBufferIndex >= 0) {
                        ByteBuffer inputBuffer;
                        if (Build.VERSION.SDK_INT < 21) {
                            inputBuffer = inputBuffers[inputBufferIndex];
                        } else {
                            inputBuffer = decoder.getInputBuffer(inputBufferIndex);
                        }
                        int size = extractor.readSampleData(inputBuffer, 0);
                        if (size < 0) {
                            decoder.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            end = true;
                        } else {
                            decoder.queueInputBuffer(inputBufferIndex, 0, size, extractor.getSampleTime(), 0);
                            extractor.advance();
                        }
                    }

                    if (outputBufferIndex >= 0) {
                        ByteBuffer outputBuffer;
                        if (Build.VERSION.SDK_INT < 21) {
                            outputBuffer = outputBuffers[outputBufferIndex];
                        } else {
                            outputBuffer = decoder.getOutputBuffer(outputBufferIndex);
                        }
                        // Ensure that the data is placed at the start of the buffer
                        outputBuffer.position(0);
                    }

                    outputBufferIndex = decoder.dequeueOutputBuffer(info, chunkTime);
                    while (outputBufferIndex != MediaCodec.INFO_TRY_AGAIN_LATER && !end) {
                        if (outputBufferIndex >= 0) {
                            ByteBuffer outputBuffer;
                            if (Build.VERSION.SDK_INT < 21) {
                                outputBuffer = outputBuffers[outputBufferIndex];
                            } else {
                                outputBuffer = decoder.getOutputBuffer(outputBufferIndex);
                            }
                            if (outputBuffer != null && info.size > 0) {
                                while (outputBuffer.remaining() > 0) {
                                    byte a = outputBuffer.get();
                                    byte b = outputBuffer.get();

                                    short value = (short) (((b & 0xFF) << 8) | (a & 0xFF));

                                    if (count >= barWidth) {
                                        chunk[index - chunkindex] = peak;
                                        index++;
                                        if (index - chunkindex >= chunk.length || index >= this.count) {
                                            short[] dataToSend = chunk;
                                            int length = index - chunkindex;
                                            chunk = new short[chunk.length];
                                            chunkindex = index;
                                            AndroidUtilities.runOnUIThread(() -> receiveData(dataToSend, length));
                                        }
                                        peak = 0;
                                        count = 0;
                                        if (index >= data.length) {
                                            break;
                                        }
                                    }

                                    if (peak < value) {
                                        peak = value;
                                    }
                                    count++;

                                    if (outputBuffer.remaining() < skip * 2)
                                        break;
                                    outputBuffer.position(outputBuffer.position() + skip * 2);
                                }
                            }
                            decoder.releaseOutputBuffer(outputBufferIndex, false);

                            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                                end = true;
                                break;
                            }

                        } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                            outputBuffers = decoder.getOutputBuffers();
                        }
                        outputBufferIndex = decoder.dequeueOutputBuffer(info, chunkTime);
                    }
                    synchronized (lock) {
                        if (stop) {
                            break;
                        }
                    }
                } while (!end && index < this.count);

                decoder.stop();
                decoder.release();
                extractor.release();
            } catch (Exception e) {
                FileLog.e(e);
            }
        }

        private void receiveData(short[] data, int len) {
            for (int i = 0; i < len; ++i) {
                if (loaded + i >= this.data.length) {
                    break;
                }
                this.data[loaded + i] = data[i];
                if (max < data[i]) {
                    max = data[i];
                }
            }
            loaded += len;
            invalidate();
        }

        public void destroy() {
            if (waveformLoader != null) {
                waveformLoader.destroy();
            }
            Utilities.phoneBookQueue.cancelRunnable(this::run);
            synchronized (lock) {
                stop = true;
            }
        }

        public short getMaxBar() {
            return max;
        }

        public short getBar(int index) {
            return data[index];
        }

        public int getLoadedCount() {
            return loaded;
        }

        public int getCount() {
            return count;
        }
    }
}
