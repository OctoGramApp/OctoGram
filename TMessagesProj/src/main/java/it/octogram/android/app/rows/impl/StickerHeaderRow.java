/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.rows.impl;

import androidx.annotation.Nullable;

import it.octogram.android.ConfigProperty;
import it.octogram.android.app.PreferenceType;
import it.octogram.android.app.rows.BaseRow;
import it.octogram.android.app.rows.BaseRowBuilder;

public class StickerHeaderRow extends BaseRow {

    private final Object stickerView;
    private final boolean useOctoAnimation;

    private StickerHeaderRow(Object stickerView, @Nullable String description, boolean useOctoAnimation, ConfigProperty<Boolean> showIf) {
        super(null, description, false, showIf, false, PreferenceType.STICKER_HEADER);
        this.stickerView = stickerView;
        this.useOctoAnimation = useOctoAnimation;
    }

    public Object getStickerView() {
        return stickerView;
    }

    public boolean getUseOctoAnimation() {
        return useOctoAnimation;
    }

    public static class StickerHeaderRowBuilder extends BaseRowBuilder<StickerHeaderRow> {

        private Object stickerView;
        private boolean useOctoAnimation;

        public StickerHeaderRowBuilder stickerView(Object stickerView) {
            this.stickerView = stickerView;
            return this;
        }

        public StickerHeaderRowBuilder useOctoAnimation(boolean useOctoAnimation) {
            this.useOctoAnimation = useOctoAnimation;
            return this;
        }

        @Override
        public StickerHeaderRow build() {
            return new StickerHeaderRow(stickerView, description, useOctoAnimation, showIf);
        }
    }

}
