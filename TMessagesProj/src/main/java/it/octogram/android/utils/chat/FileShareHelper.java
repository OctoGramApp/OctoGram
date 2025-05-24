/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.chat;

import androidx.collection.LongSparseArray;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Components.ShareAlert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

import it.octogram.android.OctoConfig;
import it.octogram.android.utils.OctoUtils;

public class FileShareHelper implements NotificationCenter.NotificationCenterDelegate {
    private final FileShareData data;
    private final int currentAccount;

    private String sharingFullLocation;
    private String sharingFileName;
    private LongSparseArray<TLRPC.Dialog> sharingDid = new LongSparseArray<>();

    public static void init(FileShareData data) {
        new FileShareHelper(data);
    }

    private FileShareHelper(FileShareData data) {
        this.data = data;
        this.currentAccount = UserConfig.selectedAccount;

        if (isInvalidName(data.fileName)) {
            if (data.delegate != null) {
                data.delegate.onInvalidName();
            }
            return;
        }

        File cacheDir = AndroidUtilities.getCacheDir();
        File cacheFile = new File(cacheDir.getPath(), data.fileName + data.fileExtension);
        FileLoader instance = FileLoader.getInstance(UserConfig.selectedAccount);

        if (cacheFile.exists()) {
            boolean ignored = cacheFile.delete();
        }

        try {
            FileOutputStream fos = new FileOutputStream(cacheFile);
            if (BuildConfig.DEBUG) {
                fos.write(data.fileContent.toString(4).getBytes());
            } else {
                fos.write(data.fileContent.toString().getBytes());
            }
            fos.close();

            sharingFileName = cacheFile.getName();
            sharingFullLocation = cacheFile.getAbsolutePath();

            if (data.delegate != null) {
                data.delegate.onChatSelectSheetOpen();
            }

            if (data.shareToSavedMessages) {
                long userID = UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId();
                TLRPC.Dialog dialog = MessagesController.getInstance(UserConfig.selectedAccount).getDialog(userID);
                sharingDid.append(userID, dialog);
                initUpdateReceiver();
                instance.uploadFile(cacheFile.getPath(), false, true, ConnectionsManager.FileTypeFile);
            } else {
                ShareAlert shAlert = new ShareAlert(data.fragment.getParentActivity(), null, null, false, null, false, true) {
                    @Override
                    protected void onSend(LongSparseArray<TLRPC.Dialog> did, int count, TLRPC.TL_forumTopic topic, boolean showToast) {
                        sharingDid = did;
                        if (!showToast) return;
                        super.onSend(sharingDid, count, topic, true);

                        initUpdateReceiver();
                        instance.uploadFile(cacheFile.getPath(), false, true, ConnectionsManager.FileTypeFile);
                    }
                };
                data.fragment.showDialog(shAlert);
            }
        } catch (IOException | JSONException ignored) {
            if (data.delegate != null) {
                data.delegate.onFailed();
            }
        }
    }

    public static boolean isInvalidName(String fileName) {
        return fileName.contains("/") || fileName.contains("\\") || fileName.length() > 40;
    }

    private NotificationCenter getNotificationInstance() {
        return NotificationCenter.getInstance(UserConfig.selectedAccount);
    }

    private void initUpdateReceiver() {
        getNotificationInstance().addObserver(this, NotificationCenter.fileUploaded);
        getNotificationInstance().addObserver(this, NotificationCenter.fileUploadFailed);
    }

    private void stopUpdateReceiver() {
        getNotificationInstance().removeObserver(this, NotificationCenter.fileUploaded);
        getNotificationInstance().removeObserver(this, NotificationCenter.fileUploadFailed);
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.fileUploaded) {
            String location = (String) args[0];
            TLRPC.InputFile inputFile = (TLRPC.InputFile) args[1];

            if (inputFile == null) {
                return;
            }

            if (!Objects.equals(location, sharingFullLocation)) {
                return;
            }

            stopUpdateReceiver();

            TLRPC.TL_documentAttributeFilename attr = new TLRPC.TL_documentAttributeFilename();
            attr.file_name = sharingFileName;

            TLRPC.TL_inputMediaUploadedDocument inputMediaDocument = new TLRPC.TL_inputMediaUploadedDocument();
            inputMediaDocument.file = inputFile;
            inputMediaDocument.attributes.add(attr);
            inputMediaDocument.mime_type = OctoConfig.EXPORT_BACKUP_MIME_TYPE;

            StringBuilder baseInfo = new StringBuilder();
            baseInfo.append(data.caption);
            baseInfo.append(String.format(Locale.US, " - https://%s/", OctoUtils.getDomain()));

            for (int i = 0; i < sharingDid.size(); i++) {
                TLRPC.TL_messages_sendMedia req = new TLRPC.TL_messages_sendMedia();
                req.peer = MessagesController.getInstance(currentAccount).getInputPeer(sharingDid.keyAt(i));
                req.random_id = SendMessagesHelper.getInstance(currentAccount).getNextRandomId();
                req.message = baseInfo.toString();
                req.silent = false;
                req.invert_media = true;
                req.media = inputMediaDocument;
                ConnectionsManager.getInstance(currentAccount).sendRequest(req, null);
            }

            if (data.delegate != null) {
                data.delegate.onSuccess();
            }
        } else if (id == NotificationCenter.fileUploadFailed) {
            String location = (String) args[0];

            if (!Objects.equals(location, sharingFullLocation)) {
                return;
            }

            stopUpdateReceiver();

            if (data.delegate != null) {
                data.delegate.onFailed();
            }
        }
    }

    public static class FileShareData {
        public String fileName;
        public String fileExtension;
        public JSONObject fileContent;
        public String caption = "";

        public BaseFragment fragment;
        public FileShareDelegate delegate;

        public boolean shareToSavedMessages = false;

        public interface FileShareDelegate {
            default void onChatSelectSheetOpen() {

            }

            default void onSuccess() {

            }

            default void onFailed() {

            }

            default void onInvalidName() {
                onFailed();
            }
        }
    }
}
