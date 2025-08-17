/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui.cells;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.LinearLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.INavigationLayout;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.ChatActionCell;
import org.telegram.ui.Cells.ChatMessageCell;
import org.telegram.ui.Components.BackgroundGradientDrawable;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.MotionBackgroundDrawable;

@SuppressLint("ViewConstructor")
public class MessagesPreviewDetailsPageCell extends LinearLayout {
    private BackgroundGradientDrawable.Disposable backgroundGradientDisposable;
    private BackgroundGradientDrawable.Disposable oldBackgroundGradientDisposable;

    private Drawable backgroundDrawable;
    private Drawable oldBackgroundDrawable;
    private final Drawable shadowDrawable;
    private final INavigationLayout parentLayout;

    private final ChatMessageCell replyMessageCell;
    private final ChatMessageCell mainMessageCell;
    private final ChatActionCell replyActionCell;
    private final ChatActionCell mainActionCell;

    private MessageObject currentMainMessage;

    public MessagesPreviewDetailsPageCell(Context context, INavigationLayout layout) {
        super(context);

        parentLayout = layout;

        setWillNotDraw(false);
        setOrientation(LinearLayout.VERTICAL);
        setPadding(0, dp(11), 0, dp(11));

        shadowDrawable = Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow);

        replyMessageCell = createNewChatMessageCell();
        replyMessageCell.setVisibility(View.GONE);
        addView(replyMessageCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        replyActionCell = createNewChatActionCell();
        replyActionCell.setVisibility(View.GONE);
        addView(replyActionCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        mainMessageCell = createNewChatMessageCell();
        mainMessageCell.setVisibility(View.GONE);
        addView(mainMessageCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        mainActionCell = createNewChatActionCell();
        mainActionCell.setVisibility(View.GONE);
        addView(mainActionCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    public void setMessages(MessageObject messageObject) {
        if (currentMainMessage != null && currentMainMessage.getId() == messageObject.getId()) {
            return;
        }
        currentMainMessage = messageObject;

        if (messageObject.replyMessageObject != null) {
            MessageObject replyObject = messageObject.replyMessageObject;
            if (replyObject.messageOwner.reply_to != null) {
                replyObject.messageOwner.reply_to.reply_to_msg_id = 0;
            }
            replyObject.replyMessageObject = null;

            if (isActionMessage(replyObject)) {
                replyActionCell.setVisibility(View.VISIBLE);
                replyActionCell.setMessageObject(replyObject);
                replyMessageCell.setVisibility(View.GONE);
            } else {
                replyMessageCell.setVisibility(View.VISIBLE);
                replyMessageCell.setMessageObject(replyObject, null, false, false, false);
                replyActionCell.setVisibility(View.GONE);
            }
        } else {
            replyMessageCell.setVisibility(View.GONE);
            replyActionCell.setVisibility(View.GONE);
        }

        if (isActionMessage(messageObject)) {
            mainActionCell.setVisibility(View.VISIBLE);
            mainActionCell.setMessageObject(messageObject);
            mainMessageCell.setVisibility(View.GONE);
        } else {
            mainMessageCell.setVisibility(View.VISIBLE);
            mainMessageCell.setMessageObject(messageObject, null, false, false, false);
            mainActionCell.setVisibility(View.GONE);
        }
    }

    private boolean isActionMessage(MessageObject obj) {
        return obj.type == 10 || obj.type == MessageObject.TYPE_ACTION_PHOTO || obj.type == MessageObject.TYPE_PHONE_CALL;
    }

    private ChatMessageCell createNewChatMessageCell() {
        ChatMessageCell cell = new ChatMessageCell(getContext(), UserConfig.selectedAccount);
        cell.isChat = true;
        cell.setFullyDraw(true);
        return cell;
    }
    private ChatActionCell createNewChatActionCell() {
        ChatActionCell cell = new ChatActionCell(getContext());
        cell.setDelegate(new ChatActionCell.ChatActionCellDelegate() {
            @Override
            public long getDialogId() {
                return currentMainMessage != null ? currentMainMessage.getDialogId() : 0;
            }
        });
        return cell;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable newDrawable = Theme.getCachedWallpaperNonBlocking();
        if (newDrawable != backgroundDrawable && newDrawable != null) {
            if (Theme.isAnimatingColor()) {
                oldBackgroundDrawable = backgroundDrawable;
                oldBackgroundGradientDisposable = backgroundGradientDisposable;
            } else if (backgroundGradientDisposable != null) {
                backgroundGradientDisposable.dispose();
                backgroundGradientDisposable = null;
            }
            backgroundDrawable = newDrawable;
        }
        float themeAnimationValue = parentLayout.getThemeAnimationValue();
        for (int a = 0; a < 2; a++) {
            Drawable drawable = a == 0 ? oldBackgroundDrawable : backgroundDrawable;
            if (drawable == null) {
                continue;
            }
            if (a == 1 && oldBackgroundDrawable != null) {
                drawable.setAlpha((int) (255 * themeAnimationValue));
            } else {
                drawable.setAlpha(255);
            }
            if (drawable instanceof ColorDrawable || drawable instanceof GradientDrawable || drawable instanceof MotionBackgroundDrawable) {
                drawable.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
                if (drawable instanceof BackgroundGradientDrawable backgroundGradientDrawable) {
                    backgroundGradientDisposable = backgroundGradientDrawable.drawExactBoundsSize(canvas, this);
                } else {
                    drawable.draw(canvas);
                }
            } else if (drawable instanceof BitmapDrawable bitmapDrawable) {
                if (bitmapDrawable.getTileModeX() == Shader.TileMode.REPEAT) {
                    canvas.save();
                    float scale = 2.0f / AndroidUtilities.density;
                    canvas.scale(scale, scale);
                    drawable.setBounds(0, 0, (int) Math.ceil(getMeasuredWidth() / scale), (int) Math.ceil(getMeasuredHeight() / scale));
                } else {
                    int viewHeight = getMeasuredHeight();
                    float scaleX = (float) getMeasuredWidth() / (float) drawable.getIntrinsicWidth();
                    float scaleY = (float) (viewHeight) / (float) drawable.getIntrinsicHeight();
                    float scale = Math.max(scaleX, scaleY);
                    int width = (int) Math.ceil(drawable.getIntrinsicWidth() * scale);
                    int height = (int) Math.ceil(drawable.getIntrinsicHeight() * scale);
                    int x = (getMeasuredWidth() - width) / 2;
                    int y = (viewHeight - height) / 2;
                    canvas.save();
                    canvas.clipRect(0, 0, width, getMeasuredHeight());
                    drawable.setBounds(x, y, x + width, y + height);
                }
                drawable.draw(canvas);
                canvas.restore();
            }
            if (a == 0 && oldBackgroundDrawable != null && themeAnimationValue >= 1.0f) {
                if (oldBackgroundGradientDisposable != null) {
                    oldBackgroundGradientDisposable.dispose();
                    oldBackgroundGradientDisposable = null;
                }
                oldBackgroundDrawable = null;
                invalidate();
            }
        }
        shadowDrawable.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
        shadowDrawable.draw(canvas);

        drawAvatarForCell(canvas, replyMessageCell);
        drawAvatarForCell(canvas, mainMessageCell);
    }

    private void drawAvatarForCell(Canvas canvas, ChatMessageCell cell) {
        if (cell.getVisibility() != View.VISIBLE) {
            return;
        }
        ImageReceiver imageReceiver = cell.getAvatarImage();
        if (imageReceiver != null) {
            int top = cell.getTop();
            float tx = cell.getTranslationX();
            int y = cell.getTop() + cell.getLayoutHeight();
            if (y - dp(48) < top) {
                y = top + dp(48);
            }
            if (tx != 0) {
                canvas.save();
                canvas.translate(tx, 0);
            }
            imageReceiver.setImageY(y - dp(44));
            imageReceiver.draw(canvas);
            if (tx != 0) {
                canvas.restore();
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (backgroundGradientDisposable != null) {
            backgroundGradientDisposable.dispose();
            backgroundGradientDisposable = null;
        }
        if (oldBackgroundGradientDisposable != null) {
            oldBackgroundGradientDisposable.dispose();
            oldBackgroundGradientDisposable = null;
        }
    }

    @Override
    protected void dispatchSetPressed(boolean pressed) {
    }
}