/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.ai.ui;

import static android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO;
import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.dpf2;
import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;
import static org.telegram.ui.Components.LayoutHelper.createLinear;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.os.Vibrator;
import android.text.InputType;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ReplacementSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.math.MathUtils;
import androidx.core.util.Supplier;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.TranslateController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.XiaomiUtilities;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimatedFloat;
import org.telegram.ui.Components.AnimatedTextView;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.LinkPath;
import org.telegram.ui.Components.LinkSpanDrawable;
import org.telegram.ui.Components.LoadingDrawable;
import org.telegram.ui.Components.OutlineEditText;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.TranslateAlert2;
import org.telegram.ui.LaunchActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import it.octogram.android.AiModelMessagesState;
import it.octogram.android.AiModelType;
import it.octogram.android.AiProvidersDetails;
import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.app.fragment.PreferencesFragment;
import it.octogram.android.app.ui.OctoChatsAiFeaturesUI;
import it.octogram.android.app.ui.OctoChatsAiNewModelUI;
import it.octogram.android.utils.OctoUtils;
import it.octogram.android.utils.ai.AiPrompt;
import it.octogram.android.utils.ai.AiUtils;
import it.octogram.android.utils.ai.CustomModelsHelper;
import it.octogram.android.utils.ai.CustomModelsMenuWrapper;
import it.octogram.android.utils.ai.MainAiHelper;
import it.octogram.android.utils.ai.icons.AiFeatureIcons;

/**
 * @noinspection SequencedCollectionMethodCanBeUsed
 */
@SuppressLint("ClickableViewAccessibility")
public class MainAiBottomSheet extends BottomSheet {
    private AiProvidersDetails favoriteAiProvider;

    private final CustomModelsMenuWrapper.FillStateData data;
    private boolean isProcessingData = false;

    private String toLanguage;

    private final HeaderView headerView;
    private final TranslateAlert2.LoadingTextView loadingTextView;
    private final FrameLayout textViewContainer;
    private final LinkSpanDrawable.LinksTextView textView;

    private boolean sheetTopNotAnimate;
    private final RecyclerListView listView;
    private final TranslateAlert2.PaddedAdapter adapter;

    private final View buttonShadowView;
    private final TextView buttonTextView;
    private final ImageView regenerateButton;
    private final ImageView configButton;

    private OutlineEditText customPromptInputText;

    /**
     * @noinspection SequencedCollectionMethodCanBeUsed
     */
    private record EligibleVariations(int title, int icon, String hashtag,
                                      ConfigProperty<Integer> lastValue,
                                      List<String> eligibleVariations) {
        public String getCurrentVariation() {
            int currentSavedValue = lastValue.getValue();
            if (currentSavedValue > 0 && currentSavedValue < eligibleVariations.size()) {
                return eligibleVariations.get(currentSavedValue);
            }

            return eligibleVariations.get(0); // getFirst may cause strange things
        }

        public void updateVariation(int index) {
            if (index < 0 || index >= eligibleVariations.size()) {
                return;
            }

            lastValue.updateValue(index);
        }
    }

    private final EligibleVariations formalityRecord = new EligibleVariations(R.string.AiFeatures_CustomModels_Feature_Formality, R.drawable.msg_work, "#formality", OctoConfig.INSTANCE.aiFeaturesLastUsedFormality, List.of(
            getString(R.string.AiFeatures_CustomModels_Feature_State_Default),
            getString(R.string.AiFeatures_CustomModels_Feature_State_Informal),
            getString(R.string.AiFeatures_CustomModels_Feature_State_Professional)
    ));
    private final EligibleVariations lengthRecord = new EligibleVariations(R.string.AiFeatures_CustomModels_Feature_Length, R.drawable.text_field_focus, "#length", OctoConfig.INSTANCE.aiFeaturesLastUsedLength, List.of(
            getString(R.string.AiFeatures_CustomModels_Feature_State_Default),
            getString(R.string.AiFeatures_CustomModels_Feature_State_Short),
            getString(R.string.AiFeatures_CustomModels_Feature_State_Medium),
            getString(R.string.AiFeatures_CustomModels_Feature_State_Long)
    ));


    public MainAiBottomSheet(CustomModelsMenuWrapper.FillStateData data) {
        super(data.context, true, null);

        toLanguage = TranslateAlert2.getToLanguage();
        if (!OctoConfig.INSTANCE.aiFeaturesLastUsedLanguage.getValue().isEmpty()) {
            String temp = TranslateAlert2.languageName(OctoConfig.INSTANCE.aiFeaturesLastUsedLanguage.getValue());
            if (temp != null && !temp.isEmpty()) {
                toLanguage = OctoConfig.INSTANCE.aiFeaturesLastUsedLanguage.getValue();
            }
        }

        backgroundPaddingLeft = 0;
        favoriteAiProvider = MainAiHelper.getPreferredProvider();
        this.data = data;

        fixNavigationBar();

        containerView = new ContainerView(data.context);
        sheetTopAnimated = new AnimatedFloat(containerView, 320, CubicBezierInterpolator.EASE_OUT_QUINT);

        loadingTextView = new TranslateAlert2.LoadingTextView(data.context);
        loadingTextView.setPadding(dp(22), dp(12), dp(22), dp(6));
        loadingTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, SharedConfig.fontSize);
        loadingTextView.setTextColor(getThemedColor(Theme.key_dialogTextBlack));
        loadingTextView.setLinkTextColor(Theme.multAlpha(getThemedColor(Theme.key_dialogTextBlack), .2f));

        textViewContainer = new FrameLayout(data.context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), heightMeasureSpec);
            }
        };
        textView = new LinkSpanDrawable.LinksTextView(data.context, resourcesProvider);
        textView.setDisablePaddingsOffsetY(true);
