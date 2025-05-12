/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android

import android.graphics.Color
import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import androidx.annotation.StringDef
import androidx.core.graphics.toColorInt
import it.octogram.android.preferences.fragment.OctoAnimationFragment
import org.telegram.messenger.LocaleController.getString
import org.telegram.messenger.R

enum class EmojiStatus(val value: Int) {
    CAN_BE_ADDED(0),
    CAN_BE_REMOVED(1),
    UNAVAILABLE(2)
}

enum class DcIdStyle(val value: Int) {
    NONE(0),
    OWLGRAM(1),
    TELEGRAM(2),
    MINIMAL(3);

    companion object {
        fun fromInt(value: Int): DcIdStyle {
            return DcIdStyle.entries.find { it.value == value } ?: NONE
        }
    }
}

enum class DcIdType(val value: Int) {
    BOT_API(0),
    TELEGRAM(1)
}

enum class DefaultEmojiButtonAction(val value: Int) {
    DEFAULT(0),
    EMOJIS(1),
    STICKERS(2),
    GIFS(3)
}

enum class DoubleTapAction(val value: Int) {
    DISABLED(0),
    REACTION(1),
    COPY(2),
    FORWARD(3),
    REPLY(4),
    DELETE(5),
    SAVE(6),
    EDIT(7),
    TRANSLATE(8);

    companion object {
        fun fromInt(value: Int): DoubleTapAction {
            return DoubleTapAction.entries.find { it.value == value } ?: DISABLED
        }
    }
}

enum class WarningsBehavior(val value: Int) {
    SHOW_PROMPT(0),
    REQUEST_BIOMETRIC_AUTH(1)
}

enum class PhoneNumberAlternative(val value: Int) {
    SHOW_HIDDEN_NUMBER_STRING(0),
    SHOW_FAKE_PHONE_NUMBER(1),
    SHOW_USERNAME(2);

    companion object {
        fun fromInt(value: Int): PhoneNumberAlternative {
            return PhoneNumberAlternative.entries.find { it.value == value } ?: SHOW_HIDDEN_NUMBER_STRING
        }
    }
}

enum class DeviceIdentifyState(val value: Int) {
    DEFAULT(0),
    FORCE_TABLET(1),
    FORCE_SMARTPHONE(2)
}

enum class EventType(val value: Int) {
    NONE(5),
    DEFAULT(0),
    HOLIDAY(1),
    VALENTINE(2),
    HALLOWEEN(3),
    LUNAR_NEW_YEAR(4);

    companion object {
        fun fromInt(value: Int): EventType {
            return EventType.entries.find { it.value == value } ?: DEFAULT
        }
    }
}

enum class AudioType(val value: Int) {
    MONO(0),
    STEREO(1)
}

object CameraXResolution {
    @IntDef(SD, HD, FHD, UHD, NONE)
    @Retention(AnnotationRetention.SOURCE)
    annotation class CameraXResolution

    const val SD = 0
    const val HD = 1
    const val FHD = 2
    const val UHD = 3
    const val NONE = 4
    const val MAX = 5

    private val resolutionMap = mapOf(
        SD to 480,
        HD to 720,
        FHD to 1080,
        UHD to 2160,
        NONE to -1,
        MAX to 4096
    )

    fun getCameraXResolution(@CameraXResolution resolution: Int): Int {
        return if (resolutionMap.containsValue(resolution)) {
            resolutionMap.entries.find { it.value == resolution }?.value ?: NONE
        } else {
            NONE
        }
    }

    fun enumToValue(enum: Int): Int {
        when (enum) {
            SD -> return 480
            HD -> return 720
            FHD -> return 1080
            UHD -> return 2160
            MAX -> return 4096
            NONE -> return -1
        }
        return -1
    }
}

