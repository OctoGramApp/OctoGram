/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.config;

import static org.telegram.messenger.LocaleController.getString;

import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Locale;

import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.logs.OctoLogging;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.ui.OctoAiFeaturesUI;
import it.octogram.android.preferences.ui.OctoAppearanceUI;
import it.octogram.android.preferences.ui.OctoCameraSettingsUI;
import it.octogram.android.preferences.ui.OctoChatsSettingsUI;
import it.octogram.android.preferences.ui.OctoDrawerSettingsUI;
import it.octogram.android.preferences.ui.OctoExperimentsUI;
import it.octogram.android.preferences.ui.OctoGeneralSettingsUI;
import it.octogram.android.preferences.ui.OctoPrivacySettingsUI;
import it.octogram.android.preferences.ui.OctoTranslatorUI;
import it.octogram.android.preferences.ui.OctoUpdatesUI;

public class ImportSettingsScanHelper {
    public ArrayList<SettingsScanCategory> categories = new ArrayList<>();
    public ArrayList<String> excludedOptions = new ArrayList<>();
    public static ImportSettingsScanHelper INSTANCE = new ImportSettingsScanHelper();

    public ImportSettingsScanHelper() {
        fillExcludedOptions();

        SettingsScanCategory generalCategory = new SettingsScanCategory("general", R.string.General, R.drawable.msg_media, (t) -> new PreferencesFragment(new OctoGeneralSettingsUI(), t));
        SettingsScanCategory appearanceCategory = new SettingsScanCategory("appearance", R.string.Appearance, R.drawable.settings_appearance, (t) -> new PreferencesFragment(new OctoAppearanceUI(), t));
        SettingsScanCategory appearanceDrawerCategory = new SettingsScanCategory("appearanceDrawer", R.string.DrawerTitle, R.drawable.msg_map_type, (t) -> new PreferencesFragment(new OctoDrawerSettingsUI(), t)).setRefersTo(appearanceCategory);
        SettingsScanCategory chatsCategory = new SettingsScanCategory("chats", R.string.ChatTitle, R.drawable.msg2_discussion, (t) -> new PreferencesFragment(new OctoChatsSettingsUI(), t));
        SettingsScanCategory chatsTranslatorCategory = new SettingsScanCategory("translator", R.string.Translator, R.drawable.msg_translate, (t) -> new PreferencesFragment(new OctoTranslatorUI(), t)).setRefersTo(chatsCategory);
        SettingsScanCategory chatsAiFeaturesCategory = new SettingsScanCategory("aiFeatures", R.string.AiFeatures_Brief, R.drawable.cup_star_solar, (t) -> new PreferencesFragment(new OctoAiFeaturesUI(), t)).setRefersTo(chatsCategory);
        SettingsScanCategory chatCameraCategory = new SettingsScanCategory("chatcamera", R.string.ChatCamera, R.drawable.msg_camera, (t) -> new PreferencesFragment(new OctoCameraSettingsUI(), t));
        SettingsScanCategory privacyCategory = new SettingsScanCategory("privacy", R.string.PrivacySettings, R.drawable.menu_privacy, (t) -> new PreferencesFragment(new OctoPrivacySettingsUI(), t));
        SettingsScanCategory experimentsCategory = new SettingsScanCategory("experiments", R.string.Experiments, R.drawable.outline_science_white, (t) -> new PreferencesFragment(new OctoExperimentsUI(), t));
        SettingsScanCategory updatesCategory = new SettingsScanCategory("updates", R.string.Updates, R.drawable.round_update_white_28, (t) -> new PreferencesFragment(new OctoUpdatesUI(), t));

        fillGeneralOptions(generalCategory);
        fillAppearanceOptions(appearanceCategory);
        fillAppearanceDrawerOptions(appearanceDrawerCategory);
        fillChatsOptions(chatsCategory);
        fillChatsTanslatorOptions(chatsTranslatorCategory);
        fillChatsAiFeaturesOptions(chatsAiFeaturesCategory);
        //fillAppearanceAppOptions(appearanceAppCategory);
        fillChatCameraOptions(chatCameraCategory);
        fillPrivacyOptions(privacyCategory);
        fillExperimentsOptions(experimentsCategory);
        fillUpdatesOptions(updatesCategory);

        categories.add(generalCategory);
        categories.add(appearanceCategory);
//        categories.add(appearanceChatsCategory);
        categories.add(appearanceDrawerCategory);
        categories.add(chatsCategory);
        categories.add(chatsTranslatorCategory);
        categories.add(chatsAiFeaturesCategory);
        categories.add(chatCameraCategory);
        categories.add(privacyCategory);
        categories.add(experimentsCategory);
        categories.add(updatesCategory);

        if (BuildConfig.DEBUG_PRIVATE_VERSION) {
            testSettings();
        }
    }

