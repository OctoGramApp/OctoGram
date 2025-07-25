/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.translator.providers;

import org.telegram.messenger.MessageObject;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

import it.octogram.android.utils.translator.SingleTranslationsHandler;

public interface BaseTranslator {
    String getName();
    int getMaxExecutionPoolSize();
    default void executeTranslation(MessageObject object, TLRPC.InputPeer peer, int msgId, String text, ArrayList<TLRPC.MessageEntity> entities, String toLanguage, SingleTranslationsHandler.OnTranslationResultCallback callback) {
        executeTranslation(text, entities, toLanguage, callback);
    }
    default void executeTranslation(String text, ArrayList<TLRPC.MessageEntity> entities, String toLanguage, SingleTranslationsHandler.OnTranslationResultCallback callback) {

    }
    boolean isUnsupportedLanguage(String currentLanguage);
}
