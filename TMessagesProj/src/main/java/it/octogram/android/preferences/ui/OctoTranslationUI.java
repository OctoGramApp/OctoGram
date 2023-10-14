package it.octogram.android.preferences.ui;

import android.content.Context;
import android.util.Pair;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

import java.util.ArrayList;
import java.util.List;

import it.octogram.android.OctoConfig;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.rows.impl.FooterInformativeRow;
import it.octogram.android.preferences.rows.impl.ListRow;
import it.octogram.android.preferences.rows.impl.SwitchRow;

public class OctoTranslationUI implements PreferencesEntry {
    @Override
    public OctoPreferences getPreferences(PreferencesFragment fragment, Context context) {
        return OctoPreferences.builder("Translation Messages")
                .sticker(context, R.raw.utyan_translator, true, LocaleController.formatString("OctoGeneralSettingsHeader", R.string.OctoGeneralSettingsHeader))
                .category(LocaleController.formatString(LocaleController.getString(R.string.TranslateMessages), R.string.PrivacyHeader), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.showTranslateButton)
                            .title(LocaleController.getString(R.string.ShowTranslateButton))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.translateEntireChat)
                            .title(LocaleController.getString(R.string.ShowTranslateChatButton))
                            .description(LocaleController.getString(R.string.ShowTranslateChatButtonDesc))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .options(List.of(
                                    new Pair<>(0, LocaleController.getString(R.string.TranslatorTypeOcto)),
                                    new Pair<>(1, LocaleController.getString(R.string.TranslatorTypeTG))
                            ))
                            .currentValue(OctoConfig.INSTANCE.translateMode)
                            .title(LocaleController.getString(R.string.TranslatorType)).build());
                    category.row(new ListRow.ListRowBuilder()
                            .options(new ArrayList<>() {{
                                add(new Pair<>(OctoConfig.TranslatorType.TELEGRAM, LocaleController.getString(R.string.ProviderTelegram)));
                                add(new Pair<>(OctoConfig.TranslatorType.GOOGLE, LocaleController.getString(R.string.ProviderGoogleTranslate)));
                                add(new Pair<>(OctoConfig.TranslatorType.DEEPL, LocaleController.getString(R.string.ProviderDeepLTranslate)));
                                add(new Pair<>(OctoConfig.TranslatorType.NIU, LocaleController.getString(R.string.ProviderNiuTrans)));
                                add(new Pair<>(OctoConfig.TranslatorType.YANDEX, LocaleController.getString(R.string.ProviderYandex)));
                                add(new Pair<>(OctoConfig.TranslatorType.DUCKDUCKGO, LocaleController.getString(R.string.ProviderDuckDuckGo)));
                            }})
                            .currentValue(OctoConfig.INSTANCE.translatorType)
                            .title(LocaleController.getString(R.string.TranslationProvider)).build());
                    category.row(new ListRow.ListRowBuilder()
                            .options(List.of(
                                    new Pair<>(0, "Default")
                            ))
                            .currentValue(OctoConfig.INSTANCE.translateLanguage)
                            .title(LocaleController.getString(R.string.TranslationLanguage)).build());
                    category.row(new ListRow.ListRowBuilder()
                            .options(List.of(
                                    new Pair<>(0, "Default")
                            ))
                            .currentValue(OctoConfig.INSTANCE.doNotTranslate)
                            .title(LocaleController.getString(R.string.DoNotTranslate)).build());
                    category.row(new ListRow.ListRowBuilder()
                            .options(List.of(
                                    new Pair<>(0, "Never")
                            ))
                            .currentValue(OctoConfig.INSTANCE.autoTranslate)
                            .title("Auto-Translate").build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.keepMarkdown)
                            .title(LocaleController.getString(R.string.KeepMarkdown))
                            .description(LocaleController.getString(R.string.KeepMarkdownDesc))
                                    .showIf(OctoConfig.INSTANCE.featureNotAvailable)
                            .build());
                })
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(String.format("%s\n\n%s", LocaleController.getString(R.string.TranslateMessagesInfo1), LocaleController.getString(R.string.TranslateMessagesInfo2)))
                        .build()
                ).build();
    }
}