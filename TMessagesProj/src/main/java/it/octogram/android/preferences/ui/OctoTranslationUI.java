package it.octogram.android.preferences.ui;

import android.content.Context;
import android.util.Pair;
import it.octogram.android.OctoConfig;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.rows.impl.FooterRow;
import it.octogram.android.preferences.rows.impl.HeaderRow;
import it.octogram.android.preferences.rows.impl.ListRow;
import it.octogram.android.preferences.rows.impl.SwitchRow;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.List;

public class OctoTranslationUI implements PreferencesEntry {
    @Override
    public OctoPreferences getPreferences(BaseFragment fragment, Context context) {
        return OctoPreferences.builder("Translation Messages")
                .sticker(context, R.raw.utyan_translator, true, LocaleController.formatString("OctoGeneralSettingsHeader", R.string.OctoGeneralSettingsHeader))
                .category(LocaleController.formatString("PrivacyHeader", R.string.PrivacyHeader), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.showTranslateButton)
                            .title("Show Translate Button")
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.translateEntireChat)
                            .title("Translate Entire Chat")
                            .description("Show the translation bar below the chat title")
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .options(List.of(
                                    new Pair<>(0, "In-Message"),
                                    new Pair<>(1, "Dialog Popup")
                            ))
                            .title("Translator Type").build());
                    category.row(new ListRow.ListRowBuilder()
                            .options(List.of(
                                    new Pair<>(0, "Google Translate"),
                                    new Pair<>(1, "Yandex Translate"),
                                    new Pair<>(2, "Microsoft Translate")
                            ))
                            .title("Provider").build());
                    category.row(new ListRow.ListRowBuilder()
                            .options(List.of(
                                    new Pair<>(0, "Default")
                            ))
                            .title("Translation Language").build());
                    category.row(new ListRow.ListRowBuilder()
                            .options(List.of(
                                    new Pair<>(0, "Default")
                            ))
                            .title("Do Not Translate").build());
                    category.row(new ListRow.ListRowBuilder()
                            .options(List.of(
                                    new Pair<>(0, "Never")
                            ))
                            .title("Auto-Translate").build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.keepMarkdown)
                            .title("Keep Markdown Formatting")
                            .description("When translating a message, keep the markdown formatting.")
                            .build());
                    category.row(new FooterRow.FooterRowBuilder()
                            .title("The 'Translate' button will appear when you make a single tap on a text message.\n\nGoogle Translate may have access to the message you translate.")
                            .build());
                }).build();
    }
}