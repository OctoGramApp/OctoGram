/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui.components;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.formatShortNumber;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.INavigationLayout;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.ChatMessageCell;
import org.telegram.ui.Components.BackgroundGradientDrawable;
import org.telegram.ui.Components.ChatAvatarContainer;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.MotionBackgroundDrawable;

import java.util.Locale;
import java.util.Random;

import it.octogram.android.ActionBarCenteredTitle;
import it.octogram.android.OctoConfig;

public class StickerSizeUI extends BaseFragment {
    private ActionBarMenuItem searchIconItem;
    private ChatAvatarContainer avatarContainer;
    StickerSizePreviewMessages previewMessages;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(false);

        ActionBarMenu menu = actionBar.createMenu();
        menu.setCenteredTitle(isCenteredTitle());

        if (!isCenteredTitle()) {
            searchIconItem = menu.addItem(0, R.drawable.ic_ab_search);
        }

        menu.addItem(1, R.drawable.ic_ab_other);

        avatarContainer = new ChatAvatarContainer(context, this, false, getResourceProvider()) {
            @Override
            protected boolean isCentered() {
                return isCenteredTitle();
            }
        };
        avatarContainer.allowDrawStories = false;
        avatarContainer.setClipChildren(false);

        actionBar.addView(avatarContainer, 0, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER_VERTICAL | Gravity.RIGHT, 40, 0, 0, 0));
        reloadActionBar();

        FrameLayout layout = new FrameLayout(context);
        layout.setLayoutParams(LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        layout.addView(previewMessages = new StickerSizePreviewMessages(context, INavigationLayout.newLayout(context, false)));

        return layout;
    }

    private boolean isCenteredTitle() {
        int centeredTitle = OctoConfig.INSTANCE.uiTitleCenteredState.getValue();
        return centeredTitle == ActionBarCenteredTitle.ALWAYS.getValue() || centeredTitle == ActionBarCenteredTitle.JUST_IN_CHATS.getValue();
    }

    public void reloadActionBar() {
        if (searchIconItem != null) {
            searchIconItem.setVisibility(OctoConfig.INSTANCE.searchIconInHeader.getValue() ? View.VISIBLE : View.GONE);
        }

        boolean slidingTitle = OctoConfig.INSTANCE.slidingTitle.getValue();

        int[] result = new int[1];
        String shortNumber = formatShortNumber(2024, result);

        if (!slidingTitle) {
            avatarContainer.getTitleTextView().resetScrolling();
        }

        avatarContainer.getAvatarImageView().setImageResource(R.drawable.ic_unsized_octo);
        avatarContainer.setTitle(LocaleController.getString(R.string.StickersSizeChannelTitle), false, false, true, false, null, false, slidingTitle);
        avatarContainer.setSubtitle(LocaleController.formatPluralString("Members", result[0]).replace(String.format(Locale.getDefault(), "%d", result[0]), shortNumber));
        ((ViewGroup.MarginLayoutParams) avatarContainer.getLayoutParams()).rightMargin = isCenteredTitle() ? 0 : dp(OctoConfig.INSTANCE.searchIconInHeader.getValue() ? 96 : 40);
    }

    @SuppressLint("ViewConstructor")
    public static class StickerSizePreviewMessages extends LinearLayout {

        private BackgroundGradientDrawable.Disposable backgroundGradientDisposable;
        private BackgroundGradientDrawable.Disposable oldBackgroundGradientDisposable;

        private Drawable backgroundDrawable;
        private Drawable oldBackgroundDrawable;
        private final ChatMessageCell[] cells = new ChatMessageCell[2];
        private final MessageObject[] messageObjects = new MessageObject[2];
        private final Drawable shadowDrawable;
        private final INavigationLayout parentLayout;

        public StickerSizePreviewMessages(Context context, INavigationLayout layout) {
            super(context);

            parentLayout = layout;

            setWillNotDraw(false);
            setOrientation(LinearLayout.VERTICAL);
            //setPadding(0, dp(11), 0, dp(11));

            shadowDrawable = Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow);

            int date = (int) (System.currentTimeMillis() / 1000) - 60 * 60;
            TLRPC.TL_message message = new TLRPC.TL_message();
            message.date = date + 10;
            message.dialog_id = 1;
            message.flags = 257;
            message.id = 1;
            message.post_author = "Nick";
            message.media = new TLRPC.TL_messageMediaDocument();
            message.media.flags = 1;
            message.media.document = new TLRPC.TL_document();
            message.media.document.mime_type = "image/webp";
            message.media.document.file_reference = new byte[0];
            message.media.document.access_hash = 0;
            message.media.document.date = date;
            TLRPC.TL_documentAttributeSticker attributeSticker = new TLRPC.TL_documentAttributeSticker();
            attributeSticker.alt = "\ud83d\udc19";
            message.media.document.attributes.add(attributeSticker);
            TLRPC.TL_documentAttributeImageSize attributeImageSize = new TLRPC.TL_documentAttributeImageSize();
            attributeImageSize.h = 512;
            attributeImageSize.w = 512;
            message.media.document.attributes.add(attributeImageSize);
            message.message = "\ud83d\udc19";
            message.out = true;
            message.peer_id = new TLRPC.TL_peerUser();
            message.peer_id.user_id = 0;
            message.views = 350;
            messageObjects[0] = new MessageObject(UserConfig.selectedAccount, message, true, false);
            messageObjects[0].useCustomPhoto = true;

            message = new TLRPC.TL_message();
            String[] messageReply = {"StickerSizePreviewMessage1", "StickerSizePreviewMessage2", "StickerSizePreviewMessage3", "StickerSizePreviewMessage5"};
            String x = messageReply[new Random().nextInt(messageReply.length)];
            message.message = LocaleController.getString(x);

            message.date = date + 1270;
            message.dialog_id = -1;
            message.flags = 259;
            message.id = 2;
            message.media = new TLRPC.TL_messageMediaEmpty();
            message.out = false;
            message.peer_id = new TLRPC.TL_peerUser();
            message.peer_id.user_id = 1;

            String[] messageRead = {"StickerSizePreviewMessage1", "StickerSizePreviewMessage2", "StickerSizePreviewMessage3", "StickerSizePreviewMessage5"};
            String y = messageRead[new Random().nextInt(messageRead.length)];
            String cool = "";

            long documentId = switch (y) {
                case "StickerSizePreviewMessage1" -> {
                    cool = "\ud83d\udc19";
                    yield 5352815688010441881L;
                }
                case "StickerSizePreviewMessage2" -> {
                    cool = "\ud83c\udf55";
                    yield 5370980663778351052L;
                }
                default -> 0;
            };

            message.message = LocaleController.getString(y);


            int index1 = message.message.indexOf(cool);
            if (index1 >= 0) {
                TLRPC.TL_messageEntityCustomEmoji entity = new TLRPC.TL_messageEntityCustomEmoji();
                entity.offset = index1;
                entity.length = cool.length();
                entity.document_id = documentId;
                message.entities.add(entity);
            }

            message.date = date + 1270;
            message.dialog_id = 1;
            message.flags = 257 + 8;
            message.from_id = new TLRPC.TL_peerUser();
            message.id = 2;
            message.reply_to = new TLRPC.TL_messageReplyHeader();
            message.reply_to.flags |= 16;
            message.reply_to.reply_to_msg_id = 1;
            message.media = new TLRPC.TL_messageMediaEmpty();
            message.out = false;
            message.peer_id = new TLRPC.TL_peerUser();
            message.peer_id.user_id = UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId();
            message.flags |= TLRPC.MESSAGE_FLAG_EDITED | TLRPC.MESSAGE_FLAG_HAS_VIEWS | TLRPC.MESSAGE_FLAG_HAS_ENTITIES;
            message.edit_date = date + 1950;
            message.edit_hide = false;
            message.views = 250;
            messageObjects[1] = new MessageObject(UserConfig.selectedAccount, message, true, false);
            //String[] strings = {"Nick", "OctoGram Dev"};
            messageObjects[1].overrideLinkEmoji = 5258073068852485953L;
            messageObjects[1].overrideLinkColor = 9;
            messageObjects[1].customReplyName = LocaleController.getString(R.string.StickersSizeChannelTitleMini);
            messageObjects[1].replyMessageObject = messageObjects[0];

            for (int a = 0; a < cells.length; a++) {
                cells[a] = new ChatMessageCell(context, UserConfig.selectedAccount);
                cells[a].setDelegate(new ChatMessageCell.ChatMessageCellDelegate() {
                });
                cells[a].isChat = false;
                cells[a].setFullyDraw(true);
                cells[a].setMessageObject(messageObjects[a], null, a == cells.length - 1, a == 0);
                cells[a].requestLayout();
                addView(cells[a], LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            }
        }

        @Override
        public void invalidate() {
            super.invalidate();
            for (int a = 0; a < cells.length; a++) {
                boolean hasMedia = !(cells[a].getMessageObject().messageOwner.media instanceof TLRPC.TL_messageMediaEmpty);
                if (hasMedia) {
                    cells[a].setMessageObject(messageObjects[a], null, a == cells.length - 1, a == 0);
                    cells[a].invalidate();
                } else {
                    cells[a].getMessageObject().resetLayout();
                    cells[a].requestLayout();
                }
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
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            return false;
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            return false;
        }

        @Override
        protected void dispatchSetPressed(boolean pressed) {

        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            return false;
        }
    }
}
