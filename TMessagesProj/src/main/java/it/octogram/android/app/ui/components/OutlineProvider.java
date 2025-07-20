/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui.components;

import android.graphics.Outline;
import android.view.View;
import android.view.ViewOutlineProvider;

import org.telegram.messenger.AndroidUtilities;

import it.octogram.android.OctoConfig;

public class OutlineProvider extends ViewOutlineProvider {

    private final int width;
    private final int height;
    private final boolean isSquare;

    /**
     * Constructor for the OutlineProvider class.
     *
     * @param width    The width of the outline.
     * @param height   The height of the outline.
     * @param isSquare A boolean indicating whether the outline should be a square.
     *                 If true, the width and height will be equal.
     */
    public OutlineProvider(int width, int height, boolean isSquare) {
        this.width = width;
        this.height = height;
        this.isSquare = isSquare;
    }

    /**
     * Constructor for the OutlineProvider class.
     *
     * @param width  The width of the outline.
     * @param height The height of the outline.
     */
    public OutlineProvider(int width, int height) {
        this.width = width;
        this.height = height;
        this.isSquare = OctoConfig.INSTANCE.useSquaredFab.getValue();
    }

    /**
     * Constructor for the OutlineProvider.
     *
     * @param isSquare True if the outline should be a square, false otherwise.
     *                 If true, the width and height will be equal and set to 56dp.
     *                 If false, the width and height will be determined by the content.
     */
    public OutlineProvider(boolean isSquare) {
        var dp = AndroidUtilities.dp(56);
        this.width = dp;
        this.height = dp;
        this.isSquare = isSquare;
    }

    /**
     * Constructor for the OutlineProvider.
     * Initializes the width and height of the outline to 56dp.
     * Sets the isSquare flag based on the OctoConfig's squareFab setting.
     */
    public OutlineProvider() {
        var dp = AndroidUtilities.dp(56);
        this.width = dp;
        this.height = dp;
        this.isSquare = OctoConfig.INSTANCE.useSquaredFab.getValue();
    }

    /**
     * This method is called to define the outline of the view.
     * It determines whether to draw a rounded rectangle or an oval shape based on the 'isSquare' flag.
     *
     * @param view    The view whose outline is being defined.
     * @param outline The outline object to be modified and used to define the view's outline.
     */
    @Override
    public void getOutline(View view, Outline outline) {
        if (isSquare) {
            outline.setRoundRect(0, 0, width, height, AndroidUtilities.dp(16));
        } else {
            outline.setOval(0, 0, width, height);
        }
    }
}

