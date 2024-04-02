/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */
package it.octogram.android;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.AudioFormat;

import org.telegram.messenger.ApplicationLoader;

import java.util.ArrayList;
import java.util.List;


/*
 * Super big TODO list:
 *
 * - Option to delete account
 * - Option to delete all personal messages in a chat
 * - two-way translation. And automatically turn over the text sent by the user
 * - custom double tap action
 * - zalgo filters?
 * -
 */
public class OctoConfig {

    public static final OctoConfig INSTANCE = new OctoConfig();
    private final List<ConfigProperty<?>> properties = new ArrayList<>();
    private final SharedPreferences PREFS = ApplicationLoader.applicationContext.getSharedPreferences("octoconfig", Activity.MODE_PRIVATE);

    /*General*/
    public final ConfigProperty<Boolean> hidePhoneNumber = newConfigProperty("hidePhoneNumber", true);
    public final ConfigProperty<Boolean> showFakePhoneNumber = newConfigProperty("showFakePhoneNumber", false);
    public final ConfigProperty<Boolean> showUsernameAsPhoneNumber = newConfigProperty("showUsernameAsPhoneNumber", false);
    public final ConfigProperty<Boolean> hideOtherPhoneNumber = newConfigProperty("hideOtherPhoneNumber", true);
    public final ConfigProperty<Boolean> promptBeforeCalling = newConfigProperty("promptBeforeCalling", true);
    public final ConfigProperty<Integer> dcIdStyle = newConfigProperty("dcIdStyle", DcIdStyle.TELEGRAM);
    public final ConfigProperty<Integer> dcIdType = newConfigProperty("dcIdType", DcIdType.BOT_API);
    public final ConfigProperty<Boolean> registrationDateInProfiles = newConfigProperty("registrationDateInProfiles", false);
    public final ConfigProperty<Boolean> jumpToNextChannel = newConfigProperty("jumpToNextChannel", true);
    public final ConfigProperty<Boolean> hideGreetingSticker = newConfigProperty("hideGreetingSticker", false);
    public final ConfigProperty<Boolean> playGifAsVideo = newConfigProperty("playGifAsVideo", false);
    public final ConfigProperty<Boolean> hideKeyboardOnScroll = newConfigProperty("hideKeyboardOnScroll", false);
    public final ConfigProperty<Boolean> hideSendAsChannel = newConfigProperty("hideSendAsChannel", false);
    public final ConfigProperty<Boolean> showOnlineStatus = newConfigProperty("showOnlineStatus", false);
    public final ConfigProperty<Boolean> hideCustomEmojis = newConfigProperty("hideCustomEmojis", false);
    public final ConfigProperty<Boolean> activeNoiseSuppression = newConfigProperty("activeNoiseSuppression", false);
    public final ConfigProperty<Boolean> unmuteVideosWithVolumeDown = newConfigProperty("unmuteVideosWithVolumeDown", true);
    public final ConfigProperty<Boolean> disableProximityEvents = newConfigProperty("disableProximityEvents", false);
    public final ConfigProperty<Boolean> startWithRearCamera = newConfigProperty("startWithRearCamera", false);
    public final ConfigProperty<Boolean> disableCameraPreview = newConfigProperty("disableCameraPreview", false);
    public final ConfigProperty<Boolean> hideSentTimeOnStickers = newConfigProperty("hideSentTimeOnStickers", false);
    public final ConfigProperty<Boolean> hideOnlyAllChatsFolder = newConfigProperty("hideOnlyAllChatsFolder", false);
    public final ConfigProperty<Boolean> hideChatFolders = newConfigProperty("hideChatFolders", false);
    public final ConfigProperty<Boolean> hideStories = newConfigProperty("hideStories", false);
    public final ConfigProperty<Integer> doubleTapAction = newConfigProperty("doubleTapAction", DoubleTapAction.REACTION);
    public final ConfigProperty<Integer> doubleTapActionOut = newConfigProperty("doubleTapActionOut", DoubleTapAction.REACTION);
    public final ConfigProperty<Boolean> hideFoldersWhenForwarding = newConfigProperty("showFoldersWhenForwarding", false);
    public final ConfigProperty<Boolean> accentColorAsNotificationColor = newConfigProperty("accentColorAsNotificationColor", false);
    public final ConfigProperty<Boolean> openArchiveOnPull = newConfigProperty("openArchiveOnPull", false);
    public final ConfigProperty<Boolean> tabletMode = newConfigProperty("tabletMode", false);
    public final ConfigProperty<Boolean> forceUseIpV6 = newConfigProperty("forceUseIpV6", false);
    public final ConfigProperty<Boolean> warningBeforeDeletingChatHistory = newConfigProperty("warningBeforeDeletingChatHistory", true);

