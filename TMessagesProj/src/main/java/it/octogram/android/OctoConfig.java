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

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.MessageObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import it.octogram.android.camerax.CameraXUtils;
import it.octogram.android.drawer.MenuOrderController;
import it.octogram.android.utils.OctoUtils;

@SuppressWarnings("unchecked")
public class OctoConfig {
    public static final OctoConfig INSTANCE = new OctoConfig();
    public static final String TAG = "OctoConfig";
    public static final int ROUND_MESSAGE_BITRATE = 512;
    private final List<ConfigProperty<?>> properties = new ArrayList<>();
    private final SharedPreferences PREFS = ApplicationLoader.applicationContext.getSharedPreferences("octoconfig", Activity.MODE_PRIVATE);
    public static final String STICKERS_PLACEHOLDER_PACK_NAME = "octo_placeholders_android";
    public static final String CRASH_MIME_TYPE = "message/rfc822";
    public static final String EXPORT_BACKUP_MIME_TYPE = "text/json";

    /*General*/
    public final ConfigProperty<Boolean> hidePhoneNumber = newConfigProperty("hidePhoneNumber", true);
    public final ConfigProperty<Boolean> hideOtherPhoneNumber = newConfigProperty("hideOtherPhoneNumber", true);
    public final ConfigProperty<Integer> phoneNumberAlternative = newConfigProperty("phoneNumberAlternative", PhoneNumberAlternative.SHOW_HIDDEN_NUMBER_STRING.getValue());
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
    public final ConfigProperty<Boolean> hideSentTimeOnStickers = newConfigProperty("hideSentTimeOnStickers", false);
    public final ConfigProperty<Boolean> hideOnlyAllChatsFolder = newConfigProperty("hideOnlyAllChatsFolder", false);
    public final ConfigProperty<Boolean> hideChatFolders = newConfigProperty("hideChatFolders", false);
    public final ConfigProperty<Boolean> hideStories = newConfigProperty("hideStories", false);
    public final ConfigProperty<Integer> doubleTapAction = newConfigProperty("doubleTapAction", DoubleTapAction.REACTION.getValue());
    public final ConfigProperty<Integer> doubleTapActionOut = newConfigProperty("doubleTapActionOut", DoubleTapAction.REACTION.getValue());
    public final ConfigProperty<Boolean> hideFoldersWhenForwarding = newConfigProperty("showFoldersWhenForwarding", false);
    public final ConfigProperty<Boolean> accentColorAsNotificationColor = newConfigProperty("accentColorAsNotificationColor", false);
    public final ConfigProperty<Boolean> openArchiveOnPull = newConfigProperty("openArchiveOnPull", false);
    public final ConfigProperty<Integer> deviceIdentifyState = newConfigProperty("deviceIdentifyState", DeviceIdentifyState.DEFAULT.getValue());
    public final ConfigProperty<Boolean> forceUseIpV6 = newConfigProperty("forceUseIpV6", false);
    public final ConfigProperty<Boolean> warningBeforeDeletingChatHistory = newConfigProperty("warningBeforeDeletingChatHistory", true);
    public final ConfigProperty<Boolean> enableSmartNotificationsForPrivateChats = newConfigProperty("enableSmartNotificationsForPrivateChats", false);
    public final ConfigProperty<Integer> defaultEmojiButtonAction = newConfigProperty("defaultEmojiButtonAction", DefaultEmojiButtonAction.DEFAULT.getValue());

