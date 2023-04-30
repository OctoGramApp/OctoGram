package it.octogram.android.utils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Base64;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;

import java.util.Map;

public class SharedPreferencesHelper {
    private static final SharedPreferences preferences;
    private static Runnable changeListener;
    private final static int GROUPING_BACKUP_TIMEOUT = 250;
    private static Runnable groupingRunnable;

    static {
        preferences = ApplicationLoader.applicationContext.getSharedPreferences("octogram", Activity.MODE_PRIVATE);
    }

    protected static void putValue(String key, Object value) {
        SharedPreferences.Editor editor = preferences.edit();
        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof byte[]) {
            editor.putString(key, Base64.encodeToString((byte[]) value, Base64.DEFAULT));
        }
        editor.apply();
        onSharedPreferenceChanged();
    }

    private static void onSharedPreferenceChanged() {
        if (SharedPreferencesHelper.changeListener != null) {
            if (groupingRunnable != null) {
                AndroidUtilities.cancelRunOnUIThread(groupingRunnable);
            }
            groupingRunnable = () -> {
                SharedPreferencesHelper.changeListener.run();
                groupingRunnable = null;
            };
            AndroidUtilities.runOnUIThread(groupingRunnable, GROUPING_BACKUP_TIMEOUT);
        }
    }

    protected static String getString(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }

    protected static byte[] getByteArray(String key) {
        String value = preferences.getString(key, null);
        if (value != null) {
            try {
                return Base64.decode(value, Base64.DEFAULT);
            } catch (IllegalArgumentException ignored) {}
        }
        return null;
    }

    protected static int getInt(String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }

    protected static boolean getBoolean(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    protected static long getLong(String key, long defaultValue) {
        try {
            return preferences.getLong(key, defaultValue);
        } catch (ClassCastException e) {
            return getInt(key, (int) defaultValue);
        }
    }

    protected static Map<String, ?> getAll() {
        return preferences.getAll();
    }
    
    protected static void remove(String key) {
        preferences.edit().remove(key).apply();
        onSharedPreferenceChanged();
    }

    protected static void registerOnSharedPreferenceChangeListener(Runnable listener) {
        SharedPreferencesHelper.changeListener = listener;
    }
}
