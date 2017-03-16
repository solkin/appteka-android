package com.tomclaw.appsend.main.controller;

import com.orhanobut.logger.Logger;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.main.dto.StoreInfo;
import com.tomclaw.appsend.main.dto.StoreVersion;
import com.tomclaw.appsend.main.item.StoreItem;
import com.tomclaw.appsend.util.HttpParamsBuilder;
import com.tomclaw.appsend.util.HttpUtil;
import com.tomclaw.appsend.util.VariableBuffer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    private long downloadedBytes;
    private boolean isDownloading;
    private boolean isDownloadError;
    private boolean isDownloadCancelled;

    private Future<?> future;

    @Override
    void onAttached(DownloadCallback callback) {
        actuateCallback(callback);
    }

    private void actuateCallback(DownloadCallback callback) {
        if (isInfoLoaded()) {
            callback.onInfoLoaded(storeInfo);
        } else if (isInfoError) {
            callback.onInfoError();
        } else {
            callback.onInfoProgress();
        }
        if (isDownloadError) {
            callback.onDownloadError();
        } else if (isDownloading) {
            callback.onDownloadStarted();
            callback.onDownloadProgress(downloadedBytes);
        }
    }

    public boolean isInfoLoaded() {
        return storeInfo != null;
    }

    public boolean isStarted() {
        return future != null;
    }

    public boolean isDownloading() {
        return isDownloading;
    }

    @Override
    void onDetached(DownloadCallback callback) {
    }

    public void loadInfo(final String appId) {
        storeInfo = null;
        isInfoError = false;
        isDownloading = false;
        isDownloadError = false;
        downloadedBytes = 0;
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
        future = null;
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
        future = null;
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

    public void download(final String link, final String filePath) {
        isDownloading = true;
        isDownloadError = false;
        downloadedBytes = 0;
        future = executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    isDownloadCancelled = false;
                    downloadInternal(link, filePath);
                } catch (Throwable ignored) {
                    onInfoError();
                }
            }
        });
    }

    public void cancelDownload() {
        if (future != null & isDownloading) {
            isDownloadCancelled = true;
            future.cancel(true);
            future = null;
        }
    }

    private void onDownloadStarted() {
        if (isDownloadCancelled) {
            return;
        }
        isDownloading = true;
        isDownloadError = false;
        MainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                operateCallbacks(new CallbackOperation<DownloadCallback>() {
                    @Override
                    public void invoke(DownloadCallback callback) {
                        callback.onDownloadStarted();
                    }
                });
            }
        });
    }

    private void onDownloadProgress(final long downloadedBytes) {
        if (isDownloadCancelled) {
            return;
        }
        this.downloadedBytes = downloadedBytes;
        MainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                operateCallbacks(new CallbackOperation<DownloadCallback>() {
                    @Override
                    public void invoke(DownloadCallback callback) {
                        callback.onDownloadProgress(downloadedBytes);
                    }
                });
            }
        });
    }

    private void onDownloaded(final String filePath) {
        isDownloading = false;
        isDownloadError = false;
        isDownloadCancelled = false;
        future = null;
        MainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                operateCallbacks(new CallbackOperation<DownloadCallback>() {
                    @Override
                    public void invoke(DownloadCallback callback) {
                        callback.onDownloaded(filePath);
                    }
                });
            }
        });
    }

    private void onDownloadError() {
        isDownloading = false;
        if (!isDownloadCancelled) {
            isDownloadError = true;
        }
        future = null;
        MainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                operateCallbacks(new CallbackOperation<DownloadCallback>() {
                    @Override
                    public void invoke(DownloadCallback callback) {
                        if (isDownloadCancelled) {
                            actuateCallback(callback);
                        } else {
                            callback.onDownloadError();
                        }
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
                    String webUrl = jsonObject.getString("url");
                    List<StoreVersion> storeVersions = new ArrayList<>();
                    JSONArray versions = jsonObject.getJSONArray("versions");
                    for (int c = 0; c < versions.length(); c++) {
                        JSONObject version = versions.getJSONObject(c);
                        StoreVersion storeVersion = parseStoreVersion(version);
                        storeVersions.add(storeVersion);
                    }
                    JSONObject info = jsonObject.getJSONObject("info");
                    StoreItem storeItem = parseStoreItem(info);
                    StoreInfo storeInfo = new StoreInfo(expiresIn, storeItem, link, webUrl, status, storeVersions);
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

    private void downloadInternal(String link, String filePath) {
        onDownloadStarted();
        HttpURLConnection connection = null;
        InputStream in = null;
        OutputStream out = null;
        try {
            Logger.d("Download app url: %s", link);
            URL url = new URL(link);
            connection = (HttpURLConnection) url.openConnection();
            // Executing request.
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
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
            out = new FileOutputStream(file);

            VariableBuffer buffer = new VariableBuffer();
            int cache;
            long read = 0;
            buffer.onExecuteStart();
            while ((cache = in.read(buffer.calculateBuffer())) != -1) {
                buffer.onExecuteCompleted(cache);
                out.write(buffer.getBuffer(), 0, cache);
                out.flush();
                read += cache;
                onDownloadProgress(read);
                buffer.onExecuteStart();
            }

            onDownloaded(filePath);
        } catch (Throwable ex) {
            Logger.e(ex, "Exception while application uploading");
            onDownloadError();
        } finally {
            // Trying to disconnect in any case.
            if (connection != null) {
                connection.disconnect();
            }
            HttpUtil.closeSafely(in);
            HttpUtil.closeSafely(out);
        }
    }

    public interface DownloadCallback extends AbstractController.ControllerCallback {

        void onInfoLoaded(StoreInfo storeInfo);

        void onInfoError();

        void onInfoProgress();

        void onDownloadStarted();

        void onDownloadProgress(long downloadedBytes);

        void onDownloaded(String filePath);

        void onDownloadError();
    }
}
