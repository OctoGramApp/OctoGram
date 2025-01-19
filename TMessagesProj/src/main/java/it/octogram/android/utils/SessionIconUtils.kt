/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils

import android.graphics.Color
import it.octogram.android.logs.OctoLogging
import org.telegram.messenger.BuildVars
import org.telegram.messenger.R
import org.telegram.tgnet.TLRPC
import org.telegram.ui.ActionBar.Theme

class SessionIconUtils {
    private val tag = "SessionIconUtils"

    private fun getSessionTypeObject(authorization: TLRPC.TL_authorization): SessionType {
        val appName = safeLowerCase(authorization.app_name)
        val deviceModel = safeLowerCase(authorization.device_model)
        val platform = safeLowerCase(authorization.platform).ifEmpty { safeLowerCase(authorization.system_version) }
        val systemVersion = safeLowerCase(authorization.system_version)
        val apiId = authorization.api_id

        if (BuildVars.DEBUG_PRIVATE_VERSION) {
            OctoLogging.e(tag, "appName: $appName")
            OctoLogging.e(tag, "apiId: $apiId")
            OctoLogging.e(tag, "deviceModel: $deviceModel")
            OctoLogging.e(tag, "platform: $platform")
            OctoLogging.e(tag, "systemVersion: $systemVersion")
        }

        val isWeb = appName.isNotEmpty() && appName.contains("web") &&
                (appName.indexOf("web") + 3 == appName.length ||
                        !Character.isLowerCase(appName[appName.indexOf("web") + 3]))
        val isOcto = apiId == 14565251
        val isTGX = apiId == 21724
        val isKitsuGram = apiId == 25806020
        val isMaterialGram = apiId == 26417228
        val isSwiftGram = apiId == 14196478
        val isWearGram = apiId == 7991083

        return when {
            isWeb -> when {
                deviceModel.contains("brave") -> SessionType.BRAVE
                deviceModel.contains("vivaldi") -> SessionType.VIVALDI
                deviceModel.contains("opera") || deviceModel.contains("opr") -> SessionType.OPERA
                deviceModel.contains("edg") -> SessionType.EDGE
                deviceModel.contains("chrome") -> SessionType.CHROME
                deviceModel.contains("firefox") || deviceModel.contains("fxios") -> SessionType.FIREFOX
                deviceModel.contains("safari") -> SessionType.SAFARI
                else -> SessionType.UNKNOWN
            }
            isOcto -> SessionType.OCTOGRAM
            isTGX -> SessionType.TELEGRAMX
            isKitsuGram -> SessionType.KITSUGRAM
            isMaterialGram -> SessionType.MATERIALGRAM
            isSwiftGram -> SessionType.SWIFTGRAM
            isWearGram -> SessionType.WEARGRAM
            platform.isNotEmpty() && platform.startsWith("android") || systemVersion.contains("android") -> SessionType.ANDROID
            deviceModel.contains("tab") -> SessionType.ANDROID_TABLET
            platform.contains("windows") || systemVersion.contains("windows") -> SessionType.WINDOWS
            platform.contains("ubuntu") || systemVersion.contains("ubuntu") || systemVersion.contains("linux") -> SessionType.LINUX
            platform.isNotEmpty() && platform.startsWith("ios") || systemVersion.contains("ios") -> when {
                deviceModel.contains("iphone") -> SessionType.IPHONE
                deviceModel.contains("ipad") -> SessionType.IPAD
                else -> SessionType.UNKNOWN
            }
            platform.contains("macos") || systemVersion.contains("macos") -> when {
                deviceModel.contains("mac") || platform.contains("macos") -> SessionType.MAC
                else -> SessionType.UNKNOWN
            }
            platform == "fragment" -> SessionType.FRAGMENT
            platform == "premiumbot" -> SessionType.PREMIUMBOT
            platform == "api" -> SessionType.API
            platform == "anonymous" -> SessionType.ANONYMOUS
            platform == "ads" -> SessionType.ADS
            platform == "?" -> SessionType.QUESTION
            else -> SessionType.UNKNOWN
        }
    }

