/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.theme

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.PatternMatcher
import androidx.annotation.RequiresApi
import it.octogram.android.utils.OctoLogging
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.Theme

/**
 *  Controller for managing Monet theming in OctoGram.
 *
 *  This object handles loading colors based on Android's Monet theming system (Material You)
 *  and dynamically updates the application theme when system overlay changes are detected.
 *
 *  Requires API level S (31) or higher for Monet color access.
 */
@RequiresApi(Build.VERSION_CODES.S)
object MonetThemeController {

    private val TAG = MonetThemeController::class.java.simpleName

    /**
     *  Mapping of color keys to resource IDs for Monet system colors.
     *  Generated programmatically for better readability and maintainability.
     *  Now using direct R.color access to avoid `getIdentifier`.
     */
    private val ids: Map<String, Int> = generateMonetColorMap()

    /**
     * Generates the map of Monet color keys to their corresponding resource IDs using direct R.color access.
     * This avoids the use of `getIdentifier` for better performance and compile-time safety.
     */
    private fun generateMonetColorMap(): Map<String, Int> {
        return mapOf(
            "a1_0" to R.color.system_accent1_0,
            "a1_10" to R.color.system_accent1_10,
            "a1_50" to R.color.system_accent1_50,
            "a1_100" to R.color.system_accent1_100,
            "a1_200" to R.color.system_accent1_200,
            "a1_300" to R.color.system_accent1_300,
            "a1_400" to R.color.system_accent1_400,
            "a1_500" to R.color.system_accent1_500,
            "a1_600" to R.color.system_accent1_600,
            "a1_700" to R.color.system_accent1_700,
            "a1_800" to R.color.system_accent1_800,
            "a1_900" to R.color.system_accent1_900,
            "a1_1000" to R.color.system_accent1_1000,
            "a2_0" to R.color.system_accent2_0,
            "a2_10" to R.color.system_accent2_10,
            "a2_50" to R.color.system_accent2_50,
            "a2_100" to R.color.system_accent2_100,
            "a2_200" to R.color.system_accent2_200,
            "a2_300" to R.color.system_accent2_300,
            "a2_400" to R.color.system_accent2_400,
            "a2_500" to R.color.system_accent2_500,
            "a2_600" to R.color.system_accent2_600,
            "a2_700" to R.color.system_accent2_700,
            "a2_800" to R.color.system_accent2_800,
            "a2_900" to R.color.system_accent2_900,
            "a2_1000" to R.color.system_accent2_1000,
            "a3_0" to R.color.system_accent3_0,
            "a3_10" to R.color.system_accent3_10,
            "a3_50" to R.color.system_accent3_50,
            "a3_100" to R.color.system_accent3_100,
            "a3_200" to R.color.system_accent3_200,
            "a3_300" to R.color.system_accent3_300,
            "a3_400" to R.color.system_accent3_400,
            "a3_500" to R.color.system_accent3_500,
            "a3_600" to R.color.system_accent3_600,
            "a3_700" to R.color.system_accent3_700,
            "a3_800" to R.color.system_accent3_800,
            "a3_900" to R.color.system_accent3_900,
            "a3_1000" to R.color.system_accent3_1000,
            "n1_0" to R.color.system_neutral1_0,
            "n1_10" to R.color.system_neutral1_10,
            "n1_50" to R.color.system_neutral1_50,
            "n1_100" to R.color.system_neutral1_100,
            "n1_200" to R.color.system_neutral1_200,
            "n1_300" to R.color.system_neutral1_300,
            "n1_400" to R.color.system_neutral1_400,
            "n1_500" to R.color.system_neutral1_500,
            "n1_600" to R.color.system_neutral1_600,
            "n1_700" to R.color.system_neutral1_700,
            "n1_800" to R.color.system_neutral1_800,
            "n1_900" to R.color.system_neutral1_900,
            "n1_1000" to R.color.system_neutral1_1000,
            "n2_0" to R.color.system_neutral2_0,
            "n2_10" to R.color.system_neutral2_10,
            "n2_50" to R.color.system_neutral2_50,
            "n2_100" to R.color.system_neutral2_100,
            "n2_200" to R.color.system_neutral2_200,
            "n2_300" to R.color.system_neutral2_300,
            "n2_400" to R.color.system_neutral2_400,
            "n2_500" to R.color.system_neutral2_500,
            "n2_600" to R.color.system_neutral2_600,
            "n2_700" to R.color.system_neutral2_700,
            "n2_800" to R.color.system_neutral2_800,
            "n2_900" to R.color.system_neutral2_900,
            "n2_1000" to R.color.system_neutral2_1000,
            "monetRedDark" to R.color.monetRedDark,
            "monetRedLight" to R.color.monetRedLight,
            "monetRedCall" to R.color.colorCallRed,
            "monetGreenCall" to R.color.colorCallGreen
        )
    }


