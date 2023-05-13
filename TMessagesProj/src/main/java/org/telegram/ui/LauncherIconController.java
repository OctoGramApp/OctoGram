package org.telegram.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.R;

public class LauncherIconController {
    public static void tryFixLauncherIconIfNeeded() {
        for (LauncherIcon icon : LauncherIcon.values()) {
            if (isEnabled(icon)) {
                return;
            }
        }

        setIcon(LauncherIcon.DEFAULT);
    }

    public static boolean isEnabled(LauncherIcon icon) {
        Context ctx = ApplicationLoader.applicationContext;
        int i = ctx.getPackageManager().getComponentEnabledSetting(icon.getComponentName(ctx));
        return i == PackageManager.COMPONENT_ENABLED_STATE_ENABLED || i == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT && icon == LauncherIcon.DEFAULT;
    }

    public static void setIcon(LauncherIcon icon) {
        Context ctx = ApplicationLoader.applicationContext;
        PackageManager pm = ctx.getPackageManager();
        for (LauncherIcon i : LauncherIcon.values()) {
            pm.setComponentEnabledSetting(i.getComponentName(ctx), i == icon ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }

    public enum LauncherIcon {
        DEFAULT("DefaultIcon", R.drawable.ic_launcher_background, R.drawable.icon_launcher_foreground, R.string.AppIconDefault),
        DEVELOPER("DeveloperIcon", R.drawable.iconc_1_background_sa, R.drawable.icon_launcher_foreground, R.string.AppIconDeveloper),
        AQUA("AquaIcon", R.drawable.icon_4_background_sa, R.drawable.icon_launcher_foreground, R.string.AppIconAqua),
        MONO_BLACK("MonoBlackIcon", R.drawable.iconc_2_background_sa, R.drawable.icon_launcher_foreground, R.string.AppIconMonoBlack),
        ARCTIC("ArcticIcon", R.drawable.iconc_3_background_sa, R.drawable.iconc_3_launcher_foreground, R.string.AppIconArctic),
        MONET("MonetIcon", -1, -1, R.string.MonetIcon, false, true),
        CHUPA("ChupaIcon", R.drawable.iconc_3_background_sa, R.mipmap.icon_9_launcher_foreground, R.string.AppIconChupa, true),
        PREMIUM("PremiumIcon", R.drawable.icon_3_background_sa, R.mipmap.icon_3_foreground, R.string.AppIconPremium, true),
        TURBO("TurboIcon", R.drawable.icon_5_background_sa, R.drawable.icon_launcher_foreground, R.string.AppIconTurbo, true),
        NOX("NoxIcon", R.drawable.icon_2_background_sa, R.drawable.icon_launcher_foreground, R.string.AppIconNox, true);

        /*
        Disabled Icon
         - VINTAGE("VintageIcon", R.drawable.icon_6_background_sa, -1, R.string.AppIconVintage),
         - FOXGRAM("FoxGramIcon", R.drawable.icon_7_launcher_background, R.drawable.icon_launcher_foreground, R.string.AppIconFoxgram, false, true),
         - RAINBOW("RainbowIcon", R.drawable.icon_8_launcher_background, R.drawable.icon_launcher_foreground, R.string.AppIconRainbow),
        */

        public final String key;
        public final int background;
        public final int foreground;
        public final int title;
        public final boolean premium;
        public final boolean hidden;

        private ComponentName componentName;

        public ComponentName getComponentName(Context ctx) {
            if (componentName == null) {
                componentName = new ComponentName(ctx.getPackageName(), "org.telegram.messenger." + key);
            }
            return componentName;
        }

        LauncherIcon(String key, int background, int foreground, int title) {
            this(key, background, foreground, title, false);
        }

        LauncherIcon(String key, int background, int foreground, int title, boolean premium) {
            this(key, background, foreground, title, premium, false);
        }

        LauncherIcon(String key, int background, int foreground, int title, boolean premium, boolean hidden) {
            this.key = key;
            this.background = background;
            this.foreground = foreground;
            this.title = title;
            this.premium = premium;
            this.hidden = hidden;
        }
    }
}
