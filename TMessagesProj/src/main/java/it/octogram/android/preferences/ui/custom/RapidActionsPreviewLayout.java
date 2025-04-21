/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui.custom;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.FlickerLoadingView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RLottieImageView;

import java.util.HashMap;
import java.util.Objects;

import it.octogram.android.InterfaceRapidButtonsActions;
import it.octogram.android.OctoConfig;
import it.octogram.android.utils.chat.RapidActionsHelper;

@SuppressLint("UseCompatLoadingForDrawables")
public class RapidActionsPreviewLayout extends FrameLayout {

    private final FrameLayout floatingButtonContainer;
    private final FrameLayout floatingButton2Container;
    private final RLottieImageView floatingButton2;
    private final RLottieImageView floatingButton;

    private ViewPropertyAnimator floatingButton1VisibilityChange;
    private ViewPropertyAnimator floatingButton2VisibilityChange;
    private ViewPropertyAnimator floatingButtonHideState;
    private ViewPropertyAnimator floatingButton2HideState;
    private ValueAnimator squaredFabAnimator;

    private final HashMap<RLottieImageView, Float> scaledViews = new HashMap<>();

    public RapidActionsPreviewLayout(Context context) {
        super(context);

        FrameLayout internalFrameLayout = new FrameLayout(context);
        internalFrameLayout.setClipToPadding(true);
        internalFrameLayout.setClipToOutline(true);
        internalFrameLayout.setClipChildren(true);

        GradientDrawable border = new GradientDrawable();
        border.setShape(GradientDrawable.RECTANGLE);
        border.setAlpha(150);
        border.setStroke(dp(1), Theme.getColor(Theme.key_windowBackgroundWhiteGrayText), dp(5), dp(5));
        border.setCornerRadius(dp(25));
        internalFrameLayout.setBackground(border);

        if (SharedConfig.getDevicePerformanceClass() >= SharedConfig.PERFORMANCE_CLASS_AVERAGE) {
            FlickerLoadingView progressView = new FlickerLoadingView(context);
            progressView.setViewType(FlickerLoadingView.DIALOG_CELL_TYPE);
            progressView.setIsSingleCell(true);
            progressView.setItemsCount(3);
            progressView.setColors(Theme.key_listSelector, Theme.key_listSelector, -1);
            internalFrameLayout.addView(progressView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP));
        }