enum class AiProvidersDetails(
    val id: Int,
    val title: String,
    val keyMinLength: Int,
    val keyMaxLength: Int,
    val animationScope: Int,
    val statusProperty: ConfigProperty<Boolean>,
    val keyProperty: ConfigProperty<String>,
    val needWarningZone: Boolean
) {
    GEMINI(
        0,
        "Gemini",
        20,
        45,
        OctoAnimationFragment.OctoAnimationScopes.GEMINI,
        OctoConfig.INSTANCE.aiFeaturesUseGoogleAPIs,
        OctoConfig.INSTANCE.aiFeaturesUseGoogleAPIKey,
        false
    ),
    CHATGPT(
        1,
        "ChatGPT",
        100,
        180,
        OctoAnimationFragment.OctoAnimationScopes.CHATGPT,
        OctoConfig.INSTANCE.aiFeaturesUseChatGPTAPIs,
        OctoConfig.INSTANCE.aiFeaturesUseChatGPTAPIKey,
        true
    ),
    OPENROUTER(
        2,
        "OpenRouter",
        45,
        100,
        OctoAnimationFragment.OctoAnimationScopes.CHATGPT,
        OctoConfig.INSTANCE.aiFeaturesUseOpenRouterAPIs,
        OctoConfig.INSTANCE.aiFeaturesOpenRouterAPIKey,
        true
    );

    companion object {
        fun fromMainProperty(property: ConfigProperty<Boolean>) : AiProvidersDetails? {
            return AiProvidersDetails.entries.find { it.statusProperty == property }
        }
    }
}

enum class PhotoResolution(val value: Int) {
    LOW(0),
    DEFAULT(1),
    HIGH(2);

    companion object {
        fun fromInt(value: Int): PhotoResolution {
            return PhotoResolution.entries.find { it.value == value } ?: DEFAULT
        }
    }
}

enum class TranslatorMode(val value: Int) {
    DEFAULT(0),
    INLINE(1),
    EXTERNAL(2)
}

enum class TranslatorProvider(val value: Int) {
    DEFAULT(0),
    GOOGLE(1),
    YANDEX(2),
    DEEPL(3),
    BAIDU(4),
    LINGO(5),
    EMOJIS(6)
}

enum class TranslatorFormality(val value: Int) {
    DEFAULT(0),
    LOW(1),
    HIGH(2)
}

enum class TabMode(val value: Int) {
    TEXT(0),
    MIXED(1),
    ICON(2)
}

enum class TabStyle(val value: Int) {
    DEFAULT(0),
    ROUNDED(1),
    FLOATING(2),
    TEXT_ONLY(3),
    CHIPS(4),
    PILLS(5),
    FULL(6)
}

enum class DownloadBoost(val value: Int) {
    NORMAL(0),
    FAST(1),
    EXTREME(2)
}

enum class StickerShape(val value: Int) {
    DEFAULT(0),
    ROUND(1),
    MESSAGE(2)
}

enum class MediaFilter(val value: Int) {
    ALL(0),
    PHOTOS(1),
    VIDEOS(2),
    VOICE_MESSAGES(3),
    VIDEO_MESSAGES(4),
    FILES(5),
    MUSIC(6),
    GIFS(7),
    LOCATIONS(8),
    CONTACTS(9),
    MENTIONS(10),
    URL(11),
    PINNED_MESSAGES(12),
    CHAT_PHOTOS(13);
    companion object {
        @JvmStatic
        fun fromValue(value: Int): MediaFilter {
            return MediaFilter.entries.find { it.value == value } ?: ALL
        }
    }
}

enum class CameraType(val value: Int) {
    TELEGRAM(0),
    CAMERA_X(1),
    CAMERA_2(2),
    SYSTEM_CAMERA(3);

    companion object {
        fun fromInt(value: Int): CameraType {
            return CameraType.entries.find { it.value == value } ?: TELEGRAM
        }
    }
}

enum class QualityPreset(val value: Int) {
    AUTO(0),
    HIGHEST(1),
    LOWEST(2),
    DYNAMIC(3)
}

