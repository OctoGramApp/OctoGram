/*
 * This is the source code of OctoGram for Android
 * It is licensed under GNU GPL v2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright OctoGram, 2023-2024.
 */

package it.octogram.android.http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Scanner;

public class StandardHTTPRequest {
    private final HttpURLConnection httpURLConnection;

    public StandardHTTPRequest(String url) throws IOException {
        httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
        httpURLConnection.setConnectTimeout(1000);
    }

    public StandardHTTPRequest header(String key, String value) {
        httpURLConnection.setRequestProperty(key, value);
        return this;
    }

    //public native static int ping(String ip) throws SocketException;
    public static int ping(String address) throws IOException {
        long dnsResolved = System.currentTimeMillis();
        Socket socket = new Socket(address, 80);
        socket.close();
        return Math.round(System.currentTimeMillis() - dnsResolved);
    }

    public StandardHTTPRequest data(String data) throws IOException {
        httpURLConnection.setDoOutput(true);
        DataOutputStream dataOutputStream = new DataOutputStream(httpURLConnection.getOutputStream());
        byte[] t = data.getBytes(Charset.defaultCharset());
        dataOutputStream.write(t);
        dataOutputStream.flush();
        dataOutputStream.close();
        return this;
    }

    public String request() throws IOException {
        httpURLConnection.connect();
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
}
