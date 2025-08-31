/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2025.
 */

package it.octogram.android.utils.network;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import it.octogram.android.utils.OctoLogging;

/**
 * Provides basic functionality for making HTTP requests using Java's standard {@link HttpURLConnection}.
 * <p>
 * This class offers a simple way to perform GET, POST, etc., requests with custom headers, timeouts, and data.
 * It uses a {@link Builder} pattern for configuration.
 * <p>
 * Note: For more complex networking tasks, robustness, and features (like connection pooling, interceptors,
 * automatic GZIP, modern TLS features, etc.), consider using modern libraries like OkHttp or Retrofit.
 *
 * @see HttpURLConnection
 * @see Builder
 */
public class StandardHTTPRequest {

    private static final String TAG = "StandardHTTPRequest";

    private final HttpURLConnection httpURLConnection;

    /**
     * Private constructor to be called by the {@link Builder}.
     * Initializes the {@link HttpURLConnection} based on the builder's configuration.
     *
     * @param builder The configured builder instance.
     * @throws IOException If an error occurs opening the connection or writing data.
     */
    private StandardHTTPRequest(Builder builder) throws IOException {
        httpURLConnection = (HttpURLConnection) new URL(builder.url).openConnection();
        httpURLConnection.setConnectTimeout(builder.connectTimeout);
        httpURLConnection.setReadTimeout(builder.readTimeout);
        httpURLConnection.setRequestMethod(builder.method);

        for (Map.Entry<String, String> entry : builder.headers.entrySet()) {
            httpURLConnection.setRequestProperty(entry.getKey(), entry.getValue());
        }

        if (builder.data != null) {
            httpURLConnection.setDoOutput(true);

            byte[] postDataBytes = builder.data.getBytes(StandardCharsets.UTF_8);

            httpURLConnection.setRequestProperty("Content-Length", Integer.toString(postDataBytes.length));

            try (DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream())) {
                dataOutputStream.write(postDataBytes);
            }
        }
    }

    /**
     * Gets the underlying {@link HttpURLConnection} instance.
     * Primarily for advanced use cases or inspection; typical interaction is via {@link #request()}.
     *
     * @return The HttpURLConnection instance.
     */
    public HttpURLConnection getHttpURLConnection() {
        return httpURLConnection;
    }

    /**
     * Native method to perform a network ping.
     * Note: Implementation is platform-dependent (JNI). Requires native library.
     * Consider alternatives if portability or simplicity is key.
     *
     * @param ip The IP address or hostname to ping.
     * @return Ping time in milliseconds or an indicator of success/failure (implementation-specific).
     * @throws SocketException If a network error occurs at the socket level.
     */
    public native static int ping(String ip) throws SocketException;
