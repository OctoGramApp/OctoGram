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
import android.os.Build;
import android.util.Pair;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.ui.ActionBar.AlertDialog;
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
import it.octogram.android.deeplink.DeepLinkDef;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.rows.impl.HeaderRow;
import it.octogram.android.preferences.rows.impl.ListRow;
import it.octogram.android.preferences.rows.impl.ShadowRow;
import it.octogram.android.preferences.rows.impl.SliderChooseRow;
import it.octogram.android.preferences.rows.impl.SwitchRow;
import it.octogram.android.preferences.rows.impl.TextIconRow;
import it.octogram.android.preferences.ui.custom.AllowExperimentalBottomSheet;
import it.octogram.android.theme.MonetIconController;
import it.octogram.android.utils.PopupChoiceDialogOption;

public class OctoExperimentsUI implements PreferencesEntry {
    private final ConfigProperty<Boolean> canShowMonetIconSwitch = new ConfigProperty<>(null, Build.VERSION.SDK_INT == Build.VERSION_CODES.S || Build.VERSION.SDK_INT == Build.VERSION_CODES.S_V2);
    private final ConfigProperty<Boolean> isMonetSelected = new ConfigProperty<>(null, MonetIconController.INSTANCE.isSelectedMonet());

    @NonNull
    @Override
    public OctoPreferences getPreferences(@NonNull PreferencesFragment fragment, @NonNull Context context) {
        return OctoPreferences.builder(getString(R.string.Experiments))
                .deepLink(DeepLinkDef.EXPERIMENTAL)
                .addContextMenuItem(new OctoPreferences.OctoContextMenuElement(R.drawable.msg_reset, getString(R.string.ResetSettings), () -> OctoMainSettingsUI.openResetSettingsProcedure(context, true)).asDanger())
                .sticker(context, OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME, StickerUi.EXPERIMENTAL, true, getString(R.string.OctoExperimentsSettingsHeader))
                .category(getString(R.string.ExperimentalSettings), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkExperimentsEnabled(context))
                            .preferenceValue(OctoConfig.INSTANCE.mediaInGroupCall)
                            .title(getString(R.string.MediaStream))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkExperimentsEnabled(context))
                            .preferenceValue(OctoConfig.INSTANCE.showRPCErrors)
                            .title(getString(R.string.ShowRPCErrors))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .onClick(() -> checkExperimentsEnabled(context))
                            .options(List.of(
                                    new PopupChoiceDialogOption().setId(AudioType.MONO.getValue()).setItemTitle(getString(R.string.AudioTypeMono)),
                                    new PopupChoiceDialogOption().setId(AudioType.STEREO.getValue()).setItemTitle(getString(R.string.AudioTypeStereo))
                            ))
                            .currentValue(OctoConfig.INSTANCE.gcOutputType)
                            .title(getString(R.string.AudioTypeInCall))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .onClick(() -> checkExperimentsEnabled(context))
                            .options(List.of(
                                    new PopupChoiceDialogOption().setId(PhotoResolution.LOW.getValue()).setItemTitle(getString(R.string.ResolutionLow)),
                                    new PopupChoiceDialogOption().setId(PhotoResolution.DEFAULT.getValue()).setItemTitle(getString(R.string.ResolutionMedium)),
                                    new PopupChoiceDialogOption().setId(PhotoResolution.HIGH.getValue()).setItemTitle(getString(R.string.ResolutionHigh))
                            ))
                            .currentValue(OctoConfig.INSTANCE.photoResolution)
                            .title(getString(R.string.PhotoResolution))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .onClick(() -> checkExperimentsEnabled(context))
                            .currentValue(OctoConfig.INSTANCE.maxRecentStickers)
                            .options(List.of(
                                    new PopupChoiceDialogOption().setId(0).setItemTitle(getString(R.string.MaxStickerSizeDefault)),
                                    new PopupChoiceDialogOption().setId(1).setItemTitle("30"),
                                    new PopupChoiceDialogOption().setId(2).setItemTitle("40"),
                                    new PopupChoiceDialogOption().setId(3).setItemTitle("50"),
                                    new PopupChoiceDialogOption().setId(4).setItemTitle("80"),
                                    new PopupChoiceDialogOption().setId(5).setItemTitle("100"),
                                    new PopupChoiceDialogOption().setId(6).setItemTitle("120"),
                                    new PopupChoiceDialogOption().setId(7).setItemTitle("150"),
                                    new PopupChoiceDialogOption().setId(8).setItemTitle("180"),
                                    new PopupChoiceDialogOption().setId(9).setItemTitle("200"),
                                    new PopupChoiceDialogOption().setId(10).setItemTitle(R.string.MaxRecentSticker_Unlimited)
                            ))
                            .title(getString(R.string.MaxRecentStickers))
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
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .onClick(() -> checkExperimentsEnabled(context))
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
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> {
                                if (checkExperimentsEnabled(context)) {
                                    fragment.presentFragment(new NavigationSettingsUI());
                                }
                            })
                            .value(getString(OctoConfig.INSTANCE.alternativeNavigation.getValue() ? R.string.NotificationsOn : R.string.NotificationsOff))
                            .title(getString(R.string.AlternativeNavigation))
                            .build()
                    );
                })
                .row(new SwitchRow.SwitchRowBuilder()
                        .onClick(() -> {
                            if (!checkExperimentsEnabled(context)) return false;
                            MonetIconController.INSTANCE.switchToMonet();
                            var progressDialog = new AlertDialog(context, 3);
                            progressDialog.show();
                            AndroidUtilities.runOnUIThread(progressDialog::dismiss, 2000);
                            return true;
                        })
                        .preferenceValue(isMonetSelected)
                        .showIf(canShowMonetIconSwitch)
                        .title(getString(R.string.MonetIcon))
                        .description(getString(R.string.MonetIconDesc))
                        .build())
                .row(new ShadowRow(canShowMonetIconSwitch))
                .category(getString(R.string.Chats), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkExperimentsEnabled(context))
                            .preferenceValue(OctoConfig.INSTANCE.hideBottomBarChannels)
                            .title(getString(R.string.HideBottomBarChannels))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkExperimentsEnabled(context))
                            .onPostUpdate(fragment::rebuildAllFragmentsWithLast)
                            .preferenceValue(OctoConfig.INSTANCE.hideOpenButtonChatsList)
                            .title(getString(R.string.HideOpenButtonChatsList))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkExperimentsEnabled(context))
                            .preferenceValue(OctoConfig.INSTANCE.alwaysExpandBlockQuotes)
                            .title(getString(R.string.AlwaysExpandBlockQuotes))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkExperimentsEnabled(context))
                            .preferenceValue(OctoConfig.INSTANCE.enableQuickShare)
                            .title(getString(R.string.QuickShareEnabled))
                            .description(getString(R.string.QuickShareEnabled_Desc))
                            .build());
                })
                .category(getString(R.string.DrawerHeaderAsBubble), OctoConfig.INSTANCE.drawerProfileAsBubble, category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> AndroidUtilities.runOnUIThread(() -> LaunchActivity.instance.reloadDrawerHeader()))
                            .onClick(() -> checkExperimentsEnabled(context))
                            .preferenceValue(OctoConfig.INSTANCE.profileBubbleHideBorder)
                            .title(getString(R.string.ProfileBubbleHideBorder))
                            .showIf(OctoConfig.INSTANCE.drawerProfileAsBubble)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> AndroidUtilities.runOnUIThread(() -> LaunchActivity.instance.reloadDrawerHeader()))
                            .onClick(() -> checkExperimentsEnabled(context))
                            .preferenceValue(OctoConfig.INSTANCE.profileBubbleMoreTopPadding)
                            .title(getString(R.string.ProfileBubbleMoreTopPadding))
                            .showIf(OctoConfig.INSTANCE.drawerProfileAsBubble)
                            .build());
                })
                .category(getString(R.string.DownloadAndUploadBoost), category -> {
                    category.row(new ListRow.ListRowBuilder()
                            .onClick(() -> checkExperimentsEnabled(context))
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
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkExperimentsEnabled(context))
                            .preferenceValue(OctoConfig.INSTANCE.uploadBoost)
                            .title(getString(R.string.UploadBoost))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkExperimentsEnabled(context))
                            .preferenceValue(OctoConfig.INSTANCE.downloadBoost)
                            .title(getString(R.string.DownloadBoost))
                            .build());
                    category.row(new HeaderRow(getString(R.string.DownloadBoostType), OctoConfig.INSTANCE.downloadBoost).headerStyle(false));
                    category.row(new SliderChooseRow.SliderChooseRowBuilder()
                            .options(new ArrayList<>() {{
                                add(new Pair<>(0, getString(R.string.Default)));
                                add(new Pair<>(1, getString(R.string.Fast)));
                                add(new Pair<>(2, getString(R.string.Extreme)));
                            }})
                            .preferenceValue(OctoConfig.INSTANCE.downloadBoostValue)
                            .showIf(OctoConfig.INSTANCE.downloadBoost)
                            .build());
                })
                .build();
    }

    public static void resetSettings() {
        OctoConfig.INSTANCE.experimentsEnabled.clear();
        OctoConfig.INSTANCE.mediaInGroupCall.clear();
        OctoConfig.INSTANCE.showRPCErrors.clear();
        OctoConfig.INSTANCE.gcOutputType.clear();
        OctoConfig.INSTANCE.photoResolution.clear();
        OctoConfig.INSTANCE.maxRecentStickers.clear();
        OctoConfig.INSTANCE.forceUseIpV6.clear();
        OctoConfig.INSTANCE.deviceIdentifyState.clear();
        OctoConfig.INSTANCE.alternativeNavigation.clear();
        OctoConfig.INSTANCE.animatedActionBar.clear();
        OctoConfig.INSTANCE.navigationSmoothness.clear();
        OctoConfig.INSTANCE.hideBottomBarChannels.clear();
        OctoConfig.INSTANCE.hideOpenButtonChatsList.clear();
        OctoConfig.INSTANCE.alwaysExpandBlockQuotes.clear();
        OctoConfig.INSTANCE.profileBubbleMoreTopPadding.clear();
        OctoConfig.INSTANCE.profileBubbleHideBorder.clear();
        OctoConfig.INSTANCE.uploadBoost.clear();
        OctoConfig.INSTANCE.downloadBoost.clear();
        OctoConfig.INSTANCE.downloadBoostValue.clear();
        OctoConfig.INSTANCE.enableQuickShare.clear();
    }

    public static boolean checkExperimentsEnabled(Context context) {
        if (OctoConfig.INSTANCE.experimentsEnabled.getValue()) return true;
        var bottomSheet = new AllowExperimentalBottomSheet(context);
        bottomSheet.show();
        return OctoConfig.INSTANCE.experimentsEnabled.getValue();
    }
}
