/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.deeplink;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@StringDef({
        DeepLinkDef.FRANCESCO,
        DeepLinkDef.FOX,
        DeepLinkDef.CHUPAGRAM,
        DeepLinkDef.YUKIGRAM,
        DeepLinkDef.EXPERIMENTAL,
        DeepLinkDef.EXPERIMENTAL_NAVIGATION,
        DeepLinkDef.CAMERA,
        DeepLinkDef.GENERAL,
        DeepLinkDef.OCTOSETTINGS,
        DeepLinkDef.APPEARANCE,
        DeepLinkDef.UPDATE,
        DeepLinkDef.USER,
        DeepLinkDef.APPEARANCE_APP,
        DeepLinkDef.APPEARANCE_CHAT,
        DeepLinkDef.APPEARANCE_DRAWER,
        DeepLinkDef.PINNED_EMOJIS,
        DeepLinkDef.PINNED_REACTIONS,
        DeepLinkDef.XIMI,
        DeepLinkDef.TRANSLATOR,
        DeepLinkDef.INFO,
        DeepLinkDef.DC_STATUS,
        DeepLinkDef.PRIVACY,
        DeepLinkDef.LOCKED_CHATS
})
@Retention(RetentionPolicy.SOURCE)
public @interface DeepLinkType {
}

