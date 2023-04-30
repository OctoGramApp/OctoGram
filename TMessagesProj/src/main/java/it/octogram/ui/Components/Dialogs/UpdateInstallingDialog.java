package it.octogram.ui.Components.Dialogs;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.LineProgressView;
import org.telegram.ui.Components.RLottieImageView;
import org.telegram.ui.Components.StickerImageView;

import java.util.Locale;

public class UpdateInstallingDialog extends BottomSheet {
    private final LinearLayout linearLayout;
    private int scrollOffsetY;
    private final NestedScrollView scrollView;
    private final int[] location = new int[2];
    private final View shadow;
    private AnimatorSet shadowAnimation;
    private final LineProgressView lineProgressView;
    private final TextView percentTextView;
    private final StickerImageView imageView;
    private final BottomSheetCell cell;
    private final TextView[] importCountTextView = new TextView[2];
    private final TextView[] infoTextView = new TextView[2];

    public UpdateInstallingDialog(Context context) {
        super(context, false);
        setApplyTopPadding(false);
        setApplyBottomPadding(false);
        setCanDismissWithSwipe(false);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        setDisableScroll(true);

        int colorBackground = Theme.getColor(Theme.key_dialogBackground);
        shadowDrawable = context.getResources().getDrawable(R.drawable.sheet_shadow_round).mutate();
        shadowDrawable.setColorFilter(new PorterDuffColorFilter(colorBackground, PorterDuff.Mode.MULTIPLY));
        FrameLayout container = new FrameLayout(context) {
            @Override
            public void setTranslationY(float translationY) {
                super.setTranslationY(translationY);
                updateLayout();
            }

            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                return super.onInterceptTouchEvent(ev);
            }

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouchEvent(MotionEvent e) {
                return false;
            }

            @Override
            protected void onDraw(Canvas canvas) {
                int top = (int) (scrollOffsetY - backgroundPaddingTop - getTranslationY());
                shadowDrawable.setBounds(0, top, getMeasuredWidth(), getMeasuredHeight());
                shadowDrawable.draw(canvas);
            }
        };
        container.setWillNotDraw(false);
        containerView = container;
        scrollView = new NestedScrollView(context) {

            private boolean ignoreLayout;

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int height = MeasureSpec.getSize(heightMeasureSpec);
                measureChildWithMargins(linearLayout, widthMeasureSpec, 0, heightMeasureSpec, 0);
                int contentHeight = linearLayout.getMeasuredHeight();
                int padding = (height / 5 * 2);
                int visiblePart = height - padding;
                if (contentHeight - visiblePart < AndroidUtilities.dp(90) || contentHeight < height / 2 + AndroidUtilities.dp(90)) {
                    padding = height - contentHeight;
                }
                if (padding < 0) {
                    padding = 0;
                }
                if (getPaddingTop() != padding) {
                    ignoreLayout = true;
                    setPadding(0, padding, 0, 0);
                    ignoreLayout = false;
                }
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
            }

            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                super.onLayout(changed, left, top, right, bottom);
                updateLayout();
            }

            @Override
            public void requestLayout() {
                if (ignoreLayout) {
                    return;
                }
                super.requestLayout();
            }

            @Override
            protected void onScrollChanged(int l, int t, int old_l, int old_t) {
                super.onScrollChanged(l, t, old_l, old_t);
                updateLayout();
            }

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouchEvent(@NonNull MotionEvent ev) {
                return false;
            }
        };
        scrollView.setFillViewport(true);
        scrollView.setWillNotDraw(false);
        scrollView.setClipToPadding(false);
        scrollView.setVerticalScrollBarEnabled(false);
        container.addView(scrollView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP, 0, 0, 0, 0));
        linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        FrameLayout mainLayout = new FrameLayout(context);
        linearLayout.addView(mainLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 4, 0, 4, 0));

        TextView textView = new TextView(context);
        textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        textView.setTextColor(getThemedColor(Theme.key_dialogTextBlack));
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setText(LocaleController.getString("InstallingUpdate", R.string.InstallingUpdate));
        mainLayout.addView(textView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT, 17, 20, 17, 0));

        imageView = new StickerImageView(context, currentAccount);
        imageView.setStickerPackName("UtyaDuck");
        imageView.setStickerNum(31);
        imageView.getImageReceiver().setAutoRepeat(1);
        mainLayout.addView(imageView, LayoutHelper.createFrame(160, 160, Gravity.CENTER_HORIZONTAL | Gravity.TOP, 17, 79, 17, 0));

        percentTextView = new TextView(context);
        percentTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        percentTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24);
        percentTextView.setTextColor(getThemedColor(Theme.key_dialogTextBlack));
        percentTextView.setText("0%");
        mainLayout.addView(percentTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 17, 262, 17, 0));

        for (int a = 0; a < 2; a++) {
            importCountTextView[a] = new TextView(context);
            importCountTextView[a].setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            importCountTextView[a].setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            importCountTextView[a].setTextColor(getThemedColor(Theme.key_dialogTextBlack));
            mainLayout.addView(importCountTextView[a], LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 17, 340, 17, 0));

            infoTextView[a] = new TextView(context);
            infoTextView[a].setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            infoTextView[a].setTextColor(getThemedColor(Theme.key_dialogTextGray3));
            infoTextView[a].setGravity(Gravity.CENTER_HORIZONTAL);
            mainLayout.addView(infoTextView[a], LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 30, 368, 30, 44));

            if (a == 0) {
                importCountTextView[a].setText(LocaleController.getString("InstallationInProgress", R.string.InstallationInProgress));
                infoTextView[a].setText(LocaleController.getString("InstallationWarning", R.string.InstallationWarning));
            } else {
                importCountTextView[a].setText(LocaleController.getString("ErrorDuringInstallation", R.string.ErrorDuringInstallation));
                infoTextView[a].setAlpha(0.0f);
                infoTextView[a].setTranslationY(AndroidUtilities.dp(10));
                importCountTextView[a].setAlpha(0.0f);
                importCountTextView[a].setTranslationY(AndroidUtilities.dp(10));
            }
        }

        lineProgressView = new LineProgressView(getContext());
        lineProgressView.setProgressColor(getThemedColor(Theme.key_featuredStickers_addButton));
        lineProgressView.setBackColor(getThemedColor(Theme.key_dialogLineProgressBackground));
        lineProgressView.setProgress(0f, false);
        mainLayout.addView(lineProgressView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 4, Gravity.LEFT | Gravity.TOP, 50, 307, 50, 0));

        cell = new BottomSheetCell(context, resourcesProvider);
        cell.setBackground(null);
        cell.setText(LocaleController.getString("Close", R.string.Close));
        cell.setVisibility(View.INVISIBLE);
        cell.background.setOnClickListener(v -> dismiss());
        cell.background.setPivotY(AndroidUtilities.dp(48));
        cell.background.setScaleY(0.04f);
        mainLayout.addView(cell, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 50, Gravity.LEFT | Gravity.TOP, 34, 247, 34, 0));

        scrollView.addView(linearLayout, LayoutHelper.createScroll(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP));
        FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(LayoutHelper.MATCH_PARENT, AndroidUtilities.getShadowHeight(), Gravity.BOTTOM | Gravity.LEFT);
        shadow = new View(context);
        shadow.setBackgroundColor(Theme.getColor(Theme.key_dialogShadowLine));
        shadow.setAlpha(0.0f);
        shadow.setTag(1);
        mainLayout.addView(shadow, frameLayoutParams);
    }

    public void setInstallingProgress(float status) {
        lineProgressView.setProgress(status, true);
        percentTextView.setText(String.format(Locale.getDefault(), "%d%%", Math.round(status * 100)));
    }

    private void runShadowAnimation(final boolean show) {
        if (show && shadow.getTag() != null || !show && shadow.getTag() == null) {
            shadow.setTag(show ? null : 1);
            if (show) {
                shadow.setVisibility(View.VISIBLE);
            }
            if (shadowAnimation != null) {
                shadowAnimation.cancel();
            }
            shadowAnimation = new AnimatorSet();
            shadowAnimation.playTogether(ObjectAnimator.ofFloat(shadow, View.ALPHA, show ? 1.0f : 0.0f));
            shadowAnimation.setDuration(150);
            shadowAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (shadowAnimation != null && shadowAnimation.equals(animation)) {
                        if (!show) {
                            shadow.setVisibility(View.INVISIBLE);
                        }
                        shadowAnimation = null;
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    if (shadowAnimation != null && shadowAnimation.equals(animation)) {
                        shadowAnimation = null;
                    }
                }
            });
            shadowAnimation.start();
        }
    }

    public void setError(String errorMessage) {
        imageView.setStickerNum(14);
        imageView.reloadSticker();
        cell.setVisibility(View.VISIBLE);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(250);
        animatorSet.setInterpolator(CubicBezierInterpolator.EASE_OUT);
        infoTextView[1].setText(errorMessage);
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(percentTextView, View.ALPHA, 0.0f),
                ObjectAnimator.ofFloat(percentTextView, View.TRANSLATION_Y, -AndroidUtilities.dp(10)),
                ObjectAnimator.ofFloat(infoTextView[0], View.ALPHA, 0.0f),
                ObjectAnimator.ofFloat(infoTextView[0], View.TRANSLATION_Y, -AndroidUtilities.dp(10)),
                ObjectAnimator.ofFloat(importCountTextView[0], View.ALPHA, 0.0f),
                ObjectAnimator.ofFloat(importCountTextView[0], View.TRANSLATION_Y, -AndroidUtilities.dp(10)),
                ObjectAnimator.ofFloat(infoTextView[1], View.ALPHA, 1.0f),
                ObjectAnimator.ofFloat(infoTextView[1], View.TRANSLATION_Y, 0),
                ObjectAnimator.ofFloat(importCountTextView[1], View.ALPHA, 1.0f),
                ObjectAnimator.ofFloat(importCountTextView[1], View.TRANSLATION_Y, 0),
                ObjectAnimator.ofFloat(lineProgressView, View.ALPHA, 0),
                ObjectAnimator.ofFloat(cell.linearLayout, View.TRANSLATION_Y, AndroidUtilities.dp(8), 0)
        );
        cell.background.animate().scaleY(1.0f).setInterpolator(new OvershootInterpolator(1.02f)).setDuration(250).start();
        cell.imageView.animate().scaleY(1.0f).scaleX(1.0f).setInterpolator(new OvershootInterpolator(1.02f)).setDuration(250).start();
        cell.imageView.playAnimation();
        animatorSet.start();
    }

    @Override
    protected boolean canDismissWithTouchOutside() {
        return false;
    }

    private void updateLayout() {
        View child = linearLayout.getChildAt(0);
        child.getLocationInWindow(location);
        int top = location[1];
        int newOffset = Math.max(top, 0);
        runShadowAnimation(!(location[1] + linearLayout.getMeasuredHeight() <= container.getMeasuredHeight() - AndroidUtilities.dp(113) + containerView.getTranslationY()));
        if (scrollOffsetY != newOffset) {
            scrollOffsetY = newOffset;
            scrollView.invalidate();
        }
    }

    @SuppressLint("ViewConstructor")
    public static class BottomSheetCell extends FrameLayout {

        private final View background;
        private final TextView textView;
        private final RLottieImageView imageView;
        private final LinearLayout linearLayout;
        private final Theme.ResourcesProvider resourcesProvider;

        public BottomSheetCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;

            background = new View(context);
            background.setBackground(Theme.createSimpleSelectorRoundRectDrawable(AndroidUtilities.dp(4), getThemedColor(Theme.key_featuredStickers_addButton), getThemedColor(Theme.key_featuredStickers_addButtonPressed)));
            addView(background, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, 0, 16, 16, 16, 16));

            linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            addView(linearLayout, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

            imageView = new RLottieImageView(context);
            imageView.setBackground(Theme.createCircleDrawable(AndroidUtilities.dp(20), getThemedColor(Theme.key_featuredStickers_buttonText)));
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            imageView.setColorFilter(new PorterDuffColorFilter(getThemedColor(Theme.key_featuredStickers_addButton), PorterDuff.Mode.MULTIPLY));
            imageView.setAnimation(R.raw.import_check, 26, 26);
            imageView.setScaleX(0.8f);
            imageView.setScaleY(0.8f);
            linearLayout.addView(imageView, LayoutHelper.createLinear(20, 20, Gravity.CENTER_VERTICAL));

            textView = new TextView(context);
            textView.setLines(1);
            textView.setSingleLine(true);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(getThemedColor(Theme.key_featuredStickers_buttonText));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 10, 0, 0, 0));
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(80), MeasureSpec.EXACTLY));
        }

        public void setTextColor(int color) {
            textView.setTextColor(color);
        }

        public void setGravity(int gravity) {
            textView.setGravity(gravity);
        }

        public void setText(CharSequence text) {
            textView.setText(text);
        }

        private int getThemedColor(String key) {
            Integer color = resourcesProvider != null ? resourcesProvider.getColor(key) : null;
            return color != null ? color : Theme.getColor(key);
        }
    }
}
