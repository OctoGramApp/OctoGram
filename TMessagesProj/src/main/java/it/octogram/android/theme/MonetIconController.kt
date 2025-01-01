/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.theme

import android.os.Build
import org.telegram.ui.LauncherIconController


object MonetIconController {
    fun isSelectedMonet(): Boolean {
        return LauncherIconController.isEnabled(LauncherIconController.LauncherIcon.MONET)
    }

    fun needMonetMigration(): Boolean {
        return isSelectedMonet() && Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2
    }

    fun switchToMonet() {
        if (isSelectedMonet()) {
            LauncherIconController.setIcon(LauncherIconController.LauncherIcon.DEFAULT)
        } else {
            LauncherIconController.setIcon(LauncherIconController.LauncherIcon.MONET)
        }
    }
}
