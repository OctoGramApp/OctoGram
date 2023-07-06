package it.octogram.android;

import android.app.Activity;
import android.content.SharedPreferences;

import org.telegram.messenger.ApplicationLoader;

public class OctoConfig {

    private static final SharedPreferences PREFS = ApplicationLoader.applicationContext.getSharedPreferences("octoconfig", Activity.MODE_PRIVATE);

    public static boolean hidePhoneNumber = false;
    public static boolean showPreviews = true;

    private static final Object sync = new Object();

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