    public SettingsScanCategory getCategoryById(String categoryId) {
        for (SettingsScanCategory category : categories) {
            if (category.categoryId.equals(categoryId)) {
                return category;
            }
        }
        return null;
    }

    public ArrayList<SettingsScanCategory> getSubCategories(SettingsScanCategory refersTo) {
        ArrayList<SettingsScanCategory> subCategories = new ArrayList<>();
        for (SettingsScanCategory category : categories) {
            if (category.refersTo == refersTo) {
                subCategories.add(category);
            }
        }
        return subCategories;
    }

    private void fillExcludedOptions() {
        excludedOptions.clear();
        excludedOptions.add(OctoConfig.INSTANCE.unlockedYuki.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.unlockedChupa.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.unlockedFoxIcon.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.unlockedConfetti.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.selectedEmojiPack.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.languagePackVersioning.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.lastSelectedCompression.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.contextClearFromCache.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.contextCopyPhoto.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.contextSaveMessage.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.contextReportMessage.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.contextMessageDetails.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.contextNoQuoteForward.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.contextReplyPrivateChat.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.contextReplyMessage.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.useTranslationsArgsFix.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.receivePBetaUpdates.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.mediaFiltering.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.isMigrateOldLogs.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.syncPowerSaver.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.experimentsEnabled.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.lastTranslatePreSendLanguage.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.newBadgeIds.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.updateSignalingCommitID.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.updateSignalingChangelog.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.shortcutsAdministrators.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.shortcutsMembers.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.shortcutsInviteLinks.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.shortcutsRecentActions.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.shortcutsStatistics.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.shortcutsPermissions.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.pinnedEmojisList.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.hideRecentEmojis.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.usePinnedEmojisFeature.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.hiddenFolderAssoc.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.forceHideLockScreenPopup.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.usePinnedReactionsChats.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.usePinnedReactionsChannels.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.pinnedReactionsChats.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.pinnedReactionsChannels.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.usePinnedHashtagsFeature.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.pinnedHashtagsList.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.verifyLinkTip.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.shownHiddenChatsHint.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.hiddenChats.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.hiddenAccounts.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.hideHiddenAccounts.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.advancedBiometricUnlock.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.aiFeaturesRecentProvider.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.hasShownLockedChatsTip.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.aiFeaturesLastUsedFormality.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.aiFeaturesLastUsedLanguage.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.aiFeaturesLastUsedLength.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.aiFeaturesOpenRouterAPIKey.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.aiFeaturesUseOpenRouterAPIs.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.aiFeaturesOpenRouterSelectedModel.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.aiFeaturesUseGoogleAPIs.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.aiFeaturesUseGoogleAPIKey.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.aiFeaturesUseChatGPTAPIs.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.aiFeaturesUseChatGPTAPIKey.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.aiFeaturesCustomModels.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.aiFeaturesAcceptedTerms.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.hasFingerprintSavedState.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.aiFeaturesUseGroqAPIs.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.aiFeaturesUseGroqAPIKey.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.aiFeaturesGroqSelectedModel.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.aiFeaturesUseOllamaAPIs.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.aiFeaturesOllamaSelectedModel.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.aiFeaturesOllamaApiUrl.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.usePredictiveGestures.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.roundedTextBox.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.hideOnlyAllChatsFolder.getKey()); // part of main settings page
    }

