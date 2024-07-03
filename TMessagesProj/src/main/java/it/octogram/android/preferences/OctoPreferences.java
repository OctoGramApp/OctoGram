package it.octogram.android.preferences;

import android.content.Context;

import com.google.android.exoplayer2.util.Consumer;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.Components.RLottieImageView;
import org.telegram.ui.Components.StickerImageView;

import java.util.ArrayList;
import java.util.List;

import it.octogram.android.ConfigProperty;
import it.octogram.android.StickerUi;
import it.octogram.android.preferences.rows.BaseRow;
import it.octogram.android.preferences.rows.impl.EmptyCellRow;
import it.octogram.android.preferences.rows.impl.HeaderRow;
import it.octogram.android.preferences.rows.impl.ShadowRow;
import it.octogram.android.preferences.rows.impl.StickerHeaderRow;

public record OctoPreferences(String title, List<BaseRow> preferences) {

    public static OctoPreferencesBuilder builder(String name) {
        return new OctoPreferencesBuilder(name);
    }

    public static class OctoPreferencesBuilder {
        private final String name;
        private final List<BaseRow> preferences = new ArrayList<>();

        public OctoPreferencesBuilder(String name) {
            this.name = name;
        }

        public OctoPreferencesBuilder category(String title, Consumer<OctoPreferencesBuilder> consumer) {
            preferences.add(new HeaderRow(title));
            consumer.accept(this);
            preferences.add(new ShadowRow());
            return this;
        }

        public OctoPreferencesBuilder category(String title, ConfigProperty<Boolean> showIfHeader, ConfigProperty<Boolean> showIfShadow, Consumer<OctoPreferencesBuilder> consumer) {
            preferences.add(new HeaderRow(title, showIfHeader));
            consumer.accept(this);
            preferences.add(new ShadowRow(showIfShadow));
            return this;
        }

        public OctoPreferencesBuilder category(String title, ConfigProperty<Boolean> showIf, Consumer<OctoPreferencesBuilder> consumer) {
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
            preferences.add(new EmptyCellRow());
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
            rlottieImageView.setAnimation(packName, AndroidUtilities.dp(130), AndroidUtilities.dp(130));
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
            return new OctoPreferences(name, preferences);
        }
    }
}