    /**
     * Parses the color key, adjusting for AMOLED themes where necessary.
     * For AMOLED themes, 'n1_900' is mapped to 'n1_1000' for deeper blacks.
     *
     * @param color    The base color key (e.g., "n1_900").
     * @param isAmoled True if the current theme is AMOLED, false otherwise.
     * @return         The adjusted color key for resource lookup.
     */
    private fun parseColorKey(color: String, isAmoled: Boolean): String =
        if (isAmoled && color == "n1_900") "n1_1000" else color

    fun getColor(color: String): Int {
        return getColor(color, false)
    }

    /**
     * Retrieves the color value for a given color key.
     *
     * @param color The color key to lookup (e.g., "a1_500").
     * @param isAmoled True if the current theme is AMOLED, false otherwise.
     * @return      The color integer value, or 0 if an error occurs or the color is not found.
     *              Returning 0 indicates a potential issue loading the color. Consider handling this in the caller.
     */
    fun getColor(color: String, isAmoled: Boolean): Int {
        val resolvedColorKey = parseColorKey(color, isAmoled)
        return ids[resolvedColorKey]?.let { resourceId ->
            try {
                OctoLogging.d(
                    "Theme",
                    "Loading color $resolvedColorKey from resource ID: $resourceId"
                )
                ApplicationLoader.applicationContext.getColor(resourceId)
            } catch (e: Exception) {
                OctoLogging.e("Theme", "Error loading color $resolvedColorKey", e)
                0 // Return 0 to indicate error, caller should handle this case
            }
        } ?: run {
            OctoLogging.w("Theme", "Color key '$resolvedColorKey' not found in Monet color map.")
            0 // Color key not found, return 0
        }
    }

    /**
     * BroadcastReceiver to listen for system overlay changes, specifically theme changes.
     * When a change is detected and the current theme is Monet, it reapplies the theme.
     */
    private class OverlayChangeReceiver : BroadcastReceiver() {
        fun register(context: Context) {
            val packageFilter = IntentFilter(ACTION_OVERLAY_CHANGED).apply {
                addDataScheme("package")
                addDataSchemeSpecificPart("android", PatternMatcher.PATTERN_LITERAL)
            }
            context.registerReceiver(this, packageFilter)
        }

        fun unregister(context: Context) {
            context.unregisterReceiver(this)
        }

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_OVERLAY_CHANGED) {
                Theme.getActiveTheme()?.takeIf { it.isMonet }
                    ?.let { theme -> Theme.applyTheme(theme) }
            }
        }
    }

    private const val ACTION_OVERLAY_CHANGED = "android.intent.action.OVERLAY_CHANGED"
    private val overlayChangeReceiver = OverlayChangeReceiver()

    /**
     * Registers the OverlayChangeReceiver to listen for theme changes.
     * Should be called when the application or relevant component starts.
     */
    fun registerReceiver(context: Context) {
        overlayChangeReceiver.register(context)
    }

    /**
     * Unregisters the OverlayChangeReceiver to stop listening for theme changes.
     * Should be called when the application or relevant component is destroyed to prevent leaks.
     */
    fun unregisterReceiver(context: Context) {
        try {
            overlayChangeReceiver.unregister(context)
        } catch (e: IllegalArgumentException) {
            OctoLogging.e(TAG, "Error unregistering OverlayChangeReceiver", e)
        }
    }

