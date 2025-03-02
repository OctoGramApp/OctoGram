/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui.custom.doublebottom;

import android.content.Context;
import android.content.SharedPreferences;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.SharedConfig;

public class PasscodeController {
    private static final SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("OwlPasscode", Context.MODE_PRIVATE);



    public static boolean isProtectedAccount(long accountId) {
        return preferences.contains("passcodeHash" + accountId) && preferences.contains("passcodeSalt" + accountId) && !SharedConfig.passcodeHash.isEmpty();
    }

}