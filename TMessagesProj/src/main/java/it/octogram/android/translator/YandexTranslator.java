/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.translator;

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
import java.util.List;

import it.octogram.android.http.StandardHTTPRequest;
import it.octogram.android.logs.OctoLogging;
import it.octogram.android.utils.OctoUtils;

public class YandexTranslator {
    private static final String uuid = OctoUtils.generateRandomString().replace("-", "");
    private static final String contentType = "application/x-www-form-urlencoded";
    private static final String userAgent = "ru.yandex.translate/21.15.4.21402814 (Xiaomi Redmi K20 Pro; Android 11)";

    private static final List<String> targetLanguages = List.of(
            "af", "sq", "am", "ar", "hy", "az", "ba", "eu", "be", "bn", "bs", "bg", "my",
            "ca", "ceb", "zh", "cv", "hr", "cs", "da", "nl", "en", "eo", "et", "fi", "fr",
            "gl", "ka", "de", "el", "gu", "ht", "he", "mrj", "hi", "hu", "is", "id", "ga",
            "it", "ja", "jv", "kn", "kk", "km", "ko", "ky", "lo", "la", "lv",
            "lt", "lb", "mk", "mg", "ms", "ml", "mt", "mi", "mr", "mn", "ne", "no", "pap",
            "fa", "pl", "pt", "pa", "ro", "ru", "gd", "sr", "si", "sk", "sl", "es", "su",
            "sw", "sv", "tl", "tg", "ta", "tt", "te", "th", "tr", "udm", "uk", "ur", "uz",
            "vi", "cy", "xh", "sah", "yi", "zu");

    public static void executeTranslation(String text, ArrayList<TLRPC.MessageEntity> entities, String toLanguage, SingleTranslationManager.OnTranslationResultCallback callback) {
        new Thread() {
            @Override
            public void run() {
                try {
                    TLRPC.TL_textWithEntities originalText = new TLRPC.TL_textWithEntities();
                    originalText.text = text;
                    originalText.entities = entities;

                    String text2 = entities == null ? text : HTMLKeeper.entitiesToHtml(text, entities, false);
                    StandardHTTPRequest request = new StandardHTTPRequest.Builder(composeUrl())
                            .header("User-Agent", userAgent)
                            .header("Content-Type", contentType)
                            .data(composeData(text2, toLanguage))
                            .build();

                    String response = request.request();

                    if (TextUtils.isEmpty(response)) {
                        callback.onResponseReceived();
                        callback.onError();
                        return;
                    }

                    TLRPC.TL_textWithEntities finalText = new TLRPC.TL_textWithEntities();
                    if (entities != null) {
                        Pair<String, ArrayList<TLRPC.MessageEntity>> text3 = HTMLKeeper.htmlToEntities(composeResult(response), entities, false);
                        finalText.text = text3.first;
                        finalText.entities = text3.second;
                        finalText = TranslateAlert2.preprocess(originalText, finalText);
                    } else {
                        finalText.text = composeResult(response);
                    }

                    callback.onResponseReceived();
                    callback.onSuccess(finalText);
                } catch (IOException | JSONException e) {
                    OctoLogging.e("YandexTranslator", e);
                    callback.onResponseReceived();
                    callback.onError();
                }
            }
        }.start();
    }

    private static String composeUrl() {
        String url = "https://translate.yandex.net/api/v1/tr.json/translate";
        url += "?id=" + uuid + "-0-0"; // app uid
        url += "&srv=android"; // server

        return url;
    }

    private static String composeData(String part, String toLanguage) {
        String data = "lang=" + toLanguage;
        try {
            data += "&text=" + URLEncoder.encode(part, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return data;
    }

    private static String composeResult(String response) throws IOException, JSONException {
        JSONObject object = new JSONObject(response);

        if (object.has("text")) {
            StringBuilder translation = new StringBuilder();
            JSONArray text = object.getJSONArray("text");

            for (int i = 0; i < text.length(); i++) {
                translation.append(text.getString(i));
            }

            if (!translation.toString().isEmpty()) {
                return translation.toString();
            }
        }

        throw new IOException(object.has("message") ? object.getString("message") : "empty translation message");
    }

    public static String convertLanguageCode(String completeLanguage) {
        if (!completeLanguage.contains("-")) {
            return completeLanguage.toLowerCase();
        }

        if (targetLanguages.contains(completeLanguage.toLowerCase())) {
            return completeLanguage.toLowerCase();
        }

        String languageCode = completeLanguage.split("-")[0].toLowerCase();
        return languageCode.toLowerCase();
    }

    public static boolean isUnsupportedLanguage(String completeLanguage) {
        return !targetLanguages.contains(convertLanguageCode(completeLanguage));
    }
}