enum class IconsUIType(val value: Int) {
    DEFAULT(0),
    SOLAR(1),
    MATERIAL_DESIGN_3(2)
}
@StringDef(
    DrawerItem.MY_PROFILE_ID, DrawerItem.NEW_GROUP_ID, DrawerItem.CONTACTS_ID, DrawerItem.CALLS_ID,
    DrawerItem.SAVED_MESSAGE_ID, DrawerItem.SETTINGS_ID, DrawerItem.OCTOGRAM_SETTINGS_ID,
    DrawerItem.NEW_CHANNEL_ID, DrawerItem.NEW_SECRET_CHAT_ID, DrawerItem.INVITE_FRIENDS_ID,
    DrawerItem.TELEGRAM_FEATURES_ID, DrawerItem.ARCHIVED_MESSAGES_ID, DrawerItem.DATACENTER_STATUS_ID,
    DrawerItem.QR_LOGIN_ID, DrawerItem.SET_STATUS_ID, DrawerItem.CONNECTED_DEVICES_ID,
    DrawerItem.POWER_USAGE_ID, DrawerItem.PROXY_SETTINGS_ID, DrawerItem.DIVIDER_ID,
    DrawerItem.ATTACH_MENU_BOT_ID, DrawerItem.DOWNLOADS_ID, DrawerItem.TELEGRAM_BROWSER_ID,
    DrawerItem.DATA_AND_STORAGE_ID
)
@Retention(AnnotationRetention.SOURCE)
annotation class DrawerItemDef

object DrawerItem {
    const val MY_PROFILE_ID = "my_profile"
    const val NEW_GROUP_ID = "new_group"
    const val CONTACTS_ID = "contacts"
    const val CALLS_ID = "calls"
    const val SAVED_MESSAGE_ID = "saved_message"
    const val SETTINGS_ID = "settings"
    const val OCTOGRAM_SETTINGS_ID = "octogram_settings"
    const val NEW_CHANNEL_ID = "new_channel"
    const val NEW_SECRET_CHAT_ID = "new_secret_chat"
    const val INVITE_FRIENDS_ID = "invite_friends"
    const val TELEGRAM_FEATURES_ID = "telegram_features"
    const val ARCHIVED_MESSAGES_ID = "archived_messages"
    const val DATACENTER_STATUS_ID = "datacenter_status"
    const val QR_LOGIN_ID = "qr_login"
    const val SET_STATUS_ID = "set_status"
    const val CONNECTED_DEVICES_ID = "connected_devices"
    const val POWER_USAGE_ID = "power_usage"
    const val PROXY_SETTINGS_ID = "proxy_settings"
    const val DIVIDER_ID = "divider"
    const val ATTACH_MENU_BOT_ID = "attach_menu_bot"
    const val DOWNLOADS_ID = "downloads"
    const val TELEGRAM_BROWSER_ID = "tg_browser"
    const val DATA_AND_STORAGE_ID = "data_and_storage"
    const val AI_FEATURES_ID = "ai_features"

    enum class Id(@DrawerItemDef val id: String, val itemId: Int) {
        MY_PROFILE(MY_PROFILE_ID, 16),
        NEW_GROUP(NEW_GROUP_ID, 2),
        CONTACTS(CONTACTS_ID, 6),
        CALLS(CALLS_ID, 10),
        SAVED_MESSAGE(SAVED_MESSAGE_ID, 11),
        SETTINGS(SETTINGS_ID, 8),
        OCTOGRAM_SETTINGS(OCTOGRAM_SETTINGS_ID, 100),
        NEW_CHANNEL(NEW_CHANNEL_ID, 4),
        NEW_SECRET_CHAT(NEW_SECRET_CHAT_ID, 3),
        INVITE_FRIENDS(INVITE_FRIENDS_ID, 7),
        TELEGRAM_FEATURES(TELEGRAM_FEATURES_ID, 13),
        ARCHIVED_MESSAGES(ARCHIVED_MESSAGES_ID, 202),
        DATACENTER_STATUS(DATACENTER_STATUS_ID, 101),
        QR_LOGIN(QR_LOGIN_ID, 204),
        SET_STATUS(SET_STATUS_ID, 15),
        CONNECTED_DEVICES(CONNECTED_DEVICES_ID, 913),
        POWER_USAGE(POWER_USAGE_ID, 206),
        PROXY_SETTINGS(PROXY_SETTINGS_ID, 207),
        DIVIDER(DIVIDER_ID, 0),
        ATTACH_MENU_BOT(ATTACH_MENU_BOT_ID, 205),
        DOWNLOADS(DOWNLOADS_ID, 912),
        TELEGRAM_BROWSER(TELEGRAM_BROWSER_ID, 914),
        DATA_AND_STORAGE(DATA_AND_STORAGE_ID, 915),
        AI_FEATURES(AI_FEATURES_ID, 916);

