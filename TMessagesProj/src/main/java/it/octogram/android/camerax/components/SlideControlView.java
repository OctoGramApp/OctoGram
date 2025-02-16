/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.camerax.components;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.Property;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.R;
import org.telegram.ui.Components.AnimationProperties;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SuppressLint("ViewConstructor")
public class SlideControlView extends View {

    private final Drawable progressDrawable;
    private final Drawable knobDrawable;
    private final Drawable pressedKnobDrawable;
    private final @SliderMode int mode;
    public boolean enabledTouch = true;
    private Drawable minusDrawable;
    private Drawable plusDrawable;
    private Drawable filledProgressDrawable;
    private int minusCx;
    private int minusCy;
    private int plusCx;
    private int plusCy;
    private int progressStartX;
    private int progressStartY;
    private int progressEndX;
    private int progressEndY;
    private float sliderValue;
    private boolean pressed;
    private boolean knobPressed;
    private float knobStartX;
    private float knobStartY;
    private float animatingToZoom;
    private AnimatorSet animatorSet;
    private SliderControlViewDelegate delegate;
    public final Property<SlideControlView, Float> SLIDER_PROPERTY = new AnimationProperties.FloatProperty<>("clipProgress") {
        @Override
        public void setValue(SlideControlView object, float value) {
            sliderValue = value;
            if (delegate != null) {
                delegate.didSlide(sliderValue);
            }
            invalidate();
        }

        @Override
        public Float get(SlideControlView object) {
            return sliderValue;
        }
    };

    public SlideControlView(Context context, @SliderMode int mode) {
        super(context);
        this.mode = mode;

        if (mode == SliderMode.SLIDER_MODE_ZOOM) {
            minusDrawable = getDrawable(context, R.drawable.zoom_minus);
            plusDrawable = getDrawable(context, R.drawable.zoom_plus);
        } else if (mode == SliderMode.SLIDER_MODE_EV) {
            minusDrawable = getDrawable(context, R.drawable.ev_minus);
            plusDrawable = getDrawable(context, R.drawable.ev_plus);
        }

        progressDrawable = getDrawable(context, R.drawable.zoom_slide);

        if (mode == SliderMode.SLIDER_MODE_ZOOM) {
            filledProgressDrawable = getDrawable(context, R.drawable.zoom_slide_a);
        } else if (mode == SliderMode.SLIDER_MODE_EV) {
            filledProgressDrawable = getDrawable(context, R.drawable.zoom_slide);
        }

        knobDrawable = getDrawable(context, R.drawable.zoom_round);
        pressedKnobDrawable = getDrawable(context, R.drawable.zoom_round_b);
    }

    private Drawable getDrawable(Context context, int drawable) {
        return ResourcesCompat.getDrawable(context.getResources(), drawable, null);
    }

    public float getSliderValue() {
        if (animatorSet != null) {
            return animatingToZoom;
        }
        return sliderValue;
    }

    public void setSliderValue(float value, boolean notify) {
        float MIN_SLIDER_VALUE = 0.0f;
        float MAX_SLIDER_VALUE = 1.0f;

        if (Float.compare(value, sliderValue) == 0) {
            return;
        }

        sliderValue = Math.min(MAX_SLIDER_VALUE, Math.max(MIN_SLIDER_VALUE, value));

        if (notify && delegate != null) {
            delegate.didSlide(sliderValue);
        }
        invalidate();
    }

