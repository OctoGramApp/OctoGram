/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.media;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Storage.CacheModel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import it.octogram.android.app.ui.cells.SingleDatacenterStatusPreview;
import it.octogram.android.utils.OctoLogging;

/**
 * @noinspection SequencedCollectionMethodCanBeUsed
 */
public class DcMediaController implements NotificationCenter.NotificationCenterDelegate {
    private static final int FIRST_ACCOUNT_ID = 0;
    private static final String CHANNEL_USERNAME = "TeAEpdywrkYaBXP9Q32Hn4zjhmfDZN87";
    private final ArrayList<Integer> ASSOC_DC_IDS = new ArrayList<>();
    private final ArrayList<TLRPC.Document> downloadingMedias = new ArrayList<>();
    private final HashMap<String, Integer> assocFileNames = new HashMap<>();
    private final HashSet<Integer> parsedDcs = new HashSet<>();
    private OnFetchResult callback;
    private long startedAt;
    private int specificDcId = 0;

    {
        ASSOC_DC_IDS.add(11); // DC1
        ASSOC_DC_IDS.add(10); // DC2
        ASSOC_DC_IDS.add(7); // DC3
        ASSOC_DC_IDS.add(4); // DC4
        ASSOC_DC_IDS.add(2); // DC5
    }

    protected static MessagesController getMessagesController() {
        return MessagesController.getInstance(UserConfig.selectedAccount);
    }

    protected static ConnectionsManager getConnectionsManager() {
        return ConnectionsManager.getInstance(UserConfig.selectedAccount);
    }

    protected static MessagesStorage getMessagesStorage() {
        return MessagesStorage.getInstance(UserConfig.selectedAccount);
    }

    protected static FileLoader getFileLoader() {
        return FileLoader.getInstance(UserConfig.selectedAccount);
    }

    protected static NotificationCenter getNotificationCenter() {
        return NotificationCenter.getInstance(UserConfig.selectedAccount);
    }

    public void startFetching(int specificDcId) {
        if (callback == null) {
            OctoLogging.w("DcMediaController", "Cannot start fetching - callback is null");
            return;
        }

        OctoLogging.d("DcMediaController", "Starting fetch for DC " + (specificDcId == 0 ? "all" : specificDcId));
        destroy(false);
        this.specificDcId = specificDcId;

        for (int i = 1; i <= 5; i++) {
            if (specificDcId == 0 || specificDcId == i) {
                callback.onUpdate(i, SingleDatacenterStatusPreview.DOWNLOADING);
            }
        }

        getNotificationCenter().addObserver(this, NotificationCenter.fileLoaded);
        getNotificationCenter().addObserver(this, NotificationCenter.fileLoadProgressChanged);
        getNotificationCenter().addObserver(this, NotificationCenter.fileLoadFailed);

        resolveUsername(this::fetchContent);
    }

    public void startFetching() {
        startFetching(0);
    }

    public void setCallback(OnFetchResult callback) {
        this.callback = callback;
    }

    public void destroy(boolean promptInterrupt) {
        OctoLogging.d("DcMediaController", "Destroying controller, promptInterrupt: " + promptInterrupt);
        getNotificationCenter().removeObserver(this, NotificationCenter.fileLoaded);
        getNotificationCenter().removeObserver(this, NotificationCenter.fileLoadProgressChanged);
        getNotificationCenter().removeObserver(this, NotificationCenter.fileLoadFailed);

        if (callback != null && promptInterrupt) {
            for (int i = 1; i <= 5; i++) {
                if (!parsedDcs.contains(i)) {
                    callback.onUpdate(i, SingleDatacenterStatusPreview.INTERRUPTED);
                }
            }
        }

        assocFileNames.clear();
        parsedDcs.clear();

        for (TLRPC.Document document : downloadingMedias) {
            getFileLoader().cancelLoadFile(document);
        }
        downloadingMedias.clear();

        specificDcId = 0;
    }

    public void destroy() {
        destroy(true);
    }

