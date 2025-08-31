/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui.bottomsheets;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Vibrator;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
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
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.ColorUtils;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CodepointsLengthInputFilter;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.OutlineEditText;
import org.telegram.ui.Components.StickerImageView;
import org.telegram.ui.Components.URLSpanNoUnderline;
import org.telegram.ui.LaunchActivity;

import java.util.Locale;

import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;
import it.octogram.android.TranslatorProvider;
import it.octogram.android.utils.OctoUtils;
import it.octogram.android.utils.translator.SingleTranslationsHandler;
import it.octogram.android.utils.translator.providers.NewGoogleTranslator;
import it.octogram.android.utils.updater.UpdatesManager;

public class TranslatorConfigBottomSheet extends BottomSheet {
    private static final String SERVICE_NAME = "Google Cloud";
    private static final Integer MIN_LENGTH = 20;
    private static final Integer MAX_LENGTH = 90;

    private int selectedPosition;
    private final ViewPager viewPager;
    private final TranslatorConfigInterface callback;
    private OutlineEditText editTextCursor;
    private final BaseFragment fragment;
    
    private final boolean skipToApiKeyConfig;
    private final boolean isFromLocalTranslation;

    public TranslatorConfigBottomSheet(Context context, BaseFragment fragment, boolean isFromLocalTranslation, TranslatorConfigInterface callback) {
        super(context, true);
        setApplyBottomPadding(false);
        setApplyTopPadding(false);
        fixNavigationBar(getThemedColor(Theme.key_windowBackgroundWhite));

        this.callback = callback;
        this.fragment = fragment;

        this.isFromLocalTranslation = isFromLocalTranslation;
        skipToApiKeyConfig = OctoConfig.INSTANCE.translatorProvider.getValue() == TranslatorProvider.GOOGLE_CLOUD.getValue() && !TextUtils.isEmpty(OctoConfig.INSTANCE.googleCloudKeyTranslator.getValue()) && !isFromLocalTranslation;

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
                return (skipToApiKeyConfig || isFromLocalTranslation) ? 1 : 2;
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
        buttonTextView.setText(getString(isFromLocalTranslation ? R.string.TranslatorProvider_NeededExtension_Button : skipToApiKeyConfig ? R.string.AiFeatures_AccessVia_Login_NextStep_SaveKey : R.string.AiFeatures_AccessVia_Login_NextStep));
        buttonTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        buttonTextView.setBackground(Theme.AdaptiveRipple.filledRectByKey(Theme.key_featuredStickers_addButton, 4));
        buttonTextView.setOnClickListener(view -> {
            if (isFromLocalTranslation) {
                AlertDialog progress = new AlertDialog(LaunchActivity.instance, AlertDialog.ALERT_TYPE_SPINNER);
                progress.setCanCancel(false);
                progress.show();
                UpdatesManager.INSTANCE.getLastExtensionUpdate(new UpdatesManager.ExtensionGetStateCallback() {
                    @Override
                    public void onExtensionResultReceived(UpdatesManager.ExtensionUpdateState state) {
                        dismiss();
                        progress.dismiss();
                        Browser.openUrl(LaunchActivity.instance, String.format(Locale.US, "https://t.me/%s/%d", state.channelUsername(), state.messageID()));
                    }

                    @Override
                    public void onFailed() {
                        dismiss();
                        progress.dismiss();
                        Browser.openUrl(LaunchActivity.instance, String.format(Locale.US, "https://t.me/%s/%d", OctoConfig.EXTENSION_CHANNEL_TAG, OctoConfig.EXTENSION_CHANNEL_ID));
                    }
                });

                return;
            }

            if (skipToApiKeyConfig || selectedPosition == CurrentStep.INSERT_API_KEY) {
                checkApiKey();
                return;
            }

            viewPager.setCurrentItem(++selectedPosition);
        });
        linearLayout.addView(buttonTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, 0, 16, 15, 16, 8));

