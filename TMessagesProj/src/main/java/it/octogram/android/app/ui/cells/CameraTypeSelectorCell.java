/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui.cells;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.getTransparentColor;
import static org.telegram.messenger.LocaleController.getString;

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

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;

import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberPicker;

import it.octogram.android.OctoConfig;

public abstract class CameraTypeSelectorCell extends LinearLayout {

    private final NumberPicker picker1;
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint lensPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF cameraBodyRect = new RectF();
    private final Rect cameraIconRect = new Rect();
    private final Drawable[] cameraDrawables = new Drawable[4];
    private final PorterDuffColorFilter cameraColorFilter;
    private final int color;

    private final String[] strings = new String[]{
            getString(R.string.CameraTypeDefault),
            getString(R.string.CameraTypeX),
            getString(R.string.CameraType2),
            getString(R.string.CameraTypeSystem),
    };

    public CameraTypeSelectorCell(Context context) {
        super(context);

        setWillNotDraw(false);

        Paint pickerDividersPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pickerDividersPaint.setStyle(Paint.Style.STROKE);
        pickerDividersPaint.setStrokeCap(Paint.Cap.ROUND);
        pickerDividersPaint.setStrokeWidth(dp(2));

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(dp(2));

        lensPaint.setStyle(Paint.Style.STROKE);
        lensPaint.setStrokeWidth(4);

        int colorIcon = Theme.getColor(Theme.key_switchTrack);
        color = getTransparentColor(colorIcon, 0.5f);
        cameraColorFilter = new PorterDuffColorFilter(colorIcon, PorterDuff.Mode.SRC_ATOP);

        cameraDrawables[0] = AppCompatResources.getDrawable(context, R.drawable.telegram_camera_icon);
        cameraDrawables[1] = AppCompatResources.getDrawable(context, R.drawable.x_camera_icon);
        cameraDrawables[2] = AppCompatResources.getDrawable(context, R.drawable.camera_revert2);
        cameraDrawables[3] = AppCompatResources.getDrawable(context, R.drawable.system_camera_icon);

        AppCompatImageView appCompatImageView = drawMock(context);
        addView(appCompatImageView);

        picker1 = new NumberPicker(context, 13);
        picker1.setWrapSelectorWheel(true);
        picker1.setMinValue(0);
        picker1.setDrawDividers(true);
        picker1.setMaxValue(strings.length - 1);
        picker1.setFormatter(value -> strings[value]);

        final int[] _newVal = {-1};
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

        int selectedButton = OctoConfig.INSTANCE.cameraType.getValue();
        picker1.setValue(selectedButton);

        addView(picker1, LayoutHelper.createFrame(132, LayoutHelper.MATCH_PARENT, Gravity.RIGHT, 0, 0, 21, 0));
    }

    @NonNull
    private AppCompatImageView drawMock(Context context) {
        AppCompatImageView appCompatImageView = new AppCompatImageView(context) {
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);

                int h = getMeasuredHeight() - dp(20);
                int w = Math.round((h * 222.22F) / 100F);

                int left = (getMeasuredWidth() / 2) - (w / 2);
                int top = (getMeasuredHeight() / 2) - (h / 2);
                int right = w + left;
                int bottom = top + h;

                float radius = Math.round((h * 14.77F) / 100F);

                cameraBodyRect.set(left, top, right, bottom);
                borderPaint.setColor(color);
                canvas.drawRoundRect(cameraBodyRect, radius, radius, borderPaint);

                int sensorHeight = Math.round(h * 0.3201F);
                int frameWidth = Math.round(sensorHeight * 1.7692F);
                int gapBetween = dp(6);
                int captureLeft = left + gapBetween;
                int captureBottom = bottom - gapBetween;
                int captureTop = captureBottom - sensorHeight;
                int captureRight = captureLeft + frameWidth;

                int frameStroke = dp(2);
                int frameBottom = captureBottom - frameStroke;
                int frameStart = captureLeft + frameStroke;
                int frameTop = captureTop + frameStroke;
                int frameEnd = captureRight - frameStroke;

                int blockWidth = Math.round((frameEnd - frameStart) / 3F);
                int blockHeight = Math.round((frameBottom - frameTop) / 2F);

                lensPaint.setColor(color);
                for (int x = 0; x < 2; x++) {
                    int y = 1;
                    int xPosition = frameStart + (blockWidth * x);
                    int yPosition = frameTop + (blockHeight * y);
                    int lensDiameter = Math.round(blockWidth * 0.5F);
                    int lensCenterX = xPosition + (blockWidth / 2);
                    int lensCenterY = yPosition + (blockHeight / 2);
                    canvas.drawCircle(lensCenterX, lensCenterY, lensDiameter / 2F, lensPaint);
                }

                Drawable cameraDrawable = cameraDrawables[picker1.getValue()];
                if (cameraDrawable != null) {
                    int iconHeight = Math.round(h * 0.37F);
                    int iconWidth = Math.round(iconHeight * 0.9803F);
                    int iconLeft = (getMeasuredWidth() / 2) - (iconWidth / 2);
                    int iconTop = (getMeasuredHeight() / 2) - (iconHeight / 2);
                    cameraIconRect.set(iconLeft, iconTop, iconLeft + iconWidth, iconTop + iconHeight);

                    cameraDrawable.setBounds(cameraIconRect);
                    cameraDrawable.setAlpha(Math.round(255 * 0.5F));
                    cameraDrawable.setColorFilter(cameraColorFilter);
                    cameraDrawable.draw(canvas);
                }
            }
        };
        appCompatImageView.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f));

        return appCompatImageView;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(
                MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(dp(102), MeasureSpec.EXACTLY)
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (picker1.getValue() == 1) {
            canvas.drawLine(dp(8), getMeasuredHeight() - 1, getMeasuredWidth() - dp(8), getMeasuredHeight() - 1, Theme.dividerPaint);
        }
    }

    protected abstract void onSelectedCamera(int cameraSelected);
}
