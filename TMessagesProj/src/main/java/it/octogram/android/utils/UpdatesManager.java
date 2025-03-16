/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils;

import static org.telegram.messenger.LocaleController.getString;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.LaunchActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import it.octogram.android.AutoDownloadUpdate;
import it.octogram.android.OctoConfig;
import it.octogram.android.http.StandardHTTPRequest;
import it.octogram.android.logs.OctoLogging;

public class UpdatesManager {
    private static final String TAG = "UpdatesManager";
    private static final long privateChatId = -1733655252L;
    private static final long privateBotId = 6563667131L;

    public static Map<String, String> pbetaVersionsReplacement;
    static {
        pbetaVersionsReplacement = new HashMap<>();
        pbetaVersionsReplacement.put("arm64-v8a", "arm64");
    }

    private static JSONObject updatesData;
    private static boolean checkingForChanges;
    private static long lastUpdateCheck;

    private static String currentChannelUsername;
    private static long currentChannelId;
    private static int pbetaStickerId;

    private static boolean isLoadingTLRPCAppUpdate;
    private static TLRPC.TL_help_appUpdate currentUpdateData;
    private static final int FIRST_ACCOUNT_ID = 0;

    protected static MessagesController getMessagesController() {
        return MessagesController.getInstance(FIRST_ACCOUNT_ID);
    }

    protected static ConnectionsManager getConnectionsManager() {
        return ConnectionsManager.getInstance(FIRST_ACCOUNT_ID);
    }

    protected static MessagesStorage getMessagesStorage() {
        return MessagesStorage.getInstance(FIRST_ACCOUNT_ID);
    }

    public static boolean canReceivePrivateBetaUpdates() {
        if (!OctoConfig.INSTANCE.receivePBetaUpdates.getValue()) {
            return false;
        }

        if (getPrivateBetaChatInstance() == null) {
            OctoConfig.INSTANCE.receivePBetaUpdates.updateValue(false);
            return false;
        }

        return true;
    }

    public static TLRPC.Chat getPrivateBetaChatInstance() {
        TLRPC.Chat chat = getMessagesController().getChat(-privateChatId);

        boolean hasPrivateBetaAccessC = chat != null;
        if (!hasPrivateBetaAccessC) {
            OctoLogging.d(TAG, "ACCESS HAS BEEN FORBIDDEN - STATE: chat empty");
        }

        if (hasPrivateBetaAccessC) {
            hasPrivateBetaAccessC = chat.id != 0;
            hasPrivateBetaAccessC &= chat.access_hash != 0;
            if (!hasPrivateBetaAccessC) {
                OctoLogging.d(TAG, "ACCESS HAS BEEN FORBIDDEN - STATE:" + chat.id + " - " + chat.access_hash);
            }
        }

        if (hasPrivateBetaAccessC) {
            hasPrivateBetaAccessC = ChatObject.isInChat(chat);
            hasPrivateBetaAccessC &= ChatObject.isChannel(chat);
            hasPrivateBetaAccessC &= !ChatObject.isPublic(chat);
            if (!hasPrivateBetaAccessC) {
                OctoLogging.d(TAG, "ACCESS HAS BEEN FORBIDDEN - STATE:" + ChatObject.isInChat(chat) + ChatObject.isChannel(chat) + ChatObject.isPublic(chat));
            }
        }

        return hasPrivateBetaAccessC ? chat : null;
    }

    public static void isUpdateAvailable(UpdatesManagerCheckInterface callback) {
        if (updatesData == null) {
            checkForUpdates(new UpdatesManagerPrepareInterface() {
                @Override
                public void onSuccess() {
                    isUpdateAvailableAfterDataEnsure(callback);
                }

                @Override
                public void onError() {
                    callback.onError();
                }
            });
            return;
        }

        isUpdateAvailableAfterDataEnsure(callback);
    }

    // after data is available
    private static void isUpdateAvailableAfterDataEnsure(UpdatesManagerCheckInterface callback) {
        try {
            JSONArray versionsHistory = updatesData.getJSONArray("versions");

            for (int i = 0; i < versionsHistory.length(); i++) {
                JSONObject update = versionsHistory.getJSONObject(i);

                if (!isUpdateCorrupted(update) && isNewerVersion(update.getInt("versionCode")) && canUpdate(update)) {
                    callback.onThereIsUpdate(update);
                    return;
                }
            }

            checkPrivateBetaData(callback);
        } catch (JSONException e) {
            OctoLogging.e(TAG, "Update check failed due to invalid data!", e);
            callback.onError();
        }
    }

