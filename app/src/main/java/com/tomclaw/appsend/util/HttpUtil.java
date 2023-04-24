package com.tomclaw.appsend.util;

import android.util.Pair;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Solkin
 * Date: 04.11.13
 * Time: 14:40
 */
public class HttpUtil {

    public static final String GET = "GET";
    public static final String POST = "POST";

    public static final String UTF8_ENCODING = "UTF-8";

    public static final int SC_BAD_REQUEST = 400;

    /**
     * Builds Url request string from specified parameters.
     *
     * @param pairs
     * @return String - Url request parameters.
     * @throws UnsupportedEncodingException
     */
    public static String prepareParameters(List<Pair<String, String>> pairs)
            throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();
        // Perform pair concatenation.
        for (Pair<String, String> pair : pairs) {
            if (builder.length() > 0) {
                builder.append('&');
            }
            builder.append(pair.first)
                    .append('=')
                    .append(urlEncode(pair.second));
        }
        return builder.toString();
    }

    public static String urlEncode(String string) throws UnsupportedEncodingException {
        return URLEncoder.encode(string, UTF8_ENCODING).replace("+", "%20");
    }

    public static String streamToString(InputStream inputStream) throws IOException {
        return new String(streamToArray(inputStream), HttpUtil.UTF8_ENCODING);
    }

    public static byte[] streamToArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, read);
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static void closeSafely(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }
}
