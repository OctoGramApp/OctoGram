/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui;

import static org.telegram.messenger.LocaleController.getPluralString;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.TranslateController;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.ColoredImageSpan;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.LinkSpanDrawable;
import org.telegram.ui.Components.Premium.PremiumFeatureBottomSheet;
import org.telegram.ui.Components.ScaleStateListAnimator;
import org.telegram.ui.Components.TranslateAlert2;
import org.telegram.ui.Components.spoilers.SpoilersTextView;
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
import it.octogram.android.deeplink.DeepLinkDef;
import it.octogram.android.logs.OctoLogging;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.rows.impl.CustomCellRow;
import it.octogram.android.preferences.rows.impl.FooterInformativeRow;
import it.octogram.android.preferences.rows.impl.ListRow;
import it.octogram.android.preferences.rows.impl.SwitchRow;
import it.octogram.android.preferences.rows.impl.TextIconRow;
import it.octogram.android.translator.TranslationsWrapper;
import it.octogram.android.utils.appearance.PopupChoiceDialogOption;


public class OctoTranslatorUI implements PreferencesEntry {
    private final ConfigProperty<Boolean> showButtonBool = new ConfigProperty<>("showTranslateButton", false);
    private final ConfigProperty<Boolean> translateEntireChat = new ConfigProperty<>("canTranslateEntireChat", false);
    private final ConfigProperty<Boolean> canSelectDoNotTranslate = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> canSelectProvider = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> canSelectFormality = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> canSelectKeepMarkdown = new ConfigProperty<>(null, false);

    @NonNull
    @Override
    public OctoPreferences getPreferences(@NonNull PreferencesFragment fragment, @NonNull Context context) {
        TranslateController controller = MessagesController.getInstance(UserConfig.selectedAccount).getTranslateController();

        updateItemsVisibility();

        List<PopupChoiceDialogOption> translatorModeOptions = new ArrayList<>();
        translatorModeOptions.add(new PopupChoiceDialogOption()
                .setId(TranslatorMode.DEFAULT.getValue())
                .setItemTitle(getString(R.string.TranslatorModeDefault)));
        translatorModeOptions.add(new PopupChoiceDialogOption()
                .setId(TranslatorMode.INLINE.getValue())
                .setItemTitle(getString(R.string.TranslatorModeInline)));

        if (TranslationsWrapper.canUseExternalApp()) {
            translatorModeOptions.add(new PopupChoiceDialogOption()
                    .setId(TranslatorMode.EXTERNAL.getValue())
                    .setItemTitle(getString(R.string.TranslatorModeExternal)));
        } else if (OctoConfig.INSTANCE.translatorMode.getValue() == TranslatorMode.EXTERNAL.getValue()) {
            OctoConfig.INSTANCE.translatorMode.updateValue(TranslatorMode.DEFAULT.getValue());
            canSelectProvider.updateValue(showButtonBool.getValue());
        }

        return OctoPreferences.builder(getString(R.string.Translator))
                .deepLink(DeepLinkDef.TRANSLATOR)
                .sticker(context, OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME, StickerUi.TRANSLATOR, true, getString(R.string.TranslatorHeader))
                .row(new SwitchRow.SwitchRowBuilder()
                        .onClick(() -> {
                            controller.setContextTranslateEnabled(!showButtonBool.getValue());
                            NotificationCenter.getInstance(UserConfig.selectedAccount).postNotificationName(NotificationCenter.updateSearchSettings);
                            updateItemsVisibility(showButtonBool);
                            return true;
                        })
                        .preferenceValue(showButtonBool)
                        .title(getString(R.string.ShowTranslateButton))
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
                        .preferenceValue(translateEntireChat)
                        .title(getString(R.string.ShowTranslateChatButton))
                        .build()
                )
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(getString(R.string.TranslateMessagesInfo1))
                        .build()
                )
                .category(getString(R.string.TranslatorCategoryOptions), canSelectDoNotTranslate, category -> {
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.translatorMode)
                            .options(translatorModeOptions)
                            .onSelected(() -> {
                                updateItemsVisibility();
                                resetManualSavedMessages();
                            })
                            .showIf(showButtonBool)
                            .title(getString(R.string.TranslatorMode))
                            .build()
                    );
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new DestinationLanguageSettings()))
                            .propertySelectionTag("destinationLanguage")
                            .value(getTranslateDestinationStatus())
                            .setDynamicDataUpdate(new TextIconRow.OnDynamicDataUpdate() {
                                @Override
                                public String getTitle() {
                                    return getString(R.string.TranslatorDestination);
                                }

                                @Override
                                public String getValue() {
                                    return getTranslateDestinationStatus();
                                }
                            })
                            .showIf(canSelectProvider)
                            .title(getString(R.string.TranslatorDestination))
                            .build()
                    );
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new RestrictedLanguagesSelectActivity()))
                            .propertySelectionTag("doNotTranslate")
                            .value(getDoNotTranslateStatus())
                            .setDynamicDataUpdate(new TextIconRow.OnDynamicDataUpdate() {
                                @Override
                                public String getTitle() {
                                    return getString(R.string.DoNotTranslate);
                                }

                                @Override
                                public String getValue() {
                                    return getDoNotTranslateStatus();
                                }
                            })
                            .showIf(canSelectDoNotTranslate)
                            .title(getString(R.string.DoNotTranslate))
                            .build()
                    );
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> {
                                OctoTranslatorProviderUI ui = new OctoTranslatorProviderUI();
                                ui.setOnChangedRunnable(fragment::reloadUIAfterValueUpdate);
                                fragment.presentFragment(new PreferencesFragment(ui));
                            })
                            .propertySelectionTag("translatorProvider")
                            .value(TranslationsWrapper.getProviderName())
                            .setDynamicDataUpdate(new TextIconRow.OnDynamicDataUpdate() {
                                @Override
                                public String getTitle() {
                                    return getString(R.string.TranslatorProvider);
                                }

                                @Override
                                public String getValue() {
                                    return TranslationsWrapper.getProviderName();
                                }
                            })
                            .showIf(canSelectProvider)
                            .title(getString(R.string.TranslatorProvider))
                            .build()
                    );