    private static void checkForUpdates(UpdatesManagerPrepareInterface callback) {
        if (checkingForChanges || (new Date().getTime() - lastUpdateCheck) < 30000) { // 30s
            // FORCE 1 REQUEST TO GITHUB EVERY 30S

            if (updatesData != null) {
                callback.onSuccess();
            } else {
                callback.onError();
            }

            return;
        }

        lastUpdateCheck = new Date().getTime();
        checkingForChanges = true;

        new Thread() {
            @Override
            public void run() {
                try {
                    String reqUrl = String.format(Locale.getDefault(),"https://raw.githubusercontent.com/OctoGramApp/assets/ota/version_history/history.json?ms=%d", System.currentTimeMillis());
                    String reqResponse = new StandardHTTPRequest(reqUrl).request();
                    updatesData = new JSONObject(reqResponse);

                    if (!isResponseCorrupted(updatesData)) {
                        currentChannelUsername = updatesData.getString("channel_username");
                        currentChannelId = updatesData.getLong("channel_id");
                        pbetaStickerId = updatesData.getInt("pbeta_sticker_id");
                        callback.onSuccess();
                    } else {
                        updatesData = null;
                        callback.onError();
                    }

                } catch (Exception ignored) {
                    callback.onError();
                } finally {
                    checkingForChanges = false;
                }
            }
        }.start();
    }

    private static boolean isResponseCorrupted(JSONObject object) {
        try {
            if (object.has("channel_username")) {
                if (object.getString("channel_username").length() < 5) {
                    return true;
                }
            } else {
                return true;
            }

            if (object.has("channel_id")) {
                object.getLong("channel_id");
            } else {
                return true;
            }

            if (object.has("pbeta_sticker_id")) {
                object.getInt("pbeta_sticker_id");
            } else {
                return true;
            }

            if (object.has("versions")) {
                object.getJSONArray("versions");
            } else {
                return true;
            }

            return false;
        } catch (JSONException e) {
            return true;
        }
    }

    private static boolean isUpdateCorrupted(JSONObject object) {
        try {
            if (object.has("version")) {
                object.getString("version");
            } else {
                return true;
            }

            if (object.has("can_not_skip")) {
                object.getBoolean("can_not_skip");
            }

            if (object.has("beta")) {
                object.getBoolean("beta");
            }

            if (object.has("versionCode")) {
                object.getInt("versionCode");
            } else {
                return true;
            }

            if (object.has("sticker")) {
                if (object.getInt("sticker") <= 1) {
                    return true;
                }
            } else {
                return true;
            }

            if (object.has("localized_changelogs")) {
                object.getJSONObject("localized_changelogs");
            } else {
                return true;
            }

            if (object.has("files")) {
                JSONObject filesObject = object.getJSONObject("files");
                if (filesObject.getInt("x86_64") <= 1) {
                    return true;
                }
                if (filesObject.getInt("universal") <= 1) {
                    return true;
                }
                if (filesObject.getInt("x86") <= 1) {
                    return true;
                }
                if (filesObject.getInt("arm64-v8a") <= 1) {
                    return true;
                }
                return filesObject.getInt("arm-v7a") <= 1;
            } else {
                return true;
            }
        } catch (JSONException e) {
            return true;
        }
    }

