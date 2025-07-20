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

import org.telegram.messenger.R;

import java.util.HashMap;
import java.util.Objects;

import it.octogram.android.AiProvidersDetails;
import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;
import it.octogram.android.app.OctoPreferences;
import it.octogram.android.app.PreferencesEntry;
import it.octogram.android.app.fragment.PreferencesFragment;
import it.octogram.android.app.rows.impl.FooterInformativeRow;
import it.octogram.android.app.rows.impl.SwitchRow;
import it.octogram.android.app.rows.impl.TextIconRow;
import it.octogram.android.utils.ai.MainAiHelper;
import it.octogram.android.utils.ai.ui.AiConfigBottomSheet;
import it.octogram.android.utils.deeplink.DeepLinkDef;

public class OctoChatsAiProvidersUI implements PreferencesEntry {
    private final ConfigProperty<Boolean> hasZeroAddedProviders = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> canAddMoreProviders = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> hasAddedProviders = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> hasAvailableProviders = new ConfigProperty<>(null, false);
    private final HashMap<Integer, ConfigProperty<Boolean>> addedCategoryVisibility = new HashMap<>();
    private final HashMap<Integer, ConfigProperty<Boolean>> availableCategoryVisibility = new HashMap<>();
    private PreferencesFragment fragment;
    private Context context;

    @NonNull
    @Override
    public OctoPreferences getPreferences(@NonNull PreferencesFragment fragment, @NonNull Context context) {
        this.fragment = fragment;
        this.context = context;

        updateState();
        addedCategoryVisibility.clear();
        availableCategoryVisibility.clear();

        return OctoPreferences.builder(getString(R.string.TranslatorProvider))
                .deepLink(DeepLinkDef.AI_FEATURES_PROVIDERS)
                .sticker(context, OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME, StickerUi.MAIN, true, getString(R.string.AiFeatures_AccessVia_Desc))
                .category(getString(R.string.AiFeatures_AccessVia_Added), hasAddedProviders, category -> listProviders(category, true))
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(getString(R.string.AiFeatures_AccessVia_SelectableOne))
                        .showIf(hasAddedProviders)
                        .build()
                )
                .category(getString(R.string.AiFeatures_AccessVia_Available), hasAvailableProviders, category -> listProviders(category, false))
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(getString(R.string.AiFeatures_AccessVia_Empty))
                        .showIf(hasZeroAddedProviders)
                        .build()
                )
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(getString(R.string.AiFeatures_AccessVia_AddMore))
                        .showIf(canAddMoreProviders)
                        .build()
                )
                .build();
    }

    private void listProviders(OctoPreferences.OctoPreferencesBuilder category, boolean isAddedCategory) {
        for (AiProvidersDetails provider : AiProvidersDetails.getEntries()) {
            boolean isAdded = MainAiHelper.isProviderAvailable(provider);
            if (isAddedCategory) {
                ConfigProperty<Boolean> property = new ConfigProperty<>(null, isAdded);
                addedCategoryVisibility.put(provider.getId(), property);
                category.row(new SwitchRow.SwitchRowBuilder()
                        .onClick(() -> handleSwitch(provider.getStatusProperty()))
                        .preferenceValue(provider.getStatusProperty())
                        .title(provider.getUseThisProviderString())
                        .showIf(property)
                        .build());
            } else {
                ConfigProperty<Boolean> property = new ConfigProperty<>(null, !isAdded);
                availableCategoryVisibility.put(provider.getId(), property);

                category.row(new TextIconRow.TextIconRowBuilder()
                        .isBlue(true)
                        .onClick(() -> handleSwitch(provider.getStatusProperty()))
                        .propertySelectionTag(provider.getStatusProperty().getKey())
                        .icon(R.drawable.msg_add)
                        .showIf(property)
                        .title(provider.getUseThisProviderString())
                        .build());
            }
        }
    }

    private void updateState() {
        int enabledProviders = MainAiHelper.getEnabledProvidersCount();
        hasZeroAddedProviders.updateValue(enabledProviders == 0);
        hasAddedProviders.updateValue(enabledProviders > 0);
        hasAvailableProviders.updateValue(enabledProviders != AiProvidersDetails.getEntries().size());
        canAddMoreProviders.updateValue(hasAddedProviders.getValue() && hasAvailableProviders.getValue());

        if (!addedCategoryVisibility.isEmpty() || !availableCategoryVisibility.isEmpty()) {
            for (AiProvidersDetails provider : AiProvidersDetails.getEntries()) {
                boolean isAdded = MainAiHelper.isProviderAvailable(provider);
                if (addedCategoryVisibility.containsKey(provider.getId())) {
                    Objects.requireNonNull(addedCategoryVisibility.get(provider.getId())).updateValue(isAdded);
                }
                if (availableCategoryVisibility.containsKey(provider.getId())) {
                    Objects.requireNonNull(availableCategoryVisibility.get(provider.getId())).updateValue(!isAdded);
                }
            }
        }
    }

    private boolean handleSwitch(ConfigProperty<Boolean> currentProperty) {
        AiProvidersDetails provider = AiProvidersDetails.Companion.fromMainProperty(currentProperty);
        if (provider != null) {
            openPropertyConfig(provider);
        }

        return false;
    }

    private void openPropertyConfig(AiProvidersDetails property) {
        new AiConfigBottomSheet(context, fragment, property, new AiConfigBottomSheet.AiConfigInterface() {
            @Override
            public void onStateUpdated() {
                updateState();
                fragment.reloadUIAfterValueUpdate();
            }

            @Override
            public boolean canShowSuccessBulletin() {
                return MainAiHelper.hasAvailableProviders();
            }
        }).show();
    }
}
