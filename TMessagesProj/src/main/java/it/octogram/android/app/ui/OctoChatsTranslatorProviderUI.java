/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;

import androidx.annotation.NonNull;

import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.TranslateController;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.Components.TranslateAlert2;

import java.util.ArrayList;

import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;
import it.octogram.android.StoreUtils;
import it.octogram.android.TranslatorProvider;
import it.octogram.android.app.OctoPreferences;
import it.octogram.android.app.PreferencesEntry;
import it.octogram.android.app.fragment.PreferencesFragment;
import it.octogram.android.app.rows.impl.CheckboxRow;
import it.octogram.android.app.rows.impl.FooterInformativeRow;
import it.octogram.android.app.ui.bottomsheets.TranslatorConfigBottomSheet;
import it.octogram.android.utils.translator.MainTranslationsHandler;
import it.octogram.android.utils.translator.localhelper.LocalTranslator;
import it.octogram.android.utils.translator.localhelper.OnDeviceHelper;

public class OctoChatsTranslatorProviderUI implements PreferencesEntry {
    private PreferencesFragment fragment;
    private Context context;
    private Runnable onChangedRunnable;
    @NonNull
    @Override
    public OctoPreferences getPreferences(@NonNull PreferencesFragment fragment, @NonNull Context context) {
        this.fragment = fragment;
        this.context = context;
        return OctoPreferences.builder(getString(R.string.TranslatorProvider))
                .sticker(context, OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME, StickerUi.MAIN, true, "Choose the provider for translation")
                .category(R.string.TranslatorProviderTelegramOptions, this::fillTelegramDefaultOptions)
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(getString(R.string.TranslatorProviderTelegramOptions_Desc))
                        .build())
                .category(R.string.TranslatorProviderOctoGramOptions, this::fillOctogramDefaultOptions)
                .category(R.string.TranslatorProviderCloudOptions, this::fillCloudOptions)
                .build();
    }

    public void setOnChangedRunnable(Runnable onChangedRunnable) {
        this.onChangedRunnable = onChangedRunnable;
    }

    private void fillTelegramDefaultOptions(OctoPreferences.OctoPreferencesBuilder category) {
        addPropertyToCategory(TranslatorProvider.DEFAULT.getValue(), category);
    }

    private void fillOctogramDefaultOptions(OctoPreferences.OctoPreferencesBuilder category) {
        ArrayList<Integer> mainOptions = new ArrayList<>();
        if (!StoreUtils.isFromPlayStore()) {
            mainOptions.add(TranslatorProvider.DEVICE_TRANSLATION.getValue());
        }
        mainOptions.add(TranslatorProvider.GOOGLE.getValue());
        mainOptions.add(TranslatorProvider.DEEPL.getValue());
        mainOptions.add(TranslatorProvider.YANDEX.getValue());
        mainOptions.add(TranslatorProvider.BAIDU.getValue());
        mainOptions.add(TranslatorProvider.LINGO.getValue());

        for (int key : mainOptions) {
            addPropertyToCategory(key, category);
        }
    }

    private void fillCloudOptions(OctoPreferences.OctoPreferencesBuilder category) {
        addPropertyToCategory(TranslatorProvider.GOOGLE_CLOUD.getValue(), category);
    }

    private void addPropertyToCategory(int key, OctoPreferences.OctoPreferencesBuilder category) {
        ConfigProperty<Boolean> property = new ConfigProperty<>(null, OctoConfig.INSTANCE.translatorProvider.getValue() == key);
        category.row(updateProviderAvailability(key, (CheckboxRow.CheckboxRowBuilder) new CheckboxRow.CheckboxRowBuilder()
                .onClick(() -> {
                    handleOnClick(key);
                    return false;
                })
                .preferenceValue(property)
                .title(MainTranslationsHandler.getProviderName(key))));
    }

    private CheckboxRow updateProviderAvailability(int key, CheckboxRow.CheckboxRowBuilder builder) {
        if (MainTranslationsHandler.isLanguageUnavailable(TranslateAlert2.getToLanguage(), key)) {
            return builder.description(getString(R.string.TranslatorProviderUnsuggested)).build();
        }

        return builder.build();
    }

    private void handleOnClick(int key) {
        if (MainTranslationsHandler.isLanguageUnavailable(TranslateAlert2.getToLanguage(), key)) {
            AlertDialog.Builder warningBuilder = new AlertDialog.Builder(context);
            warningBuilder.setTitle(getString(R.string.Warning));
            warningBuilder.setPositiveButton(getString(R.string.OK), null);
            warningBuilder.setMessage(getString(R.string.TranslatorUnsupportedLanguage));
            AlertDialog alertDialog = warningBuilder.create();
            alertDialog.show();
            return;
        }
        if (key == TranslatorProvider.GOOGLE_CLOUD.getValue() || (key == TranslatorProvider.DEVICE_TRANSLATION.getValue() && !OnDeviceHelper.isAvailable(context))) {
            new TranslatorConfigBottomSheet(context, fragment, key == TranslatorProvider.DEVICE_TRANSLATION.getValue(), new TranslatorConfigBottomSheet.TranslatorConfigInterface() {
                @Override
                public void onStateUpdated() {
                    saveKey(key);
                }

                @Override
                public void onDisabled() {
                    saveKey(TranslatorProvider.DEFAULT.getValue());
                }
            }).show();
            return;
        }
        saveKey(key);
    }

    private void saveKey(int key) {
        if (OctoConfig.INSTANCE.translatorProvider.getValue() == TranslatorProvider.DEVICE_TRANSLATION.getValue() && key != TranslatorProvider.DEVICE_TRANSLATION.getValue() && OnDeviceHelper.isAvailable(context)) {
            LocalTranslator.deleteAllModels();
        }
        OctoConfig.INSTANCE.translatorProvider.updateValue(key);
        if (onChangedRunnable != null) {
            onChangedRunnable.run();
        }
        fixStateAfterProviderChange();
        if (key == TranslatorProvider.DEFAULT.getValue()) {
            for (int i = 0; i < UserConfig.MAX_ACCOUNT_COUNT; i++) {
                if (UserConfig.getInstance(i).isClientActivated() && !UserConfig.getInstance(i).isPremium()) {
                    MessagesController.getInstance(i).getTranslateController().setChatTranslateEnabled(false);
                    NotificationCenter.getInstance(i).postNotificationName(NotificationCenter.updateSearchSettings);
                }
            }
        }
        if (fragment != null) {
            fragment.finishFragment();
        }
    }

    private void fixStateAfterProviderChange() {
        String preTranslateDestLanguage = OctoConfig.INSTANCE.lastTranslatePreSendLanguage.getValue();
        if (preTranslateDestLanguage != null && MainTranslationsHandler.isLanguageUnavailable(preTranslateDestLanguage)) {
            OctoConfig.INSTANCE.lastTranslatePreSendLanguage.updateValue(null);
        }

        int dialogsWithUnavailableLanguage = 0;
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            if (UserConfig.getInstance(a).isClientActivated()) {
                TranslateController currentTranslateController = MessagesController.getInstance(a).getTranslateController();
                dialogsWithUnavailableLanguage += currentTranslateController.fixChatsWithUnavailableLanguage();
            }
        }

        if (dialogsWithUnavailableLanguage > 0) {
            AlertDialog.Builder warningBuilder = new AlertDialog.Builder(context);
            warningBuilder.setTitle(getString(R.string.Warning));
            warningBuilder.setPositiveButton(getString(R.string.OK), null);
            warningBuilder.setMessage(getString(R.string.TranslatorProviderUnsupportedDialogs));
            AlertDialog alertDialog = warningBuilder.create();
            alertDialog.show();
        }
    }
}
