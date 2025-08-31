package it.octogram.android.app.ui.bottomsheets;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Components.BottomSheetWithRecyclerListView;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.ItemOptions;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ListView.AdapterWithDiffUtils;
import org.telegram.ui.Components.Premium.boosts.cells.selector.SelectorBtnCell;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.Stories.recorder.ButtonWithCounterView;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import it.octogram.android.app.ui.cells.MessagesPreviewDetailsPageCell;
import it.octogram.android.utils.OctoLogging;
import it.octogram.android.utils.OctoUtils;
import it.octogram.android.utils.account.UserAccountInfoController;

public class MessageDetailsBottomSheet extends BottomSheetWithRecyclerListView {

    private final static String TAG = "MessageDetailsBottomSheet";

    private ListAdapter adapter;

    private final ButtonWithCounterView actionButton;
    private final SelectorBtnCell buttonContainer;

    private final ArrayList<ItemInner> items = new ArrayList<>();

    private final MessageObject messageObject;
    private TLRPC.Chat fromChat;
    private TLRPC.User fromUser;
    private TLRPC.User fromForwardedUser;
    private TLRPC.User fromRepliedUser;
    private UserAccountInfoController.UserAccountInfo fromForwardedUserInfo;
    private UserAccountInfoController.UserAccountInfo fromRepliedUserInfo;
    private UserAccountInfoController.UserAccountInfo fromUserInfo;
    private UserAccountInfoController.UserAccountInfo fromChatInfo;

    private final ArrayList<Integer> expandedRows = new ArrayList<>();

