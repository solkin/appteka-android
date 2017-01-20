package com.tomclaw.appsend.main.controller;

import android.text.TextUtils;

import com.orhanobut.logger.Logger;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.main.item.CommonItem;
import com.tomclaw.appsend.main.task.ExportApkTask;
import com.tomclaw.appsend.util.HttpUtil;
import com.tomclaw.appsend.util.MultipartStream;
import com.tomclaw.appsend.util.StringUtil;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.HttpStatus;

/**
 * Created by ivsolkin on 02.01.17.
 * Control application file uploading
 */
public class UploadController extends AbstractController<UploadController.UploadCallback> {

    private static class Holder {

        static UploadController instance = new UploadController();
    }

    public static UploadController getInstance() {
        return Holder.instance;
    }

    private static final String HOST_URL = "http://appsend.store/api/upload.php";

    private CommonItem item;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private Future<?> future;

    private int lastPercent = 0;
    private boolean isUploaded = false;
    private String url = null;
    private boolean isError = false;

    public boolean isCompleted() {
        return !TextUtils.isEmpty(url);
    }

    @Override
    public void onAttached(UploadCallback callback) {
        if (item != null) {
            if (isCompleted()) {
                callback.onCompleted(url);
            } else if (isUploaded) {
                callback.onUploaded();
            } else if (isError) {
                callback.onError();
            } else {
                callback.onProgress(lastPercent);
            }
        }
    }

    @Override
    public void onDetached(UploadCallback callback) {
    }

    public void upload(CommonItem item) {
        this.item = item;
        this.lastPercent = 0;
        this.isUploaded = false;
        this.url = null;
        this.isError = false;
        future = executor.submit(new Runnable() {
            @Override
            public void run() {
                uploadInternal();
            }
        });
    }

    public void cancel() {
        detachAll();
        future.cancel(true);
        this.item = null;
        this.lastPercent = 0;
        this.isUploaded = false;
        this.url = null;
        this.isError = false;
    }

    private void onPercent(final int percent) {
        this.lastPercent = percent;
        MainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                operateCallbacks(new CallbackOperation<UploadCallback>() {
                    @Override
                    public void invoke(UploadCallback callback) {
                        callback.onProgress(percent);
                    }
                });
            }
        });
    }

    private void onUploaded() {
        this.isUploaded = true;
        MainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                operateCallbacks(new CallbackOperation<UploadCallback>() {
                    @Override
                    public void invoke(UploadCallback callback) {
                        callback.onUploaded();
                    }
                });
            }
        });
    }

    private void onCompleted(final String url) {
        this.url = url;
        MainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                operateCallbacks(new CallbackOperation<UploadCallback>() {
                    @Override
                    public void invoke(UploadCallback callback) {
                        callback.onCompleted(url);
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
                operateCallbacks(new CallbackOperation<UploadCallback>() {
                    @Override
                    public void invoke(UploadCallback callback) {
                        callback.onError();
                    }
                });
            }
        });
    }

    private void uploadInternal() {
        File file = new File(item.getPath());
        final long size = file.length();
        String name = ExportApkTask.getApkName(item);
        MultipartStream.ProgressHandler handler = new MultipartStream.ProgressHandler() {
            @Override
            public void onProgress(long sent) {
                final int percent = size > 0 ? (int) (100 * sent / size) : 0;
                onPercent(percent);
            }

            @Override
            public void onError() {
                UploadController.this.onError();
            }
        };
        String boundary = StringUtil.generateBoundary();
        HttpURLConnection connection = null;
        InputStream in = null;
        try {
            InputStream inputStream = new FileInputStream(file);
            URL url = new URL(HOST_URL);
            connection = (HttpURLConnection) url.openConnection();
            // Connect.
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            connection.setReadTimeout((int) TimeUnit.MINUTES.toMillis(2));
            connection.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(30));
            connection.setRequestMethod(HttpUtil.POST);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setChunkedStreamingMode(256);
            connection.connect();
            // Write data into output stream.
            OutputStream outputStream = connection.getOutputStream();
            MultipartStream multipartStream = new MultipartStream(outputStream, boundary);
            multipartStream.writePart("v", "1");
            multipartStream.writePart("apk_file", name, inputStream, "application/vnd.android.package-archive", handler);
            multipartStream.writeLastBoundaryIfNeeds();
            multipartStream.flush();
            onUploaded();
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
                    String appId = jsonObject.getString("app_id");
                    String location = jsonObject.getString("url");
                    onCompleted(location);
                    break;
                }
                default: {
                    throw new IOException("File upload error");
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

    public interface UploadCallback extends AbstractController.ControllerCallback {

        void onProgress(int percent);

        void onUploaded();

        void onCompleted(String url);

        void onError();

    }
}
