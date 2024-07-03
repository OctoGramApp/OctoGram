package it.octogram.android.preferences.ui.components;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.DrawerLayoutContainer;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.DrawerProfileCell;

@SuppressLint("ViewConstructor")
public class DrawerPreviewCell extends FrameLayout {
    private final DrawerProfileCell view;

    public static final int height = dp(148);

    public DrawerPreviewCell(Context context) {
        super(context);

        FrameLayout scaledFrameLayout = new FrameLayout(context);
        scaledFrameLayout.setClipToPadding(true);
        scaledFrameLayout.setClipToOutline(true);
        scaledFrameLayout.setClipChildren(true);
        GradientDrawable scaledFrameBorder = new GradientDrawable();
        scaledFrameBorder.setShape(GradientDrawable.RECTANGLE);
        scaledFrameBorder.setColor(Color.TRANSPARENT);
        scaledFrameBorder.setCornerRadius(dp(25));
        scaledFrameLayout.setBackground(scaledFrameBorder);
        view = new DrawerProfileCell(context, new DrawerLayoutContainer(context) {
            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                super.onLayout(changed, left, top, right, bottom);
                setDrawerPosition(getDrawerPosition());
            }
        });
        view.isPreviewMode = true;
        view.setBackground(Theme.getThemedDrawable(context, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
        view.setUser(UserConfig.getInstance(UserConfig.selectedAccount).getCurrentUser(), false);
        scaledFrameLayout.addView(view);

        FrameLayout internalFrameLayout = new FrameLayout(context);
        internalFrameLayout.setClipToPadding(true);
        internalFrameLayout.setClipToOutline(true);
        internalFrameLayout.setClipChildren(true);
        internalFrameLayout.setPadding(dp(3), dp(3), dp(3), dp(3));
        GradientDrawable border = new GradientDrawable();
        border.setShape(GradientDrawable.RECTANGLE);
        border.setColor(Color.TRANSPARENT);
        border.setAlpha(150);
        border.setStroke(dp(1), Theme.getColor(Theme.key_windowBackgroundWhiteGrayText), dp(5), dp(5));
        border.setCornerRadius(dp(25));
        internalFrameLayout.setBackground(border);
        internalFrameLayout.addView(scaledFrameLayout);

        setPadding(dp(15), dp(15), dp(15), dp(15));
        setBackground(Theme.createRoundRectDrawable(0, Theme.getColor(Theme.key_windowBackgroundWhite)));
        addView(internalFrameLayout, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
    }

    public void reloadInstance() {
        view.setUser(UserConfig.getInstance(UserConfig.selectedAccount).getCurrentUser(), false);
    }

    public void updateMiniIcon() {
        view.updateMiniIcon();
    }

    public void updateDarkerBackgroundLevel(int level) {
        view.updateDarkerBackgroundLevel(level);
    }

    public void updateImageReceiver() {
        view.updateImageReceiver(UserConfig.getInstance(UserConfig.selectedAccount).getCurrentUser());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
}
