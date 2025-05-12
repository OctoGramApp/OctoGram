/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui.custom;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Vibrator;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ReplacementSpan;
import android.text.style.URLSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.ColorUtils;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.CodepointsLengthInputFilter;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.OutlineEditText;
import org.telegram.ui.Components.RLottieImageView;
import org.telegram.ui.Components.StickerImageView;
import org.telegram.ui.Components.URLSpanNoUnderline;
import org.telegram.ui.LaunchActivity;

import it.octogram.android.AiProvidersDetails;
import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;
import it.octogram.android.ai.helper.MainAiHelper;
import it.octogram.android.ai.openrouter.OpenRouterModels;
import it.octogram.android.preferences.fragment.OctoAnimationFragment;
import it.octogram.android.utils.OctoUtils;
import it.octogram.android.utils.appearance.PopupChoiceDialogUtils;

public class AiConfigBottomSheet extends BottomSheet {
    private final AiProvidersDetails provider;

    private int selectedPosition;
    private final ViewPager viewPager;
    private final AiConfigInterface callback;
    private OutlineEditText editTextCursor;
    private OutlineEditText modelTextCursor;
    private final BaseFragment fragment;

    private boolean shouldShowSuccessBulletin = false;

    public AiConfigBottomSheet(Context context, BaseFragment fragment, AiProvidersDetails provider, AiConfigInterface callback) {
        super(context, true);
        setApplyBottomPadding(false);
        setApplyTopPadding(false);
        fixNavigationBar(getThemedColor(Theme.key_windowBackgroundWhite));

        this.provider = provider;
        this.callback = callback;
        this.fragment = fragment;
        
        TextView textView;

        FrameLayout frameLayout = new FrameLayout(getContext());

        viewPager = new ViewPager(getContext()) {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouchEvent(MotionEvent ev) {
                return false;
            }

            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                return false;
            }
        };
        viewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
        viewPager.setOffscreenPageLimit(0);
        PagerAdapter pagerAdapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return provider.getNeedWarningZone() ? 3 : 2;
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                ViewPage viewPage = new ViewPage(context, position);
                container.addView(viewPage);
                return viewPage;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView((View) object);
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }
        };
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(selectedPosition = CurrentStep.INITIAL_STAGE);
        frameLayout.addView(viewPager, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 300, 0, 0, 18, 0, 0));

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(frameLayout);

        TextView buttonTextView = new TextView(context);
        buttonTextView.setPadding(dp(34), 0, dp(34), 0);
        buttonTextView.setGravity(Gravity.CENTER);
        buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        buttonTextView.setTypeface(AndroidUtilities.bold());
        buttonTextView.setText(getString(R.string.AiFeatures_AccessVia_Login_NextStep));
        buttonTextView.setTextColor(Color.WHITE);
        buttonTextView.setBackground(Theme.createSimpleSelectorRoundRectDrawable(dp(6), Color.parseColor("#8d3067"), ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_windowBackgroundWhite), 120)));
        buttonTextView.setOnClickListener(view -> {
            if (selectedPosition == (CurrentStep.INSERT_API_KEY + (provider.getNeedWarningZone() ? 1 : 0))) {
                checkApiKey();
                return;
            }

            viewPager.setCurrentItem(++selectedPosition);
        });
        linearLayout.addView(buttonTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, 0, 16, 15, 16, 8));

        if (provider.getStatusProperty().getValue()) {
            textView = new TextView(context);
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            textView.setText(formatString(R.string.AiFeatures_AccessVia_Login_DisableS, provider.getTitle()));
            textView.setTextColor(ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_dialogTextBlack), 150));
            textView.setOnClickListener(view -> {
                provider.getStatusProperty().updateValue(false);
                callback.onStateUpdated();

                if (OctoConfig.INSTANCE.aiFeaturesRecentProvider.getValue() == provider.getId()) {
                    OctoConfig.INSTANCE.aiFeaturesRecentProvider.clear();
                }

                shouldShowSuccessBulletin = false;
                dismiss();
            });
            linearLayout.addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, 0, 16, 0, 16, 0));
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                View child = viewPager.getChildAt(1);
                if (child != null) {
                    child.measure(
                            View.MeasureSpec.makeMeasureSpec(viewPager.getWidth(), View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    );

                    int currentHeight = viewPager.getLayoutParams().height;
                    int newHeight = child.getMeasuredHeight();

                    if (newHeight != currentHeight) {
                        ValueAnimator animator = ValueAnimator.ofInt(currentHeight, newHeight);
                        animator.addUpdateListener(valueAnimator -> {
                            int animatedValue = (Integer) valueAnimator.getAnimatedValue();
                            ViewGroup.LayoutParams layoutParams = viewPager.getLayoutParams();
                            layoutParams.height = animatedValue;
                            viewPager.setLayoutParams(layoutParams);
                        });
                        animator.setDuration(300);
                        animator.start();
                    }

                    if (position == (CurrentStep.INSERT_API_KEY + (provider.getNeedWarningZone() ? 1 : 0))) {
                        buttonTextView.setText(getString(R.string.AiFeatures_AccessVia_Login_NextStep_SaveKey));
                        AndroidUtilities.runOnUIThread(() -> {
                            editTextCursor.getEditText().requestFocus();
                            AndroidUtilities.showKeyboard(editTextCursor.getEditText());
                        }, 200);
                    }

                    if (position == CurrentStep.WARNING && provider.getNeedWarningZone() && child instanceof ViewPage d) {
                        AndroidUtilities.runOnUIThread(d::playAnimation, 200);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewPager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (viewPager.getChildCount() > 0) {
                    View firstChild = viewPager.getChildAt(0);
                    firstChild.measure(
                            View.MeasureSpec.makeMeasureSpec(viewPager.getWidth(), View.MeasureSpec.EXACTLY),
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    );

                    int initialHeight = firstChild.getMeasuredHeight();
                    ViewGroup.LayoutParams layoutParams = viewPager.getLayoutParams();
                    layoutParams.height = initialHeight;
                    viewPager.setLayoutParams(layoutParams);
                }
                viewPager.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        setCustomView(linearLayout);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (shouldShowSuccessBulletin) {
            BulletinFactory.of(fragment).createSuccessBulletin(formatString(R.string.AiFeatures_AccessVia_Login_Step3, provider.getTitle())).show();
        }
    }

    private String getTitleForStepId(int stepId) {
        if (stepId == CurrentStep.INITIAL_STAGE) {
            return getString(R.string.AiFeatures_AccessVia_Login_Step1);
        }

        if (provider.getNeedWarningZone() && stepId == CurrentStep.WARNING) {
            return getString(R.string.AiFeatures_AccessVia_Login_ExtraStep_Warning);
        }

        return getString(R.string.AiFeatures_AccessVia_Login_Step2);
    }

    private String getDescriptionForStepId(int stepId) {
        if (stepId == CurrentStep.INITIAL_STAGE) {
            return formatString(R.string.AiFeatures_AccessVia_Login_Step1_Desc, provider.getTitle());
        }

        if (provider.getNeedWarningZone() && stepId == CurrentStep.WARNING) {
            return formatString(R.string.AiFeatures_AccessVia_Login_ExtraStep_Warning_Desc, provider.getTitle());
        }

        return getString(R.string.AiFeatures_AccessVia_Login_Step2_Desc);
    }

    private boolean isCheckingApiKey = false;
    private void checkApiKey() {
        if (isCheckingApiKey) {
            return;
        }

        if (editTextCursor != null) {
            String value = editTextCursor.getEditText().getText().toString().replaceAll(" ", "").trim();
            if (value.length() > provider.getKeyMinLength() && value.length() < provider.getKeyMaxLength()) {
                Runnable saveData = () -> {
                    provider.getStatusProperty().updateValue(true);
                    provider.getKeyProperty().updateValue(value);

                    if (provider == AiProvidersDetails.OPENROUTER && modelTextCursor != null) {
                        OctoConfig.INSTANCE.aiFeaturesOpenRouterSelectedModel.updateValue(modelTextCursor.getEditText().getText().toString().trim());
                    }

                    callback.onStateUpdated();
                    shouldShowSuccessBulletin = callback.canShowSuccessBulletin();
                    dismiss();
                };

                if (!provider.getKeyProperty().getValue().equals(value)) {
                    isCheckingApiKey = true;
                    AndroidUtilities.hideKeyboard(editTextCursor);
                    final AlertDialog progressDialog = new AlertDialog(LaunchActivity.instance, AlertDialog.ALERT_TYPE_SPINNER);
                    progressDialog.show();
                    MainAiHelper.ping(provider, value, new MainAiHelper.OnResultState() {
                        @Override
                        public void onSuccess(String result) {
                            AndroidUtilities.runOnUIThread(() -> {
                                progressDialog.dismiss();
                                saveData.run();
                            });
                        }

                        @Override
                        public void onFailed() {
                            onFailedState(false);
                        }

                        @Override
                        public void onTooManyRequests() {
                            onFailedState(true);
                        }

                        @Override
                        public void onModelOverloaded() {
                            onFailedState(true);
                        }

                        public void onFailedState(boolean isServerError) {
                            isCheckingApiKey = false;
                            AndroidUtilities.runOnUIThread(() -> {
                                progressDialog.dismiss();

                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(fragment.getParentActivity());
                                alertDialogBuilder.setTitle(getString(isServerError ? R.string.AppName : R.string.AiFeatures_AccessVia_Login_NextStep_SaveKey_Wrong));
                                alertDialogBuilder.setMessage(getString(isServerError ? R.string.AiFeatures_AccessVia_Login_NextStep_SaveKey_Wrong_Server : R.string.AiFeatures_AccessVia_Login_NextStep_SaveKey_Wrong_Desc));
                                alertDialogBuilder.setPositiveButton(getString(R.string.AiFeatures_AccessVia_Login_NextStep_SaveKey), (dialog, which) -> saveData.run());
                                alertDialogBuilder.setNegativeButton(getString(R.string.Cancel), null);
                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();
                            });
                        }
                    });
                    return;
                }

                saveData.run();
            } else {
                shakeEditText();
            }
        }
    }

    private void shakeEditText() {
        if (editTextCursor != null) {
            AndroidUtilities.shakeView(editTextCursor);
            Vibrator v = (Vibrator) LaunchActivity.instance.getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) {
                v.vibrate(200);
            }
        }
    }

    public interface AiConfigInterface {
        void onStateUpdated();

        boolean canShowSuccessBulletin();
    }

    public static class CurrentStep {
        public static final int INITIAL_STAGE = 0;
        public static final int WARNING = 1;
        public static final int INSERT_API_KEY = 1;
    }

    private FrameLayout makeHint(int resId, CharSequence title, String subtitle) {
        FrameLayout hint = new FrameLayout(getContext());

        ImageView imageView = new ImageView(getContext());
        imageView.setColorFilter(Theme.getColor(Theme.key_dialogTextBlack, resourcesProvider));
        imageView.setImageResource(resId);
        hint.addView(imageView, LayoutHelper.createFrame(24, 24, Gravity.LEFT | Gravity.TOP, 0, 8, 0, 0));

        LinearLayout textLayout = new LinearLayout(getContext());
        textLayout.setOrientation(LinearLayout.VERTICAL);
        TextView textView1 = new TextView(getContext());
        textView1.setTextColor(Theme.getColor(Theme.key_dialogTextBlack, resourcesProvider));
        textView1.setTypeface(AndroidUtilities.bold());
        textView1.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp(14));
        textView1.setText(title);
        textLayout.addView(textView1, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 2.6f, 0, 0));

        AppCompatTextView textView2 = new AppCompatTextView(getContext());
        textView2.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        textView2.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp(14));
        textView2.setLinkTextColor(Color.parseColor("#8d3067"));
        textView2.setHighlightColor(ColorUtils.setAlphaComponent(Color.parseColor("#8d3067"), 100));
        textLayout.addView(textView2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 2.6f, 0, 0));
        hint.addView(textLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.FILL_HORIZONTAL, 41, 0, 0, 0));

        textView2.setMovementMethod(new AndroidUtilities.LinkMovementMethodMy());

        Spannable spannable = new SpannableString(OctoUtils.fromHtml(subtitle));
        URLSpan[] spans = spannable.getSpans(0, spannable.length(), URLSpan.class);
        for (URLSpan urlSpan : spans) {
            URLSpan span = urlSpan;
            int start = spannable.getSpanStart(span);
            int end = spannable.getSpanEnd(span);
            spannable.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL()) {
                @Override
                public void onClick(View widget) {
                    Browser.openInExternalBrowser(getContext(), getURL(), true);
                }
            };
            spannable.setSpan(span, start, end, 0);
        }
        textView2.setText(spannable);

        return hint;
    }

    private void openOpenRouterModelSelector() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.AiFeatures_AccessVia_Login_Step2_Model));

        final LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        Runnable[] runs = {null};

        String currentValue = modelTextCursor.getEditText().getText().toString();
        for (String value : OpenRouterModels.ALL_MODELS) {
            String finalValue = value;
            if (value.contains("/")) {
                value = value.split("/")[1];
            }

            PopupChoiceDialogUtils.CustomRadioDataCell cell = new PopupChoiceDialogUtils.CustomRadioDataCell(getContext());
            cell.setPadding(dp(4), 0, dp(4), 0);
            cell.setData(value.endsWith(":free") ? applyFreeSpan(value.replaceAll(":free", "")) : value, null, currentValue.equals(finalValue), 0);
            cell.setTag(value);
            cell.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector), Theme.RIPPLE_MASK_ALL));
            cell.setCheckColor(Theme.getColor(Theme.key_radioBackground), Theme.getColor(Theme.key_dialogRadioBackgroundChecked));
            linearLayout.addView(cell);
            cell.setOnClickListener(v -> {
                modelTextCursor.getEditText().setText(finalValue);
                if (runs[0] != null) {
                    runs[0].run();
                }
            });
        }

        builder.setView(linearLayout);
        builder.setNegativeButton(getString(R.string.Cancel), null);

        AlertDialog dialog = builder.create();
        dialog.show();
        runs[0] = dialog::dismiss;
    }

    private CharSequence applyFreeSpan(CharSequence str) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(" d  ");
        spannableStringBuilder.append(str);
        FreeSpan span = new FreeSpan(10);
        span.setColor(Theme.getColor(Theme.key_premiumGradient1));
        spannableStringBuilder.setSpan(span, 1, 2, 0);
        return spannableStringBuilder;
    }


    public static class FreeSpan extends ReplacementSpan {

        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        StaticLayout layout;
        float width, height;

        private final boolean outline;
        private int color;

        public void setTypeface(Typeface typeface) {
            textPaint.setTypeface(typeface);
        }

        public FreeSpan(float textSize) {
            this.outline = false;
            textPaint.setTypeface(AndroidUtilities.bold());
            bgPaint.setStyle(Paint.Style.FILL);
            textPaint.setTextSize(dp(textSize));
        }

        public void setColor(int color) {
            this.color = color;
        }

        private CharSequence text = "FREE";
        public void setText(CharSequence text) {
            this.text = text;
            if (layout != null) {
                layout = null;
                makeLayout();
            }
        }

        public void makeLayout() {
            if (layout == null) {
                layout = new StaticLayout(text, textPaint, AndroidUtilities.displaySize.x, Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
                width = layout.getLineWidth(0);
                height = layout.getHeight();
            }
        }

        @Override
        public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
            makeLayout();
            return (int) (dp(10) + width);
        }

        @Override
        public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float _x, int top, int _y, int bottom, @NonNull Paint paint) {
            makeLayout();

            int color = this.color;
            if (color == 0) {
                color = paint.getColor();
            }
            bgPaint.setColor(color);
            if (outline) {
                textPaint.setColor(color);
            } else {
                textPaint.setColor(AndroidUtilities.computePerceivedBrightness(color) > .721f ? Color.BLACK : Color.WHITE);
            }

            float x = _x + dp(2), y = _y - height + dp(1);
            AndroidUtilities.rectTmp.set(x, y, x + width, y + height);
            float r;
            r = dp(3.66f);
            AndroidUtilities.rectTmp.left -= dp(4);
            AndroidUtilities.rectTmp.top -= dp(2.33f);
            AndroidUtilities.rectTmp.right += dp(3.66f);
            AndroidUtilities.rectTmp.bottom += dp(1.33f);
            canvas.drawRoundRect(AndroidUtilities.rectTmp, r, r, bgPaint);

            canvas.save();
            canvas.translate(x, y);
            layout.draw(canvas);
            canvas.restore();
        }
    }

    private class ViewPage extends LinearLayout {
        private RLottieImageView imageView;

        public ViewPage(Context context, int p) {
            super(context);
            setOrientation(VERTICAL);

            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.VERTICAL);

            if (p == CurrentStep.INITIAL_STAGE) {
                OctoAnimationFragment octoFragment = new OctoAnimationFragment(context, null, provider.getAnimationScope());
                octoFragment.setDisableEffect(true);
                linearLayout.addView(octoFragment, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, OctoAnimationFragment.sz_no_text, Gravity.CENTER_HORIZONTAL));
            } else if (p == CurrentStep.WARNING && provider.getNeedWarningZone()) {
                RLottieImageView rLottieImageView = new RLottieImageView(context);
                rLottieImageView.setScaleType(AppCompatImageView.ScaleType.CENTER);
                rLottieImageView.setAnimation(R.raw.error, 46, 46);
                rLottieImageView.setBackground(Theme.createCircleDrawable(dp(72), Color.parseColor("#8d3067")));
                imageView = rLottieImageView;
                FrameLayout frameLayout = new FrameLayout(context);
                frameLayout.addView(rLottieImageView, LayoutHelper.createFrame(72, 72, Gravity.CENTER));
                linearLayout.addView(frameLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 16));
            } else {
                StickerImageView imageView = new StickerImageView(context, UserConfig.selectedAccount);
                imageView.setStickerPackName(OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME);
                imageView.setStickerNum(StickerUi.DUCK_DEV.getValue());
                imageView.getImageReceiver().setAutoRepeat(1);
                linearLayout.addView(imageView, LayoutHelper.createLinear(144, 144, Gravity.CENTER_HORIZONTAL, 0, 16, 0, 16));
            }

            TextView textView = new TextView(context);
            textView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
            textView.setTypeface(AndroidUtilities.bold());
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setText(getTitleForStepId(p));
            textView.setPadding(dp(30), 0, dp(30), 0);
            linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            textView = new TextView(context);
            textView.setTextColor(ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_dialogTextBlack), 150));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setText(getDescriptionForStepId(p));
            textView.setPadding(dp(30), dp(10), dp(30), dp(21));
            linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            if (p == CurrentStep.INITIAL_STAGE) {
                boolean isChatGPT = provider == AiProvidersDetails.CHATGPT;
                boolean isOpenRouter = provider == AiProvidersDetails.OPENROUTER;
                linearLayout.addView(
                        makeHint(
                                R.drawable.msg_language,
                                getString(isChatGPT ? R.string.AiFeatures_AccessVia_Login_ChatGPT_1 : isOpenRouter ? R.string.AiFeatures_AccessVia_Login_OpenRouter_1 : R.string.AiFeatures_AccessVia_Login_Google_1),
                                getString(isChatGPT ? R.string.AiFeatures_AccessVia_Login_ChatGPT_1_Desc : isOpenRouter ? R.string.AiFeatures_AccessVia_Login_OpenRouter_1_Desc : R.string.AiFeatures_AccessVia_Login_Google_1_Desc)
                        ),
                        LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.FILL_HORIZONTAL, 32, 0, 32, 16)
                );
                linearLayout.addView(
                        makeHint(
                                R.drawable.edit_passcode,
                                getString(isChatGPT ? R.string.AiFeatures_AccessVia_Login_ChatGPT_2 : isOpenRouter ? R.string.AiFeatures_AccessVia_Login_OpenRouter_2 : R.string.AiFeatures_AccessVia_Login_Google_2),
                                getString(isChatGPT ? R.string.AiFeatures_AccessVia_Login_ChatGPT_2_Desc : isOpenRouter ? R.string.AiFeatures_AccessVia_Login_OpenRouter_2_Desc : R.string.AiFeatures_AccessVia_Login_Google_2_Desc)
                        ),
                        LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.FILL_HORIZONTAL, 32, 0, 32, 16)
                );
                linearLayout.addView(
                        makeHint(R.drawable.msg_copy, getString(R.string.AiFeatures_AccessVia_Login_3), getString(R.string.AiFeatures_AccessVia_Login_3_Desc)),
                        LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.FILL_HORIZONTAL, 32, 0, 32, 16)
                );
            } else if (p == CurrentStep.WARNING && provider.getNeedWarningZone()) {
                String serviceName = provider.getTitle();
                linearLayout.addView(
                        makeHint(R.drawable.msg_media, getString(R.string.AiFeatures_AccessVia_Login_ExtraStep_Warning_1), formatString(R.string.AiFeatures_AccessVia_Login_ExtraStep_Warning_1_Desc, serviceName)),
                        LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.FILL_HORIZONTAL, 32, 0, 32, 16)
                );
                linearLayout.addView(
                        makeHint(R.drawable.msg_stories_timer, getString(R.string.AiFeatures_AccessVia_Login_ExtraStep_Warning_2), formatString(R.string.AiFeatures_AccessVia_Login_ExtraStep_Warning_2_Desc, serviceName)),
                        LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.FILL_HORIZONTAL, 32, 0, 32, 16)
                );
                if (provider == AiProvidersDetails.CHATGPT) {
                    linearLayout.addView(
                            makeHint(R.drawable.msg_payment_card, getString(R.string.AiFeatures_AccessVia_Login_ExtraStep_Warning_3), formatString(R.string.AiFeatures_AccessVia_Login_ExtraStep_Warning_3_Desc, serviceName)),
                            LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.FILL_HORIZONTAL, 32, 0, 32, 16)
                    );
                }
            } else if (p == (CurrentStep.INSERT_API_KEY + (provider.getNeedWarningZone() ? 1 : 0))) {
                OutlineEditText editText = new OutlineEditText(context);
                editText.setHint(getString(R.string.AiFeatures_AccessVia_Login_Step2_Hint));
                editText.getEditText().setText(provider.getKeyProperty().getValue());
                editText.updateColorAsDefined(Color.parseColor("#8d3067"));
                editText.getEditText().setCursorColor(Color.parseColor("#8d3067"));
                linearLayout.addView(editText, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 58, Gravity.LEFT | Gravity.TOP, 17, 0, 17, 0));
                editTextCursor = editText;

                InputFilter[] inputFilters = new InputFilter[1];
                inputFilters[0] = new CodepointsLengthInputFilter(provider.getKeyMaxLength()) {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dStart, int dEnd) {
                        if (source != null && !TextUtils.isEmpty(source) && TextUtils.indexOf(source, '\n') == source.length() - 1) {
                            checkApiKey();
                            return "";
                        }
                        CharSequence result = super.filter(source, start, end, dest, dStart, dEnd);
                        if (result != null && source != null && result.length() != source.length()) {
                            shakeEditText();
                        }
                        return result;
                    }
                };
                editText.getEditText().setFilters(inputFilters);
                editText.getEditText().setOnEditorActionListener((currentTextView, i, keyEvent) -> {
                    if (i == EditorInfo.IME_ACTION_DONE) {
                        checkApiKey();
                        return true;
                    }
                    return false;
                });

                if (provider == AiProvidersDetails.OPENROUTER) {
                    OutlineEditText modelText = new OutlineEditText(context);
                    modelText.setHint(getString(R.string.AiFeatures_AccessVia_Login_Step2_Model));
                    modelText.getEditText().setText(OctoConfig.INSTANCE.aiFeaturesOpenRouterSelectedModel.getValue());
                    modelText.updateColorAsDefined(Color.parseColor("#8d3067"));
                    modelText.getEditText().setFocusable(false);
                    modelText.getEditText().setClickable(true);
                    modelText.setFocusable(false);
                    modelText.setClickable(true);
                    modelText.getEditText().setCursorVisible(false);
                    modelText.getEditText().setInputType(InputType.TYPE_NULL);
                    linearLayout.addView(modelText, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 58, Gravity.LEFT | Gravity.TOP, 17, 15, 17, 0));
                    modelTextCursor = modelText;

                    modelText.getEditText().setOnClickListener((v) -> openOpenRouterModelSelector());
                }
            }

            addView(linearLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0, 0, 0));
        }

        public void playAnimation() {
            if (imageView != null) {
                imageView.playAnimation();
            }
        }
    }
}
