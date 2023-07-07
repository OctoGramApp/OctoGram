package it.octogram.android.preferences.tgkit.preference.types;

import androidx.annotation.Nullable;

import org.telegram.ui.Components.StickerImageView;

import it.octogram.android.preferences.tgkit.preference.TGKitPreference;

public class TGKitStickerHeaderRow extends TGKitPreference {

    private final StickerImageView stickerView;
    private final String description;

    public TGKitStickerHeaderRow(StickerImageView stickerView, @Nullable String description) {
        this.stickerView = stickerView;
        this.description = description;
    }

    public StickerImageView getStickerView() {
        return stickerView;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public TGPType getType() {
        return TGPType.STICKER_HEADER;
    }
}
