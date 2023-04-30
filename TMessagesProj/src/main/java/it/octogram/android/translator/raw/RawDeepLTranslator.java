package it.octogram.android.translator.raw;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

public class RawDeepLTranslator {
    private final AtomicLong id = new AtomicLong(ThreadLocalRandom.current().nextLong(Long.parseLong("10000000000")));
    private final ReentrantLock lock = new ReentrantLock();
    private static volatile String cookie;
    private static int retry_429 = 3;
    private static int retry_timeout = 10;
    private static long sleepTime_429 = 1000L;
    private static final Pattern iPattern = Pattern.compile("i");
    private static final String xInstance = UUID.randomUUID().toString();

    public RawDeepLTranslator() {
    }

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

    public String[] translate(String text, String targetLanguage, String formality, String splitting) throws Exception {
        return this.translate(text, "", targetLanguage, formality, splitting);
    }

    public String[] translate(String text, String sourceLanguage, String targetLanguage, String formality, String splitting) throws Exception {
        if (formality != null && !formality.equals("formal") && !formality.equals("informal")) {
            throw new Exception("Unable to communicate with DeepL. Formality must be either formal or informal.");
        } else if (!splitting.equals("newlines") && !splitting.equals("sentences") && !splitting.equals("paragraphs")) {
            throw new Exception("Unable to communicate with DeepL. Splitting must be either newlines, sentences, or paragraphs.");
        } else {
            System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
            String regionalVariant = null;
            sourceLanguage = sourceLanguage.toLowerCase();
            targetLanguage = targetLanguage.toLowerCase();
            if (targetLanguage.contains("-")) {
                String[] tempArray = targetLanguage.split("-");
                targetLanguage = tempArray[0];
                regionalVariant = tempArray[0] + "-" + tempArray[1].toUpperCase();
            }

            this.id.incrementAndGet();
            JSONObject _body = new JSONObject();
            JSONObject params = new JSONObject();
            JSONObject lang = new JSONObject();
            JSONArray texts = new JSONArray();
            lang.put("source_lang_user_selected", sourceLanguage);
            lang.put("target_lang", targetLanguage);
            texts.put((new JSONObject()).put("text", text).put("requestAlternatives", 0));
            params.put("texts", texts);
            params.put("splitting", splitting);
            params.put("commonJobParams", (new JSONObject()).put("regionalVariant", regionalVariant == null ? JSONObject.NULL : regionalVariant).put("wasSpoken", false).put("formality", formality == null ? JSONObject.NULL : formality));
            params.put("lang", lang);
            int iCounter = 1;

            //noinspection StatementWithEmptyBody
            for (Matcher iMatcher = iPattern.matcher(text); iMatcher.find(); ++iCounter) {
            }

            params.put("timestamp", this.getTimestamp(iCounter));
            _body.put("jsonrpc", "2.0");
            _body.put("method", "LMT_handle_texts");
            _body.put("params", params);
            _body.put("id", this.id.get());
            String body = (this.id.get() + 3L) % 13L != 0L && (this.id.get() + 5L) % 29L != 0L ? _body.toString().replace("hod\":\"", "hod\": \"") : _body.toString().replace("hod\":\"", "hod\" : \"");
            JSONObject result = (new JSONObject(Objects.requireNonNull(this.request(body)))).getJSONObject("result");
            return new String[]{result.getString("lang"), ((JSONObject) result.getJSONArray("texts").get(0)).getString("text")};
        }
    }

    private String request(String body) throws IOException {
        int i = retry_timeout;
        int var5 = retry_429;

        boolean flag;
        do {
            flag = false;

            try {
                return this.rawRequest(body);
            } catch (ConnectException | SocketTimeoutException var9) {
                flag = true;
                if (i-- <= 0) {
                    throw var9;
                }
            } catch (IOException var10) {
                var10.printStackTrace();
                if (Objects.requireNonNull(var10.getMessage()).contains("429")) {
                    flag = true;
                    if (var5-- <= 0) {
                        throw var10;
                    }

                    try {
                        Thread.sleep(sleepTime_429);
                    } catch (InterruptedException var8) {
                        var8.printStackTrace();
                    }
                }
            }
        } while (flag);

        return null;
    }

    private String rawRequest(String body) throws IOException {
        boolean errorOccurred = false;
        URL downloadUrl = new URL("https://www2.deepl.com/jsonrpc");
        HttpURLConnection httpConnection = (HttpURLConnection) downloadUrl.openConnection();
        httpConnection.setConnectTimeout(10000);
        httpConnection.setRequestProperty("referer", "https://www.deepl.com/");
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
        httpConnection.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8.name()));
        httpConnection.getOutputStream().flush();
        httpConnection.getOutputStream().close();

        InputStream httpConnectionStream;
        try {
            httpConnectionStream = AndroidUtilities.decompressStream(httpConnection.getInputStream());
        } catch (IOException var12) {
            errorOccurred = true;
            httpConnectionStream = AndroidUtilities.decompressStream(httpConnection.getErrorStream());
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

    private Long getTimestamp(int iNumber) {
        long now = System.currentTimeMillis();
        return now + (long) iNumber - now % (long) iNumber;
    }
}
