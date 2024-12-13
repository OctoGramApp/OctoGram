/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

package it.octogram.android.crashlytics

import androidx.annotation.IntDef

@IntDef(CrashOption.OPEN_LOG, CrashOption.SEND_LOG, CrashOption.COPY_CRASH_LINE, CrashOption.OPEN_REPORT_URL)
@Retention(AnnotationRetention.SOURCE)
annotation class CrashOption {
    companion object {
        const val OPEN_LOG = 0
        const val SEND_LOG = 1
        const val COPY_CRASH_LINE = 2
        const val OPEN_REPORT_URL = 3

        fun fromValue(value: Int): Int {
            return when (value) {
                OPEN_LOG, SEND_LOG, COPY_CRASH_LINE, OPEN_REPORT_URL -> value
                else -> throw IllegalArgumentException("Invalid option value: $value")
            }
        }
    }
}