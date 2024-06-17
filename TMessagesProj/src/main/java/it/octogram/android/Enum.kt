package it.octogram.android

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
    EDIT(7)
}

enum class EventType(val value: Int) {
    NONE(5),
    DEFAULT(0),
    HOLIDAY(1),
    VALENTINE(2),
    HALLOWEEN(3),
    LUNAR_NEW_YEAR(4)
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

enum class PhotoResolution(val value: Int) {
    LOW(0),
    DEFAULT(1),
    HIGH(2)
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

enum class Shape(val value: Int) {
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
    CAMERA_X(0),
    CAMERA_2(1),
    SYSTEM(2)
}