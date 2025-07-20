/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android;

import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.Nullable;

import org.telegram.messenger.ApplicationLoader;

import it.octogram.android.utils.OctoLogging;

public class StoreUtils {

    private static final String TAG = "StoreUtils";

    private static final String PLAY_STORE_PACKAGE = "com.android.vending";

    private static final String HUAWEI_STORE_PACKAGE = "com.huawei.appmarket";

    public static boolean isDownloadedFromAnyStore() {
        return isFromPlayStore() || isFromHuaweiStore();
    }


    public static boolean isFromCheckableStore() {
        return isFromPlayStore();
    }

    public static boolean isFromPlayStore() {
        return PLAY_STORE_PACKAGE.equalsIgnoreCase(getInstallerPackageName());
    }

    public static boolean isFromHuaweiStore() {
        return HUAWEI_STORE_PACKAGE.equalsIgnoreCase(getInstallerPackageName());
    }

    private static String getInstallerPackageName() {
        try {
            return getPackageName();
        } catch (PackageManager.NameNotFoundException e) {
            OctoLogging.e(TAG, "Package name not found: " + ApplicationLoader.applicationContext.getPackageName(), e);
            return null;
        } catch (IllegalArgumentException e) {
            OctoLogging.e(TAG, "Invalid package name: " + ApplicationLoader.applicationContext.getPackageName(), e);
            return null;
        } catch (Exception e) {
            OctoLogging.e(TAG, "Error retrieving installer package name for: " + ApplicationLoader.applicationContext.getPackageName(), e);
            return null;
        }
    }

    @Nullable
    private static String getPackageName() throws PackageManager.NameNotFoundException {
        var context = ApplicationLoader.applicationContext;
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();

        String installerPackageName;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            installerPackageName = packageManager.getInstallSourceInfo(packageName).getInstallingPackageName();
        } else {
            installerPackageName = packageManager.getInstallerPackageName(packageName);
        }
        return installerPackageName;
    }
}