        floatingButton2Container = new FrameLayout(context);
        internalFrameLayout.addView(floatingButton2Container, LayoutHelper.createFrame(36, 36, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.BOTTOM, LocaleController.isRTL ? 24 : 0, 0, LocaleController.isRTL ? 0 : 24, 14 + 60 + 8));
        floatingButton2Container.setOutlineProvider(new ViewOutlineProvider() {
            @SuppressLint("NewApi")
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, dp(36), dp(36));
            }
        });
        floatingButton2 = new RLottieImageView(context);
        floatingButton2.setScaleType(ImageView.ScaleType.CENTER);
        floatingButton2.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon), PorterDuff.Mode.MULTIPLY));
        floatingButton2.setImageResource(R.drawable.fab_compose_small);
        floatingButton2Container.setContentDescription(LocaleController.getString(R.string.NewMessageTitle));
        floatingButton2Container.addView(floatingButton2, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        floatingButtonContainer = new FrameLayout(context);
        internalFrameLayout.addView(floatingButtonContainer, LayoutHelper.createFrame(56, 56, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.BOTTOM, LocaleController.isRTL ? 14 : 0, 0, LocaleController.isRTL ? 0 : 14, 14));
        floatingButton = new RLottieImageView(context);
        floatingButton.setScaleType(ImageView.ScaleType.CENTER);
        floatingButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chats_actionIcon), PorterDuff.Mode.MULTIPLY));
        floatingButton.setAnimation(R.raw.write_contacts_fab_icon_camera, 56, 56);
        floatingButtonContainer.setOutlineProvider(new ViewOutlineProvider() {
            @SuppressLint("NewApi")
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, dp(56), dp(56));
            }
        });
        floatingButtonContainer.addView(floatingButton, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        Drawable drawable;
        drawable = Theme.createSimpleSelectorCircleDrawable(dp(56), Theme.getColor(Theme.key_chats_actionBackground), Theme.getColor(Theme.key_chats_actionPressedBackground));
        floatingButtonContainer.setBackground(drawable);

        drawable = Theme.createSimpleSelectorCircleDrawable(
                dp(36),
                ColorUtils.blendARGB(Theme.getColor(Theme.key_windowBackgroundWhite), Color.WHITE, 0.1f),
                Theme.blendOver(Theme.getColor(Theme.key_windowBackgroundWhite), Theme.getColor(Theme.key_listSelector))
        );
        floatingButton2Container.setBackground(drawable);

        internalFrameLayout.setPadding(dp(3), dp(10), dp(3), dp(3));
        setPadding(dp(15), dp(15), dp(15), dp(15));
        addView(internalFrameLayout, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        restart();
    }

    private boolean _buttonsVisible;
    private InterfaceRapidButtonsActions _mainButtonAction;
    private boolean _showSecondaryButton;
    private InterfaceRapidButtonsActions _secondaryButtonAction;
    private boolean _useSquaredFab;

    public void restart() {
        boolean buttonsVisible = OctoConfig.INSTANCE.rapidActionsMainButtonAction.getValue() != InterfaceRapidButtonsActions.HIDDEN.getValue();
        InterfaceRapidButtonsActions mainButtonAction = RapidActionsHelper.getStatus(true, false);
        boolean showSecondaryButton = buttonsVisible && OctoConfig.INSTANCE.rapidActionsSecondaryButtonAction.getValue() != InterfaceRapidButtonsActions.HIDDEN.getValue();
        InterfaceRapidButtonsActions secondaryButtonAction = RapidActionsHelper.getStatus(false, false);

        if (OctoConfig.INSTANCE.rapidActionsDefaultConfig.getValue()) {
            boolean storiesEnabled = MessagesController.getInstance(UserConfig.selectedAccount).storiesEnabled();
            buttonsVisible = true;
            mainButtonAction = storiesEnabled ? InterfaceRapidButtonsActions.POST_STORY : InterfaceRapidButtonsActions.SEND_MESSAGE;
            showSecondaryButton = storiesEnabled;
            secondaryButtonAction = InterfaceRapidButtonsActions.SEND_MESSAGE;
        }

        boolean useSquaredFab = OctoConfig.INSTANCE.useSquaredFab.getValue();

        if (_mainButtonAction == null) { // disable animations
            updateIconInView(mainButtonAction, true);
            updateIconInView(secondaryButtonAction, false);
            floatingButtonContainer.setAlpha(buttonsVisible ? 1f : 0f);
            floatingButton2Container.setAlpha(showSecondaryButton ? 1f : 0f);
            floatingButton2.setScaleX(getScaledView(floatingButton2));
            floatingButton2.setScaleY(getScaledView(floatingButton2));
            floatingButtonContainer.setBackground(generateDrawableForState(true, useSquaredFab ? 1f : 0f));
            floatingButton2Container.setBackground(generateDrawableForState(false, useSquaredFab ? 1f : 0f));
        } else {
            if (mainButtonAction != _mainButtonAction && buttonsVisible == _buttonsVisible && buttonsVisible) {
                InterfaceRapidButtonsActions finalMainButtonAction = mainButtonAction;
                animateSingleIconVisibility(true, true, () -> {
                    updateIconInView(finalMainButtonAction, true);
                    animateSingleIconVisibility(true);
                });
            }

            if (buttonsVisible != _buttonsVisible) {
                if (buttonsVisible) {
                    updateIconInView(mainButtonAction, true);
                }
                boolean finalButtonsVisible = buttonsVisible;
                InterfaceRapidButtonsActions finalMainButtonAction1 = mainButtonAction;
                updateContainerVisibility(true, !buttonsVisible, () -> {
                    if (!finalButtonsVisible) {
                        updateIconInView(finalMainButtonAction1, true);
                    }
                });
            }

            if (secondaryButtonAction != _secondaryButtonAction && showSecondaryButton == _showSecondaryButton && showSecondaryButton) {
                InterfaceRapidButtonsActions finalSecondaryButtonAction = secondaryButtonAction;
                animateSingleIconVisibility(false, true, () -> {
                    updateIconInView(finalSecondaryButtonAction, false);
                    animateSingleIconVisibility(false);
                });
            }

            if (showSecondaryButton != _showSecondaryButton) {
                if (showSecondaryButton) {
                    updateIconInView(secondaryButtonAction, false);
                    floatingButton2.setScaleX(getScaledView(floatingButton2));
                    floatingButton2.setScaleY(getScaledView(floatingButton2));
                }
                InterfaceRapidButtonsActions finalSecondaryButtonAction1 = secondaryButtonAction;
                boolean finalShowSecondaryButton = showSecondaryButton;
                updateContainerVisibility(false, !showSecondaryButton, () -> {
                    if (!finalShowSecondaryButton) {
                        updateIconInView(finalSecondaryButtonAction1, false);
                    }
                });
            }

            if (useSquaredFab != _useSquaredFab) {
                if (squaredFabAnimator != null) {
                    squaredFabAnimator.cancel();
                }

                ValueAnimator animator = ValueAnimator.ofFloat(useSquaredFab ? 0f : 1f, useSquaredFab ? 1f : 0f);
                animator.addUpdateListener(animation -> {
                    float val = (float) animation.getAnimatedValue();
                    floatingButtonContainer.setBackground(generateDrawableForState(true, val));
                    floatingButton2Container.setBackground(generateDrawableForState(false, val));
                });
                animator.start();
                squaredFabAnimator = animator;
            }
        }

        _buttonsVisible = buttonsVisible;
        _mainButtonAction = mainButtonAction;
        _showSecondaryButton = showSecondaryButton;
        _secondaryButtonAction = secondaryButtonAction;
        _useSquaredFab = useSquaredFab;
    }

    private ShapeDrawable generateDrawableForState(boolean isMainButton, float progress) {
        int size = isMainButton ? dp(56) : dp(36);
        float rad = size / 2f;
        if (progress > 0) {
            rad = rad - (progress * (rad - ((size * 16) / 56.0f)));
        }
        float[] radii = new float[]{rad, rad, rad, rad, rad, rad, rad, rad};

        ShapeDrawable defaultDrawable = new ShapeDrawable(new RoundRectShape(radii, null, null));
        defaultDrawable.getPaint().setColor(isMainButton ? Theme.getColor(Theme.key_chats_actionBackground) : ColorUtils.blendARGB(Theme.getColor(Theme.key_windowBackgroundWhite), Color.WHITE, 0.1f));
        defaultDrawable.setBounds(0, 0, size, size);

        return defaultDrawable;
    }

    private void updateContainerVisibility(boolean isMainButton, boolean shouldHide, Runnable onEnd) {
        if (isMainButton) {
            if (floatingButtonHideState != null) {
                floatingButtonHideState.cancel();
            }

            floatingButtonContainer.setAlpha(shouldHide ? 1f : 0f);
            floatingButtonContainer.setScaleX(shouldHide ? 1f : 0.5f);
            floatingButtonContainer.setScaleY(shouldHide ? 1f : 0.5f);

            ViewPropertyAnimator animator = floatingButtonContainer.animate().alpha(shouldHide ? 0f : 1f).scaleX(shouldHide ? 0.5f : 1f).scaleY(shouldHide ? 0.5f : 1f).setDuration(250);
            if (onEnd != null) {
                animator.withEndAction(onEnd);
            }
            floatingButtonHideState = animator;
            animator.start();
        } else {
            if (floatingButton2HideState != null) {
                floatingButton2HideState.cancel();
            }

            floatingButton2Container.setAlpha(shouldHide ? 1f : 0f);
            floatingButton2Container.setTranslationY(shouldHide ? 0f : dp(15));
            floatingButton2Container.setScaleX(shouldHide ? 1f : 0.7f);
            floatingButton2Container.setScaleY(shouldHide ? 1f : 0.7f);

            ViewPropertyAnimator animator = floatingButton2Container.animate().alpha(shouldHide ? 0f : 1f).scaleX(shouldHide ? 0.7f : 1f).scaleY(shouldHide ? 0.7f : 1f).translationY(shouldHide ? dp(25) : 0f).setDuration(250);
            if (onEnd != null) {
                animator.withEndAction(onEnd);
            }
            floatingButton2HideState = animator;
            animator.start();
        }
    }

    private void animateSingleIconVisibility(boolean isMainButton) {
        animateSingleIconVisibility(isMainButton, false, null);
    }

    private void animateSingleIconVisibility(boolean isMainButton, boolean shouldHide, Runnable onEnd) {
        RLottieImageView view = isMainButton ? floatingButton : floatingButton2;
        view.setAlpha(shouldHide ? 1f : 0f);
        view.setScaleX(shouldHide ? getScaledView(view) : 0.7f);
        view.setScaleY(shouldHide ? getScaledView(view) : 0.7f);
        if (isMainButton) {
            if (floatingButton1VisibilityChange != null) {
                floatingButton1VisibilityChange.cancel();
            }
        } else {
            if (floatingButton2VisibilityChange != null) {
                floatingButton2VisibilityChange.cancel();
            }
        }
        ViewPropertyAnimator animator = view.animate().scaleX(getScaledView(view) + (shouldHide ? 0.2f : 0)).scaleY(getScaledView(view) + (shouldHide ? 0.2f : 0)).alpha(shouldHide ? 0f : 1f).setDuration(180);
        if (onEnd != null) {
            animator.withEndAction(onEnd);
        }
        if (isMainButton) {
            floatingButton1VisibilityChange = animator;
        } else {
            floatingButton2VisibilityChange = animator;
        }
        animator.start();
    }

    private float getScaledView(RLottieImageView item) {
        if (scaledViews.containsKey(item)) {
            return Objects.requireNonNull(scaledViews.get(item));
        }

        return 1.0f;
    }

    private void updateIconInView(InterfaceRapidButtonsActions action, boolean isMainButton) {
        if (action == InterfaceRapidButtonsActions.HIDDEN) {
            return;
        }

        if (isMainButton) {
            scaledViews.remove(floatingButton);
        } else {
            scaledViews.remove(floatingButton2);
        }

        RapidActionsHelper.updateIconState(action, isMainButton ? floatingButtonContainer : floatingButton2Container, isMainButton ? floatingButton : floatingButton2, isMainButton);
        float scale = RapidActionsHelper.getScaleState(action, isMainButton);
        if (scale != 1f) {
            scaledViews.put(isMainButton ? floatingButton : floatingButton2, scale);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
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
