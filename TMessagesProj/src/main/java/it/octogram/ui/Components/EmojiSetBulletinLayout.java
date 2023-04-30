package it.octogram.ui.Components;

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
