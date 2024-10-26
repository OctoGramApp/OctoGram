/*
 * This is the source code of OctoGram for Android v.2.0.x
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

package it.octogram.android.utils;

import android.app.Activity;
import android.app.Dialog;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;

import it.octogram.android.PromptBeforeSendMedia;

public class PopupPromptUtils {
    public static Dialog createAlertDialog(Activity parentActivity, PromptBeforeSendMedia media, OnPromptConfirmed callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
        builder.setTitle(LocaleController.getString(R.string.Warning));
        builder.setMessage(LocaleController.getString(getString(media)));
        builder.setPositiveButton(LocaleController.getString(R.string.PromptBeforeSendingConfirm), (dialog1, which1) -> callback.onPromptConfirmed());
        builder.setNeutralButton(LocaleController.getString(R.string.Cancel), null);

        AlertDialog dialog = builder.create();
        dialog.show();

        return builder.create();
    }

    private static int getString(PromptBeforeSendMedia media) {
        if (media.getId() == PromptBeforeSendMedia.STICKERS.getId()) {
            return R.string.PromptBeforeSendingSticker;
        } else {
            return R.string.PromptBeforeSendingGIF;
        }
    }

    public interface OnPromptConfirmed {
        void onPromptConfirmed();
    }
}
