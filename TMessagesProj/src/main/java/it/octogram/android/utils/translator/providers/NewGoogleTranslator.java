/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.translator.providers;

import android.text.TextUtils;

import androidx.core.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.TranslateAlert2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import it.octogram.android.OctoConfig;
import it.octogram.android.http.StandardHTTPRequest;
import it.octogram.android.utils.OctoLogging;
import it.octogram.android.utils.OctoUtils;
import it.octogram.android.utils.translator.HTMLKeeper;
import it.octogram.android.utils.translator.SingleTranslationsHandler;

public class NewGoogleTranslator implements BaseTranslator {
    public static final NewGoogleTranslator INSTANCE = new NewGoogleTranslator();

    private static final String TAG = "NewGoogleTranslator";
    private static final String contentType = "application/json";
    private static final String url = "https://translation.googleapis.com/language/translate/v2";

    private static final List<String> targetLanguages = List.of(
            "sq", "ar", "am", "az", "ga", "et", "or", "eu", "be", "bg", "is", "pl", "bs", "fa",
            "af", "tt", "da", "de", "ru", "fr", "tl", "fi", "fy", "km", "ka", "gu",
            "kk", "ht", "ko", "ha", "nl", "ky", "gl", "ca", "cs", "kn", "co", "hr", "ku",
            "la", "lv", "lo", "lt", "lb", "rw", "ro", "mg", "mt", "mr", "ml", "ms", "mk",
            "mi", "mn", "bn", "my", "hmn", "xh", "zu", "ne", "no", "pa", "pt", "ps", "ny",
            "ja", "sv", "sm", "sr", "st", "si", "eo", "sk", "sl", "sw", "gd", "ceb", "so",
            "tg", "te", "ta", "th", "tr", "tk", "cy", "ug", "ur", "uk", "uz", "es", "iw",
            "el", "haw", "sd", "hu", "sn", "hy", "ig", "it", "yi", "hi", "su", "id", "jw",
            "en", "yo", "vi", "zh-TW", "zh-CN", "zh");

    @Override
    public String getName() {
        return "Google Cloud";
    }

    @Override
    public int getMaxExecutionPoolSize() {
        return 10;
    }

