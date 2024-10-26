/*
 * This is the source code of OwlGram for Android v. 2.8.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Laky64, 2021-2023.
 */
package it.octogram.android.media

import android.media.AudioRecord
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import it.octogram.android.OctoConfig

/**
 * This object provides methods to manage audio enhancements, including:
 * - Automatic Gain Control (AGC)
 * - Noise Suppression
 * - Acoustic Echo Cancellation (AEC)
 *
 * It is responsible for initializing and releasing these enhancements based on the configuration
 * and device capabilities.
 *
 * Usage:
 * 1. Call `initVoiceEnhancements(audioRecord)` to initialize the enhancements for a given `AudioRecord` instance.
 * 2. Call `releaseVoiceEnhancements()` to release the resources held by the enhancements when they are no longer needed.
 * 3. Use `isAvailable()` to check if any of the supported audio effects are available on the device.
 *
 * Note: The enhancements are enabled only if they are available on the device and enabled in the configuration (OctoConfig).
 */
object AudioEnhance {
    private var automaticGainControl: AutomaticGainControl? = null
    private var noiseSuppressor: NoiseSuppressor? = null
    private var acousticEchoCanceler: AcousticEchoCanceler? = null

    /**
     * Initializes voice enhancements for the given audio record.
     *
     * This function checks if voice enhancements are enabled in the OctoConfig and
     * then attempts to enable Automatic Gain Control, Noise Suppression, and Acoustic Echo Cancellation
     * if they are available on the device.
     *
     * @param audioRecord The AudioRecord instance to apply voice enhancements to.
     */
    @JvmStatic
    fun initVoiceEnhancements(audioRecord: AudioRecord) {
        if (!OctoConfig.INSTANCE.activeNoiseSuppression.getValue()) return

        if (AutomaticGainControl.isAvailable()) {
            automaticGainControl = AutomaticGainControl.create(audioRecord.audioSessionId)
            automaticGainControl?.enabled = true
        }

        if (NoiseSuppressor.isAvailable()) {
            noiseSuppressor = NoiseSuppressor.create(audioRecord.audioSessionId)
            noiseSuppressor?.enabled = true
        }

        if (AcousticEchoCanceler.isAvailable()) {
            acousticEchoCanceler = AcousticEchoCanceler.create(audioRecord.audioSessionId)
            acousticEchoCanceler?.enabled = true
        }
    }

    /**
     * Releases the resources held by the voice enhancement components:
     * - Automatic Gain Control (AGC)
     * - Noise Suppressor
     * - Acoustic Echo Canceler (AEC)
     *
     * This function should be called when the voice enhancements are no longer needed
     * to free up system resources. After calling this function, the voice enhancement
     * components will be unavailable until they are re-initialized.
     */
    @JvmStatic
    fun releaseVoiceEnhancements() {
        automaticGainControl?.release()
        automaticGainControl = null

        noiseSuppressor?.release()
        noiseSuppressor = null

        acousticEchoCanceler?.release()
        acousticEchoCanceler = null
    }

    /**
     * Checks if any of the supported audio effects (Automatic Gain Control, Noise Suppressor, Acoustic Echo Canceler)
     * are available on the device.
     *
     * @return `true` if at least one of the audio effects is available, `false` otherwise.
     */
    @JvmStatic
    fun isAvailable(): Boolean {
        return AutomaticGainControl.isAvailable() || NoiseSuppressor.isAvailable() || AcousticEchoCanceler.isAvailable()
    }
}
