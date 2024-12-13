/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

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