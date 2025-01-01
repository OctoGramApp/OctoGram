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
import android.util.Log
import androidx.annotation.RequiresApi
import it.octogram.android.logs.OctoLogging
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.R
import org.telegram.ui.ActionBar.Theme

@RequiresApi(Build.VERSION_CODES.S)
object MonetThemeController {
    private val ids: MutableList<Pair<String, Int>> = mutableListOf(
        "a1_0" to android.R.color.system_accent1_0,
        "a1_10" to android.R.color.system_accent1_10,
        "a1_50" to android.R.color.system_accent1_50,
        "a1_100" to android.R.color.system_accent1_100,
        "a1_200" to android.R.color.system_accent1_200,
        "a1_300" to android.R.color.system_accent1_300,
        "a1_400" to android.R.color.system_accent1_400,
        "a1_500" to android.R.color.system_accent1_500,
        "a1_600" to android.R.color.system_accent1_600,
        "a1_700" to android.R.color.system_accent1_700,
        "a1_800" to android.R.color.system_accent1_800,
        "a1_900" to android.R.color.system_accent1_900,
        "a1_1000" to android.R.color.system_accent1_1000,
        "a2_0" to android.R.color.system_accent2_0,
        "a2_10" to android.R.color.system_accent2_10,
        "a2_50" to android.R.color.system_accent2_50,
        "a2_100" to android.R.color.system_accent2_100,
        "a2_200" to android.R.color.system_accent2_200,
        "a2_300" to android.R.color.system_accent2_300,
        "a2_400" to android.R.color.system_accent2_400,
        "a2_500" to android.R.color.system_accent2_500,
        "a2_600" to android.R.color.system_accent2_600,
        "a2_700" to android.R.color.system_accent2_700,
        "a2_800" to android.R.color.system_accent2_800,
        "a2_900" to android.R.color.system_accent2_900,
        "a2_1000" to android.R.color.system_accent2_1000,
        "a3_0" to android.R.color.system_accent3_0,
        "a3_10" to android.R.color.system_accent3_10,
        "a3_50" to android.R.color.system_accent3_50,
        "a3_100" to android.R.color.system_accent3_100,
        "a3_200" to android.R.color.system_accent3_200,
        "a3_300" to android.R.color.system_accent3_300,
        "a3_400" to android.R.color.system_accent3_400,
        "a3_500" to android.R.color.system_accent3_500,
        "a3_600" to android.R.color.system_accent3_600,
        "a3_700" to android.R.color.system_accent3_700,
        "a3_800" to android.R.color.system_accent3_800,
        "a3_900" to android.R.color.system_accent3_900,
        "a3_1000" to android.R.color.system_accent3_1000,
        "n1_0" to android.R.color.system_neutral1_0,
        "n1_10" to android.R.color.system_neutral1_10,
        "n1_50" to android.R.color.system_neutral1_50,
        "n1_100" to android.R.color.system_neutral1_100,
        "n1_200" to android.R.color.system_neutral1_200,
        "n1_300" to android.R.color.system_neutral1_300,
        "n1_400" to android.R.color.system_neutral1_400,
        "n1_500" to android.R.color.system_neutral1_500,
        "n1_600" to android.R.color.system_neutral1_600,
        "n1_700" to android.R.color.system_neutral1_700,
        "n1_800" to android.R.color.system_neutral1_800,
        "n1_900" to android.R.color.system_neutral1_900,
        "n1_1000" to android.R.color.system_neutral1_1000,
        "n2_0" to android.R.color.system_neutral2_0,
        "n2_10" to android.R.color.system_neutral2_10,
        "n2_50" to android.R.color.system_neutral2_50,
        "n2_100" to android.R.color.system_neutral2_100,
        "n2_200" to android.R.color.system_neutral2_200,
        "n2_300" to android.R.color.system_neutral2_300,
        "n2_400" to android.R.color.system_neutral2_400,
        "n2_500" to android.R.color.system_neutral2_500,
        "n2_600" to android.R.color.system_neutral2_600,
        "n2_700" to android.R.color.system_neutral2_700,
        "n2_800" to android.R.color.system_neutral2_800,
        "n2_900" to android.R.color.system_neutral2_900,
        "n2_1000" to android.R.color.system_neutral2_1000,
        "monetRedDark" to R.color.monetRedDark,
        "monetRedLight" to R.color.monetRedLight,
        "monetRedCall" to R.color.colorCallRed,
        "monetGreenCall" to R.color.colorCallGreen
    )

    fun getColor(color: String): Int {
        return try {
            val id = ids.find { it.first == color }?.second ?: 0
            ApplicationLoader.applicationContext.getColor(id)
        } catch (e: Exception) {
            Log.e("Theme", "Error loading color $color")
            e.printStackTrace()
            0
        }
    }

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
            if (ACTION_OVERLAY_CHANGED == intent.action) {
                if (Theme.getActiveTheme().isMonet) {
                    Theme.applyTheme(Theme.getActiveTheme())
                }
            }
        }
    }

    private const val ACTION_OVERLAY_CHANGED = "android.intent.action.OVERLAY_CHANGED"
    private val overlayChangeReceiver = OverlayChangeReceiver()

    fun registerReceiver(context: Context) {
        overlayChangeReceiver.register(context)
    }

    fun unregisterReceiver(context: Context) {
        try {
            overlayChangeReceiver.unregister(context)
        } catch (e: IllegalArgumentException) {
            OctoLogging.e(e)
        }
    }
}
