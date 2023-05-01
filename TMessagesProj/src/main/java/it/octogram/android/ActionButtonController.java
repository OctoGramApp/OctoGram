package it.octogram.android;

import android.text.TextUtils;

import org.telegram.messenger.ChatObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;

import java.util.ArrayList;

public class ActionButtonController {

    private final ArrayList<String> data = new ArrayList<>();

    public void reset() {
        data.clear();
    }

    public void load(
            TLRPC.UserFull userInfo,
            TLRPC.User user,
            long userId,
            boolean isSelf,
            boolean isBot,
            boolean userBlocked,
            long chatId,
            TLRPC.Chat chat,
            TLRPC.ChatFull chatInfo,
            boolean existGroupCall,
            boolean canEdit,
            boolean canSearchMembers,
            int maxMegaGroupCount
    ) {
        if (userInfo != null) {
            if (isSelf) {
                data.add("camera");
                data.add("edit_name");
                data.add("logout");
            } else {
                boolean isTgUser = userId == 777000 || userId == 42777;
                data.add("send_message");
                if (isBot || isTgUser) {
                    if (isBot && !user.bot_nochats) {
                        data.add("add_bot");
                    }
                } else {
                    data.add("call");
                }

                if (userInfo.video_calls_available) {
                    data.add("video_call");
                }

                if (data.size() < 3) {
                    if (!isBot && !TextUtils.isEmpty(user.phone)) {
                        data.add("share_contact");
                    } else if (!TextUtils.isEmpty(user.username)) {
                        data.add("share");
                    }
                }

                if (!isTgUser) {
                    if (userBlocked) {
                        if (isBot) {
                            data.add("restart");
                        } else {
                            data.add("unblock");
                        }
                    } else {
                        if (isBot) {
                            data.add("stop");
                        } else {
                            data.add("block");
                        }
                    }
                }
            }
        } else if (chatId != 0 && chat != null) {
            boolean hasAdminRights = ChatObject.hasAdminRights(chat);
            boolean isCreator = chat.creator;
            boolean canLeave = !chat.creator && !chat.left && !chat.kicked;
            boolean discuss_available = chatInfo != null && chatInfo.linked_chat_id != 0;
            boolean canShare = !TextUtils.isEmpty(chat.username);
            boolean isGroup = !ChatObject.isChannelAndNotMegaGroup(chat);
            boolean canAddUsers = false;
            if (ChatObject.isChannel(chat)) {
                if (chatInfo != null && chat.megagroup && chatInfo.participants != null && !chatInfo.participants.participants.isEmpty()) {
                    if (!ChatObject.isNotInChat(chat) && ChatObject.canAddUsers(chat) && chatInfo.participants_count < maxMegaGroupCount) {
                        canAddUsers = true;
                    }
                }
            } else if (chatInfo != null) {
                if (!(chatInfo.participants instanceof TLRPC.TL_chatParticipantsForbidden)) {
                    if (ChatObject.canAddUsers(chat) || chat.default_banned_rights == null || !chat.default_banned_rights.invite_users) {
                        canAddUsers = true;
                    }
                }
            }

            if (chat.left && !chat.kicked) {
                data.add("join");
            } else if (canAddUsers) {
                data.add("add_user");
            }

            if (existGroupCall) {
                data.add("join_call");
            }

            if (isGroup) {
                if (data.size() <= 1 && canSearchMembers) {
                    data.add("search");
                }
                if (!canEdit || !hasAdminRights) {
                    data.add("info");
                }
            }

            if (canEdit && hasAdminRights) {
                data.add("edit");
            }

            if ((data.size() + (discuss_available ? 1 : 0) + (canLeave ? 1 : 0)) < 4 - (canShare ? 1 : 0) && !isCreator) {
                data.add("report");
            }

            if ((data.size() + (discuss_available ? 1 : 0) + (canLeave ? 1 : 0)) < 4 && canShare) {
                data.add("share");
            }

            if (discuss_available && data.size() < 3 + (canLeave ? 0 : 1)) {
                if (isGroup) {
                    data.add("open_channel");
                } else {
                    data.add("open_discussion");
                }
            }

            if (canLeave) {
                data.add("leave");
            }
        }
    }

    public boolean hasItem(String id) {
        return data.contains(id);
    }

    public int size() {
        return data.size();
    }

    private int getColor(String id) {
        switch (id) {
            case "leave":
            case "stop":
            case "block":
            case "logout":
                return Theme.key_dialogTextRed;
            case "unblock":
            case "restart":
                return Theme.key_windowBackgroundWhiteGreenText2;
        }
        return Theme.key_switch2TrackChecked;
    }

