/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;

import androidx.annotation.NonNull;

import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.Components.Premium.PremiumFeatureBottomSheet;
import org.telegram.ui.Components.TranslateAlert2;
import org.telegram.ui.PremiumPreviewFragment;

import java.util.HashMap;

import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;
import it.octogram.android.TranslatorProvider;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.rows.impl.CheckboxRow;
import it.octogram.android.preferences.rows.impl.FooterInformativeRow;
import it.octogram.android.translator.TranslationsWrapper;

public class OctoTranslatorProviderUI implements PreferencesEntry {
    private PreferencesFragment fragment;
    private Context context;
    private Runnable onChangedRunnable;

    @NonNull
    @Override
    public OctoPreferences getPreferences(@NonNull PreferencesFragment fragment, @NonNull Context context) {
        this.fragment = fragment;
        this.context = context;
        return OctoPreferences.builder("Translator Provider")
                .sticker(context, OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME, StickerUi.MAIN, true, "Choose the provider for translation")
                .category("Telegram Options", this::fillTelegramDefaultOptions)
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title("Telegram translation adapts to server data and may use Google Translate or local fallback methods when needed. It's the default option and is recommended for most users.")
                        .build())
                .category("Local Options", this::fillLocalOptions)
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title("With Device Translation on, single messages use it — translate entire chats rely on Google Translate.")
                        .build())
                .category("OctoGram Options", this::fillOctogramDefaultOptions)
                .build();
    }

    public void setOnChangedRunnable(Runnable onChangedRunnable) {
        this.onChangedRunnable = onChangedRunnable;
    }

    private void fillTelegramDefaultOptions(OctoPreferences.OctoPreferencesBuilder category) {
        HashMap<Integer, String> mainOptions = new HashMap<>();
        mainOptions.put(TranslatorProvider.DEFAULT.getValue(), "Telegram");

        for (int key : mainOptions.keySet()) {
            category.row(new CheckboxRow.CheckboxRowBuilder()
                    .onClick(() -> handleOnClick(key))
                    .preferenceValue(new ConfigProperty<>(null, OctoConfig.INSTANCE.translatorProvider.getValue() == key))
                    .title(mainOptions.get(key))
                    .build()
            );
        }
    }

    private void fillLocalOptions(OctoPreferences.OctoPreferencesBuilder category) {
        HashMap<Integer, String> mainOptions = new HashMap<>();
        mainOptions.put(TranslatorProvider.DEVICE_TRANSLATION.getValue(), "Device Translation");

        for (int key : mainOptions.keySet()) {
            category.row(new CheckboxRow.CheckboxRowBuilder()
                    .onClick(() -> handleOnClick(key))
                    .preferenceValue(new ConfigProperty<>(null, OctoConfig.INSTANCE.translatorProvider.getValue() == key))
                    .title(mainOptions.get(key))
                    .build()
            );
        }
    }

    private void fillOctogramDefaultOptions(OctoPreferences.OctoPreferencesBuilder category) {
        HashMap<Integer, String> mainOptions = new HashMap<>();
        mainOptions.put(TranslatorProvider.GOOGLE.getValue(), "Google");
        mainOptions.put(TranslatorProvider.DEEPL.getValue(), "DeepL");
        mainOptions.put(TranslatorProvider.YANDEX.getValue(), "Yandex");
        mainOptions.put(TranslatorProvider.BAIDU.getValue(), "Baidu");
        mainOptions.put(TranslatorProvider.LINGO.getValue(), "Lingo");

        for (int key : mainOptions.keySet()) {
            category.row(updateProviderAvailability(key, (CheckboxRow.CheckboxRowBuilder) new CheckboxRow.CheckboxRowBuilder()
                    .onClick(() -> handleOnClick(key))
                    .preferenceValue(new ConfigProperty<>(null, OctoConfig.INSTANCE.translatorProvider.getValue() == key))
                    .title(mainOptions.get(key))));
        }
    }

    private CheckboxRow updateProviderAvailability(int key, CheckboxRow.CheckboxRowBuilder builder) {
        if (TranslationsWrapper.isLanguageUnavailable(TranslateAlert2.getToLanguage(), key)) {
            return builder.description(getString(R.string.TranslatorProviderUnsuggested)).build();
        }

        return builder.build();
    }

    private boolean handleOnClick(int key) {
        if (TranslationsWrapper.isLanguageUnavailable(TranslateAlert2.getToLanguage(), key)) {
            AlertDialog.Builder warningBuilder = new AlertDialog.Builder(context);
            warningBuilder.setTitle(getString(R.string.Warning));
            warningBuilder.setPositiveButton(getString(R.string.OK), null);
            warningBuilder.setMessage(getString(R.string.TranslatorUnsupportedLanguage));
            AlertDialog alertDialog = warningBuilder.create();
            alertDialog.show();
            return false;
        }
        OctoConfig.INSTANCE.translatorProvider.updateValue(key);
        if (fragment != null) {
            fragment.finishFragment();
        }
        if (onChangedRunnable != null) {
            onChangedRunnable.run();
        }
        if (key == TranslatorProvider.DEFAULT.getValue()) {
            for (int i = 0; i < UserConfig.MAX_ACCOUNT_COUNT; i++) {
                if (UserConfig.getInstance(i).isClientActivated() && !UserConfig.getInstance(i).isPremium()) {
                    MessagesController.getInstance(i).getTranslateController().setChatTranslateEnabled(false);
                    NotificationCenter.getInstance(i).postNotificationName(NotificationCenter.updateSearchSettings);
                }
            }
        }
        return false;
    }
}
