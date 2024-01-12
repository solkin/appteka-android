package com.tomclaw.appsend.core;

import com.tomclaw.appsend.util.HttpParamsBuilder;
import com.tomclaw.appsend.util.HttpUtil;
import com.tomclaw.appsend.util.LegacyLogger;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public abstract class HttpTask extends Task {

    private final String host;
    private final HttpParamsBuilder builder;

    protected HttpTask(String host, HttpParamsBuilder builder) {
        this.host = host;
        this.builder = builder;
    }

    @Override
    public void executeBackground() throws Throwable {
        HttpURLConnection connection = null;
        InputStream in = null;
        try {
            String storeUrl = host + "?" + builder.build();
            LegacyLogger.log(String.format("Store url: %s", storeUrl));
            URL url = new URL(storeUrl);
            connection = (HttpURLConnection) url.openConnection();
            // Executing request.
            connection.setReadTimeout((int) TimeUnit.MINUTES.toMillis(2));
            connection.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(30));
            connection.setRequestMethod(HttpUtil.GET);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.connect();
            // Open connection to response.
            int responseCode = connection.getResponseCode();
            // Checking for this is error stream.
            if (responseCode >= HttpUtil.SC_BAD_REQUEST) {
                in = connection.getErrorStream();
            } else {
                in = connection.getInputStream();
            }
            String result = HttpUtil.streamToString(in);
            LegacyLogger.log(result);
            JSONObject jsonObject = new JSONObject(result);
            int status = jsonObject.getInt("status");
            if (status == 200) {
                onLoaded(jsonObject);
            } else {
                throw new IOException("Store count loading error: " + status);
            }
        } catch (Throwable ex) {
            LegacyLogger.log("Exception while count loading", ex);
            onError();
        } finally {
            // Trying to disconnect in any case.
            if (connection != null) {
                connection.disconnect();
            }
            HttpUtil.closeSafely(in);
        }
    }

    protected abstract void onLoaded(JSONObject jsonObject);

    protected abstract void onError();

}
