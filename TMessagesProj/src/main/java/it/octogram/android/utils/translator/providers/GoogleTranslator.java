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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import it.octogram.android.utils.OctoLogging;
import it.octogram.android.utils.OctoUtils;
import it.octogram.android.utils.network.StandardHTTPRequest;
import it.octogram.android.utils.translator.HTMLKeeper;
import it.octogram.android.utils.translator.SingleTranslationsHandler;

public class GoogleTranslator implements BaseTranslator {
    public static final GoogleTranslator INSTANCE = new GoogleTranslator();

    private static final String TAG = "GoogleTranslator";
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

    private static final String[] devices = new String[]{
            "Linux; U; Android 10; Pixel 4",
            "Linux; U; Android 10; Pixel 4 XL",
            "Linux; U; Android 10; Pixel 4a",
            "Linux; U; Android 10; Pixel 4a XL",
            "Linux; U; Android 11; Pixel 4",
            "Linux; U; Android 11; Pixel 4 XL",
            "Linux; U; Android 11; Pixel 4a",
            "Linux; U; Android 11; Pixel 4a XL",
            "Linux; U; Android 11; Pixel 5",
            "Linux; U; Android 11; Pixel 5a",
            "Linux; U; Android 12; Pixel 4",
            "Linux; U; Android 12; Pixel 4 XL",
            "Linux; U; Android 12; Pixel 4a",
            "Linux; U; Android 12; Pixel 4a XL",
            "Linux; U; Android 12; Pixel 5",
            "Linux; U; Android 12; Pixel 5a",
            "Linux; U; Android 12; Pixel 6",
            "Linux; U; Android 12; Pixel 6 Pro",
            "Linux; U; Android 13; Pixel 6a",
            "Linux; U; Android 13; Pixel 7",
            "Linux; U; Android 13; Pixel 7 Pro",
            "Linux; U; Android 14; Pixel 8",
            "Linux; U; Android 14; Pixel 8 Pro",
            "Linux; U; Android 14; Pixel 8a",
            "Linux; U; Android 15; Pixel 9",
            "Linux; U; Android 15; Pixel 9 Pro",
            "Linux; U; Android 15; Pixel 9 Pro XL",
            "Linux; U; Android 15; Pixel 9a"
    };

    @Override
    public String getName() {
        return "Google";
    }

    @Override
    public int getMaxExecutionPoolSize() {
        return 7;
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
                                StandardHTTPRequest.Builder requestBuilder = new StandardHTTPRequest.Builder(composeUrl(currentPart, toLanguage));
                                requestBuilder.header("User-Agent", getUserAgent());
                                StandardHTTPRequest httpRequest = requestBuilder.build();
                                String response = httpRequest.request();

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

    private static String getUserAgent() {
        String randomDevice = devices[(int) Math.round(Math.random() * (devices.length - 1))];
        return "GoogleTranslate/6.28.0.05.421483610 (" + randomDevice + ")";
    }

    private static String composeUrl(String part, String toLanguage) {
        String url = "https://translate.google.com/translate_a/single";
        url += "?dj=1";
        url += "&sl=auto"; // source language
        url += "&tl=" + toLanguage; // to language
        url += "&ie=UTF-8"; // input encoding
        url += "&oe=UTF-8"; // output encoding
        url += "&client=at"; // android client
        url += "&dt=t";
        url += "&otf=2";

        try {
            url += "&q=" + URLEncoder.encode(part, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return url;
    }

    private static Pair<String, String> parseGoogleResponse(String response) throws JSONException, IOException {
        JSONObject jsonObject = new JSONObject(response);

        String sourceLanguage = jsonObject.optString("src");

        if (jsonObject.has("sentences")) {
            StringBuilder translation = new StringBuilder();
            JSONArray sentences = jsonObject.getJSONArray("sentences");

            for (int i = 0; i < sentences.length(); i++) {
                try {
                    JSONObject sentence = sentences.getJSONObject(i);
                    if (sentence.has("trans")) {
                        translation.append(sentence.getString("trans"));
                    }
                } catch (JSONException ignored) {

                }
            }

            if (!translation.toString().isEmpty()) {
                return new Pair<>(translation.toString(), sourceLanguage);
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