/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.translator.localhelper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import androidx.annotation.NonNull;

import org.telegram.ui.LaunchActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.octogram.android.utils.OctoLogging;
import it.octogram.android.utils.OctoUtils;

public class OnDeviceHelper {
    private static final String TAG =  "OnDeviceHelper";
    private static final String PACKAGE_NAME = "it.octogram.ondevice";
    private static final String ACTION_TRANSLATE = "it.octogram.ondevice.ACTION_TRANSLATE";
    private static final String TRANSLATOR_RECEIVER_SERVICE = "it.octogram.ondevice.TranslatorReceiver";

    public static final int ID_ACTION_TRANSLATE = 1;
    public static final int ID_ACTION_LIST_MODELS = 2;
    public static final int ID_ACTION_DELETE_MODEL = 3;
    public static final int ID_ACTION_DELETE_ALL_MODELS = 4;
    public static final String MIN_ONDEVICE_VERSION= "1.0.0";
    public static final int MIN_ONDEVICE_VERSION_CODE = 5;

    private static Messenger mlKitServiceMessenger = null;
    private static final ArrayList<Message> pendingMessages = new ArrayList<>();
    private static final HashMap<String, OnMessageReceived> messagesCallbacks = new HashMap<>();
    private static final Messenger replyMessenger = new Messenger(new ReplyHandler(Looper.getMainLooper()));

    private static boolean didInitSecondaryApp = false;
    private static final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            OctoLogging.e(TAG, "service connected");
            mlKitServiceMessenger = new Messenger(service);
            didInitSecondaryApp = true;

            for (Message msg : pendingMessages) {
                try {
                    OctoLogging.d(TAG, "Sending pending translation message to service");
                    mlKitServiceMessenger.send(msg);
                } catch (RemoteException e) {
                    OctoLogging.e(TAG, "Error sending pending message to ML Kit Service: " + e.getMessage());
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            OctoLogging.e(TAG, "service disconnected");
            mlKitServiceMessenger = null;
            didInitSecondaryApp = false;
        }
    };

    private static void initConnection() {
        if (!didInitSecondaryApp) {
            didInitSecondaryApp = true;
            OctoLogging.e(TAG, "app bind!");
            Intent intent = new Intent("it.octogram.ondevice.ACTION_TRANSLATE");
            intent.setPackage("it.octogram.ondevice");
            try {
                boolean state = LaunchActivity.instance.bindService(intent, connection, Context.BIND_AUTO_CREATE);
                if (!state) {
                    didInitSecondaryApp = false;
                    declareGeneralFailure();
                }
            } catch (SecurityException e) {
                OctoLogging.e(TAG, "Permission denied when binding service: " + e.getMessage());
                declareGeneralFailure();
            }
        }
    }

    private static void declareGeneralFailure() {
        OctoLogging.e(TAG, "Failed to bind to the ML Kit service. Declaring total failure.");
        for (OnMessageReceived callback : messagesCallbacks.values()) {
            callback.onDeclareTotalFailure();
        }
        didInitSecondaryApp = false;
        messagesCallbacks.clear();
        pendingMessages.clear();
        mlKitServiceMessenger = null;
    }

    public static void sendMessageWithoutResponse(Message msg) {
        sendMessage(msg, null);
    }

    public static void sendMessage(Message msg, OnMessageReceived callback) {
        initConnection();
        msg.replyTo = replyMessenger;

        String reqID = OctoUtils.generateRandomString().replace("-", "");
        Bundle data = msg.getData();
        if (data == null) {
            data = new Bundle();
        }
        data.putString("request_id", reqID);
        msg.setData(data);

        if (callback != null) {
            messagesCallbacks.put(reqID, callback);
        }

        if (mlKitServiceMessenger != null) {
            try {
                OctoLogging.d(TAG, "Sending " + msg.what + " message to service");
                mlKitServiceMessenger.send(msg);
            } catch (RemoteException e) {
                OctoLogging.e(TAG, "Error sending " + msg.what + " message: " + e.getMessage());
            }
        } else {
            OctoLogging.d(TAG, "ML Kit service not yet connected, adding listModels to pending.");
            pendingMessages.add(msg);
        }
    }

