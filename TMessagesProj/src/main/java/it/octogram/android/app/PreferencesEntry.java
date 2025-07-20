/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import it.octogram.android.app.fragment.PreferencesFragment;

public interface PreferencesEntry {
    @NonNull
    OctoPreferences getPreferences(@NonNull PreferencesFragment fragment, @NonNull Context context);

    default void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    }

    default boolean isLockedContent() {
        return false;
    }

    default boolean canBeginSlide() {
        return true;
    }

    default void onSaveButtonPressed() {

    }

    default void onTransitionAnimationEnd(boolean isOpen, boolean backward) {

    }

    default void onBecomeFullyVisible() {

    }

    default void onBecomeFullyHidden() {
        
    }

}