        companion object INSTANCE {
            fun getById(id: String): Id? {
                return Id.entries.find { it.id == id }
            }
        }
    }
}

enum class NewFeaturesBadgeId(val id: String) {
    AI_FEATURES_ID("AFI"),
    PRIVACY_MAIN("PVL"),
    PRIVACY_LOCKED_CHATS("PVLS"),
    APPEARANCE("APLS"),
    APPEARANCE_INTERFACE("APLSD")
}

enum class StickerUi(val value: Int) {
    TRANSLATOR(0),
    MAIN(1),
    GROUPS_AND_CHANNELS(2),
    APPEARANCE(3),
    SIZE(4),
    PRIVATE(5),
    GENERAL(6),
    DATACENTER_STATUS(7),
    EXPERIMENTAL(8),
    IMPORT_SETTINGS(9),
    CAMERA(10),
    UPDATES(11),
    CRASHED(12),
    DUCK_COUNTING(13),
    DUCK_DEV(14),
    CROSS(15),
    HOURGLASS(16),
    VALENTINE_HEART(17),
    HALLOWEEN(18),
    HOLIDAY(19),
    LUNAR_NEW_YEAR(20),
    DRAWER(21),
    HEADER_CUSTOM_TITLE(22),
    MONET_DIALOG(23),
    LINK_VERIFY(24),
    MAIN_SETTINGS(25),
    MEDIA_LOADING(26),
    WEB_SEARCH(27),
    OLD_PRIVACY(28),
    PRIVACY(29),
    CHATS_PRIVACY(30),
    TRANSLATOR_GEMINI(31),
    DUCK_OK(32),
    NEW_MODEL_GENERATION(33),
    OCTO_LOGS_PLACEHOLDER(34),
}

enum class DrawerBackgroundState(val value: Int) {
    TRANSPARENT(0),
    WALLPAPER(1),
    PROFILE_PIC(2),
    COLOR(3),
    PREMIUM_DETAILS(4)
}

enum class DrawerFavoriteOption(val value: Int) {
    NONE(0),
    DEFAULT(1),
    SAVED_MESSAGES(2),
    SETTINGS(3),
    CONTACTS(4),
    CALLS(5),
    DOWNLOADS(6),
    ARCHIVED_CHATS(7),
    TELEGRAM_BROWSER(8)
}

enum class ActionBarCenteredTitle(val value: Int) {
    ALWAYS(0),
    JUST_IN_CHATS(1),
    JUST_IN_SETTINGS(2),
    NEVER(3)
}

enum class ActionBarTitleOption(val value: Int) {
    EMPTY(0),
    APP_NAME(1),
    ACCOUNT_NAME(2),
    ACCOUNT_USERNAME(3),
    CUSTOM(4),
    FOLDER_NAME(5)
}

enum class AutoDownloadUpdate(val value: Int) {
    ALWAYS(0),
    ONLY_ON_WIFI(1),
    NEVER(2)
}

enum class ExpandableRowsIds(val id: Int) {
    REPLIES_AND_LINKS(1),
    PROMPT_BEFORE_SENDING(2),
    CONTEXT_MENU_ELEMENTS(3),
    ADMIN_SHORTCUTS(4),
    LOCKED_ELEMENTS(5),
    LOCKED_ACCOUNTS(6),
    AI_MODEL_OPTIONS_MESSAGES(7)
}