    /*Appearance*/
    public final ConfigProperty<Boolean> showNameInActionBar = newConfigProperty("showNameInActionBar", false);
    public final ConfigProperty<Boolean> forceChatBlurEffect = newConfigProperty("forceChatBlurEffect", false);
    public final ConfigProperty<Integer> blurEffectStrength = newConfigProperty("blurEffectStrength", 155);
    public final ConfigProperty<Boolean> forcePacmanAnimation = newConfigProperty("forcePacmanAnimation", false);
    public final ConfigProperty<Boolean> formatTimeWithSeconds = newConfigProperty("formatTimeWithSeconds", false);
    public final ConfigProperty<Boolean> numberRounding = newConfigProperty("numberRounding", false);
    public final ConfigProperty<Boolean> pencilIconForEditedMessages = newConfigProperty("pencilIconForEditedMessages", false);
    public final ConfigProperty<Boolean> searchIconInHeader = newConfigProperty("searchIconInHeader", false);
    public final ConfigProperty<Boolean> slidingTitle = newConfigProperty("slidingTitle", false);
    public final ConfigProperty<Integer> eventType = newConfigProperty("eventType", EventType.NONE);
    public final ConfigProperty<Integer> maxStickerSize = newConfigProperty("maxStickerSize", 14);
    public final ConfigProperty<Boolean> useSystemFont = newConfigProperty("useSystemFont", false);
    public final ConfigProperty<Boolean> useSystemEmoji = newConfigProperty("useSystemEmoji", false);
    public final ConfigProperty<String> selectedEmojiPack = newConfigProperty("selectedEmojiPack", "default");
    public final ConfigProperty<Boolean> showSnowflakes = newConfigProperty("showSnowflakes", false);
    public final ConfigProperty<Boolean> disableDividers = newConfigProperty("disableDividers", false);
    /*Folders*/
    public final ConfigProperty<Integer> tabMode = newConfigProperty("tabMode", TabMode.MIXED);
    /*Drawer elements*/
    public final ConfigProperty<Boolean> drawerChangeStatus = newConfigProperty("drawer_changeStatus", true);
    public final ConfigProperty<Boolean> drawerMyStories = newConfigProperty("drawer_myStories", true);
    public final ConfigProperty<Boolean> drawerNewGroup = newConfigProperty("drawer_newGroup", true);
    public final ConfigProperty<Boolean> drawerNewChannel = newConfigProperty("drawer_newChannel", false);
    public final ConfigProperty<Boolean> drawerContacts = newConfigProperty("drawer_contacts", true);
    public final ConfigProperty<Boolean> drawerCalls = newConfigProperty("drawer_calls", true);
    public final ConfigProperty<Boolean> drawerPeopleNearby = newConfigProperty("drawer_peopleNearby", true);
    public final ConfigProperty<Boolean> drawerSavedMessages = newConfigProperty("drawer_savedMessages", true);
    public final ConfigProperty<Boolean> drawerOctogramSettings = newConfigProperty("drawer_octogramSettings", false);
    public final ConfigProperty<Boolean> drawerDatacenterInfo = newConfigProperty("drawer_datacenterInfo", true);
    public final ConfigProperty<Boolean> drawerInviteFriends = newConfigProperty("drawer_inviteFriends", true);
    public final ConfigProperty<Boolean> drawerTelegramFeatures = newConfigProperty("drawer_telegramFeatures", true);
    /*Context menu elements*/
    public final ConfigProperty<Boolean> contextClearFromCache = newConfigProperty("context_clearFromCache", false);
    public final ConfigProperty<Boolean> contextCopyPhoto = newConfigProperty("context_copyPhoto", true);
    public final ConfigProperty<Boolean> contextSaveMessage = newConfigProperty("context_saveMessage", false);
    public final ConfigProperty<Boolean> contextReportMessage = newConfigProperty("context_reportMessage", true);
    public final ConfigProperty<Boolean> contextMessageDetails = newConfigProperty("context_messageDetails", true);
    public final ConfigProperty<Boolean> contextNoQuoteForward = newConfigProperty("context_noQuoteForward", false);