    private void resolveUsername(Utilities.Callback<TLRPC.TL_inputChannel> run) {
        OctoLogging.d("DcMediaController", "Resolving username: " + CHANNEL_USERNAME);
        TLRPC.TL_contacts_resolveUsername req = new TLRPC.TL_contacts_resolveUsername();
        req.username = CHANNEL_USERNAME;
        getConnectionsManager().sendRequest(req, (response, error) -> {
            if (error == null) {
                TLRPC.TL_contacts_resolvedPeer res = (TLRPC.TL_contacts_resolvedPeer) response;
                if (!res.chats.isEmpty()) {
                    OctoLogging.d("DcMediaController", "Username resolved successfully");
                    getMessagesController().putChat(res.chats.get(0), false);
                    getMessagesStorage().putUsersAndChats(null, res.chats, true, true);

                    TLRPC.TL_inputChannel channel = new TLRPC.TL_inputChannel();
                    channel.channel_id = res.chats.get(0).id;
                    channel.access_hash = res.chats.get(0).access_hash;
                    run.run(channel);
                } else {
                    OctoLogging.e("DcMediaController", "Username resolution returned no chats");
                    if (callback != null) {
                        callback.onFailed();
                    }
                }
            } else {
                OctoLogging.e("DcMediaController", "Username resolution request failed: " + (error != null ? error.text : "unknown"));
                if (callback != null) {
                    callback.onFailed();
                }
            }
        });
    }

    private void fetchContent(TLRPC.InputChannel inputChannel) {
        OctoLogging.d("DcMediaController", "Starting content fetch for channel: " + inputChannel.channel_id);
        startedAt = System.currentTimeMillis();

        TLRPC.TL_channels_getMessages req = new TLRPC.TL_channels_getMessages();
        req.channel = inputChannel;
        if (specificDcId == 0) {
            req.id = ASSOC_DC_IDS;
        } else {
            ArrayList<Integer> tempList = new ArrayList<>();
            tempList.add(ASSOC_DC_IDS.get(specificDcId - 1));
            req.id = tempList;
        }
        OctoLogging.d("DcMediaController", "Requesting messages for ids: " + req.id);
        getConnectionsManager().sendRequest(req, (response, error) -> {
            if (error == null) {
                TLRPC.messages_Messages messagesRes = (TLRPC.messages_Messages) response;
                if (!messagesRes.messages.isEmpty()) {
                    handleCacheDeleteFromChannel(messagesRes, () -> handleMessagesFromChannels(messagesRes));
                } else {
                    OctoLogging.e("DcMediaController", "No messages received from channel response");
                    if (callback != null) {
                        callback.onFailed();
                    }
                }
            } else {
                OctoLogging.e("DcMediaController", "Error fetching messages: " + (error != null ? error.text : "unknown"));
                if (callback != null) {
                    callback.onFailed();
                }
            }
        });
    }

    private void handleCacheDeleteFromChannel(TLRPC.messages_Messages messagesRes, Runnable run) {
        ArrayList<CacheModel.FileInfo> fileInfos = new ArrayList<>();
        for (TLRPC.Message message : messagesRes.messages) {
            int dcIdAssoc = getDcIdByMessage(message);

            if (dcIdAssoc == 0 || (specificDcId != 0 && dcIdAssoc != specificDcId)) {
                continue;
            }

            if (message.media != null && message.media.document != null) {
                fileInfos.add(getDownloadedFileInstance(message));
            }
        }

        if (fileInfos.isEmpty()) {
            run.run();
        } else {
            getFileLoader().getFileDatabase().removeFiles(fileInfos);
            getFileLoader().getFileLoaderQueue().postRunnable(() -> {
                for (int i = 0; i < fileInfos.size(); i++) {
                    if (fileInfos.get(i).file.delete()) {
                        OctoLogging.d("DcMediaController", "File deleted: " + fileInfos.get(i).file.getAbsolutePath());
                    } else {
                        OctoLogging.d("DcMediaController", "File not deleted: " + fileInfos.get(i).file.getAbsolutePath());
                    }
                }

                AndroidUtilities.runOnUIThread(() -> {
                    getFileLoader().checkCurrentDownloadsFiles();
                    run.run();
                });
            });
        }
    }

