package com.tomclaw.appsend.main.upload;

import android.text.TextUtils;

import com.orhanobut.logger.Logger;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.main.controller.AbstractController;
import com.tomclaw.appsend.main.item.CommonItem;
import com.tomclaw.appsend.main.task.ExportApkTask;
import com.tomclaw.appsend.net.Session;
import com.tomclaw.appsend.util.HttpUtil;
import com.tomclaw.appsend.util.MultipartStream;
import com.tomclaw.appsend.util.PackageHelper;
import com.tomclaw.appsend.util.StringUtil;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
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

import static com.tomclaw.appsend.AppSend.app;
import static com.tomclaw.appsend.core.Config.HOST_URL;

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

    private static final String HOST_UPLOAD_URL = HOST_URL + "/api/upload.php";

    private CommonItem item;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private Future<?> future;

    private int lastPercent = 0;
    private boolean isUploaded = false;
    private String url = null;
    private String appId = null;
    private boolean isError = false;

    public boolean isCompleted() {
        return !TextUtils.isEmpty(appId) && !TextUtils.isEmpty(url);
    }

    @Override
    public void onAttached(UploadCallback callback) {
        if (item != null) {
            if (isCompleted()) {
                callback.onCompleted(appId, url);
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
        this.appId = null;
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
        this.appId = null;
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

    private void onCompleted(final String appId, final String url) {
        this.appId = appId;
        this.url = url;
        MainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                operateCallbacks(new CallbackOperation<UploadCallback>() {
                    @Override
                    public void invoke(UploadCallback callback) {
                        callback.onCompleted(appId, url);
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
        File apk = new File(item.getPath());
        byte[] icon = PackageHelper.getPackageIconPng(
                item.getPackageInfo().applicationInfo,
                app().getPackageManager()
        );
        final long size = apk.length() + icon.length;
        String apkName = ExportApkTask.getApkName(item);
        String iconName = ExportApkTask.getIconName(item);
        String guid = null;
        if (Session.getInstance().getUserData().isRegistered()) {
            guid = Session.getInstance().getUserData().getGuid();
        }
        MultipartStream.ProgressHandler emptyHandler = new MultipartStream.ProgressHandler() {
            @Override
            public void onProgress(long sent) {
            }

            @Override
            public void onError() {
            }
        };
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
        InputStream apkStream = null;
        InputStream iconStream = null;
        try {
            apkStream = new FileInputStream(apk);
            iconStream = new ByteArrayInputStream(icon);
            URL url = new URL(HOST_UPLOAD_URL);
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
            if (!TextUtils.isEmpty(guid)) {
                multipartStream.writePart("guid", guid);
            }
            multipartStream.writePart("icon_file", iconName, iconStream, "image/png", emptyHandler);
            multipartStream.writePart("apk_file", apkName, apkStream, "application/vnd.android.package-archive", handler);
            multipartStream.writeLastBoundaryIfNeeds();
            multipartStream.flush();
            onUploaded();
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
                    String appId = jsonObject.getString("app_id");
                    String location = jsonObject.getString("url");
                    onCompleted(appId, location);
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
            HttpUtil.closeSafely(apkStream);
            HttpUtil.closeSafely(iconStream);
            HttpUtil.closeSafely(in);
        }
    }

    public interface UploadCallback extends AbstractController.ControllerCallback {

        void onProgress(int percent);

        void onUploaded();

        void onCompleted(String appId, String url);

        void onError();

    }
}
