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

import it.octogram.android.logs.OctoLogging;
import it.octogram.android.preferences.ui.custom.DatacenterStatus;

/** @noinspection SequencedCollectionMethodCanBeUsed*/
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
            return;
        }

        destroy(false);
        this.specificDcId = specificDcId;

        for (int i = 1; i <= 5; i++) {
            if (specificDcId == 0 || specificDcId == i) {
                callback.onUpdate(i, DatacenterStatus.DOWNLOADING);
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
        getNotificationCenter().removeObserver(this, NotificationCenter.fileLoaded);
        getNotificationCenter().removeObserver(this, NotificationCenter.fileLoadProgressChanged);
        getNotificationCenter().removeObserver(this, NotificationCenter.fileLoadFailed);
        
        if (callback != null && promptInterrupt) {
            for (int i = 1; i <= 5; i++) {
                if (!parsedDcs.contains(i)) {
                    callback.onUpdate(i, DatacenterStatus.INTERRUPTED);
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
        TLRPC.TL_contacts_resolveUsername req = new TLRPC.TL_contacts_resolveUsername();
        req.username = CHANNEL_USERNAME;
        getConnectionsManager().sendRequest(req, (response, error) -> {
            if (error == null) {
                TLRPC.TL_contacts_resolvedPeer res = (TLRPC.TL_contacts_resolvedPeer) response;
                if (!res.chats.isEmpty()) {
                    getMessagesController().putChat(res.chats.get(0), false);
                    getMessagesStorage().putUsersAndChats(null, res.chats, true, true);

                    TLRPC.TL_inputChannel channel = new TLRPC.TL_inputChannel();
                    channel.channel_id = res.chats.get(0).id;
                    channel.access_hash = res.chats.get(0).access_hash;
                    run.run(channel);
                } else {
                    callback.onFailed();
                }
            } else {
                callback.onFailed();
            }
        });
    }

    private void fetchContent(TLRPC.InputChannel inputChannel) {
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
        getConnectionsManager().sendRequest(req, (response, error) -> {
            if (error == null) {
                TLRPC.messages_Messages messagesRes = (TLRPC.messages_Messages) response;
                if (!messagesRes.messages.isEmpty()) {
                    handleCacheDeleteFromChannel(messagesRes, () -> handleMessagesFromChannels(messagesRes));
                }
            } else {
                callback.onFailed();
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
        ArrayList<Integer> parsedDcIds = new ArrayList<>();
        for (TLRPC.Message message : messagesRes.messages) {
            int dcIdAssoc = getDcIdByMessage(message);

            if (dcIdAssoc == 0 || (specificDcId != 0 && dcIdAssoc != specificDcId)) {
                continue;
            }

            if (message.media != null && message.media.document != null) {
                parsedDcIds.add(dcIdAssoc);
                downloadingMedias.add(message.media.document);
                assocFileNames.put(FileLoader.getAttachFileName(message.media.document), dcIdAssoc);
                getFileLoader().loadFile(message.media.document, "dc_id_test", FileLoader.PRIORITY_NORMAL, 1);
                callback.onUpdate(dcIdAssoc, DatacenterStatus.DOWNLOADING, 0);
            }
        }
        for (int i = 1; i <= 5; i++) {
            if (!parsedDcIds.contains(i) && (specificDcId == 0 || specificDcId == i)) {
                parsedDcs.add(i);
                callback.onUpdate(i, DatacenterStatus.DOWNLOAD_FAILED);
            }
        }
    }

    private CacheModel.FileInfo getDownloadedFileInstance(TLRPC.Message message) {
        File f = FileLoader.getInstance(UserConfig.selectedAccount).getPathToAttach(message.media.document, true);
        if (f != null) {
            return new CacheModel.FileInfo(f);
        } else {
            return null;
        }
    }

    private int getDcIdByMessage(TLRPC.Message message) {
        int dcIdAssoc = 0;
        for (int i = 0; i < ASSOC_DC_IDS.size(); i++) {
            if (ASSOC_DC_IDS.get(i) == message.id) {
                dcIdAssoc = i+1;
                break;
            }
        }
        return dcIdAssoc;
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (account != FIRST_ACCOUNT_ID) {
            return;
        }

        String path = (String) args[0];
        if (!assocFileNames.containsKey(path)) {
            return;
        }

        Integer dcId = assocFileNames.get(path);
        if (dcId == null) {
            return;
        }

        if (id == NotificationCenter.fileLoaded) {
            int time = (int) ((System.currentTimeMillis() - startedAt)/1000F);
            parsedDcs.add(dcId);
            callback.onUpdate(dcId, DatacenterStatus.DOWNLOAD_END, time, parsedDcs.size());
        } else if (id == NotificationCenter.fileLoadProgressChanged) {
            Float p = ImageLoader.getInstance().getFileProgress(path);
            callback.onUpdate(dcId, DatacenterStatus.DOWNLOADING, p != null ? ((int) (p * 100)) : 0);
        } else if (id == NotificationCenter.fileLoadFailed) {
            int reason = (int) args[1];
            parsedDcs.add(dcId);
            callback.onUpdate(dcId, reason == 2 ? DatacenterStatus.DOWNLOAD_FAILED_TRY_LATER : DatacenterStatus.DOWNLOAD_FAILED, parsedDcs.size());
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