enum class AiModelType(val id: Int) {
    RELATED_TO_MESSAGES(0),
    RELATED_TO_INPUT(1),
    RELATED_TO_CHATS(2);

    companion object {
        fun hasState(value: Int): Boolean {
            return AiModelType.entries.any { it.id == value }
        }
        fun find(value: Int): AiModelType {
            return AiModelType.entries.find { it.id == value } ?: RELATED_TO_MESSAGES
        }
    }
}

enum class AiModelMessagesState(val id: String, val stateName: String) {
    TEXT_MESSAGES("msg", getString(R.string.AiFeatures_CustomModels_Create_OptionsHeader_MessagesTexts)),
    PHOTOS("pts", getString(R.string.SendMediaPermissionPhotos)),
    STICKERS("sts", getString(R.string.AccDescrStickers)),
    MUSIC("msc", getString(R.string.SendMediaPermissionMusic)),
    VOICE_MESSAGES("vms", getString(R.string.SendMediaPermissionVoice)),
    VIDEOS("vid", getString(R.string.SendMediaPermissionVideos)),
    GIFS("gif", getString(R.string.AccDescrGIFs));

    companion object {
        fun hasState(value: String): Boolean {
            return AiModelMessagesState.entries.any { it.id == value }
        }
        fun find(value: String): AiModelMessagesState {
            return AiModelMessagesState.entries.find { it.id == value } ?: TEXT_MESSAGES
        }
    }
}

enum class ShortcutsPosition(val id: Int) {
    THREE_DOTS(0),
    CHAT_INFO(1),
    PROFILE_DOTS(2)
}

enum class InterfaceSwitchUI(val value: Int) {
    DEFAULT(0),
    ONEUIOLD(1),
    ONEUINEW(2),
    GOOGLE(3),
    GOOGLE_NEW(4)
}

enum class InterfaceCheckboxUI(val value: Int) {
    DEFAULT(0),
    ROUNDED(1),
    TRANSPARENT_UNCHECKED(2),
    ALWAYS_TRANSPARENT(3)
}

enum class InterfaceSliderUI(val value: Int) {
    DEFAULT(0),
    MODERN(1),
    ANDROID(2)
}

enum class InterfaceRapidButtonsActions(val value: Int) {
    HIDDEN(0),
    POST_STORY(1),
    SEND_MESSAGE(2),
    SAVED_MESSAGES(3),
    ARCHIVED_CHATS(4),
    SETTINGS(5),
    LOCKED_CHATS(6),
    PROXY(7),
    SEARCH(8);

    companion object {
        fun getState(value: Int): InterfaceRapidButtonsActions {
            return InterfaceRapidButtonsActions.entries.find { it.value == value } ?: POST_STORY
        }
    }
}

enum class Datacenter(
    val dcId: Int,
    val dcName: String,
    val icon: Int,
    val color: Int,
    val ip: String
) {
    USA_1(
        1,
        "MIA, Miami FL, USA",
        R.drawable.ic_pluto_datacenter,
        "#329AFE".toColorInt(),
        "149.154.175.50"
    ),
    USA_2(
        3,
        "MIA, Miami FL, USA",
        R.drawable.ic_aurora_datacenter,
        "#DA5653".toColorInt(),
        "149.154.175.100"
    ),
    AMSTERDAM_1(
        2,
        "AMS, Amsterdam, NL",
        R.drawable.ic_venus_datacenter,
        "#8B31FD".toColorInt(),
        "149.154.167.50"
    ),
    AMSTERDAM_2(
        4,
        "AMS, Amsterdam, NL",
        R.drawable.ic_vesta_datacenter,
        "#F7B139".toColorInt(),
        "149.154.167.91"
    ),
    SINGAPORE(
        5,
        "SIN, Singapore, SG",
        R.drawable.ic_flora_datacenter,
        "#4BD199".toColorInt(),
        "91.108.56.100"
    ),
    UNKNOWN(
        -1,
        getString(R.string.NumberUnknown),
        R.drawable.msg_secret_hw,
        Color.TRANSPARENT,
        ""
    );

    companion object {
        fun getDcInfo(dcId: Int): Datacenter {
            return Datacenter.entries.find { it.dcId == dcId } ?: UNKNOWN
        }
    }
}

