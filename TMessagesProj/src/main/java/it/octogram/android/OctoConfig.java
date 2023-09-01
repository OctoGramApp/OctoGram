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

    private final SharedPreferences PREFS = ApplicationLoader.applicationContext.getSharedPreferences("octoconfig", Activity.MODE_PRIVATE);

    /*General*/
    public final ConfigProperty<Boolean> hidePhoneNumber = new ConfigProperty<>("hidePhoneNumber", true);
    public final ConfigProperty<Boolean> showFakePhoneNumber = new ConfigProperty<>("showFakePhoneNumber", false);
    public final ConfigProperty<Boolean> showUsernameAsPhoneNumber = new ConfigProperty<>("showUsernameAsPhoneNumber", false);
    public final ConfigProperty<Boolean> hideOtherPhoneNumber = new ConfigProperty<>("hideOtherPhoneNumber", true);
    public final ConfigProperty<Boolean> promptBeforeCalling = new ConfigProperty<>("promptBeforeCalling", true);
    public final ConfigProperty<Integer> dcIdStyle = new ConfigProperty<>("dcIdStyle", DcIdStyle.MINIMAL);
    public final ConfigProperty<Integer> dcIdType = new ConfigProperty<>("dcIdType", DcIdType.BOT_API);
    public final ConfigProperty<Boolean> registrationDateInProfiles = new ConfigProperty<>("registrationDateInProfiles", false);
    public final ConfigProperty<Boolean> jumpToNextChannel = new ConfigProperty<>("jumpToNextChannel", true);
    public final ConfigProperty<Boolean> hideGreetingSticker = new ConfigProperty<>("hideGreetingSticker", false);
    public final ConfigProperty<Boolean> playGifAsVideo = new ConfigProperty<>("playGifAsVideo", false);
    public final ConfigProperty<Boolean> hideKeyboardOnScroll = new ConfigProperty<>("hideKeyboardOnScroll", false);
    public final ConfigProperty<Boolean> hideSendAsChannel = new ConfigProperty<>("hideSendAsChannel", false);
    public final ConfigProperty<Boolean> showOnlineStatus = new ConfigProperty<>("showOnlineStatus", false);
    public final ConfigProperty<Boolean> hideCustomEmojis = new ConfigProperty<>("hideCustomEmojis", false);
    public final ConfigProperty<Boolean> activeNoiseSuppression = new ConfigProperty<>("activeNoiseSuppression", false);
    public final ConfigProperty<Boolean> unmuteVideosWithVolumeDown = new ConfigProperty<>("unmuteVideosWithVolumeDown", true);
    public final ConfigProperty<Boolean> disableProximityEvents = new ConfigProperty<>("disableProximityEvents", false);
    public final ConfigProperty<Boolean> startWithRearCamera = new ConfigProperty<>("startWithRearCamera", false);
    public final ConfigProperty<Boolean> disableCameraPreview = new ConfigProperty<>("disableCameraPreview", false);
    public final ConfigProperty<Boolean> hideSentTimeOnStickers = new ConfigProperty<>("hideSentTimeOnStickers", false);
    public final ConfigProperty<Boolean> hideChatFolders = new ConfigProperty<>("hideChatFolders", false);
    public final ConfigProperty<Boolean> hideStories = new ConfigProperty<>("hideStories", false);
    public final ConfigProperty<Integer> doubleTapAction = new ConfigProperty<>("doubleTapAction", DoubleTapAction.REACTION);
    public final ConfigProperty<Integer> doubleTapActionOut = new ConfigProperty<>("doubleTapActionOut", DoubleTapAction.REACTION);

    public final ConfigProperty<Boolean> hideFoldersWhenForwarding = new ConfigProperty<>("showFoldersWhenForwarding", false);
    public final ConfigProperty<Boolean> accentColorAsNotificationColor = new ConfigProperty<>("accentColorAsNotificationColor", false);
    public final ConfigProperty<Boolean> openArchiveOnPull = new ConfigProperty<>("openArchiveOnPull", false);
    public final ConfigProperty<Boolean> tabletMode = new ConfigProperty<>("tabletMode", false);


    /*Appearance*/
    public final ConfigProperty<Boolean> showNameInActionBar = new ConfigProperty<>("showNameInActionBar", false);
    public final ConfigProperty<Boolean> forceChatBlurEffect = new ConfigProperty<>("forceChatBlurEffect", false);
    public final ConfigProperty<Integer> blurEffectStrength = new ConfigProperty<>("blurEffectStrength", 155);
    public final ConfigProperty<Boolean> forcePacmanAnimation = new ConfigProperty<>("forcePacmanAnimation", false);
    public final ConfigProperty<Boolean> formatTimeWithSeconds = new ConfigProperty<>("formatTimeWithSeconds", false);
    public final ConfigProperty<Boolean> numberRounding = new ConfigProperty<>("numberRounding", false);
    public final ConfigProperty<Boolean> pencilIconForEditedMessages = new ConfigProperty<>("pencilIconForEditedMessages", false);
    public final ConfigProperty<Boolean> searchIconInHeader = new ConfigProperty<>("searchIconInHeader", false);
    public final ConfigProperty<Boolean> slidingTitle = new ConfigProperty<>("slidingTitle", false);
    public final ConfigProperty<Integer> eventType = new ConfigProperty<>("eventType", EventType.NONE);
    public final ConfigProperty<Integer> maxStickerSize = new ConfigProperty<>("maxStickerSize", 14);
    public final ConfigProperty<Boolean> useSystemFont = new ConfigProperty<>("useSystemFont", false);
    public final ConfigProperty<Boolean> useSystemEmoji = new ConfigProperty<>("useSystemEmoji", false);
    public final ConfigProperty<String> selectedEmojiPack = new ConfigProperty<>("selectedEmojiPack", "default");
    public final ConfigProperty<Boolean> showSnowflakes = new ConfigProperty<>("showSnowflakes", false);
    public final ConfigProperty<Boolean> disableDividers = new ConfigProperty<>("disableDividers", false);

    /*Folders*/
    public final ConfigProperty<Integer> tabMode = new ConfigProperty<>("tabMode", TabMode.MIXED);

    /*Drawer elements*/
    public final ConfigProperty<Boolean> changeStatus = new ConfigProperty<>("drawer_changeStatus", true);
    public final ConfigProperty<Boolean> myStories = new ConfigProperty<>("drawer_myStories", true);
    public final ConfigProperty<Boolean> newGroup = new ConfigProperty<>("drawer_newGroup", true);
    public final ConfigProperty<Boolean> newChannel = new ConfigProperty<>("drawer_newChannel", false);
    public final ConfigProperty<Boolean> contacts = new ConfigProperty<>("drawer_contacts", true);
    public final ConfigProperty<Boolean> calls = new ConfigProperty<>("drawer_calls", true);
    public final ConfigProperty<Boolean> peopleNearby = new ConfigProperty<>("drawer_peopleNearby", true);
    public final ConfigProperty<Boolean> savedMessages = new ConfigProperty<>("drawer_savedMessages", true);
    public final ConfigProperty<Boolean> settings = new ConfigProperty<>("drawer_settings", true);
    public final ConfigProperty<Boolean> octogramSettings = new ConfigProperty<>("drawer_octogramSettings", false);
    public final ConfigProperty<Boolean> datacenterInfo = new ConfigProperty<>("drawer_datacenterInfo", true);
    public final ConfigProperty<Boolean> inviteFriends = new ConfigProperty<>("drawer_inviteFriends", true);
    public final ConfigProperty<Boolean> telegramFeatures = new ConfigProperty<>("drawer_telegramFeatures", true);

    /*Unlock Secret Icons*/
    public final ConfigProperty<Boolean> unlockedYuki = new ConfigProperty<>("unlockedYuki", false);
    public final ConfigProperty<Boolean> unlockedChupa = new ConfigProperty<>("unlockedChupa", false);

    /*CameraX*/
    public final ConfigProperty<Boolean> cameraXEnabled = new ConfigProperty<>("cameraXEnabled", true);
    public final ConfigProperty<Boolean> cameraXPerfOverQuality = new ConfigProperty<>("cameraXPerformanceMode", false);
    public final ConfigProperty<Boolean> cameraXZeroShutter = new ConfigProperty<>("cameraXZeroShutter", false);
    public final ConfigProperty<Integer> cameraXResolution = new ConfigProperty<>("cameraXResolution", -1);

    /*Experiments*/
    public final ConfigProperty<Boolean> experimentsEnabled = new ConfigProperty<>("experimentsEnabled", false);
    public final ConfigProperty<Boolean> alternativeNavigation = new ConfigProperty<>("alternativeNavigation", false);
    public final ConfigProperty<Boolean> uploadBoost = new ConfigProperty<>("uploadBoost", false);
    public final ConfigProperty<Boolean> downloadBoost = new ConfigProperty<>("downloadBoost", false);
    public final ConfigProperty<Integer> downloadBoostValue = new ConfigProperty<>("downloadBoostValue", 0);
    public final ConfigProperty<Integer> photoResolution = new ConfigProperty<>("photoResolution", PhotoResolution.DEFAULT);
    public final ConfigProperty<Integer> lastSelectedCompression = new ConfigProperty<>("lastSelectedCompression", 3);
    public final ConfigProperty<Integer> gcOutputType = new ConfigProperty<>("gcOutputType", AudioType.MONO);
    public final ConfigProperty<Boolean> mediaInGroupCall = new ConfigProperty<>("mediaInGroupCall", false);
    public final ConfigProperty<Integer> maxRecentStickers = new ConfigProperty<>("maxRecentStickers", 0);

    private final List<ConfigProperty<?>> properties = List.of(
            hidePhoneNumber, showFakePhoneNumber, hideOtherPhoneNumber, promptBeforeCalling, dcIdStyle, dcIdType, registrationDateInProfiles,
            jumpToNextChannel, hideGreetingSticker, playGifAsVideo, hideKeyboardOnScroll, hideSendAsChannel, showOnlineStatus,
            hideCustomEmojis, activeNoiseSuppression, unmuteVideosWithVolumeDown, disableProximityEvents, startWithRearCamera,
            disableCameraPreview, hideSentTimeOnStickers, hideChatFolders, hideStories, doubleTapAction, hideFoldersWhenForwarding, accentColorAsNotificationColor,
            openArchiveOnPull, showNameInActionBar, forceChatBlurEffect, blurEffectStrength, forcePacmanAnimation, formatTimeWithSeconds,
            numberRounding, pencilIconForEditedMessages, searchIconInHeader, slidingTitle, eventType, useSystemFont, useSystemEmoji, selectedEmojiPack, showSnowflakes,
            disableDividers, changeStatus, myStories, newGroup, newChannel, contacts, calls, peopleNearby, savedMessages, settings,
            octogramSettings, datacenterInfo, inviteFriends, telegramFeatures,
            cameraXEnabled, cameraXPerfOverQuality, cameraXZeroShutter, cameraXResolution, unlockedYuki, unlockedChupa,
            experimentsEnabled, alternativeNavigation, uploadBoost, downloadBoost, downloadBoostValue, photoResolution, lastSelectedCompression,
            tabletMode, maxStickerSize, gcOutputType, mediaInGroupCall, maxRecentStickers, tabMode
    );


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

    public static class DcIdStyle {
        public static final int NONE = 0;
        public static final int MINIMAL = 1;
        public static final int OWLGRAM = 2;
        public static final int TELEGRAM = 3;
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
}
