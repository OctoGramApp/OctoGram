/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui.custom;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CheckBox2;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.SeekBarView;
import org.telegram.ui.Components.Switch;

import it.octogram.android.OctoConfig;

public class InterfaceSwitchesPreview extends FrameLayout {
    private final Context context;
    private final LinearLayout linearLayout;

    private Switch switchView;
    private int interfaceSwitchUI;

    private CheckBox2 checkBoxView;
    private int interfaceCheckboxUI;

    private SeekBarView seekBarView;
    private int interfaceSliderUI;

    public InterfaceSwitchesPreview(Context context) {
        super(context);
        this.context = context;

        linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);

        interfaceSwitchUI = OctoConfig.INSTANCE.interfaceSwitchUI.getValue();
        switchView = getSwitch(context, interfaceSwitchUI);
        linearLayout.addView(switchView, LayoutHelper.createLinear(37, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.CENTER_VERTICAL, 0, 0, 30, 0));

        interfaceCheckboxUI = OctoConfig.INSTANCE.interfaceCheckboxUI.getValue();
        checkBoxView = getCheckbox(context, interfaceCheckboxUI);
        linearLayout.addView(checkBoxView, LayoutHelper.createLinear(23, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 0, 0, 15, 0));

        interfaceSliderUI = OctoConfig.INSTANCE.interfaceSliderUI.getValue();
        seekBarView = getSeekBar(context, interfaceSliderUI);
        linearLayout.addView(seekBarView, LayoutHelper.createLinear(0, 44, 1f, Gravity.CENTER_VERTICAL, 0, 0, 0, 0));

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

        internalFrameLayout.addView(linearLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER));

        setPadding(dp(15), dp(15), dp(15), dp(15));
        setBackground(Theme.createRoundRectDrawable(0, Theme.getColor(Theme.key_windowBackgroundWhite)));
        addView(internalFrameLayout, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
    }

    private static Switch getSwitch(Context context, int forcedUIState) {
        Switch switchView = new Switch(context, forcedUIState);
        switchView.setColors(Theme.key_switchTrack, Theme.key_switchTrackChecked, Theme.key_windowBackgroundWhite, Theme.key_windowBackgroundWhite);
        switchView.setEnabled(false);
        switchView.setChecked(true, false);
        return switchView;
    }

    private static CheckBox2 getCheckbox(Context context, int forcedUIState) {
        CheckBox2 checkBoxView = new CheckBox2(context, 21, forcedUIState);
        checkBoxView.setColor(Theme.key_radioBackgroundChecked, Theme.key_checkboxDisabled, Theme.key_checkboxCheck);
        checkBoxView.setDrawUnchecked(true);
        checkBoxView.setDrawBackgroundAsArc(10);
        checkBoxView.setChecked(true, false);
        return checkBoxView;
    }

    private static SeekBarView getSeekBar(Context context, int forcedUIState) {
        SeekBarView sizeBar = new SeekBarView(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int maxWidth = (int) ((float) (AndroidUtilities.displaySize.x * 40) /100);
                int width = MeasureSpec.getSize(widthMeasureSpec);
                int mode = MeasureSpec.getMode(widthMeasureSpec);

                if (width > maxWidth) {
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec(maxWidth, mode);
                }

                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        };
        sizeBar.setReportChanges(false);
        sizeBar.setEnabled(false);
        sizeBar.setPreviewingState(forcedUIState);
        sizeBar.setProgress(0.5f);
        return sizeBar;
    }

    public void animateUpdate() {
        if (interfaceSwitchUI != OctoConfig.INSTANCE.interfaceSwitchUI.getValue()) {
            interfaceSwitchUI = OctoConfig.INSTANCE.interfaceSwitchUI.getValue();
            forceRedrawItem(switchView);
        }

        if (interfaceCheckboxUI != OctoConfig.INSTANCE.interfaceCheckboxUI.getValue()) {
            interfaceCheckboxUI = OctoConfig.INSTANCE.interfaceCheckboxUI.getValue();
            forceRedrawItem(checkBoxView);
        }

        if (interfaceSliderUI != OctoConfig.INSTANCE.interfaceSliderUI.getValue()) {
            interfaceSliderUI = OctoConfig.INSTANCE.interfaceSliderUI.getValue();
            forceRedrawItem(seekBarView);
        }
    }

    private void forceRedrawItem(View item) {
        if (item.getTag() != null && item.getTag() instanceof ViewPropertyAnimator animator) {
            animator.cancel();
            item.setTag(null);
        }

        int index = linearLayout.indexOfChild(item);
        if (index == -1) {
            return;
        }
        ViewGroup.LayoutParams layoutParams = item.getLayoutParams();

        item.setAlpha(1f);
        item.setScaleX(1f);
        item.setScaleY(1f);
        ViewPropertyAnimator animator = item.animate().alpha(0f).scaleX(1.1f).scaleY(1.1f).withEndAction(() -> {
            View newView;
            if (item instanceof Switch) {
                newView = switchView = getSwitch(context, interfaceSwitchUI);
            } else if (item instanceof CheckBox2) {
                newView = checkBoxView = getCheckbox(context, interfaceCheckboxUI);
            } else {
                newView = seekBarView = getSeekBar(context, interfaceSliderUI);
            }

            newView.setAlpha(0f);
            newView.setScaleX(0.9f);
            newView.setScaleY(0.9f);

            linearLayout.removeView(item);
            linearLayout.addView(newView, index, layoutParams);

            ViewPropertyAnimator a2 = newView.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(200);
            newView.setTag(a2);
            a2.start();
        }).setDuration(200);
        item.setTag(animator);
        animator.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(dp(102), MeasureSpec.EXACTLY));
    }
}