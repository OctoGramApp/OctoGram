/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.LaunchActivity;

public class BrowserUtils {
    public static void openBrowserHome(OnHomePageOpened callback) {
        if (SharedConfig.inappBrowser) {
            if (callback != null) {
                callback.onHomePageOpened();
            }

            Browser.openUrl(LaunchActivity.instance, getDefaultBrowserHome());
            return;
        }

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(LaunchActivity.instance);
        alertBuilder.setTitle(LocaleController.getString(R.string.OctoTgBrowserOpenFail));
        alertBuilder.setMessage(LocaleController.getString(R.string.OctoTgBrowserOpenFail_Desc));
        alertBuilder.setPositiveButton(LocaleController.getString(R.string.OK), null);
        alertBuilder.show();
    }

    public static void openBrowserHome() {
        openBrowserHome(null);
    }

    public interface OnHomePageOpened {
        void onHomePageOpened();
    }

    public static String getDefaultBrowserHome() {
        int engineType = SharedConfig.searchEngineType + 1;
        String searchUrl = LocaleController.getString("SearchEngine" + engineType + "SearchURL");
        String host = AndroidUtilities.getHostAuthority(searchUrl);
        return "https://"+host;
    }

    public static boolean isUsingWifi() {
        ConnectivityManager connectivityManager = (ConnectivityManager) LaunchActivity.instance.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return false;
        }

        try {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isAvailable() && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
        } catch (SecurityException e) {
            return false;
        }
    }
}
