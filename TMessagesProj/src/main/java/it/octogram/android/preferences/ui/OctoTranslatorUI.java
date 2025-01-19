/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui;

import android.content.Context;

import org.telegram.messenger.BuildVars;
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

import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;
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
import it.octogram.android.utils.PopupChoiceDialogOption;
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

        List<PopupChoiceDialogOption> translatorModeOptions = new ArrayList<>();
        translatorModeOptions.add(new PopupChoiceDialogOption()
                .setId(TranslatorMode.DEFAULT.getValue())
                .setItemTitle(LocaleController.getString(R.string.TranslatorModeDefault)));
        translatorModeOptions.add(new PopupChoiceDialogOption()
                .setId(TranslatorMode.INLINE.getValue())
                .setItemTitle(LocaleController.getString(R.string.TranslatorModeInline)));

        if (TranslationsWrapper.canUseExternalApp()) {
            translatorModeOptions.add(new PopupChoiceDialogOption()
                    .setId(TranslatorMode.EXTERNAL.getValue())
                    .setItemTitle(LocaleController.getString(R.string.TranslatorModeExternal)));
        } else if (OctoConfig.INSTANCE.translatorMode.getValue() == TranslatorMode.EXTERNAL.getValue()) {
            OctoConfig.INSTANCE.translatorMode.updateValue(TranslatorMode.DEFAULT.getValue());
            canSelectProvider.setValue(showButtonBool.getValue());
        }

        return OctoPreferences.builder(LocaleController.getString(R.string.Translator))
                .sticker(context, OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME, StickerUi.TRANSLATOR, true, LocaleController.getString(R.string.TranslatorHeader))
                .row(new SwitchRow.SwitchRowBuilder()
                        .onClick(() -> {
                            controller.setContextTranslateEnabled(!showButtonBool.getValue());
                            NotificationCenter.getInstance(UserConfig.selectedAccount).postNotificationName(NotificationCenter.updateSearchSettings);
                            updateItemsVisibility(showButtonBool);
                            return true;
                        })
                        .preferenceValue(showButtonBool)
                        .title(LocaleController.getString(R.string.ShowTranslateButton))
                        .build()
                )
                .row(new SwitchRow.SwitchRowBuilder()
                        .onClick(() -> {
                            if (!translateEntireChat.getValue() && OctoConfig.INSTANCE.translatorProvider.getValue() == TranslatorProvider.DEFAULT.getValue() && !UserConfig.getInstance(UserConfig.selectedAccount).isPremium()) {
                                showUnavailableFeatureTgPremium(fragment, context);
                                return false;
                            }

                            MessagesController.getInstance(UserConfig.selectedAccount).getTranslateController().setChatTranslateEnabled(!translateEntireChat.getValue());
                            NotificationCenter.getInstance(UserConfig.selectedAccount).postNotificationName(NotificationCenter.updateSearchSettings);
                            updateItemsVisibility(translateEntireChat);

                            return true;
                        })
                        .premium(OctoConfig.INSTANCE.translatorProvider.getValue() == TranslatorProvider.DEFAULT.getValue() && !UserConfig.getInstance(UserConfig.selectedAccount).isPremium())
                        .autoShowPremiumAlert(false)
                        .preferenceValue(translateEntireChat)
                        .title(LocaleController.getString(R.string.ShowTranslateChatButton))
                        .build()
                )
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(LocaleController.getString(R.string.TranslateMessagesInfo1))
                        .build()
                )
                .category(LocaleController.getString(R.string.TranslatorCategoryOptions), canSelectDoNotTranslate, category -> {
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.translatorMode)
                            .options(translatorModeOptions)
                            .onSelected(() -> {
                                updateItemsVisibility();
                                resetManualSavedMessages();
                            })
                            .showIf(showButtonBool)
                            .title(LocaleController.getString(R.string.TranslatorMode))
                            .build()
                    );
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new DestinationLanguageSettings()))
                            .value(getTranslateDestinationStatus())
                            .showIf(canSelectProvider)
                            .title(LocaleController.getString(R.string.TranslatorDestination))
                            .build()
                    );
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new RestrictedLanguagesSelectActivity()))
                            .value(getDoNotTranslateStatus())
                            .showIf(canSelectDoNotTranslate)
                            .title(LocaleController.getString(R.string.DoNotTranslate))
                            .build()
                    );
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.translatorProvider)
                            .options(getProvidersPopupOptions())
                            .onSelected(() -> {
                                boolean needEntireChatReload = false;
                                if (!UserConfig.getInstance(UserConfig.selectedAccount).isPremium()) {
                                    if (OctoConfig.INSTANCE.translatorProvider.getValue() == TranslatorProvider.DEFAULT.getValue() && translateEntireChat.getValue()) {
                                        MessagesController.getInstance(UserConfig.selectedAccount).getTranslateController().setChatTranslateEnabled(false);
                                        NotificationCenter.getInstance(UserConfig.selectedAccount).postNotificationName(NotificationCenter.updateSearchSettings);
                                    }
                                    needEntireChatReload = true;
                                }

                                updateItemsVisibility();
                                checkProviderAvailability(context);

                                if (needEntireChatReload) {
                                    fragment.getParentLayout().rebuildFragments(INavigationLayout.REBUILD_FLAG_REBUILD_LAST);
                                }
                            })
                            .showIf(canSelectProvider)
                            .title(LocaleController.getString(R.string.TranslatorProvider))
                            .build()
                    );
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.translatorFormality)
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(TranslatorFormality.DEFAULT.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.TranslatorFormalityDefault)),
                                    new PopupChoiceDialogOption()
                                            .setId(TranslatorFormality.LOW.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.TranslatorFormalityLow)),
                                    new PopupChoiceDialogOption()
                                            .setId(TranslatorFormality.HIGH.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.TranslatorFormalityHigh))
                            ))
                            .showIf(canSelectFormality)
                            .title(LocaleController.getString(R.string.TranslatorFormality))
                            .build()
                    );
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.translatorKeepMarkdown)
                            .showIf(canSelectKeepMarkdown)
                            .title(LocaleController.getString(R.string.TranslatorKeepMarkdown))
                            .build()
                    );
                }).build();
    }

    public static List<PopupChoiceDialogOption> getProvidersPopupOptions() {
        List<PopupChoiceDialogOption> list = List.of(
                updateProviderAvailability(new PopupChoiceDialogOption()
                        .setId(TranslatorProvider.DEFAULT.getValue())
                        .setItemTitle("Telegram")
                        .setItemDescription(LocaleController.getString(R.string.TranslatorProviderSuggestedPremium))),
                updateProviderAvailability(new PopupChoiceDialogOption()
                        .setId(TranslatorProvider.GOOGLE.getValue())
                        .setItemTitle("Google")
                        .setItemDescription(LocaleController.getString(R.string.TranslatorProviderSuggestedGeneral))),
                updateProviderAvailability(new PopupChoiceDialogOption()
                        .setId(TranslatorProvider.DEEPL.getValue())
                        .setItemTitle("DeepL")
                        .setItemDescription(LocaleController.getString(R.string.TranslatorProviderSuggestedAccurate))),
                updateProviderAvailability(new PopupChoiceDialogOption().setId(TranslatorProvider.YANDEX.getValue()).setItemTitle("Yandex")),
                updateProviderAvailability(new PopupChoiceDialogOption()
                        .setId(TranslatorProvider.BAIDU.getValue())
                        .setItemTitle("Baidu")
                        .setItemDescription(LocaleController.getString(R.string.TranslatorProviderSuggestedChinese))),
                updateProviderAvailability(new PopupChoiceDialogOption()
                        .setId(TranslatorProvider.LINGO.getValue())
                        .setItemTitle("Lingo")
                        .setItemDescription(LocaleController.getString(R.string.TranslatorProviderSuggestedLingo)))
        );

        if (BuildVars.DEBUG_PRIVATE_VERSION || BuildVars.DEBUG_VERSION || OctoConfig.INSTANCE.translatorProvider.getValue() == TranslatorProvider.EMOJIS.getValue()) {
            ArrayList<PopupChoiceDialogOption> data = new ArrayList<>(list);
            data.add(new PopupChoiceDialogOption()
                    .setId(TranslatorProvider.EMOJIS.getValue())
                    .setItemTitle("Emojis")
                    .setItemDescription("For the meme"));
            return data;
        }

        return list;
    }

    private static PopupChoiceDialogOption updateProviderAvailability(PopupChoiceDialogOption provider) {
        if (TranslationsWrapper.isLanguageUnavailable(TranslateAlert2.getToLanguage(), provider.id)) {
            provider.setClickable(false);
            provider.setItemDescription(LocaleController.getString(R.string.TranslatorProviderUnsuggested));
        }

        return provider;
    }

    private void updateItemsVisibility(ConfigProperty<Boolean> except) {
        TranslateController translateController = MessagesController.getInstance(UserConfig.selectedAccount).getTranslateController();
        boolean isSingleMessageTranslationEnabled = translateController.isContextTranslateEnabled();
        boolean isTranslateEntireChatEnabled = translateController.isFeatureAvailable();
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
            canSelectKeepMarkdown.setValue(canShowOtherOptions && OctoConfig.INSTANCE.translatorProvider.getValue() != TranslatorProvider.LINGO.getValue() && OctoConfig.INSTANCE.translatorMode.getValue() == TranslatorMode.INLINE.getValue());
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

    private String getTranslateDestinationStatus() {
        String currentDestination = MessagesController.getGlobalMainSettings().getString("translate_to_language", null);
        if (currentDestination == null || currentDestination.isEmpty()) {
            return LocaleController.getString(R.string.TranslatorDestinationFollow);
        } else {
            return TranslateAlert2.capitalFirst(TranslateAlert2.languageName(currentDestination, null));
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

    private void showUnavailableFeatureTgPremium(PreferencesFragment fragment, Context context) {
        AlertDialog.Builder warningBuilder = new AlertDialog.Builder(context);
        warningBuilder.setTitle(LocaleController.getString(R.string.Warning));
        if (MessagesController.getInstance(UserConfig.selectedAccount).premiumFeaturesBlocked()) {
            warningBuilder.setPositiveButton(LocaleController.getString(R.string.OK), null);
        } else {
            warningBuilder.setPositiveButton(LocaleController.getString(R.string.MoreInfo), (dialog1, which1) -> {
                dialog1.dismiss();
                fragment.showDialog(new PremiumFeatureBottomSheet(fragment, PremiumPreviewFragment.PREMIUM_FEATURE_TRANSLATIONS, false));
            });
            warningBuilder.setNeutralButton(LocaleController.getString(R.string.Cancel), null);
        }
        warningBuilder.setMessage(LocaleController.getString(R.string.TranslatorEntireUnavailable));
        AlertDialog alertDialog = warningBuilder.create();
        alertDialog.show();
    }

    private void checkProviderAvailability(Context context) {
        String preTranslateDestLanguage = OctoConfig.INSTANCE.lastTranslatePreSendLanguage.getValue();
        if (preTranslateDestLanguage != null && TranslationsWrapper.isLanguageUnavailable(preTranslateDestLanguage)) {
            OctoConfig.INSTANCE.lastTranslatePreSendLanguage.updateValue(null);
        }

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
            AlertDialog.Builder warningBuilder = new AlertDialog.Builder(context);
            warningBuilder.setTitle(LocaleController.getString(R.string.Warning));
            warningBuilder.setPositiveButton(LocaleController.getString(R.string.OK), null);
            warningBuilder.setMessage(LocaleController.getString(R.string.TranslatorProviderUnsupportedDialogs));
            AlertDialog alertDialog = warningBuilder.create();
            alertDialog.show();
        }
    }
}