    private void handleMessagesFromChannels(TLRPC.messages_Messages messagesRes) {
        OctoLogging.d("DcMediaController", "Processing messages from channels");
        ArrayList<Integer> parsedDcIds = new ArrayList<>();
        for (TLRPC.Message message : messagesRes.messages) {
            int dcIdAssoc = getDcIdByMessage(message);

            if (dcIdAssoc == 0 || (specificDcId != 0 && dcIdAssoc != specificDcId)) {
                continue;
            }

            if (message.media != null && message.media.document != null) {
                parsedDcIds.add(dcIdAssoc);
                downloadingMedias.add(message.media.document);
                String fileName = FileLoader.getAttachFileName(message.media.document);
                OctoLogging.d("DcMediaController", "Queueing download: doc_id=" + message.media.document.id + " file=" + fileName + " dc=" + dcIdAssoc);
                assocFileNames.put(fileName, dcIdAssoc);
                OctoLogging.d("DcMediaController", "Mapped file to DC: " + fileName + " -> " + dcIdAssoc);
                getFileLoader().loadFile(message.media.document, "dc_id_test", FileLoader.PRIORITY_NORMAL, 1);
                callback.onUpdate(dcIdAssoc, SingleDatacenterStatusPreview.DOWNLOADING, 0);
            }
        }
        for (int i = 1; i <= 5; i++) {
            if (!parsedDcIds.contains(i) && (specificDcId == 0 || specificDcId == i)) {
                parsedDcs.add(i);
                callback.onUpdate(i, SingleDatacenterStatusPreview.DOWNLOAD_FAILED);
            }
        }
    }

    private CacheModel.FileInfo getDownloadedFileInstance(TLRPC.Message message) {
        File f = FileLoader.getInstance(UserConfig.selectedAccount).getPathToAttach(message.media.document, true);
        if (f != null) {
            return new CacheModel.FileInfo(f);
        } else {
            OctoLogging.d("DcMediaController", "Downloaded file instance not found for message id: " + message.id);
            return null;
        }
    }

    private int getDcIdByMessage(TLRPC.Message message) {
        int dcIdAssoc = 0;
        for (int i = 0; i < ASSOC_DC_IDS.size(); i++) {
            if (ASSOC_DC_IDS.get(i) == message.id) {
                dcIdAssoc = i + 1;
                OctoLogging.d("DcMediaController", "Found DC association for message " + message.id + ": DC" + dcIdAssoc);
                break;
            }
        }
        return dcIdAssoc;
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (account != FIRST_ACCOUNT_ID) {
            OctoLogging.d("DcMediaController", "Ignoring notification for non-first account: " + account);
            return;
        }

        String path = (String) args[0];
        if (!assocFileNames.containsKey(path)) {
            OctoLogging.d("DcMediaController", "Ignoring notification for unknown file: " + path);
            return;
        }

        Integer dcId = assocFileNames.get(path);
        if (dcId == null) {
            return;
        }

        if (id == NotificationCenter.fileLoaded) {
            int time = (int) ((System.currentTimeMillis() - startedAt) / 1000F);
            parsedDcs.add(dcId);
            OctoLogging.d("DcMediaController", "File loaded for DC " + dcId + " in " + time + "s (path=" + path + ")");
            callback.onUpdate(dcId, SingleDatacenterStatusPreview.DOWNLOAD_END, time, parsedDcs.size());
        } else if (id == NotificationCenter.fileLoadProgressChanged) {
            Float p = ImageLoader.getInstance().getFileProgress(path);
            OctoLogging.d("DcMediaController", "Progress for " + path + " -> " + (p != null ? (p * 100) + "%" : "unknown"));
            callback.onUpdate(dcId, SingleDatacenterStatusPreview.DOWNLOADING, p != null ? ((int) (p * 100)) : 0);
        } else if (id == NotificationCenter.fileLoadFailed) {
            int reason = (int) args[1];
            OctoLogging.e("DcMediaController", "File load failed for path=" + path + " reason=" + reason);
            parsedDcs.add(dcId);
            callback.onUpdate(dcId, reason == 2 ? SingleDatacenterStatusPreview.DOWNLOAD_FAILED_TRY_LATER : SingleDatacenterStatusPreview.DOWNLOAD_FAILED, parsedDcs.size());
        }
    }

    public interface OnFetchResult {
        void onUpdate(int dcId, int status, int parameter, int parsedDcs);

        default void onUpdate(int dcId, int status) {
            onUpdate(dcId, status, 0, 0);
        }

        default void onUpdate(int dcId, int status, int parameter) {
            onUpdate(dcId, status, parameter, 0);
        }

        void onFailed();
    }
}
