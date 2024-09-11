/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

package it.octogram.android

import android.graphics.Color
import org.telegram.messenger.LocaleController
import org.telegram.messenger.R

enum class DcIdStyle(val value: Int) {
    NONE(0),
    OWLGRAM(1),
    TELEGRAM(2),
    MINIMAL(3)
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
    EDIT(7);

    companion object {
        fun fromInt(value: Int): DoubleTapAction {
            return entries.find { it.value == value } ?: DISABLED
        }
    }
}

enum class PhoneNumberAlternative(val value: Int) {
    SHOW_HIDDEN_NUMBER_STRING(0),
    SHOW_FAKE_PHONE_NUMBER(1),
    SHOW_USERNAME(2);

    companion object {
        fun fromInt(value: Int): PhoneNumberAlternative {
            return entries.find { it.value == value } ?: SHOW_HIDDEN_NUMBER_STRING
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

    companion object{
        fun fromInt(value: Int): EventType {
            return entries.find { it.value == value } ?: DEFAULT
        }
    }
}

enum class AudioType(val value: Int) {
    MONO(0),
    STEREO(1)
}

enum class CameraXResolution(val value: Int) {
    SD(0),
    HD(1),
    FHD(2),
    UHD(3),
    None(4)
}

enum class CameraResolution(val value: Int, val id: Int) {
    LOW(360, 0),
    SD(480, 1),
    HD(720, 2),
    FULL_HD(1080, 3),
    QHD(1440, 4),
    UHD_4K(2160, 5),
    UHD_8K(4320, 6);

    companion object {
        fun fromHeight(height: Int): CameraResolution {
            return entries.find { it.value == height } ?: LOW
        }
    }
}

enum class PhotoResolution(val value: Int) {
    LOW(0),
    DEFAULT(1),
    HIGH(2);
    companion object {
        fun fromInt(value: Int): PhotoResolution {
            return entries.find { it.value == value } ?: DEFAULT
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
    DEEPL(3)
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
    CHAT_PHOTOS(13)
}

enum class CameraType(val value: Int) {
    TELEGRAM(0),
    CAMERA_X(1),
    CAMERA_2(2),
    SYSTEM_CAMERA(3);

    companion object {
        fun fromInt(value: Int): CameraType {
            return entries.find { it.value == value } ?: TELEGRAM
        }
    }
}

enum class IconsUIType(val value: Int) {
    DEFAULT(0),
    SOLAR(1),
    MATERIAL_DESIGN_3(2)
}

enum class MenuItemId(val id: String, val itemId: Int) {
    MY_PROFILE("my_profile", 16),
    NEW_GROUP("new_group", 2),
    CONTACTS("contacts", 6),
    CALLS("calls", 10),
    SAVED_MESSAGE("saved_message", 11),
    SETTINGS("settings", 8),
    OCTOGRAM_SETTINGS("octogram_settings", 100),
    NEW_CHANNEL("new_channel", 4),
    NEW_SECRET_CHAT("new_secret_chat", 3),
    INVITE_FRIENDS("invite_friends", 7),
    TELEGRAM_FEATURES("telegram_features", 13),
    ARCHIVED_MESSAGES("archived_messages", 202),
    DATACENTER_STATUS("datacenter_status", 101),
    QR_LOGIN("qr_login", 204),
    SET_STATUS("set_status", 15),
    CONNECTED_DEVICES("connected_devices", 913),
    POWER_USAGE("power_usage", 206),
    PROXY_SETTINGS("proxy_settings", 207),
    DIVIDER("divider", 0),
    ATTACH_MENU_BOT("attach_menu_bot", 205),
    DOWNLOADS("downloads", 912),
    TELEGRAM_BROWSER("tg_browser", 914);

    companion object {
        fun getById(id: String): MenuItemId? {
            return entries.find { it.id == id }
        }
    }
}

enum class NewFeaturesBadgeId(val id: String) {
    UPDATES_BADGE("ub"),

    GENERAL_BADGE("gb"),

    APPEARANCE_BADGE("ab"),
    CHATS_BADGE("cb"),
    DRAWER_BADGE("db"),

    EXPERIMENTAL_BADGE("eb"),
    ALTERNATIVE_NAVIGATION_BADGE("anb")
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
    HEADER_CUSTOM_TITLE(22)
}

enum class DrawerBackgroundState(val value: Int) {
    TRANSPARENT(0),
    WALLPAPER(1),
    PROFILE_PIC(2),
    COLOR(3)
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
    CUSTOM(4)
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
}

enum class InterfaceSwitchUI(val value: Int) {
    DEFAULT(0),
    ONEUIOLD(1),
    ONEUINEW(2),
    GOOGLE(3)
}

enum class InterfaceCheckboxUI(val value: Int) {
    DEFAULT(0),
    ROUNDED(1),
    TRANSPARENT_UNCHECKED(2),
    ALWAYS_TRANSPARENT(3)
}

enum class ViewType {
    ACCOUNT,
    ADD_EXCEPTION,
    BLUR_INTENSITY,
    CAMERA_SELECTOR,
    CHAT,
    CHECKBOX,
    CHECKBOX_CELL,
    CREATION_TEXT_CELL,
    DC_STYLE_SELECTOR,
    DETAILED_SETTINGS,
    DYNAMIC_BUTTON_SELECTOR,
    EDIT_TOPIC,
    EMOJI_PACK_SET_CELL,
    HEADER,
    HEADER_NO_SHADOW,
    HINT_HEADER,
    IMAGE_HEADER,
    MANAGE_CHAT,
    MENU_ITEM,
    PLACEHOLDER,
    PROFILE_PREVIEW,
    RADIO,
    SETTINGS,
    SHADOW,
    SLIDE_CHOOSE,
    STICKER_HOLDER,
    STICKER_SIZE,
    SUGGESTED_OPTIONS,
    SWITCH,
    TEXT_CELL,
    TEXT_CHECK_CELL2,
    TEXT_HINT,
    TEXT_HINT_WITH_PADDING,
    TEXT_RADIO,
    THEME_SELECTOR,
    UPDATE,
    UPDATE_CHECK;

    fun toInt(): Int {
        return ordinal
    }

    companion object {
        fun fromInt(value: Int): ViewType = entries[value] }

}

enum class Datacenter(
    val dcId: Int,
    val dcName: String,
    val icon: Int,
    val color: Int,
    val ip: String
) {
    USA_1(1, "MIA, Miami FL, USA", R.drawable.ic_pluto_datacenter, Color.parseColor("#329AFE"), "149.154.175.50"),
    USA_2(3, "MIA, Miami FL, USA", R.drawable.ic_aurora_datacenter, Color.parseColor("#DA5653"), "149.154.175.100"),
    AMSTERDAM_1(2, "AMS, Amsterdam, NL", R.drawable.ic_venus_datacenter, Color.parseColor("#8B31FD"), "149.154.167.50"),
    AMSTERDAM_2(4, "AMS, Amsterdam, NL", R.drawable.ic_vesta_datacenter, Color.parseColor("#F7B139"), "149.154.167.91"),
    SINGAPORE(5, "SIN, Singapore, SG", R.drawable.ic_flora_datacenter, Color.parseColor("#4BD199"), "91.108.56.100"),
    UNKNOWN(-1, LocaleController.getString(R.string.NumberUnknown), R.drawable.msg_secret_hw, Color.TRANSPARENT, "");

    companion object {
        fun getDcInfo(dcId: Int): Datacenter {
            return entries.find { it.dcId == dcId } ?: UNKNOWN
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
        fun fromPath(path: String): FontType? = entries.find { it.path == path }
    }
}

enum class VideoQuality(val value: Int) {
    UNKNOWN(0),
    SD(1),
    HD(2),
    FHD(3),
    QHD(4),
    UHD(5),
    MAX(6);

    companion object {
        fun fromInt(value: Int): VideoQuality {
            return entries.find { it.value == value } ?: UNKNOWN
        }
    }
}

enum class PromptBeforeSendMedia(val id: Int) {
    STICKERS(0),
    GIFS(1)
}