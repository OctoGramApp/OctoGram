package it.octogram.android.preferences.rows.impl;

import android.content.Context;

import androidx.annotation.Nullable;

import org.telegram.messenger.UserConfig;
import org.telegram.ui.Components.StickerImageView;

import it.octogram.android.preferences.PreferenceType;
import it.octogram.android.preferences.rows.BaseRow;
import it.octogram.android.preferences.rows.BaseRowBuilder;

public class StickerHeaderRow extends BaseRow {

    private final Object stickerView;

    private StickerHeaderRow(Object stickerView, @Nullable String description) {
        super(null, description, false, null, false, PreferenceType.STICKER_HEADER);
        this.stickerView = stickerView;
    }

    public Object getStickerView() {
        return stickerView;
    }


    public static class StickerHeaderRowBuilder extends BaseRowBuilder<StickerHeaderRow> {

        private Object stickerView;

        public StickerHeaderRowBuilder stickerView(Object stickerView) {
            this.stickerView = stickerView;
            return this;
        }

        @Override
        public StickerHeaderRow build() {
            return new StickerHeaderRow(stickerView, description);
        }
    }

}
