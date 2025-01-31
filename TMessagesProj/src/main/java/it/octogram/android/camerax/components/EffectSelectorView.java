/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.camerax.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.DisplayCutout;
import android.view.Gravity;
import android.view.WindowInsets;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.Components.LayoutHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.octogram.android.camerax.CameraXController;
import it.octogram.android.camerax.CameraXView;

public class EffectSelectorView extends LinearLayout {

    private ButtonEffectView oldSelection;
    private boolean isEnabledButtons = true;

    public EffectSelectorView(Context context) {
        super(context);
        setPadding(0, getSpaceNotch(), 0, 0);
        setGravity(Gravity.CENTER);
        int colorBackground = Color.BLACK;
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{
                        AndroidUtilities.getTransparentColor(colorBackground, 0.4f),
                        AndroidUtilities.getTransparentColor(colorBackground, 0)
                });
        setBackground(gd);
    }

    @NonNull
    private static List<Integer> getListEffect(CameraXView cameraXView) {
        Set<Integer> effectSet = new HashSet<>();

        if (cameraXView.isNightModeSupported()) {
            effectSet.add(CameraXController.EffectFacing.CAMERA_NIGHT);
        }
        if (cameraXView.isAutoModeSupported()) {
            effectSet.add(CameraXController.EffectFacing.CAMERA_AUTO);
        }
        effectSet.add(CameraXController.EffectFacing.CAMERA_NONE);
        if (cameraXView.isWideModeSupported()) {
            effectSet.add(CameraXController.EffectFacing.CAMERA_WIDE);
        }
        if (cameraXView.isHdrModeSupported()) {
            effectSet.add(CameraXController.EffectFacing.CAMERA_HDR);
        }
        if (cameraXView.isBokehModeSupported()) {
            effectSet.add(CameraXController.EffectFacing.CAMERA_BOKEH);
        }
        if (cameraXView.isFaceRetouchModeSupported()) {
            effectSet.add(CameraXController.EffectFacing.CAMERA_FACE_RETOUCH);
        }

        return List.copyOf(effectSet);
    }

    public void resetSelectedEffect() {
        for (int i = 0; i < getChildCount(); i++) {
            LinearLayout linearLayout = (LinearLayout) getChildAt(i);
            ButtonEffectView buttonEffect = (ButtonEffectView) linearLayout.getChildAt(0);
            buttonEffect.toggleButton(buttonEffect.cameraType == CameraXController.EffectFacing.CAMERA_NONE, false);
            if (buttonEffect.cameraType == CameraXController.EffectFacing.CAMERA_NONE) {
                oldSelection = buttonEffect;
            }
        }
    }

    public void loadEffects(CameraXView cameraXView) {
        if (getChildCount() == 0) {
            var list_effect = getListEffect(cameraXView);
            if (list_effect.size() == 1) {
                return;
            }
            for (int i = 0; i < list_effect.size(); i++) {
                int effect = list_effect.get(i);
                LinearLayout linearLayout = new LinearLayout(getContext());
                linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1));
                linearLayout.setGravity(Gravity.CENTER);
                ButtonEffectView buttonEffect = getButtonEffectView(effect);
                if (effect == CameraXController.EffectFacing.CAMERA_NONE) {
                    oldSelection = buttonEffect;
                }
                linearLayout.addView(buttonEffect, LayoutHelper.createLinear(50, 50));
                addView(linearLayout);
            }
        }
    }

    @NonNull
    private ButtonEffectView getButtonEffectView(int effect) {
        ButtonEffectView buttonEffect = new ButtonEffectView(getContext(), effect) {
            @Override
            protected void onItemClick(ButtonEffectView buttonEffect, int camera_type) {
                if (isEnabledButtons) {
                    super.onItemClick(buttonEffect, camera_type);
                    if (oldSelection != null) {
                        oldSelection.toggleButton(false, true);
                    }
                    buttonEffect.toggleButton(true, true);
                    oldSelection = buttonEffect;
                    onEffectSelected(camera_type);
                }
            }
        };
        buttonEffect.toggleButton(effect == CameraXController.EffectFacing.CAMERA_NONE, false);
        return buttonEffect;
    }

    protected void onEffectSelected(int cameraEffect) {
    }

    public void setEnabledButtons(boolean clickable) {
        isEnabledButtons = clickable;
    }

    public void setScreenOrientation(int screenOrientation) {
        setOrientation(screenOrientation);
        int orientation = screenOrientation == VERTICAL ? -180 : 0;
        for (int i = 0; i < getChildCount(); i++) {
            ((LinearLayout) getChildAt(i)).getChildAt(0).setRotationX(orientation);
        }
        int colorBackground = Color.BLACK;
        if (screenOrientation == HORIZONTAL) {
            setPadding(0, getSpaceNotch(), 0, 0);
            GradientDrawable gd = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{
                            AndroidUtilities.getTransparentColor(colorBackground, 0.4f),
                            AndroidUtilities.getTransparentColor(colorBackground, 0)
                    });
            setBackground(gd);
        } else {
            setPadding(getSpaceNotch(), 0, 0, 0);
            GradientDrawable gd = new GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    new int[]{
                            AndroidUtilities.getTransparentColor(colorBackground, 0.4f),
                            AndroidUtilities.getTransparentColor(colorBackground, 0)
                    });
            setBackground(gd);
        }
    }

    public int getSpaceNotch() {
        int notchSize = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowInsets windowInsets = AndroidUtilities.findActivity(getContext()).getWindow().getDecorView().getRootWindowInsets();
            if (windowInsets != null) {
                DisplayCutout cutout = windowInsets.getDisplayCutout();
                if (cutout != null) {
                    List<Rect> boundRect = cutout.getBoundingRects();
                    if (!boundRect.isEmpty()) {
                        if (getOrientation() == HORIZONTAL) {
                            notchSize = boundRect.get(0).bottom;
                        } else {
                            notchSize = boundRect.get(0).right;
                            notchSize = notchSize > 500 ? 0 : notchSize;
                        }
                    }
                }
            }
        }
        return notchSize;
    }
}