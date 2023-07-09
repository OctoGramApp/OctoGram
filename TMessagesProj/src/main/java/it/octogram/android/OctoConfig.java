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

import org.telegram.messenger.ApplicationLoader;

import java.util.List;

import it.octogram.android.config.ConfigProperty;


/*
 * Super big TODO list:
 *
 * - Adjust chat blur effect
 * - Option to delete account
 * - Option to delete all personal messages in a chat
 * - two-way translation. And automatically turn over the text sent by the user
 * - custom double tap action
 * - zalgo filters?
 * -
*/

public class OctoConfig {

    private static final SharedPreferences PREFS = ApplicationLoader.applicationContext.getSharedPreferences("octoconfig", Activity.MODE_PRIVATE);
    private static final Object sync = new Object();

    /*General*/
    public static final ConfigProperty<Boolean> hidePhoneNumber = new ConfigProperty<>("hidePhoneNumber", true);
    public static final ConfigProperty<Boolean> hideOtherPhoneNumber = new ConfigProperty<>("hideOtherPhoneNumber", true);
    public static final ConfigProperty<Boolean> promptBeforeCalling = new ConfigProperty<>("promptBeforeCalling", true);
    public static final ConfigProperty<String> dcIdStyle = new ConfigProperty<>("dcIdStyle", "OctoGram");
    public static final ConfigProperty<Boolean> registrationDateInProfiles = new ConfigProperty<>("registrationDateInProfiles", false);
    public static final ConfigProperty<Boolean> jumpToNextChannel = new ConfigProperty<>("jumpToNextChannel", true);
    public static final ConfigProperty<Boolean> hideGreetingSticker = new ConfigProperty<>("hideGreetingSticker", false);
    public static final ConfigProperty<Boolean> playGifAsVideo = new ConfigProperty<>("playGifAsVideo", false);
    public static final ConfigProperty<Boolean> hideKeyboardOnScroll = new ConfigProperty<>("hideKeyboardOnScroll", false);
    public static final ConfigProperty<Boolean> hideSendAsChannel = new ConfigProperty<>("hideSendAsChannel", false);
    public static final ConfigProperty<Boolean> showOnlineStatus = new ConfigProperty<>("showOnlineStatus", true);
    public static final ConfigProperty<Boolean> hideCustomEmojis = new ConfigProperty<>("hideCustomEmojis", false);
    public static final ConfigProperty<Boolean> activeNoiseSuppression = new ConfigProperty<>("activeNoiseSuppression", false);
    public static final ConfigProperty<Boolean> unmuteVideosWithVolumeDown = new ConfigProperty<>("unmuteVideosWithVolumeDown", true);
    public static final ConfigProperty<Boolean> disableProximityEvents = new ConfigProperty<>("disableProximityEvents", false);
    public static final ConfigProperty<Boolean> startWithRearCamera = new ConfigProperty<>("startWithRearCamera", false);
    public static final ConfigProperty<Boolean> disableCameraPreview = new ConfigProperty<>("disableCameraPreview", false);
    public static final ConfigProperty<Boolean> hideSentTimeOnStickers = new ConfigProperty<>("hideSentTimeOnStickers", false);
    public static final ConfigProperty<Boolean> hideChatFolders = new ConfigProperty<>("hideChatFolders", false);
    public static final ConfigProperty<Boolean> hideFoldersWhenForwarding = new ConfigProperty<>("showFoldersWhenForwarding", false);
    public static final ConfigProperty<Boolean> accentColorAsNotificationColor = new ConfigProperty<>("accentColorAsNotificationColor", false);

    private static final List<ConfigProperty<?>> properties = List.of(
            hidePhoneNumber, hideOtherPhoneNumber, promptBeforeCalling, dcIdStyle, registrationDateInProfiles, jumpToNextChannel,
            hideGreetingSticker, playGifAsVideo, hideKeyboardOnScroll, hideSendAsChannel, showOnlineStatus, hideCustomEmojis,
            activeNoiseSuppression, unmuteVideosWithVolumeDown, disableProximityEvents, startWithRearCamera, disableCameraPreview,
            hideSentTimeOnStickers, hideChatFolders, hideFoldersWhenForwarding, accentColorAsNotificationColor
    );

    static {
        loadConfig();
    }

    public static void loadConfig() {
        synchronized (sync) {
            for (ConfigProperty<?> property : properties) {
                if (property.getValue() instanceof Boolean) {
                    ConfigProperty<Boolean> booleanProperty = (ConfigProperty<Boolean>) property;
                    booleanProperty.setValue(PREFS.getBoolean(booleanProperty.getKey(), booleanProperty.getValue()));
                } else if (property.getValue() instanceof String) {
                    ConfigProperty<String> booleanProperty = (ConfigProperty<String>) property;
                    booleanProperty.setValue(PREFS.getString(booleanProperty.getKey(), booleanProperty.getValue()));
                }
            }
        }
    }

    public static void toggleBooleanSetting(ConfigProperty<Boolean> property) {
        property.setValue(!property.getValue());

        SharedPreferences.Editor editor = PREFS.edit();
        editor.putBoolean(property.getKey(), property.getValue());
        editor.apply();
    }

    public static void updateStringSetting(ConfigProperty<String> property, String value) {
        property.setValue(value);

        SharedPreferences.Editor editor = PREFS.edit();
        editor.putString(property.getKey(), property.getValue());
        editor.apply();
    }

}
