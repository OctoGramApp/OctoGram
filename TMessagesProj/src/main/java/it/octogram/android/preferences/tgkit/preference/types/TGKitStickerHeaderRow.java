package it.octogram.android.preferences.tgkit.preference.types;

import androidx.annotation.Nullable;

import it.octogram.android.preferences.tgkit.preference.TGKitPreference;

public class TGKitStickerHeaderRow extends TGKitPreference {

    private final Object stickerView;
    private final String description;

    public TGKitStickerHeaderRow(Object stickerView, @Nullable String description) {
        this.stickerView = stickerView;
        this.description = description;
    }

    public Object getStickerView() {
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
