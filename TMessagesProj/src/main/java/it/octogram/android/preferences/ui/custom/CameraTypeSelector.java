/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui.custom;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.getTransparentColor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.widget.LinearLayout;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberPicker;

import it.octogram.android.CameraType;
import it.octogram.android.OctoConfig;


public abstract class CameraTypeSelector extends LinearLayout {
    Paint pickerDividersPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    String[] strings = new String[]{
            LocaleController.getString(R.string.CameraTypeDefault),
            LocaleController.getString(R.string.CameraTypeX),
            LocaleController.getString(R.string.CameraType2),
            LocaleController.getString(R.string.CameraTypeSystem),
    };
    private final NumberPicker picker1;

    public CameraTypeSelector(Context context) {
        super(context);

        pickerDividersPaint.setStyle(Paint.Style.STROKE);
        pickerDividersPaint.setStrokeCap(Paint.Cap.ROUND);
        pickerDividersPaint.setStrokeWidth(dp(2));
        var colorIcon = Theme.getColor(Theme.key_switchTrack);
        var color = getTransparentColor(colorIcon, 0.5f);
        AppCompatImageView appCompatImageView = new AppCompatImageView(context) {
            @Override
            @SuppressLint("DrawAllocation")
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                var h = getMeasuredHeight() - dp(20);
                var w = Math.round((h * 222.22F) / 100F);

                var left = (getMeasuredWidth() / 2) - (w / 2);
                var top = (getMeasuredHeight() / 2) - (h / 2);
                var right = w + left;
                var bottom = top + h;
                var radius = Math.round((h * 14.77F) / 100F);

                var rectF = new RectF(left, top, right, bottom);
                var p = new Paint(Paint.ANTI_ALIAS_FLAG);
                p.setStyle(Paint.Style.STROKE);
                p.setColor(color);
                p.setStrokeWidth(dp(2));
                canvas.drawRoundRect(rectF, radius, radius, p);

                var sensorHeight = Math.round(h * 0.3201F);
                var frameWidth = Math.round(sensorHeight * 1.7692F);
                var gapBetween = dp(6);
                var captureLeft = left + gapBetween;
                var captureBottom = bottom - gapBetween;
                var captureTop = captureBottom - sensorHeight;
                var captureRight = captureLeft + frameWidth;

                var frameStroke = dp(2);
                var frameBottom = captureBottom - frameStroke;
                var frameStart = captureLeft + frameStroke;
                var frameTop = captureTop + frameStroke;
                var frameEnd = captureRight - frameStroke;
                var blockWidth = Math.round((frameEnd - frameStart) / 3F);
                var blockHeight = Math.round((frameBottom - frameTop) / 2F);

                Paint lensPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                for (var x = 0; x < 2; x++) {
                    var y = 1;
                    var xPosition = frameStart + (blockWidth * x);
                    var yPosition = frameTop + (blockHeight * y);
                    var lensDiameter = Math.round(blockWidth * 0.5F);
                    var lensCenterX = xPosition + (blockWidth / 2);
                    var lensCenterY = yPosition + (blockHeight / 2);
                    lensPaint.setStyle(Paint.Style.STROKE);
                    lensPaint.setStrokeWidth(4);
                    lensPaint.setColor(color);
                    canvas.drawCircle(lensCenterX, lensCenterY, Math.round(lensDiameter / 2F), lensPaint);
                }
                int cameraIconId;
                switch (CameraType.Companion.fromInt(picker1.getValue())) {
                    case TELEGRAM -> cameraIconId = R.drawable.telegram_camera_icon;
                    case CAMERA_X -> cameraIconId = R.drawable.x_camera_icon;
                    case CAMERA_2 -> cameraIconId = R.drawable.camera_revert2;
                    default -> cameraIconId = R.drawable.system_camera_icon;
                }

                Drawable cameraDrawable = AppCompatResources.getDrawable(context, cameraIconId);
                var iconHeight = Math.round(h * 0.37F);
                var iconWidth = Math.round(iconHeight * 0.9803F);
                var iconLeft = (getMeasuredWidth() / 2) - (iconWidth / 2);
                var iconTop = (getMeasuredHeight() / 2) - (iconHeight / 2);
                var iconRight = iconLeft + iconWidth;
                var iconBottom = iconTop + iconHeight;
                var cameraIconRect = new Rect(iconLeft, iconTop, iconRight, iconBottom);
                if (cameraDrawable != null) {
                    cameraDrawable.setBounds(cameraIconRect);
                    cameraDrawable.setAlpha(Math.round(255 * 0.5F));
                    cameraDrawable.setColorFilter(new PorterDuffColorFilter(colorIcon, PorterDuff.Mode.SRC_ATOP));
                    cameraDrawable.draw(canvas);
                }
            }
        };
        appCompatImageView.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f));

        addView(appCompatImageView);

        picker1 = new NumberPicker(context, 13) {
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                var y = dp(31);
                pickerDividersPaint.setColor(Theme.getColor(Theme.key_radioBackgroundChecked));
                canvas.drawLine(dp(2), y, getMeasuredWidth() - dp(2), y, pickerDividersPaint);

                y = getMeasuredHeight() - dp(31);
                canvas.drawLine(dp(2), y, getMeasuredWidth() - dp(2), y, pickerDividersPaint);
            }
        };

        int[] _newVal = {-1};
        picker1.setWrapSelectorWheel(true);
        picker1.setMinValue(0);
        picker1.setDrawDividers(true);
        picker1.setMaxValue(strings.length - 1);
        picker1.setFormatter(value -> strings[value]);
        picker1.setOnValueChangedListener((picker, oldVal, newVal) -> {
            appCompatImageView.invalidate();
            invalidate();
            _newVal[0] = newVal;
            picker.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        });
        picker1.setOnScrollListener((view, scrollState) -> {
            if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE && _newVal[0] != -1) {
                onSelectedCamera(_newVal[0]);
            }
        });
        var selectedButton = OctoConfig.INSTANCE.cameraType.getValue();
        picker1.setValue(selectedButton);
        addView(picker1, LayoutHelper.createFrame(132, LayoutHelper.MATCH_PARENT, Gravity.RIGHT, 0, 0, 21, 0));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(dp(102), MeasureSpec.EXACTLY));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (picker1.getValue() == 1) {
            canvas.drawLine(dp(8), getMeasuredHeight() - 1, getMeasuredWidth() - dp(8), getMeasuredHeight() - 1, Theme.dividerPaint);
        }
    }

    protected abstract void onSelectedCamera(int cameraSelected);
}