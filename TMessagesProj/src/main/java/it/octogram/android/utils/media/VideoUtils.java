/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */
package it.octogram.android.utils.media;


import android.annotation.SuppressLint;

import it.octogram.android.VideoQuality;

public class VideoUtils {
    @SuppressLint("SwitchIntDef")
    public static float getMaxSize(int w, int h, @VideoQuality.Quality int selectedCompression) {
        float ratio = (float) w / (float) h;
        return switch (selectedCompression) {
            case VideoQuality.SD -> getNewSide(480, ratio);
            case VideoQuality.HD -> getNewSide(720, ratio);
            case VideoQuality.FHD -> getNewSide(1080, ratio);
            case VideoQuality.QHD -> getNewSide(1440, ratio);
            case VideoQuality.UHD -> getNewSide(2160, ratio);
            case VideoQuality.MAX -> getNewSide(4096, ratio);
            default -> getNewSide(270, ratio);
        };
    }

    public static int getCompressionsCount(int width, int height) {
        var count = getCompressionsCount(width * height) + 1;
        var oldSize = determineSize(width, height, count - 1, count);
        var newSize = determineSize(width, height, count, count + 1);
        if (newSize > oldSize) {
            count++;
        }
        return Math.min(count, VideoQuality.MAX);
    }

    private static int determineSize(int originalWidth, int originalHeight, int selectedCompression, int compressionsCount) {
        float maxSize = getMaxSize(originalWidth, originalHeight, selectedCompression);
        float scale = originalWidth > originalHeight ? maxSize / originalWidth : maxSize / originalHeight;
        return (selectedCompression == compressionsCount - 1 && scale >= 1f) ? originalWidth : Math.round(originalWidth * scale / 2) * 2;
    }

    private static @VideoQuality.Quality int getCompressionsCount(int area) {
        if (area >= 3840 * 2160) {
            return VideoQuality.UHD;
        } else if (area >= 2560 * 1440) {
            return VideoQuality.QHD;
        } else if (area >= 1920 * 1080) {
            return VideoQuality.FHD;
        } else if (area >= 1280 * 720) {
            return VideoQuality.HD;
        } else if (area >= 854 * 480) {
            return VideoQuality.SD;
        } else {
            return VideoQuality.UNKNOWN;
        }
    }

    private static float getNewSide(int side, float ratio) {
        return (ratio > 1) ? side * ratio : side / ratio;
    }
}
