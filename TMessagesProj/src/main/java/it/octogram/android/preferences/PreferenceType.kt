/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences

import it.octogram.android.preferences.PreferenceType.entries


enum class PreferenceType(val adapterType: Int, val isEnabled: Boolean) {
    CUSTOM(-1, false),
    SHADOW(0, false),
    EMPTY_CELL(2, false),
    HEADER(2, false),
    SWITCH(3, true),
    TEXT_DETAIL(4, true),
    TEXT_ICON(5, true),
    SLIDER(6, false),
    LIST(7, true),
    SLIDER_CHOOSE(8, true),
    FOOTER_INFORMATIVE(13, false),
    FOOTER(14, false),
    STICKER_HEADER(15, false),
    CHECKBOX(16, true),
    EXPANDABLE_ROWS(17, true),
    EXPANDABLE_ROWS_CHILD(18, true);

    companion object {
        fun fromAdapterType(adapterType: Int): PreferenceType? {
            return entries.find { it.adapterType == adapterType }
        }
    }
}