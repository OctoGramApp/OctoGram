/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */

package it.octogram.android.preferences.ui;

import android.content.Context;
import android.util.Pair;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;

import java.util.List;

import it.octogram.android.OctoConfig;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.rows.impl.FooterInformativeRow;
import it.octogram.android.preferences.rows.impl.ListRow;
import it.octogram.android.preferences.rows.impl.SwitchRow;
import kotlin.Triple;

public class OctoGeneralSettingsUI implements PreferencesEntry {

    @Override
    public OctoPreferences getPreferences(PreferencesFragment fragment, Context context) {
        return OctoPreferences.builder(LocaleController.formatString("OctoGeneralSettings", R.string.OctoGeneralSettings))
                .sticker(context, R.raw.utyan_umbrella, true, LocaleController.formatString("OctoGeneralSettingsHeader", R.string.OctoGeneralSettingsHeader))
                .category(LocaleController.formatString("PrivacyHeader", R.string.PrivacyHeader), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hidePhoneNumber)
                            .title(LocaleController.formatString("HidePhoneNumber", R.string.HidePhoneNumber))
                            .description(LocaleController.formatString("HidePhoneNumber_Desc", R.string.HidePhoneNumber_Desc))
                            .postNotificationName(NotificationCenter.reloadInterface, NotificationCenter.mainUserInfoChanged)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hideOtherPhoneNumber)
                            .title(LocaleController.formatString("HideOtherPhoneNumber", R.string.HideOtherPhoneNumber))
                            .description(LocaleController.formatString("HideOtherPhoneNumber_Desc", R.string.HideOtherPhoneNumber_Desc))
                            .showIf(OctoConfig.INSTANCE.hidePhoneNumber)
                            .postNotificationName(NotificationCenter.reloadInterface, NotificationCenter.mainUserInfoChanged)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.showFakePhoneNumber)
                            .title(LocaleController.formatString("ShowFakePhoneNumber", R.string.ShowFakePhoneNumber))
                            .description(LocaleController.formatString("ShowFakePhoneNumber_Desc", R.string.ShowFakePhoneNumber_Desc))
                            .showIf(OctoConfig.INSTANCE.hidePhoneNumber)
                            .postNotificationName(NotificationCenter.reloadInterface, NotificationCenter.mainUserInfoChanged)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.showUsernameAsPhoneNumber)
                            .title(LocaleController.formatString(R.string.ShowUsernameAsPhoneNumber))
                            .description(LocaleController.formatString("ShowUsernameAsPhoneNumber_Desc", R.string.ShowUsernameAsPhoneNumber_Desc))
                            .showIf(OctoConfig.INSTANCE.hidePhoneNumber)
                            .postNotificationName(NotificationCenter.reloadInterface, NotificationCenter.mainUserInfoChanged)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.promptBeforeCalling)
                            .title(LocaleController.formatString("PromptBeforeCalling", R.string.PromptBeforeCalling))
                            .description(LocaleController.formatString("PromptBeforeCalling_Desc", R.string.PromptBeforeCalling_Desc))
                            .build());
                })
                .category(LocaleController.formatString("DcIdHeader", R.string.DcIdHeader), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.registrationDateInProfiles)
                            .title(LocaleController.formatString("ShowRegistrationDate", R.string.ShowRegistrationDate))
                            .description(LocaleController.formatString("ShowRegistrationDate_Desc", R.string.ShowRegistrationDate_Desc))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .onClick(() -> {
                                fragment.getParentLayout().rebuildAllFragmentViews(false, false);
                                return true;
                            })
                            .currentValue(OctoConfig.INSTANCE.dcIdStyle)
                            .options(List.of(
                                    new Pair<>(OctoConfig.DcIdStyle.NONE, LocaleController.formatString("Nothing", R.string.Nothing)),
                                    new Pair<>(OctoConfig.DcIdStyle.OWLGRAM, "OwlGram"),
                                    new Pair<>(OctoConfig.DcIdStyle.TELEGRAM, "Telegram"),
                                    new Pair<>(OctoConfig.DcIdStyle.MINIMAL, "Minimal")
                            ))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .title(LocaleController.formatString("Style", R.string.Style))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.dcIdType)
                            .options(List.of(
                                    new Pair<>(OctoConfig.DcIdType.BOT_API, "Bot API"),
                                    new Pair<>(OctoConfig.DcIdType.TELEGRAM, "Telegram")
                            ))
                            .title(LocaleController.formatString("Type", R.string.Type))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                    category.row(new FooterInformativeRow.FooterInformativeRowBuilder()
                            .title(LocaleController.formatString("DcIdTypeDescription", R.string.DcIdTypeDescription))
                            .build());
                })
                .category(LocaleController.formatString("Chats", R.string.Chats), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.jumpToNextChannel)
                            .title(LocaleController.formatString("JumpToNextChannel", R.string.JumpToNextChannel))
                            .description(LocaleController.formatString("JumpToNextChannel_Desc", R.string.JumpToNextChannel_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hideGreetingSticker)
                            .title(LocaleController.formatString("HideGreetingSticker", R.string.HideGreetingSticker))
                            .description(LocaleController.formatString("HideGreetingSticker_Desc", R.string.HideGreetingSticker_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.playGifAsVideo)
                            .title(LocaleController.formatString("PlayGifsAsVideo", R.string.PlayGifsAsVideo))
                            .description(LocaleController.formatString("PlayGifsAsVideo_Desc", R.string.PlayGifsAsVideo_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hideKeyboardOnScroll)
                            .title(LocaleController.formatString("HideKeyboardOnScroll", R.string.HideKeyboardOnScroll))
                            .description(LocaleController.formatString("HideKeyboardOnScroll_Desc", R.string.HideKeyboardOnScroll_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hideSendAsChannel)
                            .title(LocaleController.formatString("HideSendAsChannel", R.string.HideSendAsChannel))
                            .description(LocaleController.formatString("HideSendAsChannel_Desc", R.string.HideSendAsChannel_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.showOnlineStatus)
                            .title(LocaleController.formatString("ShowOnlineStatus", R.string.ShowOnlineStatus))
                            .description(LocaleController.formatString("ShowOnlineStatus_Desc", R.string.ShowOnlineStatus_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hideCustomEmojis)
                            .title(LocaleController.formatString("HideCustomEmojis", R.string.HideCustomEmojis))
                            .description(LocaleController.formatString("HideCustomEmojis_Desc", R.string.HideCustomEmojis_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.openArchiveOnPull)
                            .title(LocaleController.formatString("OpenArchiveOnPull", R.string.OpenArchiveOnPull))
                            .description(LocaleController.formatString("OpenArchiveOnPull_Desc", R.string.OpenArchiveOnPull_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.warningBeforeDeletingChatHistory)
                            .title(LocaleController.formatString("WarningBeforeDeletingChatHistory", R.string.WarningBeforeDeletingChatHistory))
                            .description(LocaleController.formatString("WarningBeforeDeletingChatHistory_Desc", R.string.WarningBeforeDeletingChatHistory_Desc))
                            .build());
                })
                .category(LocaleController.formatString("MediaTab", R.string.MediaTab), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.activeNoiseSuppression)
                            .title(LocaleController.formatString("VoiceImprovements", R.string.VoiceImprovements))
                            .description(LocaleController.formatString("VoiceImprovements_Desc", R.string.VoiceImprovements_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.unmuteVideosWithVolumeDown)
                            .title(LocaleController.formatString("UnmuteWithVolumeDown", R.string.UnmuteWithVolumeDown))
                            .description(LocaleController.formatString("UnmuteWithVolumeDown_Desc", R.string.UnmuteWithVolumeDown_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.disableProximityEvents)
                            .title(LocaleController.formatString("DisableProximitySensor", R.string.DisableProximitySensor))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.startWithRearCamera)
                            .title(LocaleController.formatString("StartWithRearCamera", R.string.StartWithRearCamera))
                            .description(LocaleController.formatString("StartWithRearCamera_Desc", R.string.StartWithRearCamera_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.disableCameraPreview)
                            .title(LocaleController.formatString("DisableCameraPreview", R.string.DisableCameraPreview))
                            .description(LocaleController.formatString("DisableCameraPreview_Desc", R.string.DisableCameraPreview_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hideSentTimeOnStickers)
                            .title(LocaleController.formatString("RemoveTimeOnStickers", R.string.RemoveTimeOnStickers))
                            .build());
                })
                .category(LocaleController.formatString("FilterAvailableTitle", R.string.FilterAvailableTitle), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hideOnlyAllChatsFolder)
                            .premium(!UserConfig.getInstance(UserConfig.selectedAccount).isPremium())
                            .title(LocaleController.formatString("HideAllChatFolder", R.string.HideAllChatFolder))
                            .requiresRestart(true)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hideChatFolders)
                            .title(LocaleController.formatString("HideAllChatFolders", R.string.HideAllChatFolders))
                            .requiresRestart(true)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hideFoldersWhenForwarding)
                            .title(LocaleController.formatString("HideChatFoldersWhenForwarding", R.string.HideChatFoldersWhenForwarding))
                            .requiresRestart(true)
                            .build());
                })
                .category(LocaleController.getString(R.string.DoubleTapActionsHeader), category -> {
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.doubleTapAction)
                            .optionsIcons(List.of(
                                    new Triple<>(OctoConfig.DoubleTapAction.DISABLED, LocaleController.getString(R.string.Disable), R.drawable.msg_block),
                                    new Triple<>(OctoConfig.DoubleTapAction.REACTION, LocaleController.getString(R.string.Reaction), R.drawable.msg_emoji_cat),
                                    new Triple<>(OctoConfig.DoubleTapAction.COPY, LocaleController.getString(R.string.Copy), R.drawable.msg_copy),
                                    new Triple<>(OctoConfig.DoubleTapAction.FORWARD, LocaleController.getString(R.string.Forward), R.drawable.msg_forward),
                                    new Triple<>(OctoConfig.DoubleTapAction.REPLY, LocaleController.getString(R.string.Reply), R.drawable.msg_reply),
                                    new Triple<>(OctoConfig.DoubleTapAction.DELETE, LocaleController.getString(R.string.Delete), R.drawable.msg_delete),
                                    new Triple<>(OctoConfig.DoubleTapAction.SAVE, LocaleController.getString(R.string.Save), R.drawable.msg_saved)
                            ))
                            .title(LocaleController.getString(R.string.PreferredActionIncoming))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.doubleTapActionOut)
                            .optionsIcons(List.of(
                                    new Triple<>(OctoConfig.DoubleTapAction.DISABLED, LocaleController.getString(R.string.Disable), R.drawable.msg_block),
                                    new Triple<>(OctoConfig.DoubleTapAction.REACTION, LocaleController.getString(R.string.Reaction), R.drawable.msg_emoji_cat),
                                    new Triple<>(OctoConfig.DoubleTapAction.COPY, LocaleController.getString(R.string.Copy), R.drawable.msg_copy),
                                    new Triple<>(OctoConfig.DoubleTapAction.FORWARD, LocaleController.getString(R.string.Forward), R.drawable.msg_forward),
                                    new Triple<>(OctoConfig.DoubleTapAction.REPLY, LocaleController.getString(R.string.Reply), R.drawable.msg_reply),
                                    new Triple<>(OctoConfig.DoubleTapAction.DELETE, LocaleController.getString(R.string.Delete), R.drawable.msg_delete),
                                    new Triple<>(OctoConfig.DoubleTapAction.SAVE, LocaleController.getString(R.string.Save), R.drawable.msg_saved),
                                    new Triple<>(OctoConfig.DoubleTapAction.EDIT, LocaleController.getString(R.string.Edit), R.drawable.msg_edit)
                            ))
                            .title(LocaleController.getString(R.string.PreferredActionOutgoing))
                            .build());
