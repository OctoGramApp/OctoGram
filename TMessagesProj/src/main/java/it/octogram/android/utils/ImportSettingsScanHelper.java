package it.octogram.android.utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.R;

import java.util.ArrayList;

public class ImportSettingsScanHelper {
    public JSONObject data = new JSONObject();
    public ArrayList<String> excludedOptions = new ArrayList<>();
    public ArrayList<String> forceRequestReloadOptions = new ArrayList<>();

    public ImportSettingsScanHelper() {
        fillForceReloadOptions();
        fillExcludedOptions();

        try {
            data.put("general", getGeneralOptions());
            data.put("general_string", R.string.General);
            data.put("general_icon", R.drawable.msg_media);
            data.put("appearance", getAppearance());
            data.put("appearance_string", R.string.Appearance);
            data.put("appearance_icon", R.drawable.settings_appearance);
            data.put("chatcamera", getChatCamera());
            data.put("chatcamera_string", R.string.ChatCamera);
            data.put("chatcamera_icon", R.drawable.msg_camera);
            data.put("experiments", getExperiments());
            data.put("experiments_string", R.string.Experiments);
            data.put("experiments_icon", R.drawable.outline_science_white);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void fillForceReloadOptions() {
        forceRequestReloadOptions.clear();
        forceRequestReloadOptions.add("tabletMode");
        forceRequestReloadOptions.add("alternativeNavigation");
        forceRequestReloadOptions.add("hideOnlyAllChatsFolder");
        forceRequestReloadOptions.add("hideChatFolders");
        forceRequestReloadOptions.add("hideFoldersWhenForwarding");
        forceRequestReloadOptions.add("useSystemFont");
        forceRequestReloadOptions.add("hideStories");
        forceRequestReloadOptions.add("showSnowflakes");
    }

    private void fillExcludedOptions() {
        excludedOptions.clear();
        excludedOptions.add("unlockedYuki");
        excludedOptions.add("unlockedChupa");
        excludedOptions.add("selectedEmojiPack");
        excludedOptions.add("languagePackVersioning");
        excludedOptions.add("lastSelectedCompression");
        excludedOptions.add("drawer_changeStatus");
        excludedOptions.add("drawer_myStories");
        excludedOptions.add("drawer_newGroup");
        excludedOptions.add("drawer_newChannel");
        excludedOptions.add("drawer_contacts");
        excludedOptions.add("drawer_calls");
        excludedOptions.add("drawer_savedMessages");
        excludedOptions.add("drawer_octogramSettings");
        excludedOptions.add("drawer_inviteFriends");
        excludedOptions.add("drawer_telegramFeatures");
        excludedOptions.add("context_clearFromCache");
        excludedOptions.add("context_copyPhoto");
        excludedOptions.add("context_saveMessage");
        excludedOptions.add("context_reportMessage");
        excludedOptions.add("context_messageDetails");
        excludedOptions.add("context_noQuoteForward");
        excludedOptions.add("useTranslationsArgsFix");
    }

    private JSONObject getGeneralOptions() {
        JSONObject generalOptions = new JSONObject();
        try {
            generalOptions.put("hidePhoneNumber", R.string.HidePhoneNumber);
            generalOptions.put("hideOtherPhoneNumber", R.string.HideOtherPhoneNumber);
            generalOptions.put("showFakePhoneNumber", R.string.ShowFakePhoneNumber);
            generalOptions.put("showUsernameAsPhoneNumber", R.string.ShowUsernameAsPhoneNumber);
            generalOptions.put("promptBeforeCalling", R.string.PromptBeforeCalling);
            generalOptions.put("registrationDateInProfiles", R.string.ShowRegistrationDate);
            generalOptions.put("jumpToNextChannelOrTopic", R.string.JumpToNextChannelOrTopic);
            generalOptions.put("registrationDateInProfiles", R.string.ShowRegistrationDate);
            generalOptions.put("hideGreetingSticker", R.string.HideGreetingSticker);
            generalOptions.put("playGifAsVideo", R.string.PlayGifsAsVideo);
            generalOptions.put("hideKeyboardOnScroll", R.string.HideKeyboardOnScroll);
            generalOptions.put("hideSendAsChannel", R.string.HideSendAsChannel);
            generalOptions.put("showOnlineStatus", R.string.ShowOnlineStatus);
            generalOptions.put("hideCustomEmojis", R.string.HideCustomEmojis);
            generalOptions.put("openArchiveOnPull", R.string.OpenArchiveOnPull);
            generalOptions.put("warningBeforeDeletingChatHistory", R.string.WarningBeforeDeletingChatHistory);
            generalOptions.put("activeNoiseSuppression", R.string.VoipNoiseCancellation);
            generalOptions.put("unmuteVideosWithVolumeDown", R.string.UnmuteWithVolumeDown);
            generalOptions.put("disableProximityEvents", R.string.DisableProximitySensor);
            generalOptions.put("startWithRearCamera", R.string.StartWithRearCamera);
            generalOptions.put("disableCameraPreview", R.string.DisableCameraPreview);
            generalOptions.put("hideSentTimeOnStickers", R.string.RemoveTimeOnStickers);
            generalOptions.put("hideOnlyAllChatsFolder", R.string.HideAllChatFolder);
            generalOptions.put("hideChatFolders", R.string.HideAllChatFolders);
            generalOptions.put("hideFoldersWhenForwarding", R.string.HideChatFoldersWhenForwarding);
            generalOptions.put("doubleTapActionOut", R.string.PreferredActionOutgoing);
            generalOptions.put("doubleTapAction", R.string.PreferredActionIncoming);
            generalOptions.put("tabletMode", R.string.ForceTableMode);
            generalOptions.put("forceUseIpV6", R.string.TryConnectWithIPV6);
            generalOptions.put("accentColorAsNotificationColor", R.string.AccentColorAsNotificationColor);
            generalOptions.put("enableSmartNotificationsForPrivateChats", R.string.EnableSmartNotificationsForPrivateChats);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return generalOptions;
    }

    private JSONObject getAppearance() {
        JSONObject appearance = new JSONObject();
        try {
            appearance.put("eventType", R.string.OctoAppearanceSettingsHeader);
            appearance.put("useSystemEmoji", R.string.UseSystemEmojis);
            appearance.put("useSystemFont", R.string.UseSystemFont);
            appearance.put("formatTimeWithSeconds", R.string.FormatTimeWithSeconds);
            appearance.put("numberRounding", R.string.NumberRounding);
            appearance.put("pencilIconForEditedMessages", R.string.PencilIconForEdited);
            appearance.put("disableDividers", R.string.HideDividers);
            appearance.put("showNameInActionBar", R.string.ShowNameActionBar);
            appearance.put("showUserIconsInChatsList", R.string.ShowUserIconsInChatsList);
            appearance.put("searchIconInHeader", R.string.SearchIconInHeader);
            appearance.put("slidingTitle", R.string.SlidingTitle);
            appearance.put("hideStories", R.string.HideStories);
            appearance.put("forceChatBlurEffect", R.string.ForceChatBlurEffect);
            appearance.put("forceChatBlurEffect", R.string.ForceChatBlurEffect);
            appearance.put("blurEffectStrength", R.string.ForceChatBlurEffect);
            appearance.put("maxStickerSize", R.string.StickersSizeHeader);
            appearance.put("forcePacmanAnimation", R.string.ForcePacmanAnimation);
            appearance.put("showSnowflakes", R.string.ShowSnowflakes);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return appearance;
    }

    private JSONObject getChatCamera() {
        JSONObject chatCamera = new JSONObject();
        try {
            chatCamera.put("cameraXEnabled", R.string.UseCameraX);
            chatCamera.put("cameraXZeroShutter", R.string.ZeroShutter);
            chatCamera.put("cameraXPerfOverQuality", R.string.PerformanceMode);
            chatCamera.put("cameraXResolution", R.string.CurrentCameraXResolution);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return chatCamera;
    }

    private JSONObject getExperiments() {
        JSONObject experiments = new JSONObject();
        try {
            experiments.put("alternativeNavigation", R.string.AlternativeNavigation);
            experiments.put("mediaInGroupCall", R.string.MediaStream);
            experiments.put("showRPCErrors", R.string.ShowRPCErrors);
            experiments.put("gcOutputType", R.string.AudioTypeInCall);
            experiments.put("photoResolution", R.string.PhotoResolution);
            experiments.put("maxRecentStickers", R.string.MaxRecentStickers);
            experiments.put("uploadBoost", R.string.UploadBoost);
            experiments.put("downloadBoost", R.string.DownloadBoost);
            experiments.put("downloadBoostValue", R.string.DownloadBoostType);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return experiments;
    }
}