    private void fillGeneralOptions(SettingsScanCategory category) {
        category.options.add(SettingsScanOption.asTitle(R.string.InterfaceHeader));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.showUserIconsInChatsList, R.string.ShowUserIconsInChatsList));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hideStories, R.string.HideStories));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.alwaysShowDownloads, R.string.AlwaysShowDownloads));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.actionBarTitleOption, R.string.ActionBarTitle));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.actionBarCustomTitle, R.string.ActionBarTitleCustom));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.disableDividers, R.string.HideDividers));
        category.options.add(SettingsScanOption.asTitle(R.string.ImproveRapidActions));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.rapidActionsDefaultConfig, R.string.ImproveRapidActionsDefault));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.rapidActionsMainButtonAction, R.string.ImproveRapidActionsMainButtonAction));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.rapidActionsMainButtonActionLongPress, R.string.ImproveRapidActionsMainButtonActionLongPress));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.rapidActionsSecondaryButtonAction, R.string.ImproveRapidActionsSecondaryButtonAction));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.useSquaredFab, R.string.SquaredFab));
        category.options.add(SettingsScanOption.asTitle(R.string.FontEmojisHeader));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.useSystemEmoji, R.string.UseSystemEmojis));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.useSystemFont, R.string.UseSystemFont));
        category.options.add(SettingsScanOption.asTitle(R.string.Notifications));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.accentColorAsNotificationColor, R.string.AccentColorAsNotificationColor));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.enableSmartNotificationsForPrivateChats, R.string.EnableSmartNotificationsForPrivateChats));
    }

    private void fillChatsTanslatorOptions(SettingsScanCategory category) {
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.translatorMode, R.string.TranslatorMode));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.translatorProvider, R.string.TranslatorProvider));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.translatorFormality, R.string.TranslatorFormality));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.translatorKeepMarkdown, R.string.TranslatorKeepMarkdown));
    }

    private void fillChatsAiFeaturesOptions(SettingsScanCategory category) {
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.aiFeatures, R.string.AiFeatures));
        category.options.add(SettingsScanOption.asTitle(R.string.AiFeatures_Features));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.aiFeaturesTranslateMessages, R.string.TranslateMessages));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.aiFeaturesChatContext, R.string.AiFeatures_Features_ChatContext));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.aiFeaturesAskOnMedia, R.string.AiFeatures_Features_AskOnPhoto));
    }

    private void fillAppearanceOptions(SettingsScanCategory category) {
        category.options.add(SettingsScanOption.asTitle(R.string.ImproveInterface));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.uiTitleCenteredState, R.string.ImproveInterfaceTitleCentered));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.interfaceSwitchUI, R.string.ImproveInterfaceSwitch));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.interfaceCheckboxUI, R.string.ImproveInterfaceCheckbox));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.interfaceSliderUI, R.string.ImproveInterfaceSlider));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.uiIconsType, R.string.ImproveIconsShort));
        category.options.add(SettingsScanOption.asTitle(R.string.ManageFolders));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.tabMode, R.string.FolderType));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.tabStyle, R.string.FolderStyle));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hideUnreadCounterOnFolder, R.string.HideUnreadCounter));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.showFoldersMessagesCounter, R.string.ShowMessagesCounter));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.includeMutedChatsInCounter, R.string.IncludeMutedChats));
        category.options.add(SettingsScanOption.asTitle(R.string.LocalOther));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.forcePacmanAnimation, R.string.ForcePacmanAnimation));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.showSnowflakes, R.string.ShowSnowflakes));
    }

    private void fillChatsOptions(SettingsScanCategory category) {
        category.options.add(SettingsScanOption.asTitle(R.string.HeaderHeader));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.slidingTitle, R.string.SlidingTitle));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.numberRounding, R.string.NumberRounding));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.searchIconInHeader, R.string.SearchIconInHeader));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.headerLongPressSearch, R.string.HeaderLongPressSearch));
        category.options.add(SettingsScanOption.asTitle(R.string.MessagesSettings));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.repliesLinksShowColors, composeName(R.string.RepliesLinksHeader, R.string.RepliesLinksShowColors)));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.repliesLinksShowEmojis, composeName(R.string.RepliesLinksHeader, R.string.RepliesLinksShowEmojis)));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.formatTimeWithSeconds, R.string.FormatTimeWithSeconds));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.pencilIconForEditedMessages, R.string.PencilIconForEdited));
        category.options.add(SettingsScanOption.asTitle(R.string.ContextMenu));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.contextMenuBriefingState, R.string.ContextMenuBriefingState));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.contextMenuShortcutsTitles, R.string.ContextMenuBriefingState_Shortcuts_Title));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.contextMenuBottomShortcuts, R.string.ContextMenuBriefingState_Shortcuts_Bottom));
        category.options.add(SettingsScanOption.asTitle(R.string.AccDescrStickers));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.maxStickerSize, R.string.StickersSizeHeader));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.stickerShape, R.string.StickerShape));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hideSentTimeOnStickers, R.string.RemoveTimeOnStickers));
        category.options.add(SettingsScanOption.asTitle(R.string.Warnings));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.promptBeforeSendingStickers, R.string.PromptBeforeSendingStickers2));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.promptBeforeSendingGIFs, R.string.PromptBeforeSendingGIFs2));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.promptBeforeSendingVoiceMessages, R.string.PromptBeforeSendingVoiceMessages2));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.promptBeforeSendingVideoMessages, R.string.PromptBeforeSendingVideoMessages2));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.promptBeforeCalling, R.string.PromptBeforeCalling2));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.warningBeforeDeletingChatHistory, R.string.PromptBeforeDeletingChatHistory2));
        category.options.add(SettingsScanOption.asTitle(R.string.ActionsHeader));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.shortcutsPosition, R.string.AdminShortcutsPosition));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.jumpToNextChannelOrTopic, R.string.JumpToNextChannelOrTopic));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.swipeToPip, R.string.SwipeToPIP));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hideGreetingSticker, R.string.HideGreetingSticker));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.showOnlineStatus, R.string.ShowOnlineStatus));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.openArchiveOnPull, R.string.OpenArchiveOnPull));
        category.options.add(SettingsScanOption.asTitle(R.string.InputBoxSettings));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.defaultEmojiButtonAction, R.string.DefaultEmojiButtonType2));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.defaultRightButtonAction, R.string.DefaultRightButtonType));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hideKeyboardOnScroll, R.string.HideKeyboardOnScroll));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hideSendAsChannel, R.string.HideSendAsChannel));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hideCustomEmojis, R.string.HideCustomEmojis));
        category.options.add(SettingsScanOption.asTitle(R.string.BottomBar));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hideBottomBarChannels, R.string.HideBottomBarChannels));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hideChatButtonChannels, R.string.HideChatButtonChannels));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hideGiftButtonChannels, R.string.HideGiftButtonChannels));
        category.options.add(SettingsScanOption.asTitle(R.string.MediaTab));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.activeNoiseSuppression, R.string.VoipNoiseCancellation));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.playGifAsVideo, R.string.PlayGifsAsVideo));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.unmuteVideosWithVolumeDown, R.string.UnmuteWithVolumeDown));
        category.options.add(SettingsScanOption.asTitle(R.string.DoubleTapActionsHeader));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.doubleTapActionOut, R.string.PreferredActionOutgoing));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.doubleTapAction, R.string.PreferredActionIncoming));
        category.options.add(SettingsScanOption.asTitle(R.string.DcIdHeader));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.showDcId, R.string.ShowDcID));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.dcIdStyle, R.string.Style));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.dcIdType, R.string.Type));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.registrationDateInProfiles, R.string.ShowRegistrationDate));
        category.options.add(SettingsScanOption.asTitle(R.string.BlurHeader));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.forceChatBlurEffect, R.string.ForceChatBlurEffect));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.blurEffectStrength, R.string.ForceChatBlurEffectName));
    }

    private void fillAppearanceDrawerOptions(SettingsScanCategory category) {
        category.options.add(SettingsScanOption.asTitle(R.string.DrawerTitle));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.drawerBackground, R.string.DrawerBackground));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.drawerShowProfilePic, R.string.DrawerShowProfilePic));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.drawerGradientBackground, R.string.DrawerGradientBackground));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.drawerFavoriteOption, R.string.DrawerFavoriteOption));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.drawerDarkenBackground, R.string.DrawerDarkenBackground));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.drawerDarkenBackgroundLevel, R.string.DrawerDarkenBackgroundLevel));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.drawerBlurBackground, R.string.DrawerBlurBackground));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.drawerBlurBackgroundLevel, R.string.DrawerBlurBackgroundLevel));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.drawerItems, R.string.DrawerElements));
        category.options.add(SettingsScanOption.asTitle(R.string.Style));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.eventType, R.string.EventType));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.drawerProfileAsBubble, R.string.DrawerHeaderAsBubble));
    }

    private void fillChatCameraOptions(SettingsScanCategory category) {
        category.options.add(SettingsScanOption.asTitle(R.string.OctoCameraSettings));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.cameraType, R.string.CameraType));
        category.options.add(SettingsScanOption.asTitle(R.string.TranslatorCategoryOptions));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.cameraXZeroShutter, R.string.ZeroShutter));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.cameraXPerformanceMode, R.string.PerformanceMode));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.cameraXResolution, R.string.CurrentCameraXResolution));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.startWithRearCamera, R.string.StartWithRearCamera));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.cameraPreview, R.string.CameraButtonPosition));
    }

    private void fillPrivacyOptions(SettingsScanCategory category) {
        category.isSecureContext = true;
        category.options.add(SettingsScanOption.asTitle(R.string.LockedActions));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.biometricOpenArchive, R.string.BiometricSettingsOpenArchive));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.biometricOpenCallsLog, R.string.BiometricSettingsOpenCallsLog));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.biometricOpenSecretChats, R.string.BiometricSettingsOpenSecretChats));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.biometricOpenSettings, R.string.BiometricSettingsOpenSettings));
        category.options.add(SettingsScanOption.asTitle(R.string.LockedChats));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.lockedChatsHideChats, R.string.LockedChats_Options_HideChats));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.lockedChatsLockScreenshots, R.string.LockedChats_Options_LockScreenshots));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.lockedChatsShowNotifications, R.string.LockedChats_Options_ShowNotifications));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.lockedChatsSpoilerNotifications, composeName(R.string.Notifications, R.string.LockedChats_Options_SpoilerContent_Desc)));
        category.options.add(SettingsScanOption.asTitle(R.string.BiometricSettings));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.allowUsingDevicePIN, composeName(R.string.BiometricSettings, R.string.BiometricSettingsAllowDevicePIN)));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.allowUsingFaceUnlock, composeName(R.string.BiometricSettings, R.string.BiometricSettingsAllowFaceUnlock)));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.biometricAskEvery, composeName(R.string.BiometricSettings, R.string.BiometricAskEvery)));
        category.options.add(SettingsScanOption.asTitle(R.string.PhoneNumberPrivacy));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hidePhoneNumber, R.string.HidePhoneNumber));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hideOtherPhoneNumber, R.string.HideOtherPhoneNumber));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.phoneNumberAlternative, R.string.InsteadPhoneNumber));
    }

    private void fillExperimentsOptions(SettingsScanCategory category) {
        category.options.add(SettingsScanOption.asTitle(R.string.ExperimentalSettings));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.useFluentNavigationBar, R.string.UseFluentNavigationBar));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.moreHapticFeedbacks, R.string.MoreHapticFeedbacks));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.mediaInGroupCall, R.string.MediaStream));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.showRPCErrors, R.string.ShowRPCErrors));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.gcOutputType, R.string.AudioTypeInCall));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.photoResolution, R.string.PhotoResolution));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.maxRecentStickers, R.string.MaxRecentStickers));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.forceUseIpV6, R.string.TryConnectWithIPV6));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.deviceIdentifyState, R.string.DeviceIdentifyStatus));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.alternativeNavigation, R.string.AlternativeNavigation));
        category.options.add(SettingsScanOption.asTitle(R.string.AlternativeNavigation));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.navigationSmoothness, R.string.SmootherNavigation));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.animatedActionBar, R.string.AnimatedActionBar));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.navigationBounceLevel, R.string.BounceNavigation));
        category.options.add(SettingsScanOption.asTitle(R.string.Chats));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hideOpenButtonChatsList, R.string.HideOpenButtonChatsList));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.alwaysExpandBlockQuotes, R.string.AlwaysExpandBlockQuotes));
        category.options.add(SettingsScanOption.asTitle(R.string.DrawerHeaderAsBubble));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.profileBubbleHideBorder, R.string.ProfileBubbleHideBorder));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.profileBubbleMoreTopPadding, R.string.ProfileBubbleMoreTopPadding));
        category.options.add(SettingsScanOption.asTitle(R.string.DownloadAndUploadBoost));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.useQualityPreset, R.string.UseQualityPreset));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.keepOriginalFileName, R.string.KeepOriginalFileName));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.uploadBoost, R.string.UploadBoost));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.downloadBoost, R.string.DownloadBoost));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.downloadBoostValue, R.string.DownloadBoostType));
    }

    private void fillUpdatesOptions(SettingsScanCategory category) {
        category.options.add(SettingsScanOption.asTitle(R.string.UpdatesOptions));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.autoDownloadUpdatesStatus, R.string.UpdatesSettingsAutoDownload));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.autoCheckUpdateStatus, R.string.UpdatesSettingsAuto));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.preferBetaVersion, R.string.UpdatesSettingsBeta));
    }

    private void testSettings() {
        ArrayList<String> allKeys = new ArrayList<>();

        for (SettingsScanCategory category : categories) {
            for (SettingsScanOption option : category.options) {
                if (option.isTitle || option.property == null) {
                    continue;
                }
                allKeys.add(option.property.getKey());
            }
        }

        allKeys.addAll(excludedOptions);

        for (Field field : OctoConfig.INSTANCE.getClass().getDeclaredFields()) {
            if (field.getType().equals(ConfigProperty.class)) {
                try {
                    ConfigProperty<?> configProperty = (ConfigProperty<?>) field.get(OctoConfig.INSTANCE);

                    if (configProperty != null) {
                        String key = configProperty.getKey();
                        if (!allKeys.contains(key)) {
                            OctoLogging.e(OctoConfig.TAG, "Setting key " + key + " is not part of backup");
                        }
                    }
                } catch (IllegalAccessException ignored) {

                }
            }
        }
    }

    private String composeName(int string1, int string2, boolean isCategory) {
        return String.format(Locale.US, "%s%s%s", getString(string1), isCategory ? " — " : ": ", getString(string2).toLowerCase());
    }

    private String composeName(int string1, int string2) {
        return composeName(string1, string2, false);
    }

    public static class SettingsScanCategory {
        public String categoryId;
        public String categoryName;
        public int categoryResourceName;
        public int categoryIcon;
        public boolean isSecureContext = false;
        public ArrayList<SettingsScanOption> options = new ArrayList<>();
        public SettingsScanRequestInterface onGetFragment;
        public SettingsScanCategory refersTo;

        public SettingsScanCategory(String categoryId, String categoryName, int categoryIcon, SettingsScanRequestInterface onGetFragment) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.categoryIcon = categoryIcon;
            this.onGetFragment = onGetFragment;
        }

        public SettingsScanCategory(String categoryId, int categoryResourceName, int categoryIcon, SettingsScanRequestInterface onGetFragment) {
            this.categoryId = categoryId;
            this.categoryResourceName = categoryResourceName;
            this.categoryIcon = categoryIcon;
            this.onGetFragment = onGetFragment;
        }

        public SettingsScanCategory setRefersTo(SettingsScanCategory category) {
            this.refersTo = category;
            return this;
        }

        public String getName() {
            if (categoryName != null) {
                return categoryName;
            }

            return getString(categoryResourceName);
        }

        public interface SettingsScanRequestInterface {
            BaseFragment onCall(String t);

            default BaseFragment onCall() {
                return onCall(null);
            }
        }
    }

    public static class SettingsScanOption {
        public String optionName;
        public int optionResourceName;
        public ConfigProperty<?> property;

        public boolean isTitle = false;

        public SettingsScanOption(ConfigProperty<?> property, String optionName) {
            this.property = property;
            this.optionName = optionName;
        }

        public SettingsScanOption(ConfigProperty<?> property, int optionResourceName) {
            this.property = property;
            this.optionResourceName = optionResourceName;
        }

        public SettingsScanOption(String title) {
            optionName = title;
            isTitle = true;
        }

        public SettingsScanOption(int title) {
            optionResourceName = title;
            isTitle = true;
        }

        public static SettingsScanOption asTitle(String title) {
            return new SettingsScanOption(title);
        }

        public static SettingsScanOption asTitle(int title) {
            return new SettingsScanOption(title);
        }

        public String getName() {
            if (optionName != null) {
                return optionName;
            }

            return getString(optionResourceName);
        }
    }
}
