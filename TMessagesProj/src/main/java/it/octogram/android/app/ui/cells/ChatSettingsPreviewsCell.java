/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui.cells;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.formatPluralString;
import static org.telegram.messenger.LocaleController.formatShortNumber;
import static org.telegram.messenger.LocaleController.getString;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
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
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarMenuSubItem;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.ChatMessageCell;
import org.telegram.ui.Components.BackgroundGradientDrawable;
import org.telegram.ui.Components.ChatActivityEnterView;
import org.telegram.ui.Components.ChatAvatarContainer;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.FilterTabsView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ListView.AdapterWithDiffUtils;
import org.telegram.ui.Components.MotionBackgroundDrawable;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SizeNotifierFrameLayout;
import org.telegram.ui.Components.UnreadCounterTextView;
import org.telegram.ui.Components.ViewPagerFixed;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import it.octogram.android.ActionBarCenteredTitle;
import it.octogram.android.ContextMenuBriefingState;
import it.octogram.android.OctoConfig;
import it.octogram.android.TabMode;
import it.octogram.android.TabStyle;
import it.octogram.android.app.fragment.PreferencesFragment;
import it.octogram.android.app.ui.OctoGeneralSearchOrderUI;
import it.octogram.android.utils.chat.ContextMenuHelper;
import it.octogram.android.utils.config.SearchOptionsOrderController;

@SuppressLint({"ClickableViewAccessibility", "ViewConstructor"})
public class ChatSettingsPreviewsCell extends FrameLayout {
    private final Context context;
    private final PreferencesFragment fragment;
    private final int viewType;

    private StickerSizePreviewMessages messages;

    private ActionBarMenuItem searchIconItem;
    private ChatAvatarContainer avatarContainer;

    private ChatActivityEnterView inputBox;

    private ImageView bottomHiddenView;
    private ImageView bottomChatButton;
    private ImageView bottomGiftButton;
    private UnreadCounterTextView bottomOverlayChatText;
    private AnimatorSet discussValueAnimator;
    private boolean wasBarHidden = false;
    private boolean wasChatHidden = false;
    private boolean wasGiftHidden = false;

    private ViewPagerFixed.TabsView filterTabsView;

    private final ImageView gradientBackground;

    public static class PreviewType {
        public static int HEADER = 0;
        public static int MESSAGES = 1;
        public static int STICKER = 2;
        public static int INPUT_BOX = 3;
        public static int DISCUSS = 4;
        public static int CONTEXT_MENU = 5;
        public static int SEARCH_ORDER = 6;
    }

    public ChatSettingsPreviewsCell(Context context, int viewType) {
        this(context, null, viewType);
    }

