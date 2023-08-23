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
    public final ConfigProperty<Boolean> showOnlineStatus = new ConfigProperty<>("showOnlineStatus", true);
    public final ConfigProperty<Boolean> hideCustomEmojis = new ConfigProperty<>("hideCustomEmojis", false);
    public final ConfigProperty<Boolean> activeNoiseSuppression = new ConfigProperty<>("activeNoiseSuppression", false);
    public final ConfigProperty<Boolean> unmuteVideosWithVolumeDown = new ConfigProperty<>("unmuteVideosWithVolumeDown", true);
    public final ConfigProperty<Boolean> disableProximityEvents = new ConfigProperty<>("disableProximityEvents", false);
    public final ConfigProperty<Boolean> startWithRearCamera = new ConfigProperty<>("startWithRearCamera", false);
    public final ConfigProperty<Boolean> disableCameraPreview = new ConfigProperty<>("disableCameraPreview", false);
    public final ConfigProperty<Boolean> hideSentTimeOnStickers = new ConfigProperty<>("hideSentTimeOnStickers", false);
    public final ConfigProperty<Boolean> hideChatFolders = new ConfigProperty<>("hideChatFolders", false);
    public final ConfigProperty<Boolean> hideStories = new ConfigProperty<>("hideStories", false);

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
    public final ConfigProperty<Integer> gcOutputType = new ConfigProperty<>("gcOutputType", 0);
    public final ConfigProperty<Boolean> mediaInGroupCall = new ConfigProperty<>("mediaInGroupCall", false);

    private final List<ConfigProperty<?>> properties = List.of(
            hidePhoneNumber, showFakePhoneNumber, hideOtherPhoneNumber, promptBeforeCalling, dcIdStyle, dcIdType, registrationDateInProfiles,
            jumpToNextChannel, hideGreetingSticker, playGifAsVideo, hideKeyboardOnScroll, hideSendAsChannel, showOnlineStatus,
            hideCustomEmojis, activeNoiseSuppression, unmuteVideosWithVolumeDown, disableProximityEvents, startWithRearCamera,
            disableCameraPreview, hideSentTimeOnStickers, hideChatFolders, hideStories, hideFoldersWhenForwarding, accentColorAsNotificationColor,
            openArchiveOnPull, showNameInActionBar, forceChatBlurEffect, blurEffectStrength, forcePacmanAnimation, formatTimeWithSeconds,
            numberRounding, pencilIconForEditedMessages, searchIconInHeader, slidingTitle, eventType, useSystemFont,
            cameraXEnabled, cameraXPerfOverQuality, cameraXZeroShutter, cameraXResolution, unlockedYuki, unlockedChupa,
            experimentsEnabled, alternativeNavigation, uploadBoost, downloadBoost, downloadBoostValue, photoResolution,lastSelectedCompression,
            tabletMode, maxStickerSize, gcOutputType, mediaInGroupCall
    );


    private OctoConfig() {
        loadConfig();
    }

    public static int getOutputType(int output) {
        if (output == 1) {
            return AudioFormat.CHANNEL_OUT_STEREO;
        }
        return AudioFormat.CHANNEL_OUT_MONO;
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
        property.setValue(!property.getValue());

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

    public static class EventType {
        public static final int NONE = 5;
        public static final int DEFAULT = 0;
        public static final int HOLIDAY = 1;
        public static final int VALENTINE = 2;
        public static final int HALLOWEEN = 3;
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

}
