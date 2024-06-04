/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */
package it.octogram.android;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.MessageObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import it.octogram.android.utils.OctoUtils;


/*
 * Super big TODO list:
 * - Option to delete account
 * - Option to delete all personal messages in a chat
 * - two-way translation. And automatically turn over the text sent by the user
 * - custom double tap action
 * - Zalgo filters?
 * - ???
 */
@SuppressWarnings("unchecked")
public class OctoConfig {
    public static final OctoConfig INSTANCE = new OctoConfig();
    private static final String TAG = "OctoConfig";
    private final List<ConfigProperty<?>> properties = new ArrayList<>();
    private final SharedPreferences PREFS = ApplicationLoader.applicationContext.getSharedPreferences("octoconfig", Activity.MODE_PRIVATE);

    /*General*/
    public final ConfigProperty<Boolean> hidePhoneNumber = newConfigProperty("hidePhoneNumber", true);
    public final ConfigProperty<Boolean> showFakePhoneNumber = newConfigProperty("showFakePhoneNumber", false);
    public final ConfigProperty<Boolean> showUsernameAsPhoneNumber = newConfigProperty("showUsernameAsPhoneNumber", false);
    public final ConfigProperty<Boolean> hideOtherPhoneNumber = newConfigProperty("hideOtherPhoneNumber", true);
    public final ConfigProperty<Boolean> promptBeforeCalling = newConfigProperty("promptBeforeCalling", true);
    public final ConfigProperty<Integer> dcIdStyle = newConfigProperty("dcIdStyle", DcIdStyle.TELEGRAM.getValue());
    public final ConfigProperty<Integer> dcIdType = newConfigProperty("dcIdType", DcIdType.BOT_API.getValue());
    public final ConfigProperty<Boolean> registrationDateInProfiles = newConfigProperty("registrationDateInProfiles", false);
    public final ConfigProperty<Boolean> jumpToNextChannelOrTopic = newConfigProperty("jumpToNextChannel", true);
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
    public final ConfigProperty<Integer> doubleTapAction = newConfigProperty("doubleTapAction", DoubleTapAction.REACTION.getValue());
    public final ConfigProperty<Integer> doubleTapActionOut = newConfigProperty("doubleTapActionOut", DoubleTapAction.REACTION.getValue());
    public final ConfigProperty<Boolean> hideFoldersWhenForwarding = newConfigProperty("showFoldersWhenForwarding", false);
    public final ConfigProperty<Boolean> accentColorAsNotificationColor = newConfigProperty("accentColorAsNotificationColor", false);
    public final ConfigProperty<Boolean> openArchiveOnPull = newConfigProperty("openArchiveOnPull", false);
    public final ConfigProperty<Boolean> tabletMode = newConfigProperty("tabletMode", false);
    public final ConfigProperty<Boolean> forceUseIpV6 = newConfigProperty("forceUseIpV6", false);
    public final ConfigProperty<Boolean> warningBeforeDeletingChatHistory = newConfigProperty("warningBeforeDeletingChatHistory", true);
    public final ConfigProperty<Boolean> enableSmartNotificationsForPrivateChats = newConfigProperty("enableSmartNotificationsForPrivateChats", false);

