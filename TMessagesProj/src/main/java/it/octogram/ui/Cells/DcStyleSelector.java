package it.octogram.ui.Cells;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberPicker;

import it.octogram.android.OctoConfig;

public class DcStyleSelector extends LinearLayout {
    Paint pickerDividersPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    String[] strings = new String[]{
            LocaleController.getString("Automatic", R.string.Automatic),
            "Telegram",
            "OctoGram",
    };
    public static int TELEGRAM_DC = 1;
    public static int OCTOGRAM_DC = 2;

    public DcStyleSelector(Context context) {
        super(context);
        pickerDividersPaint.setStyle(Paint.Style.STROKE);
        pickerDividersPaint.setStrokeCap(Paint.Cap.ROUND);
        pickerDividersPaint.setStrokeWidth(AndroidUtilities.dp(2));
        int colorIcon = Theme.getColor(Theme.key_switchTrack);
        int color = AndroidUtilities.getTransparentColor(colorIcon, 0.5f);
        ImageView imageView = new ImageView(context) {
            @Override
            @SuppressLint("DrawAllocation")
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                int w = ((getMeasuredWidth() - AndroidUtilities.dp(30)) >> 2) * 4;
                int maxWidth = ((getMeasuredHeight() - AndroidUtilities.dp(40)) * 4);
                w = Math.min(w, maxWidth);
                int h = w >> 2;
                int padding = Math.round((h * 15f) / 100f);
                h += padding;

                int left = (getMeasuredWidth() >> 1) - (w >> 1);
                int top = (getMeasuredHeight() >> 1) - (h >> 1);
                int right = w + left;
                int bottom = top + h;
                int radius = Math.round(((h - padding) * 14.77F) / 100F);

                RectF rectF = new RectF(left, top, right, bottom);
                Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
                p.setStyle(Paint.Style.STROKE);
                p.setColor(color);
                p.setStrokeWidth(AndroidUtilities.dp(1));
                canvas.drawRoundRect(rectF, radius, radius, p);

                int sizeContainer = w - padding;
                int xContainer = left + (w >> 1) - (sizeContainer >> 1);
                int hItem = sizeContainer >> 2;
                int yContainer = top + (h >> 1) - (hItem >> 1);

                int hSubItem = hItem - padding;
                int wItem = hItem * 4;
                int wSubItem = wItem - padding;
                int xMiddle = xContainer + (wItem >> 1) - (wSubItem >> 1);
                int yMiddle = yContainer + (hItem >> 1) - (hSubItem >> 1);

                drawStyleSelected(canvas, xMiddle, yMiddle, wSubItem, hSubItem, getMeasuredWidth(), getMeasuredHeight(), color);
            }
        };
        imageView.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f));

        addView(imageView);

        NumberPicker picker1 = new NumberPicker(context, 13) {
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                float y = AndroidUtilities.dp(31);
                pickerDividersPaint.setColor(Theme.getColor(Theme.key_radioBackgroundChecked));
                canvas.drawLine(AndroidUtilities.dp(2), y, getMeasuredWidth() - AndroidUtilities.dp(2), y, pickerDividersPaint);

                y = getMeasuredHeight() - AndroidUtilities.dp(31);
                canvas.drawLine(AndroidUtilities.dp(2), y, getMeasuredWidth() - AndroidUtilities.dp(2), y, pickerDividersPaint);
            }
        };

        picker1.setWrapSelectorWheel(true);
        picker1.setMinValue(0);
        picker1.setDrawDividers(false);
        picker1.setMaxValue(strings.length - 1);
        picker1.setFormatter(value -> strings[value]);
        picker1.setOnValueChangedListener((picker, oldVal, newVal) -> {
            OctoConfig.setDcStyleType(newVal);
            imageView.invalidate();
            invalidate();
            onSelectedStyle();
            picker.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        });
        picker1.setValue(OctoConfig.dcStyleType);
        addView(picker1, LayoutHelper.createLinear(132, LayoutHelper.MATCH_PARENT, Gravity.RIGHT, 21, 0, 21, 0));
    }

    private void drawStyleSelected(Canvas canvas, int x, int y, int w, int h, int maxW, int maxH, int color) {
        int heightTopText = (h * 30) / 100;
        int heightBottomText = (h * 20) / 100;
        int spaceBetween = (h * 18) / 100;
        int yMiddleTexts = y + (h >> 1) - ((heightTopText + spaceBetween + heightBottomText) >> 1);
        int strokeWidth = AndroidUtilities.dp(3);
        long rotation = angleOf(new PointF(x, y + h), new PointF(x + w, y));

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        Paint p2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        p2.setColor(color);
        p2.setStyle(Paint.Style.STROKE);
        p2.setStrokeWidth(strokeWidth);
        p2.setStrokeCap(Paint.Cap.ROUND);

        canvas.save();
        if (OctoConfig.dcStyleType == 0) {
            canvas.rotate(-rotation, maxW >> 1, maxH >> 1);
            Path clipPath = new Path();
            clipPath.addRect(0, 0, maxW, (strokeWidth * 2) + (maxH >> 1), Path.Direction.CW);
            canvas.clipPath(clipPath, Region.Op.DIFFERENCE);
            canvas.rotate(rotation, maxW >> 1, maxH >> 1);
        }

        if (OctoConfig.dcStyleType == 1 || OctoConfig.dcStyleType == 0) {
            int circleHeight = (h * 75) / 100;
            RectF rectF = new RectF(x, yMiddleTexts, x + Math.round((w * 70f) / 100f), yMiddleTexts + heightTopText);
            canvas.drawRoundRect(rectF, heightTopText >> 1, heightTopText >> 1, p);
            rectF.set(
                    x,
                    yMiddleTexts + heightTopText + spaceBetween,
                    x + Math.round((w * 50f) / 100f),
                    yMiddleTexts + heightTopText + spaceBetween + heightBottomText
            );
            canvas.drawRoundRect(rectF, heightBottomText >> 1, heightBottomText >> 1, p);
            canvas.drawCircle(x + w - (circleHeight >> 1), y + (h >> 1), (circleHeight >> 1), p);
        }
        canvas.restore();
        canvas.save();
        if (OctoConfig.dcStyleType == 0) {
            canvas.drawLine(x, y + h, x + w, y, p2);
            canvas.rotate(-rotation, maxW >> 1, maxH >> 1);
            Path clipPath = new Path();
            clipPath.addRect(0, (maxH >> 1) - (strokeWidth * 2), maxW, maxH, Path.Direction.CW);
            canvas.clipPath(clipPath, Region.Op.DIFFERENCE);
            canvas.rotate(rotation, maxW >> 1, maxH >> 1);
        }
        if (OctoConfig.dcStyleType == 2 || OctoConfig.dcStyleType == 0) {
            int iconHeight = (h * 75) / 100;
            int spaceBetweenIcons = (h * 18) / 100;
            int startX = x + iconHeight + spaceBetweenIcons;
            int startIconY = y + (h >> 1) - (iconHeight >> 1);
            int radius = (iconHeight * 20) / 100;
            RectF rectF = new RectF(x, startIconY, x + iconHeight, startIconY + iconHeight);
            canvas.drawRoundRect(rectF, radius, radius, p);
            rectF.set(
                    startX,
                    yMiddleTexts,
                    startX + Math.round((w * 70f) / 100f),
                    yMiddleTexts + heightTopText
            );
            canvas.drawRoundRect(rectF, heightTopText >> 1, heightTopText >> 1, p);
            rectF.set(
                    startX,
                    yMiddleTexts + heightTopText + spaceBetween,
                    startX + Math.round((w * 50f) / 100f),
                    yMiddleTexts + heightTopText + spaceBetween + heightBottomText
            );
            canvas.drawRoundRect(rectF, heightBottomText >> 1, heightBottomText >> 1, p);
        }
        canvas.restore();
    }

    public static int getStyleSelected() {
        return OctoConfig.dcStyleType == 0 ? (OctoConfig.buttonStyleType == 5 ? 1 : 2) : OctoConfig.dcStyleType;
    }

    private static long angleOf(PointF p1, PointF p2) {
        final double deltaY = (p1.y - p2.y);
        final double deltaX = (p2.x - p1.x);
        final double result = Math.toDegrees(Math.atan2(deltaY, deltaX));
        return Math.round((result < 0) ? (360d + result) : result);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(102), MeasureSpec.EXACTLY));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawLine(AndroidUtilities.dp(8), getMeasuredHeight() - 1, getMeasuredWidth() - AndroidUtilities.dp(8), getMeasuredHeight() - 1, Theme.dividerPaint);
    }

    protected void onSelectedStyle() {
    }
}