//    val ListAvatarColors = listOf(
//        "avatar_backgroundBlue",
//        "avatar_backgroundCyan",
//        "avatar_backgroundGreen",
//        "avatar_backgroundOrange",
//        "avatar_backgroundPink",
//        "avatar_backgroundRed",
//        "avatar_backgroundSaved",
//        "avatar_backgroundViolet",
//    )
//
//    val ListToReplaceNewThemeTelegram = listOf(
//        "chat_messageLinkOut",
//        "chat_messageTextOut",
//        "chat_outAdminSelectedText",
//        "chat_outAdminText",
//        "chat_outAudioCacheSeekbar",
//        "chat_outAudioDurationSelectedText",
//        "chat_outAudioDurationText",
//        "chat_outAudioPerfomerSelectedText",
//        "chat_outAudioPerfomerText",
//        "chat_outAudioProgress",
//        "chat_outAudioSeekbar",
//        "chat_outAudioSeekbarFill",
//        "chat_outAudioSeekbarSelected",
//        "chat_outAudioSelectedProgress",
//        "chat_outAudioTitleText",
//        "chat_outBubble",
//        "chat_outBubbleGradient",
//        "chat_outBubbleGradient2",
//        "chat_outBubbleGradient3",
//        "chat_outBubbleGradientAnimated",
//        "chat_outBubbleGradientSelectedOverlay",
//        "chat_outBubbleSelected",
//        "chat_outBubbleShadow",
//        "chat_outContactBackground",
//        "chat_outContactIcon",
//        "chat_outContactNameText",
//        "chat_outContactPhoneSelectedText",
//        "chat_outContactPhoneText",
//        "chat_outFileBackground",
//        "chat_outFileBackgroundSelected",
//        "chat_outFileInfoSelectedText",
//        "chat_outFileInfoText",
//        "chat_outFileNameText",
//        "chat_outFileProgress",
//        "chat_outFileProgressSelected",
//        "chat_outForwardedNameText",
//        "chat_outInstant",
//        "chat_outInstantSelected",
//        "chat_outLinkSelectBackground",
//        "chat_outLoader",
//        "chat_outLoaderSelected",
//        "chat_outLocationIcon",
//        "chat_outMediaIcon",
//        "chat_outMediaIconSelected",
//        "chat_outMenu",
//        "chat_outMenuSelected",
//        "chat_outPollCorrectAnswer",
//        "chat_outPollWrongAnswer",
//        "chat_outPreviewInstantText",
//        "chat_outPreviewLine",
//        "chat_outPsaNameText",
//        "chat_outReactionButtonBackground",
//        "chat_outReactionButtonText",
//        "chat_outReactionButtonTextSelected",
//        "chat_outReplyLine",
//        "chat_outReplyMediaMessageSelectedText",
//        "chat_outReplyMediaMessageText",
//        "chat_outReplyMessageText",
//        "chat_outReplyNameText",
//        "chat_outSentCheck",
//        "chat_outSentCheckRead",
//        "chat_outSentCheckReadSelected",
//        "chat_outSentCheckSelected",
//        "chat_outSentClock",
//        "chat_outSentClockSelected",
//        "chat_outSiteNameText",
//        "chat_outTextSelectionCursor",
//        "chat_outTextSelectionHighlight",
//        "chat_outTimeSelectedText",
//        "chat_outTimeText",
//        "chat_outUpCall",
//        "chat_outVenueInfoSelectedText",
//        "chat_outVenueInfoText",
//        "chat_outViaBotNameText",
//        "chat_outViews",
//        "chat_outViewsSelected",
//        "chat_outVoiceSeekbar",
//        "chat_outVoiceSeekbarFill",
//        "chat_outVoiceSeekbarSelected",
//    )
}