    public ChatSettingsPreviewsCell(Context context, PreferencesFragment fragment, int viewType) {
        super(context);

        this.context = context;
        this.fragment = fragment;
        this.viewType = viewType;

        LinearLayout internalFrameLayout = new LinearLayout(context);
        internalFrameLayout.setClipToPadding(true);
        internalFrameLayout.setClipToOutline(true);
        internalFrameLayout.setClipChildren(true);
        internalFrameLayout.setPadding(dp(2), dp(2), dp(2), dp(2));

        GradientDrawable border = new GradientDrawable();
        border.setShape(GradientDrawable.RECTANGLE);
        border.setColor(Color.TRANSPARENT);
        border.setAlpha(150);
        border.setStroke(dp(1), Theme.getColor(Theme.key_windowBackgroundWhiteGrayText), dp(5), dp(5));
        border.setCornerRadius(dp(16));
        internalFrameLayout.setBackground(border);

        gradientBackground = new ImageView(context);
        gradientBackground.setAlpha(0f);
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP,
                new int[] {
                        Theme.getColor(Theme.key_chats_menuBackground),
                        ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_chats_menuBackground), 100),
                        AndroidUtilities.getTransparentColor(Theme.getColor(Theme.key_chats_menuBackground), 0)
                }
        );
        gradientBackground.setBackground(gradient);

        internalFrameLayout.addView(getInterface(), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        setPadding(dp(15), dp(15), dp(15), dp(15));
        setBackground(Theme.createRoundRectDrawable(0, Theme.getColor(Theme.key_windowBackgroundWhite)));
        addView(internalFrameLayout, LayoutHelper.createLinear(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));

        addView(gradientBackground, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.BOTTOM));
    }

    private LinearLayout getInterface() {
        GradientDrawable border = new GradientDrawable();
        border.setShape(GradientDrawable.RECTANGLE);
        border.setColor(Color.TRANSPARENT);
        border.setCornerRadius(dp(16));

        LinearLayout navigationFrame = new LinearLayout(context);
        navigationFrame.setClipToPadding(true);
        navigationFrame.setClipToOutline(true);
        navigationFrame.setClipChildren(true);
        navigationFrame.setBackground(border);

        if (viewType == PreviewType.HEADER) {
            ActionBar actionBar = new ActionBar(context);
            actionBar.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
            actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_actionBarDefaultSelector), false);
            actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_actionBarActionModeDefaultSelector), true);
            actionBar.setItemsColor(Theme.getColor(Theme.key_actionBarDefaultIcon), false);
            actionBar.setItemsColor(Theme.getColor(Theme.key_actionBarActionModeDefaultIcon), true);
            actionBar.setBackButtonImage(R.drawable.ic_ab_back);
            actionBar.setAllowOverlayTitle(false);

            ActionBarMenu menu = actionBar.createMenu();
            menu.setCenteredTitle(isCenteredTitle());

            if (!isCenteredTitle()) {
                searchIconItem = menu.addItem(0, R.drawable.ic_ab_search);
            }

            menu.addItem(1, R.drawable.ic_ab_other);

            avatarContainer = new ChatAvatarContainer(context, null, false, null) {
                @Override
                protected boolean isCentered() {
                    return isCenteredTitle();
                }
            };
            avatarContainer.allowDrawStories = false;
            avatarContainer.setClipChildren(false);

            actionBar.addView(avatarContainer, 0, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER_VERTICAL | Gravity.RIGHT, 40, 0, 0, 0));
            reloadActionBar();

            navigationFrame.addView(actionBar, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        } else if (viewType == PreviewType.INPUT_BOX) {
            handleInputBox(navigationFrame);
        } else if (viewType == PreviewType.DISCUSS) {
            handleDiscuss(navigationFrame);
        } else if (viewType == PreviewType.SEARCH_ORDER) {
            handleSearchOrder(navigationFrame);
        } else {
            messages = new StickerSizePreviewMessages(context);
            navigationFrame.addView(messages, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }

        return navigationFrame;
    }

    private void handleDiscuss(LinearLayout navigationFrame) {
        wasBarHidden = OctoConfig.INSTANCE.hideBottomBarChannels.getValue();
        wasChatHidden = OctoConfig.INSTANCE.hideChatButtonChannels.getValue();
        wasGiftHidden = OctoConfig.INSTANCE.hideGiftButtonChannels.getValue();
        boolean isBottomBarHidden = OctoConfig.INSTANCE.hideBottomBarChannels.getValue();

        FrameLayout bottomOverlayChat = new FrameLayout(context);
        bottomOverlayChat.setBackgroundColor(Theme.getColor(Theme.key_chat_messagePanelBackground));
        bottomOverlayChat.setWillNotDraw(false);
        bottomOverlayChat.setClipChildren(false);
        bottomOverlayChat.setClickable(false);
        bottomOverlayChat.setFocusable(false);
        navigationFrame.addView(bottomOverlayChat, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 51, Gravity.BOTTOM));

        bottomOverlayChatText = new UnreadCounterTextView(context) {
            @Override
            protected float getTopOffset() {
                return -AndroidUtilities.dp(2);
            }
        };
        bottomOverlayChatText.setAlpha(isBottomBarHidden ? 0.3f : 1f);
        bottomOverlayChatText.setText(LocaleController.getString(R.string.ChannelMute), false);
        bottomOverlayChat.addView(bottomOverlayChatText, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, 0, 0, 1.5f, 0, 0));

        bottomChatButton = new ImageView(getContext());
        bottomChatButton.setImageResource(R.drawable.input_message);
        bottomChatButton.setAlpha(OctoConfig.INSTANCE.hideChatButtonChannels.getValue() ? 0f : (isBottomBarHidden ? 0.3f : 1f));
        bottomChatButton.setScaleType(ImageView.ScaleType.CENTER);
        bottomChatButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chat_fieldOverlayText), PorterDuff.Mode.SRC_IN));
        bottomOverlayChat.addView(bottomChatButton, LayoutHelper.createFrame(48, 48, Gravity.LEFT | Gravity.CENTER_VERTICAL));

        bottomGiftButton = new ImageView(getContext());
        bottomGiftButton.setImageResource(R.drawable.input_gift_s);
        bottomGiftButton.setAlpha(OctoConfig.INSTANCE.hideGiftButtonChannels.getValue() ? 0f : (isBottomBarHidden ? 0.3f : 1f));
        bottomGiftButton.setScaleType(ImageView.ScaleType.CENTER);
        bottomGiftButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chat_fieldOverlayText), PorterDuff.Mode.SRC_IN));
        bottomOverlayChat.addView(bottomGiftButton, LayoutHelper.createFrame(48, 48, Gravity.RIGHT | Gravity.CENTER_VERTICAL));

        bottomHiddenView = new ImageView(getContext());
        bottomHiddenView.setImageResource(R.drawable.msg_archive_hide);
        bottomHiddenView.setAlpha(isBottomBarHidden ? 1f : 0f);
        bottomHiddenView.setScaleType(ImageView.ScaleType.CENTER);
        bottomHiddenView.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chat_fieldOverlayText), PorterDuff.Mode.SRC_IN));
        bottomOverlayChat.addView(bottomHiddenView, LayoutHelper.createFrame(48, 48, Gravity.CENTER));
    }

    private void handleInputBox(LinearLayout navigationFrame) {
        AndroidUtilities.runOnUIThread(() -> {
            SizeNotifierFrameLayout layout = new SizeNotifierFrameLayout(context);
            Activity activity = fragment.getParentActivity();
            if (activity == null) {
                return;
            }
            ChatActivityEnterView view = inputBox = new ChatActivityEnterView(activity, layout, null, false, null);
            view.setDelegate(new FakeDelegate());
            view.setBackgroundColor(Theme.getColor(Theme.key_chat_messagePanelBackground));
            view.allowBlur = false;
            view.shouldDrawBackground = false;
            view.setAllowStickersAndGifs(true, true, true);
            view.setEmojiButtonImage(false, false, true);
            view.updateSendAsButton(false, true);
            navigationFrame.addView(view, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }, 500);
    }

    private void handleSearchOrder(LinearLayout navigationFrame) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        ActionBar actionBar = new ActionBar(context);
        actionBar.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_actionBarActionModeDefaultSelector), false);
        actionBar.setItemsBackgroundColor(Theme.getColor(Theme.key_actionBarActionModeDefaultSelector), true);
        actionBar.setItemsColor(Theme.getColor(Theme.key_actionBarActionModeDefaultIcon), false);
        actionBar.setItemsColor(Theme.getColor(Theme.key_actionBarActionModeDefaultIcon), true);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(false);
        actionBar.setOccupyStatusBar(false);
        ActionBarMenu menu = actionBar.createMenu();
        menu.addItem(0, R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener()).setSearchFieldHint("Search chats...");
        layout.addView(actionBar, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, ActionBar.getCurrentActionBarHeight()));

        filterTabsView = new ViewPagerFixed.TabsView(context, false, 8, null);
        filterTabsView.setDelegate(new ViewPagerFixed.TabsView.TabsViewDelegate() {
            @Override
            public boolean canPerformActions() {
                return false;
            }
        });

        layout.addView(filterTabsView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 44));

        menu.setSearchCursorColor(Theme.getColor(Theme.key_actionBarActionModeDefaultIcon));
        menu.setSearchTextColor(Theme.getColor(Theme.key_actionBarActionModeDefaultIcon), false);
        menu.setSearchTextColor(Theme.getColor(Theme.key_actionBarActionModeDefaultIcon), true);

        navigationFrame.addView(layout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        actionBar.setSearchTextColor(Theme.getColor(Theme.key_actionBarDefaultArchivedSearch), false);
        actionBar.openSearchField(false);
        reloadSearchOrderTabs();
    }

    private boolean isCenteredTitle() {
        int centeredTitle = OctoConfig.INSTANCE.uiTitleCenteredState.getValue();
        return centeredTitle == ActionBarCenteredTitle.ALWAYS.getValue() || centeredTitle == ActionBarCenteredTitle.JUST_IN_CHATS.getValue();
    }

    public void invalidate() {
        reloadActionBar();
        reloadMessages();
        reloadInputBox();
        reloadDiscuss();
        reloadSearchOrderTabs();
    }

    public void reloadSingleActionBarMembersCount() {
        if (viewType != PreviewType.HEADER) {
            return;
        }

        int[] result = new int[1];
        String shortNumber = formatShortNumber(2024, result);

        avatarContainer.setSubtitle(formatPluralString("Members", result[0]).replace(String.format(Locale.getDefault(), "%d", result[0]), shortNumber));
    }

    private void reloadActionBar() {
        if (viewType != PreviewType.HEADER) {
            return;
        }

        if (searchIconItem != null) {
            searchIconItem.setVisibility(OctoConfig.INSTANCE.searchIconInHeader.getValue() ? View.VISIBLE : View.GONE);
        }

        boolean slidingTitle = OctoConfig.INSTANCE.slidingTitle.getValue();

        if (!slidingTitle) {
            avatarContainer.getTitleTextView().resetScrolling();
        }

        avatarContainer.getAvatarImageView().setImageResource(R.drawable.ic_unsized_octo);
        avatarContainer.setTitle(getString(R.string.StickersSizeChannelTitle), false, false, true, false, null, false, slidingTitle);
        reloadSingleActionBarMembersCount();
        ((ViewGroup.MarginLayoutParams) avatarContainer.getLayoutParams()).rightMargin = isCenteredTitle() ? 0 : dp(OctoConfig.INSTANCE.searchIconInHeader.getValue() ? 96 : 40);
    }

    private void reloadMessages() {
        if (viewType != PreviewType.STICKER && viewType != PreviewType.MESSAGES && viewType != PreviewType.CONTEXT_MENU) {
            return;
        }

        messages.invalidate();
    }

    private void reloadInputBox() {
        if (viewType != PreviewType.INPUT_BOX) {
            return;
        }

        inputBox.setEmojiButtonImage(false, true, true);
        inputBox.updateSendAsButton(true, true);
        inputBox.checkRoundVideo(true);
    }

    private void reloadDiscuss() {
        if (viewType != PreviewType.DISCUSS) {
            return;
        }

        if (discussValueAnimator != null) {
            discussValueAnimator.cancel();
            discussValueAnimator = null;
        }

        boolean isBarHidden = OctoConfig.INSTANCE.hideBottomBarChannels.getValue();
        boolean isChatHidden = OctoConfig.INSTANCE.hideChatButtonChannels.getValue();
        boolean isGiftHidden = OctoConfig.INSTANCE.hideGiftButtonChannels.getValue();

        ArrayList<Animator> animators = new ArrayList<>();
        if (wasBarHidden != isBarHidden) {
            animators.add(ObjectAnimator.ofFloat(bottomOverlayChatText, "alpha", wasBarHidden ? 0.3f : 1f, isBarHidden ? 0.3f : 1f));
            animators.add(ObjectAnimator.ofFloat(bottomHiddenView, "alpha", wasBarHidden ? 1f : 0f, isBarHidden ? 1f : 0f));
            animators.add(ObjectAnimator.ofFloat(bottomHiddenView, "scaleX", wasBarHidden ? 1f : 0.6f, isBarHidden ? 1f : 0.6f));
            animators.add(ObjectAnimator.ofFloat(bottomHiddenView, "scaleY", wasBarHidden ? 1f : 0.6f, isBarHidden ? 1f : 0.6f));
        }

        if (wasBarHidden != isBarHidden || wasGiftHidden != isGiftHidden || wasChatHidden != isChatHidden) {
            animators.add(ObjectAnimator.ofFloat(bottomGiftButton, "alpha", bottomGiftButton.getAlpha(), isGiftHidden ? 0f : (isBarHidden ? 0.3f : 1f)));
            animators.add(ObjectAnimator.ofFloat(bottomChatButton, "alpha", bottomChatButton.getAlpha(), isChatHidden ? 0f : (isBarHidden ? 0.3f : 1f)));
            if (wasGiftHidden != isGiftHidden) {
                animators.add(ObjectAnimator.ofFloat(bottomGiftButton, "scaleX", wasGiftHidden ? 0.6f : 1f, isGiftHidden ? 0.6f : 1f));
                animators.add(ObjectAnimator.ofFloat(bottomGiftButton, "scaleY", wasGiftHidden ? 0.6f : 1f, isGiftHidden ? 0.6f : 1f));
            }
            if (wasChatHidden != isChatHidden) {
                animators.add(ObjectAnimator.ofFloat(bottomChatButton, "scaleX", wasChatHidden ? 0.6f : 1f, isChatHidden ? 0.6f : 1f));
                animators.add(ObjectAnimator.ofFloat(bottomChatButton, "scaleY", wasChatHidden ? 0.6f : 1f, isChatHidden ? 0.6f : 1f));
            }
        }

        wasBarHidden = isBarHidden;
        wasChatHidden = isChatHidden;
        wasGiftHidden = isGiftHidden;

        AnimatorSet set = discussValueAnimator = new AnimatorSet();
        set.setDuration(200);
        set.playTogether(animators);
        set.start();
    }

    private void reloadSearchOrderTabs() {
        if (viewType != PreviewType.SEARCH_ORDER) {
            return;
        }

        filterTabsView.removeTabs();

        int a = 0;
        List<Integer> orders = SearchOptionsOrderController.getCurrentOrder();
        for (int order : orders) {
            filterTabsView.addTab(a, OctoGeneralSearchOrderUI.getItemName(order));
            a++;
        }

        filterTabsView.finishAddingTabs();
        filterTabsView.selectTabWithId(0, 1f);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    private class StickerSizePreviewMessages extends LinearLayout {

        private BackgroundGradientDrawable.Disposable backgroundGradientDisposable;
        private BackgroundGradientDrawable.Disposable oldBackgroundGradientDisposable;

        private Drawable backgroundDrawable;
        private Drawable oldBackgroundDrawable;
        private ChatMessageCell cell;
        private MessageObject finalMessageObject;
        private ActionBarPopupWindow.ActionBarPopupWindowLayout popupLayout;
        private final Drawable shadowDrawable;

        private RecyclerListView listView;
        private final ArrayList<ContextMenuPreviewItem> currentShownRecentItems = new ArrayList<>();
        private final ArrayList<ContextMenuPreviewItem> oldRecentItems = new ArrayList<>();
        private ContextMenuPreviewAdapter previewAdapter;

        public StickerSizePreviewMessages(Context context) {
            super(context);

            setWillNotDraw(false);
            setOrientation(LinearLayout.VERTICAL);
            if (viewType == PreviewType.MESSAGES || viewType == PreviewType.CONTEXT_MENU) {
                setPadding(0, dp(11), 0, dp(11));
            } else if (viewType == PreviewType.STICKER) {
                setPadding(0, dp(8), 0, 0);
            }

            shadowDrawable = Theme.getThemedDrawable(context, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow);

            if (viewType == PreviewType.CONTEXT_MENU) {
                popupLayout = new ActionBarPopupWindow.ActionBarPopupWindowLayout(context, R.drawable.popup_fixed_alert2, null, 0);
                popupLayout.setMinimumWidth(AndroidUtilities.dp(250));
                popupLayout.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuBackground));
                popupLayout.setAlpha(0f);
                popupLayout.setPivotX(0);
                popupLayout.setPivotY(0);
                popupLayout.setFitItems(true);

                listView = new RecyclerListView(context);
                listView.setItemAnimator(getDefaultItemAnimator());
                listView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                listView.setAdapter(previewAdapter = new ContextMenuPreviewAdapter(context));
                listView.setTag(14);
                popupLayout.addView(listView, LayoutHelper.createLinear(250 - 8*2, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 8, 0, 8, 0));

                addView(popupLayout, LayoutHelper.createLinear(250, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
                AndroidUtilities.runOnUIThread(this::invalidateInternally, 20);
            }

            if (viewType == PreviewType.STICKER || viewType == PreviewType.MESSAGES) {
                MessageObject[] messageObjects = new MessageObject[2];

                int date = (int) (System.currentTimeMillis() / 1000) - 60 * 60;
                TLRPC.TL_message stickerMessage = new TLRPC.TL_message();
                stickerMessage.date = date + 10;
                stickerMessage.dialog_id = 1;
                stickerMessage.flags = 257;
                stickerMessage.id = 1;
                stickerMessage.post_author = "OctoGram";
                stickerMessage.media = new TLRPC.TL_messageMediaDocument();
                stickerMessage.media.flags = 1;
                stickerMessage.media.document = new TLRPC.TL_document();
                stickerMessage.media.document.mime_type = "image/webp";
                stickerMessage.media.document.file_reference = new byte[0];
                stickerMessage.media.document.access_hash = 0;
                stickerMessage.media.document.date = date;
                TLRPC.TL_documentAttributeSticker stickerAttribute = new TLRPC.TL_documentAttributeSticker();
                stickerAttribute.alt = "\ud83d\udc19";
                stickerMessage.media.document.attributes.add(stickerAttribute);
                TLRPC.TL_documentAttributeImageSize imageSizeAttribute = new TLRPC.TL_documentAttributeImageSize();
                imageSizeAttribute.h = 512;
                imageSizeAttribute.w = 512;
                stickerMessage.media.document.attributes.add(imageSizeAttribute);
                stickerMessage.message = "\ud83d\udc19";
                stickerMessage.out = true;
                stickerMessage.peer_id = new TLRPC.TL_peerUser();
                stickerMessage.peer_id.user_id = 0;
                stickerMessage.views = 350;
                messageObjects[0] = new MessageObject(UserConfig.selectedAccount, stickerMessage, true, false);
                messageObjects[0].useCustomPhoto = true;

                stickerMessage = new TLRPC.TL_message();
                int[] replyMessages = {
                        R.string.StickerSizePreviewMessage1,
                        R.string.StickerSizePreviewMessage2,
                        R.string.StickerSizePreviewMessage3,
                        R.string.StickerSizePreviewMessage5
                };
                int randomReplyMessageKey = replyMessages[new Random().nextInt(replyMessages.length)];
                stickerMessage.message = getString(randomReplyMessageKey);

                stickerMessage.date = date + 1270;
                stickerMessage.dialog_id = -1;
                stickerMessage.flags = 259;
                stickerMessage.id = 2;
                stickerMessage.media = new TLRPC.TL_messageMediaEmpty();
                stickerMessage.out = false;
                stickerMessage.peer_id = new TLRPC.TL_peerUser();
                stickerMessage.peer_id.user_id = 1;

                int[] readMessageKeys = {
                        R.string.StickerSizePreviewMessage1,
                        R.string.StickerSizePreviewMessage2,
                        R.string.StickerSizePreviewMessage3,
                        R.string.StickerSizePreviewMessage5
                };

                int randomReadMessageKey = readMessageKeys[new Random().nextInt(readMessageKeys.length)];
                String emojiCharacter = "";
                long documentId = 0;

                if (randomReadMessageKey == R.string.StickerSizePreviewMessage1) {
                    emojiCharacter = "\ud83d\udc19";
                    documentId = 5352815688010441881L;
                } else if (randomReadMessageKey == R.string.StickerSizePreviewMessage2) {
                    emojiCharacter = "\ud83c\udf55";
                    documentId = 5370980663778351052L;
                }

                stickerMessage.message = getString(randomReadMessageKey);

                int emojiStartIndex = stickerMessage.message.indexOf(emojiCharacter);
                if (emojiStartIndex >= 0) {
                    TLRPC.TL_messageEntityCustomEmoji customEmojiEntity = new TLRPC.TL_messageEntityCustomEmoji();
                    customEmojiEntity.offset = emojiStartIndex;
                    customEmojiEntity.length = emojiCharacter.length();
                    customEmojiEntity.document_id = documentId;
                    stickerMessage.entities.add(customEmojiEntity);
                }

                stickerMessage.date = date + 1270;
                stickerMessage.dialog_id = 1;
                stickerMessage.flags = 257 + 8;
                stickerMessage.from_id = new TLRPC.TL_peerUser();
                stickerMessage.id = 2;
                stickerMessage.reply_to = new TLRPC.TL_messageReplyHeader();
                stickerMessage.reply_to.flags |= 16;
                stickerMessage.reply_to.reply_to_msg_id = 1;
                stickerMessage.media = new TLRPC.TL_messageMediaEmpty();
                stickerMessage.out = false;
                stickerMessage.peer_id = new TLRPC.TL_peerUser();
                stickerMessage.peer_id.user_id = UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId();
                stickerMessage.flags |= TLRPC.MESSAGE_FLAG_EDITED | TLRPC.MESSAGE_FLAG_HAS_VIEWS | TLRPC.MESSAGE_FLAG_HAS_ENTITIES;
                stickerMessage.edit_date = date + 1950;
                stickerMessage.edit_hide = false;
                stickerMessage.views = 250;
                messageObjects[1] = new MessageObject(UserConfig.selectedAccount, stickerMessage, true, false);
                messageObjects[1].overrideLinkEmoji = 5258073068852485953L;
                messageObjects[1].overrideLinkColor = 9;
                messageObjects[1].customReplyName = getString(R.string.StickersSizeChannelTitleMini);
                messageObjects[1].replyMessageObject = messageObjects[0];

                finalMessageObject = messageObjects[viewType != ChatSettingsPreviewsCell.PreviewType.STICKER ? 1 : 0];
                cell = new ChatMessageCell(context, UserConfig.selectedAccount) {
                    @Override
                    protected boolean checkNeedDrawShareButton(MessageObject messageObject) {
                        return OctoConfig.INSTANCE.showShareButtonForMessages.getValue();
                    }
                };
                cell.setDelegate(new ChatMessageCell.ChatMessageCellDelegate() {
                });
                cell.isChat = false;
                cell.setFullyDraw(true);
                cell.setMessageObject(finalMessageObject, null, true, true, false); // check for firstInChat
                cell.requestLayout();
                addView(cell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            }
        }

        @NonNull
        private DefaultItemAnimator getDefaultItemAnimator() {
            DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
            itemAnimator.setSupportsChangeAnimations(false);
            itemAnimator.setDelayAnimations(false);
            itemAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
            itemAnimator.setDurations(340);
            return itemAnimator;
        }

        @Override
        public void invalidate() {
            super.invalidate();
            invalidateInternally();
        }

        private void invalidateInternally() {
            if (popupLayout != null) {
                if (listView.getTag() instanceof ValueAnimator v2) {
                    v2.cancel();
                    listView.setTag(null);
                }

                boolean isFirstLoad = popupLayout.getAlpha() == 0f;
                int height = listView.getMeasuredHeight();
                ViewGroup.LayoutParams params = listView.getLayoutParams();
                params.height = height;
                listView.setLayoutParams(params);

                oldRecentItems.clear();
                oldRecentItems.addAll(currentShownRecentItems);
                currentShownRecentItems.clear();
                currentShownRecentItems.addAll(ContextMenuHelper.fillPreviewMenu(context));
                previewAdapter.setItems(oldRecentItems, currentShownRecentItems);

                listView.measure(
                        View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                );

                int newHeight = listView.getMeasuredHeight();

                boolean[] useLimitedView = {false};
                if (OctoConfig.INSTANCE.contextMenuBriefingState.getValue() == ContextMenuBriefingState.DISABLED.getState()) {
                    if (newHeight > AndroidUtilities.displaySize.y/3) {
                        newHeight = AndroidUtilities.displaySize.y/3;
                        useLimitedView[0] = true;
                    }
                }

                int finalNewHeight = newHeight;
                boolean wasGradientBackgroundShown = gradientBackground.getAlpha() > 0f;

                ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
                animator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
                animator.addUpdateListener(animation -> {
                    float progress = (float) animation.getAnimatedValue();
                    ViewGroup.LayoutParams p2 = listView.getLayoutParams();
                    p2.height = (int) ((height + (finalNewHeight - height) * progress) * (isFirstLoad ? progress : 1));
                    listView.setLayoutParams(p2);

                    if (useLimitedView[0] && !wasGradientBackgroundShown) {
                        gradientBackground.setAlpha(progress);
                    } else if (!useLimitedView[0] && wasGradientBackgroundShown) {
                        gradientBackground.setAlpha(1f - progress);
                    }

                    if (isFirstLoad) {
                        popupLayout.setAlpha(progress);
                        popupLayout.setScaleX(progress);
                        popupLayout.setScaleY(progress);
                    }
                });
                animator.setDuration(340);
                animator.start();
                listView.setTag(animator);
            }

            if (cell != null) {
                boolean hasMedia = !(cell.getMessageObject().messageOwner.media instanceof TLRPC.TL_messageMediaEmpty);
                if (hasMedia) {
                    cell.setMessageObject(finalMessageObject, null, true, true, false); // check for firstInChat
                    cell.invalidate();
                } else {
                    cell.getMessageObject().resetLayout();
                    cell.requestLayout();
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
            for (int a = 0; a < 2; a++) {
                Drawable drawable = a == 0 ? oldBackgroundDrawable : backgroundDrawable;
                if (drawable == null) {
                    continue;
                }
                drawable.setAlpha(255);
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

        private class ContextMenuPreviewAdapter extends AdapterWithDiffUtils {

            private final Context context;

            public ContextMenuPreviewAdapter(Context context) {
                this.context = context;
            }

            @Override
            public boolean isEnabled(RecyclerView.ViewHolder holder) {
                return true;
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                if (viewType == ContextMenuPreviewItem.ITEM) {
                    ActionBarMenuSubItem currentItem = new ActionBarMenuSubItem(context, false, false);
                    currentItem.setMinimumWidth(AndroidUtilities.dp(250 - 8*2));
                    return new RecyclerListView.Holder(currentItem);
                } else if (viewType == ContextMenuPreviewItem.GAP_SHADOW) {
                    return new RecyclerListView.Holder(new ActionBarPopupWindow.GapView(context, null));
                } else if (viewType == ContextMenuPreviewItem.SHORTCUTS) {
                    return new RecyclerListView.Holder(new ContextMenuHelper.ShortcutsLayout(context));
                }
                return new RecyclerListView.Holder(new View(context));
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                if (position < currentShownRecentItems.size()) {
                    ContextMenuPreviewItem property = currentShownRecentItems.get(position);
                    if (property.viewType == ContextMenuPreviewItem.ITEM) {
                        ActionBarMenuSubItem item = (ActionBarMenuSubItem) holder.itemView;
                        item.setTextAndIcon(property.name, property.icon);
                        item.setRightIcon(property.expandable ? R.drawable.msg_arrowright : 0);
                    } else if (property.viewType == ContextMenuPreviewItem.GAP_SHADOW) {
                        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
                        if (layoutParams == null) {
                            layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 8);
                        } else {
                            layoutParams.height = 8;
                        }
                        holder.itemView.setLayoutParams(layoutParams);
                    } else if (property.viewType == ContextMenuPreviewItem.SHORTCUTS) {
                        ContextMenuHelper.ShortcutsLayout item = (ContextMenuHelper.ShortcutsLayout) holder.itemView;
                        ViewGroup.LayoutParams layoutParams = item.getLayoutParams();
                        if (layoutParams == null) {
                            layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        } else {
                            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        }
                        item.setLayoutParams(layoutParams);
                        item.fillPreviewOptions();
                    }
                }
            }

            @Override
            public int getItemCount() {
                return currentShownRecentItems.size();
            }

            @Override
            public int getItemViewType(int position) {
                return currentShownRecentItems.get(position).viewType;
            }
        }
    }

    private static class FakeDelegate implements ChatActivityEnterView.ChatActivityEnterViewDelegate {
        @Override
        public void onMessageSend(CharSequence message, boolean notify, int scheduleDate, long payStars) {

        }

        @Override
        public void needSendTyping() {

        }

        @Override
        public void onTextChanged(CharSequence text, boolean bigChange, boolean fromDraft) {

        }

        @Override
        public void onTextSelectionChanged(int start, int end) {

        }

        @Override
        public void onTextSpansChanged(CharSequence text) {

        }

        @Override
        public void onAttachButtonHidden() {

        }

        @Override
        public void onAttachButtonShow() {

        }

        @Override
        public void onWindowSizeChanged(int size) {

        }

        @Override
        public void onStickersTab(boolean opened) {

        }

        @Override
        public void onMessageEditEnd(boolean loading) {

        }

        @Override
        public void didPressAttachButton() {

        }

        @Override
        public void needStartRecordVideo(int state, boolean notify, int scheduleDate, int ttl, long effectId, long stars) {

        }

        @Override
        public void toggleVideoRecordingPause() {

        }

        @Override
        public boolean isVideoRecordingPaused() {
            return false;
        }

        @Override
        public void needChangeVideoPreviewState(int state, float seekProgress) {

        }

        @Override
        public void onSwitchRecordMode(boolean video) {

        }

        @Override
        public void onPreAudioVideoRecord() {

        }

        @Override
        public void needStartRecordAudio(int state) {

        }

        @Override
        public void needShowMediaBanHint() {

        }

        @Override
        public void onStickersExpandedChange() {

        }

        @Override
        public void onUpdateSlowModeButton(View button, boolean show, CharSequence time) {

        }

        @Override
        public void onSendLongClick() {

        }

        @Override
        public void onAudioVideoInterfaceUpdated() {

        }
    }

    public static class ContextMenuPreviewItem extends AdapterWithDiffUtils.Item {

        public CharSequence name;
        public Integer icon;
        public boolean expandable;

        public static int ITEM = 0;
        public static int GAP_SHADOW = 1;
        public static int SHORTCUTS = 2;

        public ContextMenuPreviewItem(int viewType) {
            super(viewType, true);
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ContextMenuPreviewItem item)) {
                return false;
            }
            if (viewType != item.viewType) {
                return false;
            }
            if (viewType == ContextMenuPreviewItem.SHORTCUTS) {
                return true;
            }
            if (viewType == ContextMenuPreviewItem.GAP_SHADOW) {
                return true;
            }
            return Objects.equals(name, item.name);
        }
    }
}
