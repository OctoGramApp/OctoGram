/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class StandardHTTPRequest {
    private final HttpURLConnection httpURLConnection;

    private StandardHTTPRequest(Builder builder) throws IOException {
        httpURLConnection = (HttpURLConnection) new URL(builder.url).openConnection();
        httpURLConnection.setConnectTimeout(builder.connectTimeout);
        httpURLConnection.setRequestMethod(builder.method);

        if (builder.headers != null) {
            for (Map.Entry<String, String> entry : builder.headers.entrySet()) {
                httpURLConnection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        if (builder.data != null) {
            httpURLConnection.setDoOutput(true);
            DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
            byte[] t = builder.data.getBytes(Charset.defaultCharset());
            dataOutputStream.write(t);
            dataOutputStream.flush();
            dataOutputStream.close();
        }
    }

    public HttpURLConnection getHttpURLConnection() {
        return httpURLConnection;
    }


    public native static int ping(String ip) throws SocketException;
//    public static int ping(String address) throws IOException {
//        long dnsResolved = System.currentTimeMillis();
//        Socket socket = new Socket(address, 80);
//        socket.close();
//        return Math.round(System.currentTimeMillis() - dnsResolved);
//    }


    public static class Http429Exception extends IOException {
    }

    public String request() throws IOException {
        httpURLConnection.connect();
        if (httpURLConnection.getResponseCode() == 429) {
            throw new Http429Exception();
        }
        InputStream stream;
        if (httpURLConnection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
            stream = httpURLConnection.getInputStream();
        } else {
            stream = httpURLConnection.getErrorStream();
        }
        String response = new Scanner(stream, "UTF-8")
                .useDelimiter("\\A")
                .next();
        stream.close();
        return response;
    }

    public static class Builder {
        private final String url;
        private int connectTimeout = 1000;
        private final Map<String, String> headers = new HashMap<>();
        private String data;
        private String method = "GET";

        public Builder(String url) {
            this.url = url;
        }

        public Builder connectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder header(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        public Builder data(String data) {
            this.data = data;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }


        public StandardHTTPRequest build() throws IOException {
            return new StandardHTTPRequest(this);
        }
    }
}