/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.theme

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import it.octogram.android.logs.OctoLogging
import org.telegram.ui.LauncherIconController

object MonetIconController {

    private const val TAG = "MonetIconController"

    fun isSelectedMonet(): Boolean {
        return LauncherIconController.isEnabled(LauncherIconController.LauncherIcon.MONET)
    }

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
    fun needMonetMigration(): Boolean {
        return isSelectedMonet() && Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2
    }

    fun switchToMonet() {
        val targetIcon = if (isSelectedMonet()) {
            LauncherIconController.LauncherIcon.DEFAULT
        } else {
            LauncherIconController.LauncherIcon.MONET
        }
        try {
            LauncherIconController.setIcon(targetIcon)
        } catch (e: Exception) {
            OctoLogging.e(TAG, "Error switching launcher icon to $targetIcon", e)
        }
    }
}
