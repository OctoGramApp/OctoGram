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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.telegram.messenger.AccountInstance;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.LinkSpanDrawable;
import org.telegram.ui.Components.ScaleStateListAnimator;
import org.telegram.ui.Components.spoilers.SpoilersTextView;
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
import it.octogram.android.deeplink.DeepLinkDef;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferenceType;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.fragment.ActionBarOverride;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.rows.impl.CustomCellRow;
import it.octogram.android.preferences.rows.impl.ExpandableRows;
import it.octogram.android.preferences.rows.impl.FooterInformativeRow;
import it.octogram.android.preferences.rows.impl.HeaderRow;
import it.octogram.android.preferences.rows.impl.ListRow;
import it.octogram.android.preferences.rows.impl.ShadowRow;
import it.octogram.android.preferences.rows.impl.SliderChooseRow;
import it.octogram.android.preferences.rows.impl.SwitchRow;
import it.octogram.android.preferences.rows.impl.TextIconRow;
import it.octogram.android.preferences.ui.components.LockedChatsHelp;
import it.octogram.android.utils.OctoUtils;
import it.octogram.android.utils.account.FingerprintUtils;
import it.octogram.android.utils.appearance.PopupChoiceDialogOption;
import it.octogram.android.utils.config.ExpandableRowsOption;


public class OctoPrivacySettingsUI implements PreferencesEntry {
    private Context context;
    private PreferencesFragment fragment;
    private boolean isFirstFingerprintAsk = true;

    private final ConfigProperty<Boolean> canShowOptions = new ConfigProperty<>(null, true);
    private final ConfigProperty<Boolean> showFingerprintFailedAlert = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> canShowBiometricLockedChatsExpand = new ConfigProperty<>(null, true);
    private final ConfigProperty<Boolean> canShowBiometricLockedChatsMoreOptions = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> canShowBiometricLockedChatsMoreOptionsNotifications = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> canShowLockedAccountsOptions = new ConfigProperty<>(null, false);

    private final ConfigProperty<Boolean> canShowPhoneNumberAlternative = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> canShowDebugProperties = new ConfigProperty<>(null, false);
    private final ConfigProperty<Integer> biometricAskEveryTemp = new ConfigProperty<>(OctoConfig.INSTANCE.biometricAskEvery.getKey(), 0);
    private final ConfigProperty<Boolean> canShowFaceUnlock = new ConfigProperty<>(null, false);

    private final HashMap<Integer, ConfigProperty<Boolean>> accountAssoc = new HashMap<>();

