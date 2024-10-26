package it.octogram.android

import android.os.Build
import org.telegram.messenger.ApplicationLoader

object StoreUtils {

    fun isDownloadedFromAnyStore(): Boolean {
        return isFromPlayStore() || isFromHuaweiStore()
    }

    fun isFromCheckableStore(): Boolean {
        return isFromPlayStore()
    }

    fun isFromPlayStore(): Boolean {
        return getInstallerPackageName().equals("com.android.vending")
    }

    fun isFromHuaweiStore(): Boolean {
        return getInstallerPackageName().equals("com.huawei.appmarket")
    }

    private fun getInstallerPackageName(): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ApplicationLoader.applicationContext.packageManager
                    .getInstallSourceInfo(ApplicationLoader.getApplicationId()).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                ApplicationLoader.applicationContext.packageManager
                    .getInstallerPackageName(ApplicationLoader.getApplicationId())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}