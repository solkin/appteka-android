package com.tomclaw.appsend;

import android.util.Pair;

import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import cz.msebera.android.httpclient.HttpStatus;

/**
 * Created with IntelliJ IDEA.
 * User: Solkin
 * Date: 04.11.13
 * Time: 14:40
 */
public class HttpUtil {

    public static final String GET = "GET";
    public static final String POST = "POST";

    private static final int TIMEOUT_SOCKET = 70 * 1000;
    private static final int TIMEOUT_CONNECTION = 60 * 1000;

    public static final String UTF8_ENCODING = "UTF-8";

    private static final String HASH_ALGORITHM = "MD5";
    private static final int RADIX = 10 + 26; // 10 digits + 26 letters

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

    public static String getUrlHash(String url) {
        byte[] md5 = getMD5(url.getBytes());
        BigInteger bi = new BigInteger(md5).abs();
        return bi.toString(RADIX);
    }

    private static byte[] getMD5(byte[] data) {
        byte[] hash = null;
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            digest.update(data);
            hash = digest.digest();
        } catch (NoSuchAlgorithmException ignored) {
        }
        return hash;
    }

    public static void writeStringToConnection(HttpURLConnection connection, String data) throws IOException {
        OutputStream outputStream = connection.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        writer.write(data);
        writer.flush();
        writer.close();
    }

    public static String readStringFromConnection(HttpURLConnection connection) throws IOException {
        InputStream in = connection.getInputStream();
        String response = streamToString(in);
        in.close();
        return response;
    }

    public static InputStream executePost(HttpURLConnection connection, String data) throws IOException {
        connection.setRequestMethod(POST);
        connection.setDoOutput(true);
        connection.setDoInput(true);

        HttpUtil.writeStringToConnection(connection, data);
        // Open connection to response.
        connection.connect();

        return getResponse(connection);
    }

    public static String executePost(String urlString, HttpParamsBuilder params) throws IOException {
        InputStream responseStream = null;
        HttpURLConnection connection = null;
        try {
            // Create and config connection.
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(TIMEOUT_CONNECTION);
            connection.setReadTimeout(TIMEOUT_SOCKET);

            // Execute request.
            responseStream = HttpUtil.executePost(connection, params.build());
            return HttpUtil.streamToString(responseStream);
        } catch (IOException ex) {
            throw new IOException(ex);
        } finally {
            try {
                if (responseStream != null) {
                    responseStream.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (IOException ignored) {
            }
        }
    }

    public static InputStream executeGet(HttpURLConnection connection) throws IOException {
        connection.setRequestMethod(GET);
        connection.setDoInput(true);
        connection.setDoOutput(false);

        return getResponse(connection);
    }

    private static InputStream getResponse(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        InputStream in;
        // Checking for this is error stream.
        if (responseCode >= HttpStatus.SC_BAD_REQUEST) {
            return connection.getErrorStream();
        } else {
            return connection.getInputStream();
        }
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
}
