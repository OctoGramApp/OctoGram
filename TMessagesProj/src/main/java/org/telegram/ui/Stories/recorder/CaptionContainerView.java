package org.telegram.ui.Stories.recorder;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.dpf2;
import static org.telegram.messenger.AndroidUtilities.lerp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BotWebViewVibrationEffect;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LiteMode;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.AdjustPanLayoutHelper;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedEmojiDrawable;
import org.telegram.ui.Components.AnimatedFloat;
import org.telegram.ui.Components.AnimatedTextView;
import org.telegram.ui.Components.BlurringShader;
import org.telegram.ui.Components.ButtonBounce;
import org.telegram.ui.Components.CaptionPhotoViewer;
import org.telegram.ui.Components.CombinedDrawable;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.EditTextCaption;
import org.telegram.ui.Components.EditTextEmoji;
import org.telegram.ui.Components.EmojiView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.MentionsContainerView;
import org.telegram.ui.Components.ScaleStateListAnimator;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.Stories.DarkThemeResourceProvider;

public class CaptionContainerView extends FrameLayout {

    protected Theme.ResourcesProvider resourcesProvider;
    private final FrameLayout containerView;

    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public final EditTextEmoji editText;
    private Drawable applyButtonCheck;
    private CombinedDrawable applyButtonDrawable;
    public ImageView applyButton;
    public FrameLayout limitTextContainer;
    public AnimatedTextView limitTextView;
    private int codePointCount;

    private final Paint fadePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final LinearGradient fadeGradient = new LinearGradient(0, 0, 0, AndroidUtilities.dp(10), new int[] { 0xffff0000, 0x00000000 }, new float[] { 0.05f, 1 }, Shader.TileMode.CLAMP);
    private final Matrix matrix = new Matrix();

    private final TextPaint hintTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private Bitmap hintTextBitmap;
    private final Paint hintTextBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    private final FrameLayout rootView;
    private final SizeNotifierFrameLayout sizeNotifierFrameLayout;

    public final KeyboardNotifier keyboardNotifier;
    public MentionsContainerView mentionContainer;

    private int shiftDp = -4;
    private final BlurringShader.BlurManager blurManager;

    private final BlurringShader.StoryBlurDrawer captionBlur;
    private final BlurringShader.StoryBlurDrawer backgroundBlur;
    private BlurringShader.StoryBlurDrawer mentionBackgroundBlur;

    protected int currentAccount = UserConfig.selectedAccount;
    public void setAccount(int currentAccount) {
        this.currentAccount = currentAccount;
    }

    private boolean ignoreTextChange;

    protected int getEditTextStyle() {
        return EditTextEmoji.STYLE_STORY;
    }

    boolean waitingForScrollYChange;
    int beforeScrollY;
    int goingToScrollY;

