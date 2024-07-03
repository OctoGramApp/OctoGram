package it.octogram.android;

import org.telegram.messenger.ApplicationLoader;

public class StoreUtils {
    public static boolean isDownloadedFromAnyStore() {
        return isFromPlayStore() || isFromHuaweiStore();
    }

    public static boolean isFromCheckableStore() {
        return isFromPlayStore();
    }

    public static boolean isFromPlayStore() {
        return "com.android.vending".equals(ApplicationLoader.applicationContext.getPackageManager().getInstallerPackageName(ApplicationLoader.getApplicationId()));
    }

    public static boolean isFromHuaweiStore() {
        return "com.huawei.appmarket".equals(ApplicationLoader.applicationContext.getPackageManager().getInstallerPackageName(ApplicationLoader.getApplicationId()));
    }
}
