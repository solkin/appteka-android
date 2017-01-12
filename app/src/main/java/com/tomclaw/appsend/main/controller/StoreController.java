package com.tomclaw.appsend.main.controller;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.orhanobut.logger.Logger;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.main.item.BaseItem;
import com.tomclaw.appsend.main.item.StoreItem;
import com.tomclaw.appsend.util.HttpParamsBuilder;
import com.tomclaw.appsend.util.HttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.HttpStatus;

/**
 * Created by ivsolkin on 11.01.17.
 */
public class StoreController extends AbstractController<StoreController.StoreCallback> {

    private static class Holder {

        static StoreController instance = new StoreController();
    }

    public static StoreController getInstance() {
        return Holder.instance;
    }

    private static final String HOST_LIST_URL = "http://appsend.store/api/list.php";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private List<BaseItem> list;
    private boolean isError = false;
    private String loadAppId = null;
    private boolean endReached = false;

    private Future<?> future;

    @Override
    void onAttached(StoreCallback callback) {
        if (isLoaded()) {
            callback.onLoaded(list);
        } else if (isError) {
            callback.onError(isAppend());
        } else {
            callback.onProgress(isAppend());
        }
    }

    public boolean isLoaded() {
        return list != null;
    }

    public boolean isAppend() {
        return !TextUtils.isEmpty(loadAppId);
    }

    public boolean isError() {
        return isError;
    }

    public boolean isStarted() {
        return future != null;
    }

    public void reload(@NonNull Context context) {
        load(context, "");
    }

    public boolean load(final @NonNull Context context, @Nullable final String appId) {
        boolean isReload = TextUtils.equals(appId, "");
        if (endReached && !isReload) {
            // End is reached, but this request is not reload request.
            return false;
        }
        if (TextUtils.equals(appId, loadAppId) && !isReload && !isError) {
            // We are already loading such request.
            return false;
        }
        loadAppId = appId;
        isError = false;
        future = executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    loadInternal(context, appId);
                } catch (Throwable ignored) {
                    onError(isAppend());
                }
            }
        });
        return true;
    }

    @Override
    void onDetached(StoreCallback callback) {
    }

    private void onProgress(final boolean isAppend) {
        MainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                operateCallbacks(new CallbackOperation<StoreCallback>() {
                    @Override
                    public void invoke(StoreCallback callback) {
                        callback.onProgress(isAppend);
                    }
                });
            }
        });
    }

    private void onLoaded(final List<BaseItem> items, boolean isAppend) {
        endReached = items.isEmpty();
        if (isAppend && this.list != null) {
            this.list.addAll(items);
        } else {
            this.list = items;
        }
        MainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                operateCallbacks(new CallbackOperation<StoreCallback>() {
                    @Override
                    public void invoke(StoreCallback callback) {
                        callback.onLoaded(list);
                    }
                });
            }
        });
    }

    private void onError(final boolean isAppend) {
        isError = true;
        MainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                operateCallbacks(new CallbackOperation<StoreCallback>() {
                    @Override
                    public void invoke(StoreCallback callback) {
                        callback.onError(isAppend);
                    }
                });
            }
        });
    }

    private void loadInternal(@NonNull Context context, @Nullable String appId) {
        boolean isAppend = !TextUtils.isEmpty(appId);
        onProgress(isAppend);
        HttpURLConnection connection = null;
        InputStream in = null;
        try {
            HttpParamsBuilder builder = new HttpParamsBuilder();
            builder.appendParam("v", "1");
            if (isAppend) {
                builder.appendParam("app_id", appId);
            }
            String storeUrl = HOST_LIST_URL + "?" + builder.build();
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
                    List<BaseItem> baseItems = new ArrayList<>();
                    JSONArray files = jsonObject.getJSONArray("files");
                    for (int c = 0; c < files.length(); c++) {
                        JSONObject file = files.getJSONObject(c);
                        StoreItem item = parseStoreItem(file);
                        baseItems.add(item);
                    }
                    onLoaded(baseItems, isAppend);
                    break;
                }
                default: {
                    throw new IOException("Store files loading error: " + status);
                }
            }
        } catch (Throwable ex) {
            Logger.e(ex, "Exception while application uploading");
            onError(isAppend);
        } finally {
            // Trying to disconnect in any case.
            if (connection != null) {
                connection.disconnect();
            }
            HttpUtil.closeSafely(in);
        }
    }

    private StoreItem parseStoreItem(JSONObject file) throws JSONException {
        String appId = file.getString("app_id");
        String defLabel = file.getString("def_label");
        int downloads = file.getInt("downloads");
        String icon = file.getString("icon");
        long downloadTime = file.getLong("download_time") * 1000;
        Map<String, String> labels = parseStringMap(file.getJSONObject("labels"));
        String packageName = file.getString("package");
        List<String> permissions = parseStringList(file.getJSONArray("permissions"));
        int sdkVersion = file.getInt("sdk_version");
        String sha1 = file.getString("sha1");
        long size = file.getLong("size");
        long time = file.getLong("time") * 1000;
        int verCode = file.getInt("ver_code");
        String verName = file.getString("ver_name");
        return new StoreItem(defLabel, labels, icon, appId, packageName, verName, verCode,
                sdkVersion, permissions, size, downloads, downloadTime, time, sha1);
    }

    private Map<String, String> parseStringMap(JSONObject object) throws JSONException {
        Map<String, String> map = new HashMap<>();
        Iterator<String> keys = object.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            map.put(key, (String) object.get(key));
        }
        return map;
    }

    private List<String> parseStringList(JSONArray array) throws JSONException {
        List<String> list = new ArrayList<>();
        for (int c = 0; c < array.length(); c++) {
            list.add(array.getString(c));
        }
        return list;
    }

    public interface StoreCallback extends AbstractController.ControllerCallback {

        void onLoaded(List<BaseItem> list);

        void onError(boolean isAppend);

        void onProgress(boolean isAppend);
    }
}