    public ActionButtonInfo getItemAt(int index) {
        String id = index >= data.size() ? null:data.get(index);
        String text = null;
        int icon = -1;
        if (id != null) {
            int color = getColor(id);
            switch (id) {
                case "camera":
                    text = LocaleController.getString("AccDescrProfilePicture", R.string.AccDescrProfilePicture);
                    icon = R.raw.camera_outline;
                    break;
                case "edit_name":
                    text = LocaleController.getString("VoipEditName", R.string.VoipEditName);
                    icon = R.drawable.msg_edit;
                    break;
                case "logout":
                    text = LocaleController.getString("LogOut", R.string.LogOut);
                    icon = R.drawable.msg_leave;
                    break;
                case "send_message":
                    text = LocaleController.getString("Send", R.string.Send);
                    icon = R.drawable.profile_newmsg;
                    break;
                case "video_call":
                    text = LocaleController.getString("VideoCall", R.string.VideoCall);
                    icon = R.drawable.msg_videocall;
                    break;
                case "call":
                    text = LocaleController.getString("Call", R.string.Call);
                    icon = R.drawable.msg_calls;
                    break;
                case "restart":
                    text = LocaleController.getString("BotRestart", R.string.BotRestart);
                    icon = R.drawable.msg_retry;
                    break;
                case "unblock":
                    text = LocaleController.getString("Unblock", R.string.Unblock);
                    icon = R.drawable.msg_block;
                    break;
                case "stop":
                    text = LocaleController.getString("BotStop", R.string.BotStop);
                    icon = R.drawable.msg_block;
                    break;
                case "block":
                    text = LocaleController.getString("BlockContact", R.string.BlockContact);
                    icon = R.drawable.msg_block;
                    break;
                case "add_bot":
                    text = LocaleController.getString("Add", R.string.Add);
                    icon = R.drawable.msg_addbot;
                    break;
                case "share_contact":
                case "share":
                    text = LocaleController.getString("BotShare", R.string.BotShare);
                    icon = R.drawable.msg_share;
                    break;
                case "join":
                    text = LocaleController.getString("VoipChatJoin", R.string.VoipChatJoin);
                    icon = R.drawable.msg_link2;
                    break;
                case "add_user":
                    text = LocaleController.getString("Add", R.string.Add);
                    icon = R.drawable.msg_addcontact;
                    break;
                case "join_call":
                    text = LocaleController.getString("StartVoipChatTitle", R.string.StartVoipChatTitle);
                    icon = R.drawable.msg_voicechat;
                    break;
                case "search":
                    text = LocaleController.getString("Search", R.string.Search);
                    icon = R.drawable.msg_search;
                    break;
                case "edit":
                    text = LocaleController.getString("Edit", R.string.Edit);
                    icon = R.drawable.msg_edit;
                    break;
                case "info":
                    text = LocaleController.getString("Info", R.string.Info);
                    icon = R.drawable.msg_info;
                    break;
                case "report":
                    text = LocaleController.getString("ReportChat", R.string.ReportChat);
                    icon = R.drawable.msg_report;
                    break;
                case "open_channel":
                    text = LocaleController.getString("AccDescrChannel", R.string.AccDescrChannel);
                    icon = R.drawable.msg_channel;
                    break;
                case "open_discussion":
                    text = LocaleController.getString("Discussion", R.string.Discussion);
                    icon = R.drawable.msg_discussion;
                    break;
                case "leave":
                    text = LocaleController.getString("VoipGroupLeave", R.string.VoipGroupLeave);
                    icon = R.drawable.msg_leave;
                    break;
            }
            if (text != null) {
                return new ActionButtonInfo(id, text, icon, color);
            }
        }
        return null;
    }

    public static boolean canShowTopActions(TLRPC.Chat currentChat, boolean editItemVisible, boolean isTopic) {
        return (!canShowShortcuts(currentChat, isTopic) || !editItemVisible) && (OctoConfig.buttonStyleType == 5 || isTopic);
    }

    public static boolean canShowCall(TLRPC.Chat currentChat, boolean isTopic) {
        boolean canEdit;
        if (ChatObject.isChannel(currentChat)) {
            canEdit = !ChatObject.isChannelAndNotMegaGroup(currentChat) || ChatObject.hasAdminRights(currentChat);
        } else {
            canEdit = true;
        }
        return canShowShortcuts(currentChat, isTopic) && canEdit;
    }

    public static boolean canShowButtons(boolean isTopic) {
        return OctoConfig.buttonStyleType == 5 || isTopic;
    }

    public static boolean canShowShortcuts(TLRPC.Chat currentChat, boolean isTopic) {
        return (OctoConfig.smartButtons && (ChatObject.hasAdminRights(currentChat) || currentChat != null && currentChat.megagroup && ChatObject.canChangeChatInfo(currentChat))) && !isTopic;
    }

    public static class ActionButtonInfo {
        public final String id;
        public final String text;
        public final int icon;
        public final int color;

        public ActionButtonInfo(String id, String text, int icon, int color) {
            this.id = id;
            this.text = text;
            this.icon = icon;
            this.color = color;
        }
    }
}
