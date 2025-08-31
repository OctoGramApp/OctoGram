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
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

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

import java.util.ArrayList;

@SuppressLint("ViewConstructor")
public class MessagesPreviewDetailsPageCell extends LinearLayout {
    private BackgroundGradientDrawable.Disposable backgroundGradientDisposable;
    private BackgroundGradientDrawable.Disposable oldBackgroundGradientDisposable;

    private Drawable backgroundDrawable;
    private Drawable oldBackgroundDrawable;
    private final ArrayList<View> cells;
    private final ArrayList<MessageObject> messageObjects;
    private final Drawable shadowDrawable;
    private final INavigationLayout parentLayout;

    public MessagesPreviewDetailsPageCell(Context context, INavigationLayout layout) {
        super(context);

        parentLayout = layout;
        cells = new ArrayList<>();
        messageObjects = new ArrayList<>();

        setWillNotDraw(false);
        setOrientation(LinearLayout.VERTICAL);
        setPadding(0, dp(11), 0, dp(11));

        shadowDrawable = Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow);
    }

    public void setMessages(MessageObject messageObject) {
        messageObjects.clear();
        cells.clear();
        if (messageObject.replyMessageObject != null) {
            if (messageObject.replyMessageObject.messageOwner.reply_to != null) {
                messageObject.replyMessageObject.messageOwner.reply_to.reply_to_msg_id = 0;
            }
            messageObject.replyMessageObject.replyMessageObject = null;
            messageObjects.add(messageObject.replyMessageObject);
        }
        messageObjects.add(messageObject);
        removeAllViews();

        for (MessageObject obj : messageObjects) {
            if (obj.type == 10 || obj.type == MessageObject.TYPE_ACTION_PHOTO || obj.type == MessageObject.TYPE_PHONE_CALL) {
                ChatActionCell cell = getChatActionCell(obj);
            addView(cell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            cells.add(cell);
        } else {
        ChatMessageCell cell = new ChatMessageCell(getContext(), UserConfig.selectedAccount);
        cell.setDelegate(new ChatMessageCell.ChatMessageCellDelegate() {
            @Override
            public boolean canPerformActions() {
                return true;
            }

            @Override
            public void didLongPress(ChatMessageCell cell, float x, float y) {
                cell.resetPressedLink(-1);
            }
        });
        cell.isChat = true;
        cell.setFullyDraw(true);
        cell.setMessageObject(obj, null, false, false, false);
                addView(cell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
                cells.add(cell);
            }
        }
    }

    @NonNull
    private ChatActionCell getChatActionCell(MessageObject obj) {
        ChatActionCell cell = new ChatActionCell(getContext()) {
            @Override
            public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(info);
                info.setVisibleToUser(true);
            }
        };
        cell.setDelegate(new ChatActionCell.ChatActionCellDelegate() {
            @Override
            public long getDialogId() {
                return obj.getDialogId();
            }
        });
        cell.setMessageObject(obj);
        return cell;
    }


    @Override
    public void invalidate() {
        super.invalidate();
        for (int a = 0; a < cells.size(); a++) {
            View cell = cells.get(a);
            if (cell instanceof ChatMessageCell) {
                ((ChatMessageCell) cell).setMessageObject(messageObjects.get(a), null, false, false, false);
            } else if (cell instanceof ChatActionCell) {
                ((ChatActionCell) cell).setMessageObject(messageObjects.get(a), true);
            }
            cell.invalidate();
        }
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
        for (View view : cells) {
            if (view instanceof ChatMessageCell cell) {
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