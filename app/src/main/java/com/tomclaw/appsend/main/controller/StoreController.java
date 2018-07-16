package com.tomclaw.appsend.main.controller;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.orhanobut.logger.Logger;
import com.tomclaw.appsend.BuildConfig;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.main.item.BaseItem;
import com.tomclaw.appsend.main.item.StoreItem;
import com.tomclaw.appsend.util.HttpParamsBuilder;
import com.tomclaw.appsend.util.HttpUtil;
import com.tomclaw.appsend.util.PreferenceHelper;

import org.androidannotations.annotations.EBean;
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

import static com.tomclaw.appsend.core.Config.HOST_URL;
import static com.tomclaw.appsend.util.PackageHelper.getInstalledVersionCode;
import static com.tomclaw.appsend.util.StoreHelper.parseStoreItem;

/**
 * Created by ivsolkin on 11.01.17.
 */
@EBean
public class StoreController extends AbstractController<StoreController.StoreCallback> {

    private static class Holder {

        static StoreController instance = new StoreController();
    }

    public static StoreController getInstance() {
        return Holder.instance;
    }

    private static final String HOST_LIST_URL = HOST_URL + "/api/list.php";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private List<BaseItem> list;
    private boolean isError = false;
    private String loadAppId = null;
    private boolean endReached = false;

    private Future<?> future;

    private PackageManager packageManager;

    @Override
    protected void onAttached(final StoreCallback callback) {
        if (isLoaded()) {
            if (packageManager != null) {
                callback.onProgress(false);
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        updateItemsInstalledVersions(packageManager, list);
                        MainExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                callback.onLoaded(list);
                            }
                        });
                    }
                });
            } else {
                callback.onLoaded(list);
            }
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
        reload(context, null);
    }

    public void reload(@NonNull Context context, @Nullable String filter) {
        load(context, "", filter);
    }

    public boolean load(final @NonNull Context context,
                        final @Nullable String appId,
                        final @Nullable String filter) {
        this.packageManager = context.getPackageManager();
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
                    loadInternal(context, appId, filter);
                } catch (Throwable ignored) {
                    onError(isAppend());
                }
            }
        });
        return true;
    }

    @Override
    protected void onDetached(StoreCallback callback) {
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

    private void loadInternal(@NonNull Context context, @Nullable String appId, @Nullable String filter) {
        boolean isAppend = !TextUtils.isEmpty(appId);
        boolean isFilter = !TextUtils.isEmpty(filter);
        int build = BuildConfig.VERSION_CODE;
        onProgress(isAppend);
        HttpURLConnection connection = null;
        InputStream in = null;
        try {
            HttpParamsBuilder builder = new HttpParamsBuilder()
                    .appendParam("v", "1");
            if (isAppend) {
                builder.appendParam("app_id", appId);
            }
            if (isFilter) {
                builder.appendParam("filter", filter);
            }
            builder.appendParam("ver_code", build);
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
            if (responseCode >= HttpUtil.SC_BAD_REQUEST) {
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
                    Long time = null;
                    for (int c = 0; c < files.length(); c++) {
                        JSONObject file = files.getJSONObject(c);
                        StoreItem item = parseStoreItem(file);
                        if (c == 0) {
                            time = item.getTime();
                        }
                        item.setInstalledVersionCode(getInstalledVersionCode(
                                item.getPackageName(), packageManager));
                        baseItems.add(item);
                    }
                    onLoaded(baseItems, isAppend);
                    if (!isAppend && !isFilter && time != null) {
                        PreferenceHelper.setCountTime(context, time);
                    }
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

    private static void updateItemsInstalledVersions(PackageManager packageManager,
                                                     List<BaseItem> items) {
        for (BaseItem item : items) {
            if (item.getType() == BaseItem.STORE_ITEM) {
                StoreItem storeItem = (StoreItem) item;
                storeItem.setInstalledVersionCode(getInstalledVersionCode(
                        storeItem.getPackageName(), packageManager));
            }
        }
    }

    public interface StoreCallback extends AbstractController.ControllerCallback {

        void onLoaded(List<BaseItem> list);

        void onError(boolean isAppend);

        void onProgress(boolean isAppend);
    }
}
