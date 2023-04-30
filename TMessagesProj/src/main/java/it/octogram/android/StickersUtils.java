package it.octogram.android;

import org.telegram.messenger.MediaDataController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.tgnet.TLRPC;

public class StickersUtils implements NotificationCenter.NotificationCenterDelegate {

    private OnStickerSetLoaded onStickerSetLoaded;
    private String stickerSetName;
    private int stickerNum;
    private NotificationCenter notificationCenter;


    public void getStickerAsync(int currentAccount, String stickerSetName, int stickerNum, OnStickerSetLoaded onStickerSetLoaded) {
        notificationCenter = NotificationCenter.getInstance(currentAccount);
        notificationCenter.addObserver(this, NotificationCenter.diceStickersDidLoad);
        this.onStickerSetLoaded = onStickerSetLoaded;
        this.stickerSetName = stickerSetName;
        this.stickerNum = stickerNum;
        TLRPC.TL_messages_stickerSet set = MediaDataController.getInstance(currentAccount).getStickerSetByName(stickerSetName);
        if (set == null) {
            set = MediaDataController.getInstance(currentAccount).getStickerSetByEmojiOrName(stickerSetName);
        }
        if (set == null) {
            MediaDataController.getInstance(currentAccount).loadStickersByEmojiOrName(stickerSetName, false, true);
        } else {
            notificationCenter.removeObserver(this, NotificationCenter.diceStickersDidLoad);
            onStickerSetLoaded.onLoaded(set.documents.get(stickerNum));
        }
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.diceStickersDidLoad) {
            String name = (String) args[0];
            if (stickerSetName.equals(name)) {
                TLRPC.TL_messages_stickerSet set = MediaDataController.getInstance(account).getStickerSetByName(stickerSetName);
                if (set == null) {
                    set = MediaDataController.getInstance(account).getStickerSetByEmojiOrName(stickerSetName);
                }
                if (set != null) {
                    notificationCenter.removeObserver(this, NotificationCenter.diceStickersDidLoad);
                    onStickerSetLoaded.onLoaded(set.documents.get(stickerNum));
                }
            }
        }
    }

    public interface OnStickerSetLoaded {
        void onLoaded(TLRPC.Document document);
    }
}
