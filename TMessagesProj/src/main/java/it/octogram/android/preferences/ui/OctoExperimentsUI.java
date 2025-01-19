/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui;

import android.content.Context;
import android.os.Build;
import android.util.Pair;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
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
import it.octogram.android.StickerUi;
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

    @Override
    public OctoPreferences getPreferences(PreferencesFragment fragment, Context context) {
        return OctoPreferences.builder(LocaleController.getString(R.string.Experiments))
                .addContextMenuItem(new OctoPreferences.OctoContextMenuElement(R.drawable.msg_reset, LocaleController.getString(R.string.ResetSettings), () -> OctoMainSettingsUI.openResetSettingsProcedure(context, true)))
                .sticker(context, OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME, StickerUi.EXPERIMENTAL, true, LocaleController.getString(R.string.OctoExperimentsSettingsHeader))
                .category(LocaleController.getString(R.string.ExperimentalSettings), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkExperimentsEnabled(context))
                            .preferenceValue(OctoConfig.INSTANCE.mediaInGroupCall)
                            .title(LocaleController.getString(R.string.MediaStream))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkExperimentsEnabled(context))
                            .preferenceValue(OctoConfig.INSTANCE.showRPCErrors)
                            .title(LocaleController.getString(R.string.ShowRPCErrors))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .onClick(() -> checkExperimentsEnabled(context))
                            .options(List.of(
                                    new PopupChoiceDialogOption().setId(AudioType.MONO.getValue()).setItemTitle(LocaleController.getString(R.string.AudioTypeMono)),
                                    new PopupChoiceDialogOption().setId(AudioType.STEREO.getValue()).setItemTitle(LocaleController.getString(R.string.AudioTypeStereo))
                            ))
                            .currentValue(OctoConfig.INSTANCE.gcOutputType)
                            .title(LocaleController.getString(R.string.AudioTypeInCall))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .onClick(() -> checkExperimentsEnabled(context))
                            .options(List.of(
                                    new PopupChoiceDialogOption().setId(PhotoResolution.LOW.getValue()).setItemTitle(LocaleController.getString(R.string.ResolutionLow)),
                                    new PopupChoiceDialogOption().setId(PhotoResolution.DEFAULT.getValue()).setItemTitle(LocaleController.getString(R.string.ResolutionMedium)),
                                    new PopupChoiceDialogOption().setId(PhotoResolution.HIGH.getValue()).setItemTitle(LocaleController.getString(R.string.ResolutionHigh))
                            ))
                            .currentValue(OctoConfig.INSTANCE.photoResolution)
                            .title(LocaleController.getString(R.string.PhotoResolution))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .onClick(() -> checkExperimentsEnabled(context))
                            .currentValue(OctoConfig.INSTANCE.maxRecentStickers)
                            .options(List.of(
                                    new PopupChoiceDialogOption().setId(0).setItemTitle(LocaleController.getString(R.string.MaxStickerSizeDefault)),
                                    new PopupChoiceDialogOption().setId(1).setItemTitle("30"),
                                    new PopupChoiceDialogOption().setId(2).setItemTitle("40"),
                                    new PopupChoiceDialogOption().setId(3).setItemTitle("50"),
                                    new PopupChoiceDialogOption().setId(4).setItemTitle("80"),
                                    new PopupChoiceDialogOption().setId(5).setItemTitle("100"),
                                    new PopupChoiceDialogOption().setId(6).setItemTitle("120"),
                                    new PopupChoiceDialogOption().setId(7).setItemTitle("150"),
                                    new PopupChoiceDialogOption().setId(8).setItemTitle("180"),
                                    new PopupChoiceDialogOption().setId(9).setItemTitle("200"),
                                    new PopupChoiceDialogOption().setId(10).setItemTitle("Unlimited")
                            ))
                            .title(LocaleController.getString(R.string.MaxRecentStickers))
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
                            .title(LocaleController.getString(R.string.TryConnectWithIPV6))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .onClick(() -> checkExperimentsEnabled(context))
                            .currentValue(OctoConfig.INSTANCE.deviceIdentifyState)
                            .onSelected(fragment::showRestartTooltip)
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(DeviceIdentifyState.DEFAULT.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.DeviceIdentifyDefault)),
                                    new PopupChoiceDialogOption()
                                            .setId(DeviceIdentifyState.FORCE_TABLET.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.DeviceIdentifyTablet))
                                            .setItemDescription(LocaleController.getString(R.string.DeviceIdentifyTabletDesc)),
                                    new PopupChoiceDialogOption()
                                            .setId(DeviceIdentifyState.FORCE_SMARTPHONE.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.DeviceIdentifySmartphone))
                                            .setItemDescription(LocaleController.getString(R.string.DeviceIdentifySmartphoneDesc))

                            ))
                            .title(LocaleController.getString(R.string.DeviceIdentifyStatus))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> {
                                if (checkExperimentsEnabled(context)) {
                                    fragment.presentFragment(new NavigationSettingsUI());
                                }
                            })
                            .value(LocaleController.getString(OctoConfig.INSTANCE.alternativeNavigation.getValue() ? R.string.NotificationsOn : R.string.NotificationsOff))
                            .title(LocaleController.getString(R.string.AlternativeNavigation))
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
                        .title(LocaleController.getString(R.string.MonetIcon))
                        .description(LocaleController.getString(R.string.MonetIconDesc))
                        .build())
                .row(new ShadowRow(canShowMonetIconSwitch))
                .category(LocaleController.getString(R.string.Chats), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkExperimentsEnabled(context))
                            .preferenceValue(OctoConfig.INSTANCE.hideBottomBarChannels)
                            .title(LocaleController.getString(R.string.HideBottomBarChannels))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkExperimentsEnabled(context))
                            .onPostUpdate(fragment::rebuildAllFragmentsWithLast)
                            .preferenceValue(OctoConfig.INSTANCE.hideOpenButtonChatsList)
                            .title(LocaleController.getString(R.string.HideOpenButtonChatsList))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkExperimentsEnabled(context))
                            .preferenceValue(OctoConfig.INSTANCE.alwaysExpandBlockQuotes)
                            .title(LocaleController.getString(R.string.AlwaysExpandBlockQuotes))
                            .build());
                })
                .category(LocaleController.getString(R.string.DrawerHeaderAsBubble), OctoConfig.INSTANCE.drawerProfileAsBubble, category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> AndroidUtilities.runOnUIThread(() -> LaunchActivity.instance.reloadDrawerHeader()))
                            .onClick(() -> checkExperimentsEnabled(context))
                            .preferenceValue(OctoConfig.INSTANCE.profileBubbleHideBorder)
                            .title(LocaleController.getString(R.string.ProfileBubbleHideBorder))
                            .showIf(OctoConfig.INSTANCE.drawerProfileAsBubble)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> AndroidUtilities.runOnUIThread(() -> LaunchActivity.instance.reloadDrawerHeader()))
                            .onClick(() -> checkExperimentsEnabled(context))
                            .preferenceValue(OctoConfig.INSTANCE.profileBubbleMoreTopPadding)
                            .title(LocaleController.getString(R.string.ProfileBubbleMoreTopPadding))
                            .showIf(OctoConfig.INSTANCE.drawerProfileAsBubble)
                            .build());
                })
                .category(LocaleController.getString(R.string.DownloadAndUploadBoost), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkExperimentsEnabled(context))
                            .preferenceValue(OctoConfig.INSTANCE.uploadBoost)
                            .title(LocaleController.getString(R.string.UploadBoost))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> checkExperimentsEnabled(context))
                            .preferenceValue(OctoConfig.INSTANCE.downloadBoost)
                            .title(LocaleController.getString(R.string.DownloadBoost))
                            .build());
                    category.row(new HeaderRow(LocaleController.getString(R.string.DownloadBoostType), OctoConfig.INSTANCE.downloadBoost));
                    category.row(new SliderChooseRow.SliderChooseRowBuilder()
                            .options(new ArrayList<>() {{
                                add(new Pair<>(0, LocaleController.getString(R.string.Default)));
                                add(new Pair<>(1, LocaleController.getString(R.string.Fast)));
                                add(new Pair<>(2, LocaleController.getString(R.string.Extreme)));
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
    }

    public static boolean checkExperimentsEnabled(Context context) {
        if (OctoConfig.INSTANCE.experimentsEnabled.getValue()) return true;
        var bottomSheet = new AllowExperimentalBottomSheet(context);
        bottomSheet.show();
        return OctoConfig.INSTANCE.experimentsEnabled.getValue();
    }
}
