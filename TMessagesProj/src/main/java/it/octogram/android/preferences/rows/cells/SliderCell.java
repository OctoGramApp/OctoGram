/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.rows.cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.view.Gravity;
import android.widget.FrameLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.SeekBarView;

import it.octogram.android.preferences.rows.impl.SliderRow;

public class SliderCell extends FrameLayout {

    private final SeekBarView sizeBar;
    private final TextPaint textPaint;

    private SliderRow sliderRow;
    private int startRadius;
    private int endRadius;

    public SliderCell(Context context) {
        super(context);

        setWillNotDraw(false);

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(AndroidUtilities.dp(16));

        sizeBar = new SeekBarView(context);
        sizeBar.setReportChanges(true);
        sizeBar.setDelegate((stop, progress) -> {
            sliderRow.getPreferenceValue().updateValue(Math.round(startRadius + (endRadius - startRadius) * progress));

            Runnable runnable = sliderRow.getRunnable();
            if (runnable != null) {
                runnable.run();
            }

            requestLayout();
        });
        addView(sizeBar, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 38, Gravity.START | Gravity.TOP, 5, 5, 39, 0));
    }

    public void setSliderRow(SliderRow sliderRow) {
        this.sliderRow = sliderRow;
        this.startRadius = sliderRow.getMin();
        this.endRadius = sliderRow.getMax();
        sizeBar.setSeparatorsCount(endRadius - startRadius + 1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        textPaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteValueText));
        canvas.drawText(String.valueOf(sliderRow.getPreferenceValue().getValue()), getMeasuredWidth() - AndroidUtilities.dp(39), AndroidUtilities.dp(28), textPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), heightMeasureSpec);
        sizeBar.setProgress((sliderRow.getPreferenceValue().getValue() - startRadius) / (float) (endRadius - startRadius));
    }

    @Override
    public void invalidate() {
        super.invalidate();
        sizeBar.invalidate();
    }

}
