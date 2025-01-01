/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

/*
 * Thanks to OwlGram.
 */

package it.octogram.android.preferences.ui.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;

import org.telegram.ui.Components.BlobDrawable;
import org.telegram.ui.Components.WaveDrawable;

@SuppressLint("ViewConstructor")
public class RadialProgressView extends FrameLayout {
    private final OvershootInterpolator overshootInterpolator;
    private final BlobDrawable bigWaveDrawable;
    private final BlobDrawable tinyWaveDrawable;
    private boolean isInitiated = false;
    private int colorKey1, colorKey2;
    private boolean drawRadialBackShadow = true;

    public RadialProgressView(Context context, int colorKey1) {
        super(context);
        setColor(colorKey1);
        overshootInterpolator = new OvershootInterpolator(1.5f);
        tinyWaveDrawable = new BlobDrawable(9);
        bigWaveDrawable = new BlobDrawable(12);

        tinyWaveDrawable.paint.setColor(ColorUtils.setAlphaComponent(colorKey1, (int) (255 * WaveDrawable.CIRCLE_ALPHA_2)));
        bigWaveDrawable.paint.setColor(ColorUtils.setAlphaComponent(colorKey1, (int) (255 * WaveDrawable.CIRCLE_ALPHA_1)));
    }

    public void setColor(int colorKey1) {
        this.colorKey1 = colorKey1;
        this.colorKey2 = colorKey1;
    }

    public void setDrawRadialBackShadow(boolean drawRadialBackShadow) {
        this.drawRadialBackShadow = drawRadialBackShadow;
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        float showWavesProgressInterpolated = overshootInterpolator.getInterpolation(1f);
        showWavesProgressInterpolated = 0.4f + 0.6f * showWavesProgressInterpolated;
        float amplitude = 0F;

        int s1 = Math.round(((getMeasuredWidth() >> 1) * 90) / 100f);
        int s2 = Math.round((s1 * 86.11f) / 100f);
        int sl1 = Math.round((s1 * 96) / 100f);
        int sl2 = Math.round((sl1 * 86.11f) / 100f);
        int sC = Math.round((sl2 * 88) / 100f);
        if (!isInitiated) {
            isInitiated = true;
            tinyWaveDrawable.minRadius = sl2;
            tinyWaveDrawable.maxRadius = sl1;
            tinyWaveDrawable.generateBlob();

            bigWaveDrawable.minRadius = s2;
            bigWaveDrawable.maxRadius = s1;
            bigWaveDrawable.generateBlob();
        }

        bigWaveDrawable.update(amplitude, 1f);
        tinyWaveDrawable.update(amplitude, 1f);

        for (int i = 0; i < 2; i++) {
            int cx = getMeasuredWidth() >> 1;
            int cy = getMeasuredHeight() >> 1;
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG);
            int w = Math.round((getMeasuredWidth() * 75f) / 100f);
            paint.setShader(new RadialGradient(w, w, w, new int[]{colorKey1, colorKey2}, null, Shader.TileMode.CLAMP));
            paint.setAlpha(76);
            if (i == 1 && drawRadialBackShadow) {
                canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), getRadialPaint());
                canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), getRadialPaint());
            }

            canvas.save();
            float scale = BlobDrawable.SCALE_BIG_MIN + BlobDrawable.SCALE_BIG * amplitude * showWavesProgressInterpolated;
            canvas.scale(scale, scale, cx, cy);
            bigWaveDrawable.draw(cx, cy, canvas, paint);
            canvas.restore();

            canvas.save();
            scale = BlobDrawable.SCALE_SMALL_MIN + BlobDrawable.SCALE_SMALL * amplitude * showWavesProgressInterpolated;
            canvas.scale(scale, scale, cx, cy);
            tinyWaveDrawable.draw(cx, cy, canvas, paint);
            canvas.restore();

            paint.setAlpha(255);
            canvas.drawCircle(cx, cy, sC, paint);
        }

        super.dispatchDraw(canvas);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private Paint getRadialPaint() {
        int w = getMeasuredWidth() >> 1;
        int r = Color.red(colorKey1);
        int g = Color.green(colorKey1);
        int b = Color.blue(colorKey1);
        RadialGradient radialGradient = new RadialGradient(w, w, w, new int[]{Color.argb(50, r, g, b), Color.argb(0, r, g, b)}, null, Shader.TileMode.CLAMP);
        Paint radialPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        radialPaint.setShader(radialGradient);
        return radialPaint;
    }
}
