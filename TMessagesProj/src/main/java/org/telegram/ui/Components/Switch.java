/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui.Components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.util.StateSet;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.Keep;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.BaseCell;

import it.octogram.android.InterfaceSwitchUI;
import it.octogram.android.OctoConfig;

public class Switch extends View {

    private RectF rectF;

    private float progress;
    private ObjectAnimator checkAnimator;
    private ObjectAnimator iconAnimator;

    private boolean attachedToWindow;
    private boolean isChecked;
    private Paint paint;
    private Paint paint2;

    private int drawIconType;
    private float iconProgress = 1.0f;

    private OnCheckedChangeListener onCheckedChangeListener;

    private int trackColorKey = Theme.key_fill_RedNormal;
    private int trackCheckedColorKey = Theme.key_switch2TrackChecked;
    private int thumbColorKey = Theme.key_windowBackgroundWhite;
    private int thumbCheckedColorKey = Theme.key_windowBackgroundWhite;

    private Drawable iconDrawable;
    private int lastIconColor;

    private boolean drawRipple;
    private RippleDrawable rippleDrawable;
    private Paint ripplePaint;
    private int[] pressedState = new int[]{android.R.attr.state_enabled, android.R.attr.state_pressed};
    private int colorSet;

    private boolean bitmapsCreated;
    private Bitmap[] overlayBitmap;
    private Canvas[] overlayCanvas;
    private Bitmap overlayMaskBitmap;
    private Canvas overlayMaskCanvas;
    private float overlayCx;
    private float overlayCy;
    private float overlayRad;
    private Paint overlayEraserPaint;
    private Paint overlayMaskPaint;

    private Theme.ResourcesProvider resourcesProvider;

    private int overrideColorProgress;

    private Paint googleBorderPaint;
    private int forcedUIState = -1;
    private Drawable checkDrawable;

    public interface OnCheckedChangeListener {
        void onCheckedChanged(Switch view, boolean isChecked);
    }

    public Switch(Context context) {
        this(context, null);
    }

    public Switch(Context context, int forcedUIState) {
        this(context, null);
        this.forcedUIState = forcedUIState;
    }