    @NonNull
    @Override
    public OctoPreferences getPreferences(@NonNull PreferencesFragment fragment, @NonNull Context context) {
        this.context = context;
        this.fragment = fragment;
        accountAssoc.clear();

        updateConfig();
        biometricAskEveryTemp.updateValue(OctoConfig.INSTANCE.biometricAskEvery.getValue());

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
                .row(new CustomCellRow.CustomCellRowBuilder()
                        .layout(getFingerprintFailAlert())
                        .showIf(showFingerprintFailedAlert)
                        .build())
                .row(new ShadowRow(showFingerprintFailedAlert))
                .category(getString(R.string.LockedActions), canShowOptions, category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkAvailability(OctoConfig.INSTANCE.biometricOpenArchive))
                            .onPostUpdate(this::updateConfig)
                            .preferenceValue(OctoConfig.INSTANCE.biometricOpenArchive)
                            .title(getString(R.string.BiometricSettingsOpenArchive))
                            .showIf(canShowOptions)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkAvailability(OctoConfig.INSTANCE.biometricOpenCallsLog))
                            .onPostUpdate(this::updateConfig)
                            .preferenceValue(OctoConfig.INSTANCE.biometricOpenCallsLog)
                            .title(getString(R.string.BiometricSettingsOpenCallsLog))
                            .showIf(canShowOptions)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkAvailability(OctoConfig.INSTANCE.biometricOpenSecretChats))
                            .onPostUpdate(this::updateConfig)
                            .preferenceValue(OctoConfig.INSTANCE.biometricOpenSecretChats)
                            .title(getString(R.string.BiometricSettingsOpenSecretChats))
                            .showIf(canShowOptions)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkAvailability(OctoConfig.INSTANCE.biometricOpenSettings))
                            .onPostUpdate(this::updateConfig)
                            .preferenceValue(OctoConfig.INSTANCE.biometricOpenSettings)
                            .title(getString(R.string.BiometricSettingsOpenSettings))
                            .showIf(canShowOptions)
                            .build());
                })
                .category(getString(R.string.LockedChats), canShowOptions, category -> {
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(this::showLockedHelp)
                            .icon(R.drawable.msg_help)
                            .title(getString(R.string.HowDoesItWork))
                            .showIf(canShowOptions)
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .propertySelectionTag("lockedChats")
                            .onClick(this::runUsersSelectActivity)
                            .icon(R.drawable.msg_edit)
                            .title(getString(R.string.LockedChatsHelpHintEditList))
                            .showIf(canShowOptions)
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> {
                                canShowBiometricLockedChatsExpand.updateValue(false);
                                canShowBiometricLockedChatsMoreOptions.updateValue(true);
                                updateConfig();
                            })
                            .icon(R.drawable.arrow_more)
                            .title(getString(R.string.LockedChatsHelpHintMore))
                            .showIf(canShowBiometricLockedChatsExpand)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkAvailability(OctoConfig.INSTANCE.lockedChatsHideChats))
                            .onPostUpdate(this::updateConfig)
                            .preferenceValue(OctoConfig.INSTANCE.lockedChatsHideChats)
                            .title(getString(R.string.LockedChats_Options_HideChats))
                            .description(getString(R.string.LockedChats_Options_HideChats_Desc))
                            .showIf(canShowBiometricLockedChatsMoreOptions)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkAvailability(OctoConfig.INSTANCE.lockedChatsLockScreenshots))
                            .onPostUpdate(this::updateConfig)
                            .preferenceValue(OctoConfig.INSTANCE.lockedChatsLockScreenshots)
                            .title(getString(R.string.LockedChats_Options_LockScreenshots))
                            .showIf(canShowBiometricLockedChatsMoreOptions)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkAvailability(OctoConfig.INSTANCE.lockedChatsShowNotifications))
                            .onPostUpdate(this::updateConfig)
                            .preferenceValue(OctoConfig.INSTANCE.lockedChatsShowNotifications)
                            .title(getString(R.string.LockedChats_Options_ShowNotifications))
                            .showIf(canShowBiometricLockedChatsMoreOptions)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkAvailability(OctoConfig.INSTANCE.lockedChatsSpoilerNotifications))
                            .onPostUpdate(this::updateConfig)
                            .preferenceValue(OctoConfig.INSTANCE.lockedChatsSpoilerNotifications)
                            .title(getString(R.string.LockedChats_Options_SpoilerContent))
                            .description(getString(R.string.LockedChats_Options_SpoilerContent_Desc))
                            .showIf(canShowBiometricLockedChatsMoreOptionsNotifications)
                            .build());
                })
                .category(getString(R.string.ActionsSettingsBiometric), canShowOptions, category -> {
                    category.row(new ExpandableRows.ExpandableRowsBuilder()
                            .setId(ExpandableRowsIds.LOCKED_ACCOUNTS.getId())
                            .setIcon(R.drawable.msg_openprofile)
                            .setMainTitle(getString(R.string.LockedAccounts))
                            .hideMainSwitch(true)
                            .isLocked(true)
                            .addRow(accountsList)
                            .showIf(canShowOptions)
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
                .category(getString(R.string.BiometricSettings), canShowOptions, category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkAvailability(OctoConfig.INSTANCE.allowUsingDevicePIN))
                            .preferenceValue(OctoConfig.INSTANCE.allowUsingDevicePIN)
                            .title(getString(R.string.BiometricSettingsAllowDevicePIN))
                            .showIf(canShowOptions)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkAvailability(OctoConfig.INSTANCE.allowUsingFaceUnlock))
                            .preferenceValue(OctoConfig.INSTANCE.allowUsingFaceUnlock)
                            .title(getString(R.string.BiometricSettingsAllowFaceUnlock))
                            .showIf(canShowFaceUnlock)
                            .build());
                    category.row(new HeaderRow(getString(R.string.BiometricAskEvery), canShowOptions).headerStyle(false));
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
                            .showIf(canShowOptions)
                            .build());
                })
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .setDynamicDataUpdate(this::composeBiometricCaptionForSeconds)
                        .title(composeBiometricCaptionForSeconds())
                        .showIf(canShowOptions)
                        .build())
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
                .build();
    }

    private LinearLayout getFingerprintFailAlert() {
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
        layout.addView(detailTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT, 21, 14, 21, canShowOptions.getValue() ? 0 : 16));

        textView.setText(getString(R.string.BiometricUnavailable_Title));
        detailTextView.setText(getString(canShowOptions.getValue() ? R.string.BiometricUnavailable_Desc : R.string.BiometricUnavailable_Desc_Old));

        if (canShowOptions.getValue()) {
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
            textViewButton.setText(getString(R.string.BiometricUnavailable_Test));
            textViewButton.setOnClickListener(v -> FingerprintUtils.fixFingerprint(context, new FingerprintUtils.FingerprintResult() {
                public void handle() {
                    FingerprintUtils.cancelPendingAuthentications();
                    FingerprintUtils.reloadFingerprintState();
                    if (FingerprintUtils.hasFingerprintCached()) {
                        AndroidUtilities.runOnUIThread(() -> {
                            BulletinFactory.of(fragment).createSuccessBulletin(getString(R.string.BiometricUnavailable_Test_Fixed)).show();
                            updateConfig();
                            fragment.reloadUIAfterValueUpdate();
                        }, 300);
                    } else {
                        showError(0);
                    }
                }

                public void showError(int error) {
                    BulletinFactory.of(fragment).createSimpleBulletin(
                            R.raw.chats_infotip,
                            getString(R.string.BiometricUnavailable_Test_Wrong) + (error == 0 ? "" : " (e"+error+")"),
                            getString(R.string.BiometricUnavailable_Test_Wrong_Desc),
                            getString(R.string.BiometricUnavailable_Test_Wrong_Button), () -> {
                                Intent intent;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    intent = new Intent(Settings.ACTION_FINGERPRINT_ENROLL);
                                } else {
                                    intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                                }
                                context.startActivity(intent);
                            }).show();
                }

                @Override
                public void onSuccess() {
                    handle();
                }

                @Override
                public void onFailed() {
                    handle();
                }

                @Override
                public void onError(int error) {
                    showError(error);
                }
            }));
        }

        return layout;
    }

    @NonNull
    private static UsersSelectActivity getUsersSelectActivity() {
        UsersSelectActivity activity = new UsersSelectActivity(true, FingerprintUtils.getLockedChatsIds(), 0);
        activity.asLockedChats();
        return activity;
    }

    private void runUsersSelectActivity() {
        if (showFingerprintFailedAlert.getValue() || !FingerprintUtils.hasFingerprintCached()) {
            BulletinFactory.of(fragment).createSimpleBulletin(R.raw.chats_infotip, getString(R.string.BiometricUnavailable_Title), getString(R.string.BiometricUnavailable_Unavailable)).show();
            return;
        }

        UsersSelectActivity activity = getUsersSelectActivity();
        activity.setDelegate((ids, flags) -> {
            FingerprintUtils.clearLockedChats();
            FingerprintUtils.lockChatsMultiFromIDs(ids, true);
        });
        fragment.presentFragment(activity);
    }

    private void showLockedHelp() {
        BottomSheet[] bottomSheet = new BottomSheet[1];
        LockedChatsHelp lockedChatsHelp = new LockedChatsHelp(context, null, () -> {
            if (bottomSheet[0] != null) {
                bottomSheet[0].dismiss();
                bottomSheet[0] = null;
            }
        }, () -> {
            if (bottomSheet[0] != null) {
                bottomSheet[0].dismiss();
                bottomSheet[0] = null;
            }
            AndroidUtilities.runOnUIThread(this::runUsersSelectActivity, 300);
        });
        lockedChatsHelp.asSettingsUI();
        bottomSheet[0] = new BottomSheet.Builder(context, false, null)
                .setCustomView(lockedChatsHelp, Gravity.TOP | Gravity.CENTER_HORIZONTAL)
                .show();
        bottomSheet[0].fixNavigationBar(Theme.getColor(Theme.key_dialogBackground));
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

        boolean hasChanged = updateWithHasChangesCheck(canShowOptions, Build.VERSION.SDK_INT >= 23);
        hasChanged |= updateWithHasChangesCheck(showFingerprintFailedAlert, !hasFingerprint || !canShowOptions.getValue());
        hasChanged |= updateWithHasChangesCheck(canShowBiometricLockedChatsMoreOptionsNotifications, canShowOptions.getValue() && canShowBiometricLockedChatsMoreOptions.getValue() && OctoConfig.INSTANCE.lockedChatsShowNotifications.getValue());
        hasChanged |= updateWithHasChangesCheck(canShowLockedAccountsOptions, canShowOptions.getValue() && FingerprintUtils.hasLockedAccounts());
        hasChanged |= updateWithHasChangesCheck(canShowPhoneNumberAlternative, canShowOptions.getValue() && OctoConfig.INSTANCE.hidePhoneNumber.getValue() || OctoConfig.INSTANCE.hideOtherPhoneNumber.getValue());
        hasChanged |= updateWithHasChangesCheck(canShowDebugProperties, canShowOptions.getValue() && (BuildConfig.DEBUG_PRIVATE_VERSION || BuildVars.isBetaApp()));
        hasChanged |= updateWithHasChangesCheck(canShowFaceUnlock, canShowOptions.getValue() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ApplicationLoader.applicationContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_FACE));

        if (!canShowOptions.getValue()) {
            hasChanged |= updateWithHasChangesCheck(canShowBiometricLockedChatsExpand, false);
        }

        return hasChanged;
    }

    private boolean updateWithHasChangesCheck(ConfigProperty<Boolean> key, Boolean value) {
        if (key.getValue() == value) {
            return false;
        }

        key.updateValue(value);
        return true;
    }

    private boolean checkAvailability(ConfigProperty<Boolean> option) {
        if (showFingerprintFailedAlert.getValue() || !FingerprintUtils.hasFingerprintCached()) {
            BulletinFactory.of(fragment).createSimpleBulletin(R.raw.info, getString(R.string.BiometricUnavailable_Title), getString(R.string.BiometricUnavailable_Unavailable)).show();
            return false;
        }

        if (!option.getValue()) {
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
        if (showFingerprintFailedAlert.getValue() || !FingerprintUtils.hasFingerprintCached()) {
            BulletinFactory.of(fragment).createSimpleBulletin(R.raw.chats_infotip, getString(R.string.BiometricUnavailable_Title), getString(R.string.BiometricUnavailable_Unavailable)).show();
            biometricAskEveryTemp.updateValue(OctoConfig.INSTANCE.biometricAskEvery.getValue());
            fragment.notifyItemChanged(PreferenceType.FOOTER_INFORMATIVE.getAdapterType(), PreferenceType.SLIDER_CHOOSE.getAdapterType());
            return;
        }

        if (OctoConfig.INSTANCE.biometricAskEvery.getValue().equals(biometricAskEveryTemp.getValue())) {
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
        if (showFingerprintFailedAlert.getValue() || !FingerprintUtils.hasFingerprintCached()) {
            BulletinFactory.of(fragment).createSimpleBulletin(R.raw.chats_infotip, getString(R.string.BiometricUnavailable_Title), getString(R.string.BiometricUnavailable_Unavailable)).show();
            return false;
        }

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

    /*public boolean isFaceUnlockStrongAndAvailable(Context context) {
        var supportedDevices = new String[]{
                // Pixel 4 Series
                "flame", "coral",
                // Pixel 8 Series
                "husky",
                // Pixel 9 Series
                "tokay", "tegu", "caiman", "komodo", "comet",
        };
        var isPixelWithStrongBiometric = Arrays.asList(supportedDevices).contains(Build.DEVICE.toLowerCase(Locale.US).trim());
        var hasFace = context.getPackageManager().hasSystemFeature("android.hardware.biometrics.face");
        var biometricManager = BiometricManager.from(context);

        var canStrongAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS;

        return hasFace && (isPixelWithStrongBiometric || canStrongAuthenticate);
    }*/
}