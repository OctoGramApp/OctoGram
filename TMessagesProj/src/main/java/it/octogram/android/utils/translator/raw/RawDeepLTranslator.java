/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.translator.raw;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import it.octogram.android.logs.OctoLogging;

public class RawDeepLTranslator {
    private static final String internalRequest = "https://www2.deepl.com/jsonrpc";
    private static final String referer = "https://www.deepl.com/";

    private final AtomicLong id = new AtomicLong(ThreadLocalRandom.current().nextLong(Long.parseLong("10000000000")));
    private final ReentrantLock lock = new ReentrantLock();
    private static volatile String cookie;
    private static int retry_429 = 3;
    private static int retry_timeout = 10;
    private static long sleepTime_429 = 1000L;
    private static final Pattern iPattern = Pattern.compile("i");
    private static final String xInstance = UUID.randomUUID().toString();

    public void setParams(int retry_429, int retry_timeout, long sleepTime_429) throws Exception {
        if (retry_429 >= 0 && retry_timeout >= 0) {
            this.lock.lock();
            RawDeepLTranslator.retry_429 = retry_429;
            RawDeepLTranslator.retry_timeout = retry_timeout;
            RawDeepLTranslator.sleepTime_429 = sleepTime_429;
            this.lock.unlock();
        } else {
            throw new Exception("Unable to set params");
        }
    }

    public String executeTranslation(String text, String fromLanguage, String toLanguage, String formality, String splitting) throws IOException, JSONException {
        if (formality != null && !formality.equals("formal") && !formality.equals("informal")) {
            throw new IOException("Invalid formality parameter for raw deepl translator");
        }

        if (!splitting.equals("newlines") && !splitting.equals("sentences") && !splitting.equals("paragraphs")) {
            throw new IOException("Invalid splitting parameter for raw deepl translator");
        }

        this.id.incrementAndGet();

        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        String regionalVariant = null;
        fromLanguage = fromLanguage.toLowerCase();
        toLanguage = toLanguage.toLowerCase();
        if (toLanguage.contains("-")) {
            String[] tempArray = toLanguage.split("-");
            toLanguage = tempArray[0];
            regionalVariant = tempArray[0] + "-" + tempArray[1].toUpperCase();
        }

        JSONObject langData = new JSONObject();
        langData.put("source_lang_user_selected", fromLanguage);
        langData.put("target_lang", toLanguage);

        JSONObject singleTextObject = new JSONObject();
        singleTextObject.put("text", text);
        singleTextObject.put("requestAlternatives", 0);
        JSONArray textData = new JSONArray();
        textData.put(singleTextObject);

        JSONObject settings = new JSONObject();
        settings.put("regionalVariant", regionalVariant == null ? JSONObject.NULL : regionalVariant);
        settings.put("formality", formality == null ? JSONObject.NULL : formality);
        settings.put("wasSpoken", false);

        JSONObject params = new JSONObject();
        params.put("texts", textData);
        params.put("lang", langData);
        params.put("splitting", splitting);
        params.put("timestamp", calculateTimestamp(text));
        params.put("commonJobParams", settings);

        JSONObject finalBody = new JSONObject();
        finalBody.put("jsonrpc", "2.0");
        finalBody.put("method", "LMT_handle_texts");
        finalBody.put("params", params);
        finalBody.put("id", this.id.get());

        String finalBodyReparsed = getFixedBodyValue(finalBody);
        String response = this.request(finalBodyReparsed);

        if (TextUtils.isEmpty(response)) {
            throw new IOException("Unavailable deepl response");
        }

        return composeResult(response);
    }

    private Long calculateTimestamp(String text) {
        int iCounter = 1;

        Matcher iMatcher = iPattern.matcher(text);
        while (iMatcher.find()) {
            iCounter++;
        }

        long now = System.currentTimeMillis();
        return now + (long) iCounter - now % (long) iCounter;
    }

    private String getFixedBodyValue(JSONObject finalBody) {
        long idValue = this.id.get();
        boolean condition = (idValue + 3L) % 13L != 0L && (idValue + 5L) % 29L != 0L;

        if (condition) {
            return finalBody.toString().replace("hod\":\"", "hod\": \"");
        } else {
            return finalBody.toString().replace("hod\":\"", "hod\" : \"");
        }
    }

