/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.app.ui.cells;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.Bulletin;

import it.octogram.android.CustomEmojiController;

@SuppressLint("ViewConstructor")
public class EmojiSetBulletinLayout extends Bulletin.TwoLineLayout {
    public EmojiSetBulletinLayout(@NonNull Context context, CharSequence title, String description, CustomEmojiController.EmojiPackBase data, Theme.ResourcesProvider resourcesProvider) {
        super(context, resourcesProvider);
        titleTextView.setText(title);
        subtitleTextView.setText(description);
        imageView.setImage(data.getPreview(), null, null);
    }
}
