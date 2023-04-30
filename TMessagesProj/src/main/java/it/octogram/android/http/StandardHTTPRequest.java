package it.octogram.android.http;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Scanner;

import it.octogram.android.translator.BaseTranslator;

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

    public native static int ping(String ip) throws SocketException;

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
        if (httpURLConnection.getResponseCode() == 429) {
            throw new BaseTranslator.Http429Exception();
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
}
