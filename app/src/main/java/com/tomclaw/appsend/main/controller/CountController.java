package com.tomclaw.appsend.main.controller;

import android.content.Context;

import com.orhanobut.logger.Logger;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.util.HttpParamsBuilder;
import com.tomclaw.appsend.util.HttpUtil;
import com.tomclaw.appsend.util.PreferenceHelper;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.HttpStatus;

/**
 * Created by solkin on 19.02.17.
 */
public class CountController extends AbstractController<CountController.CountCallback> {

    private static class Holder {

        static CountController instance = new CountController();
    }

    public static CountController getInstance() {
        return Holder.instance;
    }

    private static final String HOST_COUNT_URL = "http://appsend.store/api/count.php";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private Integer count;
    private boolean isError = false;

    private Future<?> future;

    @Override
    void onAttached(CountCallback callback) {
        if (isLoaded()) {
            callback.onLoaded(count);
        } else if (isError) {
            callback.onError();
        } else {
            callback.onProgress();
        }
    }

    private boolean isLoaded() {
        return count != null;
    }

    public boolean isStarted() {
        return future != null;
    }

    @Override
    void onDetached(CountCallback callback) {
    }

    private void onProgress() {
        MainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                operateCallbacks(new CallbackOperation<CountCallback>() {
                    @Override
                    public void invoke(CountCallback callback) {
                        callback.onProgress();
                    }
                });
            }
        });
    }

    private void onLoaded(final int count) {
        this.count = count;
        MainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                operateCallbacks(new CallbackOperation<CountCallback>() {
                    @Override
                    public void invoke(CountCallback callback) {
                        callback.onLoaded(count);
                    }
                });
            }
        });
    }

    private void onError() {
        this.isError = true;
        MainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                operateCallbacks(new CallbackOperation<CountCallback>() {
                    @Override
                    public void invoke(CountCallback callback) {
                        callback.onError();
                    }
                });
            }
        });
    }

    public void load(final Context context) {
        count = null;
        isError = false;
        future = executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    long time = PreferenceHelper.getCountTime(context);
                    loadInternal(time);
                } catch (Throwable ignored) {
                    onError();
                }
            }
        });
    }

    public boolean resetCount() {
        boolean hasCount = false;
        if (isLoaded()) {
            hasCount = count > 0;
            count = 0;
            onLoaded(count);
        }
        return hasCount;
    }

    private void loadInternal(long time) {
        onProgress();
        HttpURLConnection connection = null;
        InputStream in = null;
        try {
            HttpParamsBuilder builder = new HttpParamsBuilder()
                    .appendParam("v", "1")
                    .appendParam("time", String.valueOf(time));
            String storeUrl = HOST_COUNT_URL + "?" + builder.build();
            Logger.d("Store url: %s", storeUrl);
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
            if (responseCode >= HttpStatus.SC_BAD_REQUEST) {
                in = connection.getErrorStream();
            } else {
                in = connection.getInputStream();
            }
            String result = HttpUtil.streamToString(in);
            Logger.json(result);
            JSONObject jsonObject = new JSONObject(result);
            int status = jsonObject.getInt("status");
            switch (status) {
                case 200: {
                    int count = jsonObject.getInt("count");
                    onLoaded(count);
                    break;
                }
                default: {
                    throw new IOException("Store count loading error: " + status);
                }
            }
        } catch (Throwable ex) {
            Logger.e(ex, "Exception while count loading");
            onError();
        } finally {
            // Trying to disconnect in any case.
            if (connection != null) {
                connection.disconnect();
            }
            HttpUtil.closeSafely(in);
        }
    }

    public interface CountCallback extends AbstractController.ControllerCallback {

        void onProgress();

        void onLoaded(int count);

        void onError();

    }
}