//    public static int ping(String address) throws IOException {
//        long dnsResolved = System.currentTimeMillis();
//        Socket socket = new Socket(address, 80);
//        socket.close();
//        return Math.round(System.currentTimeMillis() - dnsResolved);
//    }

    /**
     * Custom exception indicating that the server responded with HTTP status code 429 (Too Many Requests).
     */
    public static class Http429Exception extends IOException {
        /**
         * Constructs an Http429Exception with a default message.
         */
        public Http429Exception() {
            super("HTTP Response Code 429: Too Many Requests");
        }

        /**
         * Constructs an Http429Exception with a custom message.
         *
         * @param message The detail message.
         */
        public Http429Exception(String message) {
            super(message);
        }

        /**
         * Constructs an Http429Exception with a custom message and cause.
         *
         * @param message The detail message.
         * @param cause   The cause.
         */
        public Http429Exception(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Executes the configured HTTP request and reads the response body as a String.
     * <p>
     * This method handles connecting, checking the response code, reading either the
     * input stream (for success codes < 400) or the error stream (for codes >= 400),
     * and decoding the response using UTF-8. It uses a {@link BufferedReader} for
     * efficient reading.
     * <p>
     * The connection is explicitly disconnected in a finally block.
     *
     * @return The response body as a String. Returns an empty string if the response body is empty.
     * @throws IOException      If any network I/O error occurs during connection or reading the response.
     * @throws Http429Exception If the server returns an HTTP 429 status code.
     */
    @Nullable
    public String request() throws IOException {
        InputStream stream;
        try {
            httpURLConnection.connect();

            int responseCode = httpURLConnection.getResponseCode();

            if (responseCode == 429) {
                throw new Http429Exception();
            }

            if (responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
                stream = httpURLConnection.getInputStream();
            } else {
                stream = httpURLConnection.getErrorStream();
            }

            if (stream == null) {
                OctoLogging.w(TAG, "Response stream is null (Code: " + responseCode + ")");
                return null;
            }

            StringBuilder responseBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }
            }

            String response = responseBuilder.toString();

            if (response.isEmpty()) {
                if (responseCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
                    OctoLogging.w(TAG, "Empty error response body received (Code: " + responseCode + ")");
                } else {
                    OctoLogging.w(TAG, "Empty successful response body received (Code: " + responseCode + ")");
                }
            }

            return response;

        } catch (IOException e) {
            OctoLogging.e(TAG, "IOException during HTTP request or reading response", e);
            throw new IOException("Failed during HTTP request execution or response reading: " + e.getMessage(), e);
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
    }

    /**
     * A fluent builder for creating and configuring {@link StandardHTTPRequest} instances.
     */
    public static class Builder {
        private final String url;
        private int connectTimeout = 10000;
        private int readTimeout = 10000;
        private final Map<String, String> headers = new HashMap<>();
        private String data;
        private String method = "GET";

        /**
         * Creates a new Builder for the specified URL.
         *
         * @param url The target URL for the HTTP request. Must not be null.
         */
        public Builder(String url) {
            if (url == null) {
                throw new IllegalArgumentException("URL cannot be null");
            }
            this.url = url;
        }

        /**
         * Sets the connection timeout in milliseconds.
         * Default is 10000ms (10 seconds).
         *
         * @param connectTimeout Timeout duration in milliseconds.
         * @return This Builder instance for chaining.
         */
        public Builder connectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        /**
         * Sets the read timeout in milliseconds. This is the timeout for waiting for data
         * after the connection is established.
         * Default is 10000ms (10 seconds).
         *
         * @param readTimeout Timeout duration in milliseconds.
         * @return This Builder instance for chaining.
         */
        public Builder readTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        /**
         * Adds a request header. If a header with the same key already exists, it will be overwritten.
         *
         * @param key   The header name (e.g., "Content-Type", "Authorization").
         * @param value The header value.
         * @return This Builder instance for chaining.
         */
        public Builder header(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        /**
         * Sets the request body data as a String. This is typically used for POST or PUT requests.
         * The data will be encoded using UTF-8.
         * Remember to set the appropriate "Content-Type" header using {@link #header(String, String)}.
         *
         * @param data The String data to send as the request body.
         * @return This Builder instance for chaining.
         */
        public Builder data(String data) {
            this.data = data;
            return this;
        }

        /**
         * Sets the HTTP method (e.g., "GET", "POST", "PUT", "DELETE").
         * The method name will be converted to uppercase.
         * Default is "GET".
         *
         * @param method The HTTP method name.
         * @return This Builder instance for chaining.
         */
        public Builder method(String method) {
            if (method != null) {
                this.method = method.toUpperCase();
            }
            return this;
        }

        /**
         * Builds the {@link StandardHTTPRequest} instance based on the configured settings.
         *
         * @return A new {@link StandardHTTPRequest} instance.
         * @throws IOException If an error occurs during initial setup (e.g., URL parsing, though less likely here).
         *                     The main I/O errors occur during {@link StandardHTTPRequest#request()}.
         */
        public StandardHTTPRequest build() throws IOException {
            return new StandardHTTPRequest(this);
        }
    }
}