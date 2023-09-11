package it.octogram.android.preferences.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import it.octogram.android.Datacenter;
import it.octogram.android.preferences.ui.custom.DetailsPreviewMessages;
import it.octogram.android.preferences.ui.custom.TextDetailCellMultiline;
import it.octogram.android.utils.MessageStringHelper;
import it.octogram.android.utils.UserAccountInfoController;
import org.telegram.messenger.*;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.io.File;
import java.util.Date;

public class DetailsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {
    private int rowCount;
    private ListAdapter listAdapter;
    private RecyclerListView listView;

    private int detailsPreviewMessagesRow;
    private int detailsPreviewDividerRow;

    private int aboutInfoHeaderRow;
    private int nameUserHeaderRow;
    private int idUserHeaderRow;
    private int dcRow;
    private int usernameRow;
    private int aboutDividerRow;
    private int messageHeaderRow;
    private int messageIdRow;
    private int messageTextRow;
    private int messageTextLengthRow;
    private int messageForwardsRow;
    private int messageDateRow;
    private int messageDateEditedRow;
    private int messageDividerRow;

    private int forwardMessageHeaderRow;
    private int forwardMessageDateRow;
    private int forwardDividerRow;

    private int forwardUserHeaderRow;
    private int forwardUserNameRow;
    private int forwardUserUsernameRow;
    private int forwardUserDatacenterRow;
    private int forwardUserIdRow;

    private int repliedMessageHeaderRow;
    private int repliedMessageIdRow;
    private int repliedMessageTextRow;
    private int repliedMessageTextLengthRow;
    private int repliedMessageDateRow;
    private int repliedDividerRow;

    private int repliedUserHeaderRow;
    private int repliedUserNameRow;
    private int repliedUserUsernameRow;
    private int repliedUserDatacenterRow;
    private int repliedUserIdRow;

    private int groupHeaderRow;
    private int groupDatacenterRow;
    private int groupNameRow;
    private int groupIdRow;
    private int groupUsernameRow;
    private int groupDividerRow;

    private int fileHeaderRow;
    private int fileNameRow;
    private int filePathRow;
    private int fileMimeType;
    private int fileSizeRow;
    private int fileDuration;
    private int fileDCRow;
    private int fileEmojiRow;
    private int fileDividerRow;

    private String filePath;
    private String fileName;
    private String durationString;
    private String emoji;

    private final MessageObject messageObject;
    private TLRPC.Chat fromChat;
    private TLRPC.User fromUser;
    private TLRPC.User fromForwardedUser;
    private TLRPC.User fromRepliedUser;

    private UserAccountInfoController.UserAccountInfo fromForwardedUserInfo;
    private UserAccountInfoController.UserAccountInfo fromRepliedUserInfo;
    private UserAccountInfoController.UserAccountInfo fromUserInfo;
    private UserAccountInfoController.UserAccountInfo fromChatInfo;

