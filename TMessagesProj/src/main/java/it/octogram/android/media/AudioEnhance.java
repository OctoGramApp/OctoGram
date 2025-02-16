/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */
package it.octogram.android.media;

import android.media.AudioRecord;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;

import androidx.annotation.Nullable;

import it.octogram.android.OctoConfig;


public class AudioEnhance {
    @Nullable
    private static AutomaticGainControl automaticGainControl;
    @Nullable
    private static NoiseSuppressor noiseSuppressor;
    @Nullable
    private static AcousticEchoCanceler acousticEchoCanceler;

    private AudioEnhance() {
    }

    public static void initVoiceEnhancements(AudioRecord audioRecord) {
        if (!OctoConfig.INSTANCE.activeNoiseSuppression.getValue()) {
            return;
        }

        if (AutomaticGainControl.isAvailable()) {
            automaticGainControl = AutomaticGainControl.create(audioRecord.getAudioSessionId());
            if (automaticGainControl != null) {
                automaticGainControl.setEnabled(true);
            }
        }

        if (NoiseSuppressor.isAvailable()) {
            noiseSuppressor = NoiseSuppressor.create(audioRecord.getAudioSessionId());
            if (noiseSuppressor != null) {
                noiseSuppressor.setEnabled(true);
            }
        }

        if (AcousticEchoCanceler.isAvailable()) {
            acousticEchoCanceler = AcousticEchoCanceler.create(audioRecord.getAudioSessionId());
            if (acousticEchoCanceler != null) {
                acousticEchoCanceler.setEnabled(true);
            }
        }
    }

    public static void releaseVoiceEnhancements() {
        if (automaticGainControl != null) {
            automaticGainControl.release();
            automaticGainControl = null;
        }

        if (noiseSuppressor != null) {
            noiseSuppressor.release();
            noiseSuppressor = null;
        }

        if (acousticEchoCanceler != null) {
            acousticEchoCanceler.release();
            acousticEchoCanceler = null;
        }
    }

    public static boolean isAvailable() {
        return AutomaticGainControl.isAvailable()
                || NoiseSuppressor.isAvailable()
                || AcousticEchoCanceler.isAvailable();
    }
}