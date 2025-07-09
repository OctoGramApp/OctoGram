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
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ReactionsDoubleTapManageActivity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import it.octogram.android.ActionBarCenteredTitle;
import it.octogram.android.ConfigProperty;
import it.octogram.android.DcIdStyle;
import it.octogram.android.DcIdType;
import it.octogram.android.DefaultEmojiButtonAction;
import it.octogram.android.DefaultMicrophoneButtonAction;
import it.octogram.android.DoubleTapAction;
import it.octogram.android.ExpandableRowsIds;
import it.octogram.android.NewFeaturesBadgeIds;
import it.octogram.android.OctoConfig;
import it.octogram.android.ShortcutsPosition;
import it.octogram.android.StickerShape;
import it.octogram.android.StickerUi;
import it.octogram.android.deeplink.DeepLinkDef;
import it.octogram.android.preferences.OctoPreferences;
import it.octogram.android.preferences.PreferencesEntry;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.rows.impl.CustomCellRow;
import it.octogram.android.preferences.rows.impl.ExpandableRows;
import it.octogram.android.preferences.rows.impl.FooterInformativeRow;
import it.octogram.android.preferences.rows.impl.HeaderRow;
import it.octogram.android.preferences.rows.impl.ListRow;
import it.octogram.android.preferences.rows.impl.ShadowRow;
import it.octogram.android.preferences.rows.impl.SliderRow;
import it.octogram.android.preferences.rows.impl.SwitchRow;
import it.octogram.android.preferences.rows.impl.TextIconRow;
import it.octogram.android.preferences.ui.components.ChatSettingsPreviews;
import it.octogram.android.preferences.ui.custom.DcInfoSelector;
import it.octogram.android.utils.OctoUtils;
import it.octogram.android.utils.appearance.PopupChoiceDialogOption;
import it.octogram.android.utils.config.ExpandableRowsOption;

public class OctoChatsSettingsUI implements PreferencesEntry {
    private ChatSettingsPreviews headerCellPreview;
    private ChatSettingsPreviews stickersCellPreview;
    private ChatSettingsPreviews messagesCellPreview;
    private ChatSettingsPreviews inputBoxCellPreview;
    private ChatSettingsPreviews bottomBarCellPreview;
    private DcInfoSelector dcInfoSelector;

