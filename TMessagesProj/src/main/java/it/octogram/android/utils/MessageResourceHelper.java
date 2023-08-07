/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023.
 */

package it.octogram.android.utils;

import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import androidx.core.content.ContextCompat;
import it.octogram.android.OctoConfig;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.ui.Components.ColoredImageSpan;

import java.util.Objects;

public class MessageResourceHelper {

    public static SpannableStringBuilder editedSpan;
    public static Drawable editedDrawable;

    public static CharSequence createEditedString(MessageObject messageObject) {
        if (editedDrawable == null) {
            editedDrawable = Objects.requireNonNull(ContextCompat.getDrawable(ApplicationLoader.applicationContext, R.drawable.msg_edited)).mutate();
        }
        if (editedSpan == null) {
            editedSpan = new SpannableStringBuilder("\u200B");
            editedSpan.setSpan(new ColoredImageSpan(editedDrawable), 0, 1, 0);
        }
        return new SpannableStringBuilder()
                .append(' ')
                .append(OctoConfig.INSTANCE.pencilIconForEditedMessages.getValue() ? editedSpan : LocaleController.getString("EditedMessage", R.string.EditedMessage))
                .append(' ')
                .append(LocaleController.getInstance().formatterDay.format((long) (messageObject.messageOwner.date) * 1000));
    }

}
