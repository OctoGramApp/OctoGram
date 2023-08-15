package it.octogram.android.preferences;

import androidx.annotation.Nullable;

public enum PreferenceType {

    CUSTOM(-1, true),
    SHADOW(0, false),
    EMPTY_CELL(2, false),
    HEADER(2, false),
    SWITCH(3, true),
    TEXT_DETAIL(4, true),
    TEXT_ICON(5, true),
    SLIDER(6, true),
    LIST(7, true),
    SLIDER_CHOOSE(8, true),
    FOOTER(14, false),
    STICKER_HEADER(15, false),

    ;


    private final int adapterType;
    private final boolean enabled;

    PreferenceType(int adapterType, boolean enabled) {
        this.adapterType = adapterType;
        this.enabled = enabled;
    }

    public int getAdapterType() {
        return adapterType;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Nullable
    public static PreferenceType fromAdapterType(int adapterType) {
        for (PreferenceType type : values()) {
            if (type.getAdapterType() == adapterType) {
                return type;
            }
        }
        return null;
    }

}