    /*Unlock Secret Icons*/
    public final ConfigProperty<Boolean> unlockedYuki = newConfigProperty("unlockedYuki", false);
    public final ConfigProperty<Boolean> unlockedChupa = newConfigProperty("unlockedChupa", false);

    /*CameraX*/
    public final ConfigProperty<Boolean> cameraXEnabled = newConfigProperty("cameraXEnabled", true);
    public final ConfigProperty<Boolean> cameraXPerfOverQuality = newConfigProperty("cameraXPerformanceMode", false);
    public final ConfigProperty<Boolean> cameraXZeroShutter = newConfigProperty("cameraXZeroShutter", false);
    public final ConfigProperty<Integer> cameraXResolution = newConfigProperty("cameraXResolution", -1);

    /*Experiments*/
    public final ConfigProperty<Boolean> experimentsEnabled = newConfigProperty("experimentsEnabled", false);
    public final ConfigProperty<Boolean> alternativeNavigation = newConfigProperty("alternativeNavigation", false);
    public final ConfigProperty<Boolean> uploadBoost = newConfigProperty("uploadBoost", false);
    public final ConfigProperty<Boolean> downloadBoost = newConfigProperty("downloadBoost", false);
    public final ConfigProperty<Integer> downloadBoostValue = newConfigProperty("downloadBoostValue", DownloadBoost.NORMAL);
    public final ConfigProperty<Integer> photoResolution = newConfigProperty("photoResolution", PhotoResolution.DEFAULT);
    public final ConfigProperty<Integer> lastSelectedCompression = newConfigProperty("lastSelectedCompression", 3);
    public final ConfigProperty<Integer> gcOutputType = newConfigProperty("gcOutputType", AudioType.MONO);
    public final ConfigProperty<Boolean> mediaInGroupCall = newConfigProperty("mediaInGroupCall", false);
    public final ConfigProperty<Integer> maxRecentStickers = newConfigProperty("maxRecentStickers", 0);
    public final ConfigProperty<Boolean> showRPCErrors = newConfigProperty("showRPCErrors", false);

    /* Multi-Language */
    public final ConfigProperty<String> languagePackVersioning = newConfigProperty("languagePackVersioning", "{}");
    /**
     * Creates a new config property and adds it to the list of properties.
     *
     * @param key          The key of the property.
     * @param defaultValue The default value of the property.
     * @param <T>          The type of the property.
     * @return The newly created property.
     */
    private <T> ConfigProperty<T> newConfigProperty(String key, T defaultValue) {
        ConfigProperty<T> property = new ConfigProperty<>(key, defaultValue);
        properties.add(property);
        return property;
    }

    private OctoConfig() {
        loadConfig();
    }

    public static int getMaxRecentSticker() {
        int[] sizes = {20, 30, 40, 50, 80, 100, 120, 150, 180, 200};
        Integer value = OctoConfig.INSTANCE.maxRecentStickers.getValue();
        if (value >= 0 && value < sizes.length) {
            return sizes[value];
        }
        return 20;
    }

    public static int getAudioType() {
        if (OctoConfig.INSTANCE.gcOutputType.getValue() == AudioType.MONO) {
            return AudioFormat.CHANNEL_OUT_MONO;
        } else {
            return AudioFormat.CHANNEL_OUT_STEREO;
        }
    }

