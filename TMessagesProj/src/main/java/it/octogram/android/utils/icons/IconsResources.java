/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.icons;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import org.telegram.messenger.R;

import it.octogram.android.IconsUIType;
import it.octogram.android.OctoConfig;

@SuppressLint("UseCompatLoadingForDrawables")
public class IconsResources extends Resources {
    private int _iconsType = -1;

    public IconsResources(Resources resources) {
        super(resources.getAssets(), resources.getDisplayMetrics(), resources.getConfiguration());
    }

    @Override
    public Drawable getDrawable(int id) throws NotFoundException {
        return super.getDrawable(getConversion(id), null);
    }

    @Override
    public Drawable getDrawable(int id, @Nullable Theme theme) throws NotFoundException {
        return super.getDrawable(getConversion(id), theme);
    }

    @Nullable
    @Override
    public Drawable getDrawableForDensity(int id, int density, @Nullable Theme theme) {
        return super.getDrawableForDensity(getConversion(id), density, theme);
    }

    @Nullable
    @Override
    public Drawable getDrawableForDensity(int id, int density) throws NotFoundException {
        return super.getDrawableForDensity(getConversion(id), density, null);
    }

    public Drawable getForcedDrawable(int id, int forcedIconsType) throws NotFoundException {
        return super.getDrawable(getConversion(id, forcedIconsType), null);
    }

    private int getConversion(int icon) {
        return getConversion(icon, -1);
    }

    private int getConversion(int icon, int forcedIconsType) {
        if (icon == R.drawable.popup_fixed_alert || icon == R.drawable.popup_fixed_alert2) {
            return R.drawable.popup_fixed_alert3;
        }

        if (_iconsType == -1) {
            _iconsType = OctoConfig.INSTANCE.uiIconsType.getValue();
        }

        int consideredIconsType = forcedIconsType == -1 ? _iconsType : forcedIconsType;

        if (consideredIconsType == IconsUIType.SOLAR.getValue()) {
            return SolarIcons.Companion.getConversion(icon);
        } else if (consideredIconsType == IconsUIType.MATERIAL_DESIGN_3.getValue()) {
            return MaterialDesign3Icons.Companion.getConversion(icon);
        }

        return icon;
    }

}
