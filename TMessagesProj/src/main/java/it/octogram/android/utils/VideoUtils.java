/*
 * This is the source code of OwlGram for Android v. 2.8.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Laky64, 2021-2023.
 */
package it.octogram.android.utils;


import it.octogram.android.VideoQuality;

public class VideoUtils {
    public static float getMaxSize(int w, int h, int selectedCompression) {
        float ratio = (float) w / (float) h;
        return switch (VideoQuality.Companion.fromInt(selectedCompression)) {
            case SD -> getNewSide(480, ratio);
            case HD -> getNewSide(720, ratio);
            case FHD -> getNewSide(1080, ratio);
            case QHD -> getNewSide(1440, ratio);
            case UHD -> getNewSide(2160, ratio);
            default -> getNewSide(270, ratio);
        };
    }

    public static int getCompressionsCount(int width, int height) {
        int count = getCompressionsCount(width * height).ordinal() + 1;
        int oldSize = determineSize(width, height, count - 1, count);
        int newSize = determineSize(width, height, count, count + 1);
        if (newSize > oldSize) {
            count++;
        }
        return Math.min(count, VideoQuality.MAX.getValue());
    }

    private static int determineSize(int originalWidth, int originalHeight, int selectedCompression, int compressionsCount) {
        float maxSize = getMaxSize(originalWidth, originalHeight, selectedCompression);
        float scale = originalWidth > originalHeight ? maxSize / originalWidth : maxSize / originalHeight;
        if (selectedCompression == compressionsCount - 1 && scale >= 1f) {
            return originalWidth;
        } else {
            return Math.round(originalWidth * scale / 2) * 2;
        }
    }

    private static VideoQuality getCompressionsCount(int area) {
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
        if (ratio > 1) {
            return side * ratio;
        } else {
            return side / ratio;
        }
    }
}