    /*Appearance*/
    public final ConfigProperty<Boolean> showNameInActionBar = newConfigProperty("showNameInActionBar", false);
    public final ConfigProperty<Boolean> showUserIconsInChatsList = newConfigProperty("showUserIconsInChatsList", true);
    public final ConfigProperty<Boolean> forceChatBlurEffect = newConfigProperty("forceChatBlurEffect", false);
    public final ConfigProperty<Integer> blurEffectStrength = newConfigProperty("blurEffectStrength", 155);
    public final ConfigProperty<Boolean> forcePacmanAnimation = newConfigProperty("forcePacmanAnimation", false);
    public final ConfigProperty<Boolean> formatTimeWithSeconds = newConfigProperty("formatTimeWithSeconds", false);
    public final ConfigProperty<Boolean> numberRounding = newConfigProperty("numberRounding", false);
    public final ConfigProperty<Boolean> pencilIconForEditedMessages = newConfigProperty("pencilIconForEditedMessages", false);
    public final ConfigProperty<Boolean> searchIconInHeader = newConfigProperty("searchIconInHeader", false);
    public final ConfigProperty<Boolean> slidingTitle = newConfigProperty("slidingTitle", false);
    public final ConfigProperty<Integer> eventType = newConfigProperty("eventType", EventType.NONE.getValue());
    public final ConfigProperty<Integer> maxStickerSize = newConfigProperty("maxStickerSize", 14);
    public final ConfigProperty<Boolean> useSystemFont = newConfigProperty("useSystemFont", false);
    public final ConfigProperty<Boolean> useSystemEmoji = newConfigProperty("useSystemEmoji", false);
    public final ConfigProperty<String> selectedEmojiPack = newConfigProperty("selectedEmojiPack", "default");
    public final ConfigProperty<Boolean> showSnowflakes = newConfigProperty("showSnowflakes", false);
    public final ConfigProperty<Boolean> disableDividers = newConfigProperty("disableDividers", false);
    public final ConfigProperty<Integer> stickerShape = newConfigProperty("stickerShape", Shape.DEFAULT.getValue());

    /*Folders*/
    public final ConfigProperty<Integer> tabMode = newConfigProperty("tabMode", TabMode.MIXED.getValue());

    /*Drawer elements*/
    public final ConfigProperty<Boolean> drawerChangeStatus = newConfigProperty("drawer_changeStatus", true);
    // public final ConfigProperty<Boolean> drawerMyStories = newConfigProperty("drawer_myStories", true);
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
    public final ConfigProperty<Integer> downloadBoostValue = newConfigProperty("downloadBoostValue", DownloadBoost.NORMAL.getValue());
    public final ConfigProperty<Integer> photoResolution = newConfigProperty("photoResolution", PhotoResolution.DEFAULT.getValue());
    public final ConfigProperty<Integer> lastSelectedCompression = newConfigProperty("lastSelectedCompression", 3);
    public final ConfigProperty<Integer> gcOutputType = newConfigProperty("gcOutputType", AudioType.MONO.getValue());
    public final ConfigProperty<Boolean> mediaInGroupCall = newConfigProperty("mediaInGroupCall", false);
    public final ConfigProperty<Integer> maxRecentStickers = newConfigProperty("maxRecentStickers", 0);
    public final ConfigProperty<Boolean> showRPCErrors = newConfigProperty("showRPCErrors", false);
    public final ConfigProperty<Boolean> useTranslationsArgsFix = newConfigProperty("useTranslationsArgsFix", true);

    /*Updates*/
    public final ConfigProperty<Boolean> autoCheckUpdates = newConfigProperty("autoCheckUpdateStatus", true);
    public final ConfigProperty<Boolean> preferBetaVersion = newConfigProperty("preferBetaVersion", false);
    public final ConfigProperty<Boolean> receivePBetaUpdates = newConfigProperty("receivePBetaUpdates", false);

    /*Translator*/
    public final ConfigProperty<Integer> translatorMode = newConfigProperty("translatorMode", TranslatorMode.DEFAULT.getValue());
    public final ConfigProperty<Integer> translatorProvider = newConfigProperty("translatorProvider", TranslatorProvider.DEFAULT.getValue());
    public final ConfigProperty<Integer> translatorFormality = newConfigProperty("translatorFormality", TranslatorFormality.DEFAULT.getValue());
    public final ConfigProperty<Boolean> translatorKeepMarkdown = newConfigProperty("translatorKeepMarkdown", true);

    /*Lite Mode: sync power saver with device settings*/
    public final ConfigProperty<Boolean> syncPowerSaver = newConfigProperty("syncPowerSaver", false);

