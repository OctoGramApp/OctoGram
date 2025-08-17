/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.chat;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.ChatActivityEnterView;
import org.telegram.ui.Components.EditTextEmoji;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.Components.TranslateAlert2;
import org.telegram.ui.LaunchActivity;

import it.octogram.android.OctoConfig;
import it.octogram.android.app.ui.OctoChatsTranslatorDestinationUI;
import it.octogram.android.utils.OctoUtils;
import it.octogram.android.utils.ai.CustomModelsMenuWrapper;
import it.octogram.android.utils.ai.MainAiHelper;
import it.octogram.android.utils.translator.MainTranslationsHandler;
import it.octogram.android.utils.translator.SingleTranslationsHandler;

@SuppressLint({"ClickableViewAccessibility", "ViewConstructor"})
public class SendMessageOptions extends LinearLayout {
    private final Theme.ResourcesProvider resourcesProvider;
    private final Object commentTextView;
    private final Object fragment;
    private ActionBarPopupWindow sendPopupWindow;
    private boolean returnSendersNames;
    private ActionBarMenuSubItem showCaptionView;
    private ActionBarMenuSubItem hideCaptionView;

    public SendMessageOptions(Context parentActivity, Object fragment, ForwardContext forwardContext, boolean showSchedule, boolean showNotify, Delegate delegate, Object commentTextView, Theme.ResourcesProvider resourcesProvider) {
        super(parentActivity);
        setOrientation(VERTICAL);

        this.resourcesProvider = resourcesProvider;
        this.commentTextView = commentTextView;
        this.fragment = fragment;
        var forwardParams = forwardContext.getForwardParams();
        var showTranslateButton = MessagesController.getInstance(UserConfig.selectedAccount).getTranslateController().isContextTranslateEnabled();

        if (isTextFieldEmpty()) {
            showTranslateButton = false;
        }

        if (forwardContext.getForwardingMessages() != null) {
            LinearLayout linearLayout2 = new LinearLayout(getContext());
            linearLayout2.setOrientation(VERTICAL);

            Paint paint = new Paint();
            paint.setColor(Theme.getColor(Theme.key_divider, resourcesProvider));
            View dividerView = new View(getContext()) {

                @Override
                protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                    super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(2, MeasureSpec.EXACTLY));
                }

                @Override
                protected void onDraw(Canvas canvas) {
                    canvas.drawRect(getPaddingLeft(), getPaddingTop(), getMeasuredWidth() - getPaddingRight(), getMeasuredHeight() - getPaddingBottom(), paint);
                }
            };
            var sendPopupLayout1 = getSendPopupLayout1(parentActivity, resourcesProvider, dividerView);

