package it.octogram.android.preferences.ui.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.Gravity;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.INavigationLayout;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.SeekBarView;

import it.octogram.android.OctoConfig;

@SuppressLint("ViewConstructor")
public class StickerSize extends FrameLayout {

    private final StickerSizePreviewMessages messagesCell;
    private final SeekBarView sizeBar;

    private final TextPaint textPaint;

    int startStickerSize = 2;
    int endStickerSize = 20;

    public StickerSize(Context context, INavigationLayout parentLayout) {
        super(context);

        setWillNotDraw(false);

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(AndroidUtilities.dp(16));

        sizeBar = new SeekBarView(context);
        sizeBar.setReportChanges(true);
        sizeBar.setDelegate((stop, progress) -> {
            sizeBar.getSeekBarAccessibilityDelegate().postAccessibilityEventRunnable(StickerSize.this);
            int progressSave = Math.round(startStickerSize + (endStickerSize - startStickerSize) * progress);
            OctoConfig.INSTANCE.maxStickerSize.updateValue(progressSave);
            onSeek();
            StickerSize.this.invalidate();
        });
        sizeBar.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        addView(sizeBar, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 38, Gravity.LEFT | Gravity.TOP, 9, 5, 43, 11));

        messagesCell = new StickerSizePreviewMessages(context, parentLayout);
        messagesCell.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        addView(messagesCell, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 0, 53, 0, 0));
    }


    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        sizeBar.getSeekBarAccessibilityDelegate().onInitializeAccessibilityEvent(this, event);
    }

    protected void onSeek() {
    }

    @Override
    protected void onDraw(Canvas canvas) {
        textPaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteValueText));
        float roundedValue = Math.round((float) OctoConfig.INSTANCE.maxStickerSize.getValue());
        canvas.drawText(String.valueOf((int) roundedValue), getMeasuredWidth() - AndroidUtilities.dp(39), AndroidUtilities.dp(28), textPaint);
        float progress = (roundedValue - startStickerSize) / (float) (endStickerSize - startStickerSize);
        sizeBar.setProgress(progress);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        messagesCell.invalidate();
        sizeBar.invalidate();
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        sizeBar.getSeekBarAccessibilityDelegate().onInitializeAccessibilityNodeInfoInternal(this, info);
    }

    @Override
    public boolean performAccessibilityAction(int action, Bundle arguments) {
        return super.performAccessibilityAction(action, arguments) || sizeBar.getSeekBarAccessibilityDelegate().performAccessibilityActionInternal(this, action, arguments);
    }
}


