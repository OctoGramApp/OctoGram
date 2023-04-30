package it.octogram.android.translator;

import android.os.SystemClock;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.octogram.android.http.StandardHTTPRequest;

public class DuckDuckGoTranslator extends BaseTranslator {

    private final List<String> targetLanguages = Arrays.asList(
            "af", "sq", "ar", "am", "hy", "as", "az", "bn", "bs", "bg", "yue", "ca", "cs",
            "zh-Hans", "zh-Hant", "ko", "ht", "hr", "ku", "kmr", "da", "he", "et", "sw",
            "fj", "fil", "fi", "fr", "fr-CA", "cy", "ja", "el", "gu", "hi", "mww", "id",
            "en", "iu", "ga", "is", "it", "kn", "kk", "km", "lo", "lv", "lt", "ml", "ms",
            "mt", "mi", "mr", "my", "ne", "nb", "nl", "or", "ps", "fa", "pl", "pt", "ty",
            "pt-PT", "pa", "ro", "ru", "sm", "sr-Cyrl", "sr-Latn", "sl", "es", "sv", "mg",
            "th", "ta", "de", "te", "ti", "to", "tr", "uk", "hu", "ur", "vi"
    );

    private static final class InstanceHolder {
        private static final DuckDuckGoTranslator instance = new DuckDuckGoTranslator();
    }

    static DuckDuckGoTranslator getInstance() {
        return InstanceHolder.instance;
    }

    @Override
    protected Result singleTranslate(Object query, String tl) throws IOException, JSONException {
        ArrayList<String> blocks = getStringBlocks((String) query, 999);
        StringBuilder resultString = new StringBuilder();
        String resultLang = "";
        String urlAuth = "https://duckduckgo.com/?q=translate&ia=web";
        String userAgent = "Mozilla/5.0 (Linux; Android 12; SM-G980F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.98 Mobile Safari/537.36";
        String responseAuth = new StandardHTTPRequest(urlAuth)
                .header("User-Agent", userAgent)
                .request();
        if (TextUtils.isEmpty(responseAuth)) {
            return null;
        }
        int start = responseAuth.indexOf("vqd=");
        int end = responseAuth.indexOf(";", start);
        String uidRaw = responseAuth.substring(start + "vqd=".length(), end);
        Pattern p = Pattern.compile("[\\d-]+");
        Matcher m = p.matcher(uidRaw);
        if (m.find()) {
            String uid = m.group(0);
            String url = "https://duckduckgo.com/translation.js?" +
                    "vqd=" + URLEncoder.encode(uid, StandardCharsets.UTF_8.name()) +
                    "&query=translate" +
                    "&to=" + tl;
            for (String block : blocks) {
                boolean worked = false;
                for (int i = 0; i < 60; i++) {
                    String response = new StandardHTTPRequest(url)
                            .header("User-Agent", userAgent)
                            .header("Content-Length", String.valueOf(block.getBytes(Charset.defaultCharset()).length))
                            .data(block)
                            .request();
                    if (TextUtils.isEmpty(response)) {
                        return null;
                    }
                    try {
                        Result result = getResult(response);
                        if (TextUtils.isEmpty(resultLang)) {
                            resultLang = result.sourceLanguage;
                        }
                        resultString.append(buildTranslatedString(block, ((String) result.translation)));
                    } catch (Http429Exception e) {
                        SystemClock.sleep(1000);
                        continue;
                    }
                    worked = true;
                    break;
                }
                if (!worked) {
                    throw new Http429Exception();
                }
            }
        }
        return new Result(
                resultString.toString(),
                resultLang
        );
    }

    @Override
    public List<String> getTargetLanguages() {
        return targetLanguages;
    }

    @Override
    public String convertLanguageCode(String language, String country) {
        String languageLowerCase = language.toLowerCase();
        String code;
        if (!TextUtils.isEmpty(country)) {
            String countryUpperCase = country.toUpperCase();
            if (targetLanguages.contains(languageLowerCase + "-" + countryUpperCase)) {
                code = languageLowerCase + "-" + countryUpperCase;
            } else if (languageLowerCase.equals("zh")) {
                if (countryUpperCase.equals("DG")) {
                    code = "zh-Hant";
                } else {
                    code = "zh-Hans";
                }
            } else {
                code = languageLowerCase;
            }
        } else {
            code = languageLowerCase;
        }
        return code;
    }

    private Result getResult(String string) throws JSONException, Http429Exception {
        JSONObject object = new JSONObject(string);
        if (object.has("error")) {
            if (object.getString("error").equals("Forbidden")) {
               throw new Http429Exception();
            }
        }
        return new Result(object.getString("translated"), object.getString("detected_language"));
    }
}