    /*Appearance*/
    public final ConfigProperty<Integer> actionBarTitleOption = newConfigProperty("actionBarTitleOption", ActionBarTitleOption.APP_NAME.getValue());
    public final ConfigProperty<String> actionBarCustomTitle = newConfigProperty("actionBarCustomTitle", "Home");
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
    public final ConfigProperty<Integer> stickerShape = newConfigProperty("stickerShape", StickerShape.DEFAULT.getValue());
    public final ConfigProperty<Integer> drawerBackground = newConfigProperty("drawerBackground", DrawerBackgroundState.WALLPAPER.getValue());
    public final ConfigProperty<Boolean> drawerGradientBackground = newConfigProperty("drawerGradientBackground", true);
    public final ConfigProperty<Boolean> drawerShowProfilePic = newConfigProperty("drawerShowProfilePic", true);
    public final ConfigProperty<Boolean> drawerBlurBackground = newConfigProperty("drawerBlurBackground", false);
    public final ConfigProperty<Integer> drawerBlurBackgroundLevel = newConfigProperty("drawerBlurBackgroundLevel", 100);
    public final ConfigProperty<Boolean> drawerDarkenBackground = newConfigProperty("drawerDarkenBackground", false);
    public final ConfigProperty<Integer> drawerDarkenBackgroundLevel = newConfigProperty("drawerDarkenBackgroundLevel", 100);
    public final ConfigProperty<Integer> drawerFavoriteOption = newConfigProperty("drawerFavoriteOption", DrawerFavoriteOption.DEFAULT.getValue());
    public final ConfigProperty<Boolean> repliesLinksShowColors = newConfigProperty("repliesLinksShowColors", true);
    public final ConfigProperty<Boolean> repliesLinksShowEmojis = newConfigProperty("repliesLinksShowEmojis", true);
    public final ConfigProperty<Boolean> promptBeforeSendingStickers = newConfigProperty("promptBeforeSendingStickers", false);
    public final ConfigProperty<Boolean> promptBeforeSendingGIFs = newConfigProperty("promptBeforeSendingGIFs", false);
    public final ConfigProperty<Boolean> promptBeforeSendingVoiceMessages = newConfigProperty("promptBeforeSendingVoiceMessages", false);
    public final ConfigProperty<Boolean> promptBeforeSendingVideoMessages = newConfigProperty("promptBeforeSendingVideoMessages", false);

    /*Folders*/
    public final ConfigProperty<Integer> tabMode = newConfigProperty("tabMode", TabMode.MIXED.getValue());

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
    public final ConfigProperty<Boolean> unlockedConfetti = newConfigProperty("unlockedConfetti", false);
    public final ConfigProperty<Boolean> unlockedFoxIcon = newConfigProperty("unlockedFoxIcon", false);

    /*CameraX*/
    public final ConfigProperty<Boolean> cameraXPerformanceMode = newConfigProperty("cameraXPerformanceMode", false);
    public final ConfigProperty<Boolean> cameraXZeroShutter = newConfigProperty("cameraXZeroShutter", false);
    public final ConfigProperty<Integer> cameraXResolution = newConfigProperty("cameraXResolution", CameraXUtils.getCameraResolution());
    public ConfigProperty<Integer> cameraType = newConfigProperty("cameraType", CameraType.CAMERA_X.getValue());
    public final ConfigProperty<Boolean> disableCameraPreview = newConfigProperty("disableCameraPreview", false);

    /*Experiments*/
    public final ConfigProperty<Boolean> experimentsEnabled = newConfigProperty("experimentsEnabled", false);
    public final ConfigProperty<Boolean> alternativeNavigation = newConfigProperty("alternativeNavigation", false);
    public final ConfigProperty<Integer> navigationSmoothness = newConfigProperty("navigationSmoothness", 1000);
    public final ConfigProperty<Boolean> animatedActionBar = newConfigProperty("animatedActionBar", false);
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
    public final ConfigProperty<Integer> uiTitleCenteredState = newConfigProperty("uiTitleCenteredState", ActionBarCenteredTitle.NEVER.getValue());
    public final ConfigProperty<Boolean> uiImmersivePopups = newConfigProperty("uiImmersivePopups", false);
    public final ConfigProperty<Integer> interfaceSwitchUI = newConfigProperty("interfaceSwitchUI", InterfaceSwitchUI.DEFAULT.getValue());
    public final ConfigProperty<Integer> interfaceCheckboxUI = newConfigProperty("interfaceCheckboxUI", InterfaceCheckboxUI.DEFAULT.getValue());
    public final ConfigProperty<Integer> uiIconsType = newConfigProperty("uiIconsType", IconsUIType.DEFAULT.getValue());
    public final ConfigProperty<Boolean> uiRandomMemeIcons = newConfigProperty("uiRandomMemeIcons", false);

