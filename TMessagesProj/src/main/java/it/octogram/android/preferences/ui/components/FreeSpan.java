/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui.components;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.ReplacementSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.telegram.messenger.AndroidUtilities;

public class FreeSpan extends ReplacementSpan {

    TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    StaticLayout layout;
    float width, height;

    private final boolean outline;
    private int color;

    public void setTypeface(Typeface typeface) {
        textPaint.setTypeface(typeface);
    }

    public FreeSpan(float textSize) {
        this.outline = false;
        textPaint.setTypeface(AndroidUtilities.bold());
        bgPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(dp(textSize));
    }

    public void setColor(int color) {
        this.color = color;
    }

    private CharSequence text = "FREE";

    public void setText(CharSequence text) {
        this.text = text;
        if (layout != null) {
            layout = null;
            makeLayout();
        }
    }

    public void makeLayout() {
        if (layout == null) {
            layout = new StaticLayout(text, textPaint, AndroidUtilities.displaySize.x, Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
            width = layout.getLineWidth(0);
            height = layout.getHeight();
        }
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
        makeLayout();
        return (int) (dp(10) + width);
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float _x, int top, int _y, int bottom, @NonNull Paint paint) {
        makeLayout();

        int color = this.color;
        if (color == 0) {
            color = paint.getColor();
        }
        bgPaint.setColor(color);
        if (outline) {
            textPaint.setColor(color);
        } else {
            textPaint.setColor(AndroidUtilities.computePerceivedBrightness(color) > .721f ? Color.BLACK : Color.WHITE);
        }

        float x = _x + dp(2), y = _y - height + dp(1);
        AndroidUtilities.rectTmp.set(x, y, x + width, y + height);
        float r;
        r = dp(3.66f);
        AndroidUtilities.rectTmp.left -= dp(4);
        AndroidUtilities.rectTmp.top -= dp(2.33f);
        AndroidUtilities.rectTmp.right += dp(3.66f);
        AndroidUtilities.rectTmp.bottom += dp(1.33f);
        canvas.drawRoundRect(AndroidUtilities.rectTmp, r, r, bgPaint);

        canvas.save();
        canvas.translate(x, y);
        layout.draw(canvas);
        canvas.restore();
    }
}
