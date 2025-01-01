/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.nativeloader

import android.content.Context
import com.getkeepsafe.relinker.ReLinker
import it.octogram.android.logs.OctoLogging
import org.telegram.messenger.NativeLoader

object CustomNativeLoader {
    private var LIB_NAME = "octo.${NativeLoader.LIB_VERSION}"

    @Volatile
    private var nativeLoaded = false

    @Synchronized
    @JvmStatic
    fun initNativeLibs(context: Context): Boolean {
        try {
            ReLinker.loadLibrary(context, LIB_NAME)
            nativeLoaded = true
            return true
        } catch (e: Error) {
            OctoLogging.e(e)
            return false
        }
    }

    fun loaded(): Boolean {
        return nativeLoaded
    }
}