    /*
     * It is safe to suppress this warning because the method loadConfig() is only called once in the static block above.
     * Also the semantics of the data structure is pretty solid, so there is no need to worry about it.
     */
    @SuppressWarnings("unchecked")
    private void loadConfig() {
        synchronized (this) {
            for (ConfigProperty<?> property : properties) {
                if (property.getValue() instanceof Boolean) {
                    ConfigProperty<Boolean> booleanProperty = (ConfigProperty<Boolean>) property;
                    booleanProperty.setValue(PREFS.getBoolean(booleanProperty.getKey(), booleanProperty.getValue()));
                } else if (property.getValue() instanceof String) {
                    ConfigProperty<String> stringProperty = (ConfigProperty<String>) property;
                    stringProperty.setValue(PREFS.getString(stringProperty.getKey(), stringProperty.getValue()));
                } else if (property.getValue() instanceof Integer) {
                    ConfigProperty<Integer> integerProperty = (ConfigProperty<Integer>) property;
                    integerProperty.setValue(PREFS.getInt(integerProperty.getKey(), integerProperty.getValue()));
                }
            }
        }
    }

    public void toggleBooleanSetting(ConfigProperty<Boolean> property) {
        updateBooleanSetting(property, !property.getValue());
    }

    public void updateBooleanSetting(ConfigProperty<Boolean> property, boolean value) {
        property.setValue(value);

        SharedPreferences.Editor editor = PREFS.edit();
        editor.putBoolean(property.getKey(), property.getValue());
        editor.apply();
    }

    public void unlockIcon(ConfigProperty<Boolean> property, boolean value) {
        SharedPreferences.Editor editor = PREFS.edit();
        editor.putBoolean(property.getKey(), value);
        editor.apply();
    }

    public void updateStringSetting(ConfigProperty<String> property, String value) {
        property.setValue(value);

        SharedPreferences.Editor editor = PREFS.edit();
        editor.putString(property.getKey(), property.getValue());
        editor.apply();
    }

    public void updateIntegerSetting(ConfigProperty<Integer> property, int value) {
        property.setValue(value);

        SharedPreferences.Editor editor = PREFS.edit();
        editor.putInt(property.getKey(), property.getValue());
        editor.apply();
    }

    public void setPackLangVersion (String data) {
        updateStringSetting(OctoConfig.INSTANCE.languagePackVersioning, data);
    }

    public static class DcIdStyle {
        public static final int NONE = 0;
        public static final int OWLGRAM = 1;
        public static final int TELEGRAM = 2;
        public static final int MINIMAL = 3;
    }

    public static class DcIdType {
        public static final int BOT_API = 0;
        public static final int TELEGRAM = 1;
    }

    public static class DoubleTapAction {
        public static final int DISABLED = 0;
        public static final int REACTION = 1;
        public static final int COPY = 2;
        public static final int FORWARD = 3;
        public static final int REPLY = 4;
        public static final int DELETE = 5;
        public static final int SAVE = 6;
        public static final int EDIT = 7;
    }

    public static class EventType {
        public static final int NONE = 5;
        public static final int DEFAULT = 0;
        public static final int HOLIDAY = 1;
        public static final int VALENTINE = 2;
        public static final int HALLOWEEN = 3;
    }

    public static class AudioType {
        public static final int MONO = 0;
        public static final int STEREO = 1;
    }

    public static class CameraXResolution {
        public static final int SD = 0;
        public static final int HD = 1;
        public static final int FHD = 2;
        public static final int UHD = 3;
    }

    public static class PhotoResolution {
        public static final int LOW = 0;
        public static final int DEFAULT = 1;
        public static final int HIGH = 2;
    }

    public static class TabMode {
        public static final int TEXT = 0;
        public static final int MIXED = 1;
        public static final int ICON = 2;

    }

    public static class DownloadBoost {
        public static final int NORMAL = 0;
        public static final int FAST = 1;
        public static final int EXTREME = 2;
    }
}
