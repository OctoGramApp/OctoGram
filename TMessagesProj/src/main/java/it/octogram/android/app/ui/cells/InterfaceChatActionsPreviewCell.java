/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui.cells;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ProfileActionsView;
import org.telegram.ui.PeerColorActivity;

public class InterfaceChatActionsPreviewCell extends FrameLayout {
    private final ProfileActionsView actionsView;

    public InterfaceChatActionsPreviewCell(Context context) {
        super(context);

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        linearLayout.setClipToPadding(true);
        linearLayout.setClipToOutline(true);
        linearLayout.setClipChildren(true);
        linearLayout.setPadding(0, dp(6), 0, dp(1));

        GradientDrawable border = new GradientDrawable();
        border.setShape(GradientDrawable.RECTANGLE);
        border.setColor(Theme.getColor(Theme.key_actionBarDefault));
        border.setCornerRadius(dp(15));
        linearLayout.setBackground(border);

        int btnColor;
        if (AndroidUtilities.computePerceivedBrightness(Theme.getColor(Theme.key_actionBarDefault)) > .8f) {
            btnColor = Theme.multAlpha(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText), .15f);
        } else if (AndroidUtilities.computePerceivedBrightness(Theme.getColor(Theme.key_actionBarDefault)) < .2f) {
            btnColor = Theme.multAlpha(Theme.adaptHSV(Theme.getColor(Theme.key_actionBarDefault), +0.02f, +0.25f), .35f);
        } else {
            btnColor = Theme.multAlpha(PeerColorActivity.adaptProfileEmojiColor(Theme.getColor(Theme.key_actionBarDefault)), .15f);
        }

        actionsView = new ProfileActionsView(context, dp(95));
        actionsView.mode = ProfileActionsView.MODE_GROUP;
        actionsView.setAlpha(1f);
        actionsView.setNotifications(true);
        actionsView.updatePosition(0, dp(95));
        actionsView.setAsPreview(true);
        actionsView.drawingBlur(false);
        actionsView.setActionsColor(btnColor, false);

        actionsView.beginApplyingActions();
        actionsView.set(ProfileActionsView.KEY_MESSAGE, true);
        actionsView.set(ProfileActionsView.KEY_NOTIFICATION, true);
        actionsView.set(ProfileActionsView.KEY_VOICE_CHAT, true);
        actionsView.set(ProfileActionsView.KEY_LEAVE, true);
        actionsView.commitActions();
        linearLayout.addView(actionsView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

        FrameLayout internalFrameLayout = new FrameLayout(context);
        internalFrameLayout.setClipToPadding(true);
        internalFrameLayout.setClipToOutline(true);
        internalFrameLayout.setClipChildren(true);
        internalFrameLayout.setPadding(dp(2), dp(2), dp(2), dp(2));

        border = new GradientDrawable();
        border.setShape(GradientDrawable.RECTANGLE);
        border.setColor(Color.TRANSPARENT);
        border.setAlpha(150);
        border.setStroke(dp(1), Theme.getColor(Theme.key_windowBackgroundWhiteGrayText), dp(5), dp(5));
        border.setCornerRadius(dp(16));
        internalFrameLayout.setBackground(border);

        internalFrameLayout.addView(linearLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

        setPadding(dp(15), dp(15), dp(15), dp(15));
        setBackground(Theme.createRoundRectDrawable(0, Theme.getColor(Theme.key_windowBackgroundWhite)));
        addView(internalFrameLayout, new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
    }

    public void animateUpdate() {
        if (actionsView.getTag() != null && actionsView.getTag() instanceof ViewPropertyAnimator v2) {
            v2.cancel();
            actionsView.setTag(null);
        }

        actionsView.setAlpha(1f);
        actionsView.setScaleX(1f);
        actionsView.setScaleY(1f);
        ViewPropertyAnimator animator = actionsView.animate().alpha(0f).scaleX(1.05f).scaleY(1.05f).withEndAction(() -> {
            actionsView.invalidate();

            actionsView.setAlpha(0f);
            actionsView.setScaleX(0.95f);
            actionsView.setScaleY(0.95f);

            ViewPropertyAnimator a2 = actionsView.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(200);
            actionsView.setTag(a2);
            a2.start();
        });
        actionsView.setTag(animator);
        animator.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.UNSPECIFIED));
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