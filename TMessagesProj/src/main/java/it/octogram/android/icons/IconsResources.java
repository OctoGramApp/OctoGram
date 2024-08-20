package it.octogram.android.icons;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import it.octogram.android.OctoConfig;
import it.octogram.android.preferences.ui.custom.IconsSelector;

@SuppressLint("UseCompatLoadingForDrawables")
public class IconsResources extends Resources {
    private SolarIcons _solarIcons;

    public IconsResources(Resources resources) {
        super(resources.getAssets(), resources.getDisplayMetrics(), resources.getConfiguration());
    }

    @Override
    public Drawable getDrawable(int id) throws NotFoundException {
        return super.getDrawable(getConversion(id));
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
        return super.getDrawableForDensity(getConversion(id), density);
    }

    public Drawable getSolarDrawable(int id) throws NotFoundException {
        return super.getDrawable(getConversion(id));
    }

    public Drawable getDefaultDrawable(int id) throws NotFoundException {
        return super.getDrawable(id);
    }

    private int getConversion(int icon) {
        if (OctoConfig.INSTANCE.uiSolarIcons.getValue()) {
            if (_solarIcons == null) {
                _solarIcons = new SolarIcons();
            }

            if (OctoConfig.INSTANCE.uiRandomMemeIcons.getValue() && IconsSelector.canUseMemeMode()) {
                return _solarIcons.getRandom(icon);
            }

            return _solarIcons.getConversion(icon);
        }
        return icon;
    }
}
