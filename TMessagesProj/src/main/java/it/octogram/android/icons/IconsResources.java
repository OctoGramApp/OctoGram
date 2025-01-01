/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.icons;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import it.octogram.android.IconsUIType;
import it.octogram.android.OctoConfig;
import it.octogram.android.preferences.ui.custom.IconsSelector;

@SuppressLint("UseCompatLoadingForDrawables")
public class IconsResources extends Resources {
    private int _iconsType = -1;
    private boolean _useMemeMode = false;

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

    public Drawable getForcedDrawable(int id, int forcedIconsType, boolean forcedMemeModeStatus) throws NotFoundException {
        return super.getDrawable(getConversion(id, forcedIconsType, forcedMemeModeStatus, true), null);
    }

    private int getConversion(int icon) {
        return getConversion(icon, -1, false, false);
    }

    private int getConversion(int icon, int forcedIconsType, boolean forcedMemeModeStatus, boolean hasForcedMemeModeStatus) {
        if (_iconsType == -1) {
            _iconsType = OctoConfig.INSTANCE.uiIconsType.getValue();
            _useMemeMode = OctoConfig.INSTANCE.uiRandomMemeIcons.getValue();
        }

        int consideredIconsType = forcedIconsType == -1 ? _iconsType : forcedIconsType;
        boolean consideredMemeMode = hasForcedMemeModeStatus ? forcedMemeModeStatus : _useMemeMode;

        if (consideredIconsType == IconsUIType.SOLAR.getValue()) {
            if (consideredMemeMode && IconsSelector.canUseMemeMode()) {
                return SolarIcons.Companion.getRandom(icon);
            }

            return SolarIcons.Companion.getConversion(icon);
        } else if (consideredIconsType == IconsUIType.MATERIAL_DESIGN_3.getValue()) {
            if (consideredMemeMode && IconsSelector.canUseMemeMode()) {
                return MaterialDesign3Icons.Companion.getRandom(icon);
            }

            return MaterialDesign3Icons.Companion.getConversion(icon);
        }

        return icon;
    }
}