    /*Updates*/
    public final ConfigProperty<Boolean> autoCheckUpdateStatus = newConfigProperty("autoCheckUpdateStatus", true);
    public final ConfigProperty<Boolean> preferBetaVersion = newConfigProperty("preferBetaVersion", false);
    public final ConfigProperty<Boolean> receivePBetaUpdates = newConfigProperty("receivePBetaUpdates", false);
    public final ConfigProperty<Integer> autoDownloadUpdatesStatus = newConfigProperty("autoDownloadUpdatesStatus", AutoDownloadUpdate.NEVER.getValue());

    /* Updates: Signaling */
    public final ConfigProperty<Integer> updateSignalingLastBuildID = newConfigProperty("updateSignalingLastBuildID", 0);
    public final ConfigProperty<String> updateSignalingChangelog = newConfigProperty("updateSignalingChangelog", null);

    /*Translator*/
    public final ConfigProperty<Integer> translatorMode = newConfigProperty("translatorMode", TranslatorMode.DEFAULT.getValue());
    public final ConfigProperty<Integer> translatorProvider = newConfigProperty("translatorProvider", TranslatorProvider.DEFAULT.getValue());
    public final ConfigProperty<Integer> translatorFormality = newConfigProperty("translatorFormality", TranslatorFormality.DEFAULT.getValue());
    public final ConfigProperty<Boolean> translatorKeepMarkdown = newConfigProperty("translatorKeepMarkdown", true);
    public final ConfigProperty<String> lastTranslatePreSendLanguage = newConfigProperty("lastTranslatePreSendLanguage", null);

    /*Lite Mode: sync power saver with device settings*/
    public final ConfigProperty<Boolean> syncPowerSaver = newConfigProperty("syncPowerSaver", false);

    /*Media filtering*/
    public final ConfigProperty<Integer> mediaFiltering = newConfigProperty("mediaFiltering", 0);

    /*Multi-Language*/
    public final ConfigProperty<String> languagePackVersioning = newConfigProperty("languagePackVersioning", "{}");

    /*Migrate Logs*/
    public final ConfigProperty<Boolean> isMigrateOldLogs = newConfigProperty("isMigrateOldLogs", false);

    /* Drawer Reorder */
    public final ConfigProperty<String> drawerItems = newConfigProperty("drawerItems", "[]");

    /* Settings: NEW badge */
    public final ConfigProperty<String> newBadgeIds = newConfigProperty("newBadgeIds", "[]");

    /**
     * Creates a new {@link ConfigProperty} with the specified key and default value.
     * <p>
     * The newly created property is added to the internal list of properties.
     *
     * @param <T>          The type of the config property value.
     * @param key          The key of the config property.
     * @param defaultValue The default value of the config property.
     * @return The newly created {@link ConfigProperty} instance.
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
        int[] sizes = {20, 30, 40, 50, 80, 100, 120, 150, 180, 200, 10000};
        Integer value = OctoConfig.INSTANCE.maxRecentStickers.getValue();
        if (value >= 0 && value < sizes.length) {
            return sizes[value];
        }
        return 20;
    }

    public static int getAudioType() {
        return OctoConfig.INSTANCE.gcOutputType.getValue() == AudioType.MONO.getValue()
                ? AudioFormat.CHANNEL_OUT_MONO
                : AudioFormat.CHANNEL_OUT_STEREO;
    }

    /**
     * Loads the configuration values from the SharedPreferences and applies them to the corresponding properties.
     * This method handles loading Boolean, String, and Integer properties. It also executes any necessary data migrations.
     * <p>
     * This method is synchronized to ensure thread safety, as it accesses and modifies shared resources.
     * <p>
     * <b>Note:</b> This method is intended to be called only once during initialization and relies on the assumption that the data structure is well-defined.
     */
    private void loadConfig() {
        synchronized (this) {
            for (ConfigProperty<?> property : properties) {
                if (property.getValue() instanceof Boolean) {
                    ConfigProperty<Boolean> booleanProperty = (ConfigProperty<Boolean>) property;
                    booleanProperty.setValue(PREFS.getBoolean(booleanProperty.getKey(), booleanProperty.getValue()));
                } else if (property.getValue() instanceof String || property.getValue() == null) {
                    ConfigProperty<String> stringProperty = (ConfigProperty<String>) property;

                    if (executeStringMigration(stringProperty)) {
                        continue;
                    }

                    stringProperty.setValue(PREFS.getString(stringProperty.getKey(), stringProperty.getValue()));
                } else if (property.getValue() instanceof Integer) {
                    ConfigProperty<Integer> integerProperty = (ConfigProperty<Integer>) property;

                    if (executeIntegerMigration(integerProperty)) {
                        continue;
                    }

                    integerProperty.setValue(PREFS.getInt(integerProperty.getKey(), integerProperty.getValue()));
                }
            }
        }
    }