    @Override
    public void executeTranslation(String text, ArrayList<TLRPC.MessageEntity> entities, String toLanguage, SingleTranslationsHandler.OnTranslationResultCallback callback) {
        new Thread() {
            @Override
            public void run() {
                try {
                    TLRPC.TL_textWithEntities originalText = new TLRPC.TL_textWithEntities();
                    originalText.text = text;
                    originalText.entities = entities;

                    String textInHtml = entities == null ? text : HTMLKeeper.entitiesToHtml(text, entities, false);
                    ArrayList<String> parts = OctoUtils.getStringParts(textInHtml, 2500);

                    int numberOfParts = parts.size();
                    CountDownLatch latch = new CountDownLatch(numberOfParts);
                    ArrayList<String> translatedParts = new ArrayList<>(Collections.nCopies(numberOfParts, null));
                    AtomicReference<Exception> errorRef = new AtomicReference<>(null);

                    for (int i = 0; i < numberOfParts; i++) {
                        final int partIndex = i;
                        final String currentPart = parts.get(i);

                        new Thread(() -> {
                            if (errorRef.get() != null) {
                                latch.countDown();
                                return;
                            }
                            try {
                                StandardHTTPRequest request = new StandardHTTPRequest.Builder(url+"?key="+OctoConfig.INSTANCE.googleCloudKeyTranslator.getValue())
                                        .method("POST")
                                        .header("Content-Type", contentType)
                                        .data(composeBody(currentPart, toLanguage))
                                        .build();

                                String response = request.request();

                                if (TextUtils.isEmpty(response)) {
                                    throw new IOException("Empty response from Google translation for part " + partIndex);
                                }

                                Pair<String, String> partResult = parseGoogleResponse(response);
                                translatedParts.set(partIndex, partResult.first);
                            } catch (Exception e) {
                                errorRef.set(e);
                                OctoLogging.e(TAG, e);
                            } finally {
                                latch.countDown();
                            }
                        }).start();
                    }

                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        errorRef.set(e);
                        OctoLogging.e(TAG, e);
                    }

                    callback.onResponseReceived();

                    if (errorRef.get() != null) {
                        callback.onError();
                        return;
                    }

                    StringBuilder finalStringBuilder = new StringBuilder();
                    for (String translatedPart : translatedParts) {
                        if (translatedPart != null) {
                            finalStringBuilder.append(translatedPart);
                        } else {
                            throw new IOException("Missing translated part after await");
                        }
                    }

                    TLRPC.TL_textWithEntities finalTextWithEntities = new TLRPC.TL_textWithEntities();
                    if (entities != null) {
                        Pair<String, ArrayList<TLRPC.MessageEntity>> textAndEntitiesTranslated = HTMLKeeper.htmlToEntities(finalStringBuilder.toString(), entities, false);
                        finalTextWithEntities.text = textAndEntitiesTranslated.first;
                        finalTextWithEntities.entities = textAndEntitiesTranslated.second;
                        finalTextWithEntities = TranslateAlert2.preprocess(originalText, finalTextWithEntities);
                    } else {
                        finalTextWithEntities.text = finalStringBuilder.toString();
                        finalTextWithEntities.entities = new ArrayList<>();
                    }

                    callback.onSuccess(finalTextWithEntities);

                } catch (Exception e) {
                    OctoLogging.e(TAG, e);
                    callback.onResponseReceived();
                    callback.onError();
                }
            }
        }.start();
    }

    private static String composeBody(String part, String toTranslate) throws IOException {
        JSONObject object = new JSONObject();

        try {
            object.put("format", "text");
            object.put("q", part);
            object.put("target", toTranslate);
            object.put("key", OctoConfig.INSTANCE.googleCloudKeyTranslator.getValue());
        } catch (JSONException ignored) {
            throw new IOException("json failed");
        }

        return object.toString();
    }

    private static Pair<String, String> parseGoogleResponse(String response) throws JSONException, IOException {
        JSONObject jsonObject = new JSONObject(response);

        if (jsonObject.has("data")) {
            JSONObject data = jsonObject.getJSONObject("data");
            if (data.has("translations")) {
                JSONArray translations = data.getJSONArray("translations");
                if (translations.length() > 0) {
                    JSONObject translation = translations.getJSONObject(0);
                    if (translation.has("translatedText")) {
                        String translatedText = translation.getString("translatedText");
                        String sourceLanguage = null;
                        if (translation.has("detectedSourceLanguage")) {
                            sourceLanguage = translation.getString("detectedSourceLanguage");
                        }
                        if (!translatedText.trim().isEmpty()) {
                            return new Pair<>(translatedText, sourceLanguage);
                        }
                    }
                }
            }
        }

        throw new IOException("Invalid or empty translation response");
    }

    public static String convertLanguageCode(String completeLanguage) {
        if (!completeLanguage.contains("-")) {
            return completeLanguage.toLowerCase();
        }

        if (targetLanguages.contains(completeLanguage.toLowerCase())) {
            return completeLanguage.toLowerCase();
        }

        String languageCode = completeLanguage.split("-")[0].toLowerCase();

        if (languageCode.equals("zh")) {
            String countryCode = completeLanguage.split("-")[1].toLowerCase();
            if (countryCode.equals("cn") || countryCode.equals("dg")) {
                languageCode = "zh-CN";
            } else if (countryCode.equals("tw") || countryCode.equals("hk")) {
                languageCode = "zh-TW";
            }
        }

        return languageCode.toLowerCase();
    }

    @Override
    public boolean isUnsupportedLanguage(String completeLanguage) {
        return !targetLanguages.contains(convertLanguageCode(completeLanguage));
    }
}