    public static boolean checkVersionCode(Context context, int minVersionCode) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(PACKAGE_NAME, 0);

            int versionCode;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                versionCode = (int) info.getLongVersionCode();
            } else {
                versionCode = info.versionCode;
            }

            OctoLogging.d(TAG, "Found package " + PACKAGE_NAME + " with versionCode: " + versionCode);

            if (versionCode >= minVersionCode) {
                return true;
            } else {
                OctoLogging.w(TAG, "Package versionCode too low: " + versionCode + " < " + minVersionCode);
                return false;
            }
        } catch (PackageManager.NameNotFoundException e) {
            OctoLogging.w(TAG, "Package not found: " + PACKAGE_NAME);
            return false;
        }
    }

    public static boolean isAvailable(Context context) {
        final PackageManager pm = context.getPackageManager();
        OctoLogging.d(TAG, "Checking availability of package: " + PACKAGE_NAME);

        ApplicationInfo info;
        try {
            info = pm.getApplicationInfo(PACKAGE_NAME, 0);
            OctoLogging.d(TAG, "Application info found for: " + PACKAGE_NAME);
        } catch (PackageManager.NameNotFoundException e) {
            OctoLogging.w(TAG, "Package not found: " + PACKAGE_NAME);
            return false;
        }

        if (!info.enabled) {
            OctoLogging.w(TAG, "Package is installed but disabled: " + PACKAGE_NAME);
            return false;
        } else {
            OctoLogging.d(TAG, "Package is enabled: " + PACKAGE_NAME);
        }

        Intent intent = new Intent(ACTION_TRANSLATE).setPackage(PACKAGE_NAME);
        OctoLogging.d(TAG, "Querying services for intent: " + intent);

        List<ResolveInfo> services = pm.queryIntentServices(intent, PackageManager.GET_SERVICES);
        OctoLogging.d(TAG, "Number of services resolved: " + services.size());

        if (services.isEmpty()) {
            OctoLogging.w(TAG, "No services resolved for action: " + ACTION_TRANSLATE);
            return false;
        }

        boolean matched = false;
        for (ResolveInfo service : services) {
            String serviceName = (service.serviceInfo != null) ? service.serviceInfo.name : null;
            OctoLogging.d(TAG, "Found service: " + serviceName);

            if (TRANSLATOR_RECEIVER_SERVICE.equals(serviceName)) {
                OctoLogging.d(TAG, "Expected service matched: " + serviceName);
                matched = true;
                break;
            } else if (serviceName != null) {
                OctoLogging.w(TAG, "Unexpected service: " + serviceName + " (expected: " + TRANSLATOR_RECEIVER_SERVICE + ")");
            }
        }

        OctoLogging.d(TAG, "Service availability check completed. Matched: " + matched);
        return matched;
    }

    public interface OnMessageReceived {
        boolean onMessageReceived(Message msg);
        void onDeclareTotalFailure();
    }

    private static class ReplyHandler extends Handler {
        ReplyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            OctoLogging.e(TAG, "reply handler called");
            OctoLogging.d(TAG, "Received msg: what=" + msg.what + " from=" + msg.replyTo);
            OctoLogging.d(TAG, "mlKitServiceMessenger: " + mlKitServiceMessenger);

            if (mlKitServiceMessenger == null || msg.what != 0) {
                return;
            }

            OctoLogging.d(TAG, "Processed state");
            Bundle bundle = msg.getData();
            String reqID = bundle.getString("request_id");

            if (reqID == null || reqID.isEmpty()) {
                return;
            }

            OnMessageReceived callback = messagesCallbacks.get(reqID);
            if (callback != null) {
                boolean result = callback.onMessageReceived(msg);
                if (result) {
                    messagesCallbacks.remove(reqID);
                }
            }
        }
    }
}
