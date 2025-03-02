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
import android.util.Pair;
import android.view.Gravity;

import androidx.annotation.NonNull;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.UsersSelectActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import it.octogram.android.ConfigProperty;
import it.octogram.android.ExpandableRowsIds;
import it.octogram.android.OctoConfig;
import it.octogram.android.PhoneNumberAlternative;
import it.octogram.android.StickerUi;
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
import it.octogram.android.preferences.ui.components.LockedChatsHelp;
import it.octogram.android.utils.ExpandableRowsOption;
import it.octogram.android.utils.FingerprintUtils;
import it.octogram.android.utils.PopupChoiceDialogOption;

public class OctoPrivacySettingsUI implements PreferencesEntry {
    private Context context;
    private PreferencesFragment fragment;
    private final ConfigProperty<Boolean> canShowBiometricAskAfter = new ConfigProperty<>(null, false);
    private final ConfigProperty<Integer> biometricAskEveryTemp = new ConfigProperty<>(OctoConfig.INSTANCE.biometricAskEvery.getKey(), 0);
    private final HashMap<Integer, ConfigProperty<Boolean>> accountAssoc = new HashMap<>();

    @Override
    public OctoPreferences getPreferences(PreferencesFragment fragment, Context context) {
        this.context = context;
        this.fragment = fragment;
        accountAssoc.clear();

        canShowBiometricAskAfter.updateValue(canShowBiometricAskAfter());
        biometricAskEveryTemp.updateValue(OctoConfig.INSTANCE.biometricAskEvery.getValue());
        ConfigProperty<Boolean> canShowBiometricOptions = new ConfigProperty<>(null, FingerprintUtils.hasFingerprint());
        ConfigProperty<Boolean> canShowPhoneNumberAlternative = new ConfigProperty<>(null, OctoConfig.INSTANCE.hidePhoneNumber.getValue() || OctoConfig.INSTANCE.hideOtherPhoneNumber.getValue());
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
                        .onPostUpdate(() -> canShowBiometricAskAfter.updateValue(canShowBiometricAskAfter()))
                        .onClick(() -> handleAccountLock(finalA, u.id)
                ));
            }
        }

        return OctoPreferences.builder(LocaleController.getString(R.string.PrivacySettings))
                .deepLink("tg://privacy")
                .sticker(context, OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME, StickerUi.PRIVACY, true, LocaleController.getString(R.string.OctoPrivacySettingsHeader))
                .category(LocaleController.getString(R.string.ActionsSettingsBiometric), canShowBiometricOptions, category -> {
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> showLockedHelp(context, fragment.getResourceProvider(), fragment))
                            .propertySelectionTag("lockedChats")
                            .icon(R.drawable.msg_viewchats)
                            .setDynamicDataUpdate(new TextIconRow.OnDynamicDataUpdate() {
                                @Override
                                public String getTitle() {
                                    return getString(R.string.LockedChats);
                                }

                                @Override
                                public String getValue() {
                                    int lockedChats = FingerprintUtils.getLockedChatsCount();
                                    if (lockedChats == 0) {
                                        return LocaleController.getString(R.string.CheckPhoneNumberNo);
                                    }

                                    return ""+lockedChats;
                                }
                            })
                            .title(getString(R.string.LockedChats))
                            .description(""+FingerprintUtils.getLockedChats().size())
                            .showIf(canShowBiometricOptions)
                            .build());
                    category.row(new ExpandableRows.ExpandableRowsBuilder()
                            .setId(ExpandableRowsIds.LOCKED_ACCOUNTS.getId())
                            .setIcon(R.drawable.msg_openprofile)
                            .setMainTitle(LocaleController.getString(R.string.LockedAccounts))
                            .hideMainSwitch(true)
                            .addRow(accountsList)
                            .showIf(canShowBiometricOptions)
                            .build()
                    );
                    category.row(new ExpandableRows.ExpandableRowsBuilder()
                            .setId(ExpandableRowsIds.LOCKED_ELEMENTS.getId())
                            .setIcon(R.drawable.edit_passcode)
                            .setMainTitle(LocaleController.getString(R.string.LockedActions))
                            .hideMainSwitch(true)
                            .addRow(!canShowLockArchive ? null : new ExpandableRowsOption()
                                    .optionTitle(getString(R.string.BiometricSettingsOpenArchive))
                                    .property(OctoConfig.INSTANCE.biometricOpenArchive)
                                    .onClick(() -> checkAvailability(OctoConfig.INSTANCE.biometricOpenArchive))
                                    .onPostUpdate(() -> canShowBiometricAskAfter.updateValue(canShowBiometricAskAfter()))
                            )
                            .addRow(new ExpandableRowsOption()
                                    .optionTitle(getString(R.string.BiometricSettingsOpenCallsLog))
                                    .property(OctoConfig.INSTANCE.biometricOpenCallsLog)
                                    .onClick(() -> checkAvailability(OctoConfig.INSTANCE.biometricOpenCallsLog))
                                    .onPostUpdate(() -> canShowBiometricAskAfter.updateValue(canShowBiometricAskAfter()))
                            )
                            .addRow(new ExpandableRowsOption()
                                    .optionTitle(getString(R.string.BiometricSettingsOpenSavedMessages))
                                    .property(OctoConfig.INSTANCE.biometricOpenSavedMessages)
                                    .onClick(() -> checkAvailability(OctoConfig.INSTANCE.biometricOpenSavedMessages))
                                    .onPostUpdate(() -> canShowBiometricAskAfter.updateValue(canShowBiometricAskAfter()))
                            )
                            .addRow(new ExpandableRowsOption()
                                    .optionTitle(getString(R.string.BiometricSettingsOpenSecretChats))
                                    .property(OctoConfig.INSTANCE.biometricOpenSecretChats)
                                    .onClick(() -> checkAvailability(OctoConfig.INSTANCE.biometricOpenSecretChats))
                                    .onPostUpdate(() -> canShowBiometricAskAfter.updateValue(canShowBiometricAskAfter()))
                            )
                            .showIf(canShowBiometricOptions)
                            .build()
                    );
                })
                .category(LocaleController.getString(R.string.BiometricSettings), canShowBiometricOptions, category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkAvailability(OctoConfig.INSTANCE.allowUsingDevicePIN))
                            .preferenceValue(OctoConfig.INSTANCE.allowUsingDevicePIN)
                            .title(LocaleController.getString(R.string.BiometricSettingsAllowDevicePIN))
                            .showIf(canShowBiometricOptions)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkAvailability(OctoConfig.INSTANCE.allowUsingFaceUnlock))
                            .preferenceValue(OctoConfig.INSTANCE.allowUsingFaceUnlock)
                            .title(LocaleController.getString(R.string.BiometricSettingsAllowFaceUnlock))
                            .showIf(canShowBiometricOptions)
                            .build());
                    category.row(new HeaderRow(LocaleController.getString(R.string.BiometricAskEvery), canShowBiometricAskAfter).headerStyle(false));
                    category.row(new SliderChooseRow.SliderChooseRowBuilder()
                            .options(new ArrayList<>() {{
                                add(new Pair<>(10, LocaleController.formatString("SlowmodeSeconds", R.string.SlowmodeSeconds, 10)));
                                add(new Pair<>(30, LocaleController.formatString("SlowmodeSeconds", R.string.SlowmodeSeconds, 30)));
                                add(new Pair<>(60, LocaleController.formatString("SlowmodeMinutes", R.string.SlowmodeMinutes, 1)));
                                add(new Pair<>(300, LocaleController.formatString("SlowmodeMinutes", R.string.SlowmodeMinutes, 5)));
                                add(new Pair<>(900, LocaleController.formatString("SlowmodeMinutes", R.string.SlowmodeMinutes, 15)));
                                add(new Pair<>(3600, LocaleController.formatString("SlowmodeHours", R.string.SlowmodeHours, 1)));
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
                .category(LocaleController.getString(R.string.Warnings), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.promptBeforeCalling)
                            .title(LocaleController.getString(R.string.PromptBeforeCalling))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.warningBeforeDeletingChatHistory)
                            .title(LocaleController.getString(R.string.PromptBeforeDeletingChatHistory))
                            .build());
                })
                .category(LocaleController.getString(R.string.PhoneNumberPrivacy), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> canShowPhoneNumberAlternative.setValue(OctoConfig.INSTANCE.hidePhoneNumber.getValue() || OctoConfig.INSTANCE.hideOtherPhoneNumber.getValue()))
                            .preferenceValue(OctoConfig.INSTANCE.hidePhoneNumber)
                            .title(LocaleController.getString(R.string.HidePhoneNumber))
                            .description(LocaleController.getString(R.string.HidePhoneNumber_Desc))
                            .postNotificationName(NotificationCenter.reloadInterface, NotificationCenter.mainUserInfoChanged)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> canShowPhoneNumberAlternative.setValue(OctoConfig.INSTANCE.hidePhoneNumber.getValue() || OctoConfig.INSTANCE.hideOtherPhoneNumber.getValue()))
                            .preferenceValue(OctoConfig.INSTANCE.hideOtherPhoneNumber)
                            .title(LocaleController.getString(R.string.HideOtherPhoneNumber))
                            .description(LocaleController.getString(R.string.HideOtherPhoneNumber_Desc))
                            .postNotificationName(NotificationCenter.reloadInterface, NotificationCenter.mainUserInfoChanged)
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.phoneNumberAlternative)
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(PhoneNumberAlternative.SHOW_HIDDEN_NUMBER_STRING.getValue())
                                            .setItemTitle(LocaleController.formatString(R.string.ShowHiddenNumber, LocaleController.getString(R.string.MobileHidden))),
                                    new PopupChoiceDialogOption()
                                            .setId(PhoneNumberAlternative.SHOW_FAKE_PHONE_NUMBER.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.ShowFakePhoneNumber))
                                            .setItemDescription(LocaleController.formatString(R.string.ShowFakePhoneNumber_Desc, "+39 123 456 7890")),
                                    new PopupChoiceDialogOption()
                                            .setId(PhoneNumberAlternative.SHOW_USERNAME.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.ShowUsernameAsPhoneNumber))
                                            .setItemDescription(LocaleController.getString(R.string.ShowUsernameAsPhoneNumber_Desc))
                            ))
                            .showIf(canShowPhoneNumberAlternative)
                            .title(LocaleController.getString(R.string.InsteadPhoneNumber))
                            .build());
                })
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(LocaleController.getString(R.string.BiometricUnavailable))
                        .showIf(canShowBiometricOptions, true)
                        .build())
                .build();
    }

    private void showLockedHelp(Context context, Theme.ResourcesProvider resourcesProvider, PreferencesFragment fragment) {
        BottomSheet[] bottomSheet = new BottomSheet[1];
        LockedChatsHelp lockedChatsHelp = new LockedChatsHelp(context, resourcesProvider, () -> {
            if (bottomSheet[0] != null) {
                bottomSheet[0].dismiss();
                bottomSheet[0] = null;
            }
        }, () -> {
            if (bottomSheet[0] != null) {
                bottomSheet[0].dismiss();
                bottomSheet[0] = null;
            }
            AndroidUtilities.runOnUIThread(() -> {
                UsersSelectActivity activity = getUsersSelectActivity();
                activity.setDelegate((ids, flags) -> {
                    FingerprintUtils.clearLockedChats();
                    FingerprintUtils.lockChatsMultiFromIDs(ids, true);
                    fragment.notifyItemChanged(PreferenceType.TEXT_ICON.getAdapterType());
                });
                fragment.presentFragment(activity);
            }, 300);
        });
        lockedChatsHelp.asSettingsUI();
        bottomSheet[0] = new BottomSheet.Builder(context, false, resourcesProvider)
                .setCustomView(lockedChatsHelp, Gravity.TOP | Gravity.CENTER_HORIZONTAL)
                .show();
        bottomSheet[0].fixNavigationBar(Theme.getColor(Theme.key_dialogBackground));
    }

    @NonNull
    private static UsersSelectActivity getUsersSelectActivity() {
        ArrayList<Long> chatsList = new ArrayList<>();
        for (FingerprintUtils.LockedChat chat : FingerprintUtils.getLockedChats()) {
            if (chat.chat() != null) {
                chatsList.add(-chat.chat().id);
            } else if (chat.user() != null) {
                chatsList.add(chat.user().id);
            }
        }

        UsersSelectActivity activity = new UsersSelectActivity(true, chatsList, 0);
        activity.asLockedChats();
        return activity;
    }

    private String composeBiometricCaptionForSeconds() {
        return LocaleController.formatString(R.string.BiometricAskEvery_Desc, formatSeconds(biometricAskEveryTemp.getValue()));
    }

    private String formatSeconds(int seconds) {
        if (seconds < 60) {
            return LocaleController.formatPluralString("Seconds", seconds);
        } else if (seconds < 60 * 60) {
            return LocaleController.formatPluralString("Minutes", seconds / 60);
        } else {
            return LocaleController.formatPluralString("Hours", seconds / 60 / 60);
        }
    }

    private boolean canShowBiometricAskAfter() {
        if (!FingerprintUtils.hasFingerprint()) {
            return false;
        }

        return OctoConfig.INSTANCE.biometricOpenArchive.getValue() || OctoConfig.INSTANCE.biometricOpenCallsLog.getValue() || OctoConfig.INSTANCE.biometricOpenSavedMessages.getValue() || OctoConfig.INSTANCE.biometricOpenSecretChats.getValue() || FingerprintUtils.hasLockedAccounts();
    }

    private boolean checkAvailability(ConfigProperty<Boolean> option) {
        if (!option.getValue() || !FingerprintUtils.hasFingerprint()) {
            return true;
        } else {
            FingerprintUtils.checkFingerprint(context, FingerprintUtils.EDIT_SETTINGS, new FingerprintUtils.FingerprintResult() {
                @Override
                public void onSuccess() {
                    option.updateValue(false);
                    fragment.notifyItemChanged(PreferenceType.EXPANDABLE_ROWS_CHILD.getAdapterType(), PreferenceType.EXPANDABLE_ROWS.getAdapterType());

                    if (canShowBiometricAskAfter.getValue() != canShowBiometricAskAfter()) {
                        canShowBiometricAskAfter.updateValue(canShowBiometricAskAfter());
                        fragment.reloadUIAfterValueUpdate();
                    }
                }

                @Override
                public void onFailed() {

                }
            });
            return false;
        }
    }

    private void handleSlideBarUpdate() {
        if (OctoConfig.INSTANCE.biometricAskEvery.getValue().equals(biometricAskEveryTemp.getValue())) {
            return;
        }

        if (!FingerprintUtils.hasFingerprint()) {
            OctoConfig.INSTANCE.biometricAskEvery.updateValue(biometricAskEveryTemp.getValue());
            return;
        }

        FingerprintUtils.checkFingerprint(context, FingerprintUtils.EDIT_SETTINGS, true, new FingerprintUtils.FingerprintResult() {
            @Override
            public void onSuccess() {
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
            return true;
        } else {
            FingerprintUtils.checkFingerprint(context, FingerprintUtils.EDIT_SETTINGS, new FingerprintUtils.FingerprintResult() {
                @Override
                public void onSuccess() {
                    if (accountAssoc.get(accountId) == null) {
                        return;
                    }

                    if (userId == UserConfig.getInstance(UserConfig.selectedAccount).clientUserId && LaunchActivity.instance.getActionBarLayout() instanceof ActionBarOverride override) {
                        override.unlock(userId);
                    }

                    FingerprintUtils.lockAccount(userId, false);
                    Objects.requireNonNull(accountAssoc.get(accountId)).updateValue(false);
                    fragment.notifyItemChanged(PreferenceType.EXPANDABLE_ROWS_CHILD.getAdapterType(), PreferenceType.EXPANDABLE_ROWS.getAdapterType());

                    if (canShowBiometricAskAfter.getValue() != canShowBiometricAskAfter()) {
                        canShowBiometricAskAfter.updateValue(canShowBiometricAskAfter());
                        fragment.reloadUIAfterValueUpdate();
                    }
                }

                @Override
                public void onFailed() {

                }
            });
            return false;
        }
    }
}