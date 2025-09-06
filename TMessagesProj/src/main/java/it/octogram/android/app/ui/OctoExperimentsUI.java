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
import android.content.Intent;
import android.os.Build;
import android.util.Pair;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BottomSheetTabs;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.LaunchActivity;

import java.util.ArrayList;
import java.util.List;

import it.octogram.android.AudioType;
import it.octogram.android.ConfigProperty;
import it.octogram.android.DeviceIdentifyState;
import it.octogram.android.OctoConfig;
import it.octogram.android.PhotoResolution;
import it.octogram.android.QualityPreset;
import it.octogram.android.StickerUi;
import it.octogram.android.app.OctoPreferences;
import it.octogram.android.app.PreferencesEntry;
import it.octogram.android.app.fragment.PreferencesFragment;
import it.octogram.android.app.rows.impl.HeaderWithoutStyleRow;
import it.octogram.android.app.rows.impl.ListRow;
import it.octogram.android.app.rows.impl.ShadowRow;
import it.octogram.android.app.rows.impl.SliderChooseRow;
import it.octogram.android.app.rows.impl.SwitchRow;
import it.octogram.android.app.rows.impl.TextIconRow;
import it.octogram.android.app.ui.bottomsheets.AllowExperimentalBottomSheet;
import it.octogram.android.theme.MonetIconController;
import it.octogram.android.utils.AppRestartHelper;
import it.octogram.android.utils.appearance.PopupChoiceDialogOption;
import it.octogram.android.utils.deeplink.DeepLinkDef;


public class OctoExperimentsUI implements PreferencesEntry {
    private final ConfigProperty<Boolean> canShowMonetIconSwitch = new ConfigProperty<>(null, Build.VERSION.SDK_INT == Build.VERSION_CODES.S || Build.VERSION.SDK_INT == Build.VERSION_CODES.S_V2);
    private final ConfigProperty<Boolean> isMonetSelected = new ConfigProperty<>(null, MonetIconController.INSTANCE.isSelectedMonet());
    private final ConfigProperty<Boolean> canShowDrawerProfileAsBubble = new ConfigProperty<>(null, false);
    private final ConfigProperty<Boolean> canShowDownloadBoostLevel = new ConfigProperty<>(null, false);
    private final static boolean ENABLE_EXP_FEATURE = BuildConfig.BUILD_TYPE.equals("debug") || BuildConfig.BUILD_TYPE.equals("pbeta");

