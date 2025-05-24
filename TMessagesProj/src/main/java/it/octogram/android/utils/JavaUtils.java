/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */
package it.octogram.android.utils;

import android.os.Build;

import java.util.List;

public class JavaUtils {
    public static <T> void addFirst(List<T> list, T element) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            list.addFirst(element);
        } else {
            list.add(0, element);
        }
    }

    public static <T> T removeFirst(List<T> list) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            return list.removeFirst();
        }
        return list.remove(0);
    }

    public static <T> T getFirst(List<T> list) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            return list.getFirst();
        }
        return list.get(0);
    }
}
