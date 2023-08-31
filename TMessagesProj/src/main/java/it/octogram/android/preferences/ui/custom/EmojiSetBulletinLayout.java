/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */

package it.octogram.android.preferences.ui.custom;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.Bulletin;

import it.octogram.android.CustomEmojiController;

@SuppressLint("ViewConstructor")
public class EmojiSetBulletinLayout extends Bulletin.TwoLineLayout {
    public EmojiSetBulletinLayout(@NonNull Context context, String title, String description, CustomEmojiController.EmojiPackBase data, Theme.ResourcesProvider resourcesProvider) {
        super(context, resourcesProvider);
        titleTextView.setText(title);
        subtitleTextView.setText(description);
        imageView.setImage(data.getPreview(), null, null);
    }
}