    public void setDelegate(SliderControlViewDelegate sliderControlViewDelegate) {
        delegate = sliderControlViewDelegate;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!enabledTouch) {
            return false;
        }
        float x = event.getX();
        float y = event.getY();
        int action = event.getAction();
        boolean handled = false;
        boolean isPortrait = getMeasuredWidth() > getMeasuredHeight();
        int knobX = (int) (progressStartX + (progressEndX - progressStartX) * sliderValue);
        int knobY = (int) (progressStartY + (progressEndY - progressStartY) * sliderValue);
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            if (x >= knobX - dp(20) && x <= knobX + dp(20) && y >= knobY - dp(25) && y <= knobY + dp(25)) {
                if (action == MotionEvent.ACTION_DOWN) {
                    knobPressed = true;
                    knobStartX = x - knobX;
                    knobStartY = y - knobY;
                    invalidate();
                }
                handled = true;
            } else if (x >= minusCx - dp(16) && x <= minusCx + dp(16) && y >= minusCy - dp(16) && y <= minusCy + dp(16)) {
                if (action == MotionEvent.ACTION_UP && animateToValue((float) Math.floor(getSliderValue() / 0.25f) * 0.25f - 0.25f)) {
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                } else {
                    pressed = true;
                }
                handled = true;
            } else if (x >= plusCx - dp(16) && x <= plusCx + dp(16) && y >= plusCy - dp(16) && y <= plusCy + dp(16)) {
                if (action == MotionEvent.ACTION_UP && animateToValue((float) Math.floor(getSliderValue() / 0.25f) * 0.25f + 0.25f)) {
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                } else {
                    pressed = true;
                }
                handled = true;
            } else if (isPortrait) {
                if (x >= progressStartX && x <= progressEndX) {
                    if (action == MotionEvent.ACTION_DOWN) {
                        knobStartX = x;
                        pressed = true;
                    } else if (Math.abs(knobStartX - x) <= dp(10)) {
                        sliderValue = (x - progressStartX) / (progressEndX - progressStartX);
                        if (delegate != null) {
                            delegate.didSlide(sliderValue);
                        }
                        invalidate();
                    }
                    handled = true;
                }
            } else {
                if (y >= progressStartY && y <= progressEndY) {
                    if (action == MotionEvent.ACTION_UP) {
                        knobStartY = y;
                        pressed = true;
                    } else if (Math.abs(knobStartY - y) <= dp(10)) {
                        sliderValue = (y - progressStartY) / (progressEndY - progressStartY);
                        if (delegate != null) {
                            delegate.didSlide(sliderValue);
                        }
                        invalidate();
                    }
                    handled = true;
                }
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (knobPressed) {
                if (isPortrait) {
                    sliderValue = ((x + knobStartX) - progressStartX) / (progressEndX - progressStartX);
                } else {
                    sliderValue = ((y + knobStartY) - progressStartY) / (progressEndY - progressStartY);
                }
                if (sliderValue < 0) {
                    sliderValue = 0;
                } else if (sliderValue > 1.0f) {
                    sliderValue = 1.0f;
                }
                if (delegate != null) {
                    delegate.didSlide(sliderValue);
                }
                invalidate();
            }
        }
        if (action == MotionEvent.ACTION_UP) {
            pressed = false;
            knobPressed = false;
            invalidate();
        }
        return handled || pressed || knobPressed || super.onTouchEvent(event);
    }

    public boolean isTouch() {
        return pressed || knobPressed;
    }

    public boolean animateToValue(float zoom) {
        if (zoom < 0 || zoom > 1.0f) {
            return false;
        }
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        animatingToZoom = zoom;
        animatorSet = new AnimatorSet();
        animatorSet.playTogether(ObjectAnimator.ofFloat(this, SLIDER_PROPERTY, zoom));
        animatorSet.setDuration(180);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animatorSet = null;
            }
        });
        animatorSet.start();
        return true;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        int cx = getMeasuredWidth() / 2;
        int cy = getMeasuredHeight() / 2;
        boolean isPortrait = getMeasuredWidth() > getMeasuredHeight();

        if (isPortrait) {
            minusCx = dp(16 + 25);
            minusCy = cy;
            plusCx = getMeasuredWidth() - dp(16 + 25);
            plusCy = cy;

            progressStartX = minusCx + dp(18);
            progressStartY = cy;

            progressEndX = plusCx - dp(18);
            progressEndY = cy;
        } else {
            minusCx = cx;
            minusCy = dp(16 + 25);
            plusCx = cx;
            plusCy = getMeasuredHeight() - dp(16 + 25);

            progressStartX = cx;
            progressStartY = minusCy + dp(18);

            progressEndX = cx;
            progressEndY = plusCy - dp(18);
        }

        if (mode == SliderMode.SLIDER_MODE_ZOOM) {
            minusDrawable.setBounds(minusCx - dp(7), minusCy - dp(7), minusCx + dp(7), minusCy + dp(7));
            minusDrawable.draw(canvas);
            plusDrawable.setBounds(plusCx - dp(7), plusCy - dp(7), plusCx + dp(7), plusCy + dp(7));
            plusDrawable.draw(canvas);
        } else if (mode == SliderMode.SLIDER_MODE_EV) {
            minusDrawable.setBounds(minusCx - dp(7), minusCy - dp(7), minusCx + dp(7), minusCy + dp(7));
            minusDrawable.draw(canvas);
            plusDrawable.setBounds(plusCx - dp(8), plusCy - dp(8), plusCx + dp(8), plusCy + dp(8));
            plusDrawable.draw(canvas);
        }

        int totalX = progressEndX - progressStartX;
        int totalY = progressEndY - progressStartY;
        int knobX = (int) (progressStartX + totalX * sliderValue);
        int knobY = (int) (progressStartY + totalY * sliderValue);

        if (isPortrait) {
            progressDrawable.setBounds(progressStartX, progressStartY - dp(3), progressEndX, progressStartY + dp(3));
            filledProgressDrawable.setBounds(progressStartX, progressStartY - dp(3), knobX, progressStartY + dp(3));
        } else {
            progressDrawable.setBounds(progressStartY, 0, progressEndY, dp(6));
            filledProgressDrawable.setBounds(progressStartY, 0, knobY, dp(6));
            canvas.save();
            canvas.rotate(90);
            canvas.translate(0, -progressStartX - dp(3));
        }
        progressDrawable.draw(canvas);
        filledProgressDrawable.draw(canvas);
        if (!isPortrait) {
            canvas.restore();
        }

        Drawable drawable = knobPressed ? pressedKnobDrawable : knobDrawable;
        int size = drawable.getIntrinsicWidth();
        drawable.setBounds(knobX - size / 2, knobY - size / 2, knobX + size / 2, knobY + size / 2);
        drawable.draw(canvas);
    }

    public void invertColors(float invert) {
        var filter = new PorterDuffColorFilter(ColorUtils.blendARGB(Color.WHITE, Color.BLACK, invert), PorterDuff.Mode.MULTIPLY);
        minusDrawable.setColorFilter(filter);
        plusDrawable.setColorFilter(filter);
        progressDrawable.setColorFilter(filter);
        filledProgressDrawable.setColorFilter(filter);
        knobDrawable.setColorFilter(filter);
        pressedKnobDrawable.setColorFilter(filter);

        invalidate();
    }

    @IntDef({SliderMode.SLIDER_MODE_ZOOM, SliderMode.SLIDER_MODE_EV})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SliderMode {
        int SLIDER_MODE_ZOOM = 0;
        int SLIDER_MODE_EV = 1;
    }

    public interface SliderControlViewDelegate {
        void didSlide(float sliderValue);
    }
}
