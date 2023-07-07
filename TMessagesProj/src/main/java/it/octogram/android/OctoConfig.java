/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */
package it.octogram.android;

import android.app.Activity;
import android.content.SharedPreferences;

import org.telegram.messenger.ApplicationLoader;

public class OctoConfig {

    private static final SharedPreferences PREFS = ApplicationLoader.applicationContext.getSharedPreferences("octoconfig", Activity.MODE_PRIVATE);
    private static final Object sync = new Object();
    public static boolean hidePhoneNumber = false;
    public static boolean showPreviews = true;

    static {
        loadConfig();
    }

    public static void loadConfig() {
        synchronized (sync) {
            hidePhoneNumber = PREFS.getBoolean("hidePhoneNumber", false);
            showPreviews = PREFS.getBoolean("showPreviews", true);
        }
    }


}
