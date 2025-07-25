/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui;

import static org.telegram.messenger.LocaleController.getPluralString;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.TranslateController;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
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
import java.util.Locale;

import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;
import it.octogram.android.TranslatorFormality;
import it.octogram.android.TranslatorMode;
import it.octogram.android.TranslatorProvider;
import it.octogram.android.app.OctoPreferences;
import it.octogram.android.app.PreferencesEntry;
import it.octogram.android.app.fragment.PreferencesFragment;
import it.octogram.android.app.rows.impl.CustomCellRow;
import it.octogram.android.app.rows.impl.FooterInformativeRow;
import it.octogram.android.app.rows.impl.ListRow;
import it.octogram.android.app.rows.impl.ShadowRow;
import it.octogram.android.app.rows.impl.SwitchRow;
import it.octogram.android.app.rows.impl.TextDetailRow;
import it.octogram.android.app.rows.impl.TextIconRow;
import it.octogram.android.utils.OctoLogging;
import it.octogram.android.utils.appearance.PopupChoiceDialogOption;
import it.octogram.android.utils.deeplink.DeepLinkDef;
import it.octogram.android.utils.translator.MainTranslationsHandler;
import it.octogram.android.utils.translator.localhelper.OnDeviceHelper;


public class OctoChatsTranslatorUI implements PreferencesEntry {
    private PreferencesFragment fragment;
    private Context context;

    private final ConfigProperty<Boolean> showButtonBool = new ConfigProperty<>("showTranslateButton", false);
    private final ConfigProperty<Boolean> translateEntireChat = new ConfigProperty<>("canTranslateEntireChat", false);
    private final ConfigProperty<Boolean> canShowUninstallExtension = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> canSelectDoNotTranslate = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> canSelectProvider = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> canSelectFormality = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> canSelectKeepMarkdown = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> canSelectOnDeviceModels = new ConfigProperty<>(null, false);

