package it.octogram.ui.Cells;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.Gravity;
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
        sizeBar.setDelegate(new SeekBarView.SeekBarViewDelegate() {
            @Override
            public void onSeekBarDrag(boolean stop, float progress) {
                int progressSave = Math.round(startStickerSize + (endStickerSize - startStickerSize) * progress);
                OctoConfig.setStickerSize(progressSave);
                onSeek();
                StickerSize.this.invalidate();
            }

            @Override
            public void onSeekBarPressed(boolean pressed) {

            }
        });
        sizeBar.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        addView(sizeBar, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 38, Gravity.LEFT | Gravity.TOP, 9, 5, 43, 11));

        messagesCell = new StickerSizePreviewMessages(context, parentLayout);
        messagesCell.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        addView(messagesCell, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 0, 53, 0, 0));
    }

    protected void onSeek() {
    }

    @Override
    protected void onDraw(Canvas canvas) {
        textPaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteValueText));
        canvas.drawText(String.valueOf(Math.round(OctoConfig.stickerSizeStack)), getMeasuredWidth() - AndroidUtilities.dp(39), AndroidUtilities.dp(28), textPaint);
        sizeBar.setProgress((OctoConfig.stickerSizeStack - startStickerSize) / (float) (endStickerSize - startStickerSize));
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


