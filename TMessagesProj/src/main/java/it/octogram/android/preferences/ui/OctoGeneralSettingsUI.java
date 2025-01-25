/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.preferences.ui;

import android.content.Context;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ReactionsDoubleTapManageActivity;

import java.util.List;
import java.util.stream.IntStream;

import it.octogram.android.ConfigProperty;
import it.octogram.android.DcIdStyle;
import it.octogram.android.DcIdType;
import it.octogram.android.DefaultEmojiButtonAction;
import it.octogram.android.DoubleTapAction;
import it.octogram.android.OctoConfig;
import it.octogram.android.PhoneNumberAlternative;
import it.octogram.android.StickerUi;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.rows.impl.CustomCellRow;
import it.octogram.android.preferences.rows.impl.ListRow;
import it.octogram.android.preferences.rows.impl.SwitchRow;
import it.octogram.android.preferences.rows.impl.TextIconRow;
import it.octogram.android.preferences.ui.custom.DcInfoSelector;
import it.octogram.android.utils.OctoUtils;
import it.octogram.android.utils.PopupChoiceDialogOption;

public class OctoGeneralSettingsUI implements PreferencesEntry {
    SwitchRow enableSmartNotificationsSwitchRow;
    private DcInfoSelector dcInfoSelector;

    @Override
    public OctoPreferences getPreferences(PreferencesFragment fragment, Context context) {
        ConfigProperty<Boolean> canShowPhoneNumberAlternative = new ConfigProperty<>(null, OctoConfig.INSTANCE.hidePhoneNumber.getValue() || OctoConfig.INSTANCE.hideOtherPhoneNumber.getValue());
        ConfigProperty<Boolean> canShowSelectReaction = new ConfigProperty<>(null, OctoConfig.INSTANCE.doubleTapAction.getValue() == DoubleTapAction.REACTION.getValue() || OctoConfig.INSTANCE.doubleTapActionOut.getValue() == DoubleTapAction.REACTION.getValue());
        ConfigProperty<Boolean> isDcIdVisible = new ConfigProperty<>(null, OctoConfig.INSTANCE.dcIdStyle.getValue() != DcIdStyle.NONE.getValue());

        return OctoPreferences.builder(LocaleController.getString(R.string.OctoGeneralSettings))
                .sticker(context, OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME, StickerUi.GENERAL, true, LocaleController.getString(R.string.OctoGeneralSettingsHeader))
                .category(LocaleController.getString(R.string.PrivacyHeader), category -> {
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
                .category(LocaleController.getString(R.string.Warnings), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.promptBeforeCalling)
                            .title(LocaleController.getString(R.string.PromptBeforeCalling))
                            .description(LocaleController.getString(R.string.PromptBeforeCalling_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.warningBeforeDeletingChatHistory)
                            .title(LocaleController.getString(R.string.PromptBeforeDeletingChatHistory))
                            .description(LocaleController.getString(R.string.PromptBeforeDeletingChatHistory_Desc))
                            .build());
                })
                .category(LocaleController.getString(R.string.DcIdHeader), category -> {
                    category.row(new CustomCellRow.CustomCellRowBuilder()
                            .layout(dcInfoSelector = new DcInfoSelector(context))
                            .showIf(isDcIdVisible)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.registrationDateInProfiles)
                            .title(LocaleController.getString(R.string.ShowRegistrationDate))
                            .description(LocaleController.getString(R.string.ShowRegistrationDate_Desc))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .onSelected(() -> {
                                isDcIdVisible.setValue(OctoConfig.INSTANCE.dcIdStyle.getValue() != DcIdStyle.NONE.getValue());
                                AndroidUtilities.runOnUIThread(() -> dcInfoSelector.update());
                            })
                            .currentValue(OctoConfig.INSTANCE.dcIdStyle)
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(DcIdStyle.NONE.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.Nothing))
                                            .setItemDescription(LocaleController.getString(R.string.DCStyleNothing_Desc)),
                                    new PopupChoiceDialogOption()
                                            .setId(DcIdStyle.OWLGRAM.getValue())
                                            .setItemTitle("OwlGram")
                                            .setItemDescription(LocaleController.getString(R.string.DCStyleOwlGram_Desc)),
                                    new PopupChoiceDialogOption()
                                            .setId(DcIdStyle.TELEGRAM.getValue())
                                            .setItemTitle("Telegram")
                                            .setItemDescription(LocaleController.getString(R.string.DCStyleTelegram_Desc)),
                                    new PopupChoiceDialogOption()
                                            .setId(DcIdStyle.MINIMAL.getValue())
                                            .setItemTitle("Minimal")
                                            .setItemDescription(LocaleController.getString(R.string.DCStyleMinimal_Desc))
                            ))
                            .title(LocaleController.getString(R.string.Style))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .onSelected(() -> dcInfoSelector.updateChatID())
                            .currentValue(OctoConfig.INSTANCE.dcIdType)
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(DcIdType.BOT_API.getValue())
                                            .setItemTitle("Bot API")
                                            .setItemDescription(LocaleController.getString(R.string.DcIdTypeDescriptionBotapi)),
                                    new PopupChoiceDialogOption()
                                            .setId(DcIdType.TELEGRAM.getValue())
                                            .setItemTitle("Telegram")
                                            .setItemDescription(LocaleController.getString(R.string.DcIdTypeDescriptionTelegram))
                            ))
                            .showIf(isDcIdVisible)
                            .title(LocaleController.getString(R.string.Type))
                            .build());
                })
                .category(LocaleController.getString(R.string.Chats), category -> {
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .icon(R.drawable.chats_pin)
                            .value(PinnedEmojisActivity.getRowDescription())
                            .onClick(() -> {
                                PinnedEmojisActivity activity = new PinnedEmojisActivity();
                                activity.setFragment(fragment);
                                fragment.presentFragment(activity);
                            })
                            .setDynamicDataUpdate(new TextIconRow.OnDynamicDataUpdate() {
                                @Override
                                public String getTitle() {
                                    return LocaleController.getString(R.string.PinnedEmojisList);
                                }

                                @Override
                                public String getValue() {
                                    return PinnedEmojisActivity.getRowDescription();
                                }
                            })
                            .title(LocaleController.getString(R.string.PinnedEmojisList))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .icon(R.drawable.msg2_reactions2)
                            .value(PinnedReactionsActivity.getRowDescription())
                            .onClick(() -> {
                                PinnedReactionsActivity activity = new PinnedReactionsActivity();
                                activity.setFragment(fragment);
                                fragment.presentFragment(activity);
                            })
                            .setDynamicDataUpdate(new TextIconRow.OnDynamicDataUpdate() {
                                @Override
                                public String getTitle() {
                                    return LocaleController.getString(R.string.PinnedReactions);
                                }

                                @Override
                                public String getValue() {
                                    return PinnedReactionsActivity.getRowDescription();
                                }
                            })
                            .title(LocaleController.getString(R.string.PinnedReactions))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.defaultEmojiButtonAction)
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(DefaultEmojiButtonAction.DEFAULT.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.DefaultEmojiButtonTypeDefault))
                                            .setItemIcon(R.drawable.msg_forward_replace),
                                    new PopupChoiceDialogOption()
                                            .setId(DefaultEmojiButtonAction.EMOJIS.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.Emoji))
                                            .setItemIcon(R.drawable.msg_emoji_smiles),
                                    new PopupChoiceDialogOption()
                                            .setId(DefaultEmojiButtonAction.STICKERS.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.AttachSticker))
                                            .setItemIcon(R.drawable.msg_sticker),
                                    new PopupChoiceDialogOption()
                                            .setId(DefaultEmojiButtonAction.GIFS.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.AttachGif))
                                            .setItemIcon(R.drawable.msg_gif)
                            ))
                            .title(LocaleController.getString(R.string.DefaultEmojiButtonType))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.jumpToNextChannelOrTopic)
                            .title(LocaleController.getString(R.string.JumpToNextChannelOrTopic))
                            .description(LocaleController.getString(R.string.JumpToNextChannelOrTopic_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.swipeToPip)
                            .title(LocaleController.getString(R.string.SwipeToPIP))
                            .description(LocaleController.getString(R.string.SwipeToPIP_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hideGreetingSticker)
                            .title(LocaleController.getString(R.string.HideGreetingSticker))
                            .description(LocaleController.getString(R.string.HideGreetingSticker_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hideKeyboardOnScroll)
                            .title(LocaleController.getString(R.string.HideKeyboardOnScroll))
                            .description(LocaleController.getString(R.string.HideKeyboardOnScroll_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hideSendAsChannel)
                            .title(LocaleController.getString(R.string.HideSendAsChannel))
                            .description(LocaleController.getString(R.string.HideSendAsChannel_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.showOnlineStatus)
                            .title(LocaleController.getString(R.string.ShowOnlineStatus))
                            .description(LocaleController.getString(R.string.ShowOnlineStatus_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hideCustomEmojis)
                            .title(LocaleController.getString(R.string.HideCustomEmojis))
                            .description(LocaleController.getString(R.string.HideCustomEmojis_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.openArchiveOnPull)
                            .title(LocaleController.getString(R.string.OpenArchiveOnPull))
                            .description(LocaleController.getString(R.string.OpenArchiveOnPull_Desc))
                            .build());
                })
                .category(LocaleController.getString(R.string.MediaTab), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.activeNoiseSuppression)
                            .title(LocaleController.getString(R.string.VoiceImprovements))
                            .description(LocaleController.getString(R.string.VoiceImprovements_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.playGifAsVideo)
                            .title(LocaleController.getString(R.string.PlayGifsAsVideo))
                            .description(LocaleController.getString(R.string.PlayGifsAsVideo_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.unmuteVideosWithVolumeDown)
                            .title(LocaleController.getString(R.string.UnmuteWithVolumeDown))
                            .description(LocaleController.getString(R.string.UnmuteWithVolumeDown_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.disableProximityEvents)
                            .title(LocaleController.getString(R.string.DisableProximitySensor))
                            .build());
                })
                .category(LocaleController.getString(R.string.FilterAvailableTitle), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> NotificationCenter.getInstance(UserConfig.selectedAccount).postNotificationName(NotificationCenter.dialogFiltersUpdated))
                            // use account-specific notification center instead of global used by postNotificationName
                            .preferenceValue(OctoConfig.INSTANCE.hideChatFolders)
                            .title(LocaleController.getString(R.string.HideAllChatFolders))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> NotificationCenter.getInstance(UserConfig.selectedAccount).postNotificationName(NotificationCenter.dialogFiltersUpdated))
                            // use account-specific notification center instead of global used by postNotificationName
                            .preferenceValue(OctoConfig.INSTANCE.hideOnlyAllChatsFolder)
                            .title(LocaleController.getString(R.string.HideAllChatFolder))
                            .showIf(OctoConfig.INSTANCE.hideChatFolders, true)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hideFoldersWhenForwarding)
                            .title(LocaleController.getString(R.string.HideChatFoldersWhenForwarding))
                            .showIf(OctoConfig.INSTANCE.hideChatFolders, true)
                            .build());
                })
                .category(LocaleController.getString(R.string.DoubleTapActionsHeader), category -> {
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.doubleTapAction)
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(DoubleTapAction.DISABLED.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.Disable))
                                            .setItemIcon(R.drawable.msg_block),
                                    new PopupChoiceDialogOption()
                                            .setId(DoubleTapAction.REACTION.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.Reaction))
                                            .setItemIcon(OctoUtils.getPetIconFixed()),
                                    new PopupChoiceDialogOption()
                                            .setId(DoubleTapAction.COPY.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.Copy))
                                            .setItemIcon(R.drawable.msg_copy),
                                    new PopupChoiceDialogOption()
                                            .setId(DoubleTapAction.FORWARD.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.Forward))
                                            .setItemIcon(R.drawable.msg_forward),
                                    new PopupChoiceDialogOption()
                                            .setId(DoubleTapAction.REPLY.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.Reply))
                                            .setItemIcon(R.drawable.menu_reply),
                                    new PopupChoiceDialogOption()
                                            .setId(DoubleTapAction.DELETE.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.Delete))
                                            .setItemIcon(R.drawable.msg_delete),
                                    new PopupChoiceDialogOption()
                                            .setId(DoubleTapAction.SAVE.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.Save))
                                            .setItemIcon(R.drawable.msg_saved)
                            ))
                            .onSelected(() -> canShowSelectReaction.setValue(OctoConfig.INSTANCE.doubleTapAction.getValue() == DoubleTapAction.REACTION.getValue() || OctoConfig.INSTANCE.doubleTapActionOut.getValue() == DoubleTapAction.REACTION.getValue()))
                            .title(LocaleController.getString(R.string.PreferredActionIncoming))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.doubleTapActionOut)
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(DoubleTapAction.DISABLED.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.Disable))
                                            .setItemIcon(R.drawable.msg_block),
                                    new PopupChoiceDialogOption()
                                            .setId(DoubleTapAction.REACTION.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.Reaction))
                                            .setItemIcon(OctoUtils.getPetIconFixed()),
                                    new PopupChoiceDialogOption()
                                            .setId(DoubleTapAction.COPY.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.Copy))
                                            .setItemIcon(R.drawable.msg_copy),
                                    new PopupChoiceDialogOption()
                                            .setId(DoubleTapAction.FORWARD.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.Forward))
                                            .setItemIcon(R.drawable.msg_forward),
                                    new PopupChoiceDialogOption()
                                            .setId(DoubleTapAction.REPLY.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.Reply))
                                            .setItemIcon(R.drawable.menu_reply),
                                    new PopupChoiceDialogOption()
                                            .setId(DoubleTapAction.DELETE.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.Delete))
                                            .setItemIcon(R.drawable.msg_delete),
                                    new PopupChoiceDialogOption()
                                            .setId(DoubleTapAction.SAVE.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.Save))
                                            .setItemIcon(R.drawable.msg_saved),
                                    new PopupChoiceDialogOption()
                                            .setId(DoubleTapAction.EDIT.getValue())
                                            .setItemTitle(LocaleController.getString(R.string.Edit))
                                            .setItemIcon(R.drawable.msg_edit)
                            ))
                            .onSelected(() -> canShowSelectReaction.setValue(OctoConfig.INSTANCE.doubleTapAction.getValue() == DoubleTapAction.REACTION.getValue() || OctoConfig.INSTANCE.doubleTapActionOut.getValue() == DoubleTapAction.REACTION.getValue()))
                            .title(LocaleController.getString(R.string.PreferredActionOutgoing))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .icon(OctoUtils.getPetIconFixed())
                            .onClick(() -> fragment.presentFragment(new ReactionsDoubleTapManageActivity()))
                            .showIf(canShowSelectReaction)
                            .title(LocaleController.getString(R.string.CustomEmojiReaction))
                            .build());
                })
                .category(LocaleController.getString(R.string.Replies), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.rememberAllRepliesMessage)
                            .title(LocaleController.getString(R.string.ReplyTracking))
                            .description(LocaleController.getString(R.string.ReplyTracking_Desc))
                            .build());
                })
                .category(LocaleController.getString(R.string.Notifications), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.accentColorAsNotificationColor)
                            .title(LocaleController.getString(R.string.AccentColorAsNotificationColor))
                            .build());
                    category.row(enableSmartNotificationsSwitchRow = new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> {
                                checkSmartNotificationsEnabled(fragment);
                                return true;
                            })
                            .preferenceValue(OctoConfig.INSTANCE.enableSmartNotificationsForPrivateChats)
                            .title(LocaleController.getString(R.string.EnableSmartNotificationsForPrivateChats))
                            .build());
                })
                .build();
    }

    private boolean hasPremiumAccount() {
        return IntStream.range(0, UserConfig.MAX_ACCOUNT_COUNT)
                .mapToObj(UserConfig::getInstance)
                .anyMatch(instance -> instance.isClientActivated() && instance.isPremium());
    }

    private void checkSmartNotificationsEnabled(PreferencesFragment fragment) {
        if (OctoConfig.INSTANCE.enableSmartNotificationsForPrivateChats.getValue()) {
            return;
        }

        new AlertDialog.Builder(fragment.getContext())
                .setTitle(LocaleController.getString(R.string.Warning))
                .setMessage(LocaleController.getString(R.string.SmartNotificationsPvtDialogMessage))
                .setPositiveButton(LocaleController.getString(R.string.OK), (dialog, which) -> dialog.dismiss())
                .show();
    }

}