//        if (isCustomModel() && ((MessagesModelsWrapper.FillStateData) data).isInputBox) {
//            textView.setOnCreateContextMenuListener((menu, v, menuInfo) -> menu.add(R.string.AiFeatures_CustomModels_Feature_UseAsText).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//                @Override
//                public boolean onMenuItemClick(@NonNull MenuItem item) {
//                    int start = Selection.getSelectionStart(textView.getText());
//                    int end = Selection.getSelectionEnd(textView.getText());
//
//                    if (start != end) {
//                        CharSequence selectedText = textView.getText().subSequence(start, end);
//                        ((MessagesModelsWrapper.FillStateData) data).setInputBoxText.run(selectedText.toString());
//                        dismiss();
//                        return true;
//                    }
//                    return false;
//                }
//            }));
//        }
        textView.setPadding(dp(22), dp(12), dp(22), dp(6));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, SharedConfig.fontSize);
        textView.setTextColor(getThemedColor(Theme.key_dialogTextBlack));
        textView.setLinkTextColor(getThemedColor(Theme.key_chat_messageLinkIn));
        textView.setTextIsSelectable(true);
        textView.setHighlightColor(getThemedColor(Theme.key_chat_inTextSelectionHighlight));
        int handleColor = getThemedColor(Theme.key_chat_TextSelectionCursor);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !XiaomiUtilities.isMIUI()) {
                Drawable left = textView.getTextSelectHandleLeft();
                if (left != null) {
                    left.setColorFilter(new PorterDuffColorFilter(handleColor, PorterDuff.Mode.SRC_IN));
                    textView.setTextSelectHandleLeft(left);
                }

                Drawable right = textView.getTextSelectHandleRight();
                if (right != null) {
                    right.setColorFilter(new PorterDuffColorFilter(handleColor, PorterDuff.Mode.SRC_IN));
                    textView.setTextSelectHandleRight(right);
                }
            }
        } catch (Exception ignored) {
        }
        textViewContainer.addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listView = new RecyclerListView(data.context) {
            @Override
            public boolean dispatchTouchEvent(MotionEvent ev) {
                if (ev.getAction() == MotionEvent.ACTION_DOWN && ev.getY() < getSheetTop() - getTop()) {
                    dismiss();
                    return true;
                }
                return super.dispatchTouchEvent(ev);
            }
        };
        listView.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        listView.setPadding(0, AndroidUtilities.statusBarHeight + dp(56), 0, dp(80));
        listView.setClipToPadding(true);
        listView.setLayoutManager(new LinearLayoutManager(data.context));
        listView.setAdapter(adapter = new TranslateAlert2.PaddedAdapter(data.context, loadingTextView));
        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                containerView.invalidate();
                updateButtonShadow(listView.canScrollVertically(1));
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    sheetTopNotAnimate = false;
                }
                if ((newState == RecyclerView.SCROLL_STATE_IDLE || newState == RecyclerView.SCROLL_STATE_SETTLING) && getSheetTop(false) > 0 && getSheetTop(false) < dp(64 + 32) && listView.canScrollVertically(1) && hasEnoughHeight()) {
                    sheetTopNotAnimate = true;
                    listView.smoothScrollBy(0, (int) getSheetTop(false));
                }
            }
        });
        DefaultItemAnimator itemAnimator = getDefaultItemAnimator();
        listView.setItemAnimator(itemAnimator);
        containerView.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM));

        headerView = new HeaderView(data.context);
        containerView.addView(headerView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 78, Gravity.TOP | Gravity.FILL_HORIZONTAL));

        FrameLayout buttonView = new FrameLayout(data.context);
        buttonView.setBackgroundColor(getThemedColor(Theme.key_dialogBackground));

        buttonShadowView = new View(data.context);
        buttonShadowView.setBackgroundColor(getThemedColor(Theme.key_dialogShadowLine));
        buttonShadowView.setAlpha(0);
        buttonView.addView(buttonShadowView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, AndroidUtilities.getShadowHeight() / dpf2(1), Gravity.TOP | Gravity.FILL_HORIZONTAL));

        buttonTextView = new TextView(data.context);
        buttonTextView.setLines(1);
        buttonTextView.setSingleLine(true);
        buttonTextView.setGravity(Gravity.CENTER_HORIZONTAL);
        buttonTextView.setEllipsize(TextUtils.TruncateAt.END);
        buttonTextView.setGravity(Gravity.CENTER);
        buttonTextView.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText));
        buttonTextView.setTypeface(AndroidUtilities.bold());
        buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        buttonTextView.setText(getString(isCustomModel() ? R.string.AiFeatures_CustomModels_Feature_CloseModal : R.string.CloseTranslation));
        buttonTextView.setBackground(Theme.AdaptiveRipple.filledRect(Theme.getColor(Theme.key_featuredStickers_addButton), 6));
        buttonTextView.setOnClickListener(e -> {
            if (customPromptInputText != null) {
                if (customPromptInputText.getEditText().getText().toString().trim().isEmpty()) {
                    shakeEditText();
                    return;
                }

                AndroidUtilities.hideKeyboard(customPromptInputText.getEditText());
                updateConfigButtonVisibility(true);
                buttonTextView.setText(getString(isCustomModel() ? R.string.AiFeatures_CustomModels_Feature_CloseModal : R.string.CloseTranslation));
                init();
            } else {
                dismiss();
            }
        });
        buttonView.addView(buttonTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 16, 16, 16 + 48 + 8 + 48 + 8, 16));

        regenerateButton = new ImageView(data.context);
        regenerateButton.setScaleType(ImageView.ScaleType.CENTER);
        regenerateButton.setImageResource(R.drawable.repeat_solar);
        regenerateButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_featuredStickers_buttonText), PorterDuff.Mode.MULTIPLY));
        regenerateButton.setBackground(Theme.AdaptiveRipple.filledRect(Theme.getColor(Theme.key_featuredStickers_addButton), 6));
        regenerateButton.setOnClickListener(v -> init());
        buttonView.addView(regenerateButton, LayoutHelper.createFrame(48, 48, Gravity.BOTTOM | Gravity.RIGHT, 0, 16, 16 + 48 + 8, 16));

        configButton = new ImageView(data.context);
        configButton.setScaleType(ImageView.ScaleType.CENTER);
        configButton.setImageResource(R.drawable.msg_download_settings);
        configButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_featuredStickers_buttonText), PorterDuff.Mode.MULTIPLY));
        configButton.setBackground(Theme.AdaptiveRipple.filledRect(Theme.getColor(Theme.key_featuredStickers_addButton), 6));
        configButton.setOnClickListener(v -> openConfig());
        buttonView.addView(configButton, LayoutHelper.createFrame(48, 48, Gravity.BOTTOM | Gravity.RIGHT, 0, 16, 16, 16));

        containerView.addView(buttonView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL));

        init();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (data.onSheetClose != null) {
            data.onSheetClose.run();
        }
    }

    @NonNull
    private DefaultItemAnimator getDefaultItemAnimator() {
        DefaultItemAnimator itemAnimator = new DefaultItemAnimator() {
            @Override
            protected void onChangeAnimationUpdate(RecyclerView.ViewHolder holder) {
                containerView.invalidate();
            }

            @Override
            protected void onMoveAnimationUpdate(RecyclerView.ViewHolder holder) {
                containerView.invalidate();
            }
        };
        itemAnimator.setDurations(180);
        itemAnimator.setInterpolator(new LinearInterpolator());
        return itemAnimator;
    }

    private void shakeEditText() {
        if (customPromptInputText != null) {
            AndroidUtilities.shakeView(customPromptInputText);
            Vibrator v = (Vibrator) LaunchActivity.instance.getSystemService(Context.VIBRATOR_SERVICE);
            if (v != null) {
                v.vibrate(200);
            }
        }
    }

    private CharSequence getOriginalText() {
        return data.messageText;
    }

    private boolean isCustomModel() {
        return !Objects.equals(data.modelID, CustomModelsHelper.VIRTUAL_TRANSLATE_MODEL_ID);
    }

    private boolean hasEnoughHeight() {
        float height = 0;
        for (int i = 0; i < listView.getChildCount(); ++i) {
            View child = listView.getChildAt(i);
            if (listView.getChildAdapterPosition(child) == 1)
                height += child.getHeight();
        }
        return height >= listView.getHeight() - listView.getPaddingTop() - listView.getPaddingBottom();
    }

    private boolean isFirstTime = true;

    private void updateConfigButtonVisibility(boolean isVisible) {
        List<ImageView> buttons = List.of(configButton, regenerateButton);

        if (isFirstTime) {
            isFirstTime = false;

            for (ImageView button : buttons) {
                button.setEnabled(isVisible);
                button.setClickable(isVisible);
                button.setAlpha(isVisible ? 1f : 0f);
                button.setTranslationX(isVisible ? 0 : dp(60));
            }

            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) buttonTextView.getLayoutParams();
            layoutParams.rightMargin = dp(16 + (isVisible ? (48 + 8 + 48 + 8) : 0));
            buttonTextView.setLayoutParams(layoutParams);
            buttonTextView.invalidate();
        } else {
            if (!isVisible) {
                for (ImageView button : buttons) {
                    button.setEnabled(false);
                    button.setClickable(false);
                }
            }

            ValueAnimator buttonAnimatorView = ValueAnimator.ofFloat(isVisible ? 0f : 1f, isVisible ? 1f : 0f);
            buttonAnimatorView.addUpdateListener(animation -> {
                float value = (float) animation.getAnimatedValue();
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) buttonTextView.getLayoutParams();
                layoutParams.rightMargin = dp((16 + (48 + 8 + 48 + 8) * value));
                buttonTextView.setLayoutParams(layoutParams);
                buttonTextView.invalidate();

                for (ImageView button : buttons) {
                    button.setAlpha(value);
                    button.setTranslationX(dp(60) * (1f - value));
                }
            });
            if (isVisible) {
                buttonAnimatorView.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        for (ImageView button : buttons) {
                            button.setEnabled(true);
                            button.setClickable(true);
                        }
                    }
                });
            }
            buttonAnimatorView.setDuration(100);
            buttonAnimatorView.start();
        }
    }

    private Runnable loadingTextCycleStopRunnable;

    private void initLoadingTextCycle() {
        ArrayList<Integer> usedStrings = new ArrayList<>();
        Random rand = new Random();

        boolean[] isFirstState = {true};

        Thread thread = new Thread() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    if (usedStrings.size() == 5) {
                        usedStrings.clear();
                    }

                    AndroidUtilities.runOnUIThread(() -> {
                        Runnable setTextData = () -> {
                            int randomNumber;
                            do {
                                randomNumber = rand.nextInt(5) + 1;
                            } while (usedStrings.contains(randomNumber));

                            int newString = getCurrentAiLoadingString(randomNumber);

                            usedStrings.add(randomNumber);
                            loadingTextView.setText(getString(newString));
                        };

                        if (isFirstState[0]) {
                            setTextData.run();
                            isFirstState[0] = false;
                        } else {
                            loadingTextView.animate().alpha(0f).scaleX(0.8f).scaleY(0.8f).setDuration(250).withEndAction(() -> {
                                setTextData.run();
                                loadingTextView.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(250).start();
                            }).start();
                        }
                    });

                    SystemClock.sleep(5 * 1000L);
                }
            }
        };
        thread.start();

        loadingTextCycleStopRunnable = thread::interrupt;
    }

    private int getCurrentAiLoadingString(int randomNumber) {
        if (isCustomModel() && (randomNumber == 2 || randomNumber == 4 || randomNumber == 5)) {
            CustomModelsHelper.CustomModel model = CustomModelsHelper.getModelById(data.modelID);
            if (model != null && model.prompt == null) {
                return R.string.AiFeatures_CustomModels_Feature_Loading_Easy;
            }
        }

        return getRandomString(randomNumber);
    }

    private static int getRandomString(int randomNumber) {
        return switch (randomNumber) {
            case 2 -> R.string.AiFeatures_CustomModels_Feature_Loading_2;
            case 3 -> R.string.AiFeatures_CustomModels_Feature_Loading_3;
            case 4 -> R.string.AiFeatures_CustomModels_Feature_Loading_4;
            case 5 -> R.string.AiFeatures_CustomModels_Feature_Loading_5;
            default -> R.string.AiFeatures_CustomModels_Feature_Loading_1;
        };
    }

    private void destroyLoadingTextCycle() {
        if (loadingTextCycleStopRunnable != null) {
            loadingTextCycleStopRunnable.run();
            loadingTextCycleStopRunnable = null;
        }
        loadingTextView.setScaleY(1f);
        loadingTextView.setScaleX(1f);
        loadingTextView.setAlpha(1f);
    }

    private void showLoading() {
        boolean useLoadingCycle = getOriginalText() == null || getOriginalText().toString().isEmpty();

        if (isCustomModel()) {
            if (data.modelID != null) {
                CustomModelsHelper.CustomModel model = CustomModelsHelper.getModelById(data.modelID);
                if (model != null && model.uploadMedia && CustomModelsMenuWrapper.getAvailableStates(data) != AiModelMessagesState.TEXT_MESSAGES) {
                    useLoadingCycle = true;
                }
            } else {
                useLoadingCycle = true;
            }
        }

        if (useLoadingCycle) {
            adapter.updateMainView(loadingTextView);
            initLoadingTextCycle();
        } else {
            loadingTextView.setText(Emoji.replaceEmoji(getOriginalText(), loadingTextView.getPaint().getFontMetricsInt(), true));
            adapter.updateMainView(loadingTextView);
        }
    }

    private void init() {
        if (isProcessingData) {
            return;
        }

        headerView.updateView();
        destroyLoadingTextCycle();

        AiPrompt prompt;
        if (isCustomModel()) {
            if (data.modelID == null) {
                selectEligibleModels();
                return;
            }

            CustomModelsHelper.CustomModel modelData = CustomModelsHelper.getModelById(data.modelID);
            if (modelData == null) {
                return;
            }

            if (modelData.prompt == null && customPromptInputText == null) {
                requestPromptToUser();
                return;
            }

            String promptAsString;
            if (modelData.prompt == null) {
                promptAsString = customPromptInputText.getEditText().getText().toString().trim();
                customPromptInputText = null;
            } else {
                promptAsString = modelData.prompt;
            }

            String filePath = "";
            String mimeType = "";
            boolean loadAsImage = false;
            AiModelMessagesState currentState = CustomModelsMenuWrapper.getAvailableStates(data);
            if (modelData.modelType == AiModelType.RELATED_TO_MESSAGES && modelData.uploadMedia && currentState != AiModelMessagesState.TEXT_MESSAGES) {
                if (favoriteAiProvider != AiProvidersDetails.GEMINI) {
                    AndroidUtilities.runOnUIThread(() -> {
                        textView.setTextColor(getThemedColor(Theme.key_dialogTextGray3));
                        textView.setText(getString(R.string.AiFeatures_CustomModels_Feature_Failed_Upload));
                        adapter.updateMainView(textViewContainer);
                    });
                    return;
                }

                File finalFile = OctoUtils.getFileContentFromMessage(data.messageObject);
                if (finalFile != null && finalFile.exists()) {
                    filePath = finalFile.getAbsolutePath();
                    mimeType = data.messageObject.getMimeType();
                    loadAsImage = currentState == AiModelMessagesState.STICKERS || currentState == AiModelMessagesState.PHOTOS;
                } else {
                    AndroidUtilities.runOnUIThread(() -> {
                        textView.setTextColor(getThemedColor(Theme.key_dialogTextGray3));
                        textView.setText(getString(R.string.PleaseDownload));
                        adapter.updateMainView(textViewContainer);
                    });
                    return;
                }
            }

            if (modelData.prompt != null) {
                promptAsString = replaceTags(promptAsString);
            }

            String mainSystemInstruction = "Return plain text only or use markdown with double ** or __ for italic like Telegram markdown styling.\nTelegram-supported markdown formatting:\n\nBasic formatting:\n- **bold text** (double asterisks) for emphasis\n- __italic text__ (double underscores) for nuanced meaning\n- `inline code` (single backtick) for technical terms\n- ```code blocks``` (triple backticks) for longer code\n- [text](URL) for links";
            prompt = new AiPrompt(mainSystemInstruction, promptAsString, filePath, mimeType, loadAsImage);
        } else {
            prompt = AiUtils.getTranslationPrompt(true, false, data.messageText, toLanguage);
        }

        isProcessingData = true;
        showLoading();
        MainAiHelper.request(prompt, favoriteAiProvider, new MainAiHelper.OnResultState() {
            @Override
            public void onSuccess(String result) {
                isProcessingData = false;
                destroyLoadingTextCycle();
                AndroidUtilities.runOnUIThread(() -> {
                    textView.setText(AndroidUtilities.replaceTags(Emoji.replaceEmoji(result, textView.getPaint().getFontMetricsInt(), true)));
                    textView.setTextColor(getThemedColor(Theme.key_dialogTextBlack));
                    adapter.updateMainView(textViewContainer);
                });
            }

            @Override
            public void onFailed() {
                onFailedState(isCustomModel() ? R.string.AiFeatures_CustomModels_Feature_Failed : R.string.TranslationFailedAlert2);
            }

            @Override
            public void onTooManyRequests() {
                onFailedState(R.string.FloodWait);
            }

            @Override
            public void onModelOverloaded() {
                onFailedState(R.string.AiFeatures_CustomModels_Feature_Failed_Overloaded);
            }

            private void onFailedState(int str) {
                isProcessingData = false;
                destroyLoadingTextCycle();
                AndroidUtilities.runOnUIThread(() -> {
                    textView.setTextColor(getThemedColor(Theme.key_dialogTextGray3));
                    textView.setText(getString(str));
                    adapter.updateMainView(textViewContainer);
                });
            }
        });
    }

    private String replaceTags(String promptAsString) {
        if (promptAsString == null) {
            return null;
        }

        HashMap<String, String> hashtagReplacementAssoc = new HashMap<>();
        if (data.isInputBox) {
            if (promptAsString.contains("#reply_to_text")) {
                hashtagReplacementAssoc.put("#reply_to_text", data.replyMessageObject != null && data.replyMessageObject.messageText != null ? data.replyMessageObject.messageText.toString() : "");
            }
            if (promptAsString.contains("#reply_to_author")) {
                if (data.replyMessageObject == null) {
                    hashtagReplacementAssoc.put("#reply_to_author", "");
                } else {
                    TLObject object = data.replyMessageObject.getFromPeerObject();
                    if (object instanceof TLRPC.Chat chat) {
                        hashtagReplacementAssoc.put("#reply_to_author", chat.title);
                    } else if (object instanceof TLRPC.User user) {
                        hashtagReplacementAssoc.put("#reply_to_author", UserObject.getUserName(user));
                    }
                }
            }
        } else if (data.isChat) {
            if (data.currentChat != null) {
                TLRPC.Chat chat = data.currentChat;
                hashtagReplacementAssoc.put("#chat_title", chat.title);
                hashtagReplacementAssoc.put("#chat_username", chat.username == null ? "" : "@" + chat.username);
                hashtagReplacementAssoc.put("#chat_members_count", "" + chat.participants_count);

                if (promptAsString.contains("#chat_description")) {
                    TLRPC.ChatFull chatFull = MessagesController.getInstance(UserConfig.selectedAccount).getChatFull(chat.id);
                    hashtagReplacementAssoc.put("#chat_description", chatFull.about);
                }
            } else {
                TLRPC.User user = data.currentUser;
                hashtagReplacementAssoc.put("#chat_title", user == null ? "" : UserObject.getUserName(user));
                hashtagReplacementAssoc.put("#chat_username", (user == null || UserObject.getPublicUsername(user) == null) ? "" : ("@" + UserObject.getPublicUsername(user)));
                hashtagReplacementAssoc.put("#chat_description", "");
                hashtagReplacementAssoc.put("#chat_members_count", "0");
            }
        } else {
            if (promptAsString.contains("#chat_title") || promptAsString.contains("#chat_description") || promptAsString.contains("#chat_members_count") || promptAsString.contains("#chat_username")) {
                MessagesController controllerInstance = MessagesController.getInstance(UserConfig.selectedAccount);
                long chatID = data.messageObject.getChatId();
                if (chatID != 0) {
                    if (promptAsString.contains("#chat_title") || promptAsString.contains("#chat_username")) {
                        TLRPC.Chat chat = controllerInstance.getChat(chatID);
                        hashtagReplacementAssoc.put("#chat_title", chat == null ? "" : chat.title);
                        hashtagReplacementAssoc.put("#chat_username", (chat == null || chat.username == null) ? "" : ("@" + chat.username));
                        hashtagReplacementAssoc.put("#chat_members_count", "" + (chat == null ? 0 : chat.participants_count));
                    }
                    if (promptAsString.contains("#chat_description")) {
                        TLRPC.ChatFull chat = controllerInstance.getChatFull(chatID);
                        hashtagReplacementAssoc.put("#chat_description", chat == null ? "" : chat.about);
                    }
                } else {
                    TLRPC.User user = controllerInstance.getUser(data.messageObject.messageOwner.peer_id.user_id);
                    hashtagReplacementAssoc.put("#chat_title", user == null ? "" : UserObject.getUserName(user));
                    hashtagReplacementAssoc.put("#chat_username", (user == null || UserObject.getPublicUsername(user) == null) ? "" : ("@" + UserObject.getPublicUsername(user)));
                    hashtagReplacementAssoc.put("#chat_description", "");
                    hashtagReplacementAssoc.put("#chat_members_count", "0");
                }
            }
        }
        if (promptAsString.contains("#message_text") && !data.isChat) {
            hashtagReplacementAssoc.put("#message_text", data.messageText == null ? "" : data.messageText.toString());
        }
        if (promptAsString.contains(formalityRecord.hashtag)) {
            hashtagReplacementAssoc.put(formalityRecord.hashtag, formalityRecord.getCurrentVariation());
        }
        if (promptAsString.contains(lengthRecord.hashtag)) {
            hashtagReplacementAssoc.put(lengthRecord.hashtag, lengthRecord.getCurrentVariation());
        }
        if (promptAsString.contains("#language")) {
            hashtagReplacementAssoc.put("#language", TranslateAlert2.capitalFirst(TranslateAlert2.languageName(toLanguage)));
        }

        if (!hashtagReplacementAssoc.isEmpty()) {
            int tokenIndex = new Random().nextInt(90000);
            HashMap<String, String> tempTokens = new HashMap<>();
            for (HashMap.Entry<String, String> entry : hashtagReplacementAssoc.entrySet()) {
                String tag = entry.getKey();
                String tempToken = "%%TAG" + (tokenIndex++) + "%%";
                promptAsString = promptAsString.replace(tag, tempToken);
                tempTokens.put(tempToken, entry.getValue());
            }

            for (HashMap.Entry<String, String> entry : tempTokens.entrySet()) {
                promptAsString = promptAsString.replace(entry.getKey(), entry.getValue());
            }
        }

        if (data.isChat) {
            String uid1 = OctoUtils.generateRandomString();
            StringBuilder tempPrompt = new StringBuilder(promptAsString);
            tempPrompt.append("\n\n")
                    .append("USE <<BEGIN_MSG_")
                    .append(uid1)
                    .append(">> AS MESSAGE TEXT BEGIN TAG AND <<END_MSG_")
                    .append(uid1)
                    .append(">> AS END TAG")
                    .append("\n\n");
            for (int i = 0; i < data.selectedMessages.size(); i++) {
                MessageObject object = data.selectedMessages.get(i);
                AiModelMessagesState state = CustomModelsMenuWrapper.getAvailableStates(object);
                if (state != null) {
                    TLObject authorObject = object.getFromPeerObject();
                    if (authorObject instanceof TLRPC.Chat chat) {
                        tempPrompt.append(chat.title);
                    } else if (authorObject instanceof TLRPC.User user) {
                        tempPrompt.append(UserObject.getUserName(user));
                    }
                    if (state == AiModelMessagesState.TEXT_MESSAGES) {
                        tempPrompt
                                .append(": <<BEGIN_MSG_").append(uid1).append(">>")
                                .append(object.messageText.toString())
                                .append(": <<END_MSG_").append(uid1).append(">>")
                                .append("\n");
                    } else {
                        tempPrompt.append(": [").append(state.getStateName().toUpperCase()).append("]\n");
                    }
                }
            }
            promptAsString = tempPrompt.toString();
        }

        return promptAsString;
    }

    private void selectEligibleModels() {
        if (!isCustomModel()) {
            return;
        }

        if (data.modelID != null) {
            return;
        }

        AiModelMessagesState state = CustomModelsMenuWrapper.getAvailableStates(data);
        if (state == null && !data.isChat && !data.isInputBox) {
            dismiss();
            return;
        }

        ArrayList<String> availableModels = CustomModelsMenuWrapper.getEligibleModels(data, state);
        if (availableModels.isEmpty()) {
            dismiss();
            return;
        }

        if (availableModels.size() == 1) {
            data.modelID = availableModels.get(0);
            init();
            return;
        }

        updateConfigButtonVisibility(false);

        LinearLayout linearLayout = new LinearLayout(data.context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), heightMeasureSpec);
            }
        };
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        if (data.canTranslate()) {
            FrameLayout layout = createSuggestionRow(R.drawable.msg_translate, getString(R.string.AiFeatures_Features_TranslateAI2));
            layout.setOnClickListener((v) -> {
                data.modelID = CustomModelsHelper.VIRTUAL_TRANSLATE_MODEL_ID;
                updateConfigButtonVisibility(true);
                init();
            });
            linearLayout.addView(layout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 50, 0f));
        }

        for (String modelID : availableModels) {
            CustomModelsHelper.CustomModel model = CustomModelsHelper.getModelById(modelID);
            if (model == null) {
                continue;
            }

            String title;
            if (!model.title.trim().isEmpty()) {
                title = model.title.trim();
            } else {
                title = formatString(R.string.AiFeatures_CustomModels_ModelID, modelID);
            }

            FrameLayout layout = createSuggestionRow(AiFeatureIcons.getModelIcon(model.icon), title);
            layout.setOnClickListener((v) -> {
                data.modelID = modelID;
                updateConfigButtonVisibility(true);
                init();
            });
            linearLayout.addView(layout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 50, 0f));
        }

        adapter.updateMainView(linearLayout);
    }

    private void requestPromptToUser() {
        updateConfigButtonVisibility(false);

        LinearLayout linearLayout = new LinearLayout(data.context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), heightMeasureSpec);
            }
        };
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        customPromptInputText = new OutlineEditText(data.context);
        customPromptInputText.getEditText().setMinHeight(AndroidUtilities.dp(58));
        customPromptInputText.getEditText().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        customPromptInputText.getEditText().setMaxLines(7);
        customPromptInputText.getEditText().setPadding(
                AndroidUtilities.dp(15), AndroidUtilities.dp(15), AndroidUtilities.dp(15), AndroidUtilities.dp(15)
        );
        customPromptInputText.setHint(getString(R.string.AiFeatures_CustomModels_Feature_SelectModel_Desc_Brief));
        linearLayout.addView(customPromptInputText, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 17, 0, 17, 7));

        int key = CustomModelsMenuWrapper.getSuggestedAskOnMediaAction(data);

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(" d  ");
        spannableStringBuilder.append(getString(key));
        SuggestedSpan span = new SuggestedSpan(10);
        span.setColor(Theme.getColor(Theme.key_premiumGradient1));
        spannableStringBuilder.setSpan(span, 1, 2, 0);

        FrameLayout layout = createSuggestionRow(0, spannableStringBuilder);
        layout.setOnClickListener((v) -> {
            customPromptInputText.getEditText().setText(getString(key));
            buttonTextView.callOnClick();
        });
        linearLayout.addView(layout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 50, 0f));

        buttonTextView.setText(getString(R.string.AiFeatures_CustomModels_Feature_SelectModel_Desc_Done_Brief));

        adapter.updateMainView(linearLayout);
    }

    private FrameLayout createSuggestionRow(int icon, CharSequence title) {
        ImageView imageView = new ImageView(data.context);
        imageView.setVisibility(icon == 0 ? View.GONE : View.VISIBLE);
        imageView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayIcon), PorterDuff.Mode.MULTIPLY));
        imageView.setImageResource(icon);

        AppCompatTextView textView = new AppCompatTextView(data.context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST) {
                    widthMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec) - dp(52), MeasureSpec.AT_MOST);
                }
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        };
        textView.setLines(1);
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        textView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        textView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        textView.setText(title);

        ImageView arrowView = new ImageView(data.context);
        arrowView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText), PorterDuff.Mode.MULTIPLY));
        arrowView.setImageResource(R.drawable.msg_arrow_forward);

        LinearLayout textViewLayout = new LinearLayout(data.context);
        textViewLayout.setOrientation(LinearLayout.HORIZONTAL);
        textViewLayout.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);

        boolean isRTL = LocaleController.isRTL;
        textViewLayout.addView(textView, createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL));
        textViewLayout.addView(arrowView, createLinear(16, 16, 0, Gravity.CENTER_VERTICAL, isRTL ? 0 : 2, 0, 0, 0));

        FrameLayout layout = new FrameLayout(data.context);
        layout.setBackground(Theme.AdaptiveRipple.filledRect(Theme.getColor(Theme.key_dialogBackground)));
        layout.addView(imageView, LayoutHelper.createFrame(24, 24, Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT), 20, 0, 20, 0));
        layout.addView(textViewLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 50, Gravity.CENTER_VERTICAL | (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT), icon == 0 ? (73 - 24 - 20 - 5) : 73, 0, 0, 0));

        return layout;
    }

    @Override
    protected boolean canDismissWithSwipe() {
        return false;
    }

    private final AnimatedFloat sheetTopAnimated;

    private float getSheetTop() {
        return getSheetTop(true);
    }

    private float getSheetTop(boolean animated) {
        float top = listView.getTop();
        if (listView.getChildCount() >= 1) {
            top += Math.max(0, listView.getChildAt(listView.getChildCount() - 1).getTop());
        }
        top = Math.max(0, top - dp(78));
        if (animated && sheetTopAnimated != null) {
            if (!listView.scrollingByUser && !sheetTopNotAnimate) {
                top = sheetTopAnimated.set(top);
            } else {
                sheetTopAnimated.set(top, true);
            }
        }
        return top;
    }

    private void openConfig() {
        openConfig(false, false);
    }

    private void openConfig(boolean openLanguagesForeground, boolean openProviderForeground) {
        if (configButton == null) {
            return;
        }

        if (isCustomModel() && data.modelID == null) {
            return;
        }

        String customPrompt = "";
        boolean isVirtualModel;
        if (isCustomModel()) {
            CustomModelsHelper.CustomModel model = CustomModelsHelper.getModelById(data.modelID);
            if (model != null) {
                customPrompt = model.prompt;
                isVirtualModel = model.isVirtualModel();
            } else {
                isVirtualModel = false;
            }
        } else {
            isVirtualModel = false;
        }

        ActionBarPopupWindow.ActionBarPopupWindowLayout layout = new ActionBarPopupWindow.ActionBarPopupWindowLayout(getContext(), R.drawable.popup_fixed_alert2, resourcesProvider, ActionBarPopupWindow.ActionBarPopupWindowLayout.FLAG_USE_SWIPEBACK | ActionBarPopupWindow.ActionBarPopupWindowLayout.FLAG_SHOWN_FROM_BOTTOM) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(
                        MeasureSpec.makeMeasureSpec(dp(200), MeasureSpec.AT_MOST),
                        heightMeasureSpec
                );
            }
        };
        layout.swipeBackGravityRight = true;

        final Runnable[] dismiss = new Runnable[1];

        ActionBarPopupWindow.ActionBarPopupWindowLayout selectProvider = getSelectAIProviderLayout(() -> dismiss[0], layout);
        ActionBarPopupWindow.ActionBarPopupWindowLayout selectLanguageLayout = getLanguageSelectLayout(() -> dismiss[0], layout);

        int selectProviderSwipeBackIndex = layout.addViewToSwipeBack(selectProvider);
        int selectLanguageSwipeBackIndex = layout.addViewToSwipeBack(selectLanguageLayout);

        ActionBarMenuSubItem item = ActionBarMenuItem.addItem(layout, R.drawable.msg_payment_provider, getString(R.string.TranslatorProvider), false, null);
        item.setRightIcon(R.drawable.msg_arrowright);
        item.openSwipeBackLayout = () -> {
            if (layout.getSwipeBack() != null) {
                layout.getSwipeBack().openForeground(selectProviderSwipeBackIndex);
            }
        };
        ActionBarMenuSubItem finalItem2 = item;
        item.setOnClickListener(view -> finalItem2.openSwipeBack());

        if (!isCustomModel() || (customPrompt != null && customPrompt.contains("#language"))) {
            item = ActionBarMenuItem.addItem(layout, R.drawable.msg_language, getString(isCustomModel() ? R.string.AiFeatures_CustomModels_Feature_Language : R.string.TranslateTo), false, resourcesProvider);
            item.setRightIcon(R.drawable.msg_arrowright);
            item.openSwipeBackLayout = () -> {
                if (layout.getSwipeBack() != null) {
                    layout.getSwipeBack().openForeground(selectLanguageSwipeBackIndex);
                }
            };
            ActionBarMenuSubItem finalItem = item;
            item.setOnClickListener(view -> finalItem.openSwipeBack());
        }

        if (isCustomModel() && (customPrompt != null && customPrompt.contains(formalityRecord.hashtag))) {
            addItemFromEligibleVariations(formalityRecord, layout, () -> dismiss[0]);
        }

        if (isCustomModel() && (customPrompt != null && customPrompt.contains(lengthRecord.hashtag))) {
            addItemFromEligibleVariations(lengthRecord, layout, () -> dismiss[0]);
        }

        if (!isProcessingData && (isCustomModel() || !data.noforwards)) {
            item = ActionBarMenuItem.addItem(layout, R.drawable.msg_copy, getString(R.string.Copy), false, resourcesProvider);
            item.setOnClickListener(view -> {
                dismiss[0].run();
                AndroidUtilities.addToClipboard(textView.getText());
                BulletinFactory.of((FrameLayout) containerView, resourcesProvider).createCopyBulletin(getString(R.string.TextCopied)).show();
            });
        }

        if (!isProcessingData && data.isInputBox && data.setInputBoxText != null) {
            item = ActionBarMenuItem.addItem(layout, R.drawable.msg_message, getString(R.string.AiFeatures_CustomModels_Feature_UseAsText), false, resourcesProvider);
            item.setOnClickListener(view -> {
                dismiss[0].run();
                MainAiBottomSheet.this.dismiss();
                data.setInputBoxText.run(textView.getText().toString());
            });
        }

        if (isCustomModel()) {
            if (!data.isInputBox && data.modelID != null) {
                FrameLayout gap = new FrameLayout(getContext());
                gap.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuSeparator));
                View gapShadow = new View(getContext());
                gapShadow.setBackground(Theme.getThemedDrawableByKey(getContext(), R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                gap.addView(gapShadow, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
                gap.setTag(R.id.fit_width_tag, 1);
                layout.addView(gap, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 8));

                if (isVirtualModel) {
                    item = ActionBarMenuItem.addItem(layout, R.drawable.msg_customize, getString(R.string.AiFeatures_Brief_Settings), false, resourcesProvider);
                } else {
                    item = ActionBarMenuItem.addItem(layout, R.drawable.msg_edit, getString(R.string.AiFeatures_CustomModels_Model_Edit), false, resourcesProvider);
                }
                item.setOnClickListener(view -> {
                    dismiss[0].run();
                    MainAiBottomSheet.this.dismiss();
                    if (data.onNewFragmentOpen != null) {
                        data.onNewFragmentOpen.run();
                    }
                    AndroidUtilities.runOnUIThread(() -> {
                        if (isVirtualModel) {
                            String focusKey = "";
                            if (data.modelID.equals(CustomModelsHelper.VIRTUAL_ASK_ON_MEDIA_MODEL_ID)) {
                                focusKey = OctoConfig.INSTANCE.aiFeaturesAskOnMedia.getKey();
                            } else if (data.modelID.equals(CustomModelsHelper.VIRTUAL_CHAT_CONTEXT_MODEL_ID)) {
                                focusKey = OctoConfig.INSTANCE.aiFeaturesChatContext.getKey();
                            }
                            LaunchActivity.instance.presentFragment(new PreferencesFragment(new OctoChatsAiFeaturesUI(), focusKey));
                        } else {
                            OctoChatsAiNewModelUI newModelUI = new OctoChatsAiNewModelUI();
                            newModelUI.setCurrentModelId(data.modelID);
                            LaunchActivity.instance.presentFragment(new PreferencesFragment(newModelUI));
                        }
                    }, 300);
                });
            }
        }

        ActionBarPopupWindow window = new ActionBarPopupWindow(layout, LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT);
        dismiss[0] = window::dismiss;
        window.setPauseNotifications(true);
        window.setDismissAnimationDuration(220);
        window.setOutsideTouchable(true);
        window.setClippingEnabled(true);
        window.setAnimationStyle(R.style.PopupContextAnimation);
        window.setFocusable(true);
        int[] location = new int[2];
        configButton.getLocationInWindow(location);
        layout.measure(View.MeasureSpec.makeMeasureSpec(AndroidUtilities.displaySize.x, View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(AndroidUtilities.displaySize.y, View.MeasureSpec.AT_MOST));
        int height = layout.getMeasuredHeight();
        int y = location[1] > AndroidUtilities.displaySize.y * .9f - height ? location[1] - height + dp(8) : location[1] + headerView.toLanguageTextView.getMeasuredHeight() - dp(8);
        window.showAtLocation(containerView, Gravity.TOP | Gravity.LEFT, location[0] - dp(8), y);

        if (openLanguagesForeground) {
            Objects.requireNonNull(layout.getSwipeBack()).openForeground(selectLanguageSwipeBackIndex);
        } else if (openProviderForeground) {
            Objects.requireNonNull(layout.getSwipeBack()).openForeground(selectProviderSwipeBackIndex);
        }
    }

    private void addItemFromEligibleVariations(EligibleVariations variations, ActionBarPopupWindow.ActionBarPopupWindowLayout layout, Supplier<Runnable> onDismiss) {
        ActionBarPopupWindow.ActionBarPopupWindowLayout selectEligibleVariation = getSelectEligibleVariation(variations, onDismiss, layout);
        int currentSwipeBackIndex = layout.addViewToSwipeBack(selectEligibleVariation);

        ActionBarMenuSubItem item = ActionBarMenuItem.addItem(layout, variations.icon, getString(variations.title), false, resourcesProvider);
        item.setRightIcon(R.drawable.msg_arrowright);
        item.openSwipeBackLayout = () -> {
            if (layout.getSwipeBack() != null) {
                layout.getSwipeBack().openForeground(currentSwipeBackIndex);
            }
        };
        item.setOnClickListener(view -> item.openSwipeBack());
    }

    private ActionBarPopupWindow.ActionBarPopupWindowLayout getSelectEligibleVariation(EligibleVariations variations, Supplier<Runnable> dismiss, ActionBarPopupWindow.ActionBarPopupWindowLayout originalLayout) {
        ActionBarPopupWindow.ActionBarPopupWindowLayout layout = new ActionBarPopupWindow.ActionBarPopupWindowLayout(getContext(), 0, null) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(
                        MeasureSpec.makeMeasureSpec(dp(200), MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(originalLayout != null ? dp(300 + 48) : Math.min((int) (AndroidUtilities.displaySize.y * .33f), MeasureSpec.getSize(heightMeasureSpec)), MeasureSpec.EXACTLY)
                );
            }
        };

        ActionBarMenuSubItem currentBackItem = ActionBarMenuItem.addItem(layout, R.drawable.msg_arrow_back, getString(R.string.Back), false, null);
        currentBackItem.setOnClickListener(z -> Objects.requireNonNull(originalLayout.getSwipeBack()).closeForeground());

        for (int i = 0; i < variations.eligibleVariations.size(); i++) {
            ActionBarMenuSubItem button = getItem(variations, dismiss, i);
            layout.addView(button);
        }

        return layout;
    }

    @NonNull
    private ActionBarMenuSubItem getItem(EligibleVariations variations, Supplier<Runnable> dismiss, int i) {
        String variation = variations.eligibleVariations.get(i);

        ActionBarMenuSubItem button = new ActionBarMenuSubItem(getContext(), 2, false, false, resourcesProvider) {
            @Override
            public boolean dispatchTouchEvent(MotionEvent ev) {
                return super.dispatchTouchEvent(ev);
            }
        };
        button.setText(variation);
        button.setChecked(variations.getCurrentVariation().equals(variation));
        button.setOnClickListener(e -> {
            if (dismiss != null) {
                dismiss.get().run();
            }

            if (variations.getCurrentVariation().equals(variation)) {
                return;
            }

            variations.updateVariation(i);
            init();
        });
        return button;
    }

    private ActionBarPopupWindow.ActionBarPopupWindowLayout getSelectAIProviderLayout(Supplier<Runnable> dismiss, ActionBarPopupWindow.ActionBarPopupWindowLayout originalLayout) {
        ActionBarPopupWindow.ActionBarPopupWindowLayout layout = new ActionBarPopupWindow.ActionBarPopupWindowLayout(getContext(), 0, null) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(
                        widthMeasureSpec,
                        MeasureSpec.makeMeasureSpec(dp(300 + 48), MeasureSpec.EXACTLY)
                );
            }
        };

        ActionBarMenuSubItem currentBackItem = ActionBarMenuItem.addItem(layout, R.drawable.msg_arrow_back, getString(R.string.Back), false, null);
        currentBackItem.setOnClickListener(z -> Objects.requireNonNull(originalLayout.getSwipeBack()).closeForeground());

        boolean hasDisabledProviders = false;
        for (AiProvidersDetails provider : AiProvidersDetails.getEntries()) {
            if (!MainAiHelper.isProviderAvailable(provider)) {
                hasDisabledProviders = true;
                continue;
            }

            ActionBarMenuSubItem button = getItem(dismiss, provider);
            layout.addView(button);
        }

        if (hasDisabledProviders) {
            FrameLayout gap = new FrameLayout(getContext());
            gap.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuSeparator));
            View gapShadow = new View(getContext());
            gapShadow.setBackground(Theme.getThemedDrawableByKey(getContext(), R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
            gap.addView(gapShadow, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
            gap.setTag(R.id.fit_width_tag, 1);
            layout.addView(gap, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 8));

            LinkSpanDrawable.LinksTextView textView = new LinkSpanDrawable.LinksTextView(getContext());
            textView.setTag(R.id.fit_width_tag, 1);
            textView.setPadding(AndroidUtilities.dp(13), 0, AndroidUtilities.dp(13), AndroidUtilities.dp(8));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            textView.setTextColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuItem));
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            textView.setLinkTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkText));
            textView.setText(AndroidUtilities.replaceSingleLink(getString(R.string.AiFeatures_Features_TranslateAI_MoreProviders), Theme.getColor(Theme.key_windowBackgroundWhiteBlueText), () -> {
                if (dismiss != null) {
                    dismiss.get().run();
                }
                MainAiBottomSheet.this.dismiss();
                if (data.onNewFragmentOpen != null) {
                    data.onNewFragmentOpen.run();
                }
                AndroidUtilities.runOnUIThread(() -> LaunchActivity.instance.presentFragment(new PreferencesFragment(new OctoChatsAiFeaturesUI())), 300);
            }));
            layout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 8, 0, 0));
        }

        return layout;
    }

    @NonNull
    private ActionBarMenuSubItem getItem(Supplier<Runnable> dismiss, AiProvidersDetails provider) {
        ActionBarMenuSubItem button = new ActionBarMenuSubItem(getContext(), 2, false, false, resourcesProvider);
        button.setText(provider.getTitle());
        button.setChecked(favoriteAiProvider == provider);
        button.setOnClickListener(e -> {
            if (dismiss != null) {
                dismiss.get().run();
            }

            if (favoriteAiProvider == provider) {
                return;
            }

            favoriteAiProvider = provider;
            headerView.usedProviderTextView.setText(provider.getTitle());
            init();
        });
        return button;
    }

    private ActionBarPopupWindow.ActionBarPopupWindowLayout getLanguageSelectLayout(Supplier<Runnable> dismiss, ActionBarPopupWindow.ActionBarPopupWindowLayout originalLayout) {
        ActionBarPopupWindow.ActionBarPopupWindowLayout layout = new ActionBarPopupWindow.ActionBarPopupWindowLayout(getContext(), 0, null) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(
                        widthMeasureSpec,
                        MeasureSpec.makeMeasureSpec(originalLayout != null ? dp(300 + 48) : Math.min((int) (AndroidUtilities.displaySize.y * .33f), MeasureSpec.getSize(heightMeasureSpec)), MeasureSpec.EXACTLY)
                );
            }
        };
        layout.swipeBackGravityBottom = true;

        ScrollView swipeBackScrollView = new ScrollView(getContext()) {
            Drawable topShadowDrawable;
            final AnimatedFloat alphaFloat = new AnimatedFloat(this, 350, CubicBezierInterpolator.EASE_OUT_QUINT);
            private boolean wasCanScrollVertically;

            @Override
            public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
                super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
                boolean canScrollVertically = canScrollVertically(-1);
                if (wasCanScrollVertically != canScrollVertically) {
                    invalidate();
                    wasCanScrollVertically = canScrollVertically;
                }
            }

            @Override
            protected void dispatchDraw(@NonNull Canvas canvas) {
                super.dispatchDraw(canvas);

                float alpha = .5f * alphaFloat.set(canScrollVertically(-1) ? 1 : 0);
                if (alpha > 0) {
                    if (topShadowDrawable == null) {
                        topShadowDrawable = ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.header_shadow, getContext().getTheme());
                    }

                    if (topShadowDrawable != null) {
                        topShadowDrawable.setBounds(0, getScrollY(), getWidth(), getScrollY() + topShadowDrawable.getIntrinsicHeight());
                        topShadowDrawable.setAlpha((int) (0xFF * alpha));
                        topShadowDrawable.draw(canvas);
                    }
                }
            }
        };
        LinearLayout swipeBackScroll = new LinearLayout(getContext());
        swipeBackScroll.setOrientation(LinearLayout.VERTICAL);
        swipeBackScrollView.addView(swipeBackScroll);

        LinearLayout swipeBack = new LinearLayout(getContext());
        swipeBack.setOrientation(LinearLayout.VERTICAL);

        ActionBarMenuSubItem currentBackItem = ActionBarMenuItem.addItem(swipeBack, R.drawable.msg_arrow_back, getString(R.string.Back), false, null);
        currentBackItem.setOnClickListener(z -> Objects.requireNonNull(originalLayout.getSwipeBack()).closeForeground());
        swipeBack.addView(swipeBackScrollView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 300));
        layout.addView(swipeBack);

        ArrayList<LocaleController.LocaleInfo> locales = TranslateController.getLocales();
        boolean first = true;
        for (int i = 0; i < locales.size(); ++i) {
            LocaleController.LocaleInfo localeInfo = locales.get(i);

            if (!"remote".equals(localeInfo.pathToFile)) {
                continue;
            }

            ActionBarMenuSubItem button = new ActionBarMenuSubItem(getContext(), 2, originalLayout == null && first, i == locales.size() - 1, resourcesProvider);
            button.setText(TranslateAlert2.capitalFirst(TranslateAlert2.languageName(localeInfo.pluralLangCode)));
            button.setChecked(TextUtils.equals(toLanguage, localeInfo.pluralLangCode));
            button.setOnClickListener(e -> {
                if (dismiss != null) {
                    dismiss.get().run();
                }

                if (TextUtils.equals(toLanguage, localeInfo.pluralLangCode)) {
                    return;
                }

                headerView.toLanguageTextView.setText(TranslateAlert2.capitalFirst(TranslateAlert2.languageName(toLanguage = localeInfo.pluralLangCode)));
                OctoConfig.INSTANCE.aiFeaturesLastUsedLanguage.updateValue(toLanguage);
                init();
            });
            if (originalLayout != null) {
                swipeBackScroll.addView(button);
            } else {
                layout.addView(button);
            }

            first = false;
        }
        return layout;
    }

    /**
     * @noinspection rawtypes, unchecked
     */
    private class HeaderView extends FrameLayout {

        private final ImageView backButton;
        private final AnimatedTextView titleTextView;
        private final LinearLayout subtitleView;
        private AnimatedTextView toLanguageTextView;
        private AnimatedTextView usedProviderTextView;

        private final View shadow;
        private final Context context;

        public HeaderView(Context context) {
            super(context);
            this.context = context;

            View backgroundView = new View(context);
            backgroundView.setBackgroundColor(getThemedColor(Theme.key_dialogBackground));
            addView(backgroundView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 44, Gravity.TOP | Gravity.FILL_HORIZONTAL, 0, 12, 0, 0));

            backButton = new ImageView(context);
            backButton.setScaleType(ImageView.ScaleType.CENTER);
            backButton.setImageResource(R.drawable.ic_ab_back);
            backButton.setColorFilter(new PorterDuffColorFilter(getThemedColor(Theme.key_dialogTextBlack), PorterDuff.Mode.MULTIPLY));
            backButton.setBackground(Theme.createSelectorDrawable(getThemedColor(Theme.key_listSelector)));
            backButton.setAlpha(0f);
            backButton.setOnClickListener(e -> dismiss());
            addView(backButton, LayoutHelper.createFrame(54, 54, Gravity.TOP, 1, 1, 1, 1));

            titleTextView = new AnimatedTextView(context) {
                @Override
                protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                    if (LocaleController.isRTL) {
                        titleTextView.setPivotX(getMeasuredWidth());
                    }
                }
            };
            titleTextView.setTextColor(getThemedColor(Theme.key_dialogTextBlack));
            titleTextView.setTextSize(dp(20));
            titleTextView.setTypeface(AndroidUtilities.bold());
            titleTextView.setPivotX(0);
            titleTextView.setPivotY(0);
            addView(titleTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 26, Gravity.TOP | Gravity.FILL_HORIZONTAL, 22, 20, 22, 0));

            subtitleView = new LinearLayout(context) {
                @Override
                protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                    if (LocaleController.isRTL) {
                        subtitleView.setPivotX(getMeasuredWidth());
                    }
                }
            };
            if (LocaleController.isRTL) {
                subtitleView.setGravity(Gravity.RIGHT);
            }
            subtitleView.setPivotX(0);
            subtitleView.setPivotY(0);

            addView(subtitleView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.FILL_HORIZONTAL, 22, 43, 22, 0));

            shadow = new View(context);
            shadow.setBackgroundColor(getThemedColor(Theme.key_dialogShadowLine));
            shadow.setAlpha(0);
            addView(shadow, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, AndroidUtilities.getShadowHeight() / dpf2(1), Gravity.TOP | Gravity.FILL_HORIZONTAL, 0, 56, 0, 0));

            updateView();
        }

        boolean isFirstDataUpdate = true;

        private void updateView() {
            updateTitle(!isFirstDataUpdate);
            if (isFirstDataUpdate) {
                updateSubViews(customPromptInputText);
            } else {
                OutlineEditText customPromptInputTextFinal = customPromptInputText;
                subtitleView.animate().alpha(0f).withEndAction(() -> {
                    updateSubViews(customPromptInputTextFinal);
                    subtitleView.animate().alpha(1f).setDuration(100).start();
                }).setDuration(100).start();
            }
            isFirstDataUpdate = false;
        }

        private void updateTitle(boolean animated) {
            String pageTitle = getString(R.string.AiFeatures_Features_TranslateAI2);
            if (isCustomModel()) { // using custom model
                String modelID = data.modelID;
                if (modelID == null) { // model selection page
                    pageTitle = getString(R.string.AiFeatures_CustomModels_Feature_SelectModel);
                } else { // model has been selected
                    CustomModelsHelper.CustomModel model = CustomModelsHelper.getModelById(modelID);
                    if (model != null) {
                        if (!model.title.trim().isEmpty()) { // model has a title
                            pageTitle = model.title.trim();
                        } else { // model doesn't have a title
                            pageTitle = formatString(R.string.AiFeatures_CustomModels_ModelID, data.modelID);
                        }
                    }
                }
            }

            titleTextView.setText(Emoji.replaceEmoji(pageTitle, titleTextView.getPaint().getFontMetricsInt(), false), animated);
        }

        private void updateSubViews(OutlineEditText originalCustomPromptInputText) {
            // use original state because post-animation result may change
            subtitleView.removeAllViews();

            String customPrompt = "";
            if (isCustomModel()) {
                CustomModelsHelper.CustomModel model = CustomModelsHelper.getModelById(data.modelID);

                String customDescriptionText = "";

                if (data.modelID != null && model != null) {
                    customPrompt = model.prompt;

                    if (model.prompt == null) {
                        if (originalCustomPromptInputText != null) { // AI replied
                            customDescriptionText = getString(R.string.AiFeatures_CustomModels_Feature_SelectModel_Desc_Done);
                        } else { // write your question
                            customDescriptionText = getString(R.string.AiFeatures_CustomModels_Feature_SelectModel_Desc);
                        }
                    }
                } else if (data.modelID == null) {
                    AiModelMessagesState state = CustomModelsMenuWrapper.getAvailableStates(data);
                    if (state == null && !data.isChat && !data.isInputBox) {
                        return;
                    }

                    // show the counter of available models
                    ArrayList<String> availableModels = CustomModelsMenuWrapper.getEligibleModels(data, state);
                    customDescriptionText = formatString(R.string.AiFeatures_CustomModels_Model_SelectAvailable, availableModels.size());
                }

                if (!customDescriptionText.isEmpty()) {
                    TextView modelsTextView = new TextView(context);
                    if (LocaleController.isRTL) {
                        modelsTextView.setGravity(Gravity.RIGHT);
                    }
                    modelsTextView.setTextColor(getThemedColor(Theme.key_player_actionBarSubtitle));
                    modelsTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                    modelsTextView.setText(customDescriptionText);
                    subtitleView.addView(modelsTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 0, 4, 0, 0));
                    return;
                }
            }

            toLanguageTextView = new AnimatedTextView(context) {
                private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                private final LinkSpanDrawable.LinkCollector links = new LinkSpanDrawable.LinkCollector();

                @Override
                protected void onDraw(Canvas canvas) {
                    if (LocaleController.isRTL) {
                        AndroidUtilities.rectTmp.set(getWidth() - width(), (getHeight() - dp(18)) / 2f, getWidth(), (getHeight() + dp(18)) / 2f);
                    } else {
                        AndroidUtilities.rectTmp.set(0, (getHeight() - dp(18)) / 2f, width(), (getHeight() + dp(18)) / 2f);
                    }
                    bgPaint.setColor(Theme.multAlpha(getThemedColor(Theme.key_player_actionBarSubtitle), .1175f));
                    canvas.drawRoundRect(AndroidUtilities.rectTmp, dp(4), dp(4), bgPaint);
                    if (links.draw(canvas)) {
                        invalidate();
                    }

                    super.onDraw(canvas);
                }

                @Override
                public boolean onTouchEvent(MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        LinkSpanDrawable link = new LinkSpanDrawable(null, resourcesProvider, event.getX(), event.getY());
                        link.setColor(Theme.multAlpha(getThemedColor(Theme.key_player_actionBarSubtitle), .1175f));
                        LinkPath path = link.obtainNewPath();
                        if (LocaleController.isRTL) {
                            AndroidUtilities.rectTmp.set(getWidth() - width(), (getHeight() - dp(18)) / 2f, getWidth(), (getHeight() + dp(18)) / 2f);
                        } else {
                            AndroidUtilities.rectTmp.set(0, (getHeight() - dp(18)) / 2f, width(), (getHeight() + dp(18)) / 2f);
                        }
                        path.addRect(AndroidUtilities.rectTmp, Path.Direction.CW);
                        links.addLink(link);
                        invalidate();
                        return true;
                    } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            performClick();
                        }
                        links.clear();
                        invalidate();
                    }
                    return super.onTouchEvent(event);
                }
            };
            if (LocaleController.isRTL) {
                toLanguageTextView.setGravity(Gravity.RIGHT);
            }
            toLanguageTextView.setAnimationProperties(.25f, 0, 350, CubicBezierInterpolator.EASE_OUT_QUINT);
            toLanguageTextView.setTextColor(getThemedColor(Theme.key_player_actionBarSubtitle));
            toLanguageTextView.setTextSize(dp(14));
            toLanguageTextView.setText(TranslateAlert2.capitalFirst(TranslateAlert2.languageName(toLanguage)));
            toLanguageTextView.setPadding(dp(4), dp(2), dp(4), dp(2));
            toLanguageTextView.setOnClickListener(e -> openConfig(true, false));

            usedProviderTextView = new AnimatedTextView(context) {
                private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                private final LinkSpanDrawable.LinkCollector links = new LinkSpanDrawable.LinkCollector();

                @Override
                protected void onDraw(Canvas canvas) {
                    if (LocaleController.isRTL) {
                        AndroidUtilities.rectTmp.set(getWidth() - width(), (getHeight() - dp(18)) / 2f, getWidth(), (getHeight() + dp(18)) / 2f);
                    } else {
                        AndroidUtilities.rectTmp.set(0, (getHeight() - dp(18)) / 2f, width(), (getHeight() + dp(18)) / 2f);
                    }
                    bgPaint.setColor(Theme.multAlpha(getThemedColor(Theme.key_player_actionBarSubtitle), .1175f));
                    canvas.drawRoundRect(AndroidUtilities.rectTmp, dp(4), dp(4), bgPaint);
                    if (links.draw(canvas)) {
                        invalidate();
                    }

                    super.onDraw(canvas);
                }

                @Override
                public boolean onTouchEvent(MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        LinkSpanDrawable link = new LinkSpanDrawable(null, resourcesProvider, event.getX(), event.getY());
                        link.setColor(Theme.multAlpha(getThemedColor(Theme.key_player_actionBarSubtitle), .1175f));
                        LinkPath path = link.obtainNewPath();
                        if (LocaleController.isRTL) {
                            AndroidUtilities.rectTmp.set(getWidth() - width(), (getHeight() - dp(18)) / 2f, getWidth(), (getHeight() + dp(18)) / 2f);
                        } else {
                            AndroidUtilities.rectTmp.set(0, (getHeight() - dp(18)) / 2f, width(), (getHeight() + dp(18)) / 2f);
                        }
                        path.addRect(AndroidUtilities.rectTmp, Path.Direction.CW);
                        links.addLink(link);
                        invalidate();
                        return true;
                    } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            performClick();
                        }
                        links.clear();
                        invalidate();
                    }
                    return super.onTouchEvent(event);
                }
            };
            if (LocaleController.isRTL) {
                usedProviderTextView.setGravity(Gravity.RIGHT);
            }
            usedProviderTextView.setAnimationProperties(.25f, 0, 350, CubicBezierInterpolator.EASE_OUT_QUINT);
            usedProviderTextView.setTextColor(getThemedColor(Theme.key_player_actionBarSubtitle));
            usedProviderTextView.setTextSize(dp(14));
            usedProviderTextView.setText(favoriteAiProvider.getTitle());
            usedProviderTextView.setPadding(dp(4), dp(2), dp(4), dp(2));
            usedProviderTextView.setOnClickListener(e -> openConfig(false, true));

            boolean hasToLanguage = false;
            if (!isCustomModel() || (customPrompt != null && customPrompt.contains("#language"))) {
                hasToLanguage = true;
                subtitleView.addView(toLanguageTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, 0, 0, 0, 0));
            }

            subtitleView.addView(usedProviderTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_VERTICAL, hasToLanguage ? 3 : 0, 0, 0, 0));
        }

        @Override
        public void setTranslationY(float translationY) {
            super.setTranslationY(translationY);

            float t = MathUtils.clamp((translationY - AndroidUtilities.statusBarHeight) / dp(64), 0, 1);
            if (!hasEnoughHeight()) {
                t = 1;
            }
            t = CubicBezierInterpolator.EASE_OUT.getInterpolation(t);

            titleTextView.setScaleX(AndroidUtilities.lerp(.85f, 1f, t));
            titleTextView.setScaleY(AndroidUtilities.lerp(.85f, 1f, t));
            titleTextView.setTranslationY(AndroidUtilities.lerp(dpf2(-12), 0, t));
            if (!LocaleController.isRTL) {
                titleTextView.setTranslationX(AndroidUtilities.lerp(dpf2(50), 0, t));
                subtitleView.setTranslationX(AndroidUtilities.lerp(dpf2(50), 0, t));
            }

            subtitleView.setTranslationY(AndroidUtilities.lerp(dpf2(-22), 0, t));

            backButton.setTranslationX(AndroidUtilities.lerp(0, dpf2(-25), t));
            backButton.setAlpha(1f - t);

            shadow.setTranslationY(AndroidUtilities.lerp(0, dpf2(22), t));
            shadow.setAlpha(1f - t);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(
                    MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(dp(78), MeasureSpec.EXACTLY)
            );
        }
    }

    public class ContainerView extends FrameLayout {
        public ContainerView(Context context) {
            super(context);

            bgPaint.setColor(getThemedColor(Theme.key_dialogBackground));
            Theme.applyDefaultShadow(bgPaint);
        }

        private final Path bgPath = new Path();
        private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        @Override
        protected void dispatchDraw(Canvas canvas) {

            float top = getSheetTop();
            final float R = AndroidUtilities.lerp(0, dp(12), MathUtils.clamp(top / dpf2(24), 0, 1));
            headerView.setTranslationY(Math.max(AndroidUtilities.statusBarHeight, top));
            updateLightStatusBar(top <= AndroidUtilities.statusBarHeight / 2f);

            bgPath.rewind();
            AndroidUtilities.rectTmp.set(0, top, getWidth(), getHeight() + R);
            bgPath.addRoundRect(AndroidUtilities.rectTmp, R, R, Path.Direction.CW);
            canvas.drawPath(bgPath, bgPaint);

            super.dispatchDraw(canvas);
        }

        private Boolean lightStatusBarFull;

        private void updateLightStatusBar(boolean full) {
            if (lightStatusBarFull == null || lightStatusBarFull != full) {
                lightStatusBarFull = full;
                AndroidUtilities.setLightStatusBar(getWindow(), AndroidUtilities.computePerceivedBrightness(
                        full ?
                                getThemedColor(Theme.key_dialogBackground) :
                                Theme.blendOver(
                                        getThemedColor(Theme.key_actionBarDefault),
                                        0x33000000
                                )
                ) > .721f);
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.EXACTLY));
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            Bulletin.addDelegate(this, new Bulletin.Delegate() {
                @Override
                public int getBottomOffset(int tag) {
                    return AndroidUtilities.dp(16 + 48 + 16);
                }
            });
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            Bulletin.removeDelegate(this);
        }
    }

    private Boolean buttonShadowShown;

    private void updateButtonShadow(boolean show) {
        if (buttonShadowShown == null || buttonShadowShown != show) {
            buttonShadowShown = show;
            buttonShadowView.animate().cancel();
            buttonShadowView.animate().alpha(show ? 1f : 0f).setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT).setDuration(320).start();
        }
    }


    public static class LoadingTextView extends AppCompatTextView {

        private final LinkPath path = new LinkPath(true);
        private final LoadingDrawable loadingDrawable = new LoadingDrawable();

        public LoadingTextView(Context context) {
            super(context);
            loadingDrawable.usePath(path);
            loadingDrawable.setSpeed(.65f);
            loadingDrawable.setRadiiDp(4);
            setBackground(loadingDrawable);
        }

        @Override
        public void setTextColor(int color) {
            super.setTextColor(Theme.multAlpha(color, .2f));
            loadingDrawable.setColors(
                    Theme.multAlpha(color, 0.03f),
                    Theme.multAlpha(color, 0.175f),
                    Theme.multAlpha(color, 0.2f),
                    Theme.multAlpha(color, 0.45f)
            );
        }

        private void updateDrawable() {
            if (path == null || loadingDrawable == null) {
                return;
            }

            path.rewind();
            if (getLayout() != null) {
                path.setCurrentLayout(getLayout(), 0, getPaddingLeft(), getPaddingTop());
                getLayout().getSelectionPath(0, getLayout().getText().length(), path);
            }
            loadingDrawable.updateBounds();
        }

        @Override
        public void setText(CharSequence text, BufferType type) {
            super.setText(text, type);
            updateDrawable();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            updateDrawable();
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            loadingDrawable.reset();
        }
    }

    public static class SuggestedSpan extends ReplacementSpan {

        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        StaticLayout layout;
        float width, height;

        private final boolean outline;
        private int color;

        public void setTypeface(Typeface typeface) {
            textPaint.setTypeface(typeface);
        }

        public SuggestedSpan(float textSize) {
            this.outline = false;
            textPaint.setTypeface(AndroidUtilities.bold());
            bgPaint.setStyle(Paint.Style.FILL);
            textPaint.setTextSize(dp(textSize));
        }

        public void setColor(int color) {
            this.color = color;
        }

        private CharSequence text = getString(R.string.AiFeatures_CustomModels_Feature_SelectModel_Desc_Ask_Example);

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
}