//                    category.row(new TextDetailRow.TextDetailRowBuilder()
//                            .title(LocaleController.getString(R.string.CustomEmojiReaction))
//                            .description(LocaleController.getString("FeatureCurrentlyUnavailable", R.string.FeatureCurrentlyUnavailable))
//                            .build());
                })
                .category(LocaleController.getString("TabletModeSection", R.string.TabletModeSection), category -> category.row(new SwitchRow.SwitchRowBuilder()
                        .preferenceValue(OctoConfig.INSTANCE.tabletMode)
                        .title(LocaleController.getString("EnableTabletMode", R.string.ForceTableMode))
                        .requiresRestart(true)
                        .build()))
                .category(LocaleController.getString(R.string.ConnectionSection), category -> category.row(new SwitchRow.SwitchRowBuilder()
                        .preferenceValue(OctoConfig.INSTANCE.forceUseIpV6)
                        .title(LocaleController.getString(R.string.TryConnectWithIPV6))
                        .build()))
                .category(LocaleController.getString("Notifications", R.string.Notifications), category -> category.row(new SwitchRow.SwitchRowBuilder()
                        .preferenceValue(OctoConfig.INSTANCE.accentColorAsNotificationColor)
                        .title(LocaleController.getString("AccentColorAsNotificationColor", R.string.AccentColorAsNotificationColor))
                        .build()))
                .build();
    }

}
