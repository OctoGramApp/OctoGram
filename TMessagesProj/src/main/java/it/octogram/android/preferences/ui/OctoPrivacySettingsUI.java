/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui;

import static org.telegram.messenger.LocaleController.formatPluralString;
import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.NonNull;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.LaunchActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import it.octogram.android.ConfigProperty;
import it.octogram.android.ExpandableRowsIds;
import it.octogram.android.NewFeaturesBadgeId;
import it.octogram.android.OctoConfig;
import it.octogram.android.PhoneNumberAlternative;
import it.octogram.android.StickerUi;
import it.octogram.android.deeplink.DeepLinkDef;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferenceType;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.fragment.ActionBarOverride;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.rows.impl.ExpandableRows;
import it.octogram.android.preferences.rows.impl.FooterInformativeRow;
import it.octogram.android.preferences.rows.impl.HeaderRow;
import it.octogram.android.preferences.rows.impl.ListRow;
import it.octogram.android.preferences.rows.impl.SliderChooseRow;
import it.octogram.android.preferences.rows.impl.SwitchRow;
import it.octogram.android.preferences.rows.impl.TextIconRow;
import it.octogram.android.utils.OctoUtils;
import it.octogram.android.utils.account.FingerprintUtils;
import it.octogram.android.utils.appearance.PopupChoiceDialogOption;
import it.octogram.android.utils.config.ExpandableRowsOption;


public class OctoPrivacySettingsUI implements PreferencesEntry {
    private Context context;
    private PreferencesFragment fragment;
    private boolean isFirstFingerprintAsk = true;
    private boolean fromLockedChatsSettingsUI = false;
    private final ConfigProperty<Boolean> canShowBiometricOptions = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> canShowBiometricAskAfter = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> canShowLockedAccountsOptions = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> canShowPhoneNumberAlternative = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> canShowDebugProperties = new ConfigProperty<>(null, false);
    private final ConfigProperty<Integer> biometricAskEveryTemp = new ConfigProperty<>(OctoConfig.INSTANCE.biometricAskEvery.getKey(), 0);
    private final HashMap<Integer, ConfigProperty<Boolean>> accountAssoc = new HashMap<>();

    @NonNull
    @Override
    public OctoPreferences getPreferences(@NonNull PreferencesFragment fragment, @NonNull Context context) {
        this.context = context;
        this.fragment = fragment;
        accountAssoc.clear();

        updateConfig();
        biometricAskEveryTemp.updateValue(OctoConfig.INSTANCE.biometricAskEvery.getValue());
        boolean canShowLockArchive = canShowBiometricOptions.getValue() && (BuildConfig.DEBUG_PRIVATE_VERSION || BuildVars.isBetaApp());

        ArrayList<ExpandableRowsOption> accountsList = new ArrayList<>();
        for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
            TLRPC.User u = AccountInstance.getInstance(a).getUserConfig().getCurrentUser();
            if (u != null) {
                ConfigProperty<Boolean> currentInstance = new ConfigProperty<>(null, FingerprintUtils.isAccountLocked(u.id));
                accountAssoc.put(a, currentInstance);
                int finalA = a;
                accountsList.add(new ExpandableRowsOption()
                        .accountId(a)
                        .property(currentInstance)
                        .onPostUpdate(this::updateConfig)
                        .onClick(() -> handleAccountLock(finalA, u.id)
                ));
            }
        }

