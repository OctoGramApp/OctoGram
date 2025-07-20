/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PopupBackgroundDrawable extends Drawable {

    private final Paint fillPaint;
    private final Paint strokePaint;
    private final float cornerRadius;
    private final float strokeWidth;
    private final float shadowRadius;
    private final float shadowDx;
    private final float shadowDy;

    private final RectF shapeRect = new RectF();
    private final Rect calculatedPadding = new Rect();

    public PopupBackgroundDrawable(int fillColor, int strokeColor, float cornerRadiusInPixels) {
        this.cornerRadius = cornerRadiusInPixels;
        this.strokeWidth = 1.0f;

        this.shadowRadius = 12f;
        this.shadowDx = 0f;
        this.shadowDy = 4f;
        int shadowColor = Color.argb(60, 0, 0, 0);

        this.fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.fillPaint.setStyle(Paint.Style.FILL);
        this.fillPaint.setColor(fillColor);
        this.fillPaint.setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor);

        this.strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.strokePaint.setStyle(Paint.Style.STROKE);
        this.strokePaint.setColor(strokeColor);
        this.strokePaint.setAlpha(80);
        this.strokePaint.setStrokeWidth(strokeWidth);

        calculatePadding(this.calculatedPadding);
    }

    private void calculatePadding(@NonNull Rect outRect) {
        float halfStroke = strokeWidth / 2f;
        int left = (int) Math.ceil(shadowRadius - shadowDx + halfStroke);
        int top = (int) Math.ceil(shadowRadius - shadowDy + halfStroke);
        int right = (int) Math.ceil(shadowRadius + shadowDx + halfStroke);
        int bottom = (int) Math.ceil(shadowRadius + shadowDy + halfStroke);
        outRect.set(left, top, right, bottom);
    }

    @Override
    public boolean getPadding(@NonNull Rect padding) {
        padding.set(this.calculatedPadding);
        return true;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();

        shapeRect.set(
                bounds.left + calculatedPadding.left,
                bounds.top + calculatedPadding.top,
                bounds.right - calculatedPadding.right,
                bounds.bottom - calculatedPadding.bottom
        );

        canvas.drawRoundRect(shapeRect, cornerRadius, cornerRadius, fillPaint);
        canvas.drawRoundRect(shapeRect, cornerRadius, cornerRadius, strokePaint);
    }

    @Override
    public void setAlpha(int alpha) {
        fillPaint.setAlpha(alpha);
        strokePaint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        fillPaint.setColorFilter(colorFilter);
        strokePaint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public void setFillColor(int newFillColor) {
        if (fillPaint.getColor() != newFillColor) {
            fillPaint.setColor(newFillColor);
            invalidateSelf();
        }
    }

    public void setStrokeColor(int newStrokeColor) {
        if (strokePaint.getColor() != newStrokeColor) {
            strokePaint.setColor(newStrokeColor);
            invalidateSelf();
        }
    }
}
