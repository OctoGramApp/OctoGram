/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.translator;

import android.os.Handler;
import android.os.Looper;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.TranslateController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import it.octogram.android.OctoConfig;
import it.octogram.android.TranslatorProvider;
import it.octogram.android.utils.OctoLogging;

public class QueueTranslationsHandler {
    private static final String TAG = "QueueTranslationsHandler";

    public TLRPC.InputPeer peer;
    public TranslateController.PendingTranslation translations;
    public ArrayList<Integer> translatedMessageIds = new ArrayList<>();

    private static final ArrayList<Integer> reqIds = new ArrayList<>();
    private static final AtomicBoolean isDestroyed = new AtomicBoolean(false);
    private Thread thread;

    public void initQueueTranslation(OnQueueTranslationResultCallback callback) {
        if (OctoConfig.INSTANCE.translatorOptimizedWay.getValue()) {
            translateUsingAtomicThreadWay(callback, OctoConfig.INSTANCE.translatorProvider.getValue() == TranslatorProvider.DEFAULT.getValue());
            return;
        }

        if (OctoConfig.INSTANCE.translatorProvider.getValue() == TranslatorProvider.DEFAULT.getValue()) {
            TLRPC.TL_messages_translateText req = new TLRPC.TL_messages_translateText();
            req.flags |= 1;
            req.peer = peer;
            req.id = translations.messageIds;
            req.to_lang = translations.language;

            int reqId = ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(req, (res, err) -> AndroidUtilities.runOnUIThread(() -> callback.onTelegramUniqueResult(res, err)));
            reqIds.add(reqId);
            return;
        }

        translateUsingSingleWay(callback);
    }

    private void translateUsingSingleWay(OnQueueTranslationResultCallback callback) {
        for (int i = 0; i < translations.messageIds.size(); i++) {
            if (isDestroyed.get()) {
                break;
            }

            int singleMessageId = translations.messageIds.get(i);
            if (translatedMessageIds.contains(singleMessageId)) {
                continue;
            }

            translatedMessageIds.add(singleMessageId);
            TLRPC.TL_textWithEntities messageContent = translations.messageTexts.get(i);
            int finalI = i;
            MainTranslationsHandler.translate(UserConfig.selectedAccount, peer, singleMessageId, translations.language, messageContent.text, messageContent.entities, new SingleTranslationsHandler.OnTranslationResultCallback() {
                @Override
                public boolean isFromQueue() {
                    return true;
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

                @Override
                public void onExtensionNeedInstall() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void onExtensionNeedUpdate() {
                    throw new UnsupportedOperationException();
                }
            });
            break;
        }
    }

    private void translateUsingAtomicThreadWay(OnQueueTranslationResultCallback callback, boolean traditionalTelegramWay) {
        AtomicInteger failedTranslations = new AtomicInteger(0);
        ExecutorService transPool = Executors.newFixedThreadPool(traditionalTelegramWay ? 3 : MainTranslationsHandler.getMaxExecutionPoolSize());
        ArrayList<Future<?>> futures = new ArrayList<>();

        // divide translations.messageIds into chunks based on size/2, if size > 15 then size / 3
        ArrayList<ArrayList<Integer>> chunks = new ArrayList<>();
        if (traditionalTelegramWay) {
            int chunkSize = translations.messageIds.size() > 15 ? translations.messageIds.size() / 3 : translations.messageIds.size() / 2;
            if (chunkSize < 1) {
                chunkSize = 1;
            }

            for (int i = 0; i < translations.messageIds.size(); i += chunkSize) {
                ArrayList<Integer> chunk = new ArrayList<>();
                for (int j = i; j < i + chunkSize && j < translations.messageIds.size(); j++) {
                    chunk.add(translations.messageIds.get(j));
                }
                chunks.add(chunk);
            }
        }


        thread = new Thread(() -> {
            for (int i = 0; i < (traditionalTelegramWay ? chunks.size() : translations.messageIds.size()); i++) {
                final int finalI = i;
                Future<?> future = transPool.submit(() -> {
                    if (isDestroyed.get()) {
                        return;
                    }

                    ArticleTranslationsHandler.TranslationCompletable completable = new ArticleTranslationsHandler.TranslationCompletable();

                    if (traditionalTelegramWay) {
                        ArrayList<Integer> chunk = chunks.get(finalI);

                        TLRPC.TL_messages_translateText req = new TLRPC.TL_messages_translateText();
                        req.flags |= 1;
                        req.peer = peer;
                        req.id = chunk;
                        req.to_lang = translations.language;

                        int reqId = ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(req, (res, err) -> {
                            new Handler(Looper.getMainLooper()).post(() -> callback.onTelegramUniqueResult(res, err));
                            completable.complete();
                        });
                        reqIds.add(reqId);
                    } else {
                        int singleMessageId = translations.messageIds.get(finalI);
                        if (translatedMessageIds.contains(singleMessageId)) {
                            return;
                        }

                        translatedMessageIds.add(singleMessageId);

                        TLRPC.TL_textWithEntities messageContent = translations.messageTexts.get(finalI);
                        MainTranslationsHandler.translate(UserConfig.selectedAccount, peer, singleMessageId, translations.language, messageContent.text, messageContent.entities, new SingleTranslationsHandler.OnTranslationResultCallback() {
                            @Override
                            public boolean isFromQueue() {
                                return true;
                            }

                            @Override
                            public void onSuccess(TLRPC.TL_textWithEntities finalText) {
                                new Handler(Looper.getMainLooper()).post(() -> callback.onSingleMessageTranslation(finalI, finalText));
                                completable.complete();
                            }

                            @Override
                            public void onError() {
                                callback.onGeneralError();
                                completable.completeExceptionally();
                            }

                            @Override
                            public void onUnavailableLanguage() {
                                callback.onGeneralError();
                                completable.completeExceptionally();
                            }

                            @Override
                            public void onExtensionNeedInstall() {
                                throw new UnsupportedOperationException();
                            }

                            @Override
                            public void onExtensionNeedUpdate() {
                                throw new UnsupportedOperationException();
                            }
                        });
                    }

                    try {
                        completable.get();
                    } catch (Exception e) {
                        failedTranslations.incrementAndGet();
                    }
                });
                futures.add(future);
            }

            for (Future<?> f : futures) {
                if (isDestroyed.get()) {
                    break;
                }

                try {
                    f.get();
                } catch (Exception e) {
                    OctoLogging.e(TAG, "Future failed", e);
                }
            }

            transPool.shutdown();
        });
        thread.start();
    }

    public void destroyInstance() {
        for (int reqId : reqIds) {
            ConnectionsManager.getInstance(UserConfig.selectedAccount).cancelRequest(reqId, true);
        }

        isDestroyed.set(true);
        reqIds.clear();
        thread.interrupt();
    }

    public interface OnQueueTranslationResultCallback {
        // translation via telegram

        void onTelegramUniqueResult(TLObject res, TLRPC.TL_error err);

        // translation via other provider
        void onSingleMessageTranslation(int id, TLRPC.TL_textWithEntities result);

        void onGeneralError();

        void onUnavailableLanguage();
    }
}