        if (skipToApiKeyConfig) {
            textView = new TextView(context);
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            textView.setText(formatString(R.string.AiFeatures_AccessVia_Login_DisableS, SERVICE_NAME));
            textView.setTextColor(ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_dialogTextBlack), 150));
            textView.setOnClickListener(view -> {
                callback.onDisabled();
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

                    if (position == CurrentStep.INSERT_API_KEY) {
                        buttonTextView.setText(getString(R.string.AiFeatures_AccessVia_Login_NextStep_SaveKey));
                        AndroidUtilities.runOnUIThread(() -> {
                            editTextCursor.getEditText().requestFocus();
                            AndroidUtilities.showKeyboard(editTextCursor.getEditText());
                        }, 200);
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


    private boolean isCheckingApiKey = false;

    private void checkApiKey() {
        if (isCheckingApiKey) {
            return;
        }

        if (editTextCursor != null) {
            String value = editTextCursor.getEditText().getText().toString().replaceAll(" ", "").trim();
            if (value.length() > MIN_LENGTH && value.length() < MAX_LENGTH) {
                Runnable saveData = () -> {
                    OctoConfig.INSTANCE.googleCloudKeyTranslator.updateValue(value);
                    callback.onStateUpdated();
                    dismiss();
                };

                if (!OctoConfig.INSTANCE.googleCloudKeyTranslator.getValue().equals(value)) {
                    String oldApiKey = OctoConfig.INSTANCE.googleCloudKeyTranslator.getValue();
                    OctoConfig.INSTANCE.googleCloudKeyTranslator.updateValue(value);
                    isCheckingApiKey = true;
                    AndroidUtilities.hideKeyboard(editTextCursor);
                    final AlertDialog progressDialog = new AlertDialog(LaunchActivity.instance, AlertDialog.ALERT_TYPE_SPINNER);
                    progressDialog.show();
                    NewGoogleTranslator.INSTANCE.executeTranslation("Ciao", null, "en", new SingleTranslationsHandler.OnTranslationResultCallback() {
                        @Override
                        public void onResponseReceived() {
                            OctoConfig.INSTANCE.googleCloudKeyTranslator.updateValue(oldApiKey);
                        }

                        @Override
                        public void onExtensionNeedUpdate() {
                            onFailedState(false);
                        }

                        @Override
                        public void onSuccess(TLRPC.TL_textWithEntities finalText) {
                            AndroidUtilities.runOnUIThread(() -> {
                                progressDialog.dismiss();
                                saveData.run();
                            });
                        }

                        @Override
                        public void onError() {
                            onFailedState(true);
                        }

                        @Override
                        public void onUnavailableLanguage() {
                            onFailedState(false);
                        }

                        private void onFailedState(boolean isServerError) {
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

    private String getTitleForStepId(int stepId) {
        if (isFromLocalTranslation) {
            return getString(R.string.TranslatorProvider_NeededExtension);
        }

        if (!skipToApiKeyConfig && stepId == CurrentStep.INITIAL_STAGE) {
            return getString(R.string.AiFeatures_AccessVia_Login_Step1);
        }

        return skipToApiKeyConfig ? formatString(R.string.AiFeatures_AccessVia_Login_Step2_Directly, "Google Cloud") : getString(R.string.AiFeatures_AccessVia_Login_Step2);
    }

    private String getDescriptionForStepId(int stepId) {
        if (isFromLocalTranslation) {
            return getString(R.string.TranslatorProvider_NeededExtension_Desc);
        }

        if (!skipToApiKeyConfig && stepId == CurrentStep.INITIAL_STAGE) {
            return formatString(R.string.AiFeatures_AccessVia_Login_Step1_Desc, SERVICE_NAME);
        }

        return skipToApiKeyConfig ? formatString(R.string.AiFeatures_AccessVia_Login_Step2_Directly_Desc, SERVICE_NAME) : getString(R.string.AiFeatures_AccessVia_Login_Step2_Desc);
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

    public interface TranslatorConfigInterface {
        default void onStateUpdated() {

        };
        default void onDisabled() {

        };
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
        textView2.setLinkTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkText));
        textView2.setHighlightColor(ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_featuredStickers_buttonText), 100));
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

    private class ViewPage extends LinearLayout {
        public ViewPage(Context context, int p) {
            super(context);
            setOrientation(VERTICAL);

            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.VERTICAL);

            StickerImageView imageView = new StickerImageView(context, UserConfig.selectedAccount);
            imageView.setStickerPackName(OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME);
            imageView.setStickerNum(isFromLocalTranslation ? StickerUi.COMMUNICATION.getValue() : (p == CurrentStep.INITIAL_STAGE && !skipToApiKeyConfig) ? StickerUi.TRANSLATOR.getValue() : StickerUi.DUCK_DEV.getValue());
            imageView.getImageReceiver().setAutoRepeat(1);
            linearLayout.addView(imageView, LayoutHelper.createLinear(144, 144, Gravity.CENTER_HORIZONTAL, 0, 16, 0, 16));

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

            if (p == CurrentStep.INITIAL_STAGE && !skipToApiKeyConfig) {
                linearLayout.addView(
                        makeHint(
                                isFromLocalTranslation ? R.drawable.msg_channel : R.drawable.msg_language,
                                getString(isFromLocalTranslation ? R.string.TranslatorProvider_NeededExtension_1 : R.string.TranslatorProvider_GoogleCloud_1),
                                getString(isFromLocalTranslation ? R.string.TranslatorProvider_NeededExtension_1_Desc : R.string.TranslatorProvider_GoogleCloud_1_Desc)
                        ),
                        LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.FILL_HORIZONTAL, 32, 0, 32, 16)
                );
                linearLayout.addView(
                        makeHint(
                                isFromLocalTranslation ? R.drawable.msg_download : R.drawable.edit_passcode,
                                getString(isFromLocalTranslation ? R.string.TranslatorProvider_NeededExtension_2 : R.string.TranslatorProvider_GoogleCloud_2),
                                getString(isFromLocalTranslation ? R.string.TranslatorProvider_NeededExtension_2_Desc : R.string.TranslatorProvider_GoogleCloud_2_Desc)
                        ),
                        LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.FILL_HORIZONTAL, 32, 0, 32, 16)
                );
                linearLayout.addView(
                        makeHint(
                                isFromLocalTranslation ? R.drawable.msg_translate : R.drawable.msg_copy,
                                getString(isFromLocalTranslation ? R.string.TranslatorProvider_NeededExtension_3 : R.string.TranslatorProvider_GoogleCloud_3),
                                getString(isFromLocalTranslation ? R.string.TranslatorProvider_NeededExtension_3_Desc : R.string.TranslatorProvider_GoogleCloud_3_Desc)
                        ),
                        LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.FILL_HORIZONTAL, 32, 0, 32, 16)
                );
            } else if (skipToApiKeyConfig || p == CurrentStep.INSERT_API_KEY) {
                OutlineEditText editText = new OutlineEditText(context);
                editText.setHint(getString(R.string.AiFeatures_AccessVia_Login_Step2_Hint));
                editText.getEditText().setText(OctoConfig.INSTANCE.googleCloudKeyTranslator.getValue());
                linearLayout.addView(editText, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 58, Gravity.LEFT | Gravity.TOP, 17, 0, 17, 0));
                editTextCursor = editText;

                InputFilter[] inputFilters = new InputFilter[1];
                inputFilters[0] = new CodepointsLengthInputFilter(MAX_LENGTH) {
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
            }

            addView(linearLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0, 0, 0));
        }
    }
}
