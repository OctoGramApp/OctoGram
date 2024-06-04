/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

package it.octogram.android.preferences.ui;

import android.content.Context;
import android.util.Pair;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.TranslateController;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.INavigationLayout;
import org.telegram.ui.Components.Premium.PremiumFeatureBottomSheet;
import org.telegram.ui.Components.TranslateAlert2;
import org.telegram.ui.PremiumPreviewFragment;
import org.telegram.ui.RestrictedLanguagesSelectActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.TranslatorFormality;
import it.octogram.android.TranslatorMode;
import it.octogram.android.TranslatorProvider;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.rows.impl.FooterInformativeRow;
import it.octogram.android.preferences.rows.impl.ListRow;
import it.octogram.android.preferences.rows.impl.SwitchRow;
import it.octogram.android.preferences.rows.impl.TextIconRow;
import it.octogram.android.utils.translator.TranslationsWrapper;

public class OctoTranslatorUI implements PreferencesEntry {
    private final ConfigProperty<Boolean> showButtonBool = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> translateEntireChat = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> canSelectDoNotTranslate = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> canSelectProvider = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> canSelectFormality = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> canSelectKeepMarkdown = new ConfigProperty<>(null, false);

    @Override
    public OctoPreferences getPreferences(PreferencesFragment fragment, Context context) {
        TranslateController controller = MessagesController.getInstance(UserConfig.selectedAccount).getTranslateController();

        updateItemsVisibility();

        List<Pair<Integer, String>> translatorModeOptions = new ArrayList<>();
        translatorModeOptions.add(new Pair<>(TranslatorMode.DEFAULT.getValue(), LocaleController.getString("TranslatorModeDefault", R.string.TranslatorModeDefault)));
        translatorModeOptions.add(new Pair<>(TranslatorMode.INLINE.getValue(), LocaleController.getString("TranslatorModeInline", R.string.TranslatorModeInline)));

        if (TranslationsWrapper.canUseExternalApp()) {
            translatorModeOptions.add(new Pair<>(TranslatorMode.EXTERNAL.getValue(), LocaleController.getString("TranslatorModeExternal", R.string.TranslatorModeExternal)));
        } else if (OctoConfig.INSTANCE.translatorMode.getValue() == TranslatorMode.EXTERNAL.getValue()) {
            OctoConfig.INSTANCE.translatorMode.updateValue(TranslatorMode.DEFAULT.getValue());
            canSelectProvider.setValue(showButtonBool.getValue());
        }

        return OctoPreferences.builder(LocaleController.formatString("Translator", R.string.Translator))
                .sticker(context, R.raw.translator_anim, true, LocaleController.formatString("TranslatorHeader", R.string.TranslatorHeader))
                .row(new SwitchRow.SwitchRowBuilder()
                        .onClick(() -> {
                            controller.setContextTranslateEnabled(!showButtonBool.getValue());
                            NotificationCenter.getInstance(UserConfig.selectedAccount).postNotificationName(NotificationCenter.updateSearchSettings);
                            updateItemsVisibility(showButtonBool);
                            return true;
                        })
                        .preferenceValue(showButtonBool)
                        .title(LocaleController.getString("ShowTranslateButton", R.string.ShowTranslateButton))
                        .build()
                )
                .row(new SwitchRow.SwitchRowBuilder()
                        .onClick(() -> {
                            if (!translateEntireChat.getValue() && OctoConfig.INSTANCE.translatorProvider.getValue() == TranslatorProvider.DEFAULT.getValue() && !UserConfig.getInstance(UserConfig.selectedAccount).isPremium()) {
                                fragment.showDialog(new PremiumFeatureBottomSheet(fragment, PremiumPreviewFragment.PREMIUM_FEATURE_TRANSLATIONS, false));
                                return false;
                            }

                            MessagesController.getInstance(UserConfig.selectedAccount).getTranslateController().setChatTranslateEnabled(!translateEntireChat.getValue());
                            NotificationCenter.getInstance(UserConfig.selectedAccount).postNotificationName(NotificationCenter.updateSearchSettings);
                            updateItemsVisibility(translateEntireChat);

                            return true;
                        })
                        .preferenceValue(translateEntireChat)
                        .title(LocaleController.getString("ShowTranslateChatButton", R.string.ShowTranslateChatButton))
                        .build()
                )
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(LocaleController.getString("TranslateMessagesInfo1", R.string.TranslateMessagesInfo1))
                        .build()
                )
                .category(LocaleController.getString("TranslatorCategoryOptions", R.string.TranslatorCategoryOptions), canSelectDoNotTranslate, category -> {
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.translatorMode)
                            .options(translatorModeOptions)
                            .onSelected(() -> {
                                updateItemsVisibility();
                                resetManualSavedMessages();
                            })
                            .showIf(showButtonBool)
                            .title(LocaleController.getString("TranslatorMode", R.string.TranslatorMode))
                            .build()
                    );
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new RestrictedLanguagesSelectActivity()))
                            .value(getDoNotTranslateStatus())
                            .showIf(canSelectDoNotTranslate)
                            .title(LocaleController.getString("DoNotTranslate", R.string.DoNotTranslate))
                            .build()
                    );
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.translatorProvider)
                            .options(List.of(
                                    new Pair<>(TranslatorProvider.DEFAULT.getValue(), "Telegram"),
                                    new Pair<>(TranslatorProvider.GOOGLE.getValue(), "Google"),
                                    new Pair<>(TranslatorProvider.YANDEX.getValue(), "Yandex"),
                                    new Pair<>(TranslatorProvider.DEEPL.getValue(), "Deepl")
                            ))
                            .onSelected(() -> {
                                boolean needEntireChatReload = false;
                                if (OctoConfig.INSTANCE.translatorProvider.getValue() == TranslatorProvider.DEFAULT.getValue() && !UserConfig.getInstance(UserConfig.selectedAccount).isPremium() && translateEntireChat.getValue()) {
                                    MessagesController.getInstance(UserConfig.selectedAccount).getTranslateController().setChatTranslateEnabled(false);
                                    NotificationCenter.getInstance(UserConfig.selectedAccount).postNotificationName(NotificationCenter.updateSearchSettings);
                                    needEntireChatReload = true;
                                }

                                updateItemsVisibility();
                                checkProviderAvailability(fragment, context);

                                if (needEntireChatReload) {
                                    fragment.getParentLayout().rebuildFragments(INavigationLayout.REBUILD_FLAG_REBUILD_LAST);
                                }
                            })
                            .showIf(canSelectProvider)
                            .title(LocaleController.getString("TranslatorProvider", R.string.TranslatorProvider))
                            .build()
                    );
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.translatorFormality)
                            .options(List.of(
                                    new Pair<>(TranslatorFormality.DEFAULT.getValue(), LocaleController.getString("TranslatorFormalityDefault", R.string.TranslatorFormalityDefault)),
                                    new Pair<>(TranslatorFormality.LOW.getValue(), LocaleController.getString("TranslatorFormalityLow", R.string.TranslatorFormalityLow)),
                                    new Pair<>(TranslatorFormality.HIGH.getValue(), LocaleController.getString("TranslatorFormalityHigh", R.string.TranslatorFormalityHigh))
                            ))
                            .showIf(canSelectFormality)
                            .title(LocaleController.getString("TranslatorFormality", R.string.TranslatorFormality))
                            .build()
                    );
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.translatorKeepMarkdown)
                            .showIf(canSelectKeepMarkdown)
                            .title(LocaleController.getString("TranslatorKeepMarkdown", R.string.TranslatorKeepMarkdown))
                            .build()
                    );
                }).build();
    }

    private void updateItemsVisibility(ConfigProperty<Boolean> except) {
        TranslateController translateController = MessagesController.getInstance(UserConfig.selectedAccount).getTranslateController();
        boolean isSingleMessageTranslationEnabled = translateController.isContextTranslateEnabled();
        boolean isTranslateEntireChatEnabled = translateController.isChatTranslateEnabled();
        boolean canShowOtherOptions = isSingleMessageTranslationEnabled || isTranslateEntireChatEnabled;

        if (except != showButtonBool) {
            showButtonBool.setValue(isSingleMessageTranslationEnabled);
        }

        if (except != translateEntireChat) {
            translateEntireChat.setValue(isTranslateEntireChatEnabled);
        }

        if (except != canSelectDoNotTranslate) {
            canSelectDoNotTranslate.setValue(canShowOtherOptions);
        }

        if (except != canSelectProvider) {
            canSelectProvider.setValue(canShowOtherOptions && OctoConfig.INSTANCE.translatorMode.getValue() != TranslatorMode.EXTERNAL.getValue());
        }

        if (except != canSelectFormality) {
            canSelectFormality.setValue(canShowOtherOptions && OctoConfig.INSTANCE.translatorProvider.getValue() == TranslatorProvider.DEEPL.getValue());
        }

        if (except != canSelectKeepMarkdown) {
            canSelectKeepMarkdown.setValue(canShowOtherOptions && OctoConfig.INSTANCE.translatorMode.getValue() == TranslatorMode.INLINE.getValue() && OctoConfig.INSTANCE.translatorProvider.getValue() != TranslatorProvider.YANDEX.getValue());
        }
    }

    private void updateItemsVisibility() {
        updateItemsVisibility(null);
    }

    private String getDoNotTranslateStatus() {
        HashSet<String> langCodes = RestrictedLanguagesSelectActivity.getRestrictedLanguages();
        if (langCodes.isEmpty()) {
            return "";
        } else if (langCodes.size() == 1) {
            return TranslateAlert2.capitalFirst(TranslateAlert2.languageName(langCodes.iterator().next(), null));
        } else {
            return String.format(LocaleController.getPluralString("Languages", langCodes.size()), langCodes.size());
        }
    }

    private void resetManualSavedMessages() {
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            if (UserConfig.getInstance(a).isClientActivated()) {
                TranslateController controller = MessagesController.getInstance(a).getTranslateController();
                controller.resetTranslatingItems();
                controller.resetManualTranslations();
            }
        }
    }

    private void checkProviderAvailability(PreferencesFragment fragment, Context context) {
        String finalPopupText = "";

        String currentTranslateToDefault = TranslateAlert2.getToLanguage();
        if (currentTranslateToDefault != null && TranslationsWrapper.isLanguageUnavailable(currentTranslateToDefault)) {
            TranslateAlert2.resetToLanguage();
        }

        Locale currentLocale = LocaleController.getInstance().getCurrentLocale();
        if (TranslationsWrapper.isLanguageUnavailable(currentLocale.getLanguage())) {
            AndroidUtilities.runOnUIThread(() -> {
                OctoConfig.INSTANCE.translatorProvider.updateValue(TranslatorProvider.DEFAULT.getValue());
                fragment.getParentLayout().rebuildFragments(INavigationLayout.REBUILD_FLAG_REBUILD_LAST);
            });
            finalPopupText = LocaleController.formatString("TranslatorProviderUnsupported", R.string.TranslatorProviderUnsupported, currentLocale.getDisplayName());
        }

        if (finalPopupText.isEmpty()) {
            int dialogsWithUnavailableLanguage = 0;
            for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
                if (UserConfig.getInstance(a).isClientActivated()) {
                    TranslateController currentTranslateController = MessagesController.getInstance(a).getTranslateController();
                    int dialogsBrokenCurrentAccount = currentTranslateController.getDialogsWithUnavailableLanguage();

                    if (dialogsBrokenCurrentAccount > 0) {
                        dialogsWithUnavailableLanguage += dialogsBrokenCurrentAccount;
                        currentTranslateController.fixChatsWithUnavailableLanguage();
                    }
                }
            }

            if (dialogsWithUnavailableLanguage > 0) {
                finalPopupText = LocaleController.getString("TranslatorProviderUnsupportedDialogs", R.string.TranslatorProviderUnsupportedDialogs);
            }
        }

        if (!finalPopupText.isEmpty()) {
            AlertDialog.Builder warningBuilder = new AlertDialog.Builder(context);
            warningBuilder.setTitle(LocaleController.getString(R.string.Warning));
            warningBuilder.setPositiveButton(LocaleController.getString("OK", R.string.OK), (dialog1, which1) -> dialog1.dismiss());
            warningBuilder.setMessage(finalPopupText);
            AlertDialog alertDialog = warningBuilder.create();
            alertDialog.show();
        }
    }
}
