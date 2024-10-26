package it.octogram.android.deeplink;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@StringDef({DeepLinkDef.FRANCESCO, DeepLinkDef.FOX, DeepLinkDef.CHUPAGRAM, DeepLinkDef.YUKIGRAM, DeepLinkDef.EXPERIMENTAL, DeepLinkDef.CAMERA, DeepLinkDef.GENERAL, DeepLinkDef.OCTOSETTINGS, DeepLinkDef.APPEARANCE, DeepLinkDef.UPDATE, DeepLinkDef.USER})
@Retention(RetentionPolicy.SOURCE)
public @interface DeepLinkType {
}

