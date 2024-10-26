package it.octogram.android.utils;

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
}
