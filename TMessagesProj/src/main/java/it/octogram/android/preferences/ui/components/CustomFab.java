/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

package it.octogram.android.preferences.ui.components;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.CombinedDrawable;

import it.octogram.android.OctoConfig;

public abstract class CustomFab {

    /**
     * Creates a circular drawable with an optional icon overlay.
     *
     * @param context   The context for accessing resources.
     * @param iconResId The resource ID of the icon.
     * @param size      The diameter of the circular drawable in pixels.
     * @return A CombinedDrawable with the icon and circular background.
     */
    public static CombinedDrawable createCircleDrawableWithIcon(Context context, int iconResId, int size) {
        Drawable iconDrawable = iconResId != 0 ? ContextCompat.getDrawable(context, iconResId) : null;

        var backgroundShape = new OvalShape();
        backgroundShape.resize(size, size);
        ShapeDrawable backgroundDrawable = new ShapeDrawable(backgroundShape);
        backgroundDrawable.getPaint().setColor(Color.WHITE);

        var iconWithBackground = new CombinedDrawable(backgroundDrawable, iconDrawable);
        if (iconDrawable != null) {
            iconDrawable.mutate();
        }
        iconWithBackground.setCustomSize(size, size);

        return iconWithBackground;
    }

    /**
     * Creates the default Floating Action Button (FAB) background.
     *
     * @return A Drawable for the FAB.
     */
    public static Drawable createFabBackground() {
        int fabDefaultSize = 56;
        int defaultBackgroundColor = Theme.getColor(Theme.key_chats_actionBackground);
        int pressedBackgroundColor = Theme.getColor(Theme.key_chats_actionPressedBackground);

        return createFabBackground(fabDefaultSize, defaultBackgroundColor, pressedBackgroundColor);
    }

    /**
     * Creates a customizable Floating Action Button (FAB) background.
     *
     * @param size            The size of the FAB in dp.
     * @param defaultBackgroundColor The background color.
     * @param pressedBackgroundColor    The pressed background color.
     * @return A Drawable for the FAB.
     */
    public static Drawable createFabBackground(int size, int defaultBackgroundColor, int pressedBackgroundColor) {
        int fabCornerRadius = !OctoConfig.INSTANCE.useSquaredFab.getValue() ? 100 : (int) Math.ceil((size * 16) / 56.0f);

        if (size == 40) {
            int defaultFabColor = Theme.key_windowBackgroundWhite;
            defaultBackgroundColor = ColorUtils.blendARGB(Theme.getColor(defaultFabColor), Color.WHITE, 0.1f);
            pressedBackgroundColor = Theme.blendOver(Theme.getColor(defaultFabColor), Theme.getColor(Theme.key_listSelector));
        }

        return Theme.createSimpleSelectorRoundRectDrawable(
                dp(fabCornerRadius),
                defaultBackgroundColor,
                pressedBackgroundColor
        );
    }
}
