package it.octogram.android.preferences.ui.custom;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.NumberPicker;
import org.telegram.ui.Components.Premium.StarParticlesView;

import java.util.ArrayList;

import it.octogram.android.OctoConfig;
import it.octogram.android.icons.IconsResources;

public abstract class IconsSelector extends LinearLayout {
    private final Paint pickerDividersPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final NumberPicker picker1;
    private final IconsPreviewCell iconsPreviewCell;

    private final ArrayList<String> strings = new ArrayList<>();
    {
        strings.add(LocaleController.getString(R.string.ImproveIconsDefault));
        strings.add(LocaleController.getString(R.string.ImproveIconsSolar));

        if (canUseMemeMode()) {
            strings.add("Meme");
        }
    }

    public IconsSelector(Context context) {
        super(context);

        pickerDividersPaint.setStyle(Paint.Style.STROKE);
        pickerDividersPaint.setStrokeCap(Paint.Cap.ROUND);
        pickerDividersPaint.setStrokeWidth(dp(2));

        iconsPreviewCell = new IconsPreviewCell(context);
        iconsPreviewCell.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f));
        addView(iconsPreviewCell);

        picker1 = new NumberPicker(context, 13) {
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                var y = dp(31);
                pickerDividersPaint.setColor(Theme.getColor(Theme.key_radioBackgroundChecked));
                canvas.drawLine(dp(2), y, getMeasuredWidth() - dp(2), y, pickerDividersPaint);

                y = getMeasuredHeight() - dp(31);
                canvas.drawLine(dp(2), y, getMeasuredWidth() - dp(2), y, pickerDividersPaint);
            }
        };

        int[] _newVal = {-1};
        picker1.setWrapSelectorWheel(true);
        picker1.setMinValue(0);
        picker1.setDrawDividers(true);
        picker1.setMaxValue(strings.size() - 1);
        picker1.setFormatter(strings::get);
        picker1.setOnValueChangedListener((picker, oldVal, newVal) -> {
            invalidate();
            _newVal[0] = newVal;
            OctoConfig.INSTANCE.uiSolarIcons.updateValue(newVal >= 1);
            OctoConfig.INSTANCE.uiRandomMemeIcons.updateValue(newVal == 2);
            iconsPreviewCell.animateUpdate();
            picker.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
        });
        picker1.setOnScrollListener((view, scrollState) -> {
            if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE && _newVal[0] != -1) {
                onSelectedIcons();
            }
        });
        picker1.setValue(OctoConfig.INSTANCE.uiSolarIcons.getValue() ? ((OctoConfig.INSTANCE.uiRandomMemeIcons.getValue() && canUseMemeMode()) ? 2 : 1) : 0);
        addView(picker1, LayoutHelper.createFrame(132, LayoutHelper.MATCH_PARENT, Gravity.RIGHT, 0, 0, 21, 0));
    }

    public static boolean canUseMemeMode() {
        return BuildConfig.DEBUG_PRIVATE_VERSION || BuildVars.isBetaApp();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(dp(102), MeasureSpec.EXACTLY));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (picker1.getValue() == 1) {
            canvas.drawLine(dp(8), getMeasuredHeight() - 1, getMeasuredWidth() - dp(8), getMeasuredHeight() - 1, Theme.dividerPaint);
        }
    }

    protected abstract void onSelectedIcons();

    private static class IconsPreviewCell extends FrameLayout {
        private final ArrayList<ViewPropertyAnimator> animators = new ArrayList<>();
        private final ArrayList<ImageView> icons = new ArrayList<>();
        private boolean wasLastCompletedAnimationSolar = false;
        private boolean wasLastCompletedAnimationMemeIcons = false;
        private final StarParticlesView particlesView;

        private final ArrayList<Integer> previewIcons = new ArrayList<>();
        {
            previewIcons.add(R.drawable.msg_voice_phone);
            previewIcons.add(R.drawable.msg_permissions);
            previewIcons.add(R.drawable.msg_media);
            previewIcons.add(R.drawable.msg_link);
            previewIcons.add(R.drawable.msg_folders);
        }

        public IconsPreviewCell(Context context) {
            super(context);

            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);

            for (int i = 0; i < previewIcons.size(); i++) {
                ImageView imageView = new ImageView(getContext());
                imageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon), PorterDuff.Mode.MULTIPLY));
                icons.add(imageView);

                linearLayout.addView(imageView, LayoutHelper.createLinear(25, 25, Gravity.CENTER, 0, 0, i == previewIcons.size() - 1 ? 0 : 15, 0));
            }

            particlesView = new StarParticlesView(context);
            particlesView.setClipWithGradient();
            particlesView.drawable.colorKey = Theme.key_switchTrackChecked;
            particlesView.drawable.isCircle = true;
            particlesView.drawable.centerOffsetY = dp(102 / 2f);
            particlesView.drawable.minLifeTime = 7000;
            particlesView.drawable.randLifeTime = 8000;
            particlesView.drawable.size1 = 8;
            particlesView.drawable.useRotate = false;
            particlesView.drawable.updateColors();

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

            internalFrameLayout.addView(particlesView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER));
            internalFrameLayout.addView(linearLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER));

            setPadding(dp(15), dp(15), dp(15), dp(15));
            setBackground(Theme.createRoundRectDrawable(0, Theme.getColor(Theme.key_windowBackgroundWhite)));
            addView(internalFrameLayout, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT));

            particlesView.setAlpha(areMemeIconsEnabled() ? 0.3f : 0f);
            particlesView.setPaused(!areMemeIconsEnabled());
            fillIcons();
        }

        private void fillIcons() {
            if (icons.isEmpty()) {
                return;
            }

            int i = -1;
            for (int icon : previewIcons) {
                ImageView imageView = icons.get(++i);
                setIcon(imageView, icon);
            }
        }

        private void setIcon(ImageView imageView, int icon) {
            IconsResources resources = (IconsResources) getResources();
            Drawable drawable = resources.getDefaultDrawable(icon);

            if (OctoConfig.INSTANCE.uiSolarIcons.getValue()) {
                drawable = resources.getSolarDrawable(icon);
            }

            wasLastCompletedAnimationSolar = OctoConfig.INSTANCE.uiSolarIcons.getValue();
            wasLastCompletedAnimationMemeIcons = OctoConfig.INSTANCE.uiRandomMemeIcons.getValue();

            imageView.setImageDrawable(drawable);
        }

        private void animateUpdate() {
            for (ViewPropertyAnimator animator : animators) {
                if (animator != null) {
                    animator.cancel();
                }
            }
            animators.clear();

            if (wasLastCompletedAnimationSolar == OctoConfig.INSTANCE.uiSolarIcons.getValue() && wasLastCompletedAnimationMemeIcons == OctoConfig.INSTANCE.uiRandomMemeIcons.getValue()) {
                return;
            }

            particlesView.animate().alpha(areMemeIconsEnabled() ? 0.3f : 0f).setDuration(200).start();
            particlesView.setPaused(!areMemeIconsEnabled());

            for (int i = 0; i < icons.size(); i++) {
                ImageView icon = icons.get(i);

                int finalI = i;
                animateDisappear(icon, () -> animateUpdateAndAppear(icon, finalI));
            }
        }

        private boolean areMemeIconsEnabled() {
            return OctoConfig.INSTANCE.uiSolarIcons.getValue() && OctoConfig.INSTANCE.uiRandomMemeIcons.getValue() && canUseMemeMode();
        }

        private void animateDisappear(ImageView icon, Runnable onPostAnimationEnd) {
            final Boolean[] canceled = {false};

            int index = animators.size();
            ViewPropertyAnimator animator = icon.animate().scaleY(1.2f).scaleX(1.2f).alpha(0).setDuration(200).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    canceled[0] = true;

                    icon.setScaleX(1f);
                    icon.setScaleY(1f);
                    icon.setAlpha(1f);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);

                    if (!canceled[0]) {
                        animators.set(index, null);
                        onPostAnimationEnd.run();
                    }
                }
            });

            animators.add(animator);
            animator.start();
        }

        private void animateUpdateAndAppear(ImageView icon, int mainIndex) {
            final Boolean[] canceled = {false};

            setIcon(icon, previewIcons.get(mainIndex));

            icon.setScaleX(0.7f);
            icon.setScaleY(0.7f);
            icon.setAlpha(0f);

            int animatorIndex = animators.size();
            ViewPropertyAnimator animator = icon.animate().scaleY(1f).scaleX(1f).alpha(1f).setDuration(200).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    canceled[0] = true;

                    icon.setScaleX(1f);
                    icon.setScaleY(1f);
                    icon.setAlpha(1f);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);

                    if (!canceled[0]) {
                        animators.set(animatorIndex, null);
                    }
                }
            });

            animators.add(animator);
            animator.start();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(dp(102), MeasureSpec.EXACTLY));
        }
    }
}