    @NonNull
    @Override
    public OctoPreferences getPreferences(@NonNull PreferencesFragment fragment, @NonNull Context context) {
        updateState();
        return OctoPreferences.builder(getString(R.string.Experiments))
                .deepLink(DeepLinkDef.EXPERIMENTAL)
                .row(new SwitchRow.SwitchRowBuilder()
                        .onClick(() -> {
                            if (OctoConfig.INSTANCE.experimentsEnabled.getValue()) {
                                AlertDialog.Builder alertDialogBuilder = getDisableFeaturesAlert(fragment, context);
                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();
                                alertDialog.redPositive();
                            } else {
                                AllowExperimentalBottomSheet bottomSheet = new AllowExperimentalBottomSheet(context, () -> {
                                    OctoConfig.INSTANCE.experimentsEnabled.updateValue(true);
                                    resetSettings();
                                    updateState();
                                    fragment.reloadUIAfterValueUpdate();
                                    AndroidUtilities.runOnUIThread(() -> BulletinFactory.of(fragment).createSuccessBulletin(getString(R.string.UseExperimentalFeatures_Success)).show(), 300);
                                });
                                bottomSheet.show();
                            }
                            return false;
                        })
                        .isMainPageAction(true)
                        .preferenceValue(OctoConfig.INSTANCE.experimentsEnabled)
                        .title(getString(R.string.UseExperimentalFeatures))
                        .build())
                .sticker(context, OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME, StickerUi.EXPERIMENTAL, true, getString(R.string.OctoExperimentsSettingsHeader))
                .category(getString(R.string.ExperimentalSettings), OctoConfig.INSTANCE.experimentsEnabled, category -> {
                    if (BuildConfig.DEBUG && ActionBarLayout.CAN_USE_PREDICTIVE_GESTURES) {
                        category.row(new SwitchRow.SwitchRowBuilder()
                                .onPostUpdate(() -> {
                                    if (!OctoConfig.INSTANCE.usePredictiveGestures.getValue()) {
                                        LaunchActivity.instance.attachBackEvent();
                                    }
                                })
                                .preferenceValue(OctoConfig.INSTANCE.usePredictiveGestures)
                                .title("Predictive Gestures")
                                .showIf(OctoConfig.INSTANCE.experimentsEnabled)
                                .build());
                    }
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.useFluentNavigationBar)
                            .title(getString(R.string.UseFluentNavigationBar))
                            .showIf(OctoConfig.INSTANCE.experimentsEnabled)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.moreHapticFeedbacks)
                            .title(getString(R.string.MoreHapticFeedbacks))
                            .showIf(OctoConfig.INSTANCE.experimentsEnabled)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.mediaInGroupCall)
                            .title(getString(R.string.MediaStream))
                            .showIf(OctoConfig.INSTANCE.experimentsEnabled)
                            .build());
                    if (ENABLE_EXP_FEATURE) {
                        category.row(new SwitchRow.SwitchRowBuilder()
                                .onClick(() -> handleExperimentalAlert(context, fragment,
                                        OctoConfig.INSTANCE.roundedTextBox,
                                        "Applies a rounded style to text boxes. \nSome text may overlap or be cut off."
                                ))
                                .preferenceValue(OctoConfig.INSTANCE.roundedTextBox)
                                .title("Rounded Text Boxes")
                                .description("Applies a rounded style to text boxes. \nSome text may overlap or be cut off.")
                                .showIf(OctoConfig.INSTANCE.experimentsEnabled)
                                .build());
                    }
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.useSmoothContextMenuStyling)
                            .title("Smooth Rounded Context Menus")
                            .description("Adds smoother, more rounded corners to context menus.\nMay cause minor visual glitches.")
                            .showIf(OctoConfig.INSTANCE.experimentsEnabled)
                            .requiresRestart(true)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.showRPCErrors)
                            .title(getString(R.string.ShowRPCErrors))
                            .showIf(OctoConfig.INSTANCE.experimentsEnabled)
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .options(List.of(
                                    new PopupChoiceDialogOption().setId(AudioType.MONO.getValue()).setItemTitle(getString(R.string.AudioTypeMono)),
                                    new PopupChoiceDialogOption().setId(AudioType.STEREO.getValue()).setItemTitle(getString(R.string.AudioTypeStereo))
                            ))
                            .currentValue(OctoConfig.INSTANCE.gcOutputType)
                            .title(getString(R.string.AudioTypeInCall))
                            .showIf(OctoConfig.INSTANCE.experimentsEnabled)
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .options(List.of(
                                    new PopupChoiceDialogOption().setId(PhotoResolution.LOW.getValue()).setItemTitle(getString(R.string.ResolutionLow)),
                                    new PopupChoiceDialogOption().setId(PhotoResolution.DEFAULT.getValue()).setItemTitle(getString(R.string.ResolutionMedium)),
                                    new PopupChoiceDialogOption().setId(PhotoResolution.HIGH.getValue()).setItemTitle(getString(R.string.ResolutionHigh))
                            ))
                            .currentValue(OctoConfig.INSTANCE.photoResolution)
                            .title(getString(R.string.PhotoResolution))
                            .showIf(OctoConfig.INSTANCE.experimentsEnabled)
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.maxRecentStickers)
                            .options(buildMaxRecentStickersOptions())
                            .title(getString(R.string.MaxRecentStickers))
                            .showIf(OctoConfig.INSTANCE.experimentsEnabled)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> {
                                AndroidUtilities.runOnUIThread(() -> {
                                    for (int i = 0; i < UserConfig.MAX_ACCOUNT_COUNT; i++) {
                                        if (UserConfig.getInstance(i).isClientActivated()) {
                                            ConnectionsManager.getInstance(i).checkConnection();
                                        }
                                    }
                                }, 300);
                                return true;
                            })
                            .preferenceValue(OctoConfig.INSTANCE.forceUseIpV6)
                            .title(getString(R.string.TryConnectWithIPV6))
                            .showIf(OctoConfig.INSTANCE.experimentsEnabled)
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.deviceIdentifyState)
                            .onSelected(fragment::showRestartTooltip)
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(DeviceIdentifyState.DEFAULT.getValue())
                                            .setItemTitle(getString(R.string.DeviceIdentifyDefault)),
                                    new PopupChoiceDialogOption()
                                            .setId(DeviceIdentifyState.FORCE_TABLET.getValue())
                                            .setItemTitle(getString(R.string.DeviceIdentifyTablet))
                                            .setItemDescription(getString(R.string.DeviceIdentifyTabletDesc)),
                                    new PopupChoiceDialogOption()
                                            .setId(DeviceIdentifyState.FORCE_SMARTPHONE.getValue())
                                            .setItemTitle(getString(R.string.DeviceIdentifySmartphone))
                                            .setItemDescription(getString(R.string.DeviceIdentifySmartphoneDesc))

                            ))
                            .title(getString(R.string.DeviceIdentifyStatus))
                            .showIf(OctoConfig.INSTANCE.experimentsEnabled)
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new OctoExperimentsNavigationUI()))
                            .value(getString(OctoConfig.INSTANCE.alternativeNavigation.getValue() ? R.string.NotificationsOn : R.string.NotificationsOff))
                            .title(getString(R.string.AlternativeNavigation))
                            .showIf(OctoConfig.INSTANCE.experimentsEnabled)
                            .build()
                    );
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> {
                                if (OctoConfig.INSTANCE.disableTelegramTabsStack.getValue()) {
                                    BottomSheetTabs tabs = LaunchActivity.instance.getBottomSheetTabs();
                                    if (tabs != null) {
                                        tabs.removeAll();
                                    }
                                }
                            })
                            .preferenceValue(OctoConfig.INSTANCE.disableTelegramTabsStack)
                            .title(getString(R.string.DisableTabsStack))
                            .showIf(OctoConfig.INSTANCE.experimentsEnabled)
                            .build());
                })
                .row(new SwitchRow.SwitchRowBuilder()
                        .onClick(() -> {
                            MonetIconController.INSTANCE.switchToMonet();
                            var progressDialog = new AlertDialog(context, 3);
                            progressDialog.show();
                            AndroidUtilities.runOnUIThread(progressDialog::dismiss, 2000);
                            return true;
                        })
                        .preferenceValue(isMonetSelected)
                        .title(getString(R.string.MonetIcon))
                        .description(getString(R.string.MonetIconDesc))
                        .showIf(canShowMonetIconSwitch)
                        .build())
                .row(new ShadowRow(canShowMonetIconSwitch))
                .category(getString(R.string.Chats), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(fragment::rebuildAllFragmentsWithLast)
                            .preferenceValue(OctoConfig.INSTANCE.hideOpenButtonChatsList)
                            .title(getString(R.string.HideOpenButtonChatsList))
                            .showIf(OctoConfig.INSTANCE.experimentsEnabled)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.alwaysExpandBlockQuotes)
                            .title(getString(R.string.AlwaysExpandBlockQuotes))
                            .showIf(OctoConfig.INSTANCE.experimentsEnabled)
                            .build());
                })
                .category(getString(R.string.DrawerHeaderAsBubble), canShowDrawerProfileAsBubble, category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> AndroidUtilities.runOnUIThread(() -> LaunchActivity.instance.reloadDrawerHeader()))
                            .preferenceValue(OctoConfig.INSTANCE.profileBubbleHideBorder)
                            .title(getString(R.string.ProfileBubbleHideBorder))
                            .showIf(canShowDrawerProfileAsBubble)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> AndroidUtilities.runOnUIThread(() -> LaunchActivity.instance.reloadDrawerHeader()))
                            .preferenceValue(OctoConfig.INSTANCE.profileBubbleMoreTopPadding)
                            .title(getString(R.string.ProfileBubbleMoreTopPadding))
                            .showIf(canShowDrawerProfileAsBubble)
                            .build());
                })
                .category(getString(R.string.DownloadAndUploadBoost), category -> {
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.useQualityPreset)
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(QualityPreset.AUTO.getValue())
                                            .setItemTitle(getString(R.string.UseQualityPreset_Default)),
                                    new PopupChoiceDialogOption()
                                            .setId(QualityPreset.HIGHEST.getValue())
                                            .setItemTitle(getString(R.string.UseQualityPreset_HighQuality))
                                            .setItemDescription(getString(R.string.UseQualityPreset_HighQuality_Desc)),
                                    new PopupChoiceDialogOption()
                                            .setId(QualityPreset.LOWEST.getValue())
                                            .setItemTitle(getString(R.string.UseQualityPreset_LowQuality))
                                            .setItemDescription(getString(R.string.UseQualityPreset_LowQuality_Desc)),
                                    new PopupChoiceDialogOption()
                                            .setId(QualityPreset.DYNAMIC.getValue())
                                            .setItemTitle(getString(R.string.UseQualityPreset_Dynamic))
                                            .setItemDescription(getString(R.string.UseQualityPreset_Dynamic_Desc))
                            ))
                            .title(getString(R.string.UseQualityPreset))
                            .showIf(OctoConfig.INSTANCE.experimentsEnabled)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.uploadBoost)
                            .title(getString(R.string.UploadBoost))
                            .showIf(OctoConfig.INSTANCE.experimentsEnabled)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(this::updateState)
                            .preferenceValue(OctoConfig.INSTANCE.downloadBoost)
                            .title(getString(R.string.DownloadBoost))
                            .showIf(OctoConfig.INSTANCE.experimentsEnabled)
                            .build());
                    category.row(new HeaderWithoutStyleRow(getString(R.string.DownloadBoostType), canShowDownloadBoostLevel));
                    category.row(new SliderChooseRow.SliderChooseRowBuilder()
                            .options(new ArrayList<>() {{
                                add(new Pair<>(0, getString(R.string.Default)));
                                add(new Pair<>(1, getString(R.string.Fast)));
                                add(new Pair<>(2, getString(R.string.Extreme)));
                            }})
                            .preferenceValue(OctoConfig.INSTANCE.downloadBoostValue)
                            .showIf(canShowDownloadBoostLevel)
                            .build());
                })
                .build();
    }

    private AlertDialog.Builder getDisableFeaturesAlert(@NonNull PreferencesFragment fragment, @NonNull Context context) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(getString(R.string.DisableExperimentalFeatures));
        alertDialogBuilder.setMessage(getString(R.string.DisableExperimentalFeatures_Text));
        alertDialogBuilder.setPositiveButton(getString(R.string.DisableExperimentalFeatures_Do), (v, d) -> {
            OctoConfig.INSTANCE.experimentsEnabled.updateValue(false);
            resetSettings();
            updateState();
            fragment.reloadUIAfterValueUpdate();
            v.dismiss();
            AppRestartHelper.triggerRebirth(context, new Intent(context, LaunchActivity.class));
        });
        alertDialogBuilder.setNegativeButton(getString(R.string.Cancel), null);
        return alertDialogBuilder;
    }

    private void updateState() {
        canShowDrawerProfileAsBubble.updateValue(OctoConfig.INSTANCE.experimentsEnabled.getValue() && OctoConfig.INSTANCE.drawerProfileAsBubble.getValue());
        canShowDownloadBoostLevel.updateValue(OctoConfig.INSTANCE.experimentsEnabled.getValue() && OctoConfig.INSTANCE.downloadBoost.getValue());
    }

    private void resetSettings() {
        OctoConfig.INSTANCE.useFluentNavigationBar.clear();
        OctoConfig.INSTANCE.moreHapticFeedbacks.clear();
        OctoConfig.INSTANCE.mediaInGroupCall.clear();
        OctoConfig.INSTANCE.roundedTextBox.clear();
        OctoConfig.INSTANCE.useSmoothContextMenuStyling.clear();
        OctoConfig.INSTANCE.showRPCErrors.clear();
        OctoConfig.INSTANCE.gcOutputType.clear();
        OctoConfig.INSTANCE.photoResolution.clear();
        OctoConfig.INSTANCE.maxRecentStickers.clear();
        OctoConfig.INSTANCE.forceUseIpV6.clear();
        OctoConfig.INSTANCE.deviceIdentifyState.clear();
        OctoConfig.INSTANCE.alternativeNavigation.clear();
        OctoConfig.INSTANCE.animatedActionBar.clear();
        OctoConfig.INSTANCE.navigationSmoothness.clear();
        OctoConfig.INSTANCE.navigationBounceLevel.clear();
        OctoConfig.INSTANCE.disableTelegramTabsStack.clear();
        OctoConfig.INSTANCE.hideOpenButtonChatsList.clear();
        OctoConfig.INSTANCE.alwaysExpandBlockQuotes.clear();
        OctoConfig.INSTANCE.profileBubbleMoreTopPadding.clear();
        OctoConfig.INSTANCE.profileBubbleHideBorder.clear();
        OctoConfig.INSTANCE.uploadBoost.clear();
        OctoConfig.INSTANCE.downloadBoost.clear();
        OctoConfig.INSTANCE.downloadBoostValue.clear();
    }

    private List<PopupChoiceDialogOption> buildMaxRecentStickersOptions() {
        List<PopupChoiceDialogOption> options = new ArrayList<>();
        options.add(new PopupChoiceDialogOption().setId(0).setItemTitle(getString(R.string.MaxStickerSizeDefault)));

        int[] values = {30, 40, 50, 80, 100, 120, 150, 180, 200};
        for (int i = 0; i < values.length; i++) {
            options.add(new PopupChoiceDialogOption().setId(i + 1).setItemTitle(String.valueOf(values[i])));
        }

        options.add(new PopupChoiceDialogOption().setId(values.length + 1).setItemTitle(getString(R.string.MaxRecentSticker_Unlimited)));
        return options;
    }

    private boolean handleExperimentalAlert(Context context, PreferencesFragment fragment, ConfigProperty<Boolean> featureProperty, String messageRes) {
        if (!featureProperty.getValue()) {
            new AlertDialog.Builder(context, fragment.getResourceProvider())
                    .setTitle("Experimental Feature")
                    .setMessage(messageRes)
                    .setPositiveButton(getString(R.string.OK), (d, w) -> {
                        featureProperty.setValue(true);
                        fragment.reloadUIAfterValueUpdate();
                    })
                    .setNegativeButton(getString(android.R.string.cancel), null)
                    .show();
            return false;
        }
        return true;
    }
}
