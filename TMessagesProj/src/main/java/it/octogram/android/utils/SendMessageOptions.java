package it.octogram.android.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AlertsCreator;
import org.telegram.ui.Components.LayoutHelper;

@SuppressLint({"ClickableViewAccessibility", "ViewConstructor"})
public class SendMessageOptions extends LinearLayout {
    private final Theme.ResourcesProvider resourcesProvider;
    private ActionBarPopupWindow sendPopupWindow;
    private boolean returnSendersNames;
    private ActionBarMenuSubItem showCaptionView;
    private ActionBarMenuSubItem hideCaptionView;

    /* TODO: NEED REVISION
    });
        sendPopupLayout2.setDispatchKeyEventListener(keyEvent -> {
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK && keyEvent.getRepeatCount() == 0 && sendPopupWindow != null && sendPopupWindow.isShowing()) {
                sendPopupWindow.dismiss();
            }
        });
        sendPopupLayout2.setShownFromBottom(false);
        sendPopupLayout2.setupRadialSelectors(getThemedColor(Theme.key_dialogButtonSelector));

        ActionBarMenuSubItem sendWithoutSound = new ActionBarMenuSubItem(parentActivity, true, true, resourcesProvider);
        sendWithoutSound.setTextAndIcon(LocaleController.getString("SendWithoutSound", R.string.SendWithoutSound), R.drawable.input_notify_off);
        sendWithoutSound.setMinimumWidth(dp(196));
        sendPopupLayout2.addView(sendWithoutSound, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
        sendWithoutSound.setOnClickListener(v -> {
            if (sendPopupWindow != null && sendPopupWindow.isShowing()) {
                sendPopupWindow.dismiss();
            }
            this.notify = false;
     */

