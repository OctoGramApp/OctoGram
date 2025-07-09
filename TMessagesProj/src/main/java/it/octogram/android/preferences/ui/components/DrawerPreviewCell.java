/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui.components;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.DrawerLayoutContainer;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.DrawerProfileCell;

import it.octogram.android.OctoConfig;

@SuppressLint("ViewConstructor")
public class DrawerPreviewCell extends FrameLayout implements NotificationCenter.NotificationCenterDelegate {
    private final DrawerProfileCell drawerProfileCell;
    private boolean inDrawer;
    private GradientDrawable border;

    public static final int height = dp(148);

    public DrawerPreviewCell(Context context) {
        this(context, null);
    }

    public DrawerPreviewCell(Context context, DrawerProfileCell view) {
        super(context);

        inDrawer = view != null;
        if (view == null) {
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
        }

        drawerProfileCell = view;

        FrameLayout internalFrameLayout = getInternalFrameLayout(context);

        setPadding(dp(inDrawer ? 10 : 15), dp(inDrawer ? (OctoConfig.INSTANCE.profileBubbleMoreTopPadding.getValue() ? 60 : 40) : 15), dp(inDrawer ? 10 : 15), dp(15));

        if (!inDrawer) {
            setBackground(Theme.createRoundRectDrawable(0, Theme.getColor(Theme.key_windowBackgroundWhite)));
        }

        addView(internalFrameLayout, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
    }

    public DrawerProfileCell getView() {
        return drawerProfileCell;
    }

    public void reloadInstance() {
        drawerProfileCell.setUser(UserConfig.getInstance(UserConfig.selectedAccount).getCurrentUser(), false);
    }

    public void updateMiniIcon() {
        drawerProfileCell.updateMiniIcon();
    }

    public void updateDarkerBackgroundLevel(int level) {
        drawerProfileCell.updateDarkerBackgroundLevel(level);
    }

    public void updateImageReceiver() {
        drawerProfileCell.updateImageReceiver(UserConfig.getInstance(UserConfig.selectedAccount).getCurrentUser());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return !inDrawer || super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return !inDrawer || super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return !inDrawer || super.onTouchEvent(ev);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.didApplyNewTheme);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didApplyNewTheme);
    }

    private FrameLayout getInternalFrameLayout(Context context) {
        FrameLayout scaledFrameLayout = new FrameLayout(context);
        scaledFrameLayout.setClipToPadding(true);
        scaledFrameLayout.setClipToOutline(true);
        scaledFrameLayout.setClipChildren(true);
        GradientDrawable scaledFrameBorder = new GradientDrawable();
        scaledFrameBorder.setShape(GradientDrawable.RECTANGLE);
        scaledFrameBorder.setColor(Color.TRANSPARENT);
        scaledFrameBorder.setCornerRadius(dp(25));
        scaledFrameLayout.setBackground(scaledFrameBorder);
        scaledFrameLayout.addView(drawerProfileCell);

        FrameLayout internalFrameLayout = new FrameLayout(context);
        internalFrameLayout.setClipToPadding(true);
        internalFrameLayout.setClipToOutline(true);
        internalFrameLayout.setClipChildren(true);
        if (!OctoConfig.INSTANCE.profileBubbleHideBorder.getValue() || !inDrawer) {
            internalFrameLayout.setPadding(dp(3), dp(3), dp(3), dp(3));
        }
        border = new GradientDrawable();
        border.setShape(GradientDrawable.RECTANGLE);
        border.setAlpha(255);
        if (!OctoConfig.INSTANCE.profileBubbleHideBorder.getValue() || !inDrawer) {
            border.setColor(Theme.getColor(Theme.key_chats_menuBackground));
            border.setStroke(dp(1), AndroidUtilities.getTransparentColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText), 0.5f), dp(5), dp(5));
        }
        border.setCornerRadius(dp(25));
        internalFrameLayout.setBackground(border);
        internalFrameLayout.addView(scaledFrameLayout);

        return internalFrameLayout;
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.didApplyNewTheme) {
            if (!OctoConfig.INSTANCE.profileBubbleHideBorder.getValue() || !inDrawer) {
                border.setColor(Theme.getColor(Theme.key_chats_menuBackground));
                border.setStroke(dp(1), AndroidUtilities.getTransparentColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText), 0.5f), dp(5), dp(5));
            }
            if (!inDrawer) {
                setBackground(Theme.createRoundRectDrawable(0, Theme.getColor(Theme.key_windowBackgroundWhite)));
            }
        }
    }
}
