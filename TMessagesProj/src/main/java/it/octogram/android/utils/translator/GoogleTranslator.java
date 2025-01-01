/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.translator;

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

public class GoogleTranslator {

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
    };

    public static void executeTranslation(String text, ArrayList<TLRPC.MessageEntity> entities, String toLanguage, SingleTranslationManager.OnTranslationResultCallback callback) {
        new Thread() {
            @Override
            public void run() {
                try {
                    TLRPC.TL_textWithEntities originalText = new TLRPC.TL_textWithEntities();
                    originalText.text = text;
                    originalText.entities = entities;

                    StringBuilder finalString = new StringBuilder();
                    String text2 = entities == null ? text : HTMLKeeper.entitiesToHtml(text, entities, false);
                    ArrayList<String> parts = OctoUtils.getStringParts(text2, 2500);

                    for (String part : parts) {
                        StandardHTTPRequest request = new StandardHTTPRequest(composeUrl(part, toLanguage));
                        request.header("User-Agent", getUserAgent());
                        String response = request.request();

                        if (TextUtils.isEmpty(response)) {
                            callback.onResponseReceived();
                            callback.onError();
                            return;
                        }

                        finalString.append(composeResult(response));
                    }

                    TLRPC.TL_textWithEntities finalText = new TLRPC.TL_textWithEntities();
                    if (entities != null) {
                        Pair<String, ArrayList<TLRPC.MessageEntity>> text3 = HTMLKeeper.htmlToEntities(finalString.toString(), entities, false);
                        finalText.text = text3.first;
                        finalText.entities = text3.second;
                        finalText = TranslateAlert2.preprocess(originalText, finalText);
                    } else {
                        finalText.text = finalString.toString();
                    }

                    callback.onResponseReceived();
                    callback.onSuccess(finalText);
                } catch (IOException | JSONException e) {
                    OctoLogging.e(e);
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

    private static String composeResult(String response) throws JSONException, IOException {
        JSONObject object = new JSONObject(response);

        if (object.has("sentences")) {
            StringBuilder translation = new StringBuilder();
            JSONArray sentences = object.getJSONArray("sentences");

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
                return translation.toString();
            }
        }

        throw new IOException("empty translation message");
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
            if (countryCode.equals("dg")) {
                languageCode = "zh-CN";
            } else if (countryCode.equals("hk")) {
                languageCode = "zh-TW";
            }
        }

        return languageCode.toLowerCase();
    }

    public static boolean isUnsupportedLanguage(String completeLanguage) {
        return !targetLanguages.contains(convertLanguageCode(completeLanguage));
    }
}