    public DetailsActivity(MessageObject messageObject) {
        this.messageObject = messageObject;
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
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiLoaded);
        updateRowsId();
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiLoaded);
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString("MessageDetails", R.string.MessageDetails));
        actionBar.setAllowOverlayTitle(false);
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        listAdapter = new ListAdapter(context);
        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        listView = new RecyclerListView(context);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        listView.setVerticalScrollBarEnabled(false);
        listView.setAdapter(listAdapter);
        if (listView.getItemAnimator() != null) {
            ((DefaultItemAnimator) listView.getItemAnimator()).setDelayAnimations(false);
        }
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setOnItemClickListener((view, position) -> {
            if (view instanceof TextDetailCellMultiline) {
                TextDetailCellMultiline textDetailCell = (TextDetailCellMultiline) view;
                if (textDetailCell.haveSpoilers()) {
                    textDetailCell.revealSpoilers();
                } else if (!getMessagesController().isChatNoForwards(fromChat)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setItems(new CharSequence[]{LocaleController.getString("Copy", R.string.Copy)}, new int[]{R.drawable.msg_copy}, (dialogInterface, i) -> {
                        if (i == 0) {
                            AndroidUtilities.addToClipboard(textDetailCell.getText());
                            BulletinFactory.of(DetailsActivity.this).createCopyBulletin(LocaleController.getString("TextCopied", R.string.TextCopied)).show();
                        }
                    });
                    showDialog(builder.create());
                }
            }
        });
        listView.setOnItemLongClickListener((view, position) -> {
            if (view instanceof TextDetailCellMultiline) {
                if (getMessagesController().isChatNoForwards(fromChat)) {
                    if (fromChat != null && fromChat.broadcast) {
                        BulletinFactory.of(DetailsActivity.this).createSimpleBulletin(R.raw.ic_ban, LocaleController.getString("ForwardsRestrictedInfoChannel", R.string.ForwardsRestrictedInfoChannel)).show();
                    } else {
                        BulletinFactory.of(DetailsActivity.this).createSimpleBulletin(R.raw.ic_ban, LocaleController.getString("ForwardsRestrictedInfoGroup", R.string.ForwardsRestrictedInfoGroup)).show();
                    }
                } else {
                    TextDetailCellMultiline textDetailCell = (TextDetailCellMultiline) view;
                    AndroidUtilities.addToClipboard(textDetailCell.getText());
                    BulletinFactory.of(DetailsActivity.this).createCopyBulletin(LocaleController.getString("TextCopied", R.string.TextCopied)).show();
                }
                return true;
            }
            return false;
        });
        return fragmentView;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateRowsId() {
        rowCount = 0;
        aboutInfoHeaderRow = -1;
        nameUserHeaderRow = -1;
        idUserHeaderRow = -1;
        usernameRow = -1;
        dcRow = -1;
        aboutDividerRow = -1;
        messageHeaderRow = -1;
        messageIdRow = -1;
        messageTextRow = -1;
        messageTextLengthRow = -1;
        messageForwardsRow = -1;
        messageDateEditedRow = -1;
        messageDateRow = -1;
        messageDividerRow = -1;

        forwardMessageHeaderRow = -1;
        forwardMessageDateRow = -1;
        forwardDividerRow = -1;
        forwardUserHeaderRow = -1;
        forwardUserNameRow = -1;
        forwardUserUsernameRow = -1;
        forwardUserDatacenterRow = -1;
        forwardUserIdRow = -1;

        repliedMessageHeaderRow = -1;
        repliedMessageIdRow = -1;
        repliedMessageTextRow = -1;
        repliedMessageTextLengthRow = -1;
        repliedMessageDateRow = -1;
        repliedDividerRow = -1;
        repliedUserHeaderRow = -1;
        repliedUserNameRow = -1;
        repliedUserUsernameRow = -1;
        repliedUserDatacenterRow = -1;
        repliedUserIdRow = -1;

        groupHeaderRow = -1;
        groupDatacenterRow = -1;
        groupNameRow = -1;
        groupIdRow = -1;
        groupUsernameRow = -1;
        groupDividerRow = -1;

        fileHeaderRow = -1;
        fileNameRow = -1;
        fileMimeType = -1;
        filePathRow = -1;
        fileSizeRow = -1;
        fileDuration = -1;
        fileDCRow = -1;
        fileEmojiRow = -1;
        fileDividerRow = -1;

        detailsPreviewMessagesRow = rowCount++;
        detailsPreviewDividerRow = rowCount++;
        if ((fromChat != null && fromUser != null && fromChat.id != fromUser.id) || (fromChat != null && fromUser == null)) {
            groupHeaderRow = rowCount++;
            groupNameRow = rowCount++;
            if (fromChat.username != null) {
                groupUsernameRow = rowCount++;
            }
            if (fromChat.photo != null) {
                groupDatacenterRow = rowCount++;
            }
            groupIdRow = rowCount++;
            groupDividerRow = rowCount++;
        }

        if (fromUser != null) {
            aboutInfoHeaderRow = rowCount++;
            if (fromUser.first_name != null) {
                nameUserHeaderRow = rowCount++;
            }
            if (!TextUtils.isEmpty(fromUser.username)) {
                usernameRow = rowCount++;
            }
            if (fromUser.photo != null) {
                dcRow = rowCount++;
            }
            idUserHeaderRow = rowCount++;
            aboutDividerRow = rowCount++;
        }
        messageHeaderRow = rowCount++;
        messageIdRow = rowCount++;
        if (messageObject.messageOwner.fwd_from == null && !TextUtils.isEmpty(messageObject.messageOwner.message)) {
            messageTextRow = rowCount++;
            messageTextLengthRow = rowCount++;
        }
        if (messageObject.messageOwner.forwards > 0) {
            messageForwardsRow = rowCount++;
        }
        if (messageObject.messageOwner.edit_date != 0) {
            messageDateEditedRow = rowCount++;
        }

        messageDateRow = rowCount++;

        if (messageObject.messageOwner.fwd_from != null) {
            messageDividerRow = rowCount++;
            forwardMessageHeaderRow = rowCount++;
            if (!TextUtils.isEmpty(messageObject.messageOwner.message)) {
                messageTextRow = rowCount++;
                messageTextLengthRow = rowCount++;
            }
            forwardMessageDateRow = rowCount++;
            if (fromForwardedUser != null) {
                forwardDividerRow = rowCount++;
                forwardUserHeaderRow = rowCount++;
                if (fromForwardedUser.first_name != null) {
                    forwardUserNameRow = rowCount++;
                }
                if (fromForwardedUser.id != 0) {
                    if (!TextUtils.isEmpty(fromForwardedUser.username)) {
                        forwardUserUsernameRow = rowCount++;
                    }
                    if (fromForwardedUser.photo != null) {
                        forwardUserDatacenterRow = rowCount++;
                    }
                    forwardUserIdRow = rowCount++;
                }
            }
        }
        if (messageObject.messageOwner.replyMessage != null) {
            messageDividerRow = rowCount++;
            repliedMessageHeaderRow = rowCount++;
            repliedMessageIdRow = rowCount++;
            if (!TextUtils.isEmpty(messageObject.replyMessageObject.messageOwner.message)) {
                repliedMessageTextRow = rowCount++;
                repliedMessageTextLengthRow = rowCount++;
            }
            repliedMessageDateRow = rowCount++;
            repliedDividerRow = rowCount++;
            if (fromRepliedUser != null) {
                repliedUserHeaderRow = rowCount++;
                if (fromRepliedUser.first_name != null) {
                    repliedUserNameRow = rowCount++;
                }
                if (!TextUtils.isEmpty(fromRepliedUser.username)) {
                    repliedUserUsernameRow = rowCount++;
                }
                if (fromRepliedUser.photo != null) {
                    repliedUserDatacenterRow = rowCount++;
                }
                repliedUserIdRow = rowCount++;
            }
        }
        if (messageObject.messageOwner.media != null && !(messageObject.messageOwner.media instanceof TLRPC.TL_messageMediaWebPage)) {
            fileDividerRow = rowCount++;
            fileHeaderRow = rowCount++;
            if (messageObject.messageOwner.media.document != null) {
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
                    fileNameRow = rowCount++;
                }
                fileMimeType = rowCount++;
                if (messageObject.messageOwner.media instanceof TLRPC.TL_messageMediaDocument) {
                    for (int a = 0; a < messageObject.messageOwner.media.document.attributes.size(); a++) {
                        if (messageObject.messageOwner.media.document.attributes.get(a) instanceof TLRPC.TL_documentAttributeAudio ||
                                messageObject.messageOwner.media.document.attributes.get(a) instanceof TLRPC.TL_documentAttributeVideo) {
                            int duration = (int) messageObject.messageOwner.media.document.attributes.get(a).duration;
                            durationString = AndroidUtilities.formatShortDuration(duration);
                            fileDuration = rowCount++;
                        } else if (messageObject.messageOwner.media.document.attributes.get(a) instanceof TLRPC.TL_documentAttributeSticker) {
                            TLRPC.TL_documentAttributeSticker attribute = (TLRPC.TL_documentAttributeSticker) messageObject.messageOwner.media.document.attributes.get(a);
                            Emoji.preloadEmoji(attribute.alt);
                            emoji = attribute.alt;
                            fileEmojiRow = rowCount++;
                        }
                    }
                }
            }

            filePath = messageObject.messageOwner.attachPath;
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
                fileDCRow = rowCount++;
                filePathRow = rowCount++;
            } else if (fileNameRow == -1) {
                fileDividerRow = -1;
                fileHeaderRow = -1;
                rowCount -= 2;
            }
            if (messageObject.getSize() != 0 || !TextUtils.isEmpty(filePath)) {
                fileSizeRow = rowCount++;
            }
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {
        private final Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 1:
                    holder.itemView.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                case 2:
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == aboutInfoHeaderRow) {
                        headerCell.setText(fromUser.bot ? LocaleController.getString("BotInfo", R.string.BotInfo) : LocaleController.getString("UserInfo", R.string.UserInfo));
                    } else if (position == messageHeaderRow) {
                        headerCell.setText(LocaleController.getString("Message", R.string.Message));
                    } else if (position == forwardMessageHeaderRow) {
                        headerCell.setText(LocaleController.getString("ForwardedMessage", R.string.ForwardedMessage));
                    } else if (position == forwardUserHeaderRow) {
                        headerCell.setText(LocaleController.getString("ForwardedFrom", R.string.ForwardedFrom));
                    } else if (position == repliedMessageHeaderRow) {
                        headerCell.setText(LocaleController.getString("MessageInReply", R.string.MessageInReply));
                    } else if (position == repliedUserHeaderRow) {
                        headerCell.setText(LocaleController.getString("InReplyTo", R.string.InReplyTo));
                    } else if (position == groupHeaderRow) {
                        headerCell.setText(fromChat != null && fromChat.broadcast ? LocaleController.getString("AccDescrChannel", R.string.AccDescrChannel) : LocaleController.getString("AccDescrGroup", R.string.AccDescrGroup));
                    } else if (position == fileHeaderRow) {
                        headerCell.setText(LocaleController.getString("ChatDocument", R.string.ChatDocument));
                    }
                    break;
                case 3:
                    TextDetailCellMultiline textDetailCell = (TextDetailCellMultiline) holder.itemView;
                    if (position == idUserHeaderRow) {
                        textDetailCell.setTextAndValue(String.valueOf(fromUserInfo.userId), "ID", false);
                    } else if (position == nameUserHeaderRow) {
                        String full_name = fromUser.first_name;
                        if (fromUser.last_name != null) {
                            full_name += " " + fromUser.last_name;
                        }
                        textDetailCell.setTextWithEmojiAndValue(full_name, LocaleController.getString("FullName", R.string.FullName), true);
                    } else if (position == usernameRow) {
                        textDetailCell.setTextAndValue("@" + UserObject.getPublicUsername(fromUser), LocaleController.getString("Username", R.string.Username), true);
                    } else if (position == messageIdRow) {
                        textDetailCell.setTextAndValue(String.valueOf(messageObject.getId()), "ID", true);
                    } else if (position == messageTextRow) {
                        CharSequence message = messageObject.messageOwner.message;
                        message = MessageStringHelper.getSpannableString(message.toString(), messageObject.messageOwner.entities, true);
                        textDetailCell.setTextWithAnimatedEmojiAndValue(MessageStringHelper.getUrlNoUnderlineText(message), messageObject.messageOwner.entities, LocaleController.getString("MessageText", R.string.MessageText), true);
                    } else if (position == messageTextLengthRow) {
                        textDetailCell.setTextAndValue(String.valueOf(messageObject.messageOwner.message.length()), LocaleController.getString("MessageTextLength", R.string.MessageTextLength), true);
                    } else if (position == messageDateRow) {
                        long date = (long) messageObject.messageOwner.date * 1000;
                        String title = messageObject.scheduled ? LocaleController.getString("MessageScheduledDate", R.string.MessageScheduledDate) : LocaleController.getString("MessageDate", R.string.MessageDate);
                        textDetailCell.setTextAndValue(messageObject.messageOwner.date == 0x7ffffffe ? LocaleController.getString("MessageScheduledWhenOnline", R.string.MessageScheduledWhenOnline) : LocaleController.formatString("formatDateAtTime", R.string.formatDateAtTime, LocaleController.getInstance().formatterYear.format(new Date(date)), LocaleController.getInstance().formatterDayWithSeconds.format(new Date(date))), title, false);
                    } else if (position == forwardMessageDateRow) {
                        long date = (long) messageObject.messageOwner.fwd_from.date * 1000;
                        String title = messageObject.scheduled ? LocaleController.getString("MessageScheduledDate", R.string.MessageScheduledDate) : LocaleController.getString("MessageDate", R.string.MessageDate);
                        textDetailCell.setTextAndValue(messageObject.messageOwner.fwd_from.date == 0x7ffffffe ? LocaleController.getString("MessageScheduledWhenOnline", R.string.MessageScheduledWhenOnline) : LocaleController.formatString("formatDateAtTime", R.string.formatDateAtTime, LocaleController.getInstance().formatterYear.format(new Date(date)), LocaleController.getInstance().formatterDayWithSeconds.format(new Date(date))), title, false);
                    } else if (position == forwardUserNameRow) {
                        String full_name = fromForwardedUser.first_name;
                        if (fromForwardedUser.last_name != null) {
                            full_name += " " + fromForwardedUser.last_name;
                        }
                        textDetailCell.setTextWithEmojiAndValue(full_name, LocaleController.getString("FullName", R.string.FullName), fromForwardedUser.id != 0);
                    } else if (position == forwardUserUsernameRow) {
                        textDetailCell.setTextAndValue("@" + UserObject.getPublicUsername(fromForwardedUser), LocaleController.getString("Username", R.string.Username), true);
                    } else if (position == forwardUserIdRow) {
                        textDetailCell.setTextAndValue(String.valueOf(fromForwardedUserInfo.userId), "ID", false);
                    } else if (position == repliedMessageTextRow) {
                        CharSequence message = messageObject.replyMessageObject.messageOwner.message;
                        message = MessageStringHelper.getSpannableString(message.toString(), messageObject.replyMessageObject.messageOwner.entities, true);
                        textDetailCell.setTextWithAnimatedEmojiAndValue(MessageStringHelper.getUrlNoUnderlineText(message), messageObject.replyMessageObject.messageOwner.entities, LocaleController.getString("MessageText", R.string.MessageText), true);
                    } else if (position == repliedMessageTextLengthRow) {
                        textDetailCell.setTextAndValue(String.valueOf(messageObject.replyMessageObject.messageOwner.message.length()), LocaleController.getString("MessageTextLength", R.string.MessageTextLength), true);
                    } else if (position == repliedMessageIdRow) {
                        textDetailCell.setTextAndValue(String.valueOf(messageObject.replyMessageObject.messageOwner.id), "ID", true);
                    } else if (position == repliedMessageDateRow) {
                        long date = (long) messageObject.replyMessageObject.messageOwner.date * 1000;
                        String title = messageObject.scheduled ? LocaleController.getString("MessageScheduledDate", R.string.MessageScheduledDate) : LocaleController.getString("MessageDate", R.string.MessageDate);
                        textDetailCell.setTextAndValue(messageObject.replyMessageObject.messageOwner.date == 0x7ffffffe ? LocaleController.getString("MessageScheduledWhenOnline", R.string.MessageScheduledWhenOnline) : LocaleController.formatString("formatDateAtTime", R.string.formatDateAtTime, LocaleController.getInstance().formatterYear.format(new Date(date)), LocaleController.getInstance().formatterDayWithSeconds.format(new Date(date))), title, false);
                    } else if (position == repliedUserIdRow) {
                        textDetailCell.setTextAndValue(String.valueOf(fromRepliedUserInfo.userId), "ID", false);
                    } else if (position == repliedUserNameRow) {
                        String full_name = fromRepliedUser.first_name;
                        if (fromRepliedUser.last_name != null) {
                            full_name += " " + fromRepliedUser.last_name;
                        }
                        textDetailCell.setTextWithEmojiAndValue(full_name, LocaleController.getString("FullName", R.string.FullName), true);
                    } else if (position == repliedUserUsernameRow) {
                        textDetailCell.setTextAndValue("@" + UserObject.getPublicUsername(fromRepliedUser), LocaleController.getString("Username", R.string.Username), true);
                    } else if (position == groupNameRow) {
                        if (fromChat.broadcast) {
                            textDetailCell.setTextWithEmojiAndValue(fromChat.title, LocaleController.getString("EnterChannelName", R.string.EnterChannelName), true);
                        } else {
                            textDetailCell.setTextWithEmojiAndValue(fromChat.title, LocaleController.getString("GroupName", R.string.GroupName), true);
                        }
                    } else if (position == groupUsernameRow) {
                        textDetailCell.setTextAndValue("@" + fromChat.username, LocaleController.getString("Username", R.string.Username), true);
                    } else if (position == groupIdRow) {
                        textDetailCell.setTextAndValue(String.valueOf(fromChatInfo.userId), "ID", false);
                    } else if (position == fileNameRow) {
                        textDetailCell.setTextAndValue(fileName, LocaleController.getString("FileName", R.string.FileName), true);
                    } else if (position == filePathRow) {
                        textDetailCell.setTextAndValue(filePath, LocaleController.getString("FilePath", R.string.FilePath), true);
                    } else if (position == fileSizeRow) {
                        long size = messageObject.getSize() != 0 ? messageObject.getSize() : new File(filePath).length();
                        textDetailCell.setTextAndValue(AndroidUtilities.formatFileSize(size), LocaleController.getString("FileSize", R.string.FileSize), true);
                    } else if (position == fileDCRow) {
                        int DC = -1;
                        if (messageObject.messageOwner.media.photo != null && messageObject.messageOwner.media.photo.dc_id > 0) {
                            DC = messageObject.messageOwner.media.photo.dc_id;
                        } else if (messageObject.messageOwner.media.document != null && messageObject.messageOwner.media.document.dc_id > 0) {
                            DC = messageObject.messageOwner.media.document.dc_id;
                        }
                        textDetailCell.setTextAndValue(Datacenter.getDcInfo(DC).dcName, LocaleController.getString("FileDC", R.string.FileDC), true);
                    } else if (position == messageForwardsRow) {
                        textDetailCell.setTextAndValue(String.valueOf(messageObject.messageOwner.forwards), LocaleController.getString("ForwardsNumber", R.string.ForwardsNumber), true);
                    } else if (position == messageDateEditedRow) {
                        long date = (long) messageObject.messageOwner.edit_date * 1000;
                        textDetailCell.setTextAndValue(LocaleController.formatString("formatDateAtTime", R.string.formatDateAtTime, LocaleController.getInstance().formatterYear.format(new Date(date)), LocaleController.getInstance().formatterDayWithSeconds.format(new Date(date))), LocaleController.getString("EditedDate", R.string.EditedDate), true);
                    } else if (position == fileDuration) {
                        textDetailCell.setTextAndValue(durationString, LocaleController.getString("UserRestrictionsDuration", R.string.UserRestrictionsDuration), true);
                    } else if (position == fileEmojiRow) {
                        textDetailCell.setTextWithEmojiAndValue(emoji, LocaleController.getString("AssociatedEmoji", R.string.AssociatedEmoji), true);
                    } else if (position == fileMimeType) {
                        textDetailCell.setTextAndValue(messageObject.messageOwner.media.document.mime_type, LocaleController.getString("MimeType", R.string.MimeType), true);
                    } else if (position == groupDatacenterRow) {
                        textDetailCell.setTextAndValue(fromChatInfo.dcInfo.dcName, LocaleController.getString("Datacenter", R.string.FileDC), true);
                    } else if (position == repliedUserDatacenterRow) {
                        textDetailCell.setTextAndValue(fromRepliedUserInfo.dcInfo.dcName, LocaleController.getString("Datacenter", R.string.FileDC), true);
                    } else if (position == forwardUserDatacenterRow) {
                        textDetailCell.setTextAndValue(fromForwardedUserInfo.dcInfo.dcName, LocaleController.getString("Datacenter", R.string.FileDC), true);
                    } else if (position == dcRow) {
                        textDetailCell.setTextAndValue(fromUserInfo.dcInfo.dcName, LocaleController.getString("Datacenter", R.string.FileDC), true);
                    }
                    break;
                case 4:
                    DetailsPreviewMessages previewMessagesCell = (DetailsPreviewMessages) holder.itemView;
                    previewMessagesCell.setMessages(messageObject);
                    break;
            }
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            int type = holder.getItemViewType();
            return type == 3;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case 2:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 3:
                    view = new TextDetailCellMultiline(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 4:
                    view = new DetailsPreviewMessages(mContext, parentLayout);
                    break;
                default:
                    view = new ShadowSectionCell(mContext);
                    break;
            }
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            return new RecyclerListView.Holder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == aboutDividerRow || position == messageDividerRow || position == forwardDividerRow ||
                    position == repliedDividerRow || position == groupDividerRow || position == fileDividerRow ||
                    position == detailsPreviewDividerRow) {
                return 1;
            } else if (position == aboutInfoHeaderRow || position == messageHeaderRow || position == forwardMessageHeaderRow ||
                    position == forwardUserHeaderRow || position == repliedMessageHeaderRow || position == repliedUserHeaderRow ||
                    position == groupHeaderRow || position == fileHeaderRow) {
                return 2;
            } else if (position == idUserHeaderRow || position == nameUserHeaderRow || position == usernameRow ||
                    position == messageIdRow || position == messageTextRow || position == messageDateRow ||
                    position == forwardMessageDateRow || position == forwardUserIdRow || position == forwardUserUsernameRow ||
                    position == forwardUserNameRow || position == repliedMessageTextRow || position == repliedMessageDateRow ||
                    position == repliedMessageIdRow || position == repliedUserNameRow || position == repliedUserUsernameRow ||
                    position == repliedUserIdRow || position == groupNameRow || position == groupIdRow || position == groupUsernameRow ||
                    position == fileNameRow || position == filePathRow || position == fileSizeRow || position == fileDCRow ||
                    position == messageForwardsRow || position == messageDateEditedRow || position == fileDuration ||
                    position == fileEmojiRow || position == fileMimeType || position == groupDatacenterRow ||
                    position == repliedUserDatacenterRow || position == forwardUserDatacenterRow || position == dcRow ||
                    position == messageTextLengthRow || position == repliedMessageTextLengthRow) {
                return 3;
            } else if (position == detailsPreviewMessagesRow) {
                return 4;
            }
            return 1;
        }
    }

    @Override
    public void didReceivedNotification(int id, int account, final Object... args) {
        if (id == NotificationCenter.emojiLoaded) {
            if (listView != null) {
                listView.invalidateViews();
            }
        }
    }
}
