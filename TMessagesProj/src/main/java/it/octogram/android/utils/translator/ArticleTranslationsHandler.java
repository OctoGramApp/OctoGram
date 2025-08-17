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
import android.view.View;

import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ArticleViewer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import it.octogram.android.utils.OctoLogging;

public class ArticleTranslationsHandler {
    private static final String TAG = "ArticleTranslationsHandler";
    private static final HashMap<String, String> tempTranslations = new HashMap<>();

    public static void initArticleTranslation(String toLanguage, ArticleViewer.PageLayout pageLayout, ArticleTranslationsWrapper wrapper) {
        ArrayList<Object> textBlocks = pageLayout.adapter.textBlocks;

        boolean hasNext;
        do {
            hasNext = false;
            for (Object text : new HashSet<>(textBlocks)) {
                if (text instanceof TLRPC.TL_textConcat c2) {
                    textBlocks.remove(text);
                    textBlocks.addAll(c2.texts);
                    hasNext = true;
                }
            }
        } while (hasNext);

        AtomicInteger translatedItems = new AtomicInteger(0);
        AtomicInteger failedTranslations = new AtomicInteger(0);
        HashMap<Object, TLRPC.PageBlock> textToBlocks = pageLayout.adapter.textToBlocks;
        ExecutorService transPool = Executors.newFixedThreadPool(MainTranslationsHandler.getMaxExecutionPoolSize());
        ArrayList<Future<?>> futures = new ArrayList<>();

        new Thread(() -> {
            for (Object text : textBlocks) {
                String finalText;
                if (text instanceof TLRPC.RichText v2) {
                    TLRPC.PageBlock block;
                    if (textToBlocks.containsKey(text) && textToBlocks.get(text) != null) {
                        block = textToBlocks.get(text);
                    } else {
                        block = textToBlocks.get(v2.parentRichText);
                    }
                    finalText = wrapper.getText(pageLayout.adapter, null, v2, v2, block, 1000).toString();
                } else if (text instanceof String v2) {
                    finalText = v2;
                } else {
                    finalText = null;
                }

                if (finalText != null) {
                    Future<?> future = transPool.submit(() -> {
                        if (tempTranslations.containsKey(finalText.trim())) {
                            return;
                        }

                        TranslationCompletable completable = new TranslationCompletable();
                        MainTranslationsHandler.translate(0, toLanguage, finalText, new SingleTranslationsHandler.OnTranslationResultCallback() {
                            @Override
                            public boolean isFromQueue() {
                                return true;
                            }

                            @Override
                            public void onSuccess(TLRPC.TL_textWithEntities translatedText) {
                                String newText = translatedText.text;
                                if (finalText.startsWith(" ")) {
                                    newText = " " + newText;
                                }
                                if (finalText.endsWith(" ")) {
                                    newText = newText + " ";
                                }
                                tempTranslations.put(finalText.trim(), newText);

                                completable.complete();
                            }

                            @Override
                            public void onError() {
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
                        try {
                            completable.get();
                        } catch (Exception e) {
                            failedTranslations.incrementAndGet();
                        }
                    });
                    futures.add(future);
                }
            }

            AtomicInteger translatedItemsCopy = new AtomicInteger(0);

            for (Future<?> f : futures) {
                try {
                    f.get();
                } catch (Exception e) {
                    OctoLogging.e(TAG, "Future failed", e);
                }
                new Handler(Looper.getMainLooper()).post(() -> {
                    wrapper.setProgress((float) translatedItemsCopy.incrementAndGet() / futures.size());
                    if (translatedItemsCopy.get() % 10 == 0) {
                        wrapper.updateUI(false);
                    }
                });
            }

            transPool.shutdown();
            new Handler(Looper.getMainLooper()).post(() -> wrapper.updateUI(true));

            if (failedTranslations.get() > 2) {
                new Handler(Looper.getMainLooper()).post(() -> wrapper.onPartiallyTranslationsFailed((int) (((float) failedTranslations.get() / futures.size()) * 100)));
            }
        }).start();
    }

    public static String getTranslatedText(String originalText) {
        return tempTranslations.get(originalText.trim());
    }

    public static void clearTranslations() {
        tempTranslations.clear();
    }

    public static class TranslationCompletable {
        private final Object lock = new Object();
        private boolean isCompleted = false;
        private boolean asError = false;

        public void complete() {
            synchronized (lock) {
                if (isCompleted) return;
                isCompleted = true;
                lock.notifyAll();
            }
        }

        public void completeExceptionally() {
            synchronized (lock) {
                if (isCompleted) return;
                isCompleted = true;
                asError = true;
                lock.notifyAll();
            }
        }

        public void get() throws Exception {
            synchronized (lock) {
                while (!isCompleted) {
                    lock.wait();
                }
                if (asError) throw new Exception();
            }
        }
    }

    public interface ArticleTranslationsWrapper {
        void setProgress(float progress);
        CharSequence getText(ArticleViewer.WebpageAdapter page, View parentView, TLRPC.RichText parentRichText, TLRPC.RichText richText, TLRPC.PageBlock parentBlock, int maxWidth);
        void updateUI(boolean finished);
        default void onPartiallyTranslationsFailed(int percent) {

        }
    }
}
