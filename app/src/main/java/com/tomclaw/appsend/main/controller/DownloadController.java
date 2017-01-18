package com.tomclaw.appsend.main.controller;

import com.orhanobut.logger.Logger;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.main.dto.StoreInfo;
import com.tomclaw.appsend.main.dto.StoreVersion;
import com.tomclaw.appsend.main.item.StoreItem;
import com.tomclaw.appsend.util.HttpParamsBuilder;
import com.tomclaw.appsend.util.HttpUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.HttpStatus;

import static com.tomclaw.appsend.util.StoreHelper.parseStoreItem;
import static com.tomclaw.appsend.util.StoreHelper.parseStoreVersion;

/**
 * Created by ivsolkin on 17.01.17.
 */
public class DownloadController extends AbstractController<DownloadController.DownloadCallback> {

    private static class Holder {

        static DownloadController instance = new DownloadController();
    }

    public static DownloadController getInstance() {
        return Holder.instance;
    }

    private static final String HOST_INFO_URL = "http://appsend.store/api/info.php";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private StoreInfo storeInfo = null;
    private boolean isInfoError = false;

    private Future<?> future;

    @Override
    void onAttached(DownloadCallback callback) {
        if (isInfoLoaded()) {
            callback.onInfoLoaded(storeInfo);
        } else if (isInfoError) {
            callback.onInfoError();
        } else {
            callback.onInfoProgress();
        }
    }

    public boolean isInfoLoaded() {
        return storeInfo != null;
    }

    public boolean isStarted() {
        return future != null;
    }

    @Override
    void onDetached(DownloadCallback callback) {
    }

    public void loadInfo(final String appId) {
        storeInfo = null;
        isInfoError = false;
        future = executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    loadInfoInternal(appId);
                } catch (Throwable ignored) {
                    onInfoError();
                }
            }
        });
    }

    private void onProgress() {
        MainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                operateCallbacks(new CallbackOperation<DownloadCallback>() {
                    @Override
                    public void invoke(DownloadCallback callback) {
                        callback.onInfoProgress();
                    }
                });
            }
        });
    }

    private void onInfoLoaded(final StoreInfo storeInfo) {
        this.storeInfo = storeInfo;
        MainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                operateCallbacks(new CallbackOperation<DownloadCallback>() {
                    @Override
                    public void invoke(DownloadCallback callback) {
                        callback.onInfoLoaded(storeInfo);
                    }
                });
            }
        });
    }

    private void onInfoError() {
        this.isInfoError = true;
        MainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                operateCallbacks(new CallbackOperation<DownloadCallback>() {
                    @Override
                    public void invoke(DownloadCallback callback) {
                        callback.onInfoError();
                    }
                });
            }
        });
    }

    private void loadInfoInternal(String appId) {
        onProgress();
        HttpURLConnection connection = null;
        InputStream in = null;
        try {
            HttpParamsBuilder builder = new HttpParamsBuilder();
            builder.appendParam("v", "1");
            builder.appendParam("app_id", appId);
            String storeUrl = HOST_INFO_URL + "?" + builder.build();
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
                    long expiresIn = jsonObject.getLong("expires_in");
                    String link = jsonObject.getString("link");
                    List<StoreVersion> storeVersions = new ArrayList<>();
                    JSONArray versions = jsonObject.getJSONArray("versions");
                    for (int c = 0; c < versions.length(); c++) {
                        JSONObject version = versions.getJSONObject(c);
                        StoreVersion storeVersion = parseStoreVersion(version);
                        storeVersions.add(storeVersion);
                    }
                    JSONObject info = jsonObject.getJSONObject("info");
                    StoreItem storeItem = parseStoreItem(info);
                    StoreInfo storeInfo = new StoreInfo(expiresIn, storeItem, link, status, storeVersions);
                    onInfoLoaded(storeInfo);
                    break;
                }
                default: {
                    throw new IOException("Store files loading error: " + status);
                }
            }
        } catch (Throwable ex) {
            Logger.e(ex, "Exception while application uploading");
            onInfoError();
        } finally {
            // Trying to disconnect in any case.
            if (connection != null) {
                connection.disconnect();
            }
            HttpUtil.closeSafely(in);
        }
    }

    public interface DownloadCallback extends AbstractController.ControllerCallback {

        void onInfoLoaded(StoreInfo storeInfo);

        void onInfoError();

        void onInfoProgress();

    }
}