    fun setDeviceAttributes(session: TLRPC.TL_authorization): DeviceAttributes {
        val sessionType = getSessionTypeObject(session)
        val octoColor = Color.parseColor("#3D358B")

        val tgxColor = Color.parseColor("#6785A6")
        val tgxColor2 = Color.parseColor("#202E3D")

        val materialGramColor = Color.parseColor("#3E6EAE")
        val materialGramColor2 = Color.parseColor("#8BB3F8")

        val swiftGramColor = Color.parseColor("#F67E47")
        val swiftGramColor2 = Color.parseColor("#F1532C")

        return when (sessionType) {
            SessionType.SAFARI -> DeviceAttributes(R.drawable.device_web_safari, Theme.key_avatar_backgroundPink, Theme.key_avatar_background2Pink, R.raw.safari_30)
            SessionType.EDGE -> DeviceAttributes(R.drawable.device_web_edge, Theme.key_avatar_backgroundPink, Theme.key_avatar_background2Pink, R.raw.edge_30)
            SessionType.CHROME -> DeviceAttributes(R.drawable.device_web_chrome, Theme.key_avatar_backgroundPink, Theme.key_avatar_background2Pink, R.raw.chrome_30)
            SessionType.OPERA -> DeviceAttributes(R.drawable.device_web_opera, Theme.key_avatar_backgroundPink, Theme.key_avatar_background2Pink, -1)
            SessionType.FIREFOX -> DeviceAttributes(R.drawable.device_web_firefox, Theme.key_avatar_backgroundPink, Theme.key_avatar_background2Pink, -1)
            SessionType.VIVALDI -> DeviceAttributes(R.drawable.device_web_other, Theme.key_avatar_backgroundPink, Theme.key_avatar_background2Pink, -1)
            SessionType.IPHONE -> DeviceAttributes(R.drawable.device_phone_ios, Theme.key_avatar_backgroundBlue, Theme.key_avatar_background2Blue, R.raw.iphone_30)
            SessionType.IPAD -> DeviceAttributes(R.drawable.device_tablet_ios, Theme.key_avatar_backgroundBlue, Theme.key_avatar_background2Blue, R.raw.ipad_30)
            SessionType.WINDOWS -> DeviceAttributes(R.drawable.device_desktop_win, Theme.key_avatar_backgroundCyan, Theme.key_avatar_background2Cyan, R.raw.windows_30)
            SessionType.MAC -> DeviceAttributes(R.drawable.device_desktop_osx, Theme.key_avatar_backgroundCyan, Theme.key_avatar_background2Cyan, R.raw.mac_30)
            SessionType.UBUNTU, SessionType.LINUX -> DeviceAttributes(R.drawable.device_desktop_other, Theme.key_avatar_backgroundCyan, Theme.key_avatar_background2Cyan, R.raw.linux_30)
            SessionType.ANDROID -> DeviceAttributes(R.drawable.device_phone_android, Theme.key_avatar_backgroundGreen, Theme.key_avatar_background2Green, R.raw.android_30)
            SessionType.ANDROID_TABLET -> DeviceAttributes(R.drawable.device_tablet_android, Theme.key_avatar_backgroundGreen, Theme.key_avatar_background2Green, R.raw.android_30)
            SessionType.FRAGMENT -> DeviceAttributes(R.drawable.fragment, -1, -1, -1)
            SessionType.PREMIUMBOT -> DeviceAttributes(R.drawable.filled_star_plus, Theme.key_color_yellow, Theme.key_color_orange, -1)
            SessionType.QUESTION -> DeviceAttributes(R.drawable.msg_emoji_question, -1, -1, -1)
            SessionType.TELEGRAMX -> DeviceAttributes(R.drawable.baseline_device_android_x_24, -1, -1, -1, tgxColor, tgxColor2)
            SessionType.OCTOGRAM -> DeviceAttributes(R.drawable.baseline_octo_24, -1, -1, -1, octoColor, octoColor)
            SessionType.API -> DeviceAttributes(R.drawable.filled_paid_broadcast, Theme.key_avatar_backgroundGreen, Theme.key_avatar_background2Green, -1)
            SessionType.ANONYMOUS -> DeviceAttributes(R.drawable.large_hidden, Theme.key_avatar_backgroundBlue, Theme.key_avatar_background2Blue, -1)
            SessionType.ADS -> DeviceAttributes(R.drawable.msg_channel, Theme.key_avatar_backgroundPink, Theme.key_avatar_background2Pink, -1)
            SessionType.KITSUGRAM -> DeviceAttributes(R.drawable.kitsugram, -1, -1, -1, tgxColor, tgxColor2)
            SessionType.MATERIALGRAM -> DeviceAttributes(R.drawable.materialgram,-1,-1,-1, materialGramColor, materialGramColor2)
            SessionType.SWIFTGRAM -> DeviceAttributes(R.drawable.swiftgram, -1,-1,-1, swiftGramColor, swiftGramColor2)
            SessionType.WEARGRAM -> DeviceAttributes(R.drawable.weargram, Theme.key_avatar_backgroundCyan, Theme.key_avatar_background2Cyan, -1)
            else -> DeviceAttributes(R.drawable.device_web_other, Theme.key_avatar_backgroundPink, Theme.key_avatar_background2Pink, R.raw.chrome_30)
        }
    }

    enum class SessionType {
        UNKNOWN, BRAVE, VIVALDI, OPERA, EDGE, CHROME, FIREFOX, SAFARI,
        ANDROID, ANDROID_TABLET, WINDOWS, UBUNTU, LINUX, IPHONE, IPAD, MAC,
        PREMIUMBOT, FRAGMENT, QUESTION, TELEGRAMX, OCTOGRAM, API, ANONYMOUS,
        ADS, KITSUGRAM, MATERIALGRAM, SWIFTGRAM, WEARGRAM
    }

    data class DeviceAttributes(
        val iconId: Int,
        val colorKey: Int,
        val colorKey2: Int,
        val animatedIcon: Int,
        val customColor: Int = -1,
        val customColor2: Int = -1
    )

    private fun safeLowerCase(input: String?): String {
        if (BuildVars.DEBUG_PRIVATE_VERSION) OctoLogging.e(tag, "safeLowerCase: $input")
        return (input ?: "").lowercase()
    }
}
