/*
 * This is the source code of OwlGram for Android v. 2.8.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Laky64, 2021-2023.
 */
package it.octogram.android.media;

import android.media.AudioRecord;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;

import it.octogram.android.OctoConfig;

public class AudioEnhance {
    private static AutomaticGainControl automaticGainControl = null;
    private static NoiseSuppressor noiseSuppressor = null;
    private static AcousticEchoCanceler acousticEchoCanceler = null;

    public static void initVoiceEnhancements(AudioRecord audioRecord) {
        if (!OctoConfig.INSTANCE.voiceAgc.getValue()) return;
        if (AutomaticGainControl.isAvailable()) {
            automaticGainControl = AutomaticGainControl.create(audioRecord.getAudioSessionId());
            automaticGainControl.setEnabled(true);
        }
        if (NoiseSuppressor.isAvailable()) {
            noiseSuppressor = NoiseSuppressor.create(audioRecord.getAudioSessionId());
            noiseSuppressor.setEnabled(true);
        }
        if (AcousticEchoCanceler.isAvailable()) {
            acousticEchoCanceler = AcousticEchoCanceler.create(audioRecord.getAudioSessionId());
            acousticEchoCanceler.setEnabled(true);
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
        return AutomaticGainControl.isAvailable() || NoiseSuppressor.isAvailable() || AcousticEchoCanceler.isAvailable();
    }
}
