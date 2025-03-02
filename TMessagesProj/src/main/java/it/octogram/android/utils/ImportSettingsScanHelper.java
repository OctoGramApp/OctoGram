/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils;

import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.BaseFragment;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Objects;

import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.logs.OctoLogging;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.ui.OctoAppearanceUI;
import it.octogram.android.preferences.ui.OctoCameraSettingsUI;
import it.octogram.android.preferences.ui.OctoChatsSettingsUI;
import it.octogram.android.preferences.ui.OctoDrawerSettingsUI;
import it.octogram.android.preferences.ui.OctoExperimentsUI;
import it.octogram.android.preferences.ui.OctoGeneralSettingsUI;
import it.octogram.android.preferences.ui.OctoInterfaceSettingsUI;
import it.octogram.android.preferences.ui.OctoPrivacySettingsUI;
import it.octogram.android.preferences.ui.OctoTranslatorUI;
import it.octogram.android.preferences.ui.OctoUpdatesUI;

public class ImportSettingsScanHelper {
    public ArrayList<SettingsScanCategory> categories = new ArrayList<>();
    public ArrayList<String> excludedOptions = new ArrayList<>();

    public ImportSettingsScanHelper() {
        fillExcludedOptions();

        SettingsScanCategory generalCategory = new SettingsScanCategory("general", R.string.General, R.drawable.msg_media, (t) -> new PreferencesFragment(new OctoGeneralSettingsUI(), t));
        SettingsScanCategory translatorCategory = new SettingsScanCategory("translator", R.string.Translator, R.drawable.msg_translate, (t) -> new PreferencesFragment(new OctoTranslatorUI(), t));
        SettingsScanCategory appearanceCategory = new SettingsScanCategory("appearance", R.string.Appearance, R.drawable.settings_appearance, (t) -> new PreferencesFragment(new OctoAppearanceUI(), t));
        SettingsScanCategory appearanceChatsCategory = new SettingsScanCategory("appearanceChats", composeName(R.string.Appearance, R.string.ChatTitle, true), R.drawable.msg_groups, (t) -> new PreferencesFragment(new OctoChatsSettingsUI(), t));
        SettingsScanCategory appearanceDrawerCategory = new SettingsScanCategory("appearanceDrawer", composeName(R.string.Appearance, R.string.DrawerTitle, true), R.drawable.msg_map_type, (t) -> new PreferencesFragment(new OctoDrawerSettingsUI(), t));
        SettingsScanCategory appearanceAppCategory = new SettingsScanCategory("appearanceApp", composeName(R.string.Appearance, R.string.AppTitleSettings, true), R.drawable.media_draw, (t) -> new PreferencesFragment(new OctoInterfaceSettingsUI(), t));
        SettingsScanCategory chatCameraCategory = new SettingsScanCategory("chatcamera", R.string.ChatCamera, R.drawable.msg_camera, (t) -> new PreferencesFragment(new OctoCameraSettingsUI(), t));
        SettingsScanCategory privacyCategory = new SettingsScanCategory("privacy", R.string.PrivacySettings, R.drawable.menu_privacy, (t) -> new PreferencesFragment(new OctoPrivacySettingsUI(), t));
        SettingsScanCategory experimentsCategory = new SettingsScanCategory("experiments", R.string.Experiments, R.drawable.outline_science_white, (t) -> new PreferencesFragment(new OctoExperimentsUI(), t));
        SettingsScanCategory updatesCategory = new SettingsScanCategory("updates", R.string.Updates, R.drawable.round_update_white_28, (t) -> new PreferencesFragment(new OctoUpdatesUI(), t));

        fillGeneralOptions(generalCategory);
        fillTranslatorOptions(translatorCategory);
        fillAppearanceOptions(appearanceCategory);
        fillAppearanceChatsOptions(appearanceChatsCategory);
        fillAppearanceDrawerOptions(appearanceDrawerCategory);
        fillAppearanceAppOptions(appearanceAppCategory);
        fillChatCameraOptions(chatCameraCategory);
        fillPrivacyOptions(privacyCategory);
        fillExperimentsOptions(experimentsCategory);
        fillUpdatesOptions(updatesCategory);

        categories.add(generalCategory);
        categories.add(translatorCategory);
        categories.add(appearanceCategory);
        categories.add(appearanceChatsCategory);
        categories.add(appearanceDrawerCategory);
        categories.add(appearanceAppCategory);
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
        excludedOptions.add(OctoConfig.INSTANCE.uiRandomMemeIcons.getKey());
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
    }

