/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
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

    private static final int DEFAULT_FAB_SIZE_DP = 56;
    private static final int SMALL_FAB_SIZE_DP = 40;
    private static final int CIRCULAR_RADIUS_DP = 100;

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

    public static Drawable createFabBackground() {
        int defaultBackgroundColor = Theme.getColor(Theme.key_chats_actionBackground);
        int pressedBackgroundColor = Theme.getColor(Theme.key_chats_actionPressedBackground);

        return createFabBackground(DEFAULT_FAB_SIZE_DP, defaultBackgroundColor, pressedBackgroundColor);
    }

    public static Drawable createFabBackground(int sizeDp, int defaultBackgroundColor, int pressedBackgroundColor) {
        int fabCornerRadiusDp;
        if (!OctoConfig.INSTANCE.useSquaredFab.getValue()) {
            fabCornerRadiusDp = CIRCULAR_RADIUS_DP;
        } else {
            fabCornerRadiusDp = (int) Math.ceil((sizeDp * 16) / 56.0f);
        }

        if (sizeDp == SMALL_FAB_SIZE_DP) {
            int defaultFabColor = Theme.key_windowBackgroundWhite;
            defaultBackgroundColor = ColorUtils.blendARGB(Theme.getColor(defaultFabColor), Color.WHITE, 0.1f);
            pressedBackgroundColor = Theme.blendOver(Theme.getColor(defaultFabColor), Theme.getColor(Theme.key_listSelector));
        }

        return Theme.createSimpleSelectorRoundRectDrawable(
                dp(fabCornerRadiusDp),
                defaultBackgroundColor,
                pressedBackgroundColor
        );
    }
}