    public Switch(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        this.resourcesProvider = resourcesProvider;
        rectF = new RectF();

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint2.setStyle(Paint.Style.STROKE);
        paint2.setStrokeCap(Paint.Cap.ROUND);
        paint2.setStrokeWidth(AndroidUtilities.dp(2));

        googleBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        googleBorderPaint.setStyle(Paint.Style.STROKE);
        googleBorderPaint.setStrokeCap(Paint.Cap.ROUND);
        googleBorderPaint.setStrokeWidth(AndroidUtilities.dp(1));

        checkDrawable = getResources().getDrawable(R.drawable.floating_check).mutate();
        if (checkDrawable != null) {
            checkDrawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(trackCheckedColorKey, resourcesProvider), PorterDuff.Mode.MULTIPLY));
        }

        setHapticFeedbackEnabled(true);
    }

    @Keep
    public void setProgress(float value) {
        if (progress == value) {
            return;
        }
        progress = value;
        invalidate();
    }

    @Keep
    public float getProgress() {
        return progress;
    }

    @Keep
    public void setIconProgress(float value) {
        if (iconProgress == value) {
            return;
        }
        iconProgress = value;
        invalidate();
    }

    @Keep
    public float getIconProgress() {
        return iconProgress;
    }

    private void cancelCheckAnimator() {
        if (checkAnimator != null) {
            checkAnimator.cancel();
            checkAnimator = null;
        }
    }

    private void cancelIconAnimator() {
        if (iconAnimator != null) {
            iconAnimator.cancel();
            iconAnimator = null;
        }
    }

    public void setDrawIconType(int type) {
        drawIconType = type;
    }

    public void setDrawRipple(boolean value) {
        if (Build.VERSION.SDK_INT < 21 || value == drawRipple) {
            return;
        }
        drawRipple = value;

        if (rippleDrawable == null) {
            ripplePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            ripplePaint.setColor(0xffffffff);
            Drawable maskDrawable;
            if (Build.VERSION.SDK_INT >= 23) {
                maskDrawable = null;
            } else {
                maskDrawable = new Drawable() {
                    @Override
                    public void draw(Canvas canvas) {
                        android.graphics.Rect bounds = getBounds();
                        canvas.drawCircle(bounds.centerX(), bounds.centerY(), AndroidUtilities.dp(18), ripplePaint);
                    }

                    @Override
                    public void setAlpha(int alpha) {

                    }

                    @Override
                    public void setColorFilter(ColorFilter colorFilter) {

                    }

                    @Override
                    public int getOpacity() {
                        return PixelFormat.UNKNOWN;
                    }
                };
            }
            ColorStateList colorStateList = new ColorStateList(
                new int[][]{StateSet.WILD_CARD},
                new int[]{0}
            );
            rippleDrawable = new BaseCell.RippleDrawableSafe(colorStateList, null, maskDrawable);
            if (Build.VERSION.SDK_INT >= 23) {
                rippleDrawable.setRadius(AndroidUtilities.dp(18));
            }
            rippleDrawable.setCallback(this);
        }
        if (isChecked && colorSet != 2 || !isChecked && colorSet != 1) {
            int color = Theme.getColor(isChecked ? Theme.key_switchTrackBlueSelectorChecked : Theme.key_switchTrackBlueSelector, resourcesProvider);
            color = processColor(color);
            ColorStateList colorStateList = new ColorStateList(
                new int[][]{StateSet.WILD_CARD},
                new int[]{color}
            );
            rippleDrawable.setColor(colorStateList);
            colorSet = isChecked ? 2 : 1;
        }
        if (Build.VERSION.SDK_INT >= 28 && value) {
            rippleDrawable.setHotspot(isChecked ? 0 : AndroidUtilities.dp(100), AndroidUtilities.dp(18));
        }
        rippleDrawable.setState(value ? pressedState : StateSet.NOTHING);
        invalidate();
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || rippleDrawable != null && who == rippleDrawable;
    }

    protected int processColor(int color) {
        return color;
    }

    public void setColors(int track, int trackChecked, int thumb, int thumbChecked) {
        trackColorKey = track;
        trackCheckedColorKey = trackChecked;
        thumbColorKey = thumb;
        thumbCheckedColorKey = thumbChecked;
    }

    private void animateToCheckedState(boolean newCheckedState) {
        checkAnimator = ObjectAnimator.ofFloat(this, "progress", newCheckedState ? 1 : 0);
        checkAnimator.setDuration(200);
        checkAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                checkAnimator = null;
            }
        });
        checkAnimator.start();
    }

    private void animateIcon(boolean newCheckedState) {
        iconAnimator = ObjectAnimator.ofFloat(this, "iconProgress", newCheckedState ? 1 : 0);
        iconAnimator.setDuration(200);
        iconAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                iconAnimator = null;
            }
        });
        iconAnimator.start();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attachedToWindow = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        attachedToWindow = false;
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        onCheckedChangeListener = listener;
    }

    public void setChecked(boolean checked, boolean animated) {
        setChecked(checked, drawIconType, animated);
    }

    public void setChecked(boolean checked, int iconType, boolean animated) {
        if (checked != isChecked) {
            isChecked = checked;
            if (attachedToWindow && animated) {
                animateToCheckedState(checked);
                if (OctoConfig.INSTANCE.moreHapticFeedbacks.getValue()) {
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
                }
            } else {
                cancelCheckAnimator();
                setProgress(checked ? 1.0f : 0.0f);
            }
            if (onCheckedChangeListener != null) {
                onCheckedChangeListener.onCheckedChanged(this, checked);
            }
        }
        setDrawIconType(iconType, animated);
    }

    public void setIcon(int icon) {
        if (icon != 0) {
            iconDrawable = getResources().getDrawable(icon).mutate();
            if (iconDrawable != null) {
                iconDrawable.setColorFilter(new PorterDuffColorFilter(lastIconColor = Theme.getColor(isChecked ? trackCheckedColorKey : trackColorKey, resourcesProvider), PorterDuff.Mode.MULTIPLY));
            }
        } else {
            iconDrawable = null;
        }
        invalidate();
    }

    public void setDrawIconType(int iconType, boolean animated) {
        if (drawIconType != iconType) {
            drawIconType = iconType;
            if (attachedToWindow && animated) {
                animateIcon(iconType == 0);
            } else {
                cancelIconAnimator();
                setIconProgress(iconType == 0 ? 1.0f : 0.0f);
            }
        }
    }

    public boolean hasIcon() {
        return iconDrawable != null;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setOverrideColor(int override) {
        if (overrideColorProgress == override) {
            return;
        }
        if (overlayBitmap == null) {
            try {
                overlayBitmap = new Bitmap[2];
                overlayCanvas = new Canvas[2];
                for (int a = 0; a < 2; a++) {
                    overlayBitmap[a] = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                    overlayCanvas[a] = new Canvas(overlayBitmap[a]);
                }
                overlayMaskBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                overlayMaskCanvas = new Canvas(overlayMaskBitmap);

                overlayEraserPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                overlayEraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

                overlayMaskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                overlayMaskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
                bitmapsCreated = true;
            } catch (Throwable e) {
                return;
            }
        }
        if (!bitmapsCreated) {
            return;
        }
        overrideColorProgress = override;
        overlayCx = 0;
        overlayCy = 0;
        overlayRad = 0;
        invalidate();
    }

    public void setOverrideColorProgress(float cx, float cy, float rad) {
        overlayCx = cx;
        overlayCy = cy;
        overlayRad = rad;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getVisibility() != VISIBLE) {
            return;
        }

        int width = AndroidUtilities.dp(31);
        int thumb = AndroidUtilities.dp(20);

        boolean useHardFixTx  = false;
        if (isUsingSeparateView()) {
            width = AndroidUtilities.dp(36);
            useHardFixTx = true;
        }

        int x = (getMeasuredWidth() - width) / 2;
        float y = (getMeasuredHeight() - AndroidUtilities.dpf2(14)) / 2;
        int tx = x + AndroidUtilities.dp(7) + (int) (AndroidUtilities.dp(useHardFixTx ? 18 : 17) * progress);
        int ty = getMeasuredHeight() / 2;


        int color1;
        int color2;
        float colorProgress;
        int r1;
        int r2;
        int g1;
        int g2;
        int b1;
        int b2;
        int a1;
        int a2;
        int red;
        int green;
        int blue;
        int alpha;
        int color;

        for (int a = 0; a < 2; a++) {
            if (a == 1 && overrideColorProgress == 0) {
                continue;
            }
            Canvas canvasToDraw = a == 0 ? canvas : overlayCanvas[0];

            if (a == 1) {
                overlayBitmap[0].eraseColor(0);
                paint.setColor(0xff000000);
                overlayMaskCanvas.drawRect(0, 0, overlayMaskBitmap.getWidth(), overlayMaskBitmap.getHeight(), paint);
                overlayMaskCanvas.drawCircle(overlayCx - getX(), overlayCy - getY(), overlayRad, overlayEraserPaint);
            }
            if (overrideColorProgress == 1) {
                colorProgress = a == 0 ? 0 : 1;
            } else if (overrideColorProgress == 2) {
                colorProgress = a == 0 ? 1 : 0;
            } else {
                colorProgress = progress;
            }

            int originalColor1;
            color1 = originalColor1 = processColor(Theme.getColor(trackColorKey, resourcesProvider));
            color2 = processColor(Theme.getColor(trackCheckedColorKey, resourcesProvider));

            if (hasTransparentBg()) {
                color1 = Color.TRANSPARENT;
            }

            if (a == 0 && iconDrawable != null && lastIconColor != (isChecked ? color2 : color1)) {
                iconDrawable.setColorFilter(new PorterDuffColorFilter(lastIconColor = (isChecked ? color2 : color1), PorterDuff.Mode.MULTIPLY));
            }

            r1 = Color.red(color1);
            r2 = Color.red(color2);
            g1 = Color.green(color1);
            g2 = Color.green(color2);
            b1 = Color.blue(color1);
            b2 = Color.blue(color2);
            a1 = Color.alpha(color1);
            a2 = Color.alpha(color2);

            red = (int) (r1 + (r2 - r1) * colorProgress);
            green = (int) (g1 + (g2 - g1) * colorProgress);
            blue = (int) (b1 + (b2 - b1) * colorProgress);
            alpha = (int) (a1 + (a2 - a1) * colorProgress);
            color = ((alpha & 0xff) << 24) | ((red & 0xff) << 16) | ((green & 0xff) << 8) | (blue & 0xff);
            paint.setColor(color);
            paint2.setColor(color);

            rectF.set(x, y, x + width, y + AndroidUtilities.dpf2(14));

            int r = 7;
            if (getUIState() == InterfaceSwitchUI.ONEUIOLD.getValue()) {
                rectF.set(x, y - AndroidUtilities.dpf2(1), x + width, y + AndroidUtilities.dpf2(15));
            } else if (isUsingSeparateView()) {
                rectF.set(x, y - AndroidUtilities.dpf2(3), x + width, y + AndroidUtilities.dpf2(17));
                r = 15;
            }

            canvasToDraw.drawRoundRect(rectF, AndroidUtilities.dpf2(r), AndroidUtilities.dpf2(r), paint);

            if (hasTransparentBg()) {
                color1 = originalColor1;
                r1 = Color.red(color1);
                r2 = Color.red(color2);
                g1 = Color.green(color1);
                g2 = Color.green(color2);
                b1 = Color.blue(color1);
                b2 = Color.blue(color2);
                a1 = Color.alpha(color1);
                a2 = Color.alpha(color2);

                red = (int) (r1 + (r2 - r1) * colorProgress);
                green = (int) (g1 + (g2 - g1) * colorProgress);
                blue = (int) (b1 + (b2 - b1) * colorProgress);
                alpha = (int) (a1 + (a2 - a1) * colorProgress);
                googleBorderPaint.setColor(((alpha & 0xff) << 24) | ((red & 0xff) << 16) | ((green & 0xff) << 8) | (blue & 0xff));

                canvasToDraw.drawRoundRect(rectF, AndroidUtilities.dpf2(r), AndroidUtilities.dpf2(r), googleBorderPaint);
            }

            if (getUIState() == InterfaceSwitchUI.DEFAULT.getValue()) {
                canvasToDraw.drawCircle(tx, ty, AndroidUtilities.dpf2(10), paint);
            }

            if (a == 0 && rippleDrawable != null) {
                rippleDrawable.setBounds(tx - AndroidUtilities.dp(18), ty - AndroidUtilities.dp(18), tx + AndroidUtilities.dp(18), ty + AndroidUtilities.dp(18));
                rippleDrawable.draw(canvasToDraw);
            } else if (a == 1) {
                canvasToDraw.drawBitmap(overlayMaskBitmap, 0, 0, overlayMaskPaint);
            }
        }
        if (overrideColorProgress != 0) {
            canvas.drawBitmap(overlayBitmap[0], 0, 0, null);
        }

        for (int a = 0; a < 2; a++) {
            if (a == 1 && overrideColorProgress == 0) {
                continue;
            }
            Canvas canvasToDraw = a == 0 ? canvas : overlayCanvas[1];

            if (a == 1) {
                overlayBitmap[1].eraseColor(0);
            }
            if (overrideColorProgress == 1) {
                colorProgress = a == 0 ? 0 : 1;
            } else if (overrideColorProgress == 2) {
                colorProgress = a == 0 ? 1 : 0;
            } else {
                colorProgress = progress;
            }

            color1 = Theme.getColor(hasTransparentBg() ? trackColorKey : thumbColorKey, resourcesProvider);
            color2 = processColor(Theme.getColor(thumbCheckedColorKey, resourcesProvider));
            r1 = Color.red(color1);
            r2 = Color.red(color2);
            g1 = Color.green(color1);
            g2 = Color.green(color2);
            b1 = Color.blue(color1);
            b2 = Color.blue(color2);
            a1 = Color.alpha(color1);
            a2 = Color.alpha(color2);

            red = (int) (r1 + (r2 - r1) * colorProgress);
            green = (int) (g1 + (g2 - g1) * colorProgress);
            blue = (int) (b1 + (b2 - b1) * colorProgress);
            alpha = (int) (a1 + (a2 - a1) * colorProgress);
            paint.setColor(((alpha & 0xff) << 24) | ((red & 0xff) << 16) | ((green & 0xff) << 8) | (blue & 0xff));

            float circleSize = getCircleSize();

            if (isUsingOneUI()) {
                paint.setColor(Color.WHITE);
                paint2.setColor(Color.WHITE);
            }

            if (isUsingSeparateView()) {
                canvasToDraw.drawCircle(Utilities.clamp(tx, x + width + AndroidUtilities.dp(2), x + AndroidUtilities.dp(10)), ty, AndroidUtilities.dp(circleSize), paint);
            } else {
                canvasToDraw.drawCircle(tx, ty, AndroidUtilities.dp(circleSize), paint);
            }

            if (a == 0 && (getUIState() == InterfaceSwitchUI.DEFAULT.getValue() || getUIState() == InterfaceSwitchUI.GOOGLE_NEW.getValue())) {
                if (getUIState() == InterfaceSwitchUI.GOOGLE_NEW.getValue()) {
                    int iconWidth = checkDrawable.getIntrinsicWidth() / 2;
                    int iconHeight = checkDrawable.getIntrinsicHeight() / 2;
                    checkDrawable.setBounds(tx - iconWidth / 2, ty - iconHeight / 2, tx + iconWidth / 2, ty + iconHeight / 2);
                    checkDrawable.setAlpha((int) (255 * progress));
                    checkDrawable.draw(canvasToDraw);
                } else if (iconDrawable != null) {
                    int iconWidth = iconDrawable.getIntrinsicWidth();
                    int iconHeight = iconDrawable.getIntrinsicHeight();
                    iconDrawable.setBounds(tx - iconWidth / 2, ty - iconHeight / 2, tx + iconWidth / 2, ty + iconHeight / 2);
                    iconDrawable.draw(canvasToDraw);
                } else if (drawIconType == 1) {
                    tx -= AndroidUtilities.dp(10.8f) - AndroidUtilities.dp(1.3f) * progress;
                    ty -= AndroidUtilities.dp(8.5f) - AndroidUtilities.dp(0.5f) * progress;
                    int startX2 = (int) AndroidUtilities.dpf2(4.6f) + tx;
                    int startY2 = (int) (AndroidUtilities.dpf2(9.5f) + ty);
                    int endX2 = startX2 + AndroidUtilities.dp(2);
                    int endY2 = startY2 + AndroidUtilities.dp(2);

                    int startX = (int) AndroidUtilities.dpf2(7.5f) + tx;
                    int startY = (int) AndroidUtilities.dpf2(5.4f) + ty;
                    int endX = startX + AndroidUtilities.dp(7);
                    int endY = startY + AndroidUtilities.dp(7);

                    canvasToDraw.save();

                    startX = (int) (startX + (startX2 - startX) * progress);
                    startY = (int) (startY + (startY2 - startY) * progress);
                    endX = (int) (endX + (endX2 - endX) * progress);
                    endY = (int) (endY + (endY2 - endY) * progress);

                    canvasToDraw.drawLine(startX, startY, endX, endY, paint2);

                    startX = (int) AndroidUtilities.dpf2(7.5f) + tx;
                    startY = (int) AndroidUtilities.dpf2(12.5f) + ty;
                    endX = startX + AndroidUtilities.dp(7);
                    endY = startY - AndroidUtilities.dp(7);

                    canvasToDraw.drawLine(startX, startY, endX, endY, paint2);

                    canvasToDraw.restore();
                } else if (drawIconType == 2 || iconAnimator != null) {
                    paint2.setAlpha((int) (255 * (1.0f - iconProgress)));
                    canvasToDraw.drawLine(tx, ty, tx, ty - AndroidUtilities.dp(5), paint2);
                    canvasToDraw.save();
                    canvasToDraw.rotate(-90 * iconProgress, tx, ty);
                    canvasToDraw.drawLine(tx, ty, tx + AndroidUtilities.dp(4), ty, paint2);
                    canvasToDraw.restore();
                }
            }
            if (a == 1) {
                canvasToDraw.drawBitmap(overlayMaskBitmap, 0, 0, overlayMaskPaint);
            }
        }
        if (overrideColorProgress != 0) {
            canvas.drawBitmap(overlayBitmap[1], 0, 0, null);
        }
    }

    private float getCircleSize() {
        if (getUIState() == InterfaceSwitchUI.ONEUINEW.getValue()) {
            return 9;
        } else if (getUIState() == InterfaceSwitchUI.GOOGLE.getValue() || getUIState() == InterfaceSwitchUI.GOOGLE_NEW.getValue()) {
            return 6 + 2 * progress;
        } else if (getUIState() == InterfaceSwitchUI.ONEUIOLD.getValue()) {
            return 10;
        } else {
            return 8;
        }
    }

    private boolean isUsingSeparateView() {
        return getUIState() == InterfaceSwitchUI.ONEUINEW.getValue() || getUIState() == InterfaceSwitchUI.GOOGLE.getValue() || getUIState() == InterfaceSwitchUI.GOOGLE_NEW.getValue();
    }

    private boolean isUsingOneUI() {
        return getUIState() == InterfaceSwitchUI.ONEUINEW.getValue() || getUIState() == InterfaceSwitchUI.ONEUIOLD.getValue();
    }

    private boolean hasTransparentBg() {
        return getUIState() == InterfaceSwitchUI.GOOGLE.getValue() || getUIState() == InterfaceSwitchUI.GOOGLE_NEW.getValue();
    }

    private int getUIState() {
        if (forcedUIState != -1) {
            return forcedUIState;
        }

        return OctoConfig.INSTANCE.interfaceSwitchUI.getValue();
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName("android.widget.Switch");
        info.setCheckable(true);
        info.setChecked(isChecked);
        //info.setContentDescription(isChecked ? LocaleController.getString(R.string.NotificationsOn) : LocaleController.getString(R.string.NotificationsOff));
    }
}
