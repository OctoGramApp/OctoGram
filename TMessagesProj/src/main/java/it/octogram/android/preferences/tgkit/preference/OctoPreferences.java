/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */
package it.octogram.android.preferences.tgkit.preference;

import android.content.Context;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.Components.RLottieImageView;
import org.telegram.ui.Components.StickerImageView;

import java.util.ArrayList;
import java.util.List;

import it.octogram.android.preferences.tgkit.preference.types.TGKitHeaderRow;
import it.octogram.android.preferences.tgkit.preference.types.TGKitSectionRow;
import it.octogram.android.preferences.tgkit.preference.types.TGKitSettingsCellRow;
import it.octogram.android.preferences.tgkit.preference.types.TGKitStickerHeaderRow;

public class OctoPreferences {
    private final String name;
    private final List<TGKitPreference> preferences;

    private OctoPreferences(String name, List<TGKitPreference> preferences) {
        this.name = name;
        this.preferences = preferences;
    }

    public static OctoPreferencesBuilder builder(String name) {
        return new OctoPreferencesBuilder(name);
    }

    public String getName() {
        return name;
    }

    public List<TGKitPreference> getPreferences() {
        return preferences;
    }

    public static class OctoPreferencesBuilder {
        private final String name;
        private final List<TGKitPreference> preferenceList;

        public OctoPreferencesBuilder(String name) {
            this.name = name;
            this.preferenceList = new ArrayList<>();
        }

        public OctoPreferencesBuilder add(TGKitPreference preference) {
            preferenceList.add(preference);
            return this;
        }

        public OctoPreferencesBuilder category(String name, List<TGKitPreference> preferences) {
            preferenceList.add(new TGKitHeaderRow(name));
            preferenceList.addAll(preferences);
            preferenceList.add(new TGKitSectionRow());
            return this;
        }

        public OctoPreferencesBuilder sticker(Context context, int packName, boolean autoRepeat, String description) {
            RLottieImageView rLottieImageView = createRLottieImageView(context, packName, autoRepeat);
            preferenceList.add(new TGKitStickerHeaderRow(rLottieImageView, description));
            preferenceList.add(new TGKitSettingsCellRow());
            preferenceList.add(new TGKitSectionRow());
            return this;
        }

        private RLottieImageView createRLottieImageView(Context context, int packName, boolean autoRepeat) {
            RLottieImageView rlottieImageView = new RLottieImageView(context);
            rlottieImageView.setAutoRepeat(autoRepeat);
            rlottieImageView.setAnimation(packName, AndroidUtilities.dp(130),AndroidUtilities.dp(130));
            rlottieImageView.playAnimation();
            return rlottieImageView;
        }

        public OctoPreferencesBuilder sticker(Context context, String packName, int stickerNum, boolean autoRepeat, String description) {
            StickerImageView stickerImageView = createStickerImageView(context, packName, stickerNum, autoRepeat);
            preferenceList.add(new TGKitStickerHeaderRow(stickerImageView, description));
            preferenceList.add(new TGKitSettingsCellRow());
            preferenceList.add(new TGKitSectionRow());
            return this;
        }

        private StickerImageView createStickerImageView(Context context, String packName, int stickerNum, boolean autoRepeat) {
            StickerImageView stickerImageView = new StickerImageView(context, UserConfig.selectedAccount);
            stickerImageView.setStickerPackName(packName);
            stickerImageView.setStickerNum(stickerNum);
            if (autoRepeat) {
                stickerImageView.getImageReceiver().setAutoRepeat(1);
            }
            return stickerImageView;
        }

        public OctoPreferences build() {
            return new OctoPreferences(name, preferenceList);
        }
    }
}
