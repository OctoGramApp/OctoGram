/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;

import androidx.core.util.Consumer;

import org.telegram.messenger.UserConfig;
import org.telegram.ui.Components.RLottieImageView;
import org.telegram.ui.Components.StickerImageView;

import java.util.ArrayList;
import java.util.List;

import it.octogram.android.ConfigProperty;
import it.octogram.android.StickerUi;
import it.octogram.android.deeplink.DeepLinkType;
import it.octogram.android.preferences.rows.BaseRow;
import it.octogram.android.preferences.rows.impl.EmptyCellRow;
import it.octogram.android.preferences.rows.impl.HeaderRow;
import it.octogram.android.preferences.rows.impl.ShadowRow;
import it.octogram.android.preferences.rows.impl.StickerHeaderRow;

public record OctoPreferences(CharSequence title, String deepLink, List<BaseRow> preferences, List<OctoContextMenuElement> elements) {

    public static OctoPreferencesBuilder builder(String name) {
        return new OctoPreferencesBuilder(name);
    }

    public static class OctoContextMenuElement {
        public int icon;
        public String title;
        public Runnable run;
        public boolean danger = false;

        public OctoContextMenuElement(int icon, String title, Runnable run) {
            this.icon = icon;
            this.title = title;
            this.run = run;
        }

        public OctoContextMenuElement asDanger() {
            danger = true;
            return this;
        }
    }

    public static class OctoPreferencesBuilder {
        private final String name;
        private @DeepLinkType String deepLink = null;
        private final List<OctoContextMenuElement> elements = new ArrayList<>();
        private final List<BaseRow> preferences = new ArrayList<>();

        public OctoPreferencesBuilder(String name) {
            this.name = name;
        }

        public OctoPreferencesBuilder deepLink(@DeepLinkType String deepLink) {
            this.deepLink = deepLink;
            return this;
        }

        public OctoPreferencesBuilder addContextMenuItem(OctoContextMenuElement element) {
            elements.add(element);
            return this;
        }

        public OctoPreferencesBuilder category(CharSequence title, Consumer<OctoPreferencesBuilder> consumer) {
            preferences.add(new HeaderRow(title));
            consumer.accept(this);
            preferences.add(new ShadowRow());
            return this;
        }

        public OctoPreferencesBuilder category(int title, Consumer<OctoPreferencesBuilder> consumer) {
            return category(getString(title), consumer);
        }

        public OctoPreferencesBuilder category(CharSequence title, ConfigProperty<Boolean> showIfHeader, ConfigProperty<Boolean> showIfShadow, Consumer<OctoPreferencesBuilder> consumer) {
            preferences.add(new HeaderRow(title, showIfHeader));
            consumer.accept(this);
            preferences.add(new ShadowRow(showIfShadow));
            return this;
        }

        public OctoPreferencesBuilder category(CharSequence title, ConfigProperty<Boolean> showIf, Consumer<OctoPreferencesBuilder> consumer) {
            preferences.add(new HeaderRow(title, showIf));
            consumer.accept(this);
            preferences.add(new ShadowRow(showIf));
            return this;
        }

        public OctoPreferencesBuilder row(BaseRow row) {
            preferences.add(row);
            return this;
        }

        public OctoPreferencesBuilder sticker(Context context, String packName, StickerUi stickerNum, boolean autoRepeat, String description) {
            StickerImageView stickerImageView = createStickerImageView(context, packName, stickerNum.getValue(), autoRepeat);
            preferences.add(new StickerHeaderRow.StickerHeaderRowBuilder()
                    .stickerView(stickerImageView)
                    .description(description)
                    .build()
            );
            preferences.add(new ShadowRow());
            return this;
        }

        public OctoPreferencesBuilder octoAnimation(String description) {
            preferences.add(new StickerHeaderRow.StickerHeaderRowBuilder()
                    .useOctoAnimation(true)
                    .description(description)
                    .build()
            );
            preferences.add(new ShadowRow());
            return this;
        }

        public OctoPreferencesBuilder sticker(Context context, int packName, boolean autoRepeat, String description) {
            RLottieImageView rLottieImageView = createRLottieImageView(context, packName, autoRepeat);
            preferences.add(new StickerHeaderRow.StickerHeaderRowBuilder()
                    .stickerView(rLottieImageView)
                    .description(description)
                    .build());
            preferences.add(new EmptyCellRow());
            preferences.add(new ShadowRow());
            return this;
        }

        private RLottieImageView createRLottieImageView(Context context, int packName, boolean autoRepeat) {
            RLottieImageView rlottieImageView = new RLottieImageView(context);
            rlottieImageView.setAutoRepeat(autoRepeat);
            rlottieImageView.setAnimation(packName, dp(130), dp(130));
            rlottieImageView.playAnimation();
            return rlottieImageView;
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
            return new OctoPreferences(name, deepLink, preferences, elements);
        }
    }
}