        return OctoPreferences.builder(getString(R.string.PrivacySettings))
                .deepLink(DeepLinkDef.PRIVACY)
                .sticker(context, OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME, StickerUi.PRIVACY, true, getString(R.string.OctoPrivacySettingsHeader))
                .category(getString(R.string.ActionsSettingsBiometric), canShowBiometricOptions, category -> {
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> {
                                if (fromLockedChatsSettingsUI) {
                                    fragment.finishFragment();
                                } else {
                                    fragment.presentFragment(new PreferencesFragment(new OctoPrivacyLockedChatsSettingsUI()));
                                }
                            })
                            .propertySelectionTag("lockedChats")
                            .icon(R.drawable.msg_viewchats)
                            .isNew(NewFeaturesBadgeId.PRIVACY_LOCKED_CHATS.getId())
                            .setDynamicDataUpdate(new TextIconRow.OnDynamicDataUpdate() {
                                @Override
                                public String getTitle() {
                                    return getString(R.string.LockedChats);
                                }

                                @Override
                                public String getValue() {
                                    int lockedChats = FingerprintUtils.getLockedChatsCount();
                                    if (lockedChats == 0) {
                                        return getString(R.string.CheckPhoneNumberNo);
                                    }

                                    return ""+lockedChats;
                                }
                            })
                            .title(getString(R.string.LockedChats))
                            .description(""+FingerprintUtils.getLockedChats().size())
                            .showIf(canShowBiometricOptions)
                            .build());
                    category.row(new ExpandableRows.ExpandableRowsBuilder()
                            .setId(ExpandableRowsIds.LOCKED_ELEMENTS.getId())
                            .setIcon(R.drawable.edit_passcode)
                            .setMainTitle(getString(R.string.LockedActions))
                            .hideMainSwitch(true)
                            .addRow(!canShowLockArchive ? null : new ExpandableRowsOption()
                                    .optionTitle(getString(R.string.BiometricSettingsOpenArchive))
                                    .property(OctoConfig.INSTANCE.biometricOpenArchive)
                                    .onClick(() -> checkAvailability(OctoConfig.INSTANCE.biometricOpenArchive))
                                    .onPostUpdate(this::updateConfig)
                            )
                            .addRow(new ExpandableRowsOption()
                                    .optionTitle(getString(R.string.BiometricSettingsOpenCallsLog))
                                    .property(OctoConfig.INSTANCE.biometricOpenCallsLog)
                                    .onClick(() -> checkAvailability(OctoConfig.INSTANCE.biometricOpenCallsLog))
                                    .onPostUpdate(this::updateConfig)
                            )
                            .addRow(new ExpandableRowsOption()
                                    .optionTitle(getString(R.string.BiometricSettingsOpenSecretChats))
                                    .property(OctoConfig.INSTANCE.biometricOpenSecretChats)
                                    .onClick(() -> checkAvailability(OctoConfig.INSTANCE.biometricOpenSecretChats))
                                    .onPostUpdate(this::updateConfig)
                            )
                            .addRow(new ExpandableRowsOption()
                                    .optionTitle(getString(R.string.BiometricSettingsOpenSettings))
                                    .property(OctoConfig.INSTANCE.biometricOpenSettings)
                                    .onClick(() -> checkAvailability(OctoConfig.INSTANCE.biometricOpenSettings))
                                    .onPostUpdate(this::updateConfig)
                            )
                            .showIf(canShowBiometricOptions)
                            .build()
                    );
                    category.row(new ExpandableRows.ExpandableRowsBuilder()
                            .setId(ExpandableRowsIds.LOCKED_ACCOUNTS.getId())
                            .setIcon(R.drawable.msg_openprofile)
                            .setMainTitle(getString(R.string.LockedAccounts))
                            .hideMainSwitch(true)
                            .isLocked(true)
                            .addRow(accountsList)
                            .showIf(canShowBiometricOptions)
                            .build()
                    );
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkAvailability(OctoConfig.INSTANCE.hideHiddenAccounts))
                            .preferenceValue(OctoConfig.INSTANCE.hideHiddenAccounts)
                            .title(getString(R.string.LockedAccounts_HideFromList))
                            .description(getString(R.string.LockedAccounts_HideFromList_Desc))
                            .showIf(canShowLockedAccountsOptions)
                            .build());
                })
                .category(getString(R.string.BiometricSettings), canShowBiometricOptions, category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkAvailability(OctoConfig.INSTANCE.allowUsingDevicePIN))
                            .preferenceValue(OctoConfig.INSTANCE.allowUsingDevicePIN)
                            .title(getString(R.string.BiometricSettingsAllowDevicePIN))
                            .showIf(canShowBiometricOptions)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkAvailability(OctoConfig.INSTANCE.allowUsingFaceUnlock))
                            .preferenceValue(OctoConfig.INSTANCE.allowUsingFaceUnlock)
                            .title(getString(R.string.BiometricSettingsAllowFaceUnlock))
                            .showIf(canShowBiometricOptions)
                            .build());
                    category.row(new HeaderRow(getString(R.string.BiometricAskEvery), canShowBiometricAskAfter).headerStyle(false));
                    category.row(new SliderChooseRow.SliderChooseRowBuilder()
                            .options(new ArrayList<>() {{
                                add(new Pair<>(0, getString(R.string.BiometricAskEvery_Always)));
                                add(new Pair<>(10, formatString(R.string.SlowmodeSeconds, 10)));
                                add(new Pair<>(30, formatString(R.string.SlowmodeSeconds, 30)));
                                add(new Pair<>(60, formatString(R.string.SlowmodeMinutes, 1)));
                                add(new Pair<>(120, formatString(R.string.SlowmodeMinutes, 2)));
                                add(new Pair<>(300, formatString(R.string.SlowmodeMinutes, 5)));
                            }})
                            .onUpdate(() -> fragment.notifyItemChanged(PreferenceType.FOOTER_INFORMATIVE.getAdapterType()))
                            .onTouchEnd(this::handleSlideBarUpdate)
                            .preferenceValue(biometricAskEveryTemp)
                            .showIf(canShowBiometricAskAfter)
                            .build());
                })
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .setDynamicDataUpdate(this::composeBiometricCaptionForSeconds)
                        .title(composeBiometricCaptionForSeconds())
                        .showIf(canShowBiometricAskAfter)
                        .build())
                .category(getString(R.string.Warnings), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.promptBeforeCalling)
                            .title(getString(R.string.PromptBeforeCalling))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.warningBeforeDeletingChatHistory)
                            .title(getString(R.string.PromptBeforeDeletingChatHistory))
                            .build());
                })
                .category(getString(R.string.PhoneNumberPrivacy), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> canShowPhoneNumberAlternative.setValue(OctoConfig.INSTANCE.hidePhoneNumber.getValue() || OctoConfig.INSTANCE.hideOtherPhoneNumber.getValue()))
                            .preferenceValue(OctoConfig.INSTANCE.hidePhoneNumber)
                            .title(getString(R.string.HidePhoneNumber))
                            .description(getString(R.string.HidePhoneNumber_Desc))
                            .postNotificationName(NotificationCenter.reloadInterface, NotificationCenter.mainUserInfoChanged)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> canShowPhoneNumberAlternative.setValue(OctoConfig.INSTANCE.hidePhoneNumber.getValue() || OctoConfig.INSTANCE.hideOtherPhoneNumber.getValue()))
                            .preferenceValue(OctoConfig.INSTANCE.hideOtherPhoneNumber)
                            .title(getString(R.string.HideOtherPhoneNumber))
                            .description(getString(R.string.HideOtherPhoneNumber_Desc))
                            .postNotificationName(NotificationCenter.reloadInterface, NotificationCenter.mainUserInfoChanged)
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.phoneNumberAlternative)
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(PhoneNumberAlternative.SHOW_HIDDEN_NUMBER_STRING.getValue())
                                            .setItemTitle(formatString(R.string.ShowHiddenNumber, getString(R.string.MobileHidden))),
                                    new PopupChoiceDialogOption()
                                            .setId(PhoneNumberAlternative.SHOW_FAKE_PHONE_NUMBER.getValue())
                                            .setItemTitle(getString(R.string.ShowFakePhoneNumber))
                                            .setItemDescription(formatString(R.string.ShowFakePhoneNumber_Desc, OctoUtils.hiddenPhoneNumberSample(UserConfig.getInstance(UserConfig.selectedAccount).getCurrentUser()))),
                                    new PopupChoiceDialogOption()
                                            .setId(PhoneNumberAlternative.SHOW_USERNAME.getValue())
                                            .setItemTitle(getString(R.string.ShowUsernameAsPhoneNumber))
                                            .setItemDescription(getString(R.string.ShowUsernameAsPhoneNumber_Desc))
                            ))
                            .showIf(canShowPhoneNumberAlternative)
                            .title(getString(R.string.InsteadPhoneNumber))
                            .build());
                })
                .category("Debug", canShowDebugProperties, category -> category.row(new SwitchRow.SwitchRowBuilder()
                        .preferenceValue(OctoConfig.INSTANCE.advancedBiometricUnlock)
                        .title("Advanced unlock")
                        .showIf(canShowDebugProperties)
                        .build()))
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(getString(R.string.BiometricUnavailable))
                        .showIf(canShowBiometricOptions, true)
                        .build())
                .build();
    }

    public void setFromLockedChatsSettingsUI(boolean fromLockedChatsSettingsUI) {
        this.fromLockedChatsSettingsUI = fromLockedChatsSettingsUI;
    }

    private String composeBiometricCaptionForSeconds() {
        return formatString(R.string.BiometricAskEvery_Desc, formatSeconds(biometricAskEveryTemp.getValue()));
    }

    private String formatSeconds(int seconds) {
        if (seconds < 60) {
            return formatPluralString("Seconds", seconds);
        } else if (seconds < 60 * 60) {
            return formatPluralString("Minutes", seconds / 60);
        } else {
            return formatPluralString("Hours", seconds / 60 / 60);
        }
    }

    private void updateConfig() {
        updateConfigsWithResult();
    }
    
    private boolean updateConfigsWithResult() {
        boolean hasFingerprint = FingerprintUtils.hasFingerprintCached();
        boolean hasEnabledOptions = OctoConfig.INSTANCE.biometricOpenArchive.getValue() || OctoConfig.INSTANCE.biometricOpenCallsLog.getValue() || OctoConfig.INSTANCE.biometricOpenSecretChats.getValue() || OctoConfig.INSTANCE.biometricOpenSettings.getValue() || FingerprintUtils.hasLockedAccounts();

        boolean hasChanged = canShowBiometricOptions.getValue() != hasFingerprint;
        if (!hasChanged) {
            hasChanged = canShowBiometricAskAfter.getValue() != (hasFingerprint&&hasEnabledOptions);
        }
        if (!hasChanged) {
            hasChanged = canShowLockedAccountsOptions.getValue() != (hasFingerprint && FingerprintUtils.hasLockedAccounts());
        }
        if (!hasChanged) {
            hasChanged = canShowPhoneNumberAlternative.getValue() != (OctoConfig.INSTANCE.hidePhoneNumber.getValue() || OctoConfig.INSTANCE.hideOtherPhoneNumber.getValue());
        }
        if (!hasChanged) {
            hasChanged = canShowDebugProperties.getValue() != (hasFingerprint && (BuildConfig.DEBUG_PRIVATE_VERSION || BuildVars.isBetaApp()));
        }

        canShowBiometricOptions.updateValue(hasFingerprint);
        canShowBiometricAskAfter.updateValue(hasFingerprint && hasEnabledOptions);
        canShowLockedAccountsOptions.updateValue(hasFingerprint && FingerprintUtils.hasLockedAccounts());
        canShowPhoneNumberAlternative.updateValue(OctoConfig.INSTANCE.hidePhoneNumber.getValue() || OctoConfig.INSTANCE.hideOtherPhoneNumber.getValue());
        canShowDebugProperties.updateValue(hasFingerprint && (BuildConfig.DEBUG_PRIVATE_VERSION || BuildVars.isBetaApp()));

        return hasChanged;
    }

    private boolean checkAvailability(ConfigProperty<Boolean> option) {
        if (!option.getValue() || !FingerprintUtils.hasFingerprintCached()) {
            return true;
        } else {
            if (fragment.hasUnlockedWithAuth()) {
                isFirstFingerprintAsk = false;
            }

            FingerprintUtils.checkFingerprint(context, FingerprintUtils.FingerprintAction.EDIT_SETTINGS, isFirstFingerprintAsk, () -> {
                isFirstFingerprintAsk = false;
                option.updateValue(false);

                if (updateConfigsWithResult()) {
                    fragment.reloadUIAfterValueUpdate();
                } else {
                    fragment.notifyItemChanged(PreferenceType.EXPANDABLE_ROWS.getAdapterType(), PreferenceType.EXPANDABLE_ROWS_CHILD.getAdapterType(), PreferenceType.SWITCH.getAdapterType());
                }
            });
            return false;
        }
    }

    private void handleSlideBarUpdate() {
        if (OctoConfig.INSTANCE.biometricAskEvery.getValue().equals(biometricAskEveryTemp.getValue())) {
            return;
        }

        if (!FingerprintUtils.hasFingerprintCached()) {
            OctoConfig.INSTANCE.biometricAskEvery.updateValue(biometricAskEveryTemp.getValue());
            return;
        }

        FingerprintUtils.checkFingerprint(context, FingerprintUtils.FingerprintAction.EDIT_SETTINGS, true, new FingerprintUtils.FingerprintResult() {
            @Override
            public void onSuccess() {
                isFirstFingerprintAsk = false;
                OctoConfig.INSTANCE.biometricAskEvery.updateValue(biometricAskEveryTemp.getValue());
            }

            @Override
            public void onFailed() {
                biometricAskEveryTemp.updateValue(OctoConfig.INSTANCE.biometricAskEvery.getValue());
                fragment.notifyItemChanged(PreferenceType.FOOTER_INFORMATIVE.getAdapterType(), PreferenceType.SLIDER_CHOOSE.getAdapterType());
            }
        });
    }

    private boolean handleAccountLock(int accountId, long userId) {
        if (!FingerprintUtils.isAccountLocked(userId)) {
            FingerprintUtils.lockAccount(userId, true);

            if (userId == UserConfig.getInstance(UserConfig.selectedAccount).clientUserId && LaunchActivity.instance.getActionBarLayout() instanceof ActionBarOverride override) {
                override.unlock(userId);
            }

            return true;
        } else {
            if (fragment.hasUnlockedWithAuth()) {
                isFirstFingerprintAsk = false;
            }

            FingerprintUtils.checkFingerprint(context, FingerprintUtils.FingerprintAction.EDIT_SETTINGS, isFirstFingerprintAsk, () -> {
                isFirstFingerprintAsk = false;

                if (accountAssoc.get(accountId) == null) {
                    return;
                }

                FingerprintUtils.lockAccount(userId, false);
                Objects.requireNonNull(accountAssoc.get(accountId)).updateValue(false);

                if (updateConfigsWithResult()) {
                    fragment.reloadUIAfterValueUpdate();
                } else {
                    fragment.notifyItemChanged(PreferenceType.EXPANDABLE_ROWS.getAdapterType(), PreferenceType.EXPANDABLE_ROWS_CHILD.getAdapterType());
                }
            });
            return false;
        }
    }
}