//                    category.row(new ListRow.ListRowBuilder()
//                            .currentValue(OctoConfig.INSTANCE.translatorProvider)
//                            .options(getProvidersPopupOptions())
//                            .onSelected(() -> {
//                                boolean needEntireChatReload = false;
//                                if (!UserConfig.getInstance(UserConfig.selectedAccount).isPremium()) {
//                                    if (OctoConfig.INSTANCE.translatorProvider.getValue() == TranslatorProvider.DEFAULT.getValue() && translateEntireChat.getValue()) {
//                                        MessagesController.getInstance(UserConfig.selectedAccount).getTranslateController().setChatTranslateEnabled(false);
//                                        NotificationCenter.getInstance(UserConfig.selectedAccount).postNotificationName(NotificationCenter.updateSearchSettings);
//                                    }
//                                    needEntireChatReload = true;
//                                }
//
//                                updateItemsVisibility();
//                                checkProviderAvailability(context);
//
//                                if (needEntireChatReload) {
//                                    fragment.reloadUIAfterValueUpdate();
//                                }
//                            })
//                            .showIf(canSelectProvider)
//                            .title(getString(R.string.TranslatorProvider))
//                            .build()
//                    );
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.translatorFormality)
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(TranslatorFormality.DEFAULT.getValue())
                                            .setItemTitle(getString(R.string.TranslatorFormalityDefault)),
                                    new PopupChoiceDialogOption()
                                            .setId(TranslatorFormality.LOW.getValue())
                                            .setItemTitle(getString(R.string.TranslatorFormalityLow)),
                                    new PopupChoiceDialogOption()
                                            .setId(TranslatorFormality.HIGH.getValue())
                                            .setItemTitle(getString(R.string.TranslatorFormalityHigh))
                            ))
                            .showIf(canSelectFormality)
                            .title(getString(R.string.TranslatorFormality))
                            .build()
                    );
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.translatorKeepMarkdown)
                            .showIf(canSelectKeepMarkdown)
                            .title(getString(R.string.TranslatorKeepMarkdown))
                            .build()
                    );
                })
                .row(new CustomCellRow.CustomCellRowBuilder()
                        .layout(getGeminiSuggestion(context, fragment))
                        .build())
                .build();
    }

    private LinearLayout getGeminiSuggestion(Context context, PreferencesFragment fragment) {
        Theme.ResourcesProvider resourcesProvider = fragment.getResourceProvider();

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        SpoilersTextView textView = new SpoilersTextView(context);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        textView.setTypeface(AndroidUtilities.bold());
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueHeader, resourcesProvider));
        layout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, 21, 15, 21, 0));

        LinkSpanDrawable.LinksTextView detailTextView = new LinkSpanDrawable.LinksTextView(context, resourcesProvider);
        detailTextView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText, resourcesProvider));
        detailTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        detailTextView.setLinkTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkText, resourcesProvider));
        detailTextView.setHighlightColor(Theme.getColor(Theme.key_windowBackgroundWhiteLinkSelection, resourcesProvider));
        detailTextView.setMovementMethod(new AndroidUtilities.LinkMovementMethodMy());
        detailTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        layout.addView(detailTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 21, 14, 21, 0));

        TextView textViewButton = new TextView(context);
        textViewButton.setBackground(Theme.AdaptiveRipple.filledRectByKey(Theme.key_featuredStickers_addButton, 8));
        ScaleStateListAnimator.apply(textViewButton, 0.02f, 1.5f);
        textViewButton.setLines(1);
        textViewButton.setSingleLine(true);
        textViewButton.setGravity(Gravity.CENTER_HORIZONTAL);
        textViewButton.setEllipsize(TextUtils.TruncateAt.END);
        textViewButton.setGravity(Gravity.CENTER);
        textViewButton.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText, resourcesProvider));
        textViewButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textViewButton.setTypeface(AndroidUtilities.bold());
        layout.addView(textViewButton, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 44, 21, 16, 21, 15));

        textView.setText(getString(R.string.AiFeatures_Brief));
        detailTextView.setText(getString(R.string.AiFeatures_Desc));
        textViewButton.setText(getString(R.string.AiFeatures));

        final SpannableStringBuilder sb = new SpannableStringBuilder("G ");
        final ColoredImageSpan span = new ColoredImageSpan(R.drawable.cup_star_solar);
        sb.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.append(new SpannableStringBuilder(getString(R.string.AiFeatures)));
        textViewButton.setText(sb);

        textViewButton.setOnClickListener(v -> fragment.presentFragment(new PreferencesFragment(new OctoAiFeaturesUI(), "aiFeaturesTranslateMessages")));

        return layout;
    }


    private void updateItemsVisibility(ConfigProperty<Boolean> except) {
        TranslateController translateController = MessagesController.getInstance(UserConfig.selectedAccount).getTranslateController();
        boolean isSingleMessageTranslationEnabled = translateController.isContextTranslateEnabled();
        boolean isTranslateEntireChatEnabled = translateController.isFeatureAvailable();
        boolean canShowOtherOptions = isSingleMessageTranslationEnabled || isTranslateEntireChatEnabled;

        if (except != showButtonBool) {
            showButtonBool.updateValue(isSingleMessageTranslationEnabled);
            OctoLogging.d("updateItemsVisibility", "showButtonBool -> " + isSingleMessageTranslationEnabled);
        }

        if (except != translateEntireChat) {
            translateEntireChat.updateValue(isTranslateEntireChatEnabled);
            OctoLogging.d("updateItemsVisibility", "translateEntireChat -> " + isTranslateEntireChatEnabled);
        }

        if (except != canSelectDoNotTranslate) {
            canSelectDoNotTranslate.updateValue(canShowOtherOptions);
            OctoLogging.d("updateItemsVisibility", "canSelectDoNotTranslate -> " + canShowOtherOptions);
        }

        if (except != canSelectProvider) {
            boolean value = canShowOtherOptions && OctoConfig.INSTANCE.translatorMode.getValue() != TranslatorMode.EXTERNAL.getValue();
            canSelectProvider.updateValue(value);
            OctoLogging.d("updateItemsVisibility", "canSelectProvider -> " + value);
        }

        if (except != canSelectFormality) {
            boolean value = canShowOtherOptions && OctoConfig.INSTANCE.translatorProvider.getValue() == TranslatorProvider.DEEPL.getValue();
            canSelectFormality.updateValue(value);
            OctoLogging.d("updateItemsVisibility", "canSelectFormality -> " + value);
        }

        if (except != canSelectKeepMarkdown) {
            boolean value = canShowOtherOptions && OctoConfig.INSTANCE.translatorProvider.getValue() != TranslatorProvider.LINGO.getValue() && OctoConfig.INSTANCE.translatorMode.getValue() == TranslatorMode.INLINE.getValue();
            canSelectKeepMarkdown.updateValue(value);
            OctoLogging.d("updateItemsVisibility", "canSelectKeepMarkdown -> " + value);
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
            return String.format(getPluralString("Languages", langCodes.size()), langCodes.size());
        }
    }

    private String getTranslateDestinationStatus() {
        String currentDestination = MessagesController.getGlobalMainSettings().getString("translate_to_language", null);
        if (currentDestination == null || currentDestination.isEmpty()) {
            return getString(R.string.TranslatorDestinationFollow);
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
        warningBuilder.setTitle(getString(R.string.Warning));
        if (MessagesController.getInstance(UserConfig.selectedAccount).premiumFeaturesBlocked()) {
            warningBuilder.setPositiveButton(getString(R.string.OK), null);
        } else {
            warningBuilder.setPositiveButton(getString(R.string.MoreInfo), (dialog1, which1) -> {
                dialog1.dismiss();
                fragment.showDialog(new PremiumFeatureBottomSheet(fragment, PremiumPreviewFragment.PREMIUM_FEATURE_TRANSLATIONS, false));
            });
            warningBuilder.setNeutralButton(getString(R.string.Cancel), null);
        }
        warningBuilder.setMessage(getString(R.string.TranslatorEntireUnavailable));
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
            warningBuilder.setTitle(getString(R.string.Warning));
            warningBuilder.setPositiveButton(getString(R.string.OK), null);
            warningBuilder.setMessage(getString(R.string.TranslatorProviderUnsupportedDialogs));
            AlertDialog alertDialog = warningBuilder.create();
            alertDialog.show();
        }
    }
}