    private static void checkPrivateBetaData(UpdatesManagerCheckInterface callback) {
        /*
        shouldn't be needed
        TLRPC.TL_channels_getChannels req1 = new TLRPC.TL_channels_getChannels();
        TLRPC.TL_inputChannel inputChannel = new TLRPC.TL_inputChannel();
        inputChannel.channel_id = -privateChatId;
        req1.id.add(inputChannel);
        getConnectionsManager().sendRequest(req1, (response, error) -> {
            if (error != null || response == null) {
                if (error != null) {
                    throw new RuntimeException(error.text);
                } else {
                    throw new RuntimeException("rspo0");
                }
            }

            TLRPC.TL_messages_chats res = (TLRPC.TL_messages_chats) response;
            getMessagesController().putChats(res.chats, false);
            getMessagesStorage().putUsersAndChats(null, res.chats, false, true);
        });*/

        if (!OctoConfig.INSTANCE.receivePBetaUpdates.getValue()) {
            callback.onNoUpdate();
            return;
        }

        TLRPC.Chat chatInstance = getPrivateBetaChatInstance();

        if (chatInstance == null) {
            //OctoConfig.INSTANCE.receivePBetaUpdates.updateValue(false);
            callback.onNoUpdate();
            return;
        }

        TLRPC.InputPeer currentPeer = new TLRPC.TL_inputPeerChannel();
        currentPeer.channel_id = -privateChatId;
        currentPeer.access_hash = chatInstance.access_hash;

        TLRPC.TL_messages_search req = new TLRPC.TL_messages_search();
        req.peer = currentPeer;
        req.limit = 10;
        req.from_id = getMessagesController().getInputPeer(privateBotId);
        req.q = "";
        req.filter = new TLRPC.TL_inputMessagesFilterDocument();
        getConnectionsManager().sendRequest(req, (response, error) -> {
            boolean foundUpdate = false;

            if (error == null && response instanceof TLRPC.messages_Messages res) {
                getMessagesController().putUsers(res.users, false);
                getMessagesController().putChats(res.chats, false);
                getMessagesStorage().putUsersAndChats(res.users, res.chats, false, true);

                if (!res.messages.isEmpty()) {
                    for (TLRPC.Message message : res.messages) {
                        if (message.media == null || message.media.document == null) {
                            continue;
                        }

                        if (message.from_id.user_id != privateBotId || message.message.isEmpty()) {
                            continue;
                        }

                        String[] captionSplit = message.message.split("\n");
                        String captionLastRow = captionSplit[captionSplit.length - 1];
                        if (!captionLastRow.startsWith("#c") || !captionLastRow.contains("#universal")) {
                            continue;
                        }

                        String commitID = "";
                        StringBuilder commitText = new StringBuilder();
                        boolean hasPreviousAddedContent = false;
                        for (String captionRow : captionSplit) {
                            if (captionRow.startsWith("#c")) {
                                commitID = captionRow.split("#c")[1];
                                if (commitID.contains(" ")) {
                                    commitID = commitID.split(" ")[0];
                                }
                            } else if (!captionRow.isEmpty() || hasPreviousAddedContent) {
                                hasPreviousAddedContent = true;
                                commitText.append("\n").append(captionRow);
                            }
                        }

                        if (commitID.isEmpty()) {
                            continue;
                        }

                        if (commitID.equals(BuildConfig.GIT_COMMIT_HASH)) {
                            break;
                        }

                        StringBuilder finalUpdate = new StringBuilder(String.format("%s\n%s", getString(R.string.UpdatesPbetaWarning), commitText));

                        TLRPC.Document currentMediaDocument = message.media.document;
                        String currentAbiRelease = OctoUtils.getCurrentAbi(false);
                        if (!currentAbiRelease.equals("universal") && pbetaVersionsReplacement.containsKey(currentAbiRelease)) {
                            String replacedHashtag = pbetaVersionsReplacement.get(currentAbiRelease);
                            if (replacedHashtag != null && !replacedHashtag.isEmpty()) {
                                for (TLRPC.Message msg2 : res.messages) {
                                    if (msg2.media == null || msg2.media.document == null) {
                                        continue;
                                    }

                                    if (msg2.from_id.user_id != privateBotId || msg2.message.isEmpty()) {
                                        continue;
                                    }

                                    String[] captionSplit2 = msg2.message.split("\n");
                                    String captionLastRow2 = captionSplit2[captionSplit2.length - 1];

                                    if (!captionLastRow2.contains("#c" + commitID) || !captionLastRow2.contains("#" + replacedHashtag)) {
                                        continue;
                                    }

                                    currentMediaDocument = msg2.media.document;
                                    finalUpdate.append(String.format("\nWe selected the %s BUILD ARCHITECTURE for this update.", currentAbiRelease.toUpperCase()));
                                }
                            }
                        }

                        TLRPC.TL_help_appUpdate update = new TLRPC.TL_help_appUpdate();
                        update.can_not_skip = false;
                        update.id = BuildConfig.BUILD_VERSION + 1; // force detect as new update
                        update.text = finalUpdate.toString();
                        update.version = "PBeta " + BuildConfig.BUILD_VERSION_STRING;

                        update.document = currentMediaDocument;
                        update.flags |= 2;

                        ArrayList<Integer> needMessageIds = new ArrayList<>();
                        needMessageIds.add(pbetaStickerId);
                        tryToGetContent(needMessageIds, (messageID, mediaMessage) -> {
                            if (mediaMessage.media.document == null) {
                                return;
                            }

                            if (messageID == pbetaStickerId) {
                                update.sticker = mediaMessage.media.document;
                                update.flags |= 8;
                            }

                            if (update.sticker != null) {
                                callback.onThereIsUpdate(update);
                            }
                        });

                        foundUpdate = true;
                        break;
                    }
                }
            }

            if (!foundUpdate) {
                callback.onNoUpdate();
            }
        });
    }

