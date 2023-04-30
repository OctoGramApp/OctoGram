package it.octogram.android;

import android.os.Build;

import org.telegram.ui.LauncherIconController;

public class MonetIconController {

    public static boolean isSelectedMonet() {
        return LauncherIconController.isEnabled(LauncherIconController.LauncherIcon.MONET);
    }

    public static boolean needMonetMigration() {
        return isSelectedMonet() && Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2;
    }

    public static void switchToMonet() {
        if (isSelectedMonet()) {
            LauncherIconController.setIcon(LauncherIconController.LauncherIcon.DEFAULT);
        } else {
            LauncherIconController.setIcon(LauncherIconController.LauncherIcon.MONET);
        }
    }
}
