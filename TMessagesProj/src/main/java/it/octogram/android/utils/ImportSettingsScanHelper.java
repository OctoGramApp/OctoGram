/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

package it.octogram.android.utils;

import com.google.android.exoplayer2.util.Log;

import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Supplier;

import it.octogram.android.ConfigProperty;
import it.octogram.android.OctoConfig;
import it.octogram.android.preferences.fragment.PreferencesFragment;
import it.octogram.android.preferences.ui.OctoAppearanceUI;
import it.octogram.android.preferences.ui.OctoCameraSettingsUI;
import it.octogram.android.preferences.ui.OctoChatsSettingsUI;
import it.octogram.android.preferences.ui.OctoDrawerSettingsUI;
import it.octogram.android.preferences.ui.OctoExperimentsUI;
import it.octogram.android.preferences.ui.OctoGeneralSettingsUI;
import it.octogram.android.preferences.ui.OctoTranslatorUI;
import it.octogram.android.preferences.ui.OctoUpdatesUI;

public class ImportSettingsScanHelper {
    public ArrayList<SettingsScanCategory> categories = new ArrayList<>();
    public ArrayList<String> excludedOptions = new ArrayList<>();

    public ImportSettingsScanHelper() {
        fillExcludedOptions();

        SettingsScanCategory generalCategory = new SettingsScanCategory("general", R.string.General, R.drawable.msg_media, () -> new PreferencesFragment(new OctoGeneralSettingsUI()));
        SettingsScanCategory translatorCategory = new SettingsScanCategory("translator", R.string.Translator, R.drawable.msg_translate, () -> new PreferencesFragment(new OctoTranslatorUI()));
        SettingsScanCategory appearanceCategory = new SettingsScanCategory("appearance", R.string.Appearance, R.drawable.settings_appearance, () -> new PreferencesFragment(new OctoAppearanceUI()));
        SettingsScanCategory appearanceChatsCategory = new SettingsScanCategory("appearanceChats", composeName(R.string.Appearance, R.string.ChatTitle, true), R.drawable.msg_groups, () -> new PreferencesFragment(new OctoChatsSettingsUI()));
        SettingsScanCategory appearanceDrawerCategory = new SettingsScanCategory("appearanceDrawer", composeName(R.string.Appearance, R.string.DrawerTitle, true), R.drawable.msg_map_type, () -> new PreferencesFragment(new OctoDrawerSettingsUI()));
        SettingsScanCategory chatCameraCategory = new SettingsScanCategory("chatcamera", R.string.ChatCamera, R.drawable.msg_camera, () -> new PreferencesFragment(new OctoCameraSettingsUI()));
        SettingsScanCategory experimentsCategory = new SettingsScanCategory("experiments", R.string.Experiments, R.drawable.outline_science_white, () -> new PreferencesFragment(new OctoExperimentsUI()));
        SettingsScanCategory updatesCategory = new SettingsScanCategory("updates", R.string.Updates, R.drawable.round_update_white_28, () -> new PreferencesFragment(new OctoUpdatesUI()));

        fillGeneralOptions(generalCategory);
        fillTranslatorOptions(translatorCategory);
        fillAppearanceOptions(appearanceCategory);
        fillAppearanceChatsOptions(appearanceChatsCategory);
        fillAppearanceDrawerOptions(appearanceDrawerCategory);
        fillChatCameraOptions(chatCameraCategory);
        fillExperimentsCategory(experimentsCategory);
        fillUpdatesCategory(updatesCategory);

        categories.add(generalCategory);
        categories.add(translatorCategory);
        categories.add(appearanceCategory);
        categories.add(appearanceChatsCategory);
        categories.add(appearanceDrawerCategory);
        categories.add(chatCameraCategory);
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
        excludedOptions.add(OctoConfig.INSTANCE.useTranslationsArgsFix.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.receivePBetaUpdates.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.mediaFiltering.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.isMigrateOldLogs.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.syncPowerSaver.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.experimentsEnabled.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.lastTranslatePreSendLanguage.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.newBadgeIds.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.updateSignalingLastBuildID.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.updateSignalingChangelog.getKey());
        excludedOptions.add(OctoConfig.INSTANCE.uiRandomMemeIcons.getKey());
    }

