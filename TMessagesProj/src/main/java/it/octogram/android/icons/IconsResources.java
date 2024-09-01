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

    private int getConversion(int icon) {
        int iconsType = OctoConfig.INSTANCE.uiIconsType.getValue();

        if (iconsType == IconsUIType.SOLAR.getValue()) {
            if (OctoConfig.INSTANCE.uiRandomMemeIcons.getValue()) {
                if (IconsSelector.canUseMemeMode()) {
                    return SolarIcons.Companion.getRandom(icon);
                } else {
                    OctoConfig.INSTANCE.uiRandomMemeIcons.updateValue(false);
                }
            }
            return SolarIcons.Companion.getConversion(icon);
        } else if (iconsType == IconsUIType.MATERIAL_DESIGN_3.getValue()) {
            if (OctoConfig.INSTANCE.uiRandomMemeIcons.getValue()) {
                if (IconsSelector.canUseMemeMode()) {
                    return MaterialDesign3Icons.Companion.getRandom(icon);
                } else {
                    OctoConfig.INSTANCE.uiRandomMemeIcons.updateValue(false);
                }
            }
            return MaterialDesign3Icons.Companion.getConversion(icon);
        }
        return icon;
    }
}
