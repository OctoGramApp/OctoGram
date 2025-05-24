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

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ReactionsDoubleTapManageActivity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import it.octogram.android.ConfigProperty;
import it.octogram.android.DcIdStyle;
import it.octogram.android.DcIdType;
import it.octogram.android.DefaultEmojiButtonAction;
import it.octogram.android.DoubleTapAction;
import it.octogram.android.OctoConfig;
import it.octogram.android.StickerUi;
import it.octogram.android.deeplink.DeepLinkDef;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.rows.impl.CustomCellRow;
import it.octogram.android.preferences.rows.impl.ListRow;
import it.octogram.android.preferences.rows.impl.SwitchRow;
import it.octogram.android.preferences.rows.impl.TextIconRow;
import it.octogram.android.preferences.ui.custom.DcInfoSelector;
import it.octogram.android.utils.OctoUtils;
import it.octogram.android.utils.appearance.PopupChoiceDialogOption;


public class OctoGeneralSettingsUI implements PreferencesEntry {
    SwitchRow enableSmartNotificationsSwitchRow;
    private DcInfoSelector dcInfoSelector;

    @NonNull
    @Override
    public OctoPreferences getPreferences(@NonNull PreferencesFragment fragment, @NonNull Context context) {
        ConfigProperty<Boolean> canShowSelectReaction = new ConfigProperty<>(null, OctoConfig.INSTANCE.doubleTapAction.getValue() == DoubleTapAction.REACTION.getValue() || OctoConfig.INSTANCE.doubleTapActionOut.getValue() == DoubleTapAction.REACTION.getValue());
        ConfigProperty<Boolean> isDcIdVisible = new ConfigProperty<>(null, OctoConfig.INSTANCE.dcIdStyle.getValue() != DcIdStyle.NONE.getValue());

        return OctoPreferences.builder(getString(R.string.OctoGeneralSettings))
                .deepLink(DeepLinkDef.GENERAL)
                .sticker(context, OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME, StickerUi.GENERAL, true, getString(R.string.OctoGeneralSettingsHeader))
                .category(getString(R.string.DcIdHeader), category -> {
                    category.row(new CustomCellRow.CustomCellRowBuilder()
                            .layout(dcInfoSelector = new DcInfoSelector(context, fragment.getResourceProvider()))
                            .showIf(isDcIdVisible)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.registrationDateInProfiles)
                            .title(getString(R.string.ShowRegistrationDate))
                            .description(getString(R.string.ShowRegistrationDate_Desc))
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
                                            .setItemTitle(getString(R.string.Nothing))
                                            .setItemDescription(getString(R.string.DCStyleNothing_Desc)),
                                    new PopupChoiceDialogOption()
                                            .setId(DcIdStyle.OWLGRAM.getValue())
                                            .setItemTitle("OwlGram")
                                            .setItemDescription(getString(R.string.DCStyleOwlGram_Desc)),
                                    new PopupChoiceDialogOption()
                                            .setId(DcIdStyle.TELEGRAM.getValue())
                                            .setItemTitle("Telegram")
                                            .setItemDescription(getString(R.string.DCStyleTelegram_Desc)),
                                    new PopupChoiceDialogOption()
                                            .setId(DcIdStyle.MINIMAL.getValue())
                                            .setItemTitle("Minimal")
                                            .setItemDescription(getString(R.string.DCStyleMinimal_Desc))
                            ))
                            .title(getString(R.string.Style))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .onSelected(() -> dcInfoSelector.updateChatID())
                            .currentValue(OctoConfig.INSTANCE.dcIdType)
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(DcIdType.BOT_API.getValue())
                                            .setItemTitle("Bot API")
                                            .setItemDescription(getString(R.string.DcIdTypeDescriptionBotapi)),
                                    new PopupChoiceDialogOption()
                                            .setId(DcIdType.TELEGRAM.getValue())
                                            .setItemTitle("Telegram")
                                            .setItemDescription(getString(R.string.DcIdTypeDescriptionTelegram))
                            ))
                            .showIf(isDcIdVisible)
                            .title(getString(R.string.Type))
                            .build());
                })
                .category(getString(R.string.Chats), category -> {
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .icon(R.drawable.chats_pin)
                            .value(PinnedEmojisActivity.getRowDescription())
                            .propertySelectionTag("pinnedEmojis")
                            .onClick(() -> {
                                PinnedEmojisActivity activity = new PinnedEmojisActivity();
                                activity.setFragment(fragment);
                                fragment.presentFragment(activity);
                            })
                            .setDynamicDataUpdate(new TextIconRow.OnDynamicDataUpdate() {
                                @Override
                                public String getTitle() {
                                    return getString(R.string.PinnedEmojisList);
                                }

                                @Override
                                public String getValue() {
                                    return PinnedEmojisActivity.getRowDescription();
                                }
                            })
                            .title(getString(R.string.PinnedEmojisList))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .icon(R.drawable.msg2_reactions2)
                            .value(PinnedReactionsActivity.getRowDescription())
                            .propertySelectionTag("pinnedReactions")
                            .onClick(() -> {
                                PinnedReactionsActivity activity = new PinnedReactionsActivity();
                                activity.setFragment(fragment);
                                fragment.presentFragment(activity);
                            })
                            .setDynamicDataUpdate(new TextIconRow.OnDynamicDataUpdate() {
                                @Override
                                public String getTitle() {
                                    return getString(R.string.PinnedReactions);
                                }

                                @Override
                                public String getValue() {
                                    return PinnedReactionsActivity.getRowDescription();
                                }
                            })
                            .title(getString(R.string.PinnedReactions))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .icon(R.drawable.menu_hashtag)
                            .value(PinnedHashtagsActivity.getRowDescription())
                            .propertySelectionTag("pinnedHashtags")
                            .onClick(() -> {
                                PinnedHashtagsActivity activity = new PinnedHashtagsActivity();
                                activity.setFragment(fragment);
                                fragment.presentFragment(activity);
                            })
                            .setDynamicDataUpdate(new TextIconRow.OnDynamicDataUpdate() {
                                @Override
                                public String getTitle() {
                                    return getString(R.string.PinnedHashtags);
                                }

                                @Override
                                public String getValue() {
                                    return PinnedHashtagsActivity.getRowDescription();
                                }
                            })
                            .title(getString(R.string.PinnedHashtags))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.defaultEmojiButtonAction)
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(DefaultEmojiButtonAction.DEFAULT.getValue())
                                            .setItemTitle(getString(R.string.DefaultEmojiButtonTypeDefault))
                                            .setItemIcon(R.drawable.msg_forward_replace),
                                    new PopupChoiceDialogOption()
                                            .setId(DefaultEmojiButtonAction.EMOJIS.getValue())
                                            .setItemTitle(getString(R.string.Emoji))
                                            .setItemIcon(R.drawable.msg_emoji_smiles),
                                    new PopupChoiceDialogOption()
                                            .setId(DefaultEmojiButtonAction.STICKERS.getValue())
                                            .setItemTitle(getString(R.string.AttachSticker))
                                            .setItemIcon(R.drawable.msg_sticker),
                                    new PopupChoiceDialogOption()
                                            .setId(DefaultEmojiButtonAction.GIFS.getValue())
                                            .setItemTitle(getString(R.string.AttachGif))
                                            .setItemIcon(R.drawable.msg_gif)
                            ))
                            .title(getString(R.string.DefaultEmojiButtonType))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.jumpToNextChannelOrTopic)
                            .title(getString(R.string.JumpToNextChannelOrTopic))
                            .description(getString(R.string.JumpToNextChannelOrTopic_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.swipeToPip)
                            .title(getString(R.string.SwipeToPIP))
                            .description(getString(R.string.SwipeToPIP_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hideGreetingSticker)
                            .title(getString(R.string.HideGreetingSticker))
                            .description(getString(R.string.HideGreetingSticker_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hideKeyboardOnScroll)
                            .title(getString(R.string.HideKeyboardOnScroll))
                            .description(getString(R.string.HideKeyboardOnScroll_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hideSendAsChannel)
                            .title(getString(R.string.HideSendAsChannel))
                            .description(getString(R.string.HideSendAsChannel_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.showOnlineStatus)
                            .title(getString(R.string.ShowOnlineStatus))
                            .description(getString(R.string.ShowOnlineStatus_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hideCustomEmojis)
                            .title(getString(R.string.HideCustomEmojis))
                            .description(getString(R.string.HideCustomEmojis_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> {
                                if (OctoConfig.INSTANCE.openArchiveOnPull.getValue() && !SharedConfig.archiveHidden) {
                                    SharedConfig.toggleArchiveHidden();
                                }
                            })
                            .preferenceValue(OctoConfig.INSTANCE.openArchiveOnPull)
                            .title(getString(R.string.OpenArchiveOnPull))
                            .description(getString(R.string.OpenArchiveOnPull_Desc))
                            .build());
                })
                .category(getString(R.string.MediaTab), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.activeNoiseSuppression)
                            .title(getString(R.string.VoiceImprovements))
                            .description(getString(R.string.VoiceImprovements_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.playGifAsVideo)
                            .title(getString(R.string.PlayGifsAsVideo))
                            .description(getString(R.string.PlayGifsAsVideo_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.unmuteVideosWithVolumeDown)
                            .title(getString(R.string.UnmuteWithVolumeDown))
                            .description(getString(R.string.UnmuteWithVolumeDown_Desc))
                            .build());
                })
                .category(getString(R.string.DoubleTapActionsHeader), category -> {
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.doubleTapAction)
                            .options(composeDoubleTapOptions(false))
                            .onSelected(() -> canShowSelectReaction.setValue(OctoConfig.INSTANCE.doubleTapAction.getValue() == DoubleTapAction.REACTION.getValue() || OctoConfig.INSTANCE.doubleTapActionOut.getValue() == DoubleTapAction.REACTION.getValue()))
                            .title(getString(R.string.PreferredActionIncoming))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.doubleTapActionOut)
                            .options(composeDoubleTapOptions(true))
                            .onSelected(() -> canShowSelectReaction.setValue(OctoConfig.INSTANCE.doubleTapAction.getValue() == DoubleTapAction.REACTION.getValue() || OctoConfig.INSTANCE.doubleTapActionOut.getValue() == DoubleTapAction.REACTION.getValue()))
                            .title(getString(R.string.PreferredActionOutgoing))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .icon(OctoUtils.getPetIconFixed())
                            .onClick(() -> fragment.presentFragment(new ReactionsDoubleTapManageActivity()))
                            .showIf(canShowSelectReaction)
                            .title(getString(R.string.CustomEmojiReaction))
                            .build());
                })
                .category(getString(R.string.Notifications), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.accentColorAsNotificationColor)
                            .title(getString(R.string.AccentColorAsNotificationColor))
                            .build());
                    category.row(enableSmartNotificationsSwitchRow = new SwitchRow.SwitchRowBuilder()
                            .onClick(() -> {
                                checkSmartNotificationsEnabled(fragment);
                                return true;
                            })
                            .preferenceValue(OctoConfig.INSTANCE.enableSmartNotificationsForPrivateChats)
                            .title(getString(R.string.EnableSmartNotificationsForPrivateChats))
                            .build());
                })
                .build();
    }

    private List<PopupChoiceDialogOption> composeDoubleTapOptions(boolean isOut) {
        Map<DoubleTapAction, OptionData> baseOptions = new LinkedHashMap<>();
        baseOptions.put(DoubleTapAction.DISABLED, new OptionData(R.string.Disable, R.drawable.msg_block));
        baseOptions.put(DoubleTapAction.REACTION, new OptionData(R.string.Reaction, -1));
        baseOptions.put(DoubleTapAction.COPY, new OptionData(R.string.Copy, R.drawable.msg_copy));
        baseOptions.put(DoubleTapAction.FORWARD, new OptionData(R.string.Forward, R.drawable.msg_forward));
        baseOptions.put(DoubleTapAction.REPLY, new OptionData(R.string.Reply, R.drawable.menu_reply));
        baseOptions.put(DoubleTapAction.DELETE, new OptionData(R.string.Delete, R.drawable.msg_delete));
        baseOptions.put(DoubleTapAction.SAVE, new OptionData(R.string.Save, R.drawable.msg_saved));
        baseOptions.put(DoubleTapAction.TRANSLATE, new OptionData(R.string.TranslateMessage, R.drawable.msg_translate));

        List<PopupChoiceDialogOption> options = new ArrayList<>();

        for (Map.Entry<DoubleTapAction, OptionData> entry : baseOptions.entrySet()) {
            PopupChoiceDialogOption option = new PopupChoiceDialogOption()
                    .setId(entry.getKey().getValue())
                    .setItemTitle(getString(entry.getValue().titleResId));

            if (entry.getKey() == DoubleTapAction.REACTION) {
                option.setItemIcon(OctoUtils.getPetIconFixed());
            } else {
                option.setItemIcon(entry.getValue().iconResId);
            }

            options.add(option);
        }

        if (isOut) {
            options.add(new PopupChoiceDialogOption()
                    .setId(DoubleTapAction.EDIT.getValue())
                    .setItemTitle(getString(R.string.Edit))
                    .setItemIcon(R.drawable.msg_edit));
        }

        return options;
    }

    private void checkSmartNotificationsEnabled(PreferencesFragment fragment) {
        if (OctoConfig.INSTANCE.enableSmartNotificationsForPrivateChats.getValue()) {
            return;
        }

        new AlertDialog.Builder(fragment.getContext())
                .setTitle(getString(R.string.Warning))
                .setMessage(getString(R.string.SmartNotificationsPvtDialogMessage))
                .setPositiveButton(getString(R.string.OK), (dialog, which) -> dialog.dismiss())
                .show();
    }

    private record OptionData(int titleResId, int iconResId) {}
}
