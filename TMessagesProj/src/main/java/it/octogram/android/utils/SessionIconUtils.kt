package it.octogram.android.utils

import android.graphics.Color
import android.util.Log
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
            Log.e(tag, "appName: $appName")
            Log.e(tag, "apiId: $apiId")
            Log.e(tag, "deviceModel: $deviceModel")
            Log.e(tag, "platform: $platform")
            Log.e(tag, "systemVersion: $systemVersion")
        }

        val isWeb = appName.isNotEmpty() && appName.contains("web") &&
                (appName.indexOf("web") + 3 == appName.length ||
                        !Character.isLowerCase(appName[appName.indexOf("web") + 3]))
        val isOcto = apiId == 14565251
        val isTGX = apiId == 21724

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
            SessionType.TELEGRAMX -> DeviceAttributes(R.drawable.baseline_device_android_x_24, Theme.key_avatar_backgroundBlue, Theme.key_avatar_background2Blue, -1)
            SessionType.OCTOGRAM -> DeviceAttributes(R.drawable.baseline_octo_24, -1, -1, -1, Color.parseColor("#3D358B"))
            SessionType.API -> DeviceAttributes(R.drawable.filled_paid_broadcast, Theme.key_avatar_backgroundGreen, Theme.key_avatar_background2Green, -1)
            SessionType.ANONYMOUS -> DeviceAttributes(R.drawable.large_hidden, Theme.key_avatar_backgroundBlue, Theme.key_avatar_background2Blue, -1)
            SessionType.ADS -> DeviceAttributes(R.drawable.msg_channel, Theme.key_avatar_backgroundPink, Theme.key_avatar_background2Pink, -1)
            else -> DeviceAttributes(R.drawable.device_web_other, Theme.key_avatar_backgroundPink, Theme.key_avatar_background2Pink, R.raw.chrome_30)
        }
    }

    enum class SessionType {
        UNKNOWN, BRAVE, VIVALDI, OPERA, EDGE, CHROME, FIREFOX, SAFARI,
        ANDROID, ANDROID_TABLET, WINDOWS, UBUNTU, LINUX, IPHONE, IPAD, MAC,
        PREMIUMBOT, FRAGMENT, QUESTION, TELEGRAMX, OCTOGRAM, API, ANONYMOUS,
        ADS
    }

    data class DeviceAttributes(
        val iconId: Int,
        val colorKey: Int,
        val colorKey2: Int,
        val animatedIcon: Int,
        val customColor: Int = -1
    )

    private fun safeLowerCase(input: String?): String {
        if (BuildVars.DEBUG_PRIVATE_VERSION) Log.e(tag, "safeLowerCase: $input")
        return (input ?: "").lowercase()
    }
}
