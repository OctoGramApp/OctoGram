/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.messenger;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.telegram.ui.LaunchActivity;

import java.util.Map;
import java.util.Objects;

import it.octogram.android.OctoConfig;
import it.octogram.android.utils.UpdatesManager;

public class GcmPushListenerService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage message) {
        String from = message.getFrom();
        Map<String, String> data = message.getData();
        long time = message.getSentTime();

        if (BuildVars.LOGS_ENABLED) {
            FileLog.d("FCM received data: " + data + " from: " + from);
        }

        if (data.containsKey("loc_key") && Objects.equals(data.get("loc_key"), "NEW_UPDATE")) {
            if (OctoConfig.INSTANCE.autoCheckUpdateStatus.getValue() || UpdatesManager.INSTANCE.canReceivePrivateBetaUpdates()) {
                LaunchActivity.instance.checkAppUpdate(false, null);
            }

            return;
        }

        PushListenerController.processRemoteMessage(PushListenerController.PUSH_TYPE_FIREBASE, data.get("p"), time);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        AndroidUtilities.runOnUIThread(() -> {
            if (BuildVars.LOGS_ENABLED) {
                FileLog.d("Refreshed FCM token: " + token);
            }
            ApplicationLoader.postInitApplication();
            PushListenerController.sendRegistrationToServer(PushListenerController.PUSH_TYPE_FIREBASE, token);
        });
    }
}
