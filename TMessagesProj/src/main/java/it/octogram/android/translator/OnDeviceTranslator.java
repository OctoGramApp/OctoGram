/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.translator;

import android.text.TextUtils;

import com.google.android.exoplayer2.util.Log;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.TranslateRemoteModel;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LanguageDetector;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class OnDeviceTranslator {
    public static List<String> languagesList = TranslateLanguage.getAllLanguages();

    public static void executeTranslation(MessageObject selectedMessage, String definedToLanguage, String text, SingleTranslationManager.OnTranslationResultCallback callback) {
        AtomicBoolean declaredFailure = new AtomicBoolean(false);
        getOriginalLanguage(selectedMessage, text, new OnGetLanguageInterface() {
            @Override
            public void onLanguageReceived(String definedFromLanguage) {
                if (definedFromLanguage.contains("_")) {
                    definedFromLanguage = definedFromLanguage.split("_")[0];
                }
                if ("nb".equals(definedFromLanguage)) {
                    definedFromLanguage = "no";
                }
                final String language = TranslateLanguage.fromLanguageTag(definedFromLanguage);
                String _tmpDfTo = definedToLanguage;
                if (_tmpDfTo.contains("_")) {
                    _tmpDfTo = _tmpDfTo.split("_")[0];
                }
                if ("nb".equals(_tmpDfTo)) {
                    _tmpDfTo = "no";
                }
                final String toLanguage = TranslateLanguage.fromLanguageTag(_tmpDfTo);

                if (language == null || toLanguage == null) {
                    callback.onUnavailableLanguage();
                    return;
                }

                initIsModelDownloadedState(language, toLanguage, supportedLanguages -> {
                    Log.e("aaa", "supp lang: "+supportedLanguages);
                    if (supportedLanguages.size() != 2) {
                        ArrayList<String> toDownload = new ArrayList<>();
                        if (!supportedLanguages.contains(language)) toDownload.add(language);
                        if (!supportedLanguages.contains(toLanguage)) toDownload.add(toLanguage);

                        if (!toDownload.isEmpty()) {
                            callback.onDownloadingModel(toDownload);
                        }
                    }

                    TranslatorOptions.Builder options = new TranslatorOptions.Builder();
                    options.setSourceLanguage(language);
                    options.setTargetLanguage(toLanguage);
                    final Translator translator = Translation.getClient(options.build());
                    Log.e("aaa", "yuppy dowins from "+language+" to "+toLanguage);

                    translator.downloadModelIfNeeded().addOnSuccessListener(v -> {
                        final String[] lines = text.split("\n");
                        final String[] result = new String[lines.length];

                        Runnable checkIsDone = () -> {
                            if (declaredFailure.get()) {
                                return;
                            }

                            boolean allDone = true;
                            for (String s : result) {
                                if (s == null) {
                                    allDone = false;
                                    break;
                                }
                            }
                            if (allDone) {
                                StringBuilder finalResult = new StringBuilder();
                                for (String line : result) {
                                    if (!TextUtils.isEmpty(line)) {
                                        finalResult.append(line).append("\n");
                                    }
                                }
                                TLRPC.TL_textWithEntities finalText = new TLRPC.TL_textWithEntities();
                                finalText.text = finalResult.toString().trim();
                                callback.onSuccess(finalText);
                            }
                        };

                        for (int i = 0; i < lines.length; i++) {
                            if (declaredFailure.get()) {
                                return;
                            }

                            final int index = i;
                            final String line = lines[i];

                            if (TextUtils.isEmpty(line) || line.replaceAll("[\\t\\n\\r]", "").isEmpty()) {
                                result[i] = line;
                                checkIsDone.run();
                                continue;
                            }

                            translator.translate(lines[i])
                                    .addOnSuccessListener(translatedText -> {
                                        result[index] = translatedText;
                                        checkIsDone.run();
                                    })
                                    .addOnFailureListener(err -> {
                                        declaredFailure.set(true);
                                        callback.onError();
                                    });
                        }
                    });
                });
            }

            @Override
            public void onFailed() {
                callback.onError();
            }
        });
    }

    private static void initIsModelDownloadedState(String fromLang, String toLang, OnModelDownloadedStateResult callback) {
        AtomicInteger state = new AtomicInteger(0);
        ArrayList<String> supportedLanguages = new ArrayList<>();
        Utilities.Callback2<String, Boolean> updatedState = (lang, isDownloaded) -> {
            if (isDownloaded) {
                supportedLanguages.add(lang);
            }
            if (state.incrementAndGet() == 2) {
                AndroidUtilities.runOnUIThread(() -> {
                    if (callback != null) {
                        callback.gotResult(supportedLanguages);
                    }
                });
            }
        };

        for (int i = 0; i < 2; i++) {
            String lang = i == 0 ? fromLang : toLang;
            RemoteModelManager.getInstance().isModelDownloaded(new TranslateRemoteModel.Builder(lang).build())
                    .addOnSuccessListener(res -> updatedState.run(lang, res != null && res))
                    .addOnFailureListener(err -> updatedState.run(lang, false));
        }
    }

    private static void getOriginalLanguage(MessageObject selectedMessage, String text, OnGetLanguageInterface callback) {
        if (selectedMessage != null && selectedMessage.messageOwner != null && selectedMessage.messageOwner.originalLanguage != null) {
            callback.onLanguageReceived(selectedMessage.messageOwner.originalLanguage);
        } else if (LanguageDetector.hasSupport()) {
            LanguageDetector.detectLanguage(text, callback::onLanguageReceived, (Exception e) -> callback.onFailed());
        } else {
            callback.onFailed();
        }
    }

    public static boolean isUnsupportedLanguage(String language) {
        return TextUtils.isEmpty(language) || !languagesList.contains(language);
    }

    private interface OnGetLanguageInterface {
        void onLanguageReceived(String language);
        void onFailed();
    }

    private interface OnModelDownloadedStateResult {
        void gotResult(ArrayList<String> supportedLanguages);
    }
}
