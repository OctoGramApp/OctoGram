/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

package it.octogram.android.utils;

import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;

import androidx.core.content.ContextCompat;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.TranslateController;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.Components.ColoredImageSpan;

import java.util.Locale;
import java.util.Objects;

import it.octogram.android.OctoConfig;

public class MessageResourceHelper {

    public static SpannableStringBuilder editedSpan;
    public static Drawable editedDrawable;

    public static SpannableStringBuilder translatedSpan;
    public static Drawable translatedDrawable;

    public static CharSequence createEditedString(MessageObject messageObject, boolean edited) {
        initItems();

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(' ');

        if (edited) {
            builder.append(OctoConfig.INSTANCE.pencilIconForEditedMessages.getValue() ? editedSpan : LocaleController.getString("EditedMessage", R.string.EditedMessage));
        }

        if (canShowTranslatedItem(messageObject)) {
            if (edited) {
                builder.append(' ');
            }

            if (canUseExtendedTranslatedView(messageObject)) {
                builder.append(Locale.forLanguageTag(messageObject.messageOwner.originalLanguage).getDisplayName());
                builder.append(' ');
                builder.append(translatedSpan);
                builder.append(' ');
                builder.append(Locale.forLanguageTag(messageObject.messageOwner.translatedToLanguage).getDisplayName());
            } else {
                builder.append(LocaleController.getString("TranslatorInMessageBadge", R.string.TranslatorInMessageBadge));
            }
        }

        builder.append(' ');
        builder.append(LocaleController.getInstance().formatterDay.format((long) (messageObject.messageOwner.date) * 1000));

        return builder;
    }

    public static float getGeneralIntrinsicWidth(MessageObject messageObject, boolean edited) {
        initItems();

        float width = 0;

        if (edited && OctoConfig.INSTANCE.pencilIconForEditedMessages.getValue()) {
            width = editedDrawable.getIntrinsicWidth();
        }

        if (canUseExtendedTranslatedView(messageObject)) {
            width += translatedDrawable.getIntrinsicWidth();
        }

        return width;
    }

    private static boolean canShowTranslatedItem(MessageObject messageObject) {
        if (messageObject.translated) {
            TranslateController controller = MessagesController.getInstance(UserConfig.selectedAccount).getTranslateController();
            return controller.isManualTranslated(messageObject) && !controller.isTranslatingDialog(messageObject.getDialogId());
        }

        return false;
    }

    private static boolean canUseExtendedTranslatedView(MessageObject messageObject) {
        if (!canShowTranslatedItem(messageObject)) {
            return false;
        }

        if (messageObject.messageOwner.originalLanguage == null || messageObject.messageOwner.translatedToLanguage == null) {
            return false;
        }

        return !Objects.equals(messageObject.messageOwner.originalLanguage, TranslateController.UNKNOWN_LANGUAGE);
    }

    private static void initItems() {
        if (editedDrawable == null) {
            editedDrawable = Objects.requireNonNull(ContextCompat.getDrawable(ApplicationLoader.applicationContext, R.drawable.msg_edited)).mutate();
        }

        if (editedSpan == null) {
            editedSpan = new SpannableStringBuilder("\u200B");
            editedSpan.setSpan(new ColoredImageSpan(editedDrawable), 0, 1, 0);
        }

        if (translatedDrawable == null) {
            translatedDrawable = Objects.requireNonNull(ContextCompat.getDrawable(ApplicationLoader.applicationContext, R.drawable.search_arrow)).mutate();
        }

        if (translatedSpan == null) {
            translatedSpan = new SpannableStringBuilder("\u200B");
            translatedSpan.setSpan(new ColoredImageSpan(translatedDrawable), 0, 1, 0);
        }
    }
}