    private void fillGeneralOptions(SettingsScanCategory category) {
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.registrationDateInProfiles.getKey(), R.string.ShowRegistrationDate));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.dcIdStyle.getKey(), composeName(R.string.DcIdHeader, R.string.Style)));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.dcIdType.getKey(), composeName(R.string.DcIdHeader, R.string.Type)));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.defaultEmojiButtonAction.getKey(), R.string.DefaultEmojiButtonType));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.jumpToNextChannelOrTopic.getKey(), R.string.JumpToNextChannelOrTopic));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.swipeToPip.getKey(), R.string.SwipeToPIP));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hideGreetingSticker.getKey(), R.string.HideGreetingSticker));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hideKeyboardOnScroll.getKey(), R.string.HideKeyboardOnScroll));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hideSendAsChannel.getKey(), R.string.HideSendAsChannel));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.showOnlineStatus.getKey(), R.string.ShowOnlineStatus));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hideCustomEmojis.getKey(), R.string.HideCustomEmojis));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.openArchiveOnPull.getKey(), R.string.OpenArchiveOnPull));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.activeNoiseSuppression.getKey(), R.string.VoipNoiseCancellation));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.playGifAsVideo.getKey(), R.string.PlayGifsAsVideo));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.unmuteVideosWithVolumeDown.getKey(), R.string.UnmuteWithVolumeDown));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hideOnlyAllChatsFolder.getKey(), R.string.HideAllChatFolder));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.doubleTapActionOut.getKey(), R.string.PreferredActionOutgoing));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.doubleTapAction.getKey(), R.string.PreferredActionIncoming));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.accentColorAsNotificationColor.getKey(), R.string.AccentColorAsNotificationColor));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.enableSmartNotificationsForPrivateChats.getKey(), R.string.EnableSmartNotificationsForPrivateChats));
    }

    private void fillTranslatorOptions(SettingsScanCategory category) {
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.translatorMode.getKey(), R.string.TranslatorMode));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.translatorProvider.getKey(), R.string.TranslatorProvider));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.translatorFormality.getKey(), R.string.TranslatorFormality));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.translatorKeepMarkdown.getKey(), R.string.TranslatorKeepMarkdown));
    }

    private void fillAppearanceOptions(SettingsScanCategory category) {
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.shortcutsPosition.getKey(), R.string.AdminShortcutsPosition));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.useSystemEmoji.getKey(), R.string.UseSystemEmojis));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.useSystemFont.getKey(), R.string.UseSystemFont));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.showUserIconsInChatsList.getKey(), R.string.ShowUserIconsInChatsList));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hideStories.getKey(), R.string.HideStories));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.alwaysShowDownloads.getKey(), R.string.AlwaysShowDownloads));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.actionBarTitleOption.getKey(), R.string.ActionBarTitle));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.actionBarCustomTitle.getKey(), R.string.ActionBarTitleCustom));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.disableDividers.getKey(), R.string.HideDividers));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.forcePacmanAnimation.getKey(), R.string.ForcePacmanAnimation));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.showSnowflakes.getKey(), R.string.ShowSnowflakes));
    }

    private void fillAppearanceChatsOptions(SettingsScanCategory category) {
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.maxStickerSize.getKey(), R.string.StickersSizeHeader));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.stickerShape.getKey(), R.string.StickerShape));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.repliesLinksShowColors.getKey(), composeName(R.string.RepliesLinksHeader, R.string.RepliesLinksShowColors)));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.repliesLinksShowEmojis.getKey(), composeName(R.string.RepliesLinksHeader, R.string.RepliesLinksShowEmojis)));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.promptBeforeSendingStickers.getKey(), composeName(R.string.PromptBeforeSending, R.string.PromptBeforeSendingStickers)));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.promptBeforeSendingGIFs.getKey(), composeName(R.string.PromptBeforeSending, R.string.PromptBeforeSendingGIFs)));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.promptBeforeSendingVoiceMessages.getKey(), composeName(R.string.PromptBeforeSending, R.string.PromptBeforeSendingVoiceMessages)));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.promptBeforeSendingVideoMessages.getKey(), composeName(R.string.PromptBeforeSending, R.string.PromptBeforeSendingVideoMessages)));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hideSentTimeOnStickers.getKey(), R.string.RemoveTimeOnStickers));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.formatTimeWithSeconds.getKey(), R.string.FormatTimeWithSeconds));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.numberRounding.getKey(), R.string.NumberRounding));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.pencilIconForEditedMessages.getKey(), R.string.PencilIconForEdited));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.searchIconInHeader.getKey(), R.string.SearchIconInHeader));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.slidingTitle.getKey(), R.string.SlidingTitle));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.forceChatBlurEffect.getKey(), R.string.ForceChatBlurEffect));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.blurEffectStrength.getKey(), composeName(R.string.BlurHeader, R.string.ForceChatBlurEffectName)));
    }

    private void fillAppearanceDrawerOptions(SettingsScanCategory category) {
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.drawerBackground.getKey(), R.string.DrawerBackground));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.drawerShowProfilePic.getKey(), R.string.DrawerShowProfilePic));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.drawerGradientBackground.getKey(), R.string.DrawerGradientBackground));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.drawerFavoriteOption.getKey(), R.string.DrawerFavoriteOption));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.drawerDarkenBackground.getKey(), R.string.DrawerDarkenBackground));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.drawerDarkenBackgroundLevel.getKey(), R.string.DrawerDarkenBackgroundLevel));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.drawerBlurBackground.getKey(), R.string.DrawerBlurBackground));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.drawerBlurBackgroundLevel.getKey(), R.string.DrawerBlurBackgroundLevel));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.drawerItems.getKey(), R.string.DrawerElements));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.eventType.getKey(), R.string.EventType));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.drawerProfileAsBubble.getKey(), R.string.DrawerHeaderAsBubble));
    }

    private void fillAppearanceAppOptions(SettingsScanCategory category) {
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.uiTitleCenteredState.getKey(), R.string.ImproveInterfaceTitleCentered));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.uiImmersivePopups.getKey(), R.string.ImproveInterfaceImmersivePopups));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.interfaceSwitchUI.getKey(), R.string.ImproveInterfaceSwitch));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.interfaceCheckboxUI.getKey(), R.string.ImproveInterfaceCheckbox));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.interfaceSliderUI.getKey(), R.string.ImproveInterfaceSlider));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.tabMode.getKey(), composeName(R.string.ManageFolders, R.string.FolderType)));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.tabStyle.getKey(), composeName(R.string.ManageFolders, R.string.FolderStyle)));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hideUnreadCounterOnFolder.getKey(), composeName(R.string.ManageFolders, R.string.HideUnreadCounter)));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.showFoldersMessagesCounter.getKey(), composeName(R.string.ManageFolders, R.string.ShowMessagesCounter)));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.includeMutedChatsInCounter.getKey(), composeName(R.string.ManageFolders, R.string.IncludeMutedChats)));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.uiIconsType.getKey(), R.string.ImproveIconsShort));
    }

    private void fillChatCameraOptions(SettingsScanCategory category) {
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.cameraType.getKey(), R.string.CameraType));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.cameraXZeroShutter.getKey(), R.string.ZeroShutter));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.cameraXPerformanceMode.getKey(), R.string.PerformanceMode));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.cameraXResolution.getKey(), R.string.CurrentCameraXResolution));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.startWithRearCamera.getKey(), R.string.StartWithRearCamera));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.cameraPreview.getKey(), R.string.CameraButtonPosition));
    }

    private void fillPrivacyOptions(SettingsScanCategory category) {
        category.setCanShow(FingerprintUtils::hasFingerprint);
        category.isSecureContext = true;
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.biometricOpenArchive.getKey(), R.string.BiometricSettingsOpenArchive));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.biometricOpenCallsLog.getKey(), R.string.BiometricSettingsOpenCallsLog));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.biometricOpenSavedMessages.getKey(), R.string.BiometricSettingsOpenSavedMessages));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.biometricOpenSecretChats.getKey(), R.string.BiometricSettingsOpenSecretChats));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.allowUsingDevicePIN.getKey(), composeName(R.string.BiometricSettings, R.string.BiometricSettingsAllowDevicePIN)));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.allowUsingFaceUnlock.getKey(), composeName(R.string.BiometricSettings, R.string.BiometricSettingsAllowFaceUnlock)));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.biometricAskEvery.getKey(), composeName(R.string.BiometricSettings, R.string.BiometricAskEvery)));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.promptBeforeCalling.getKey(), composeName(R.string.Warnings, R.string.PromptBeforeCalling)));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.warningBeforeDeletingChatHistory.getKey(), composeName(R.string.Warnings, R.string.PromptBeforeDeletingChatHistory)));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hidePhoneNumber.getKey(), R.string.HidePhoneNumber));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hideOtherPhoneNumber.getKey(), R.string.HideOtherPhoneNumber));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.phoneNumberAlternative.getKey(), composeName(R.string.PhoneNumberPrivacy, R.string.InsteadPhoneNumber)));
    }

    private void fillExperimentsOptions(SettingsScanCategory category) {
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.mediaInGroupCall.getKey(), R.string.MediaStream));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.showRPCErrors.getKey(), R.string.ShowRPCErrors));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.gcOutputType.getKey(), R.string.AudioTypeInCall));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.photoResolution.getKey(), R.string.PhotoResolution));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.maxRecentStickers.getKey(), R.string.MaxRecentStickers));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.forceUseIpV6.getKey(), R.string.TryConnectWithIPV6));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.deviceIdentifyState.getKey(), R.string.DeviceIdentifyStatus));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.alternativeNavigation.getKey(), R.string.AlternativeNavigation));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.navigationSmoothness.getKey(), composeName(R.string.AlternativeNavigation, R.string.SmootherNavigation)));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.animatedActionBar.getKey(), composeName(R.string.AlternativeNavigation, R.string.AnimatedActionBar)));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hideBottomBarChannels.getKey(), R.string.HideBottomBarChannels));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hideOpenButtonChatsList.getKey(), R.string.HideOpenButtonChatsList));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.alwaysExpandBlockQuotes.getKey(), R.string.AlwaysExpandBlockQuotes));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.profileBubbleHideBorder.getKey(), composeName(R.string.DrawerHeaderAsBubble, R.string.ProfileBubbleHideBorder)));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.profileBubbleMoreTopPadding.getKey(), composeName(R.string.DrawerHeaderAsBubble, R.string.ProfileBubbleMoreTopPadding)));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.useQualityPreset.getKey(), R.string.UseQualityPreset));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.uploadBoost.getKey(), R.string.UploadBoost));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.downloadBoost.getKey(), R.string.DownloadBoost));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.downloadBoostValue.getKey(), R.string.DownloadBoostType));
    }

    private void fillUpdatesOptions(SettingsScanCategory category) {
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.autoDownloadUpdatesStatus.getKey(), R.string.UpdatesSettingsAutoDownload));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.autoCheckUpdateStatus.getKey(), R.string.UpdatesSettingsAuto));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.preferBetaVersion.getKey(), R.string.UpdatesSettingsBeta));
    }

    private void testSettings() {
        ArrayList<String> allKeys = new ArrayList<>();

        for (SettingsScanCategory category : categories) {
            for (SettingsScanOption option : category.options) {
                allKeys.add(option.optionKey);
                if (isSettingKeyInvalid(option.optionKey)) {
                    OctoLogging.e(OctoConfig.TAG, "Setting key " + option.optionKey + " is not valid - part of " + category.categoryId);
                }
            }
        }

        for (String excluded : excludedOptions) {
            allKeys.add(excluded);
            if (isSettingKeyInvalid(excluded)) {
                OctoLogging.e(OctoConfig.TAG, "Excluded option " + excluded + " is not valid");
            }
        }

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

    private boolean isSettingKeyInvalid(String key) {
        boolean isValid = false;

        for (Field field : OctoConfig.INSTANCE.getClass().getDeclaredFields()) {
            if (field.getType().equals(ConfigProperty.class)) {
                try {
                    ConfigProperty<?> configProperty = (ConfigProperty<?>) field.get(OctoConfig.INSTANCE);

                    if (configProperty != null && Objects.equals(configProperty.getKey(), key)) {
                        isValid = true;
                    }
                } catch (IllegalAccessException ignored) {

                }
            }
        }

        return !isValid;
    }

    private String composeName(int string1, int string2, boolean isCategory) {
        return String.format("%s%s%s", LocaleController.getString(string1), isCategory ? " — " : ": ", LocaleController.getString(string2).toLowerCase());
    }

    private String composeName(int string1, int string2) {
        return composeName(string1, string2, false);
    }

    public static class SettingsScanCategory {
        public String categoryId;
        public String categoryName;
        public int categoryResourceName;
        public int categoryIcon;
        private Utilities.CallbackVoidReturn<Boolean> canShow;
        public boolean isSecureContext = false;
        public ArrayList<SettingsScanOption> options = new ArrayList<>();
        public SettingsScanRequestInterface onGetFragment;

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

        public String getName() {
            if (categoryName != null) {
                return categoryName;
            }

            return LocaleController.getString(categoryResourceName);
        }

        public void setCanShow(Utilities.CallbackVoidReturn<Boolean> callback) {
            this.canShow = callback;
        }

        public boolean canShow() {
            if (canShow == null) {
                return true;
            }

            return canShow.run();
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
        public String optionKey;

        public SettingsScanOption(String optionKey, String optionName) {
            this.optionKey = optionKey;
            this.optionName = optionName;
        }

        public SettingsScanOption(String optionKey, int optionResourceName) {
            this.optionKey = optionKey;
            this.optionResourceName = optionResourceName;
        }

        public String getName() {
            if (optionName != null) {
                return optionName;
            }

            return LocaleController.getString(optionResourceName);
        }
    }
}