    private void fillGeneralOptions(SettingsScanCategory category) {
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hidePhoneNumber.getKey(), R.string.HidePhoneNumber));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hideOtherPhoneNumber.getKey(), R.string.HideOtherPhoneNumber));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.phoneNumberAlternative.getKey(), composeName(R.string.HidePhoneNumber, R.string.InsteadPhoneNumber)));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.promptBeforeCalling.getKey(), R.string.PromptBeforeCalling));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.warningBeforeDeletingChatHistory.getKey(), R.string.PromptBeforeDeletingChatHistory));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.registrationDateInProfiles.getKey(), R.string.ShowRegistrationDate));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.dcIdStyle.getKey(), composeName(R.string.DcIdHeader, R.string.Style)));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.dcIdType.getKey(), composeName(R.string.DcIdHeader, R.string.Type)));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.defaultEmojiButtonAction.getKey(), R.string.DefaultEmojiButtonType));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.jumpToNextChannelOrTopic.getKey(), R.string.JumpToNextChannelOrTopic));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hideGreetingSticker.getKey(), R.string.HideGreetingSticker));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hideKeyboardOnScroll.getKey(), R.string.HideKeyboardOnScroll));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hideSendAsChannel.getKey(), R.string.HideSendAsChannel));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.showOnlineStatus.getKey(), R.string.ShowOnlineStatus));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hideCustomEmojis.getKey(), R.string.HideCustomEmojis));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.openArchiveOnPull.getKey(), R.string.OpenArchiveOnPull));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.activeNoiseSuppression.getKey(), R.string.VoipNoiseCancellation));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.playGifAsVideo.getKey(), R.string.PlayGifsAsVideo));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.unmuteVideosWithVolumeDown.getKey(), R.string.UnmuteWithVolumeDown));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.disableProximityEvents.getKey(), R.string.DisableProximitySensor));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hideChatFolders.getKey(), R.string.HideAllChatFolders, true));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hideOnlyAllChatsFolder.getKey(), R.string.HideAllChatFolder, true));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hideFoldersWhenForwarding.getKey(), R.string.HideChatFoldersWhenForwarding));
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
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.tabMode.getKey(), R.string.FoldersType));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.useSystemEmoji.getKey(), R.string.UseSystemEmojis, true));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.useSystemFont.getKey(), R.string.UseSystemFont, true));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.showUserIconsInChatsList.getKey(), R.string.ShowUserIconsInChatsList));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.hideStories.getKey(), R.string.HideStories, true));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.actionBarTitleOption.getKey(), R.string.ActionBarTitle, true));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.actionBarCustomTitle.getKey(), R.string.ActionBarTitleCustom, true));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.disableDividers.getKey(), R.string.HideDividers));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.forcePacmanAnimation.getKey(), R.string.ForcePacmanAnimation));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.showSnowflakes.getKey(), R.string.ShowSnowflakes, true));
    }

    private void fillAppearanceChatsOptions(SettingsScanCategory category) {
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.maxStickerSize.getKey(), R.string.StickersSizeHeader));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.stickerShape.getKey(), R.string.StickerShape));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.repliesLinksShowColors.getKey(), composeName(R.string.RepliesLinksHeader, R.string.RepliesLinksShowColors)));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.repliesLinksShowEmojis.getKey(), composeName(R.string.RepliesLinksHeader, R.string.RepliesLinksShowEmojis)));
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
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.eventType.getKey(), R.string.EventType, true));
    }

    private void fillChatCameraOptions(SettingsScanCategory category) {
        //category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.cameraXEnabled.getKey(), R.string.UseCameraX));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.cameraXZeroShutter.getKey(), R.string.ZeroShutter));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.cameraXPerformanceMode.getKey(), R.string.PerformanceMode));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.cameraXResolution.getKey(), R.string.CurrentCameraXResolution));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.startWithRearCamera.getKey(), R.string.StartWithRearCamera));
        // category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.disableCameraPreview.getKey(), R.string.DisableCameraPreview));
    }

    private void fillExperimentsCategory(SettingsScanCategory category) {
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.mediaInGroupCall.getKey(), R.string.MediaStream));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.showRPCErrors.getKey(), R.string.ShowRPCErrors));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.gcOutputType.getKey(), R.string.AudioTypeInCall));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.photoResolution.getKey(), R.string.PhotoResolution));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.maxRecentStickers.getKey(), R.string.MaxRecentStickers));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.forceUseIpV6.getKey(), R.string.TryConnectWithIPV6));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.deviceIdentifyState.getKey(), R.string.DeviceIdentifyStatus));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.alternativeNavigation.getKey(), R.string.AlternativeNavigation, true));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.navigationSmoothness.getKey(), composeName(R.string.AlternativeNavigation, R.string.SmootherNavigation), true));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.animatedActionBar.getKey(), composeName(R.string.AlternativeNavigation, R.string.AnimatedActionBar), true));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.uploadBoost.getKey(), R.string.UploadBoost));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.downloadBoost.getKey(), R.string.DownloadBoost));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.downloadBoostValue.getKey(), R.string.DownloadBoostType));
    }

    private void fillUpdatesCategory(SettingsScanCategory category) {
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.autoDownloadUpdatesStatus.getKey(), R.string.UpdatesSettingsAutoDownload));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.autoCheckUpdateStatus.getKey(), R.string.UpdatesSettingsAuto));
        category.options.add(new SettingsScanOption(OctoConfig.INSTANCE.preferBetaVersion.getKey(), R.string.UpdatesSettingsBeta));
    }

    private void testSettings() {
        for (SettingsScanCategory category : categories) {
            for (SettingsScanOption option : category.options) {
                if (isSettingKeyInvalid(option.optionKey)) {
                    Log.e(OctoConfig.TAG, "Setting key " + option.optionKey + " is not valid - part of " + category.categoryId);
                }
            }
        }

        for (String excluded : excludedOptions) {
            if (isSettingKeyInvalid(excluded)) {
                Log.e(OctoConfig.TAG, "Excluded option " + excluded + " is not valid");
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
        if (isCategory) {
            return String.format("%s — %s", LocaleController.getString(string1), LocaleController.getString(string2));
        }

        return String.format("%s: %s", LocaleController.getString(string1), LocaleController.getString(string2).toLowerCase());
    }

    private String composeName(int string1, int string2) {
        return composeName(string1, string2, false);
    }

    public static class SettingsScanCategory {
        public String categoryId;
        public String categoryName;
        public int categoryResourceName;
        public int categoryIcon;
        public ArrayList<SettingsScanOption> options = new ArrayList<>();
        public Supplier<BaseFragment> onGetFragment;

        public SettingsScanCategory(String categoryId, String categoryName, int categoryIcon, Supplier<BaseFragment> onGetFragment) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.categoryIcon = categoryIcon;
            this.onGetFragment = onGetFragment;
        }

        public SettingsScanCategory(String categoryId, int categoryResourceName, int categoryIcon, Supplier<BaseFragment> onGetFragment) {
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
    }

    public static class SettingsScanOption {
        public String optionName;
        public int optionResourceName;
        public String optionKey;
        public boolean optionRequiresRestart;

        public SettingsScanOption(String optionKey, String optionName) {
            this.optionKey = optionKey;
            this.optionName = optionName;
        }

        public SettingsScanOption(String optionKey, String optionName, boolean optionRequiresRestart) {
            this.optionKey = optionKey;
            this.optionName = optionName;
            this.optionRequiresRestart = optionRequiresRestart;
        }

        public SettingsScanOption(String optionKey, int optionResourceName) {
            this.optionKey = optionKey;
            this.optionResourceName = optionResourceName;
        }

        public SettingsScanOption(String optionKey, int optionResourceName, boolean optionRequiresRestart) {
            this.optionKey = optionKey;
            this.optionResourceName = optionResourceName;
            this.optionRequiresRestart = optionRequiresRestart;
        }

        public String getName() {
            if (optionName != null) {
                return optionName;
            }

            return LocaleController.getString(optionResourceName);
        }
    }
}
