/*
 * This is the source code of OwlGram for Android v. 2.8.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Laky64, 2021-2023.
 */
package it.octogram.android.utils;


public class VideoUtils {
    public static float getMaxSize(int w, int h, int selectedCompression) {
        float ratio = (float) w / (float) h;
        switch (Quality.fromInt(selectedCompression)) {
            case SD:
                return getNewSide(480, ratio);
            case HD:
                return getNewSide(720, ratio);
            case FULL_HD:
                return getNewSide(1080, ratio);
            case QUAD_HD:
                return getNewSide(1440, ratio);
            case ULTRA_HD:
                return getNewSide(2160, ratio);
            default:
                return getNewSide(270, ratio);
        }
    }

    public static int getCompressionsCount(int width, int height) {
        int count = getCompressionsCount(width * height).ordinal() + 1;
        int oldSize = determineSize(width, height, count - 1, count);
        int newSize = determineSize(width, height, count, count + 1);
        if (newSize > oldSize) {
            count++;
        }
        return Math.min(count, Quality.MAX.ordinal());
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

    private static Quality getCompressionsCount(int area) {
        if (area >= 3840 * 2160) {
            return Quality.ULTRA_HD;
        } else if (area >= 2560 * 1440) {
            return Quality.QUAD_HD;
        } else if (area >= 1920 * 1080) {
            return Quality.FULL_HD;
        } else if (area >= 1280 * 720) {
            return Quality.HD;
        } else if (area >= 854 * 480) {
            return Quality.SD;
        } else {
            return Quality.UNKNOWN;
        }
    }

    private static float getNewSide(int side, float ratio) {
        if (ratio > 1) {
            return side * ratio;
        } else {
            return side / ratio;
        }
    }

    enum Quality {
        UNKNOWN,
        SD,
        HD,
        FULL_HD,
        QUAD_HD,
        ULTRA_HD,
        MAX;

        static Quality fromInt(int i) {
            return values()[i];
        }
    }
}