    public MessageDetailsBottomSheet(BaseFragment fragment, MessageObject messageObject) {
        super(fragment.getContext(), fragment, false, false, false, false, ActionBarType.SLIDING, fragment.getResourceProvider());
        this.messageObject = messageObject;
        setSlidingActionBar();

        if (messageObject.getChatId() != 0) {
            fromChat = getMessagesController().getChat(messageObject.getChatId());
            fromChatInfo = UserAccountInfoController.getDcInfo(fromChat);
        }
        if (messageObject.messageOwner.from_id instanceof TLRPC.TL_peerUser) {
            fromUser = getMessagesController().getUser(messageObject.messageOwner.from_id.user_id);
            fromUserInfo = UserAccountInfoController.getDcInfo(fromUser);
        }
        if (messageObject.messageOwner.fwd_from != null && messageObject.messageOwner.fwd_from.from_id instanceof TLRPC.TL_peerUser) {
            fromForwardedUser = getMessagesController().getUser(messageObject.messageOwner.fwd_from.from_id.user_id);
            fromForwardedUserInfo = UserAccountInfoController.getDcInfo(fromForwardedUser);
        } else if (messageObject.messageOwner.fwd_from != null && !TextUtils.isEmpty(messageObject.messageOwner.fwd_from.from_name)) {
            fromForwardedUser = new TLRPC.User() {
            };
            fromForwardedUser.first_name = messageObject.messageOwner.fwd_from.from_name;
        }
        if (messageObject.replyMessageObject != null && messageObject.replyMessageObject.messageOwner.from_id instanceof TLRPC.TL_peerUser) {
            fromRepliedUser = getMessagesController().getUser(messageObject.replyMessageObject.messageOwner.from_id.user_id);
            fromRepliedUserInfo = UserAccountInfoController.getDcInfo(fromRepliedUser);
        }

        topPadding = 0.35f;
        fixNavigationBar();
        setShowHandle(true);

        DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setSupportsChangeAnimations(false);
        itemAnimator.setDelayAnimations(false);
        itemAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        itemAnimator.setDurations(350);
        recyclerListView.setItemAnimator(itemAnimator);
        recyclerListView.setOnItemClickListener((view, position, x, y) -> {
            if (position < 0 || position > items.size()) return;
            ItemInner item = items.get(position - 1);
            if (item.viewType == VIEW_TYPE_EXPANDABLE) {
                if (item.expandableId == 35) {
                    dismiss();
                    new MessageJsonBottomSheet(fragment, messageObject).show();
                    return;
                }

                expandedRows.add(item.expandableId);
                updateRows(true, item.expandableId);
            } else if (item.viewType == VIEW_TYPE_ITEM) {
                ItemOptions options = ItemOptions.makeOptions(container, view);
                options.add(R.drawable.msg_copy, getString(R.string.Copy), () -> {
                    AndroidUtilities.addToClipboard(item.completeText);
                    BulletinFactory.of(container, null).createCopyBulletin(getString(R.string.TextCopied)).show();
                });
                options.addIf(item.isStorageSection, R.drawable.msg_openin, getString(R.string.OpenPath), () -> {
                    Uri uri = Uri.parse(item.completeText.toString());
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, "resource/folder");
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);

                    try {
                        LaunchActivity.instance.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        BulletinFactory.of(container, null).createErrorBulletin(getString(R.string.ErrorOccurred)).show();
                    }
                });
                if (item.hasBeenTrimmed) {
                    options.addGap();
                    options.addText(item.completeText, 13);
                }
                options.show();
            }
        });

        updateRows(false);

        buttonContainer = new SelectorBtnCell(getContext(), resourcesProvider, null);
        buttonContainer.setClickable(true);
        buttonContainer.setOrientation(LinearLayout.VERTICAL);
        buttonContainer.setPadding(dp(10), dp(10), dp(10), dp(10));
        buttonContainer.setBackgroundColor(Theme.getColor(Theme.key_dialogBackground, resourcesProvider));
        actionButton = new ButtonWithCounterView(getContext(), resourcesProvider);
        actionButton.setText(getString(R.string.Close), false);
        actionButton.setOnClickListener(v -> dismiss());
        buttonContainer.addView(actionButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL));
        containerView.addView(buttonContainer, LayoutHelper.createFrameMarginPx(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, backgroundPaddingLeft, 0, backgroundPaddingLeft, 0));

        recyclerListView.setPadding(backgroundPaddingLeft, 0, backgroundPaddingLeft, dp(68));
    }

    private MessagesController getMessagesController() {
        return MessagesController.getInstance(UserConfig.selectedAccount);
    }

    @Override
    protected CharSequence getTitle() {
        return getString(R.string.MessageDetails);
    }

    @Override
    protected RecyclerListView.SelectionAdapter createAdapter(RecyclerListView listView) {
        return adapter = new ListAdapter(getContext());
    }

    public void updateRows(boolean animated) {
        updateRows(animated, -1);
    }

    public void updateRows(boolean animated, int justExpandedTo) {
        ArrayList<ItemInner> oldItems = new ArrayList<>(items);
        items.clear();

        items.add(ItemInner.asShadow());
        items.add(ItemInner.asPreview());

        fillMessageRelatedItems(items);
        items.add(ItemInner.asShadow());

        fillForwardedMessageItems(items);

        int addedFiles = fillFilesRelatedItems(items);
        if (addedFiles > 0) {
            items.add(ItemInner.asShadow());
        }

        int needScrollTo = -1;

        boolean hasReply = messageObject.replyMessageObject != null;
        if (!expandedRows.contains(2) && hasReply) {
            items.add(ItemInner.asExpandable(2, getString(R.string.ExpandReplyDetails)));
        } else if (expandedRows.contains(2) && hasReply) {
            int addedReplyItems = fillRepliedMessageItems(items);
            if (addedReplyItems > 0) {
                items.add(ItemInner.asShadow());
                if (justExpandedTo == 0) needScrollTo = items.size() - 1;
            }
        }

        boolean hasChat = fromChat != null || fromUser != null;
        if (!expandedRows.contains(0) && hasChat) {
            items.add(ItemInner.asExpandable(0, getString(R.string.ExpandChatDetails)));
        } else if (expandedRows.contains(0) && hasChat) {
            int addedChatItems = fillChatRelatedItems(items);
            if (addedChatItems > 0) {
                items.add(ItemInner.asShadow());
                if (justExpandedTo == 0) needScrollTo = items.size() - 1;
            }
        }

        boolean hasUser = fromUser != null || fromRepliedUser != null;
        if (!expandedRows.contains(1) && hasUser) {
            items.add(ItemInner.asExpandable(1, getString(R.string.ExpandUserDetails)));
        } else if (expandedRows.contains(1) && hasUser) {
            int addedUserItems = fillUserRelatedItems(items);
            if (addedUserItems > 0) {
                items.add(ItemInner.asShadow());
                if (justExpandedTo == 1) needScrollTo = items.size() - 1;
            }
        }

        items.add(ItemInner.asExpandable(35, R.drawable.input_bot1, "Show JSON"));

        for (int i = 1; i < items.size(); i++) {
            ItemInner previousItem = items.get(i-1);
            ItemInner currentItem = items.get(i);
            if (currentItem.viewType == VIEW_TYPE_SHADOW && previousItem.viewType == VIEW_TYPE_ITEM) {
                previousItem.useDivider = false;
            }
            if (currentItem.viewType == VIEW_TYPE_EXPANDABLE && i == items.size() - 1) {
                currentItem.useDivider = false;
            }
        }

        if (adapter != null) {
            if (animated) {
                adapter.setItems(oldItems, items);
            } else {
                adapter.notifyDataSetChanged();
            }

            if (needScrollTo != -1) {
                int finalNeedScrollTo = needScrollTo;
                AndroidUtilities.runOnUIThread(() -> recyclerListView.smoothScrollToPosition(finalNeedScrollTo), 200);
            }
        }
    }

    private int fillChatRelatedItems(ArrayList<ItemInner> items) {
        int initialNumber = items.size();
        if ((fromChat != null && fromUser != null && fromChat.id != fromUser.id) || (fromChat != null && fromUser == null)) {
            items.add(ItemInner.asMiniHeader(fromChat.broadcast ? getString(R.string.AccDescrChannel) : getString(R.string.AccDescrGroup)));
            items.add(ItemInner.asItem(R.drawable.msg_message, getString(fromChat.broadcast ? R.string.EnterChannelName : R.string.GroupName), fromChat.title).withEmojiSupport());
            if (fromChat.username != null) {
                items.add(ItemInner.asItem(R.drawable.menu_username_change, getString(R.string.Username), "@" + fromChat.username));
            }
            if (fromChat.photo != null) {
                if (fromChatInfo != null && fromChatInfo.dcInfo != null) {
                    items.add(ItemInner.asItem(R.drawable.datacenter_status, getString(R.string.FileDC), fromChatInfo.dcInfo.getDcId() + " - " + fromChatInfo.dcInfo.getDcName()));
                }
            }
            if (fromChatInfo != null) {
                items.add(ItemInner.asItem(R.drawable.menu_hashtag, "ID", String.valueOf(fromChatInfo.userId)));
            }
        }
        return items.size() - initialNumber;
    }

    private void fillForwardedMessageItems(ArrayList<ItemInner> items) {
        if (messageObject.messageOwner.fwd_from != null) {
            items.add(ItemInner.asMiniHeader(getString(R.string.ForwardedMessage)));

            long date = (long) messageObject.messageOwner.fwd_from.date * 1000L;
            items.add(ItemInner.asItem(R.drawable.menu_sort_date, getString(R.string.ForwardDate), parseDate((int) (date / 1000))).withEmojiSupport());

            if (!TextUtils.isEmpty(messageObject.messageOwner.message)) {
                items.add(ItemInner.asItem(R.drawable.msg_message, getString(R.string.ForwardText), messageObject.messageOwner.message).withEmojiSupport());
                items.add(ItemInner.asItem(R.drawable.msg_info, getString(R.string.ForwardTextLength), String.valueOf(messageObject.messageOwner.message.length())));
            }

            if (fromForwardedUser != null) {
                items.add(ItemInner.asShadow());
                items.add(ItemInner.asMiniHeader(getString(R.string.ForwardedFrom)));

                if (fromForwardedUser.first_name != null) {
                    String full_name = fromForwardedUser.first_name;
                    if (fromForwardedUser.last_name != null) {
                        full_name += " " + fromForwardedUser.last_name;
                    }
                    items.add(ItemInner.asItem(R.drawable.msg_message, getString(R.string.FullName), full_name).withEmojiSupport());
                }

                if (fromForwardedUser.id != 0) {
                    if (!TextUtils.isEmpty(fromForwardedUser.username)) {
                        items.add(ItemInner.asItem(R.drawable.menu_username_change, getString(R.string.Username), "@" + fromForwardedUser.username));
                    }
                    if (fromForwardedUser.photo != null && fromForwardedUserInfo != null && fromForwardedUserInfo.dcInfo != null) {
                        items.add(ItemInner.asItem(R.drawable.datacenter_status, getString(R.string.FileDC), fromForwardedUserInfo.dcInfo.getDcName()));
                    }
                    if (fromForwardedUserInfo != null) {
                        items.add(ItemInner.asItem(R.drawable.menu_hashtag, getString(R.string.ChatId), String.valueOf(fromForwardedUserInfo.userId)));
                    }
                }
            }
        }
    }
    private int fillUserRelatedItems(ArrayList<ItemInner> items) {
        int initialNumber = items.size();
        OctoLogging.d(TAG, "fillUserRelatedItems: fromUser = " + fromUser + ", fromRepliedUser = " + fromRepliedUser);

        fillSingleUserItems(items, fromUser, fromUserInfo, getString(R.string.UserInfo));
        fillSingleUserItems(items, fromRepliedUser, fromRepliedUserInfo, getString(R.string.RepliedUser));

        return items.size() - initialNumber;
    }

    private void fillSingleUserItems(ArrayList<ItemInner> items, TLRPC.User user, UserAccountInfoController.UserAccountInfo userInfo, String headerTitle) {
        if (user == null) return;

        items.add(ItemInner.asMiniHeader(headerTitle));

        String fullName = user.first_name != null ? user.first_name : "";
        if (user.last_name != null) {
            fullName += " " + user.last_name;
        }
        items.add(ItemInner.asItem(R.drawable.msg_message, getString(R.string.FullName), fullName).withEmojiSupport());

        if (!TextUtils.isEmpty(user.username)) {
            items.add(ItemInner.asItem(R.drawable.menu_username_change, getString(R.string.Username), "@" + user.username));
        }

        if (user.photo != null && userInfo != null && userInfo.dcInfo != null) {
            items.add(ItemInner.asItem(R.drawable.datacenter_status, getString(R.string.FileDC), userInfo.dcInfo.getDcId() + " - " + userInfo.dcInfo.getDcName()));
        }

        if (userInfo != null) {
            items.add(ItemInner.asItem(R.drawable.menu_hashtag, "ID", String.valueOf(userInfo.userId)));
        }

        if (user.bot) {
            items.add(ItemInner.asItem(R.drawable.msg_info, getString(R.string.IsBot), getString(R.string.CheckPhoneNumberYes)));
        }
    }

    private void fillMessageRelatedItems(ArrayList<ItemInner> items) {
        items.add(ItemInner.asMiniHeader(getString(R.string.Message)));
        items.add(ItemInner.asItem(R.drawable.menu_hashtag, "ID", String.valueOf(messageObject.getId())));
        if (messageObject.scheduled) {
            items.add(ItemInner.asItem(R.drawable.menu_sort_date, getString(R.string.MessageScheduledDate), messageObject.messageOwner.date == 0x7ffffffe ? getString(R.string.MessageScheduledWhenOnline) : parseDate(messageObject.messageOwner.date)));
        } else {
            items.add(ItemInner.asItem(R.drawable.menu_sort_date, getString(R.string.MessageDate), parseDate(messageObject.messageOwner.date)));
        }
        if (messageObject.messageOwner.edit_date != 0) {
            items.add(ItemInner.asItem(R.drawable.msg_edit, getString(R.string.EditedDate), parseDate(messageObject.messageOwner.edit_date)));
        }
        if (messageObject.messageOwner.edit_hide) {
            items.add(ItemInner.asItem(R.drawable.msg_edit, getString(R.string.EditHidden), getString(R.string.CheckPhoneNumberYes)));
        }
        if (messageObject.messageOwner.forwards > 0) {
            items.add(ItemInner.asItem(R.drawable.msg_forward, getString(R.string.ForwardsCount), String.valueOf(messageObject.messageOwner.forwards)));
        }

        if (!TextUtils.isEmpty(messageObject.messageOwner.message)) {
            items.add(ItemInner.asItem(R.drawable.msg_info, getString(R.string.MessageTextLength), String.valueOf(messageObject.messageOwner.message.length())));
        }
    }

    private int fillFilesRelatedItems(ArrayList<ItemInner> items) {
        int initialNumber = items.size();
        if (messageObject.messageOwner.media != null && !(messageObject.messageOwner.media instanceof TLRPC.TL_messageMediaWebPage)) {
            items.add(ItemInner.asMiniHeader(getString(R.string.ChatDocument)));
            ArrayList<ItemInner> tempVideoDetails = new ArrayList<>();
            boolean isTempDetailsForPhotos = false;
            String fileName;
            if (messageObject.messageOwner.media.document != null) {
                fileName = "";
                if (TextUtils.isEmpty(messageObject.messageOwner.media.document.file_name)) {
                    for (int a = 0; a < messageObject.messageOwner.media.document.attributes.size(); a++) {
                        if (messageObject.messageOwner.media.document.attributes.get(a) instanceof TLRPC.TL_documentAttributeFilename) {
                            fileName = messageObject.messageOwner.media.document.attributes.get(a).file_name;
                        }
                    }
                } else {
                    fileName = messageObject.messageOwner.media.document.file_name;
                }
                if (!TextUtils.isEmpty(fileName)) {
                    items.add(ItemInner.asItem(R.drawable.msg_view_file, getString(R.string.FileName), fileName));
                }
                items.add(ItemInner.asItem(R.drawable.msg_sendfile, getString(R.string.MimeType), messageObject.messageOwner.media.document.mime_type));
                String durationString = "";
                String emojiAssociated = "";
                if (messageObject.messageOwner.media instanceof TLRPC.TL_messageMediaDocument) {
                    for (int a = 0; a < messageObject.messageOwner.media.document.attributes.size(); a++) {
                        if (messageObject.messageOwner.media.document.attributes.get(a) instanceof TLRPC.TL_documentAttributeAudio ||
                            messageObject.messageOwner.media.document.attributes.get(a) instanceof TLRPC.TL_documentAttributeVideo) {
                            int duration = (int) messageObject.messageOwner.media.document.attributes.get(a).duration;
                            durationString = AndroidUtilities.formatShortDuration(duration);
                        } else if (messageObject.messageOwner.media.document.attributes.get(a) instanceof TLRPC.TL_documentAttributeSticker) {
                            TLRPC.TL_documentAttributeSticker attribute;
                            attribute = (TLRPC.TL_documentAttributeSticker) messageObject.messageOwner.media.document.attributes.get(a);
                            Emoji.preloadEmoji(attribute.alt);
                            emojiAssociated = attribute.alt;
                        }

                        if (messageObject.messageOwner.media.document.attributes.get(a) instanceof TLRPC.TL_documentAttributeVideo attribute) {
                            if (attribute.duration > 0 && messageObject.messageOwner.media.document.size > 0) {
                                tempVideoDetails.add(ItemInner.asItem(R.drawable.msg_speed, getString(R.string.FileBitrate), OctoUtils.formatBitrate((double) messageObject.messageOwner.media.document.size / attribute.duration)));
                            }
                            if (attribute.w > 0 && attribute.h > 0) {
                                tempVideoDetails.add(ItemInner.asItem(R.drawable.menu_quality_hd, getString(R.string.FileResolution), String.format(Locale.US, "%sx%s", attribute.w, attribute.h)));
                            }
                            if (attribute.video_codec != null && !attribute.video_codec.isEmpty()) {
                                tempVideoDetails.add(ItemInner.asItem(R.drawable.msg_minvideo, getString(R.string.FileCodec), attribute.video_codec));
                            }
                        } else if (messageObject.messageOwner.media.document.attributes.get(a) instanceof TLRPC.TL_documentAttributeImageSize attribute) {
                            if (attribute.w > 0 && attribute.h > 0) {
                                tempVideoDetails.add(ItemInner.asItem(R.drawable.menu_quality_hd, getString(R.string.FileResolution), String.format(Locale.US, "%sx%s", attribute.w, attribute.h)));
                            }
                            isTempDetailsForPhotos = true;
                        }
                    }
                }

                if (!TextUtils.isEmpty(durationString)) {
                    tempVideoDetails.add(ItemInner.asItem(R.drawable.menu_videocall, getString(R.string.UserRestrictionsDuration), durationString));
                }
                if (!TextUtils.isEmpty(emojiAssociated)) {
                    items.add(ItemInner.asItem(R.drawable.emoji_love, getString(R.string.AssociatedEmoji), emojiAssociated).withEmojiSupport());
                }
            }

            String filePath = messageObject.messageOwner.attachPath;
            if (!TextUtils.isEmpty(filePath)) {
                File temp = new File(filePath);
                if (!temp.exists()) {
                    filePath = null;
                }
            }
            if (TextUtils.isEmpty(filePath)) {
                filePath = FileLoader.getInstance(currentAccount).getPathToMessage(messageObject.messageOwner).toString();
                File temp = new File(filePath);
                if (!temp.exists()) {
                    filePath = null;
                }
            }
            if (TextUtils.isEmpty(filePath)) {
                filePath = FileLoader.getInstance(currentAccount).getPathToAttach(messageObject.getDocument(), true).toString();
                File temp = new File(filePath);
                if (!temp.isFile()) {
                    filePath = null;
                }
            }
            if (filePath != null) {
                items.add(ItemInner.asItem(R.drawable.menu_storage_path, getString(R.string.FilePath), filePath).asStorageSection());
                int DC = -1;
                if (messageObject.messageOwner.media.photo != null && messageObject.messageOwner.media.photo.dc_id > 0) {
                    DC = messageObject.messageOwner.media.photo.dc_id;
                } else if (messageObject.messageOwner.media.document != null && messageObject.messageOwner.media.document.dc_id > 0) {
                    DC = messageObject.messageOwner.media.document.dc_id;
                }
                if (DC != -1) {
                    items.add(ItemInner.asItem(R.drawable.datacenter_status, getString(R.string.FileDC), String.valueOf(DC)));
                }
            }
            if (messageObject.getSize() != 0 || !TextUtils.isEmpty(filePath)) {
                long size = messageObject.getSize() != 0 ? messageObject.getSize() : new File(Objects.requireNonNull(filePath)).length();
                items.add(ItemInner.asItem(R.drawable.msg2_devices, getString(R.string.FileSize), AndroidUtilities.formatFileSize(size)));
            }

            if (!tempVideoDetails.isEmpty()) {
                items.add(ItemInner.asShadow());
                items.add(ItemInner.asMiniHeader(isTempDetailsForPhotos ? getString(R.string.AttachPhoto) : getString(R.string.AttachVideo)));
                items.addAll(tempVideoDetails);
            }
        }
        return items.size() - initialNumber;
    }

    private int fillRepliedMessageItems(ArrayList<ItemInner> items) {
        if (messageObject.replyMessageObject == null) return 0;

        items.add(ItemInner.asMiniHeader(getString(R.string.ReplyMessage)));
        items.add(ItemInner.asItem(R.drawable.menu_hashtag, "ID", String.valueOf(messageObject.replyMessageObject.getId())));
        items.add(ItemInner.asItem(R.drawable.msg_message, getString(R.string.MessageText), messageObject.replyMessageObject.messageOwner.message).withEmojiSupport());

        if (messageObject.replyMessageObject.messageOwner.from_id instanceof TLRPC.TL_peerUser) {
            TLRPC.User repliedUser = getMessagesController().getUser(messageObject.replyMessageObject.messageOwner.from_id.user_id);

            String fullName = repliedUser.first_name;
            if (repliedUser.last_name != null) fullName += " " + repliedUser.last_name;
            items.add(ItemInner.asShadow());
            items.add(ItemInner.asMiniHeader(getString(R.string.ReplyFrom)));
            items.add(ItemInner.asItem(R.drawable.msg_message, getString(R.string.FullName), fullName).withEmojiSupport());

            if (!TextUtils.isEmpty(repliedUser.username)) {
                items.add(ItemInner.asItem(R.drawable.menu_username_change, getString(R.string.Username), "@" + repliedUser.username));
            }
            if (repliedUser.photo != null) {
                items.add(ItemInner.asItem(R.drawable.datacenter_status, getString(R.string.FileDC), fromRepliedUserInfo.dcInfo.getDcName()));
            }
            items.add(ItemInner.asItem(R.drawable.menu_hashtag, "ID", String.valueOf(fromRepliedUserInfo.userId)));
        }

        return 1;
    }

    private String parseDate(int date) {
        long dateMs = (long) date * 1000;
        return formatString(R.string.formatDateAtTime, LocaleController.getInstance().getFormatterYear().format(new Date(dateMs)), LocaleController.getInstance().getFormatterDayWithSeconds().format(new Date(dateMs)));
    }

    @Override
    protected void onSmoothContainerViewLayout(float ty) {
        super.onSmoothContainerViewLayout(ty);
        buttonContainer.setTranslationY(-ty);
    }

    @Override
    protected boolean canDismissWithSwipe() {
        return !recyclerListView.canScrollVertically(-1);
    }

    private static final int VIEW_TYPE_MINI_HEADER = 1;
    private static final int VIEW_TYPE_ITEM = 2;
    private static final int VIEW_TYPE_SHADOW = 3;
    private static final int VIEW_TYPE_PREVIEW = 4;
    private static final int VIEW_TYPE_EXPANDABLE = 5;

    private static class ItemInner extends AdapterWithDiffUtils.Item {
        public ItemInner(int viewType) {
            super(viewType, false);
        }

        int expandableId;
        int icon;
        CharSequence title;
        CharSequence text;
        CharSequence completeText;
        boolean isStorageSection = false;
        boolean hasBeenTrimmed = false;
        boolean needEmojisSupport = false;
        boolean useDivider = true;

        public static ItemInner asPreview() {
            return new ItemInner(VIEW_TYPE_PREVIEW);
        }

        public static ItemInner asShadow() {
            return new ItemInner(VIEW_TYPE_SHADOW);
        }

        public static ItemInner asMiniHeader(String text) {
            ItemInner item = new ItemInner(VIEW_TYPE_MINI_HEADER);
            item.text = text;
            return item;
        }

        public static ItemInner asItem(int icon, CharSequence title, CharSequence text) {
            ItemInner item = new ItemInner(VIEW_TYPE_ITEM);
            item.icon = icon;
            item.title = title;
            item.completeText = text;

            CharSequence trimmed = OctoUtils.safeTrim(text, 15);
            item.text = trimmed;
            item.hasBeenTrimmed = !trimmed.equals(text);

            return item;
        }

        public static ItemInner asExpandable(int expandableId, CharSequence title) {
            return asExpandable(expandableId, R.drawable.msg_expand, title);
        }

        public static ItemInner asExpandable(int expandableId, int icon, CharSequence title) {
            ItemInner item = new ItemInner(VIEW_TYPE_EXPANDABLE);
            item.expandableId = expandableId;
            item.icon = icon;
            item.title = title;
            return item;
        }

        public ItemInner asStorageSection() {
            isStorageSection = true;
            text = getString(R.string.ClickToCopy);
            return this;
        }

        public ItemInner withEmojiSupport() {
            needEmojisSupport = true;
            return this;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof ItemInner other)) {
                return false;
            }
            if (other.viewType != viewType) {
                return false;
            }
            if (viewType == VIEW_TYPE_MINI_HEADER) {
                if (!TextUtils.equals(text, other.text)) {
                    return false;
                }
            }
            if (viewType == VIEW_TYPE_ITEM) {
                return false;
            }
            return true;
        }
    }

    private class ListAdapter extends AdapterWithDiffUtils {

        private final Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == VIEW_TYPE_ITEM || type == VIEW_TYPE_EXPANDABLE;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = null;
            switch (viewType) {
                case VIEW_TYPE_MINI_HEADER:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_EXPANDABLE:
                case VIEW_TYPE_ITEM:
                    view = new TextCell(mContext, 23, false, false, null);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case VIEW_TYPE_SHADOW:
                    view = new ShadowSectionCell(mContext);
                    break;
                case VIEW_TYPE_PREVIEW:
                    view = new MessagesPreviewDetailsPageCell(mContext, getBaseFragment().getParentLayout());
                    ((MessagesPreviewDetailsPageCell) view).setMessages(messageObject);
                    break;
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ItemInner item = items.get(position);
            if (item == null) {
                return;
            }
            switch (holder.getItemViewType()) {
                case VIEW_TYPE_MINI_HEADER: {
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    headerCell.setText(item.text);
                    break;
                }
                case VIEW_TYPE_ITEM: {
                    TextCell textCell = (TextCell) holder.itemView;
                    if (item.needEmojisSupport) {
                        textCell.setTextAndValueAndIcon(item.title, Emoji.replaceEmoji(item.text, textCell.textView.getPaint().getFontMetricsInt(), false), item.icon, item.useDivider);
                    } else {
                        textCell.setTextAndValueAndIcon(item.title, item.text, item.icon, item.useDivider);
                    }
                    break;
                }
                case VIEW_TYPE_EXPANDABLE:
                    TextCell textCell = (TextCell) holder.itemView;
                    textCell.setColors(Theme.key_windowBackgroundWhiteBlueText4, Theme.key_windowBackgroundWhiteBlueText4);
                    textCell.setTextAndIcon(item.title, item.icon, item.useDivider);
                    break;
                case VIEW_TYPE_SHADOW: {
                    holder.itemView.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundWhite));
                    break;
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position < 0 || position >= items.size()) {
                return VIEW_TYPE_MINI_HEADER;
            }
            ItemInner item = items.get(position);
            return item.viewType;
        }
    }
}