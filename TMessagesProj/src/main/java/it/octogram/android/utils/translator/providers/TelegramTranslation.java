/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.translator.providers;

import static org.telegram.ui.Components.TranslateAlert2.preprocess;

import org.telegram.messenger.MessageObject;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

import it.octogram.android.utils.translator.SingleTranslationsHandler;

public class TelegramTranslation implements BaseTranslator {
    public static final TelegramTranslation INSTANCE = new TelegramTranslation();

    @Override
    public String getName() {
        return "Telegram";
    }

    @Override
    public int getMaxPoolState() {
        return 4;
    }

    @Override
    public void executeTranslation(MessageObject object, TLRPC.InputPeer peer, int msgId, String text, ArrayList<TLRPC.MessageEntity> entities, String toLanguage, SingleTranslationsHandler.OnTranslationResultCallback callback) {
        TLRPC.TL_messages_translateText req = new TLRPC.TL_messages_translateText();
        TLRPC.TL_textWithEntities textWithEntities = new TLRPC.TL_textWithEntities();
        textWithEntities.text = text == null ? "" : text.toString();
        if (entities != null) {
            textWithEntities.entities = entities;
        }
        if (peer != null) {
            req.flags |= 1;
            req.peer = peer;
            req.id.add(msgId);
        } else {
            req.flags |= 2;
            req.text.add(textWithEntities);
        }
        String lang = toLanguage;
        if (lang != null) {
            lang = lang.split("_")[0];
        }
        if ("nb".equals(lang)) {
            lang = "no";
        }
        req.to_lang = lang;

        int reqId = ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(req, (res, err) -> {
            if (res instanceof TLRPC.TL_messages_translateResult &&
                    !((TLRPC.TL_messages_translateResult) res).result.isEmpty() &&
                    ((TLRPC.TL_messages_translateResult) res).result.get(0) != null &&
                    ((TLRPC.TL_messages_translateResult) res).result.get(0).text != null
            ) {
                callback.onResponseReceived();
                callback.onSuccess(preprocess(textWithEntities, ((TLRPC.TL_messages_translateResult) res).result.get(0)));
            } else {
                if (err != null && "TRANSLATIONS_DISABLED_ALT".equalsIgnoreCase(err.text)) {
                    GoogleTranslator.INSTANCE.executeTranslation(object, peer, msgId, text, entities, toLanguage, callback);
                    return;
                }

                callback.onResponseReceived();
                callback.onError();
            }
        });

        callback.onGotReqId(reqId);
    }

    @Override
    public boolean isUnsupportedLanguage(String currentLanguage) {
        return false;
    }
}