            ActionBarMenuSubItem showSendersNameView = new ActionBarMenuSubItem(getContext(), true, true, false, resourcesProvider);
            linearLayout2.addView(showSendersNameView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
            showSendersNameView.setTextAndIcon(getString(R.string.ShowSendersName), 0);
            showSendersNameView.setChecked(!forwardParams.noQuote);

            ActionBarMenuSubItem hideSendersNameView = new ActionBarMenuSubItem(getContext(), true, false, true, resourcesProvider);
            linearLayout2.addView(hideSendersNameView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
            hideSendersNameView.setTextAndIcon(getString(R.string.HideSendersName), 0);
            hideSendersNameView.setChecked(forwardParams.noQuote);
            showSendersNameView.setOnClickListener(e -> {
                if (forwardParams.noQuote) {
                    returnSendersNames = false;
                    showSendersNameView.setChecked(true);
                    hideSendersNameView.setChecked(false);
                    if (showCaptionView != null) {
                        showCaptionView.setChecked(true);
                        hideCaptionView.setChecked(false);
                    }
                    forwardParams.noQuote = false;
                    forwardParams.noCaption = false;
                }
            });
            hideSendersNameView.setOnClickListener(e -> {
                if (!forwardParams.noQuote) {
                    returnSendersNames = false;
                    showSendersNameView.setChecked(false);
                    hideSendersNameView.setChecked(true);
                    forwardParams.noQuote = true;
                }
            });

            boolean hasCaption = false;
            for (MessageObject message : forwardContext.getForwardingMessages()) {
                if (!TextUtils.isEmpty(message.caption)) {
                    hasCaption = true;
                    break;
                }
            }

            if (hasCaption) {
                linearLayout2.addView(dividerView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

                showCaptionView = new ActionBarMenuSubItem(getContext(), true, false, false, resourcesProvider);
                linearLayout2.addView(showCaptionView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
                showCaptionView.setTextAndIcon(getString(R.string.ShowCaption), 0);
                showCaptionView.setChecked(!forwardParams.noCaption);

                hideCaptionView = new ActionBarMenuSubItem(getContext(), true, false, true, resourcesProvider);
                linearLayout2.addView(hideCaptionView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
                hideCaptionView.setTextAndIcon(getString(R.string.HideCaption), 0);
                hideCaptionView.setChecked(forwardParams.noCaption);
                showCaptionView.setOnClickListener(e -> {
                    if (forwardParams.noCaption) {
                        if (returnSendersNames) {
                            forwardParams.noQuote = false;
                        }
                        returnSendersNames = false;
                        showCaptionView.setChecked(true);
                        hideCaptionView.setChecked(false);
                        showSendersNameView.setChecked(!forwardParams.noQuote);
                        hideSendersNameView.setChecked(forwardParams.noQuote);
                        forwardParams.noCaption = false;
                    }
                });
                hideCaptionView.setOnClickListener(e -> {
                    if (!forwardParams.noCaption) {
                        showCaptionView.setChecked(false);
                        hideCaptionView.setChecked(true);
                        showSendersNameView.setChecked(false);
                        hideSendersNameView.setChecked(true);
                        if (!forwardParams.noQuote) {
                            forwardParams.noQuote = true;
                            returnSendersNames = true;
                        }
                        forwardParams.noCaption = true;
                    }
                });
            }
            sendPopupLayout1.addView(linearLayout2, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            sendPopupLayout1.setupRadialSelectors(getThemedColor());
            addView(sendPopupLayout1, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, -8));
        }
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(VERTICAL);
        ActionBarPopupWindow.ActionBarPopupWindowLayout sendPopupLayout2 = getSendPopupLayout2(parentActivity, resourcesProvider);

        if (showSchedule) {
            ActionBarMenuSubItem scheduleButton = new ActionBarMenuSubItem(getContext(), true, !showNotify && !showTranslateButton, resourcesProvider);
            scheduleButton.setTextAndIcon(getString(R.string.ScheduleMessage), R.drawable.msg_calendar2);
            scheduleButton.setMinimumWidth(dp(196));
            scheduleButton.setOnClickListener(v -> {
                if (sendPopupWindow != null && sendPopupWindow.isShowing()) {
                    sendPopupWindow.dismiss();
                }
                AlertsCreator.createScheduleDatePickerDialog(parentActivity, 0, (notify, scheduleDate) -> {
                    forwardParams.notify = notify;
                    forwardParams.scheduleDate = scheduleDate;
                    delegate.sendMessage();
                }, resourcesProvider);
            });
            linearLayout.addView(scheduleButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
        }
        if (showNotify) {
            ActionBarMenuSubItem sendWithoutSoundButton = new ActionBarMenuSubItem(getContext(), !showSchedule, !showTranslateButton, resourcesProvider);
            sendWithoutSoundButton.setTextAndIcon(getString(R.string.SendWithoutSound), R.drawable.input_notify_off);
            sendWithoutSoundButton.setMinimumWidth(dp(196));
            sendWithoutSoundButton.setOnClickListener(v -> {
                if (sendPopupWindow != null && sendPopupWindow.isShowing()) {
                    sendPopupWindow.dismiss();
                }
                forwardParams.notify = false;
                delegate.sendMessage();
            });
            linearLayout.addView(sendWithoutSoundButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
        }

        if (showTranslateButton) {
            String destinationLanguage = OctoConfig.INSTANCE.lastTranslatePreSendLanguage.getValue() == null ? TranslateAlert2.getToLanguage() : OctoConfig.INSTANCE.lastTranslatePreSendLanguage.getValue();
            String translatedLanguageName = TranslateAlert2.languageName(destinationLanguage).toLowerCase();
            ActionBarMenuSubItem sendWithoutSoundButton = new ActionBarMenuSubItem(getContext(), !showSchedule && !showNotify, false, resourcesProvider);
            sendWithoutSoundButton.setTextAndIcon(formatString(R.string.TranslateToButton, translatedLanguageName), R.drawable.msg_translate);
            sendWithoutSoundButton.setMinimumWidth(dp(196));
            sendWithoutSoundButton.setOnClickListener(v -> executeMessageTranslation(destinationLanguage));
            if (!(fragment instanceof SizeNotifierFrameLayout)) {
                sendWithoutSoundButton.setOnLongClickListener(v -> {
                    executeTranslationToCustomDestination();
                    return true;
                });
            }
            linearLayout.addView(sendWithoutSoundButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
        }

        if (MainAiHelper.canUseAiFeatures()) {
            ActionBarMenuSubItem aiFeaturesButton = new ActionBarMenuSubItem(getContext(), !showSchedule && !showNotify && !showTranslateButton, false, resourcesProvider);
            aiFeaturesButton.setTextAndIcon(LocaleController.getString(R.string.AiFeatures_Brief), R.drawable.aifeatures_solar);
            aiFeaturesButton.setMinimumWidth(dp(196));
            linearLayout.addView(aiFeaturesButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));

            BaseFragment lastFragment = LaunchActivity.getLastFragment();
            if (lastFragment == null) {
                aiFeaturesButton.setVisibility(View.GONE);
            } else {
                CustomModelsMenuWrapper.FillStateData data = new CustomModelsMenuWrapper.FillStateData();
                data.context = getContext();
                data.onSheetOpen = () -> {
                    if (sendPopupWindow != null && sendPopupWindow.isShowing()) {
                        sendPopupWindow.dismiss();
                    }
                    if (commentTextView instanceof ChatActivityEnterView v2) {
                        AndroidUtilities.hideKeyboard(v2);
                    } else if (commentTextView instanceof EditTextEmoji v3) {
                        AndroidUtilities.hideKeyboard(v3);
                    }
                };
                data.originalSubItem = aiFeaturesButton;
                data.popupWindowLayout = sendPopupLayout2;
                data.messageText = getTextFieldContent();
                data.useSwipeBack = false;
                data.isInputBox = true;
                data.setInputBoxText = (v) -> {
                    AndroidUtilities.runOnUIThread(() -> {
                        if (commentTextView instanceof ChatActivityEnterView) {
                            ((ChatActivityEnterView) commentTextView).setFieldText(v);
                        } else if (commentTextView instanceof EditTextEmoji) {
                            ((EditTextEmoji) commentTextView).setText(v);
                        }
                    });
                };
                CustomModelsMenuWrapper.initState(data);
            }
        }

        ActionBarMenuSubItem sendMessage = new ActionBarMenuSubItem(getContext(), !showNotify || !showSchedule, true, resourcesProvider);
        sendMessage.setTextAndIcon(getString(R.string.SendMessage), R.drawable.msg_send);
        sendMessage.setMinimumWidth(dp(196));
        sendMessage.setOnClickListener(v -> {
            if (sendPopupWindow != null && sendPopupWindow.isShowing()) {
                sendPopupWindow.dismiss();
            }
            delegate.sendMessage();
        });
        linearLayout.addView(sendMessage, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
        sendPopupLayout2.addView(linearLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        sendPopupLayout2.setupRadialSelectors(getThemedColor());
        addView(sendPopupLayout2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    private ActionBarPopupWindow.ActionBarPopupWindowLayout getSendPopupLayout1(Context parentActivity, Theme.ResourcesProvider resourcesProvider, View dividerView) {
        ActionBarPopupWindow.ActionBarPopupWindowLayout sendPopupLayout1 = new ActionBarPopupWindow.ActionBarPopupWindowLayout(parentActivity, resourcesProvider) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                if (dividerView.getParent() != null) {
                    dividerView.setVisibility(View.GONE);
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                    dividerView.getLayoutParams().width = getMeasuredWidth();
                    dividerView.setVisibility(View.VISIBLE);
                }
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        };
        sendPopupLayout1.setAnimationEnabled(false);
        sendPopupLayout1.setOnTouchListener(new OnTouchListener() {
            private final android.graphics.Rect popupRect = new android.graphics.Rect();

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    if (sendPopupWindow != null && sendPopupWindow.isShowing()) {
                        v.getHitRect(popupRect);
                        if (!popupRect.contains((int) event.getX(), (int) event.getY())) {
                            sendPopupWindow.dismiss();
                        }
                    }
                }
                return false;
            }
        });
        sendPopupLayout1.setDispatchKeyEventListener(keyEvent -> {
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK && keyEvent.getRepeatCount() == 0 && sendPopupWindow != null && sendPopupWindow.isShowing()) {
                sendPopupWindow.dismiss();
            }
        });
        sendPopupLayout1.setShownFromBottom(false);
        return sendPopupLayout1;
    }

    private ActionBarPopupWindow.ActionBarPopupWindowLayout getSendPopupLayout2(Context parentActivity, Theme.ResourcesProvider resourcesProvider) {
        ActionBarPopupWindow.ActionBarPopupWindowLayout sendPopupLayout2 = new ActionBarPopupWindow.ActionBarPopupWindowLayout(parentActivity, resourcesProvider);
        sendPopupLayout2.setAnimationEnabled(false);
        sendPopupLayout2.setOnTouchListener(new OnTouchListener() {

            private final Rect popupRect = new android.graphics.Rect();

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    if (sendPopupWindow != null && sendPopupWindow.isShowing()) {
                        v.getHitRect(popupRect);
                        if (!popupRect.contains((int) event.getX(), (int) event.getY())) {
                            sendPopupWindow.dismiss();
                        }
                    }
                }
                return false;
            }
        });
        sendPopupLayout2.setDispatchKeyEventListener(keyEvent -> {
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK && keyEvent.getRepeatCount() == 0 && sendPopupWindow != null && sendPopupWindow.isShowing()) {
                sendPopupWindow.dismiss();
            }
        });
        sendPopupLayout2.setShownFromBottom(false);
        return sendPopupLayout2;
    }

    public void setSendPopupWindow(ActionBarPopupWindow sendPopupWindow) {
        this.sendPopupWindow = sendPopupWindow;
    }

    private int getThemedColor() {
        Integer color = resourcesProvider != null ? resourcesProvider.getColor(Theme.key_dialogButtonSelector) : null;
        return color != null ? color : Theme.getColor(Theme.key_dialogButtonSelector);
    }

    private boolean isTextFieldEmpty() {
        return commentTextView != null && TextUtils.isEmpty(getTextFieldContent());
    }

    private String getTextFieldContent() {
        if (commentTextView == null) {
            return "";
        }
        CharSequence text = null;
        if (commentTextView instanceof ChatActivityEnterView chatActivityEnterView) {
            text = chatActivityEnterView.getFieldText();
        } else if (commentTextView instanceof EditTextEmoji editTextEmoji) {
            text = editTextEmoji.getText();
        }
        return OctoUtils.safeToString(text).trim();
    }

    private void executeTranslationToCustomDestination() {
        if (isTextFieldEmpty()) {
            return;
        }

        if (sendPopupWindow != null && sendPopupWindow.isShowing()) {
            sendPopupWindow.dismiss();
        }

        OctoChatsTranslatorDestinationUI destinationSettings = new OctoChatsTranslatorDestinationUI();
        destinationSettings.setCallback(this::executeMessageTranslation);

        AndroidUtilities.runOnUIThread(() -> {
            BaseFragment lastFragment = LaunchActivity.getLastFragment();
            if (lastFragment != null) {
                lastFragment.presentFragment(destinationSettings);
            }
        }, 500);
    }

    private void executeMessageTranslation(String toLanguage) {
        if (isTextFieldEmpty()) {
            return;
        }

        OctoConfig.INSTANCE.lastTranslatePreSendLanguage.updateValue(toLanguage);
        String realDestination = toLanguage == null ? TranslateAlert2.getToLanguage() : toLanguage;

        final AlertDialog progressDialog = new AlertDialog(getContext(), AlertDialog.ALERT_TYPE_SPINNER);
        progressDialog.showDelayed(500);

        BulletinFactory factory = null;
        if (fragment instanceof BaseFragment) {
            factory = BulletinFactory.of((BaseFragment) fragment);
        } else if (fragment instanceof SizeNotifierFrameLayout) {
            factory = BulletinFactory.of((SizeNotifierFrameLayout) fragment, null);
        }

        BulletinFactory finalFactory = factory;
        MainTranslationsHandler.translate(UserConfig.selectedAccount, realDestination, getTextFieldContent(), new SingleTranslationsHandler.OnTranslationResultCallback() {
            @Override
            public void onResponseReceived() {
                AndroidUtilities.runOnUIThread(() -> {
                    progressDialog.dismiss();

                    if (sendPopupWindow != null && sendPopupWindow.isShowing()) {
                        sendPopupWindow.dismiss();
                    }
                });
            }

            @Override
            public void onSuccess(TLRPC.TL_textWithEntities finalText) {
                AndroidUtilities.runOnUIThread(() -> {
                    if (commentTextView instanceof ChatActivityEnterView) {
                        ((ChatActivityEnterView) commentTextView).setFieldText(finalText.text);
                    } else if (commentTextView instanceof EditTextEmoji) {
                        ((EditTextEmoji) commentTextView).setText(finalText.text);
                    }
                });
            }

            @Override
            public void onError() {
                if (finalFactory != null) {
                    finalFactory.createSimpleBulletin(R.raw.info, getString(R.string.TranslatorFailed)).show();
                }
            }

            @Override
            public void onUnavailableLanguage() {
                if (finalFactory != null) {
                    finalFactory.createSimpleBulletin(R.raw.info, getString(R.string.TranslatorUnsupportedLanguage)).show();
                }
            }

            @Override
            public void onExtensionError() {
                if (finalFactory != null) {
                    finalFactory.createSimpleBulletin(R.raw.info, getString(R.string.TranslatorExtensionFailed)).show();
                }
            }
        });
    }

    public interface Delegate {
        void sendMessage();
    }
}