    /**
     * Executes migration for integer-type config properties from deprecated boolean preferences.
     * This method checks if a deprecated preference exists for the given property and, if so,
     * updates the property's value based on the deprecated preference's value.
     * The deprecated preference is then removed from the shared preferences.
     *
     * @param property The config property to migrate.
     * @return True if a migration was performed, false otherwise.
     */
    private boolean executeIntegerMigration(ConfigProperty<Integer> property) {
        if (property.getKey() != null) {
            if (property.getKey().equals(actionBarTitleOption.getKey())) {
                if (PREFS.contains("showNameInActionBar")) {
                    boolean deprecatedOldValue = PREFS.getBoolean("showNameInActionBar", false);
                    PREFS.edit().remove("showNameInActionBar").apply();

                    if (deprecatedOldValue) {
                        property.updateValue(ActionBarTitleOption.ACCOUNT_NAME.getValue());
                        return true;
                    }
                }
            } else if (property.getKey().equals(phoneNumberAlternative.getKey())) {
                if (PREFS.contains("showFakePhoneNumber") || PREFS.contains("showUsernameAsPhoneNumber")) {
                    boolean showFakePhoneNumber = PREFS.getBoolean("showFakePhoneNumber", false);
                    boolean showUsernameAsPhoneNumber = PREFS.getBoolean("showUsernameAsPhoneNumber", false);

                    PREFS.edit().remove("showFakePhoneNumber").remove("showUsernameAsPhoneNumber").apply();

                    if (showUsernameAsPhoneNumber) {
                        property.updateValue(PhoneNumberAlternative.SHOW_USERNAME.getValue());
                        return true;
                    } else if (showFakePhoneNumber) {
                        property.updateValue(PhoneNumberAlternative.SHOW_FAKE_PHONE_NUMBER.getValue());
                        return true;
                    }
                }
            } else if (property.getKey().equals(deviceIdentifyState.getKey())) {
                if (PREFS.contains("tabletMode")) {
                    boolean deprecatedOldValue = PREFS.getBoolean("tabletMode", false);
                    PREFS.edit().remove("tabletMode").apply();

                    if (deprecatedOldValue) {
                        property.updateValue(DeviceIdentifyState.FORCE_TABLET.getValue());
                        return true;
                    }
                }
            } else if (property.getKey().equals(autoDownloadUpdatesStatus.getKey())) {
                if (PREFS.contains("autoDownloadUpdates")) {
                    boolean deprecatedOldValue = PREFS.getBoolean("autoDownloadUpdates", false);
                    PREFS.edit().remove("autoDownloadUpdates").apply();

                    if (deprecatedOldValue) {
                        property.updateValue(AutoDownloadUpdate.ALWAYS.getValue());
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean executeStringMigration(ConfigProperty<String> property) {
        if (property.getKey() != null && property.getKey().equals(drawerItems.getKey())) {
            SharedPreferences.Editor editor = PREFS.edit();
            JSONArray newDrawerItems = new JSONArray();
            boolean hasAppliedEdits = false;

            Map<String, String> associations = getStringMap();

            for (Map.Entry<String, String> e : associations.entrySet()) {
                if (PREFS.contains(e.getKey())) {
                    editor.remove(e.getKey());
                    hasAppliedEdits = true;

                    if (PREFS.getBoolean(e.getKey(), false)) {
                        if (e.getKey().equals("drawerInviteFriends")) {
                            newDrawerItems.put(MenuOrderController.DIVIDER_ITEM);
                        }

                        newDrawerItems.put(e.getValue());

                        if (e.getKey().equals("drawerChangeStatus")) {
                            newDrawerItems.put(MenuOrderController.DIVIDER_ITEM);
                        }
                    }
                }
            }

            if (hasAppliedEdits) {
                editor.apply();
            }

            if (newDrawerItems.length() > 0) {
                OctoConfig.INSTANCE.drawerItems.updateValue(newDrawerItems.toString());
                return true;
            }
        }

        return false;
    }

    private static @NonNull Map<String, String> getStringMap() {
        Map<String, String> associations = new HashMap<>();
        associations.put("drawerChangeStatus", "set_status");
        associations.put("drawerNewGroup", "new_group");
        associations.put("drawerNewChannel", "new_channel");
        associations.put("drawerContacts", "contacts");
        associations.put("drawerCalls", "calls");
        associations.put("drawerPeopleNearby", "nearby_people");
        associations.put("drawerSavedMessages", "saved_message");
        associations.put("drawerOctogramSettings", "octogram_settings");
        associations.put("drawerDatacenterInfo", "datacenter_status");
        associations.put("drawerInviteFriends", "invite_friends");
        associations.put("drawerTelegramFeatures", "telegram_features");
        return associations;
    }

    /**
     * Resets the configuration to its default values.
     * <p>
     * This method iterates through all registered configuration properties
     * and removes their corresponding keys from the shared preferences.
     * This effectively reverts the configuration to its initial state,
     * as if the application was freshly installed.
     * <p>
     * The method is synchronized to ensure thread safety when accessing
     * and modifying the shared preferences.
     */
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
                                if (configProperty == null) {
                                    continue;
                                }

                                String fieldName = configProperty.getKey();
                                Object fieldValue = configProperty.getValue();

                                if (fieldName == null || fieldValue == null || !result.has(fieldName) || !dataToImport.contains(fieldName) || excludedOptionsByConfig.contains(fieldName)) {
                                    continue;
                                }

                                if (result.get(fieldName).getClass().equals(fieldValue.getClass())) {
                                    changed++;

                                    if (fieldValue == result.get(fieldName) && !BuildConfig.DEBUG_PRIVATE_VERSION) {
                                        // в целях отладки, например, чтобы проверить правильность условий для целого числа/строки,
                                        // мы принудительно импортируем, даже если текущее установленное значение идентично значению экспорта
                                        continue;
                                    }

                                    Object exportFileSettingsValue = result.get(fieldName);
                                    if (exportFileSettingsValue instanceof Boolean) {
                                        configProperty.updateValue((ConfigProperty<Boolean>) configProperty, (Boolean) exportFileSettingsValue);
                                    } else if (exportFileSettingsValue instanceof Integer) {
                                        if (!isValueValid(fieldName, (Integer) exportFileSettingsValue)) {
                                            Log.d(TAG, "failed to import "+fieldName+" as integer value is invalid");
                                            continue;
                                        }

                                        configProperty.updateValue((ConfigProperty<Integer>) configProperty, (Integer) exportFileSettingsValue);
                                    } else if (exportFileSettingsValue instanceof String) {
                                        if (!isValueValid(fieldName, (String) exportFileSettingsValue)) {
                                            Log.d(TAG, "failed to import "+fieldName+" as string value is invalid");
                                            continue;
                                        }

                                        configProperty.updateValue((ConfigProperty<String>) configProperty, reparseStringValue(fieldName, (String) exportFileSettingsValue));
                                    }
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "Error validating put-settings export", e);
                            } catch (IllegalAccessException e) {
                                Log.e(TAG, "Error getting settings export", e);
                            }
                        }
                    }

                    if (!OctoConfig.INSTANCE.experimentsEnabled.getValue()) {
                        OctoConfig.INSTANCE.experimentsEnabled.updateValue(true);
                        // force enable experiments after import
                    }

                    MenuOrderController.reloadConfig();
                }
            } catch (IOException e) {
                Log.e(TAG, "an io exception occurred internally during isValidMessageExport octoconfig", e);
            } catch (JSONException e) {
                Log.e(TAG, "a json exception occurred internally during isValidMessageExport octoconfig", e);
            }
        }

        return changed;
    }

    private boolean isValueValid(String fieldName, int value) {
        return switch (fieldName) {
            case "blurEffectStrength" -> value >= 0 && value <= 255;
            case "cameraXResolution" -> value >= CameraXResolution.INSTANCE.enumToValue(CameraXResolution.SD) && value <= CameraXResolution.INSTANCE.enumToValue(CameraXResolution.UHD);
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
            case "translatorFormality" -> value >= TranslatorFormality.DEFAULT.getValue() && value <= TranslatorFormality.HIGH.getValue();
            case "defaultEmojiButtonAction" -> value >= DefaultEmojiButtonAction.DEFAULT.getValue() && value <= DefaultEmojiButtonAction.STICKERS.getValue();
            case "stickerShape" -> value >= StickerShape.DEFAULT.getValue() && value <= StickerShape.MESSAGE.getValue();
            case "drawerBackground" -> value >= DrawerBackgroundState.WALLPAPER.getValue() && value <= DrawerBackgroundState.COLOR.getValue();
            case "drawerFavoriteOption" -> value >= DrawerFavoriteOption.NONE.getValue() && value <= DrawerFavoriteOption.ARCHIVED_CHATS.getValue();
            case "drawerBlurBackgroundLevel" -> value >= 1 && value <= 100;
            case "drawerDarkenBackgroundLevel" -> value >= 1 && value <= 255;
            case "actionBarTitleOption" -> value >= ActionBarTitleOption.EMPTY.getValue() && value <= ActionBarTitleOption.CUSTOM.getValue();
            case "deviceIdentifyState" -> value >= DeviceIdentifyState.DEFAULT.getValue() && value <= DeviceIdentifyState.FORCE_SMARTPHONE.getValue();
            case "phoneNavigationAlternative" -> value >= PhoneNumberAlternative.SHOW_HIDDEN_NUMBER_STRING.getValue() && value <= PhoneNumberAlternative.SHOW_USERNAME.getValue();
            case "navigationSmoothness" -> value == 200 || value == 400 || value == 500 || value == 600 || value == 800 || value == 1000;
            case "autoDownloadUpdatesStatus" -> value >= AutoDownloadUpdate.ALWAYS.getValue() && value <= AutoDownloadUpdate.NEVER.getValue();
            default -> false;
        };
    }


    private boolean isValueValid(String fieldName, String value) {
        return switch (fieldName) {
            case "drawerItems" -> MenuOrderController.isMenuItemsImportValid(value);
            case "actionBarCustomTitle" -> value.length() <= 40;
            default -> false;
        };
    }

    private String reparseStringValue(String fieldName, String value) {
        if (fieldName.equals("drawerItems")) {
            return MenuOrderController.reparseMenuItemsAsString(value);
        }

        return value;
    }

    public void setDrawerItems(String drawerItemsList) {
        drawerItems.updateValue(drawerItemsList);
    }

    public boolean isNewIdVisible(String newID) {
        String value = newBadgeIds.getValue();
        JSONArray data;

        try {
            data = new JSONArray(value);
        } catch (Exception ignored) {
            newBadgeIds.setValue("[]");
            data = new JSONArray();
        }

        for (int i = 0; i < data.length(); i++) {
            try {
                if (data.getString(i).equals(newID)) {
                    return false;
                }
            } catch (Exception ignored) {

            }
        }

        return true;
    }

    public void hideNewId(String newID) {
        String value = newBadgeIds.getValue();
        JSONArray data;

        try {
            data = new JSONArray(value);
        } catch (Exception ignored) {
            newBadgeIds.setValue("[]");
            data = new JSONArray();
        }

        data.put(newID);
        newBadgeIds.updateValue(data.toString());
    }

    public CameraType getCameraType() {
        return switch (cameraType.getValue()) {
            case 1 -> CameraType.CAMERA_X;
            case 2 -> CameraType.CAMERA_2;
            case 3 -> CameraType.SYSTEM_CAMERA;
            default -> CameraType.TELEGRAM;
        };
    }
}
