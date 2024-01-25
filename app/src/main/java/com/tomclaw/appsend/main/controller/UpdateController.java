package com.tomclaw.appsend.main.controller;

import static com.tomclaw.appsend.Appteka.app;
import static com.tomclaw.appsend.core.Config.HOST_URL;
import static com.tomclaw.appsend.util.StoreHelper.parseStoreItem;

import android.content.Context;

import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.main.item.StoreItem;
import com.tomclaw.appsend.util.HttpParamsBuilder;
import com.tomclaw.appsend.util.HttpUtil;
import com.tomclaw.appsend.util.LegacyLogger;
import com.tomclaw.appsend.util.PackageHelper;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by solkin on 19.02.17.
 */
public class UpdateController extends AbstractController<UpdateController.UpdateCallback> {

    private static class Holder {

        static UpdateController instance = new UpdateController();
    }

    public static UpdateController getInstance() {
        return Holder.instance;
    }

    private static final String HOST_UPDATE_URL = HOST_URL + "/api/1/update";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private StoreItem item;

    private Future<?> future;

    @Override
    protected void onAttached(UpdateCallback callback) {
        if (isUpdateAvailable()) {
            callback.onUpdateAvailable(item);
        }
    }

    private boolean isUpdateAvailable() {
        return item != null;
    }

    public boolean isStarted() {
        return future != null;
    }

    public StoreItem getStoreItem() {
        return item;
    }

    @Override
    protected void onDetached(UpdateCallback callback) {
    }

    private void onProgress() {
    }

    private void onLoaded(final StoreItem item) {
        this.item = item;
        MainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                operateCallbacks(new CallbackOperation<UpdateCallback>() {
                    @Override
                    public void invoke(UpdateCallback callback) {
                        callback.onUpdateAvailable(item);
                    }
                });
            }
        });
    }

    private void onError() {
    }

    public void load(final Context context) {
        item = null;
        future = executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    int build = PackageHelper.getInstalledVersionCode(
                            context.getPackageName(), context.getPackageManager());
                    loadInternal(build);
                } catch (Throwable ignored) {
                    onError();
                }
            }
        });
    }

    public boolean resetUpdateFlag() {
        boolean hasUpdate = false;
        if (isUpdateAvailable()) {
            hasUpdate = item != null;
            item = null;
        }
        return hasUpdate;
    }

    private void loadInternal(int build) {
        onProgress();
        HttpURLConnection connection = null;
        InputStream in = null;
        try {
            HttpParamsBuilder builder = new HttpParamsBuilder()
                    .appendParam("build", String.valueOf(build))
                    .appendParam("inst_id", app().getInstallationID());
            String storeUrl = HOST_UPDATE_URL + "?" + builder.build();
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
                if (jsonObject.has("newer")) {
                    JSONObject newer = jsonObject.getJSONObject("newer");
                    StoreItem storeItem = parseStoreItem(newer);
                    onLoaded(storeItem);
                }
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

    public interface UpdateCallback extends AbstractController.ControllerCallback {

        void onUpdateAvailable(StoreItem item);

    }
}
