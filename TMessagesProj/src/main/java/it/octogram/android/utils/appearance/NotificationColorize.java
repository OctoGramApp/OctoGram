/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.appearance;

import android.graphics.Color;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

import it.octogram.android.OctoConfig;

public class NotificationColorize {

    public static int parseNotificationColor() {
        if (OctoConfig.INSTANCE.accentColorAsNotificationColor.getValue()) {
            int color = 0;
            if (Theme.getActiveTheme().hasAccentColors()) {
                color = Theme.getActiveTheme().getAccentColor(Theme.getActiveTheme().currentAccentId);
            }
            if (color == 0) {
                color = Color.parseColor("#3D348B");
            }
            float brightness = AndroidUtilities.computePerceivedBrightness(color);
            if (brightness >= 0.721f || brightness <= 0.279f) {
                color = Color.parseColor("#3D348B");
            }
            return color;
        } else {

            return Color.parseColor("#3D348B");
        }
    }
}
