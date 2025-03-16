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
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;

@SuppressLint("ClickableViewAccessibility")
public class StickerSizeCell extends FrameLayout {
    private final ActionBarLayout navigationLayout;
    private StickerSizeUI messagesCell;

    public static final int height = AndroidUtilities.displayMetrics.heightPixels / 5;

    public StickerSizeCell(Context context) {
        super(context);

        navigationLayout = new ActionBarLayout(context, false);

        FrameLayout internalFrameLayout = new FrameLayout(context);
        internalFrameLayout.setClipToPadding(true);
        internalFrameLayout.setClipToOutline(true);
        internalFrameLayout.setClipChildren(true);
        internalFrameLayout.setPadding(dp(2), dp(2), dp(2), dp(2));

        GradientDrawable border = new GradientDrawable();
        border.setShape(GradientDrawable.RECTANGLE);
        border.setColor(Color.TRANSPARENT);
        border.setAlpha(150);
        border.setStroke(dp(1), Theme.getColor(Theme.key_windowBackgroundWhiteGrayText), dp(5), dp(5));
        border.setCornerRadius(dp(16));
        internalFrameLayout.setBackground(border);

        internalFrameLayout.addView(getNavigationLayout(context), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        setPadding(dp(15), dp(15), dp(15), dp(15));
        setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, height));
        setBackground(Theme.createRoundRectDrawable(0, Theme.getColor(Theme.key_windowBackgroundWhite)));
        addView(internalFrameLayout, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
    }

    private FrameLayout getNavigationLayout(Context context) {
        GradientDrawable border = new GradientDrawable();
        border.setShape(GradientDrawable.RECTANGLE);
        border.setColor(Color.TRANSPARENT);
        border.setCornerRadius(dp(16));

        messagesCell = new StickerSizeUI();
        navigationLayout.setFragmentStack(new ArrayList<>());
        navigationLayout.addFragmentToStack(messagesCell);

        FrameLayout navigationFrame = new FrameLayout(context);
        navigationFrame.setClipToPadding(true);
        navigationFrame.setClipToOutline(true);
        navigationFrame.setClipChildren(true);
        navigationFrame.setBackground(border);
        navigationFrame.addView(navigationLayout);

        return navigationFrame;
    }

    public void reloadActionBar() {
        messagesCell.reloadActionBar();
    }

    public void invalidatePreviewMessages() {
        messagesCell.previewMessages.invalidate();
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
}