    /*Media filtering*/
    public final ConfigProperty<Integer> mediaFiltering = newConfigProperty("mediaFilteringId", 0);

    /*Multi-Language*/
    public final ConfigProperty<String> languagePackVersioning = newConfigProperty("languagePackVersioning", "{}");

    /*Migrate Logs*/
    public final ConfigProperty<Boolean> isMigrateOldLogs = newConfigProperty("isMigrateOldLogs", false);

    /* Drawer Reorder */
    public final ConfigProperty<String> drawerItems = newConfigProperty("drawerItems", "[]");

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
        if (OctoConfig.INSTANCE.gcOutputType.getValue() == AudioType.MONO.getValue()) {
            return AudioFormat.CHANNEL_OUT_MONO;
        } else {
            return AudioFormat.CHANNEL_OUT_STEREO;
        }
    }

    /**
     * It is safe to suppress this warning because the method loadConfig() is only called once in the static block above.
     * Also the semantics of the data structure is pretty solid, so there is no need to worry about it.
     */
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

    public void resetConfig() {
        synchronized (this) {
            SharedPreferences.Editor editor = PREFS.edit();
            for (ConfigProperty<?> property : properties) {
                editor.remove(property.getKey());
            }
            editor.apply();
        }
    }

    public static boolean isValidMessageExport(MessageObject message) {
        File downloadedFile = OctoUtils.getFileContentFromMessage(message);

        if (downloadedFile != null && downloadedFile.length() <= 1024 * 30) { // 30 kB limit
            try {
                FileInputStream downloadedFileStream = new FileInputStream(downloadedFile);

                StringBuilder jsonStringBuilder = new StringBuilder();
                int character;
                while ((character = downloadedFileStream.read()) != -1) {
                    jsonStringBuilder.append((char) character);
                }

                downloadedFileStream.close();

                JSONObject result = new JSONObject(new JSONTokener(jsonStringBuilder.toString()));
                if (INSTANCE.isJSONArrayValidData(result)) {
                    return true;
                }
            } catch (IOException e) {
                Log.e(TAG, "an io exception occurred internally during isValidMessageExport OctoConfig", e);
            } catch (JSONException e) {
                Log.e(TAG, "a json exception occurred internally during isValidMessageExport OctoConfig", e);
            }
        }

        return false;
    }

    private boolean isJSONArrayValidData(JSONObject result) {
        for (Iterator<String> it = result.keys(); it.hasNext(); ) {
            try {
                String key = it.next();
                Object value = result.get(key);

                if (!(value instanceof String || value instanceof Integer || value instanceof Boolean)) {
                    return false;
                }
            } catch (JSONException e) {
                Log.e(TAG, "failed to handle isJSONArrayValidData OctoConfig", e);
            }
        }

        return true;
    }

    public int importMessageExport(MessageObject message, ArrayList<String> dataToImport, ArrayList<String> excludedOptionsByConfig) {
        File downloadedFile = OctoUtils.getFileContentFromMessage(message);
        int changed = 0;

        if (downloadedFile != null && downloadedFile.length() <= 1024 * 30) { // 30 kB limit
            try {
                FileInputStream downloadedFileStream = new FileInputStream(downloadedFile);

                StringBuilder jsonStringBuilder = new StringBuilder();
                int character;
                while ((character = downloadedFileStream.read()) != -1) {
                    jsonStringBuilder.append((char) character);
                }

                downloadedFileStream.close();

                JSONObject result = new JSONObject(new JSONTokener(jsonStringBuilder.toString()));
                if (INSTANCE.isJSONArrayValidData(result)) {
                    for (Field field : this.getClass().getDeclaredFields()) {
                        if (field.getType().equals(ConfigProperty.class)) {
                            try {
                                ConfigProperty<?> configProperty = (ConfigProperty<?>) field.get(OctoConfig.INSTANCE);
                                String fieldName = field.getName();
                                Object fieldValue = null;
                                if (configProperty != null) {
                                    fieldValue = configProperty.getValue();
                                }

                                if (!result.has(fieldName) || !dataToImport.contains(fieldName) || excludedOptionsByConfig.contains(fieldName)) {
                                    continue;
                                }

                                assert fieldValue != null;
                                if (result.get(fieldName).getClass().equals(fieldValue.getClass())) { // same type
                                    changed++;

                                    if (fieldValue == result.get(fieldName)) {
                                        continue;
                                    }

                                    Object exportFileSettingsValue = result.get(fieldName);
                                    if (exportFileSettingsValue instanceof Boolean) {
                                        configProperty.updateValue((ConfigProperty<Boolean>) configProperty, (Boolean) exportFileSettingsValue);
                                    } else if (exportFileSettingsValue instanceof Integer) {
                                        if (!isIntegerValueValid(fieldName, (Integer) exportFileSettingsValue)) {
                                            continue;
                                        }

                                        configProperty.updateValue((ConfigProperty<Integer>) configProperty, (Integer) exportFileSettingsValue);
                                    } else //noinspection StatementWithEmptyBody
                                        if (exportFileSettingsValue instanceof String) {
                                        // конфигурация в настоящее время не содержит строковых значений.
                                        //configProperty.updateSetting((ConfigProperty<String>) configProperty, (String) exportFileSettingsValue);
                                    }
                                }
                            } catch (JSONException e) {
                                android.util.Log.e(TAG, "Error validating put-settings export", e);
                            } catch (IllegalAccessException e) {
                                android.util.Log.e(TAG, "Error getting settings export", e);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "an io exception occurred internally during isValidMessageExport octoconfig", e);
            } catch (JSONException e) {
                Log.e(TAG, "a json exception occurred internally during isValidMessageExport octoconfig", e);
            }
        }

        return changed;
    }

    private boolean isIntegerValueValid(String fieldName, int value) {
        return switch (fieldName) {
            case "blurEffectStrength" -> value >= 0 && value <= 255;
            case "cameraXResolution" ->
                    value >= CameraXResolution.SD.getValue() && value <= CameraXResolution.UHD.getValue();
            case "dcIdStyle" -> value >= DcIdStyle.NONE.getValue() && value <= DcIdStyle.MINIMAL.getValue();
            case "dcIdType" -> value == DcIdType.BOT_API.getValue() || value == DcIdType.TELEGRAM.getValue();
            case "doubleTapAction", "doubleTapActionOut" ->
                    value >= DoubleTapAction.DISABLED.getValue() && value <= DoubleTapAction.EDIT.getValue();
            case "downloadBoostValue" ->
                    value >= DownloadBoost.NORMAL.getValue() && value <= DownloadBoost.EXTREME.getValue();
            case "eventType" -> value >= EventType.DEFAULT.getValue() && value <= EventType.NONE.getValue();
            case "gcOutputType" -> value == AudioType.MONO.getValue() || value == AudioType.STEREO.getValue();
            case "maxRecentStickers" -> value >= 0 && value <= 9;
            case "maxStickerSize" -> value >= 2 && value <= 20;
            case "photoResolution" -> value >= PhotoResolution.LOW.getValue() && value <= PhotoResolution.HIGH.getValue();
            case "tabMode" -> value >= TabMode.TEXT.getValue() && value <= TabMode.ICON.getValue();
            case "translatorMode" -> value >= TranslatorMode.DEFAULT.getValue() && value <= TranslatorMode.EXTERNAL.getValue();
            case "translatorProvider" -> value >= TranslatorProvider.DEFAULT.getValue() && value <= TranslatorProvider.YANDEX.getValue();
            default ->
                // в любом другом случае считайте значение недействительным.
                    false;
        };
    }
}
