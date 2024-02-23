package it.octogram.android.preferences.ui.custom.doublebottom;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.ui.LaunchActivity;

import java.nio.charset.StandardCharsets;

public class PasscodeController {
    private static final SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("OwlPasscode", Context.MODE_PRIVATE);

    public static boolean existAtLeastOnePasscode() {
        return !preferences.getAll().isEmpty();
    }

    public static boolean checkIsAlreadySetPasscode(String passcode) {
        boolean defaultReturn = checkPasscodeHash(passcode, SharedConfig.passcodeHash, Base64.encodeToString(SharedConfig.passcodeSalt, Base64.DEFAULT));
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            UserConfig userConfig = UserConfig.getInstance(a);
            if (userConfig.isClientActivated() && isProtectedAccount(userConfig.getClientUserId())) {
                String passcodeHash = preferences.getString("passcodeHash" + userConfig.getClientUserId(), "");
                String passcodeSaltString = preferences.getString("passcodeSalt" + userConfig.getClientUserId(), "");
                if (checkPasscodeHash(passcode, passcodeHash, passcodeSaltString)) {
                    return true;
                }
            }
        }
        return defaultReturn;
    }

    public static void setPasscodeForAccount(String firstPassword, long accountId) {
        try {
            byte[] passcodeSalt = new byte[16];
            Utilities.random.nextBytes(passcodeSalt);
            byte[] passcodeBytes = firstPassword.getBytes(StandardCharsets.UTF_8);
            byte[] bytes = new byte[32 + passcodeBytes.length];
            System.arraycopy(passcodeSalt, 0, bytes, 0, 16);
            System.arraycopy(passcodeBytes, 0, bytes, 16, passcodeBytes.length);
            System.arraycopy(passcodeSalt, 0, bytes, passcodeBytes.length + 16, 16);
            preferences.edit()
                    .putString("passcodeHash" + accountId, Utilities.bytesToHex(Utilities.computeSHA256(bytes, 0, bytes.length)))
                    .putString("passcodeSalt" + accountId, Base64.encodeToString(passcodeSalt, Base64.DEFAULT))
                    .apply();
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    public static boolean checkPasscode(Activity activity, String passcode) {
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            UserConfig userConfig = UserConfig.getInstance(a);
            if (userConfig.isClientActivated() && isProtectedAccount(userConfig.getClientUserId())) {
                String passcodeHash = preferences.getString("passcodeHash" + userConfig.getClientUserId(), "");
                String passcodeSaltString = preferences.getString("passcodeSalt" + userConfig.getClientUserId(), "");
                if (checkPasscodeHash(passcode, passcodeHash, passcodeSaltString)) {
                    if (activity instanceof LaunchActivity) {
                        LaunchActivity launchActivity = (LaunchActivity) activity;
                        launchActivity.switchToAccount(a, true);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean checkPasscodeHash(String passcode, String passcodeHash, String passcodeSaltString) {
        try {
            byte[] passcodeSalt;
            if (!passcodeSaltString.isEmpty()) {
                passcodeSalt = Base64.decode(passcodeSaltString, Base64.DEFAULT);
            } else {
                passcodeSalt = new byte[0];
            }
            byte[] passcodeBytes = passcode.getBytes(StandardCharsets.UTF_8);
            byte[] bytes = new byte[32 + passcodeBytes.length];
            System.arraycopy(passcodeSalt, 0, bytes, 0, 16);
            System.arraycopy(passcodeBytes, 0, bytes, 16, passcodeBytes.length);
            System.arraycopy(passcodeSalt, 0, bytes, passcodeBytes.length + 16, 16);
            String hash = Utilities.bytesToHex(Utilities.computeSHA256(bytes, 0, bytes.length));
            return passcodeHash.equals(hash);
        } catch (Exception e) {
            FileLog.e(e);
        }
        return false;
    }

    public static void removePasscodeForAccount(long accountId) {
        preferences.edit()
                .remove("passcodeHash" + accountId)
                .remove("passcodeSalt" + accountId)
                .apply();
    }

    public static boolean isProtectedAccount(long accountId) {
        return preferences.contains("passcodeHash" + accountId) && preferences.contains("passcodeSalt" + accountId) && !SharedConfig.passcodeHash.isEmpty();
    }

    public static void disableAccountProtection() {
        preferences.edit().clear().apply();
    }

    public static int getUnprotectedAccounts() {
        int count = 0;
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            UserConfig userConfig = UserConfig.getInstance(a);
            if (userConfig.isClientActivated() && !isProtectedAccount(userConfig.getClientUserId())) {
                count++;
            }
        }
        return count;
    }
}