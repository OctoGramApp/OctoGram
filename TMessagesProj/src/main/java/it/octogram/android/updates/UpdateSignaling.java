package it.octogram.android.updates;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

import it.octogram.android.OctoConfig;

public class UpdateSignaling {
    public static void checkWasUpdated() {
        String oldBuildVersion = OctoConfig.oldBuildVersion;
        int selected_account = UserConfig.selectedAccount;
        if (oldBuildVersion == null) {
            for (int i = 0; i < UserConfig.getActivatedAccountsCount(); i++) {
                MessagesController messagesController = AccountInstance.getInstance(i).getMessagesController();
                messagesController.loadRemoteFilters(true);
                if (messagesController.suggestedFilters.isEmpty()) {
                    messagesController.loadSuggestedFilters();
                }
            }
        }
        if ((oldBuildVersion != null && oldBuildVersion.equals(OctoConfig.currentNotificationVersion())) || BuildConfig.DEBUG_PRIVATE_VERSION) {
            return;
        }
        UpdateManager.getChangelogs(updateResult -> {
            TLRPC.TL_updateServiceNotification update = new TLRPC.TL_updateServiceNotification();
            update.message = updateResult.first;
            update.entities = updateResult.second;
            update.media = new TLRPC.TL_messageMediaEmpty();
            update.type = "update";
            update.popup = false;
            update.flags |= 2;
            update.inbox_date = ConnectionsManager.getInstance(selected_account).getCurrentTime();
            ArrayList<TLRPC.Update> updates = new ArrayList<>();
            updates.add(update);
            MessagesController.getInstance(selected_account).processUpdateArray(updates, null, null, false, 0);
            OctoConfig.updateCurrentVersion();
        });
    }
}
