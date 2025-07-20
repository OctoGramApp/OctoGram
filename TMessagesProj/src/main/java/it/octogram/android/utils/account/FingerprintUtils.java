/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.account;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.FingerprintController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.support.fingerprint.FingerprintManagerCompat;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.LaunchActivity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

import it.octogram.android.OctoConfig;
import it.octogram.android.utils.OctoLogging;

public class FingerprintUtils {
    private static final HashMap<String, ArrayList<String>> cachedChatsLists = new HashMap<>();
    private static boolean readFromConfig = false;
    private static final ArrayList<Long> cachedAccountsList = new ArrayList<>();
    private static final ArrayList<BiometricPrompt> pendingAuths = new ArrayList<>();
    private static boolean readFromConfigAccountsList = false;
    private static long lastTimeAskedFingerprint = 0;
    private static final String TAG = "FingerprintUtils";

    public static void fixFingerprint(Context context, FingerprintResult callback) {
        checkFingerprint(context, FingerprintAction.FIX_FINGERPRINT, true, 0, callback);
    }

    public static void checkFingerprint(Context context, @FingerprintAction int reason, FingerprintResult callback) {
        checkFingerprint(context, reason, false, 0, callback);
    }

    public static void checkFingerprint(Context context, @FingerprintAction int reason, boolean ignoreLastTimeAsked, FingerprintResult callback) {
        checkFingerprint(context, reason, ignoreLastTimeAsked, 0, callback);
    }

