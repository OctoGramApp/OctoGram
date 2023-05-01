package it.octogram.ui.Cells.Dynamic;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.core.graphics.ColorUtils;

import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.RLottieDrawable;
import org.telegram.ui.Components.RLottieImageView;

abstract public class BaseButtonCell extends LinearLayout {
    protected RLottieDrawable cameraDrawable;
    protected RLottieImageView iv;
    private static Theme.ResourcesProvider resourcesProvider;
    protected OnClickDelegate onClickDelegate;

    public BaseButtonCell(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context);
        BaseButtonCell.resourcesProvider = resourcesProvider;
    }

    abstract public ThemeInfo getTheme();
    abstract public void updateColors();

    protected static int getThemedColor(int key) {
        return Theme.getColor(key, resourcesProvider);
    }

    public RLottieDrawable getAnimatedDrawable() {
        return cameraDrawable;
    }

    public RLottieImageView getLottieImageView() {
        return iv;
    }

    public static float getBackgroundAlpha() {
        int colorBack = getThemedColor(Theme.key_windowBackgroundWhite);
        float alphaColor = 0;
        for (int ratio = 20; ratio > 0; ratio -= 1) {
            try {
                int blendedColor = ColorUtils.blendARGB(colorBack, getBackColor(), ratio / 100f);
                double contrast = ColorUtils.calculateContrast(colorBack, blendedColor) * 100.0;
                alphaColor = ratio / 100f;
                if (Math.round(contrast) <= 112) { // 112 IS CONTRAST OF 0.07f ALPHA
                    break;
                }
            } catch (Exception ignored) {
            }
        }
        return alphaColor;
    }

    public static int getBackColor() {
        return ColorUtils.calculateLuminance(getThemedColor(Theme.key_windowBackgroundWhite)) > 0.5f ? 0xFF000000 : 0xFFFFFFFF;
    }

    protected boolean delegateOnClick(View ignored, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_UP && onClickDelegate != null) {
            onClickDelegate.onClick();
        }
        return false;
    }

    public void setOnClickDelegate(OnClickDelegate onClickDelegate) {
        this.onClickDelegate = onClickDelegate;
    }

    public interface OnClickDelegate {
        void onClick();
    }
}