    private static boolean isNewerVersion(int versionCode) {
        return versionCode > BuildConfig.BUILD_VERSION;
    }

    public static void getTLRPCUpdateFromObject(JSONObject object, UpdatesManagerTLRPCReadyInterface callback) {
        if (isLoadingTLRPCAppUpdate) {
            return;
        }

        if (currentUpdateData != null) {
            callback.onTLRPCReady(currentUpdateData);
            return;
        }

        isLoadingTLRPCAppUpdate = true;
        try {
            TLRPC.TL_help_appUpdate update = new TLRPC.TL_help_appUpdate();
            update.can_not_skip = canNotSkipUpdate(object);
            update.id = object.getInt("versionCode");
            update.text = getUpdateText(object);
            update.version = object.getString("version");

            ArrayList<Integer> needMessageIds = new ArrayList<>();

            int stickerId = object.getInt("sticker");
            needMessageIds.add(stickerId);

            int rightFileId = getFileMessageId(object);
            needMessageIds.add(rightFileId);

            needMessageIds.add(object.getJSONObject("files").getInt("universal"));
            tryToGetContent(needMessageIds, (messageID, message) -> {
                if (message.media.document == null) {
                    return;
                }

                if (messageID == stickerId) {
                    update.sticker = message.media.document;
                    update.flags |= 8;
                } else if (messageID == rightFileId) {
                    update.document = message.media.document;
                    update.flags |= 2;
                }

                if (update.sticker != null && update.document != null) {
                    currentUpdateData = update;
                    isLoadingTLRPCAppUpdate = false;
                    callback.onTLRPCReady(update);
                }
            });
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private static void tryToGetContent(ArrayList<Integer> messageIds, UpdatesManagerGetMessagesInterface callback) {
        TLRPC.InputPeer currentPeer = getMessagesController().getInputPeer(currentChannelId);
        if (currentPeer == null || currentPeer.peer == null || currentPeer.peer.access_hash == 0) {
            TLRPC.TL_contacts_resolveUsername resolve = new TLRPC.TL_contacts_resolveUsername();
            resolve.username = currentChannelUsername;
            getConnectionsManager().sendRequest(resolve, (response, error) -> {
                if (!(response instanceof TLRPC.TL_contacts_resolvedPeer peer)) {
                    return;
                }

                if (peer.chats == null || peer.chats.isEmpty()) {
                    return;
                }

                getMessagesController().putUsers(peer.users, false);
                getMessagesController().putChats(peer.chats, false);
                getMessagesStorage().putUsersAndChats(peer.users, peer.chats, false, true);

                TLRPC.TL_inputChannel channel = new TLRPC.TL_inputChannel();
                channel.channel_id = peer.chats.get(0).id;
                channel.access_hash = peer.chats.get(0).access_hash;
                getMessagesData(channel, messageIds, callback);
            });
        } else {
            TLRPC.TL_inputChannel channel = new TLRPC.TL_inputChannel();
            channel.channel_id = currentPeer.channel_id;
            channel.access_hash = currentPeer.access_hash;
            getMessagesData(channel, messageIds, callback);
        }
    }

    private static void getMessagesData(TLRPC.InputChannel peer, ArrayList<Integer> messageIds, UpdatesManagerGetMessagesInterface callback) {
        TLRPC.TL_channels_getMessages req = new TLRPC.TL_channels_getMessages();
        req.channel = peer;
        req.id = messageIds;
        getConnectionsManager().sendRequest(req, (response, error) -> {
            if (error == null) {
                TLRPC.messages_Messages messagesRes = (TLRPC.messages_Messages) response;
                if (!messagesRes.messages.isEmpty()) {
                    for (TLRPC.Message message : messagesRes.messages) {
                        for (Integer messageId : messageIds) {
                            if (message.id == messageId) {
                                callback.onGotMessage(message.id, message);
                            }
                        }
                    }
                }
            }
        });
    }

    private static int getFileMessageId(JSONObject object) {
        try {
            String currentAbi = OctoUtils.getCurrentAbi(false);
            JSONObject filesObject = object.getJSONObject("files");
            return filesObject.getInt(currentAbi);
        } catch (JSONException e) {
            // considering the checks on object made by isUpdateCorrupted,
            // these fields are available for sure so we can run runtime exception
            throw new RuntimeException(e);
        }
    }

    private static Boolean canUpdate(JSONObject object) {
        if (OctoConfig.INSTANCE.preferBetaVersion.getValue()) {
            return true;
        }

        return !isBetaUpdate(object);
    }

    private static Boolean isBetaUpdate(JSONObject object) {
        try {
            return object.has("beta") && object.getBoolean("beta");
        } catch (JSONException e) {
            return false;
        }
    }

    private static Boolean canNotSkipUpdate(JSONObject object) {
        try {
            return object.has("can_not_skip") && object.getBoolean("can_not_skip");
        } catch (JSONException e) {
            return false;
        }
    }

    private static String getUpdateText(JSONObject object) {
        try {
            JSONObject localizedChangelogs = object.getJSONObject("localized_changelogs");
            String myLanguageCode = LocaleController.getInstance().getCurrentLocale().getLanguage();

            if (localizedChangelogs.has(myLanguageCode) && !localizedChangelogs.getString(myLanguageCode).isEmpty()) {
                return localizedChangelogs.getString(myLanguageCode).trim();
            } else {
                return localizedChangelogs.getString("en").trim();
            }
        } catch (JSONException e) {
            return null;
        }
    }

    public static boolean canAutoDownloadUpdates() {
        int autoDownloadUpdatesStatus = OctoConfig.INSTANCE.autoDownloadUpdatesStatus.getValue();

        if (autoDownloadUpdatesStatus == AutoDownloadUpdate.ALWAYS.getValue()) {
            return true;
        }

        if (autoDownloadUpdatesStatus == AutoDownloadUpdate.ONLY_ON_WIFI.getValue()) {
            return BrowserUtils.isUsingWifi();
        }

        return false;
    }

    public static void installUpdate() {
        if (SharedConfig.pendingAppUpdate != null) {
            TLRPC.TL_help_appUpdate update = SharedConfig.pendingAppUpdate;

            if (!TextUtils.isEmpty(update.text)) {
                OctoConfig.INSTANCE.updateSignalingCommitID.updateValue(BuildConfig.GIT_COMMIT_HASH);
                OctoConfig.INSTANCE.updateSignalingChangelog.updateValue(update.text);
            }

            AndroidUtilities.runOnUIThread(() -> AndroidUtilities.openForView(update.document, true, LaunchActivity.instance));
        }
    }

    public static void handleUpdateSignaling() {
        if (OctoConfig.INSTANCE != null) {

            boolean isValidUpdate = !Objects.equals(OctoConfig.INSTANCE.updateSignalingCommitID.getValue(), BuildConfig.GIT_COMMIT_HASH);
            isValidUpdate &= !TextUtils.isEmpty(OctoConfig.INSTANCE.updateSignalingChangelog.getValue());

            if (isValidUpdate) {
                TLRPC.TL_updateServiceNotification update = new TLRPC.TL_updateServiceNotification();
                update.message = OctoConfig.INSTANCE.updateSignalingChangelog.getValue();
                update.media = new TLRPC.TL_messageMediaEmpty();
                update.popup = false;
                update.type = "announcement";
                update.flags = 2;
                update.inbox_date = (int) (System.currentTimeMillis() / 1000);
                final TLRPC.TL_updates updates = new TLRPC.TL_updates();
                updates.updates.add(update);
                Utilities.stageQueue.postRunnable(() -> MessagesController.getInstance(UserConfig.selectedAccount).processUpdates(updates, false));
                ConnectionsManager.getInstance(UserConfig.selectedAccount).resumeNetworkMaybe();
            }

            OctoConfig.INSTANCE.updateSignalingCommitID.clear();
            OctoConfig.INSTANCE.updateSignalingChangelog.clear();
        } else {
            OctoLogging.e(TAG, "OctoConfig INSTANCE or properties are not initialized");
        }
    }

    public interface UpdatesManagerPrepareInterface {
        void onSuccess();
        void onError();
    }

    public interface UpdatesManagerCheckInterface {
        void onThereIsUpdate(JSONObject updateData);
        void onThereIsUpdate(TLRPC.TL_help_appUpdate appUpdate);
        void onNoUpdate();
        void onError();
    }

    public interface UpdatesManagerTLRPCReadyInterface {
        void onTLRPCReady(TLRPC.TL_help_appUpdate update);
    }

    private interface UpdatesManagerGetMessagesInterface {
        void onGotMessage(int messageID, TLRPC.Message message);
    }
}
