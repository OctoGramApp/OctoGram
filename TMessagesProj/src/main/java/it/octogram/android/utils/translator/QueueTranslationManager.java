/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

package it.octogram.android.utils.translator;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.TranslateController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

import it.octogram.android.OctoConfig;
import it.octogram.android.TranslatorProvider;

public class QueueTranslationManager {
    public TLRPC.InputPeer peer;
    public TranslateController.PendingTranslation translations;
    public ArrayList<Integer> translatedMessageIds = new ArrayList<>();

    public void initQueueTranslation(OnQueueTranslationResultCallback callback) {
        if (OctoConfig.INSTANCE.translatorProvider.getValue() == TranslatorProvider.DEFAULT.getValue()) {
            TLRPC.TL_messages_translateText req = new TLRPC.TL_messages_translateText();
            req.flags |= 1;
            req.peer = peer;
            req.id = translations.messageIds;
            req.to_lang = translations.language;

            int reqId = ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> callback.onTelegramUniqueResult(res, err)));
            callback.onGotReqId(reqId);
            return;
        }

        translateUsingSingleWay(callback);
    }

    private void translateUsingSingleWay(OnQueueTranslationResultCallback callback) {
        for (int i = 0; i < translations.messageIds.size(); i++) {
            int singleMessageId = translations.messageIds.get(i);
            if (translatedMessageIds.contains(singleMessageId)) {
                continue;
            }

            translatedMessageIds.add(singleMessageId);
            TLRPC.TL_textWithEntities messageContent = translations.messageTexts.get(i);
            int finalI = i;
            TranslationsWrapper.translate(UserConfig.selectedAccount, peer, singleMessageId, translations.language, messageContent.text, messageContent.entities, new SingleTranslationManager.OnTranslationResultCallback() {
                @Override
                public void onGotReqId(int reqId) {
                    // ignored: request can't be done using telegram so reqid doesn't exist
                }

                @Override
                public void onResponseReceived() {

                }

                @Override
                public void onSuccess(TLRPC.TL_textWithEntities finalText) {
                    AndroidUtilities.runOnUIThread(() -> callback.onSingleMessageTranslation(finalI, finalText));
                    translateUsingSingleWay(callback); // proceed with queue
                }

                @Override
                public void onError() {
                    callback.onGeneralError();
                }

                @Override
                public void onUnavailableLanguage() {
                    callback.onUnavailableLanguage();
                }
            });
            break;
        }
    }

    public interface OnQueueTranslationResultCallback {
        // translation via telegram
        void onGotReqId(int reqId);
        void onTelegramUniqueResult(TLObject res, TLRPC.TL_error err);

        // translation via other provider
        void onSingleMessageTranslation(int id, TLRPC.TL_textWithEntities result);
        void onGeneralError();
        void onUnavailableLanguage();
    }
}