    public SendMessageOptions(Context parentActivity, ForwardContext forwardContext, boolean showSchedule, boolean showNotify, Delegate delegate, Theme.ResourcesProvider resourcesProvider) {
        super(parentActivity);
        setOrientation(VERTICAL);

        this.resourcesProvider = resourcesProvider;

        ForwardContext.ForwardParams forwardParams = forwardContext.getForwardParams();
        ForwardUtil(parentActivity, forwardContext, resourcesProvider, forwardParams);

        ActionBarPopupWindow.ActionBarPopupWindowLayout popupLayout2 = getActionBarPopupWindowLayout(parentActivity, resourcesProvider);

        if (showSchedule) {
            ActionBarMenuSubItem scheduleItem = new ActionBarMenuSubItem(getContext(), true, !showNotify, resourcesProvider);
            scheduleItem.setTextAndIcon(LocaleController.getString(R.string.ScheduleMessage), R.drawable.msg_calendar2);
            scheduleItem.setMinimumWidth(AndroidUtilities.dp(196));
            scheduleItem.setOnClickListener(v -> {
                if (sendPopupWindow != null && sendPopupWindow.isShowing()) {
                    sendPopupWindow.dismiss();
                }
                AlertsCreator.createScheduleDatePickerDialog(parentActivity, 0, (notify, scheduleDate) -> {
                    forwardParams.notify = notify;
                    forwardParams.scheduleDate = scheduleDate;
                    delegate.sendMessage();
                }, resourcesProvider);
            });
            popupLayout2.addView(scheduleItem, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
        }

        if (showNotify) {
            ActionBarMenuSubItem sendWithoutSoundButton = new ActionBarMenuSubItem(getContext(), !showSchedule, true, resourcesProvider);
            sendWithoutSoundButton.setTextAndIcon(LocaleController.getString(R.string.SendWithoutSound), R.drawable.input_notify_off);
            sendWithoutSoundButton.setMinimumWidth(AndroidUtilities.dp(196));
            sendWithoutSoundButton.setOnClickListener(v -> {
                if (sendPopupWindow != null && sendPopupWindow.isShowing()) {
                    sendPopupWindow.dismiss();
                }
                forwardParams.notify = false;
                delegate.sendMessage();
            });
            popupLayout2.addView(sendWithoutSoundButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
        }

        ActionBarMenuSubItem sendMessage = new ActionBarMenuSubItem(getContext(), true, true, resourcesProvider);
        sendMessage.setTextAndIcon(LocaleController.getString(R.string.SendMessage), R.drawable.msg_send);
        sendMessage.setMinimumWidth(AndroidUtilities.dp(196));
        popupLayout2.addView(sendMessage, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
        sendMessage.setOnClickListener(v -> {
            if (sendPopupWindow != null && sendPopupWindow.isShowing()) {
                sendPopupWindow.dismiss();
            }
            delegate.sendMessage();
        });
        popupLayout2.setupRadialSelectors(getThemedColor());
        addView(popupLayout2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    private void ForwardUtil(Context parentActivity, ForwardContext forwardContext, Theme.ResourcesProvider resourcesProvider, ForwardContext.ForwardParams forwardParams) {
        if (forwardContext.getForwardingMessages() != null) {
            Paint paint = new Paint();
            paint.setColor(Theme.getColor(Theme.key_divider));
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

            ActionBarPopupWindow.ActionBarPopupWindowLayout popupLayout1 = getActionBarPopupWindowLayout(parentActivity, resourcesProvider, dividerView);

            ActionBarMenuSubItem showSendersNameView = new ActionBarMenuSubItem(getContext(), true, true, false, resourcesProvider);
            popupLayout1.addView(showSendersNameView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
            showSendersNameView.setTextAndIcon(LocaleController.getString(R.string.ShowSendersName), 0);
            showSendersNameView.setChecked(!forwardParams.noQuote);

            ActionBarMenuSubItem hideSendersNameView = new ActionBarMenuSubItem(getContext(), true, false, true, resourcesProvider);
            popupLayout1.addView(hideSendersNameView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
            hideSendersNameView.setTextAndIcon(LocaleController.getString(R.string.HideSendersName), 0);
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
                popupLayout1.addView(dividerView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

                showCaptionView = new ActionBarMenuSubItem(getContext(), true, false, false, resourcesProvider);
                popupLayout1.addView(showCaptionView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
                showCaptionView.setTextAndIcon(LocaleController.getString(R.string.ShowCaption), 0);
                showCaptionView.setChecked(!forwardParams.noCaption);

                hideCaptionView = new ActionBarMenuSubItem(getContext(), true, false, true, resourcesProvider);
                popupLayout1.addView(hideCaptionView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));
                hideCaptionView.setTextAndIcon(LocaleController.getString(R.string.HideCaption), 0);
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
            popupLayout1.setupRadialSelectors(getThemedColor());
            addView(popupLayout1, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, -8));
        }
    }

    @NonNull
    private ActionBarPopupWindow.ActionBarPopupWindowLayout getActionBarPopupWindowLayout(Context parentActivity, Theme.ResourcesProvider resourcesProvider, View dividerView) {
        ActionBarPopupWindow.ActionBarPopupWindowLayout popupLayout1 = new ActionBarPopupWindow.ActionBarPopupWindowLayout(parentActivity, resourcesProvider) {
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
        popupLayout1.setAnimationEnabled(false);
        popupLayout1.setOnTouchListener(new OnTouchListener() {
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
        popupLayout1.setDispatchKeyEventListener(keyEvent -> {
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK && keyEvent.getRepeatCount() == 0 && sendPopupWindow != null && sendPopupWindow.isShowing()) {
                sendPopupWindow.dismiss();
            }
        });
        popupLayout1.setShownFromBottom(false);
        return popupLayout1;
    }

    @NonNull
    private ActionBarPopupWindow.ActionBarPopupWindowLayout getActionBarPopupWindowLayout(Context parentActivity, Theme.ResourcesProvider resourcesProvider) {
        ActionBarPopupWindow.ActionBarPopupWindowLayout popupLayout2 = new ActionBarPopupWindow.ActionBarPopupWindowLayout(parentActivity, resourcesProvider);
        popupLayout2.setAnimationEnabled(false);
        popupLayout2.setOnTouchListener(new OnTouchListener() {

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
        popupLayout2.setDispatchKeyEventListener(keyEvent -> {
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK && keyEvent.getRepeatCount() == 0 && sendPopupWindow != null && sendPopupWindow.isShowing()) {
                sendPopupWindow.dismiss();
            }
        });
        popupLayout2.setShownFromBottom(false);
        return popupLayout2;
    }

    public void setSendPopupWindow(ActionBarPopupWindow sendPopupWindow) {
        this.sendPopupWindow = sendPopupWindow;
    }

    private int getThemedColor() {
        Integer color = resourcesProvider != null ? resourcesProvider.getColor(Theme.key_dialogButtonSelector) : null;
        return color != null ? color : Theme.getColor(Theme.key_dialogButtonSelector);
    }

    public interface Delegate {
        void sendMessage();
    }
}