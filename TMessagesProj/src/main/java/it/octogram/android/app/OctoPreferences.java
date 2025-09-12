/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.util.Consumer;

import org.telegram.messenger.UserConfig;
import org.telegram.ui.Components.RLottieImageView;
import org.telegram.ui.Components.StickerImageView;

import java.util.ArrayList;
import java.util.List;

import it.octogram.android.ConfigProperty;
import it.octogram.android.StickerUi;
import it.octogram.android.app.rows.BaseRow;
import it.octogram.android.app.rows.impl.EmptyCellRow;
import it.octogram.android.app.rows.impl.HeaderRow;
import it.octogram.android.app.rows.impl.ShadowRow;
import it.octogram.android.app.rows.impl.StickerHeaderRow;
import it.octogram.android.utils.deeplink.DeepLinkType;

public record OctoPreferences(
        @NonNull CharSequence title,
        @DeepLinkType String deepLink,
        boolean hasSaveButton,
        @NonNull List<BaseRow> preferences,
        @NonNull List<OctoContextMenuElement> elements
) {
    public static OctoPreferencesBuilder builder(@NonNull CharSequence name) {
        return new OctoPreferencesBuilder(name);
    }

    public static final class OctoContextMenuElement {
        private final @DrawableRes int icon;
        private final @NonNull CharSequence title;
        private final @NonNull Runnable run;
        private final boolean danger;

        public OctoContextMenuElement(@DrawableRes int icon, @NonNull CharSequence title, @NonNull Runnable run) {
            this(icon, title, run, false);
        }

        private OctoContextMenuElement(@DrawableRes int icon, @NonNull CharSequence title, @NonNull Runnable run, boolean danger) {
            this.icon = icon;
            this.title = title;
            this.run = run;
            this.danger = danger;
        }

        public OctoContextMenuElement asDanger() {
            return new OctoContextMenuElement(icon, title, run, true);
        }

        public @DrawableRes int getIcon() {
            return icon;
        }

        public @NonNull CharSequence getTitle() {
            return title;
        }

        public @NonNull Runnable getRunnable() {
            return run;
        }

        public boolean isDanger() {
            return danger;
        }
    }

    public static final class OctoPreferencesBuilder {
        private final @NonNull CharSequence name;
        private @DeepLinkType String deepLink = null;
        private boolean saveButtonAvailable = false;
        private final List<OctoContextMenuElement> elements = new ArrayList<>();
        private final List<BaseRow> preferences = new ArrayList<>();

        public OctoPreferencesBuilder(@NonNull CharSequence name) {
            this.name = name;
        }

        public OctoPreferencesBuilder deepLink(@DeepLinkType String deepLink) {
            this.deepLink = deepLink;
            return this;
        }

        public OctoPreferencesBuilder saveButtonAvailable(boolean saveButtonAvailable) {
            this.saveButtonAvailable = saveButtonAvailable;
            return this;
        }

        public OctoPreferencesBuilder addContextMenuItem(@NonNull OctoContextMenuElement element) {
            elements.add(element);
            return this;
        }

        private OctoPreferencesBuilder addCategoryInternal(@NonNull HeaderRow header, ShadowRow shadow, @NonNull Consumer<OctoPreferencesBuilder> consumer
        ) {
            preferences.add(header);
            consumer.accept(this);
            if (shadow != null) {
                preferences.add(shadow);
            }
            return this;
        }

        public OctoPreferencesBuilder category(@NonNull CharSequence title, @NonNull Consumer<OctoPreferencesBuilder> consumer) {
            return addCategoryInternal(new HeaderRow(title), new ShadowRow(), consumer);
        }

        public OctoPreferencesBuilder category(@StringRes int titleRes, @NonNull Consumer<OctoPreferencesBuilder> consumer) {
            return category(getString(titleRes), consumer);
        }

        public OctoPreferencesBuilder category(@NonNull CharSequence title, @NonNull ConfigProperty<Boolean> showIfHeader, @NonNull ConfigProperty<Boolean> showIfShadow, @NonNull Consumer<OctoPreferencesBuilder> consumer) {
            return addCategoryInternal(new HeaderRow(title, showIfHeader), new ShadowRow(showIfShadow), consumer);
        }

        public OctoPreferencesBuilder category(@NonNull CharSequence title, @NonNull ConfigProperty<Boolean> showIf, @NonNull Consumer<OctoPreferencesBuilder> consumer) {
            return addCategoryInternal(new HeaderRow(title, showIf), new ShadowRow(showIf), consumer);
        }

        public OctoPreferencesBuilder categoryWithoutShadow(@NonNull CharSequence title, @NonNull Consumer<OctoPreferencesBuilder> consumer) {
            return addCategoryInternal(new HeaderRow(title), null, consumer);
        }

        public OctoPreferencesBuilder categoryWithoutShadow(@NonNull CharSequence title, @NonNull ConfigProperty<Boolean> showIf, @NonNull Consumer<OctoPreferencesBuilder> consumer) {
            return addCategoryInternal(new HeaderRow(title, showIf), null, consumer);
        }

        public OctoPreferencesBuilder row(@NonNull BaseRow row) {
            preferences.add(row);
            return this;
        }

        public OctoPreferencesBuilder sticker(@NonNull Context context, @NonNull String packName, @NonNull StickerUi stickerNum, boolean autoRepeat, @NonNull String description) {
            StickerImageView sticker = StickerFactory.createSticker(context, packName, stickerNum.getValue(), autoRepeat);
            preferences.add(new StickerHeaderRow.StickerHeaderRowBuilder()
                    .stickerView(sticker)
                    .description(description)
                    .build());
            preferences.add(new ShadowRow());
            return this;
        }

        public OctoPreferencesBuilder sticker(@NonNull Context context, @NonNull String packName, @NonNull StickerUi stickerNum, boolean autoRepeat, @NonNull String description, @NonNull ConfigProperty<Boolean> showIf) {
            StickerImageView sticker = StickerFactory.createSticker(context, packName, stickerNum.getValue(), autoRepeat);
            preferences.add(new StickerHeaderRow.StickerHeaderRowBuilder()
                    .stickerView(sticker)
                    .description(description)
                    .showIf(showIf)
                    .build());
            preferences.add(new ShadowRow(showIf));
            return this;
        }

        public OctoPreferencesBuilder octoAnimation(@NonNull String description) {
            preferences.add(new StickerHeaderRow.StickerHeaderRowBuilder()
                    .useOctoAnimation(true)
                    .description(description)
                    .build());
            preferences.add(new ShadowRow());
            return this;
        }

        public OctoPreferencesBuilder sticker(@NonNull Context context, @DrawableRes int animationRes, boolean autoRepeat, @NonNull String description) {
            preferences.add(new StickerHeaderRow.StickerHeaderRowBuilder()
                    .stickerView(StickerFactory.createRLottie(context, animationRes, autoRepeat))
                    .description(description)
                    .build());
            preferences.add(new EmptyCellRow());
            preferences.add(new ShadowRow());
            return this;
        }

        public OctoPreferences build() {
            return new OctoPreferences(name, deepLink, saveButtonAvailable, List.copyOf(preferences), List.copyOf(elements));
        }
    }

    public static final class StickerFactory {
        private StickerFactory() {
        }

        public static @NonNull StickerImageView createSticker(@NonNull Context context, @NonNull String packName, int stickerNum, boolean autoRepeat) {
            StickerImageView view = new StickerImageView(context, UserConfig.selectedAccount);
            view.setStickerPackName(packName);
            view.setStickerNum(stickerNum);
            if (autoRepeat) {
                view.getImageReceiver().setAutoRepeat(1);
            }
            return view;
        }

        public static @NonNull RLottieImageView createRLottie(@NonNull Context context, @DrawableRes int resId, boolean autoRepeat) {
            RLottieImageView view = new RLottieImageView(context);
            view.setAutoRepeat(autoRepeat);
            view.setAnimation(resId, dp(130), dp(130));
            view.playAnimation();
            return view;
        }
    }
}