    private String request(String body) throws IOException {
        int i = retry_timeout;
        int var5 = retry_429;

        boolean needToRetryFail;
        do {
            needToRetryFail = false;

            try {
                return this.rawRequest(body);
            } catch (ConnectException | SocketTimeoutException var9) {
                needToRetryFail = true;
                if (i-- <= 0) {
                    throw var9;
                }
            } catch (IOException var10) {
                OctoLogging.e(var10);
                if (Objects.requireNonNull(var10.getMessage()).contains("429")) {
                    needToRetryFail = true;
                    if (var5-- <= 0) {
                        throw var10;
                    }

                    try {
                        Thread.sleep(sleepTime_429);
                    } catch (InterruptedException var8) {
                        OctoLogging.e(var8);
                    }
                }
            }
        } while (needToRetryFail);

        return null;
    }

    private static String composeResult(String response) throws IOException, JSONException {
        JSONObject object = new JSONObject(response);

        if (object.has("result")) {
            JSONObject result = object.getJSONObject("result");
            if (result.has("texts")) {
                JSONObject firstTextResult = result.getJSONArray("texts").getJSONObject(0);
                if (firstTextResult.has("text")) {
                    return firstTextResult.getString("text");
                }
            }
        }

        throw new IOException("empty translation message");
    }

    private String rawRequest(String body) throws IOException {
        boolean errorOccurred = false;
        HttpURLConnection httpConnection = getHttpURLConnection();
        httpConnection.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8.name()));
        httpConnection.getOutputStream().flush();
        httpConnection.getOutputStream().close();

        InputStream httpConnectionStream;
        try {
            httpConnectionStream = decompressStream(httpConnection.getInputStream());
        } catch (IOException var12) {
            errorOccurred = true;
            httpConnectionStream = decompressStream(httpConnection.getErrorStream());
        }

        if (!errorOccurred) {
            Map<String, List<String>> map = httpConnection.getHeaderFields();
            if (cookie == null) {
                synchronized (this) {
                    if (cookie == null) {
                        cookie = (Objects.requireNonNull(map.get("Set-Cookie"))).get(0);
                        cookie = cookie.substring(0, cookie.indexOf(";"));
                    }
                }
            }
        }
        ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
        byte[] data = new byte[32768];
        while (true) {
            int read = httpConnectionStream.read(data);
            if (read <= 0) {
                String result = outBuf.toString();
                httpConnectionStream.close();
                outBuf.close();
                if (errorOccurred) {
                    throw new IOException(httpConnection.getResponseCode() + ":" + result);
                } else {
                    return result;
                }
            }
            outBuf.write(data, 0, read);
        }
    }

    @NonNull
    private static HttpURLConnection getHttpURLConnection() throws IOException {
        URL downloadUrl = new URL(internalRequest);
        HttpURLConnection httpConnection = (HttpURLConnection) downloadUrl.openConnection();
        httpConnection.setConnectTimeout(10000);
        httpConnection.setRequestProperty("referer", referer);
        httpConnection.setRequestProperty("x-instance", xInstance);
        httpConnection.setRequestProperty("user-agent", "DeepL-Android/VersionName(name=1.0.1) Android 10 (aarch64)");
        httpConnection.setRequestProperty("x-app-os-name", "Android");
        httpConnection.setRequestProperty("x-app-os-version", "10");
        httpConnection.setRequestProperty("x-app-version", "1.0.1");
        httpConnection.setRequestProperty("x-app-build", "13");
        httpConnection.setRequestProperty("x-app-device", "Pixel 5");
        httpConnection.setRequestProperty("x-app-instance-id", xInstance);
        httpConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        httpConnection.setRequestProperty("Accept-Encoding", "gzip");
        if (cookie != null) {
            httpConnection.setRequestProperty("Cookie", cookie);
        }

        httpConnection.setRequestMethod("POST");
        httpConnection.setDoOutput(true);
        return httpConnection;
    }

    public static InputStream decompressStream(InputStream input) throws IOException {
        PushbackInputStream pb = new PushbackInputStream(input, 2);
        byte[] signature = new byte[2];
        int len = pb.read(signature);
        pb.unread(signature, 0, len);
        if (signature[0] == (byte) 0x1f && signature[1] == (byte) 0x8b) {
            return new GZIPInputStream(pb);
        } else {
            return pb;
        }
    }
}
