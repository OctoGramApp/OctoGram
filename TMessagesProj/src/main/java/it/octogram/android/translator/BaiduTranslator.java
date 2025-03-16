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
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.TranslateAlert2;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import it.octogram.android.http.StandardHTTPRequest;
import it.octogram.android.logs.OctoLogging;
import it.octogram.android.utils.OctoUtils;

public class BaiduTranslator {
    private static final String TAG = "BaiduTranslator";
    private static final HashMap<String, String> targetLanguages = new HashMap<>();
    private static final String uuid = UUID.randomUUID().toString().replace("-", "");

    static {
        List<String> standardLanguages = List.of("zh", "en", "th", "ru", "pt", "de", "it", "el", "nl", "pl", "cs", "hu");
        for (String lang : standardLanguages) {
            targetLanguages.put(lang, lang);
        }
        targetLanguages.put("ko", "kor");
        targetLanguages.put("fr", "fra");
        targetLanguages.put("es", "spa");
        targetLanguages.put("ar", "ara");
        targetLanguages.put("bg", "bul");
        targetLanguages.put("et", "est");
        targetLanguages.put("da", "dan");
        targetLanguages.put("fi", "fin");
        targetLanguages.put("ro", "rom");
        targetLanguages.put("sl", "slo");
        targetLanguages.put("sv", "swe");
        targetLanguages.put("zh-TW", "cht");
        targetLanguages.put("vi", "vie");
    }

    private static final String contentType = "application/x-www-form-urlencoded";
    private static final String userAgent = "BDTApp; Android 12; BaiduTranslate/10.2.1";

    public static void executeTranslation(String text, ArrayList<TLRPC.MessageEntity> entities, String toLanguage, SingleTranslationManager.OnTranslationResultCallback callback) {
        String finalToLanguage = targetLanguages.get(toLanguage);
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
                        StandardHTTPRequest request = new StandardHTTPRequest(composeUrl());
                        request.getHttpURLConnection().setRequestMethod("POST");
                        request.header("Content-Type", contentType);
                        request.header("User-Agent", userAgent);
                        request.data(composeBody(part, finalToLanguage));

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
                    OctoLogging.e(TAG, e);
                    callback.onResponseReceived();
                    callback.onError();
                }
            }
        }.start();
    }

    private static String composeUrl() {
        String url = "https://fanyi-app.baidu.com/transapp/agent.php";
        url += "?product=transapp";
        url += "&type=json";
        url += "&version=153";
        url += "&plat=android";
        url += "&req=v2trans";
        url += "&cuid="+uuid;

        return url;
    }

    private static String composeBody(String part, String toTranslate) {
        String currentTime = String.valueOf(System.currentTimeMillis());
        String sign = Utilities.MD5("query"+part+"imeiversion153timestamp"+currentTime+"fromautoto"+toTranslate+"reqv2transtextimage607e34f0fb3bf7895c102dacf9e9b0d7");

        String url = "sign=" + sign;
        url += "&sofireId=";
        url += "&zhType=0";
        url += "&use_cache_response=1";
        url += "&from=auto";
        url += "&timestamp=" + currentTime;

        try {
            url += "&query=" + URLEncoder.encode(part, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        url += "&needfixl=1";
        url += "&lfixver=1";
        url += "&is_show_ad=1";
        url += "&appRecommendSwitch=1";
        url += "&to=" + toTranslate;
        url += "&page=translate";

        return url;
    }

    private static String composeResult(String response) throws JSONException, IOException {
        JSONObject object = new JSONObject(response);

        if (object.has("fanyi_list")) {
            StringBuilder translation = new StringBuilder();
            JSONArray list = object.getJSONArray("fanyi_list");
            for (int i = 0; i < list.length(); i++) {
                try {
                    translation.append(list.getString(i));
                    if (i != list.length() - 1) {
                        translation.append("\n");
                    }
                } catch (JSONException ignored) {}
            }

            if (!translation.toString().isEmpty()) {
                return translation.toString();
            }
        }

        throw new IOException("empty translation message");
    }

    public static boolean isUnsupportedLanguage(String completeLanguage) {
        return !targetLanguages.containsKey(completeLanguage);
    }
}