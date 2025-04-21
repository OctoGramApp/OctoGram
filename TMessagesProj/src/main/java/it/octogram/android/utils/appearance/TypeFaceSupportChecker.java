/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.appearance;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.recycleBitmaps;
import static org.telegram.messenger.LocaleController.getInstance;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

import java.util.List;

public class TypeFaceSupportChecker {
    private static final String SUPPORT_CHECK_TEXT;
    private static final int CANVAS_SIZE = dp(12);
    private static final Paint PAINT = createPaint();

    private static Boolean isMediumWeightTypefaceSupported;
    private static Boolean isItalicTypefaceSupported;

    static {
        SUPPORT_CHECK_TEXT = getTestTextBasedOnLocale();
    }

    public static boolean isMediumWeightSupported() {
        return isMediumWeightTypefaceSupported != null ? isMediumWeightTypefaceSupported : (isMediumWeightTypefaceSupported = isTypefaceSupported(Typeface.create("sans-serif-medium", Typeface.NORMAL)));
    }

    public static boolean isItalicSupported() {
        return isItalicTypefaceSupported != null ? isItalicTypefaceSupported : (isItalicTypefaceSupported = isTypefaceSupported(Typeface.create("sans-serif", Typeface.ITALIC)));
    }

    private static boolean isTypefaceSupported(Typeface typeface) {
        var defaultTypefaceBitmap = createBitmap();
        var customTypefaceBitmap = createBitmap();
        var canvas = new Canvas();

        drawTextOnBitmap(canvas, defaultTypefaceBitmap, null);
        drawTextOnBitmap(canvas, customTypefaceBitmap, typeface);

        var isTypefaceRenderedDifferently = !defaultTypefaceBitmap.sameAs(customTypefaceBitmap);
        recycleBitmaps(List.of(defaultTypefaceBitmap, customTypefaceBitmap));
        return isTypefaceRenderedDifferently;
    }

    private static void drawTextOnBitmap(Canvas canvas, Bitmap bitmap, Typeface typeface) {
        canvas.setBitmap(bitmap);
        PAINT.setTypeface(typeface);
        canvas.drawText(SUPPORT_CHECK_TEXT, 0, CANVAS_SIZE, PAINT);
    }

    private static String getTestTextBasedOnLocale() {
        return "jp".equals(getInstance().getCurrentLocale().getLanguage()) ? "日" : "R";
    }

    private static Bitmap createBitmap() {
        return Bitmap.createBitmap(CANVAS_SIZE, CANVAS_SIZE, Bitmap.Config.ALPHA_8);
    }

    private static Paint createPaint() {
        Paint paint = new Paint();
        paint.setTextSize(CANVAS_SIZE);
        paint.setAntiAlias(false);
        paint.setSubpixelText(false);
        paint.setFakeBoldText(false);
        return paint;
    }
}