enum class WebPages(
    val id: Int,
    val pageName: String,
    @DrawableRes val icon: Int,
    val website: String,
    val category: Int
) {
    TELEGRAM(1, "Telegram",  R.drawable.telegram_camera_icon, "https://telegram.org", 1),
    FAQ(2, "FAQ", R.drawable.msg_emoji_question, "https://telegram.org/faq", 1),
    DOCUMENTATION(3, "Documentation", R.drawable.msg_bot, "https://core.telegram.org", 1),
    TG_CORE(4, "Telegram Core", R.drawable.left_status_profile, "https://my.telegram.org", 2),
    TRANSLATIONS(5, "Translations", R.drawable.msg_translate, "https://translations.telegram.org", 2),
    CONTEST(6, "Contest", R.drawable.gift_unpack, "https://contest.com", 2),
    ADS(7, "Ads", R.drawable.msg_language, "https://ads.telegram.org", 2),
    GATEWAY(8, "Gateway", R.drawable.msg2_ask_question, "https://gateway.telegram.org", 2),
    TELEGRAPH(9, "Telegraph", R.drawable.msg_text_outlined, "https://telegra.ph", 3),
    FRAGMENT(10, "Fragment", R.drawable.fragment, "https://fragment.com", 3),
    QUIZ_DIRECTORY(11, "Quiz Directory", R.drawable.msg_channel, "https://quiz.directory", 3);
}

enum class WebPagesCategory(val id: Int, var text: String) {
    WEBSITES(1, getString(R.string.DatacenterStatus_Web)),
    PLATFORMS(2, getString(R.string.DatacenterStatus_WebPlatforms)),
    SERVICES(3, getString(R.string.DatacenterStatus_WebServices));

    companion object {
        fun getPageInfo(pageId: Int): WebPagesCategory {
            return WebPagesCategory.entries.find { it.id == pageId } ?: WEBSITES
        }
    }
}

enum class FontType(val path: String) {
    ROBOTO_MEDIUM("fonts/rmedium.ttf"),
    ROBOTO_MEDIUM_ITALIC("fonts/rmediumitalic.ttf"),
    ROBOTO_MONO("fonts/rmono.ttf"),
    MERRIWEATHER_BOLD("fonts/mw_bold.ttf"),
    COURIER_NEW_BOLD("fonts/courier_new_bold.ttf");

    companion object {
        fun fromPath(path: String): FontType? = FontType.entries.find { it.path == path }
    }
}

object VideoQuality {
    const val VALUE = 0

    fun getValue(@Quality quality: Int): Int {
        return quality
    }

    const val UNKNOWN = 0
    const val SD = 1
    const val HD = 2
    const val FHD = 3
    const val QHD = 4
    const val UHD = 5
    const val MAX = 6

    @IntDef(UNKNOWN, SD, HD, FHD, QHD, UHD, MAX)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Quality

    @Quality
    fun fromInt(value: Int): Int {
        return when (value) {
            SD -> SD
            HD -> HD
            FHD -> FHD
            QHD -> QHD
            UHD -> UHD
            MAX -> MAX
            else -> UNKNOWN
        }
    }
}

enum class PromptBeforeSendMedia(val id: Int) {
    STICKERS(0),
    GIFS(1)
}

enum class MonetTheme(val monetThemeName: String, val monetThemeFileName: String) {
    MONET_AMOLED("Monet Amoled", "monet_amoled.attheme"),
    MONET_DARK("Monet Dark", "monet_dark.attheme"),
    MONET_LIGHT("Monet Light", "monet_light.attheme"),
}

object CameraPreview {
    const val DEFAULT = 0
    const val HIDDEN = 1
    const val BOTTOM_BAR = 2

    @IntDef(DEFAULT, HIDDEN, BOTTOM_BAR)
    @Retention(AnnotationRetention.SOURCE)
    annotation class PreviewType
}