    @NonNull
    @Override
    public OctoPreferences getPreferences(@NonNull PreferencesFragment fragment, @NonNull Context context) {
        TranslateController controller = MessagesController.getInstance(UserConfig.selectedAccount).getTranslateController();
        this.fragment = fragment;
        this.context = context;

        updateItemsVisibility();

        List<PopupChoiceDialogOption> translatorModeOptions = new ArrayList<>();
        translatorModeOptions.add(new PopupChoiceDialogOption()
                .setId(TranslatorMode.DEFAULT.getValue())
                .setItemTitle(getString(R.string.TranslatorModeDefault)));
        translatorModeOptions.add(new PopupChoiceDialogOption()
                .setId(TranslatorMode.INLINE.getValue())
                .setItemTitle(getString(R.string.TranslatorModeInline)));

        if (MainTranslationsHandler.canUseExternalApp()) {
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
                                showUnavailableFeatureTgPremium();
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
                .row(new CustomCellRow.CustomCellRowBuilder()
                        .layout(getExtensionUninstallSuggestion())
                        .showIf(canShowUninstallExtension)
                        .build())
                .row(new ShadowRow(canShowUninstallExtension))
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
                            .onClick(() -> fragment.presentFragment(new OctoChatsTranslatorDestinationUI()))
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
                                OctoChatsTranslatorProviderUI ui = new OctoChatsTranslatorProviderUI();
                                fragment.presentFragment(new PreferencesFragment(ui));
                            })
                            .propertySelectionTag("translatorProvider")
                            .value(MainTranslationsHandler.getProviderName())
                            .setDynamicDataUpdate(new TextIconRow.OnDynamicDataUpdate() {
                                @Override
                                public String getTitle() {
                                    return getString(R.string.TranslatorProvider);
                                }

                                @Override
                                public String getValue() {
                                    return MainTranslationsHandler.getProviderName();
                                }
                            })
                            .showIf(canSelectProvider)
                            .title(getString(R.string.TranslatorProvider))
                            .build()
                    );
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
                .row(new TextDetailRow.TextDetailRowBuilder()
                    .onClick(() -> {
                        if (!OnDeviceHelper.checkVersionCode(context, OnDeviceHelper.MIN_ONDEVICE_VERSION_CODE)) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle(getString(R.string.Warning));
                            builder.setMessage(AndroidUtilities.replaceTags(String.format(Locale.US, getString(R.string.OnDeviceTranslationModels_UnsupportedVersion), OnDeviceHelper.MIN_ONDEVICE_VERSION, OnDeviceHelper.MIN_ONDEVICE_VERSION_CODE)));
                            builder.setPositiveButton(getString(R.string.OK), null);
                            fragment.showDialog(builder.create());
                            return;
                        }
                        fragment.presentFragment(new OctoChatsTranslatorModelsUI());
                    })
                    .icon(R.drawable.menu_feature_transfer)
                    .title(getString(R.string.OnDeviceTranslationModels))
                    .description(getString(R.string.OnDeviceTranslationModels_Manage))
                    .showIf(canSelectOnDeviceModels)
                    .build())
                .build();
    }


    private boolean isFirstDraw = true;

    @Override
    public void onBecomeFullyVisible() {
        if (isFirstDraw) {
            isFirstDraw = false;
            return;
        }
        updateItemsVisibility();
        fragment.reloadUIAfterValueUpdate();
    }

    private LinearLayout getExtensionUninstallSuggestion() {
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

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        layout.addView(linearLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 44, 21, 16, 21, 15));

        for (int a = 0; a < 2; a++) {
            TextView textView2 = new TextView(context);
            textView2.setBackground(Theme.AdaptiveRipple.filledRectByKey(Theme.key_featuredStickers_addButton, 8));
            ScaleStateListAnimator.apply(textView2, 0.02f, 1.5f);
            textView2.setLines(1);
            textView2.setSingleLine(true);
            textView2.setGravity(Gravity.CENTER_HORIZONTAL);
            textView2.setEllipsize(TextUtils.TruncateAt.END);
            textView2.setGravity(Gravity.CENTER);
            textView2.setTextColor(Theme.getColor(Theme.key_featuredStickers_buttonText, resourcesProvider));
            textView2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            textView2.setTypeface(AndroidUtilities.bold());
            textView2.setText(getString(a == 0 ? R.string.Settings : R.string.Hide));
            linearLayout.addView(textView2, LayoutHelper.createLinear(0, 44, 0.5f, a == 0 ? 0 : 4, 0, a == 0 ? 4 : 0, 0));
            int finalA = a;
            textView2.setOnClickListener(v -> {
                if (finalA == 0) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:it.octogram.ondevice"));
                    context.startActivity(intent);
                } else {
                    OctoConfig.INSTANCE.translatorUninstallExtensionHideTime.updateValue(System.currentTimeMillis());
                    updateItemsVisibility();
                    fragment.reloadUIAfterValueUpdate();
                }
            });
        }

        textView.setText(getString(R.string.TranslatorExtensionSpaceFree));
        detailTextView.setText(getString(R.string.TranslatorExtensionSpaceFree_Desc));

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

        if (except != canShowUninstallExtension) {
            boolean value = OctoConfig.INSTANCE.translatorProvider.getValue() != TranslatorProvider.DEVICE_TRANSLATION.getValue() && OnDeviceHelper.isAvailable(context);
            if (value) {
                long lastTimeHideAlert = OctoConfig.INSTANCE.translatorUninstallExtensionHideTime.getValue();
                if (lastTimeHideAlert > 0 && System.currentTimeMillis() - lastTimeHideAlert < 24 * 60 * 60 * 1000) {
                    value = false;
                }
            }
            canShowUninstallExtension.updateValue(value);
            OctoLogging.d("updateItemsVisibility", "canShowUninstallExtension -> " + value);
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
            boolean value = canShowOtherOptions && OctoConfig.INSTANCE.translatorProvider.getValue() != TranslatorProvider.LINGO.getValue() && OctoConfig.INSTANCE.translatorProvider.getValue() != TranslatorProvider.DEVICE_TRANSLATION.getValue() && OctoConfig.INSTANCE.translatorMode.getValue() != TranslatorMode.EXTERNAL.getValue();
            canSelectKeepMarkdown.updateValue(value);
            OctoLogging.d("updateItemsVisibility", "canSelectKeepMarkdown -> " + value);
        }

        if (except != canSelectOnDeviceModels) {
            boolean value = canShowOtherOptions && OctoConfig.INSTANCE.translatorProvider.getValue() == TranslatorProvider.DEVICE_TRANSLATION.getValue() && OctoConfig.INSTANCE.translatorMode.getValue() != TranslatorMode.EXTERNAL.getValue();
            canSelectOnDeviceModels.updateValue(value);
            OctoLogging.d("updateItemsVisibility", "canSelectOnDeviceModels -> " + value);
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

    private void showUnavailableFeatureTgPremium() {
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
}
