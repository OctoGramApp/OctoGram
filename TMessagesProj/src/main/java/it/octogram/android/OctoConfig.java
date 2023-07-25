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
    
    public static OctoConfig INSTANCE = new OctoConfig();

    private final SharedPreferences PREFS = ApplicationLoader.applicationContext.getSharedPreferences("octoconfig", Activity.MODE_PRIVATE);

    /*General*/
    public final ConfigProperty<Boolean> hidePhoneNumber = new ConfigProperty<>("hidePhoneNumber", true);
    public final ConfigProperty<Boolean> hideOtherPhoneNumber = new ConfigProperty<>("hideOtherPhoneNumber", true);
    public final ConfigProperty<Boolean> promptBeforeCalling = new ConfigProperty<>("promptBeforeCalling", true);
    public final ConfigProperty<String> dcIdStyle = new ConfigProperty<>("dcIdStyle", "OctoGram");
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
    public final ConfigProperty<Boolean> hideFoldersWhenForwarding = new ConfigProperty<>("showFoldersWhenForwarding", false);
    public final ConfigProperty<Boolean> accentColorAsNotificationColor = new ConfigProperty<>("accentColorAsNotificationColor", false);

    private final List<ConfigProperty<?>> properties = List.of(
            hidePhoneNumber, hideOtherPhoneNumber, promptBeforeCalling, dcIdStyle, registrationDateInProfiles, jumpToNextChannel,
            hideGreetingSticker, playGifAsVideo, hideKeyboardOnScroll, hideSendAsChannel, showOnlineStatus, hideCustomEmojis,
            activeNoiseSuppression, unmuteVideosWithVolumeDown, disableProximityEvents, startWithRearCamera, disableCameraPreview,
            hideSentTimeOnStickers, hideChatFolders, hideFoldersWhenForwarding, accentColorAsNotificationColor, test
    );

    
    private OctoConfig() {
        loadConfig();
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
                    ConfigProperty<String> booleanProperty = (ConfigProperty<String>) property;
                    booleanProperty.setValue(PREFS.getString(booleanProperty.getKey(), booleanProperty.getValue()));
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

    public void updateStringSetting(ConfigProperty<String> property, String value) {
        property.setValue(value);

        SharedPreferences.Editor editor = PREFS.edit();
        editor.putString(property.getKey(), property.getValue());
        editor.apply();
    }

}