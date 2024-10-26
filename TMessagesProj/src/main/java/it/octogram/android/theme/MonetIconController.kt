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
