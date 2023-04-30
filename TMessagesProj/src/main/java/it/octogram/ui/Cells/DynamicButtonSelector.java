package it.octogram.ui.Cells;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
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
import it.octogram.ui.Cells.Dynamic.ButtonCell;

public class DynamicButtonSelector extends LinearLayout {
    Paint pickerDividersPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    String[] strings = new String[]{
            LocaleController.getString("ShapeSquared", R.string.ShapeSquared),
            LocaleController.getString("ShapeRounded", R.string.ShapeRounded),
            "Iceled",
            LocaleController.getString("ShapePills", R.string.ShapePills),
            LocaleController.getString("ShapeLinear", R.string.ShapeLinear),
            LocaleController.getString("ShapeStock", R.string.ShapeStock),
    };
    private final ImageView imageview;

    public DynamicButtonSelector(Context context) {
        super(context);
        pickerDividersPaint.setStyle(Paint.Style.STROKE);
        pickerDividersPaint.setStrokeCap(Paint.Cap.ROUND);
        pickerDividersPaint.setStrokeWidth(AndroidUtilities.dp(2));
        int colorIcon = Theme.getColor(Theme.key_switchTrack);
        int color = AndroidUtilities.getTransparentColor(colorIcon, 0.5f);
        imageview = new ImageView(context) {
            @SuppressLint("DrawAllocation")
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                int selectedStyle = OctoConfig.buttonStyleType;
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
                if (selectedStyle != 5) {
                    for (int i = 0; i < 4; i++) {
                        int x = xContainer + (hItem * i);
                        int wSubItem = hItem - padding;
                        int xMiddle = x + (hItem >> 1) - (wSubItem >> 1);
                        int yMiddle = yContainer + (hItem >> 1) - (wSubItem >> 1);
                        ButtonCell.drawButtonPreview(canvas, xMiddle, yMiddle, wSubItem, i, selectedStyle, context);
                    }
                } else {
                    int hSubItem = hItem - padding;
                    int wItem = hItem * 4;
                    int wSubItem = wItem - padding;
                    int xMiddle = xContainer + (wItem >> 1) - (wSubItem >> 1);
                    int yMiddle = yContainer + (hItem >> 1) - (hSubItem >> 1);
                    drawStockPreview(canvas, xMiddle, yMiddle, wSubItem, hSubItem);
                }
            }
        };
        imageview.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f));
        addView(imageview);
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
            imageview.invalidate();
            OctoConfig.saveButtonStyle(newVal);
            invalidate();
            picker.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            onSelectionChange();
        });
        int selectedButton = OctoConfig.buttonStyleType;
        picker1.setValue(selectedButton);
        addView(picker1, LayoutHelper.createLinear(132, LayoutHelper.MATCH_PARENT, Gravity.RIGHT, 21, 0, 21, 0));
    }

    private void drawStockPreview(Canvas canvas, int x, int y, int w, int h) {
        int color = Theme.getColor(Theme.key_switchTrack);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        int rad1 = Math.round((h * 14.77F) / 100F);
        int dCircle = Math.round((h * 80f) / 100f);
        int xCircle = (x + w) - dCircle - (rad1 * 2);
        int yCircle = y + (h >> 1) - (dCircle >> 1);
        RectF rectF = new RectF(xCircle, yCircle, xCircle + dCircle, yCircle + dCircle);
        Path clipPath = new Path();
        clipPath.addRoundRect(rectF, dCircle >> 1, dCircle >> 1, Path.Direction.CW);
        p.setColor(AndroidUtilities.getTransparentColor(color, 0.5f));
        canvas.drawRoundRect(rectF, dCircle >> 1, dCircle >> 1, p);
        p.setColor(AndroidUtilities.getTransparentColor(color, 0.75f));
        canvas.drawRoundRect(rectF, dCircle >> 1, dCircle >> 1, p);
        canvas.clipPath(clipPath, Region.Op.DIFFERENCE);
        int topBarH = Math.round((h * 50f) / 100f);
        RectF rectF2 = new RectF(x, y, x + w, y + topBarH);
        p.setColor(AndroidUtilities.getTransparentColor(color, 0.5f));
        canvas.drawRoundRect(rectF2, rad1, rad1, p);
        int textH = Math.round(((h - topBarH) * 70f) / 100f);
        int textY = (y + h) - textH;
        RectF rectF3 = new RectF(x, textY, x + Math.round((w * 60f) / 100f), textY + textH);
        canvas.drawRoundRect(rectF3, rad1, rad1, p);
    }

    protected void onSelectionChange() {
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(102), MeasureSpec.EXACTLY));
    }
}
