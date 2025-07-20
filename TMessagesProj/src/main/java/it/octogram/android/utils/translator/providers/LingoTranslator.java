/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.translator.providers;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.tgnet.TLRPC;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import it.octogram.android.http.StandardHTTPRequest;
import it.octogram.android.utils.OctoLogging;
import it.octogram.android.utils.OctoUtils;
import it.octogram.android.utils.translator.SingleTranslationsHandler;

public class LingoTranslator implements BaseTranslator {
    public static final LingoTranslator INSTANCE = new LingoTranslator();

    private static final List<String> targetLanguages = List.of("zh", "en", "ja", "ko", "es", "fr", "ru");

    private static final String contentType = "application/json";
    private static final String userAgent = "okhttp/3.12.3";
    private static final String url = "https://api.interpreter.caiyunai.com/v1/translator";
    private static final String authorization = "token 9sdftiq37bnv410eon2l";

    @Override
    public String getName() {
        return "Lingo";
    }

    @Override
    public int getMaxPoolState() {
        return 3;
    }

    @Override
    public void executeTranslation(String text, ArrayList<TLRPC.MessageEntity> entities, String toLanguage, SingleTranslationsHandler.OnTranslationResultCallback callback) {
        new Thread() {
            @Override
            public void run() {
                try {
                    StringBuilder finalString = new StringBuilder();
                    ArrayList<String> parts = OctoUtils.getStringParts(text, 2500);

                    for (String part : parts) {
                        StandardHTTPRequest request = new StandardHTTPRequest.Builder(url)
                                .method("POST")
                                .header("Content-Type", contentType)
                                .header("User-Agent", userAgent)
                                .header("X-Authorization", authorization)
                                .data(composeBody(part, toLanguage))
                                .build();

                        String response = request.request();

                        if (TextUtils.isEmpty(response)) {
                            callback.onResponseReceived();
                            callback.onError();
                            return;
                        }

                        finalString.append(composeResult(response));
                    }

                    TLRPC.TL_textWithEntities finalText = new TLRPC.TL_textWithEntities();
                    finalText.text = finalString.toString();

                    callback.onResponseReceived();
                    callback.onSuccess(finalText);
                } catch (IOException | JSONException e) {
                    OctoLogging.e("LingoTranslator", e);
                    callback.onResponseReceived();
                    callback.onError();
                }
            }
        }.start();
    }

    private static String composeBody(String part, String toTranslate) throws IOException {
        JSONObject object = new JSONObject();

        try {
            object.put("trans_type", "auto2" + toTranslate);
            object.put("source", URLEncoder.encode(part, StandardCharsets.UTF_8.name()));
            object.put("request_id", String.valueOf(System.currentTimeMillis()));
            object.put("detect", true);
        } catch (JSONException ignored) {
            throw new IOException("json failed");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return object.toString();
    }

    private static String composeResult(String response) throws JSONException, IOException {
        JSONObject object = new JSONObject(response);

        if (object.has("target")) {
            try {
                return object.getString("target");
            } catch (JSONException ignored) {
            }
        }

        throw new IOException("empty translation message");
    }

    @Override
    public boolean isUnsupportedLanguage(String completeLanguage) {
        return !targetLanguages.contains(completeLanguage);
    }
}