    @NonNull
    @Override
    public OctoPreferences getPreferences(@NonNull PreferencesFragment fragment, @NonNull Context context) {
        int centeredTitleState = OctoConfig.INSTANCE.uiTitleCenteredState.getValue();
        ConfigProperty<Boolean> isTitleUncentered = new ConfigProperty<>(null, centeredTitleState != ActionBarCenteredTitle.JUST_IN_CHATS.getValue() && centeredTitleState != ActionBarCenteredTitle.ALWAYS.getValue());
        ConfigProperty<Boolean> canShowSelectReaction = new ConfigProperty<>(null, OctoConfig.INSTANCE.doubleTapAction.getValue() == DoubleTapAction.REACTION.getValue() || OctoConfig.INSTANCE.doubleTapActionOut.getValue() == DoubleTapAction.REACTION.getValue());

        ConfigProperty<Boolean> areShortcutsEnabled = new ConfigProperty<>(null, false);
        ConfigProperty<Boolean> showTooManyOptionsAlert = new ConfigProperty<>(null, false);

        Runnable updateShortcutsState = () -> {
            areShortcutsEnabled.updateValue(areShortcutsEnabled());
            showTooManyOptionsAlert.updateValue(getEnabledShortcutsCount() > 2 && OctoConfig.INSTANCE.shortcutsPosition.getValue() == ShortcutsPosition.CHAT_INFO.getId());
        };
        updateShortcutsState.run();

        return OctoPreferences.builder(getString(R.string.ChatTitle))
                .deepLink(DeepLinkDef.CHATS)
                .sticker(context, OctoConfig.STICKERS_PLACEHOLDER_PACK_NAME, StickerUi.NEW_MODEL_GENERATION, true, getString(R.string.OctoChatsSettingsHeader))
                .category(getString(R.string.OctoMainSettingsManageCategory), category -> {
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoTranslatorUI())))
                            .icon(R.drawable.msg_translate)
                            .title(getString(R.string.Translator))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoAiFeaturesUI())))
                            .icon(R.drawable.cup_star_solar)
                            .title(getString(R.string.AiFeatures_Brief))
                            .build());
                })
                .category(getString(R.string.HeaderHeader), category -> {
                    category.row(new CustomCellRow.CustomCellRowBuilder()
                            .layout(headerCellPreview = new ChatSettingsPreviews(context, ChatSettingsPreviews.PreviewType.HEADER))
                            .build()
                    );
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> headerCellPreview.invalidate())
                            .preferenceValue(OctoConfig.INSTANCE.slidingTitle)
                            .title(getString(R.string.SlidingTitle))
                            .showIf(isTitleUncentered)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> headerCellPreview.reloadSingleActionBarMembersCount())
                            .preferenceValue(OctoConfig.INSTANCE.numberRounding)
                            .title(getString(R.string.NumberRounding))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.headerLongPressSearch)
                            .title(getString(R.string.HeaderLongPressSearch))
                            .showIf(isTitleUncentered, true)
                            .build());
                    category.row(new ExpandableRows.ExpandableRowsBuilder()
                            .setId(ExpandableRowsIds.CHAT_HEADER_SEARCH.getId())
                            .setIcon(R.drawable.msg_search)
                            .setMainTitle(getString(R.string.Search))
                            .hideMainSwitch(true)
                            .addRow(new ExpandableRowsOption()
                                    .optionTitle(getString(R.string.SearchIconInHeader))
                                    .property(OctoConfig.INSTANCE.searchIconInHeader)
                                    .onPostUpdate(() -> headerCellPreview.invalidate())
                            )
                            .addRow(new ExpandableRowsOption()
                                    .optionTitle(getString(R.string.HeaderLongPressSearch))
                                    .property(OctoConfig.INSTANCE.headerLongPressSearch)
                            )
                            .showIf(isTitleUncentered)
                            .build()
                    );
                })
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(getString(R.string.AppearanceHiddenOptions))
                        .showIf(isTitleUncentered, true)
                        .build())
                .category(getString(R.string.MessagesSettings), category -> {
                    category.row(new CustomCellRow.CustomCellRowBuilder()
                            .layout(messagesCellPreview = new ChatSettingsPreviews(context, ChatSettingsPreviews.PreviewType.MESSAGES))
                            .build()
                    );
                    category.row(new ExpandableRows.ExpandableRowsBuilder()
                            .setId(ExpandableRowsIds.REPLIES_AND_LINKS.getId())
                            .setIcon(R.drawable.menu_reply)
                            .setMainTitle(getString(R.string.RepliesLinksHeader))
                            .addRow(new ExpandableRowsOption()
                                    .optionTitle(getString(R.string.RepliesLinksShowColors))
                                    .property(OctoConfig.INSTANCE.repliesLinksShowColors)
                                    .onPostUpdate(() -> messagesCellPreview.invalidate())
                            )
                            .addRow(new ExpandableRowsOption()
                                    .optionTitle(getString(R.string.RepliesLinksShowEmojis))
                                    .property(OctoConfig.INSTANCE.repliesLinksShowEmojis)
                                    .onPostUpdate(() -> messagesCellPreview.invalidate())
                            )
                            .build()
                    );
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> {
                                LocaleController.getInstance().recreateFormatters();
                                messagesCellPreview.invalidate();
                                stickersCellPreview.invalidate();
                            })
                            .preferenceValue(OctoConfig.INSTANCE.formatTimeWithSeconds)
                            .title(getString(R.string.FormatTimeWithSeconds))
                            .description(getString(R.string.FormatTimeWithSeconds_Desc))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> messagesCellPreview.invalidate())
                            .preferenceValue(OctoConfig.INSTANCE.pencilIconForEditedMessages)
                            .title(getString(R.string.PencilIconForEdited))
                            .description(getString(R.string.PencilIconForEdited_Desc))
                            .build());
                    category.row(new TextIconRow.TextIconRowBuilder()
                            .onClick(() -> fragment.presentFragment(new PreferencesFragment(new OctoContextMenuSettingsUI())))
                            .icon(R.drawable.msg_media)
                            .isNew(NewFeaturesBadgeIds.CONTEXT_MENU_NEW_INTF)
                            .title(getString(R.string.ContextMenu))
                            .build());
                })
                .category(getString(R.string.StickersSizeHeader), category -> {
                    category.row(new SliderRow.SliderRowBuilder()
                            .min(2)
                            .max(20)
                            .onSelected(() -> stickersCellPreview.invalidate())
                            .preferenceValue(OctoConfig.INSTANCE.maxStickerSize)
                            .build());
                    category.row(new CustomCellRow.CustomCellRowBuilder()
                            .layout(stickersCellPreview = new ChatSettingsPreviews(context, ChatSettingsPreviews.PreviewType.STICKER))
                            .build()
                    );
                    category.row(new ListRow.ListRowBuilder()
                            .onSelected(() -> stickersCellPreview.invalidate())
                            .currentValue(OctoConfig.INSTANCE.stickerShape)
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(StickerShape.DEFAULT)
                                            .setItemTitle(getString(R.string.StyleTypeDefault)),
                                    new PopupChoiceDialogOption()
                                            .setId(StickerShape.ROUND)
                                            .setItemTitle(getString(R.string.StickerShapeRounded)),
                                    new PopupChoiceDialogOption()
                                            .setId(StickerShape.MESSAGE)
                                            .setItemTitle(getString(R.string.StyleTypeMessage))
                            ))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .title(getString(R.string.StickerShape))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> stickersCellPreview.invalidate())
                            .preferenceValue(OctoConfig.INSTANCE.hideSentTimeOnStickers)
                            .title(getString(R.string.RemoveTimeOnStickers))
                            .build());
                })
                .row(new ShadowRow())
                .category(R.string.PinnedElements, category -> {
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
                })
                .category(getString(R.string.ActionsHeader), category -> {
                    category.row(new ExpandableRows.ExpandableRowsBuilder()
                            .setId(ExpandableRowsIds.PROMPT_BEFORE_SENDING.getId())
                            .setIcon(R.drawable.msg_send)
                            .setMainTitle(getString(R.string.Warnings))
                            .addRow(new ExpandableRowsOption()
                                    .optionTitle(getString(R.string.PromptBeforeSendingStickers2))
                                    .property(OctoConfig.INSTANCE.promptBeforeSendingStickers)
                            )
                            .addRow(new ExpandableRowsOption()
                                    .optionTitle(getString(R.string.PromptBeforeSendingGIFs2))
                                    .property(OctoConfig.INSTANCE.promptBeforeSendingGIFs)
                            )
                            .addRow(new ExpandableRowsOption()
                                    .optionTitle(getString(R.string.PromptBeforeSendingVoiceMessages2))
                                    .property(OctoConfig.INSTANCE.promptBeforeSendingVoiceMessages)
                            )
                            .addRow(new ExpandableRowsOption()
                                    .optionTitle(getString(R.string.PromptBeforeSendingVideoMessages2))
                                    .property(OctoConfig.INSTANCE.promptBeforeSendingVideoMessages)
                            )
                            .addRow(new ExpandableRowsOption()
                                    .optionTitle(getString(R.string.PromptBeforeCalling2))
                                    .property(OctoConfig.INSTANCE.promptBeforeCalling)
                            )
                            .addRow(new ExpandableRowsOption()
                                    .optionTitle(getString(R.string.PromptBeforeDeletingChatHistory2))
                                    .property(OctoConfig.INSTANCE.warningBeforeDeletingChatHistory)
                            )
                            .build()
                    );
                    category.row(new ExpandableRows.ExpandableRowsBuilder()
                            .setId(ExpandableRowsIds.ADMIN_SHORTCUTS.getId())
                            .setIcon(R.drawable.msg_admins)
                            .setMainTitle(getString(R.string.AdminShortcuts))
                            .setOnSingleStateChange(updateShortcutsState)
                            .addRow(new ExpandableRowsOption()
                                    .optionTitle(getString(R.string.ChannelAdministrators))
                                    .property(OctoConfig.INSTANCE.shortcutsAdministrators)
                            )
                            .addRow(new ExpandableRowsOption()
                                    .optionTitle(getString(R.string.EventLog))
                                    .property(OctoConfig.INSTANCE.shortcutsRecentActions)
                            )
                            .addRow(new ExpandableRowsOption()
                                    .optionTitle(getString(R.string.Statistics))
                                    .property(OctoConfig.INSTANCE.shortcutsStatistics)
                            )
                            .addRow(new ExpandableRowsOption()
                                    .optionTitle(getString(R.string.ChannelPermissions))
                                    .property(OctoConfig.INSTANCE.shortcutsPermissions)
                            )
                            .addRow(new ExpandableRowsOption()
                                    .optionTitle(getString(R.string.InviteLinks))
                                    .property(OctoConfig.INSTANCE.shortcutsInviteLinks)
                            )
                            .addRow(new ExpandableRowsOption()
                                    .optionTitle(getString(R.string.GroupMembers))
                                    .property(OctoConfig.INSTANCE.shortcutsMembers)
                            )
                            .build()
                    );
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.shortcutsPosition)
                            .onSelected(updateShortcutsState)
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(ShortcutsPosition.THREE_DOTS.getId())
                                            .setItemTitle(getString(R.string.AdminShortcutsPositionThreeDots))
                                            .setItemDescription(getString(R.string.AdminShortcutsPositionThreeDots_Desc)),
                                    new PopupChoiceDialogOption()
                                            .setId(ShortcutsPosition.CHAT_INFO.getId())
                                            .setItemTitle(getString(R.string.AdminShortcutsPositionChatInfo))
                                            .setItemDescription(getString(R.string.AdminShortcutsPositionChatInfo_Desc) + " " + getString(R.string.AdminShortcutsPositionChatInfo_Alert)),
                                    new PopupChoiceDialogOption()
                                            .setId(ShortcutsPosition.PROFILE_DOTS.getId())
                                            .setItemTitle(getString(R.string.AdminShortcutsPositionChatThreeDots))
                                            .setItemDescription(getString(R.string.AdminShortcutsPositionChatThreeDots_Desc))
                            ))
                            .showIf(areShortcutsEnabled)
                            .title(getString(R.string.AdminShortcutsPosition))
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
                            .preferenceValue(OctoConfig.INSTANCE.showOnlineStatus)
                            .title(getString(R.string.ShowOnlineStatus))
                            .description(getString(R.string.ShowOnlineStatus_Desc))
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
                .row(new FooterInformativeRow.FooterInformativeRowBuilder()
                        .title(getString(R.string.AdminShortcutsPositionChatInfo_Alert))
                        .showIf(showTooManyOptionsAlert)
                        .build())
                .category(R.string.InputBoxSettings, category -> {
                    category.row(new CustomCellRow.CustomCellRowBuilder()
                            .layout(inputBoxCellPreview = new ChatSettingsPreviews(context, fragment, ChatSettingsPreviews.PreviewType.INPUT_BOX))
                            .build()
                    );
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.defaultEmojiButtonAction)
                            .onSelected(() -> inputBoxCellPreview.invalidate())
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
                            .title(getString(R.string.DefaultEmojiButtonType2))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .currentValue(OctoConfig.INSTANCE.defaultRightButtonAction)
                            .onSelected(() -> inputBoxCellPreview.invalidate())
                            .options(List.of(
                                    new PopupChoiceDialogOption()
                                            .setId(DefaultMicrophoneButtonAction.DEFAULT.getValue())
                                            .setItemTitle(getString(R.string.DefaultEmojiButtonTypeDefault))
                                            .setItemIcon(R.drawable.msg_forward_replace),
                                    new PopupChoiceDialogOption()
                                            .setId(DefaultMicrophoneButtonAction.VOICE_MESSAGE.getValue())
                                            .setItemTitle(getString(R.string.AccDescrVoiceMessage))
                                            .setItemIcon(R.drawable.msg_voice_unmuted),
                                    new PopupChoiceDialogOption()
                                            .setId(DefaultMicrophoneButtonAction.VIDEO_MESSAGE.getValue())
                                            .setItemTitle(getString(R.string.AccDescrVideoMessage))
                                            .setItemIcon(R.drawable.msg_video)
                            ))
                            .title(getString(R.string.DefaultRightButtonType))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hideKeyboardOnScroll)
                            .title(getString(R.string.HideKeyboardOnScroll))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> inputBoxCellPreview.invalidate())
                            .preferenceValue(OctoConfig.INSTANCE.hideSendAsChannel)
                            .title(getString(R.string.HideSendAsChannel))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.hideCustomEmojis)
                            .title(getString(R.string.HideCustomEmojis))
                            .build());
                })
                .category(R.string.BottomBar, category -> {
                    category.row(new CustomCellRow.CustomCellRowBuilder()
                            .layout(bottomBarCellPreview = new ChatSettingsPreviews(context, fragment, ChatSettingsPreviews.PreviewType.DISCUSS))
                            .build()
                    );
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> bottomBarCellPreview.invalidate())
                            .preferenceValue(OctoConfig.INSTANCE.hideBottomBarChannels)
                            .title(getString(R.string.HideBottomBarChannels))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> bottomBarCellPreview.invalidate())
                            .preferenceValue(OctoConfig.INSTANCE.hideChatButtonChannels)
                            .title(getString(R.string.HideChatButtonChannels))
                            .showIf(OctoConfig.INSTANCE.hideBottomBarChannels, true)
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> bottomBarCellPreview.invalidate())
                            .preferenceValue(OctoConfig.INSTANCE.hideGiftButtonChannels)
                            .title(getString(R.string.HideGiftButtonChannels))
                            .showIf(OctoConfig.INSTANCE.hideBottomBarChannels, true)
                            .build());
                })
                .category(getString(R.string.MediaTab), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.activeNoiseSuppression)
                            .title(getString(R.string.VoiceImprovements))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.playGifAsVideo)
                            .title(getString(R.string.PlayGifsAsVideo))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.unmuteVideosWithVolumeDown)
                            .title(getString(R.string.UnmuteWithVolumeDown))
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
                .category(getString(R.string.DcIdHeader), category -> {
                    category.row(new CustomCellRow.CustomCellRowBuilder()
                            .layout(dcInfoSelector = new DcInfoSelector(context, fragment.getResourceProvider()))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .onPostUpdate(() -> AndroidUtilities.runOnUIThread(() -> dcInfoSelector.update()))
                            .preferenceValue(OctoConfig.INSTANCE.showDcId)
                            .title(getString(R.string.ShowDcID))
                            .build());
                    category.row(new ListRow.ListRowBuilder()
                            .onSelected(() -> AndroidUtilities.runOnUIThread(() -> dcInfoSelector.update()))
                            .currentValue(OctoConfig.INSTANCE.dcIdStyle)
                            .options(List.of(
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
                            .showIf(OctoConfig.INSTANCE.showDcId)
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
                            .showIf(OctoConfig.INSTANCE.showDcId)
                            .title(getString(R.string.Type))
                            .build());
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.registrationDateInProfiles)
                            .title(getString(R.string.ShowRegistrationDate))
                            .description(getString(R.string.ShowRegistrationDate_Desc))
                            .postNotificationName(NotificationCenter.reloadInterface)
                            .build());
                })
                .category(getString(R.string.BlurHeader), category -> {
                    category.row(new SwitchRow.SwitchRowBuilder()
                            .preferenceValue(OctoConfig.INSTANCE.forceChatBlurEffect)
                            .title(getString(R.string.ForceChatBlurEffect))
                            .build());
                    category.row(new HeaderRow(getString(R.string.ForceChatBlurEffectName), OctoConfig.INSTANCE.forceChatBlurEffect));
                    category.row(new SliderRow.SliderRowBuilder()
                            .min(0)
                            .max(255)
                            .preferenceValue(OctoConfig.INSTANCE.blurEffectStrength)
                            .showIf(OctoConfig.INSTANCE.forceChatBlurEffect)
                            .build());
                })
                .build();
    }

    private int getEnabledShortcutsCount() {
        int i = 0;
        if (OctoConfig.INSTANCE.shortcutsAdministrators.getValue()) i++;
        if (OctoConfig.INSTANCE.shortcutsRecentActions.getValue()) i++;
        if (OctoConfig.INSTANCE.shortcutsStatistics.getValue()) i++;
        if (OctoConfig.INSTANCE.shortcutsPermissions.getValue()) i++;
        if (OctoConfig.INSTANCE.shortcutsInviteLinks.getValue()) i++;
        if (OctoConfig.INSTANCE.shortcutsMembers.getValue()) i++;
        return i;
    }

    private boolean areShortcutsEnabled() {
        return getEnabledShortcutsCount() > 0;
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

    private record OptionData(int titleResId, int iconResId) {
    }
}