    public CaptionContainerView(Context context, FrameLayout rootView, SizeNotifierFrameLayout sizeNotifierFrameLayout, FrameLayout containerView, Theme.ResourcesProvider resourcesProvider, BlurringShader.BlurManager blurManager) {
        super(context);

        this.resourcesProvider = resourcesProvider;
        this.rootView = rootView;
        this.sizeNotifierFrameLayout = sizeNotifierFrameLayout;
        this.containerView = containerView;
        this.blurManager = blurManager;

        backgroundBlur = new BlurringShader.StoryBlurDrawer(blurManager, this, BlurringShader.StoryBlurDrawer.BLUR_TYPE_BACKGROUND, !customBlur());

        backgroundPaint.setColor(0x80000000);

        keyboardNotifier = new KeyboardNotifier(rootView, this::updateKeyboard);

        editText = new EditTextEmoji(context, sizeNotifierFrameLayout, null, getEditTextStyle(), true, new DarkThemeResourceProvider()) {
            @Override
            protected void onEmojiKeyboardUpdate() {
                keyboardNotifier.fire();
            }

            @Override
            protected void onWaitingForKeyboard() {
                keyboardNotifier.awaitKeyboard();
            }

            @Override
            protected void createEmojiView() {
                super.createEmojiView();
                EmojiView emojiView = getEmojiView();
                if (emojiView != null && getEditTextStyle() == EditTextEmoji.STYLE_STORY) {
                    emojiView.shouldLightenBackground = false;
                    emojiView.fixBottomTabContainerTranslation = false;
                    emojiView.setShouldDrawBackground(false);
                    if (CaptionContainerView.this instanceof CaptionPhotoViewer) {
                        emojiView.setPadding(0, 0, 0, AndroidUtilities.navigationBarHeight);
                        emojiView.emojiCacheType = AnimatedEmojiDrawable.CACHE_TYPE_ALERT_PREVIEW;
                    }
                }
            }

            private BlurringShader.StoryBlurDrawer blurDrawer;

            @Override
            protected void drawEmojiBackground(Canvas canvas, View view) {
                rectF.set(0, 0, view.getWidth(), view.getHeight());
                if (customBlur()) {
                    if (blurDrawer == null) {
                        blurDrawer = new BlurringShader.StoryBlurDrawer(blurManager, view, BlurringShader.StoryBlurDrawer.BLUR_TYPE_EMOJI_VIEW);
                    }
                    drawBlur(blurDrawer, canvas, rectF, 0, false, 0, -view.getY(), false);
                } else {
                    drawBackground(canvas, rectF, 0, .95f, view);
                }
            }

            @Override
            protected boolean onScrollYChange(int afterScrollY) {
                if (scrollAnimator != null && scrollAnimator.isRunning() && afterScrollY == goingToScrollY) {
                    return false;
                }
                CaptionContainerView.this.invalidate();
                if (waitingForScrollYChange) {
                    waitingForScrollYChange = false;
                    if (beforeScrollY != afterScrollY && (scrollAnimator == null || !scrollAnimator.isRunning() || afterScrollY != goingToScrollY)) {
                        if (scrollAnimator != null) {
                            scrollAnimator.cancel();
                        }
                        editText.getEditText().setScrollY(beforeScrollY);
                        scrollAnimator = ObjectAnimator.ofInt(editText.getEditText(), "scrollY", beforeScrollY, goingToScrollY = afterScrollY);
                        scrollAnimator.setDuration(240);
                        scrollAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
                        scrollAnimator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                if (scrollAnimator != animation) {
                                    return;
                                }
                                scrollAnimator = null;
                                editText.getEditText().setScrollY(goingToScrollY);
                            }
                        });
                        scrollAnimator.start();
                        return false;
                    }
                }
                return true;
            }
        };
        editText.getEditText().setShadowLayer(dp(10), 0, 0, 0);
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.getEditText().hintLayoutYFix = true;
        editText.getEditText().drawHint = this::drawHint;
        editText.getEditText().setSupportRtlHint(true);
        captionBlur = new BlurringShader.StoryBlurDrawer(blurManager, editText.getEditText(), customBlur() ? BlurringShader.StoryBlurDrawer.BLUR_TYPE_CAPTION : BlurringShader.StoryBlurDrawer.BLUR_TYPE_CAPTION_XFER);
        editText.getEditText().setHintColor(0x80ffffff);
        editText.getEditText().setHintText(LocaleController.getString("AddCaption", R.string.AddCaption), false);
        hintTextBitmapPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        editText.getEditText().setTranslationX(AndroidUtilities.dp(-40 + 18));
        editText.getEmojiButton().setAlpha(0f);
        editText.getEditText().addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (scrollAnimator == null || !scrollAnimator.isRunning()) {
                    beforeScrollY = editText.getEditText().getScrollY();
                    waitingForScrollYChange = true;
                }
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                if (editText.getEditText().suppressOnTextChanged) {
                    return;
                }
                if (mentionContainer == null) {
                    createMentionsContainer();
                }
                if (mentionContainer.getAdapter() != null) {
//                    mentionContainer.getAdapter().setUserOrChat(UserConfig.getInstance(currentAccount).getCurrentUser(), null);
                    mentionContainer.getAdapter().searchUsernameOrHashtag(text, editText.getEditText().getSelectionStart(), null, false, false);
                }
            }

            private int lastLength;
            private boolean lastOverLimit;

            @Override
            public void afterTextChanged(Editable s) {
                codePointCount = Character.codePointCount(s, 0, s.length());
                String limitText = null;
                final int limit = getCaptionLimit();
                if (codePointCount + 25 > limit) {
                    limitText = "" + (limit - codePointCount);
                }
                limitTextView.cancelAnimation();
                limitTextView.setText(limitText);
                limitTextView.setTextColor(codePointCount >= limit ? 0xffEC7777 : 0xffffffff);
                if (codePointCount > limit && !UserConfig.getInstance(currentAccount).isPremium() && codePointCount < getCaptionPremiumLimit() && codePointCount > lastLength && (captionLimitToast() || MessagesController.getInstance(currentAccount).premiumLocked)) {
                    AndroidUtilities.shakeViewSpring(limitTextView, shiftDp = -shiftDp);
                    BotWebViewVibrationEffect.APP_ERROR.vibrate();
                }
                lastLength = codePointCount;

                final boolean overLimit = codePointCount > limit;
                if (overLimit != lastOverLimit) {
                    onCaptionLimitUpdate(overLimit);
                }
                lastOverLimit = overLimit;

                if (!ignoreTextChange) {
                    AndroidUtilities.cancelRunOnUIThread(textChangeRunnable);
                    AndroidUtilities.runOnUIThread(textChangeRunnable, 1500);
                }
                ignoreTextChange = false;

                AndroidUtilities.runOnUIThread(() -> {
                    waitingForScrollYChange = false;
                });
            }
        });
        editText.getEditText().setLinkTextColor(Color.WHITE);
        addView(editText, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 12, 12, 12, 12));

        applyButton = new BounceableImageView(context);
        ScaleStateListAnimator.apply(applyButton, 0.05f, 1.25f);
        applyButtonCheck = context.getResources().getDrawable(R.drawable.input_done).mutate();
        applyButtonCheck.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogFloatingIcon), PorterDuff.Mode.SRC_IN));
        applyButtonDrawable = new CombinedDrawable(Theme.createCircleDrawable(AndroidUtilities.dp(16), Theme.getColor(Theme.key_dialogFloatingButton, resourcesProvider)), applyButtonCheck, 0, AndroidUtilities.dp(1));
        applyButtonDrawable.setCustomSize(AndroidUtilities.dp(32), AndroidUtilities.dp(32));
        applyButton.setImageDrawable(applyButtonDrawable);
        applyButton.setScaleType(ImageView.ScaleType.CENTER);
        applyButton.setAlpha(0f);
        applyButton.setVisibility(View.GONE);
        applyButton.setOnClickListener(e -> {
            closeKeyboard();
            AndroidUtilities.cancelRunOnUIThread(textChangeRunnable);
            textChangeRunnable.run();
        });
        applyButton.setTranslationY(-AndroidUtilities.dp(1));
        addView(applyButton, LayoutHelper.createFrame(44, 44, Gravity.RIGHT | Gravity.BOTTOM));

        limitTextView = new AnimatedTextView(context, false, true, true);
        limitTextView.setGravity(Gravity.CENTER);
        limitTextView.setTextSize(dp(15));
        limitTextView.setTextColor(0xffffffff);
        limitTextView.setAnimationProperties(.4f, 0, 320, CubicBezierInterpolator.EASE_OUT_QUINT);
        limitTextView.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM));
        limitTextContainer = new FrameLayout(context);
        limitTextContainer.setTranslationX(dp(2));
        limitTextContainer.addView(limitTextView, LayoutHelper.createFrame(52, 16, Gravity.RIGHT | Gravity.BOTTOM));
        addView(limitTextContainer, LayoutHelper.createFrame(52, 16, Gravity.RIGHT | Gravity.BOTTOM, 0, 0, 0, 50));

        fadePaint.setShader(fadeGradient);
        fadePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
    }

    private final Runnable textChangeRunnable = () -> onTextChange();
    protected void onTextChange() {}

    public void invalidateBlur() {
        invalidate();
        editText.getEditText().invalidate();
        editText.getEmojiButton().invalidate();
        if (mentionContainer != null) {
            mentionContainer.invalidate();
        }
        if (editText.getEmojiView() != null && customBlur()) {
            editText.getEmojiView().invalidate();
        }
    }

    private Utilities.CallbackVoidReturn<Bitmap> getUiBlurBitmap;
    public void setUiBlurBitmap(Utilities.CallbackVoidReturn<Bitmap> get) {
        getUiBlurBitmap = get;
    }

    public void closeKeyboard() {
        editText.closeKeyboard();
        editText.hidePopup(true);
    }

    public boolean ignoreTouches;

    protected boolean ignoreTouches() {
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ignoreTouches || ignoreTouches() || !bounds.contains(ev.getX(), ev.getY()) && !keyboardShown) {
            return false;
        }
        if (ev.getAction() == MotionEvent.ACTION_DOWN && !keyboardShown) {
            for (int i = 0; i < getChildCount(); ++i) {
                View child = getChildAt(i);
                if (child == null || !child.isClickable() || child.getVisibility() != View.VISIBLE || child.getAlpha() < .5f || editText == child) {
                    continue;
                }
                rectF.set(child.getX(), child.getY(), child.getX() + child.getWidth(), child.getY() + child.getHeight());
                if (rectF.contains(ev.getX(), ev.getY())) {
                    return super.dispatchTouchEvent(ev);
                }
            }
            editText.getEditText().requestFocus();
//            editText.getEditText().setSelection(editText.getEditText().length(), editText.getEditText().length());
            editText.openKeyboard();
            editText.getEditText().setScrollY(0);
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    private ObjectAnimator scrollAnimator;
    private void animateScrollTo(boolean end) {
        final EditTextCaption et = editText.getEditText();
        if (et == null || et.getLayout() == null) {
            return;
        }
        if (scrollAnimator != null) {
            scrollAnimator.cancel();
        }
        int sy = et.getScrollY();
        editText.setSelection(end ? editText.length() : 0);
        int totalLineHeight = et.getLayout().getLineTop(et.getLineCount());
        int visibleHeight = et.getHeight() - et.getPaddingTop() - et.getPaddingBottom();
        int nsy = end ? totalLineHeight - visibleHeight : 0;
        scrollAnimator = ObjectAnimator.ofInt(et, "scrollY", sy, nsy);
        scrollAnimator.setDuration(360);
        scrollAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        scrollAnimator.start();
    }

    private void createMentionsContainer() {
        mentionContainer = new MentionsContainerView(getContext(), UserConfig.getInstance(currentAccount).getClientUserId(), 0, LaunchActivity.getLastFragment(), null, new DarkThemeResourceProvider()) {
            @Override
            public void drawRoundRect(Canvas canvas, Rect rectTmp, float radius) {
                rectF.set(rectTmp);
                if (customBlur()) {
                    drawBlur(mentionBackgroundBlur, canvas, rectF, radius, false, -mentionContainer.getX(), -mentionContainer.getY(), false);
                } else {
                    Paint blurPaint = mentionBackgroundBlur.getPaint(1f);
                    if (blurPaint == null) {
                        CaptionContainerView.this.backgroundPaint.setAlpha(0x80);
                        canvas.drawRoundRect(rectF, radius, radius, CaptionContainerView.this.backgroundPaint);
                    } else {
                        canvas.drawRoundRect(rectF, radius, radius, blurPaint);
                        CaptionContainerView.this.backgroundPaint.setAlpha(0x50);
                        canvas.drawRoundRect(rectF, radius, radius, CaptionContainerView.this.backgroundPaint);
                    }
                }
            }
        };
        mentionBackgroundBlur = new BlurringShader.StoryBlurDrawer(blurManager, mentionContainer, BlurringShader.StoryBlurDrawer.BLUR_TYPE_BACKGROUND);
        setupMentionContainer();
        mentionContainer.withDelegate(new MentionsContainerView.Delegate() {

            @Override
            public void replaceText(int start, int len, CharSequence replacingString, boolean allowShort) {
                replaceWithText(start, len, replacingString, allowShort);
            }

            @Override
            public Paint.FontMetricsInt getFontMetrics() {
                return editText.getEditText().getPaint().getFontMetricsInt();
            }
        });
        containerView.addView(mentionContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.BOTTOM));
    }

    protected void setupMentionContainer() {
        mentionContainer.getAdapter().setAllowStickers(false);
        mentionContainer.getAdapter().setAllowBots(false);
        mentionContainer.getAdapter().setAllowChats(false);
        mentionContainer.getAdapter().setSearchInDailogs(true);
    }

    private void replaceWithText(int start, int len, CharSequence text, boolean parseEmoji) {
        if (editText == null) {
            return;
        }
        try {
            SpannableStringBuilder builder = new SpannableStringBuilder(editText.getText());
            builder.replace(start, start + len, text);
            if (parseEmoji) {
                Emoji.replaceEmoji(builder, editText.getEditText().getPaint().getFontMetricsInt(), AndroidUtilities.dp(20), false);
            }
            editText.setText(builder);
            editText.setSelection(start + text.length());
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public void onResume() {
        editText.onResume();
    }

    public void onPause() {
        editText.onPause();
    }

    private Utilities.Callback<Integer> onHeightUpdate;
    public void setOnHeightUpdate(Utilities.Callback<Integer> onHeightUpdate) {
        this.onHeightUpdate = onHeightUpdate;
    }

    public int getEditTextHeight() {
        return (int) heightAnimated.get();
    }

    public int getEditTextHeightClosedKeyboard() {
        return Math.min(dp(82), editText.getHeight());
    }

    private Utilities.Callback<Boolean> onKeyboardOpen;
    public void setOnKeyboardOpen(Utilities.Callback<Boolean> onKeyboardOpen) {
        this.onKeyboardOpen = onKeyboardOpen;
    }

    ObjectAnimator parentKeyboardAnimator;

    protected int additionalKeyboardHeight() {
        return AndroidUtilities.navigationBarHeight;
    }

    private void updateKeyboard(int keyboardHeight) {
        if (sizeNotifierFrameLayout != null) {
            sizeNotifierFrameLayout.notifyHeightChanged();
        }
        if (editText.isPopupShowing()) {
            keyboardHeight = Math.max(0, additionalKeyboardHeight() + editText.getEmojiPadding());
        } else if (editText.isWaitingForKeyboardOpen()) {
            keyboardHeight = Math.max(0, additionalKeyboardHeight() + editText.getKeyboardHeight());
        }
        keyboardHeight = Math.max(0, keyboardHeight - (sizeNotifierFrameLayout == null ? 0 : sizeNotifierFrameLayout.getBottomPadding()));
        View parent = (View) getParent();
        parent.clearAnimation();

        if (parentKeyboardAnimator != null) {
            parentKeyboardAnimator.removeAllListeners();
            parentKeyboardAnimator.cancel();
            parentKeyboardAnimator = null;
        }

        parentKeyboardAnimator = ObjectAnimator.ofFloat(parent, TRANSLATION_Y, parent.getTranslationY(), -keyboardHeight);
        if (keyboardHeight > AndroidUtilities.dp(20)) {
            parentKeyboardAnimator.setInterpolator(AdjustPanLayoutHelper.keyboardInterpolator);
            parentKeyboardAnimator.setDuration(AdjustPanLayoutHelper.keyboardDuration);
        } else {
            parentKeyboardAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
            parentKeyboardAnimator.setDuration(640);
        }
        parentKeyboardAnimator.start();

        toKeyboardShow = keyboardHeight > AndroidUtilities.dp(20);
        AndroidUtilities.cancelRunOnUIThread(updateShowKeyboard);
        AndroidUtilities.runOnUIThread(updateShowKeyboard);
        if (keyboardHeight < AndroidUtilities.dp(20)) {
            editText.getEditText().clearFocus();
            editText.hidePopup(true);
        }
    }

    private boolean toKeyboardShow;
    private Runnable updateShowKeyboard = () -> {
        updateShowKeyboard(toKeyboardShow, true);
    };

    protected int getEditTextLeft() {
        return 0;
    }

    protected void updateEditTextLeft() {
        editText.getEditText().setTranslationX(lerp(dp(-40 + 18) + getEditTextLeft(), dp(2), keyboardT));
    }

    public float keyboardT;
    public boolean keyboardShown;
    private ValueAnimator keyboardAnimator;
    private void updateShowKeyboard(boolean show, boolean animated) {
        if (keyboardShown == show) {
            return;
        }
        keyboardShown = show;
        if (keyboardAnimator != null) {
            keyboardAnimator.cancel();
            keyboardAnimator = null;
        }
        if (onKeyboardOpen != null) {
            onKeyboardOpen.run(show);
        }
        beforeUpdateShownKeyboard(show);
        if (animated) {
            if (show) {
                if (mentionContainer != null) {
                    mentionContainer.setVisibility(View.VISIBLE);
                }
                applyButton.setVisibility(View.VISIBLE);
            } else {
                editText.getEditText().scrollBy(0, -editText.getEditText().getScrollY());
            }
            keyboardAnimator = ValueAnimator.ofFloat(keyboardT, show ? 1 : 0);
            keyboardAnimator.addUpdateListener(anm -> {
                keyboardT = (float) anm.getAnimatedValue();
                editText.getEditText().setTranslationX(lerp(dp(-40 + 18) + getEditTextLeft(), dp(2), keyboardT));
                editText.setTranslationX(lerp(0, dp(-8), keyboardT));
                editText.setTranslationY(lerp(0, dp(10), keyboardT));
                limitTextContainer.setTranslationX(lerp(-dp(8), dp(2), keyboardT));
                limitTextContainer.setTranslationY(lerp(-dp(8), 0, keyboardT));
                editText.getEmojiButton().setAlpha(keyboardT);
                applyButton.setAlpha((float) Math.pow(keyboardT, 16));
                onUpdateShowKeyboard(keyboardT);
                if (mentionContainer != null) {
                    mentionContainer.setAlpha((float) Math.pow(keyboardT, 4));
                }
                editText.getEditText().invalidate();
                invalidate();
            });
            if (!show) {
                editText.getEditText().setAllowDrawCursor(false);
            }
            keyboardAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!show) {
                        applyButton.setVisibility(View.GONE);
                        if (mentionContainer != null) {
                            mentionContainer.setVisibility(View.GONE);
                        }
                    }
                    if (show) {
                        editText.getEditText().setAllowDrawCursor(true);
                    }
                    afterUpdateShownKeyboard(show);
                }
            });
            if (show) {
                keyboardAnimator.setInterpolator(AdjustPanLayoutHelper.keyboardInterpolator);
                keyboardAnimator.setDuration(AdjustPanLayoutHelper.keyboardDuration);
            } else {
                keyboardAnimator.setInterpolator(new FastOutSlowInInterpolator());
                keyboardAnimator.setDuration(420);
            }
            keyboardAnimator.start();
        } else {
            keyboardT = show ? 1 : 0;
            editText.getEditText().setTranslationX(lerp(AndroidUtilities.dp(-40 + 18) + getEditTextLeft(), AndroidUtilities.dp(2), keyboardT));
            editText.setTranslationX(lerp(0, AndroidUtilities.dp(-8), keyboardT));
            editText.setTranslationY(lerp(0, AndroidUtilities.dp(10), keyboardT));
            limitTextContainer.setTranslationX(lerp(-dp(8), dp(2), keyboardT));
            limitTextContainer.setTranslationY(lerp(-dp(8), 0, keyboardT));
            editText.getEmojiButton().setAlpha(keyboardT);
            applyButton.setVisibility(show ? View.VISIBLE : View.GONE);
            applyButton.setAlpha(show ? 1f : 0f);
            onUpdateShowKeyboard(keyboardT);
            editText.getEditText().setAllowDrawCursor(show);
            afterUpdateShownKeyboard(show);
            invalidate();
        }
        animateScrollTo(show);
        editText.setSuggestionsEnabled(show);
        if (!show) {
            editText.getEditText().setSpoilersRevealed(false, true);
        }

        if (show && SharedConfig.getDevicePerformanceClass() >= SharedConfig.PERFORMANCE_CLASS_AVERAGE && !LiteMode.isPowerSaverApplied()) {
            if (blurBitmap == null) {
                blurBitmap = Bitmap.createBitmap((int) (rootView.getWidth() / 12f), (int) (rootView.getHeight() / 12f), Bitmap.Config.ARGB_8888);
            }
            ignoreDraw = true;
            drawBlurBitmap(blurBitmap, 12);
            ignoreDraw = false;
            if (blurBitmap != null && !blurBitmap.isRecycled()) {
                blurBitmapShader = new BitmapShader(blurBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                if (blurBitmapMatrix == null) {
                    blurBitmapMatrix = new Matrix();
                } else {
                    blurBitmapMatrix.reset();
                }
                blurBitmapShader.setLocalMatrix(blurBitmapMatrix);
                if (blurPaint == null) {
                    blurPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
                    blurPaint.setColor(0xffffffff);
                }
                blurPaint.setShader(blurBitmapShader);
            } else {
                blurBitmap = null;
            }
        }
    }

    protected void onUpdateShowKeyboard(float keyboardT) {

    }

    protected void beforeUpdateShownKeyboard(boolean show) {

    }

    protected void afterUpdateShownKeyboard(boolean show) {

    }

    public int getCodePointCount() {
        return codePointCount;
    }

    public boolean isCaptionOverLimit() {
        return getCodePointCount() > getCaptionLimit();
    }

    protected int getCaptionLimit() {
        return UserConfig.getInstance(currentAccount).isPremium() ? getCaptionPremiumLimit() : getCaptionDefaultLimit();
    }

    protected int getCaptionDefaultLimit() {
        return 0;
    }

    protected int getCaptionPremiumLimit() {
        return 0;
    }

    protected void onCaptionLimitUpdate(boolean overLimit) {

    }

    protected boolean captionLimitToast() {
        return false;
    }

    protected void drawBlurBitmap(Bitmap bitmap, float amount) {
        // do draw
        Utilities.stackBlurBitmap(bitmap, (int) amount);
    }

    private Bitmap blurBitmap;
    private BitmapShader blurBitmapShader;
    private Matrix blurBitmapMatrix;
    private Paint blurPaint;

    public boolean onBackPressed() {
        if (editText.isPopupShowing()) {
            editText.hidePopup(true);
            return true;
        }

        if (editText.isKeyboardVisible() && !keyboardNotifier.ignoring) {
            closeKeyboard();
            return true;
        }

        return false;
    }

    private final AnimatedFloat heightAnimated = new AnimatedFloat(this, 0, 300, CubicBezierInterpolator.EASE_OUT_QUINT);
    private int lastHeight;
    private float lastHeightTranslation;

    private boolean ignoreDraw = false;

    private final RectF rectF = new RectF();
    private final RectF bounds = new RectF();

    protected void onEditHeightChange(int height) {}

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (ignoreDraw) {
            return;
        }
        int height = editText.getHeight();
        if (keyboardShown) {
            height = Math.max(dp(46), height);
        } else {
            height = Math.min(dp(82), height);
        }
        final int heightAnimated = (int) this.heightAnimated.set(height);
        if (heightAnimated != lastHeight) {
            onEditHeightChange(heightAnimated);
            if (onHeightUpdate != null) {
                onHeightUpdate.run(heightAnimated);
            }
            lastHeight = height;
        }
        updateMentionsLayoutPosition();
        final float heightTranslation = dpf2(-1) * keyboardT + height - heightAnimated;
        if (Math.abs(lastHeightTranslation - heightTranslation) >= 1) {
            editText.getEditText().setTranslationY(heightTranslation);
        }
        lastHeightTranslation = heightTranslation;

        final float pad = lerp(AndroidUtilities.dp(12), 0, keyboardT);
        bounds.set(
            pad,
            getHeight() - pad - heightAnimated,
            getWidth() - pad,
            getHeight() - pad
        );

        final float r = lerp(AndroidUtilities.dp(21), 0, keyboardT);
        if (customBlur()) {
            drawBlur(backgroundBlur, canvas, bounds, r, false, 0, 0, true);
            backgroundPaint.setAlpha((int) (lerp(0x26, 0x40, keyboardT)));
            canvas.drawRoundRect(bounds, r, r, backgroundPaint);
        } else {
            Paint[] blurPaints = backgroundBlur.getPaints(1f, 0, 0);
            if (blurPaints == null || blurPaints[1] == null) {
                backgroundPaint.setAlpha(0x80);
                canvas.drawRoundRect(bounds, r, r, backgroundPaint);
            } else {
                if (blurPaints[0] != null) {
                    canvas.drawRoundRect(bounds, r, r, blurPaints[0]);
                }
                if (blurPaints[1] != null) {
                    canvas.drawRoundRect(bounds, r, r, blurPaints[1]);
                }
                backgroundPaint.setAlpha(0x33);
                canvas.drawRoundRect(bounds, r, r, backgroundPaint);
            }
        }

        super.dispatchDraw(canvas);
    }

    public RectF getBounds() {
        return bounds;
    }

    private void drawHint(Canvas canvas, Runnable draw) {
        if (customBlur()) {
            if (hintTextBitmap == null) {
                draw.run();
                return;
            }
            final EditTextCaption e = editText.getEditText();
            canvas.translate(-e.hintLayoutX, 0);
            canvas.saveLayerAlpha(0, 0, hintTextBitmap.getWidth(), hintTextBitmap.getHeight(), 0xff, Canvas.ALL_SAVE_FLAG);
            rectF.set(0, 1, hintTextBitmap.getWidth(), hintTextBitmap.getHeight() - 1);
            drawBlur(captionBlur, canvas, rectF, 0, true, -editText.getX() - e.getPaddingLeft(), -editText.getY() - e.getPaddingTop() - e.getExtendedPaddingTop(), true);
            canvas.save();
            hintTextBitmapPaint.setAlpha(0xa5);
            canvas.drawBitmap(hintTextBitmap, 0, 0, hintTextBitmapPaint);
            canvas.restore();
            canvas.restore();
            return;
        }
        Paint blurPaint = captionBlur.getPaint(1f);
        if (blurPaint == null) {
            draw.run();
        } else {
            final EditTextCaption e = editText.getEditText();
            canvas.saveLayerAlpha(0, 0, e.getWidth(), e.getHeight(), 0xff, Canvas.ALL_SAVE_FLAG);
            draw.run();
            canvas.drawRect(0, 0, e.getWidth(), e.getHeight(), blurPaint);
            canvas.restore();
        }
    }

    protected boolean customBlur() {
        return false;
    }

    protected void drawBlur(BlurringShader.StoryBlurDrawer blur, Canvas canvas, RectF rect, float r, boolean text, float ox, float oy, boolean thisView) {

    }

    private void drawBackground(Canvas canvas, RectF rectF, float r, float alpha, View view) {
        if (keyboardT > 0 && blurPaint != null && blurBitmapShader != null && blurBitmap != null && !blurBitmap.isRecycled()) {
            blurBitmapMatrix.reset();
            blurBitmapMatrix.postScale((float) rootView.getWidth() / blurBitmap.getWidth(), (float) rootView.getHeight() / blurBitmap.getHeight());
            float x = 0, y = 0;
            for (int i = 0; i < 8 && view != null; ++i) {
                x += view.getX();
                y += view.getY();
                ViewParent parent = view.getParent();
                view = parent instanceof View ? (View) parent : null;
            }
            blurBitmapMatrix.postTranslate(-x, -y);
            blurBitmapShader.setLocalMatrix(blurBitmapMatrix);
            blurPaint.setAlpha((int) (0xFF * keyboardT * alpha));
            canvas.drawRoundRect(rectF, r, r, blurPaint);
        }
        backgroundPaint.setAlpha((int) (blurPaint == null ? 0x80 : lerp(0x80, 0x99, keyboardT) * alpha));
        canvas.drawRoundRect(rectF, r, r, backgroundPaint);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (child == editText) {
            float ty = Math.max(0, editText.getHeight() - dp(82) - editText.getScrollY()) * (1f - keyboardT);
            canvas.saveLayerAlpha(0, 0, getWidth(), getHeight(), 0xFF, Canvas.ALL_SAVE_FLAG);

            canvas.save();
            canvas.clipRect(bounds);
            canvas.translate(0, ty);
            final boolean result = super.drawChild(canvas, child, drawingTime);
            canvas.restore();

            canvas.save();
            matrix.reset();
            matrix.postTranslate(0, bounds.top - 1);
            fadeGradient.setLocalMatrix(matrix);
            canvas.drawRect(bounds.left, bounds.top, bounds.right, bounds.top + dp(10), fadePaint);

            matrix.reset();
            matrix.postRotate(180);
            matrix.postTranslate(0, bounds.bottom);
            fadeGradient.setLocalMatrix(matrix);
            canvas.drawRect(bounds.left, bounds.bottom - dp(10), bounds.right, bounds.bottom, fadePaint);
            canvas.restore();
            canvas.restore();

            return result;
        } else if (clipChild(child)) {
            canvas.save();
            canvas.clipRect(bounds);
            final boolean result = super.drawChild(canvas, child, drawingTime);
            canvas.restore();
            return result;
        }
        return super.drawChild(canvas, child, drawingTime);
    }

    protected boolean clipChild(View child) {
        return true;
    }

    public void clearFocus() {
        editText.clearFocus();
    }

    public void clear() {
        ignoreTextChange = true;
        editText.setText("");
    }

    public void setText(CharSequence text) {
        ignoreTextChange = true;
        editText.setText(text);
    }

    public CharSequence getText() {
        return editText.getText();
    }

    public void updateMentionsLayoutPosition() {
        if (mentionContainer != null) {
            float y = ((View) getParent()).getTranslationY() - heightAnimated.get();
            if (mentionContainer.getY() != y) {
                mentionContainer.setTranslationY(y);
                mentionContainer.invalidate();
            }
        }
    }

    public static class BounceableImageView extends ImageView {
        private final float scale;
        public BounceableImageView(Context context) {
            this(context, .2f);
        }
        public BounceableImageView(Context context, float scale) {
            super(context);
            this.scale = scale;
        }

        private final ButtonBounce bounce = new ButtonBounce(this);

        @Override
        public void setPressed(boolean pressed) {
            super.setPressed(pressed);
            bounce.setPressed(pressed);
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.save();
            final float scale = bounce.getScale(this.scale);
            canvas.scale(scale, scale, getWidth() / 2f, getHeight() / 2f);
            super.draw(canvas);
            canvas.restore();
        }
    }

    public int getSelectionLength() {
        if (editText == null || editText.getEditText() == null) {
            return 0;
        }
        try {
            return editText.getEditText().getSelectionEnd() - editText.getEditText().getSelectionStart();
        } catch (Exception e) {
            FileLog.e(e);
        }
        return 0;
    }

    public void updateColors(Theme.ResourcesProvider resourcesProvider) {
        this.resourcesProvider = resourcesProvider;
        applyButtonCheck.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_dialogFloatingIcon), PorterDuff.Mode.SRC_IN));
        applyButtonDrawable.setBackgroundDrawable(Theme.createCircleDrawable(AndroidUtilities.dp(16), Theme.getColor(Theme.key_dialogFloatingButton, resourcesProvider)));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (customBlur()) {
            if (hintTextBitmap != null) {
                hintTextBitmap.recycle();
                hintTextBitmap = null;
            }
            hintTextPaint.setColor(0xff000000);
            hintTextPaint.setTextSize(dp(16));
            final String text = LocaleController.getString(R.string.AddCaption);
            final int w = (int) Math.ceil(hintTextPaint.measureText(text));
            final int h = (int) Math.ceil(hintTextPaint.getFontMetrics().descent - hintTextPaint.getFontMetrics().ascent);
            hintTextBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(hintTextBitmap);
            canvas.drawText(text, 0, -(int) hintTextPaint.getFontMetrics().ascent, hintTextPaint);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (blurBitmap != null) {
            blurBitmap.recycle();
        }
        blurBitmapShader = null;
        blurPaint = null;
        if (hintTextBitmap != null) {
            hintTextBitmap.recycle();
            hintTextBitmap = null;
        }
    }

    public static class PeriodDrawable extends Drawable {

        private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        public final AnimatedTextView.AnimatedTextDrawable textDrawable = new AnimatedTextView.AnimatedTextDrawable(true, false, false) {
            @Override
            public void invalidateSelf() {
                PeriodDrawable.this.invalidateSelf();
            }
        };

        private boolean filled = false;
        private final AnimatedFloat fillT = new AnimatedFloat(this::invalidateSelf, 0, 350, CubicBezierInterpolator.EASE_OUT_QUINT);

        public PeriodDrawable() {
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setStrokeWidth(dpf2(1.66f));
            strokePaint.setStrokeCap(Paint.Cap.ROUND);

            textDrawable.setAnimationProperties(.3f, 0, 250, CubicBezierInterpolator.EASE_OUT_QUINT);
            textDrawable.setTypeface(AndroidUtilities.getTypeface("fonts/num.otf"));
            textDrawable.setTextSize(dpf2(12));
            textDrawable.setGravity(Gravity.CENTER);

            updateColors(0xffffffff, 0xff1A9CFF);
        }

        public void updateColors(int strokeColor, int fillColor) {
            strokePaint.setColor(strokeColor);
            textDrawable.setTextColor(strokeColor);
            fillPaint.setColor(fillColor);
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            final float cx = getBounds().centerY();
            final float cy = getBounds().centerY();

            final float r = dpf2(21) / 2f;
            final float fillT = this.fillT.set(filled);

            if (fillT > 0) {
                fillPaint.setAlpha((int) (0xFF * fillT));
                canvas.drawCircle(cx, cy, dpf2(11.33f) * fillT, fillPaint);
            }

            strokePaint.setAlpha((int) (0xFF * (1f - fillT)));
            AndroidUtilities.rectTmp.set(cx - r, cy - r, cx + r, cy + r);
            canvas.drawArc(AndroidUtilities.rectTmp, 90, 180, false, strokePaint);

            final int dashes = 5;
            final int gaps = dashes + 1;
            final float dashWeight = 1f, gapWeight = 1.5f;
            final float dashSweep = dashWeight / (dashes * dashWeight + gaps * gapWeight) * 180;
            final float gapSweep = gapWeight / (dashes * dashWeight + gaps * gapWeight) * 180;
            float a = gapSweep;
            for (int i = 0; i < dashes; ++i) {
                canvas.drawArc(AndroidUtilities.rectTmp, 270 + a, dashSweep, false, strokePaint);
                a += dashSweep + gapSweep;
            }

            canvas.save();
            canvas.translate(0, -1);
            AndroidUtilities.rectTmp2.set(
                (int) (cx - dp(20)),
                (int) (cy - dp(20)),
                (int) (cx + dp(20)),
                (int) (cy + dp(20))
            );
            textDrawable.setBounds(AndroidUtilities.rectTmp2);
            textDrawable.draw(canvas);
            canvas.restore();
        }

        public void setValue(int num, boolean fill, boolean animated) {
            textDrawable.setText("" + num, animated);
            filled = fill;
            if (!animated) {
                fillT.set(filled, true);
            }
            invalidateSelf();
        }

        @Override
        public void setAlpha(int alpha) {

        }

        @Override
        public int getIntrinsicHeight() {
            return dp(24);
        }

        @Override
        public int getIntrinsicWidth() {
            return dp(24);
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {}

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSPARENT;
        }
    }

}
