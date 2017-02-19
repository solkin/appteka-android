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

public class AbuseController extends AbstractController<AbuseController.AbuseCallback> {

    private static class Holder {

        static AbuseController instance = new AbuseController();
    }

    public static AbuseController getInstance() {
        return Holder.instance;
    }

    private static final String HOST_ABUSE_URL = "http://appsend.store/api/abuse.php";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private boolean isAbuseSend = false;
    private boolean isError = false;
    private boolean isCancelled = false;

    private Future<?> future;

    @Override
    void onAttached(AbuseCallback callback) {
        if (isAbuseSent()) {
            callback.onAbuseSent();
        } else if (isError) {
            callback.onError();
        } else if (isStarted()) {
            callback.onProgress();
        } else {
            callback.onReady();
        }
    }

    public boolean isAbuseSent() {
        return isAbuseSend;
    }

    public boolean isStarted() {
        return future != null;
    }

    @Override
    void onDetached(AbuseCallback callback) {
    }

    private void onProgress() {
        if (isCancelled) {
            return;
        }
        MainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                operateCallbacks(new CallbackOperation<AbuseCallback>() {
                    @Override
                    public void invoke(AbuseCallback callback) {
                        callback.onProgress();
                    }
                });
            }
        });
    }

    private void onAbuseSent() {
        isAbuseSend = true;
        future = null;
        isCancelled = false;
        MainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                operateCallbacks(new CallbackOperation<AbuseCallback>() {
                    @Override
                    public void invoke(AbuseCallback callback) {
                        callback.onAbuseSent();
                    }
                });
            }
        });
    }

    private void onError() {
        if (isCancelled) {
            return;
        }
        isError = true;
        future = null;
        MainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                operateCallbacks(new CallbackOperation<AbuseCallback>() {
                    @Override
                    public void invoke(AbuseCallback callback) {
                        callback.onError();
                    }
                });
            }
        });
    }

    public void abuse(final String appId, final String reason, final String email) {
        isAbuseSend = false;
        isError = false;
        future = executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    isCancelled = false;
                    abuseInternal(appId, reason, email);
                } catch (Throwable ignored) {
                    onError();
                }
            }
        });
    }

    public void resetAbuse() {
        isAbuseSend = false;
        isError = false;
        isCancelled = false;
        future = null;
    }

    public void cancelAbuse() {
        if (future != null) {
            isCancelled = true;
            future.cancel(true);
        }
    }

    private void abuseInternal(String appId, String reason, String email) {
        onProgress();
        HttpURLConnection connection = null;
        InputStream in = null;
        try {
            HttpParamsBuilder builder = new HttpParamsBuilder()
                    .appendParam("v", "1")
                    .appendParam("app_id", appId)
                    .appendParam("reason", reason)
                    .appendParam("email", email);
            String storeUrl = HOST_ABUSE_URL + "?" + builder.build();
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
                    onAbuseSent();
                    break;
                }
                default: {
                    throw new IOException("Store count loading error: " + status);
                }
            }
        } catch (Throwable ex) {
            Logger.e(ex, "Exception while application uploading");
            onError();
        } finally {
            // Trying to disconnect in any case.
            if (connection != null) {
                connection.disconnect();
            }
            HttpUtil.closeSafely(in);
        }
    }

    public interface AbuseCallback extends AbstractController.ControllerCallback {

        void onReady();

        void onProgress();

        void onAbuseSent();

        void onError();

    }
}