    public static void checkFingerprint(Context context, @FingerprintAction int reason, boolean useCustomAskEvery, int askEvery, FingerprintResult callback) {
        if (Build.VERSION.SDK_INT < 23) {
            callback.onFailed();
            return;
        }

        if (reason == FingerprintAction.FIX_FINGERPRINT) {
            FingerprintController.checkKeyReady();
            FingerprintController.deleteInvalidKey();
            FingerprintController.checkKeyReady();
        } else {
            if (!hasFingerprintCached()) {
                callback.onSuccess();
                return;
            }

            if (BiometricManager.from(context).canAuthenticate(getAuthenticationStatus()) != BiometricManager.BIOMETRIC_SUCCESS) {
                callback.onFailed();
                return;
            }

            if ((System.currentTimeMillis() - lastTimeAskedFingerprint) / 1000 < (useCustomAskEvery ? askEvery : OctoConfig.INSTANCE.biometricAskEvery.getValue())) {
                callback.onSuccess();
                return;
            }
        }

        cancelPendingAuthentications();

        BiometricPrompt prompt = new BiometricPrompt(LaunchActivity.instance, ContextCompat.getMainExecutor(context), new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errMsgId, @NonNull CharSequence errString) {
                OctoLogging.d(TAG, "PasscodeView onAuthenticationError " + errMsgId + " \"" + errString + "\"");
                callback.onError(errMsgId);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                OctoLogging.d(TAG, "PasscodeView onAuthenticationSucceeded");
                lastTimeAskedFingerprint = System.currentTimeMillis();
                if (FingerprintController.isKeyReady() && FingerprintController.checkDeviceFingerprintsChanged()) {
                    FingerprintController.deleteInvalidKey();
                }
                callback.onSuccess();
            }

            @Override
            public void onAuthenticationFailed() {
                OctoLogging.d(TAG, "PasscodeView onAuthenticationFailed");
                callback.onFailed();
            }
        });
        BiometricPrompt.PromptInfo.Builder builder = new BiometricPrompt.PromptInfo.Builder();
        builder.setTitle(getString(getStringByAction(reason)));
        if (OctoConfig.INSTANCE.allowUsingDevicePIN.getValue()) {
            builder.setAllowedAuthenticators(getAuthenticationStatus() | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
        } else {
            builder.setNegativeButtonText(getString(R.string.Cancel));
            builder.setAllowedAuthenticators(getAuthenticationStatus());
        }
        pendingAuths.add(prompt);
        prompt.authenticate(builder.build());
    }

    public static void cancelPendingAuthentications() {
        for (BiometricPrompt prompt : pendingAuths) {
            prompt.cancelAuthentication();
        }
        pendingAuths.clear();
    }

    @StringRes
    private static int getStringByAction(@FingerprintAction int reason) {
        return switch (reason) {
            case FingerprintAction.EDIT_SETTINGS -> R.string.UnlockToSaveOption;
            case FingerprintAction.OPEN_PAGE -> R.string.UnlockToOpenPage;
            case FingerprintAction.IMPORT_SETTINGS -> R.string.UnlockToImportSettings;
            case FingerprintAction.UNLOCK_ACCOUNT -> R.string.UnlockToSwitchAccount;
            case FingerprintAction.EXPAND_SETTINGS -> R.string.UnlockToExpand;
            case FingerprintAction.FIX_FINGERPRINT -> R.string.BiometricUnavailable_Test;
            default -> -1;
        };
    }

    public static int getAuthenticationStatus() {
        return OctoConfig.INSTANCE.allowUsingFaceUnlock.getValue() ? BiometricManager.Authenticators.BIOMETRIC_WEAK : BiometricManager.Authenticators.BIOMETRIC_STRONG;
    }

    private static boolean isFirstCheck = true;
    private static boolean fingerprintCachedState = false;

    public static boolean hasFingerprintCached() {
        if (isFirstCheck) {
            reloadFingerprintState();
        }

        return fingerprintCachedState;
    }

    public static boolean hasFingerprintSavedState() {
        OctoConfig.INSTANCE.arbitraryLoadConfig(OctoConfig.INSTANCE.hasFingerprintSavedState);
        return OctoConfig.INSTANCE.hasFingerprintSavedState.getValue();
    }

    public static void reloadFingerprintState() {
        isFirstCheck = false;
        fingerprintCachedState = hasFingerprintInternal();
        OctoConfig.INSTANCE.hasFingerprintSavedState.updateValue(fingerprintCachedState);
    }

    private static boolean hasFingerprintInternal() {
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                OctoLogging.d(TAG, "Starting fingerprint check...");

                FingerprintManagerCompat fingerprintManager = FingerprintManagerCompat.from(ApplicationLoader.applicationContext);

                boolean conditions = fingerprintManager.isHardwareDetected();
                OctoLogging.d(TAG, "Fingerprint hardware detected: " + conditions);

                conditions &= fingerprintManager.hasEnrolledFingerprints();
                OctoLogging.d(TAG, "Enrolled fingerprints: " + fingerprintManager.hasEnrolledFingerprints());

                conditions &= FingerprintController.isKeyReady();
                OctoLogging.d(TAG, "Fingerprint key ready: " + FingerprintController.isKeyReady());

                conditions &= !FingerprintController.checkDeviceFingerprintsChanged();
                OctoLogging.d(TAG, "Device fingerprints changed: " + !FingerprintController.checkDeviceFingerprintsChanged());

                OctoLogging.d(TAG, "Final fingerprint check result: " + conditions);
                return conditions;
            } catch (Throwable e) {
                OctoLogging.e("Error checking fingerprint availability", e);
            }
        } else {
            OctoLogging.d(TAG, "Fingerprint check skipped: SDK version < 23");
        }
        return false;
    }

    private static String getState(TLRPC.User currentUser, TLRPC.Chat currentChat) {
        if (currentUser != null) {
            if (currentUser.id == UserConfig.getInstance(UserConfig.selectedAccount).clientUserId) {
                return "u_sm";
            }
            return "u_" + currentUser.id;
        } else if (currentChat != null) {
            return "g_" + (-currentChat.id);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private static String getState(LockedChat lockedChat) {
        if (lockedChat.chat() == null && lockedChat.user() == null) {
            return "";
        }

        return getState(lockedChat.user(), lockedChat.chat());
    }

    private static String getState(TLRPC.Dialog dialog) {
        return getState(dialog.id);
    }

    private static String getState(MessagesStorage.TopicKey dialog) {
        return getState(dialog.dialogId);
    }

    private static String getState(long id) {
        if (DialogObject.isUserDialog(id)) {
            if (id == UserConfig.getInstance(UserConfig.selectedAccount).clientUserId) {
                return "u_sm";
            }
            return "u_" + id;
        } else {
            return "g_" + id;
        }
    }

    private static String getState(Bundle args) {
        if (args.containsKey("user_id") && args.getLong("user_id") != 0) {
            if (args.getLong("user_id") == UserConfig.getInstance(UserConfig.selectedAccount).clientUserId) {
                return "u_sm";
            }
            return "u_" + args.getLong("user_id");
        } else if (args.containsKey("chat_id") && args.getLong("chat_id") != 0) {
            return "g_" + (-args.getLong("chat_id"));
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private static String getAccountId() {
        return "U" + UserConfig.getInstance(UserConfig.selectedAccount).clientUserId;
    }

    private static String getAccountId(long accountId) {
        return "U" + accountId;
    }

    public static boolean isChatLocked(TLRPC.User currentUser, TLRPC.Chat currentChat) {
        return isChatLocked(getState(currentUser, currentChat));
    }

    public static boolean isChatLocked(TLRPC.Dialog dialog) {
        return isChatLocked(getState(dialog));
    }

    public static boolean isChatLocked(MessagesStorage.TopicKey dialog) {
        return isChatLocked(getState(dialog));
    }

    public static boolean isChatLocked(long id) {
        return isChatLocked(getState(id));
    }

    public static boolean isChatLocked(Bundle args) {
        return args != null && isChatLocked(getState(args));
    }

    public static boolean isChatLocked(String chatState) {
        reloadLockedChatsList();
        return cachedChatsLists.containsKey(getAccountId()) && Objects.requireNonNull(cachedChatsLists.get(getAccountId())).contains(chatState);
    }

    public static boolean isChatLockedNotifications(MessageObject object) {
        String check = "";
        if (object.getFromChatId() == object.getChatId() || (object.getChatId() == 0 && object.getFromChatId() != 0)) {
            if (object.getFromChatId() == UserConfig.getInstance(UserConfig.selectedAccount).clientUserId) {
                check = "u_sm";
            } else {
                check = "u_" + object.getFromChatId();
            }
        } else if (object.getChatId() != 0) {
            check = "g_-" + object.getChatId();
        }

        if (check.isEmpty()) {
            return false;
        }

        reloadLockedChatsList();
        for (String key : cachedChatsLists.keySet()) {
            if (Objects.requireNonNull(cachedChatsLists.get(key)).contains(check)) {
                return true;
            }
        }
        return false;
    }

    public static void lockChat(TLRPC.User currentUser, TLRPC.Chat currentChat, boolean state) {
        lockChat(getState(currentUser, currentChat), state);
    }

    public static void lockChat(LockedChat lockedChat, boolean state) {
        lockChat(getState(lockedChat), state);
    }

    public static void lockChat(long id, boolean state) {
        lockChat(getState(id), state);
    }

    public static void lockChat(String chatState, boolean state) {
        if (chatState.isEmpty()) {
            return;
        }

        reloadLockedChatsList();

        ArrayList<String> cachedChatsListForAccount = cachedChatsLists.containsKey(getAccountId()) ? cachedChatsLists.get(getAccountId()) : new ArrayList<>();
        assert cachedChatsListForAccount != null;
        int originalChatStatesLength = cachedChatsListForAccount.size();

        if (state && !cachedChatsListForAccount.contains(chatState)) {
            cachedChatsListForAccount.add(chatState);
        } else if (!state) {
            cachedChatsListForAccount.remove(chatState);
        }

        if (cachedChatsListForAccount.size() != originalChatStatesLength) {
            if (cachedChatsListForAccount.isEmpty()) {
                cachedChatsLists.remove(getAccountId());
            } else {
                cachedChatsLists.put(getAccountId(), cachedChatsListForAccount);
            }
            OctoConfig.INSTANCE.hiddenChats.updateValue(new Gson().toJson(cachedChatsLists));
        }
    }

    public static void lockChatsMultiFromIDs(ArrayList<Long> chatIds, boolean state) {
        ArrayList<String> states = new ArrayList<>();
        for (long chatId : chatIds) {
            if (DialogObject.isEncryptedDialog(chatId)) {
                continue;
            }

            states.add(getState(chatId));
        }
        if (!states.isEmpty()) {
            lockChatsMulti(states, state);
        }
    }

    public static void lockChatsMulti(ArrayList<String> chatStates, boolean state) {
        reloadLockedChatsList();

        ArrayList<String> cachedChatsListForAccount = cachedChatsLists.containsKey(getAccountId()) ? cachedChatsLists.get(getAccountId()) : new ArrayList<>();
        assert cachedChatsListForAccount != null;
        int originalChatStatesLength = cachedChatsListForAccount.size();

        if (state) {
            for (String chatState : chatStates) {
                if (!cachedChatsListForAccount.contains(chatState)) {
                    cachedChatsListForAccount.add(chatState);
                }
            }
        } else {
            cachedChatsListForAccount.removeIf(chatStates::contains);
        }

        if (cachedChatsListForAccount.size() != originalChatStatesLength) {
            if (cachedChatsListForAccount.isEmpty()) {
                cachedChatsLists.remove(getAccountId());
            } else {
                cachedChatsLists.put(getAccountId(), cachedChatsListForAccount);
            }
            OctoConfig.INSTANCE.hiddenChats.updateValue(new Gson().toJson(cachedChatsLists));
        }
    }

    public static void clearLockedChats() {
        readFromConfig = true;
        cachedChatsLists.clear();
        OctoConfig.INSTANCE.hiddenChats.clear();
    }

    private static void reloadLockedChatsList() {
        if (readFromConfig) {
            return;
        }

        cachedChatsLists.clear();
        readFromConfig = true;
        try {
            String value = OctoConfig.INSTANCE.hiddenChats.getValue();

            //Gson().fromJson would be a valid alternative, but using this specific method allows
            // to check local saved values before importing them

            ArrayList<String> allowedAccountIds = new ArrayList<>();
            for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
                long id = UserConfig.getInstance(a).clientUserId;
                if (id > 0) {
                    allowedAccountIds.add(getAccountId(id));
                }
            }

            JSONObject jsonObject = new JSONObject(new JSONTokener(value));
            for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
                String key = it.next();

                if (!allowedAccountIds.contains(key)) {
                    continue;
                }

                try {
                    JSONArray lockedChatsForAccount = jsonObject.getJSONArray(key);
                    ArrayList<String> tempList = new ArrayList<>();

                    for (int i = 0; i < lockedChatsForAccount.length(); i++) {
                        try {
                            tempList.add(lockedChatsForAccount.getString(i));
                        } catch (JSONException ignored) {
                        }
                    }

                    if (!tempList.isEmpty()) {
                        cachedChatsLists.put(key, tempList);
                    }
                } catch (JSONException ignored) {
                }
            }
        } catch (JSONException ignored) {
        }
    }

    public static boolean hasLockedChats() {
        reloadLockedChatsList();
        return cachedChatsLists.containsKey(getAccountId()) && !Objects.requireNonNull(cachedChatsLists.get(getAccountId())).isEmpty();
    }

    public static int getLockedChatsCount() {
        reloadLockedChatsList();
        return !cachedChatsLists.containsKey(getAccountId()) ? 0 : Objects.requireNonNull(cachedChatsLists.get(getAccountId())).size();
    }

    public static ArrayList<LockedChat> getLockedChats() {
        ArrayList<LockedChat> lockedChats = new ArrayList<>();
        reloadLockedChatsList();

        if (cachedChatsLists.containsKey(getAccountId())) {
            MessagesController messagesController = MessagesController.getInstance(UserConfig.selectedAccount);
            for (String state : Objects.requireNonNull(cachedChatsLists.get(getAccountId()))) {
                if (state.startsWith("u_")) {
                    if (state.equals("u_sm")) {
                        state = "u_" + UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId();
                    }
                    TLRPC.User user = messagesController.getUser(Long.parseLong(state.split("u_")[1]));
                    if (user != null) {
                        lockedChats.add(new LockedChat(null, user));
                    }
                } else if (state.startsWith("g_")) {
                    TLRPC.Chat chat = messagesController.getChat(-Long.parseLong(state.split("g_")[1]));
                    if (chat != null) {
                        lockedChats.add(new LockedChat(chat, null));
                    }
                }
            }
        }

        return lockedChats;
    }

    public static ArrayList<Long> getLockedChatsIds() {
        ArrayList<Long> lockedChats = new ArrayList<>();
        reloadLockedChatsList();

        if (cachedChatsLists.containsKey(getAccountId())) {
            for (String state : Objects.requireNonNull(cachedChatsLists.get(getAccountId()))) {
                if (state.startsWith("u_")) {
                    if (state.equals("u_sm")) {
                        state = "u_" + UserConfig.getInstance(UserConfig.selectedAccount).getClientUserId();
                    }
                    lockedChats.add(Long.parseLong(state.split("u_")[1]));
                } else if (state.startsWith("g_")) {
                    lockedChats.add(Long.parseLong(state.split("g_")[1]));
                }
            }
        }

        return lockedChats;
    }

    private static void reloadLockedAccountsList() {
        if (readFromConfigAccountsList) {
            return;
        }

        cachedAccountsList.clear();
        readFromConfigAccountsList = true;
        try {
            String value = OctoConfig.INSTANCE.hiddenAccounts.getValue();
            JSONArray jsonObject = new JSONArray(new JSONTokener(value));
            for (int i = 0; i < jsonObject.length(); i++) {
                try {
                    cachedAccountsList.add(jsonObject.getLong(i));
                } catch (JSONException ignored) {
                }
            }
        } catch (JSONException ignored) {
        }
    }

    public static boolean isAccountLocked(Long accountId) {
        reloadLockedAccountsList();
        return cachedAccountsList.contains(accountId);
    }

    public static boolean isAccountLockedByNumber(int accountNumber) {
        reloadLockedAccountsList();
        long user = UserConfig.getInstance(accountNumber).clientUserId;
        return user != 0 && cachedAccountsList.contains(user);
    }

    public static void lockAccount(Long accountId, boolean state) {
        reloadLockedChatsList();

        int originalAccountStatesLength = cachedAccountsList.size();

        if (state && !cachedAccountsList.contains(accountId)) {
            cachedAccountsList.add(accountId);
        } else if (!state) {
            cachedAccountsList.remove(accountId);
        }

        if (cachedAccountsList.size() != originalAccountStatesLength) {
            OctoConfig.INSTANCE.hiddenAccounts.updateValue(cachedAccountsList.toString());
        }
    }

    public static boolean hasLockedAccounts() {
        reloadLockedAccountsList();

        ArrayList<Long> availableAccounts = new ArrayList<>();
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            TLRPC.User u = UserConfig.getInstance(a).getCurrentUser();
            if (u != null) {
                availableAccounts.add(u.id);
            }
        }

        int originalAccountStatesLength = cachedAccountsList.size();
        cachedAccountsList.removeIf((x) -> !availableAccounts.contains(x));
        if (cachedAccountsList.size() != originalAccountStatesLength) {
            OctoConfig.INSTANCE.hiddenAccounts.updateValue(cachedAccountsList.toString());
        }

        return !cachedAccountsList.isEmpty();
    }

    @IntDef({FingerprintAction.EDIT_SETTINGS, FingerprintAction.OPEN_PAGE, FingerprintAction.IMPORT_SETTINGS, FingerprintAction.UNLOCK_ACCOUNT, FingerprintAction.EXPAND_SETTINGS, FingerprintAction.FIX_FINGERPRINT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FingerprintAction {
        int EDIT_SETTINGS = 1, OPEN_PAGE = 2, IMPORT_SETTINGS = 3, UNLOCK_ACCOUNT = 4, EXPAND_SETTINGS = 5, FIX_FINGERPRINT = 6;
    }

    public interface FingerprintResult {
        void onSuccess();

        default void onFailed() {
        }

        default void onError(int error) {
            onFailed();
        }
    }

    public record LockedChat(TLRPC.Chat chat, TLRPC.User user) {
    }
}
