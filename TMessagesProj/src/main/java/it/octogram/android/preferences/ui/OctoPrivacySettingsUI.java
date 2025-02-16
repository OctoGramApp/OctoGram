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

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.UsersSelectActivity;

import java.util.ArrayList;
import java.util.List;

import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.PhoneNumberAlternative;
import it.octogram.android.StickerUi;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferenceType;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.rows.impl.FooterInformativeRow;
import it.octogram.android.preferences.rows.impl.ListRow;
import it.octogram.android.preferences.rows.impl.SliderChooseRow;
import it.octogram.android.preferences.rows.impl.SwitchRow;
import it.octogram.android.preferences.rows.impl.TextIconRow;
import it.octogram.android.preferences.ui.components.LockedChatsHelp;
import it.octogram.android.utils.FingerprintUtils;
import it.octogram.android.utils.PopupChoiceDialogOption;

public class OctoPrivacySettingsUI implements PreferencesEntry {
    private Context context;
    private PreferencesFragment fragment;
    private final ConfigProperty<Boolean> canShowBiometricAskAfter = new ConfigProperty<>(null, false);
    private final ConfigProperty<Integer> biometricAskEveryTemp = new ConfigProperty<>(null, 0);

    @Override
    public OctoPreferences getPreferences(PreferencesFragment fragment, Context context) {
        this.context = context;
        this.fragment = fragment;

        canShowBiometricAskAfter.updateValue(canShowBiometricAskAfter());
        biometricAskEveryTemp.updateValue(OctoConfig.INSTANCE.biometricAskEvery.getValue());
        ConfigProperty<Boolean> canShowBiometricOptions = new ConfigProperty<>(null, FingerprintUtils.hasFingerprint());
        ConfigProperty<Boolean> canShowPhoneNumberAlternative = new ConfigProperty<>(null, OctoConfig.INSTANCE.hidePhoneNumber.getValue() || OctoConfig.INSTANCE.hideOtherPhoneNumber.getValue());

        return OctoPreferences.builder(LocaleController.getString(R.string.PrivacySettings))
                .deepLink("tg://privacy")
                .sticker(context, OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME, StickerUi.PRIVACY, true, LocaleController.getString(R.string.OctoPrivacySettingsHeader))
                .category(LocaleController.getString(R.string.BiometricSettings), canShowBiometricOptions, category -> {
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
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkAvailability(OctoConfig.INSTANCE.biometricOpenArchive))
                            .onPostUpdate(() -> canShowBiometricAskAfter.updateValue(canShowBiometricAskAfter()))
                            .preferenceValue(OctoConfig.INSTANCE.biometricOpenArchive)
                            .title(LocaleController.getString(R.string.BiometricSettingsOpenArchive))
                            .showIf(canShowBiometricOptions)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkAvailability(OctoConfig.INSTANCE.biometricOpenCallsLog))
                            .onPostUpdate(() -> canShowBiometricAskAfter.updateValue(canShowBiometricAskAfter()))
                            .preferenceValue(OctoConfig.INSTANCE.biometricOpenCallsLog)
                            .title(LocaleController.getString(R.string.BiometricSettingsOpenCallsLog))
                            .showIf(canShowBiometricOptions)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkAvailability(OctoConfig.INSTANCE.biometricOpenSavedMessages))
                            .onPostUpdate(() -> canShowBiometricAskAfter.updateValue(canShowBiometricAskAfter()))
                            .preferenceValue(OctoConfig.INSTANCE.biometricOpenSavedMessages)
                            .title(LocaleController.getString(R.string.BiometricSettingsOpenSavedMessages))
                            .showIf(canShowBiometricOptions)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkAvailability(OctoConfig.INSTANCE.biometricOpenSecretChats))
                            .onPostUpdate(() -> canShowBiometricAskAfter.updateValue(canShowBiometricAskAfter()))
                            .preferenceValue(OctoConfig.INSTANCE.biometricOpenSecretChats)
                            .title(LocaleController.getString(R.string.BiometricSettingsOpenSecretChats))
                            .showIf(canShowBiometricOptions)
                            .build());
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
                })
                .category(LocaleController.getString(R.string.BiometricAskEvery), canShowBiometricAskAfter, category -> category.row(new SliderChooseRow.SliderChooseRowBuilder()
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
                        .build()))
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

        return OctoConfig.INSTANCE.biometricOpenArchive.getValue() || OctoConfig.INSTANCE.biometricOpenCallsLog.getValue() || OctoConfig.INSTANCE.biometricOpenSavedMessages.getValue() || OctoConfig.INSTANCE.biometricOpenSecretChats.getValue();
    }

    private boolean checkAvailability(ConfigProperty<Boolean> option) {
        if (!option.getValue() || !FingerprintUtils.hasFingerprint()) {
            return true;
        } else {
            FingerprintUtils.checkFingerprint(context, FingerprintUtils.EDIT_SETTINGS, new FingerprintUtils.FingerprintResult() {
                @Override
                public void onSuccess() {
                    option.updateValue(false);
                    fragment.notifyItemChanged(PreferenceType.SWITCH.getAdapterType());

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
                fragment.notifyItemChanged(PreferenceType.FOOTER_INFORMATIVE.getAdapterType());
                fragment.notifyItemChanged(PreferenceType.SLIDER_CHOOSE.getAdapterType());
            }
        });
    }
}