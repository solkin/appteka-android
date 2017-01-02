package com.tomclaw.appsend.main.task;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.tomclaw.appsend.AppInfo;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.core.WeakObjectTask;
import com.tomclaw.appsend.util.FileHelper;
import com.tomclaw.appsend.util.HttpUtil;
import com.tomclaw.appsend.util.MultipartStream;
import com.tomclaw.appsend.util.StringUtil;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.HttpStatus;

/**
 * Created by Igor on 06.05.2015.
 */
public class UploadApkTask extends WeakObjectTask<Activity> {

    private final AppInfo appInfo;

    private transient long progressUpdateTime = 0;

    private transient long DEBOUNCE_DELAY = 500;

    private ProgressDialog dialog;
    private String text;

    public UploadApkTask(Activity activity, AppInfo appInfo) {
        super(activity);
        this.appInfo = appInfo;
    }

    public boolean isPreExecuteRequired() {
        return true;
    }

    @Override
    public void onPreExecuteMain() {
        Activity activity = getWeakObject();
        if (activity != null) {
            dialog = new ProgressDialog(activity);
            // dialog.setTitle();
            dialog.setMessage(activity.getString(R.string.uploading_message));
            dialog.setCancelable(false);
            dialog.setMax(100);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.show();
        }
    }

    @Override
    public void executeBackground() throws Throwable {
        final Activity activity = getWeakObject();
        if (activity != null) {
            File file = new File(appInfo.getPath());
            Uri uri = Uri.fromFile(file);
            final long size = file.length();
            String sizeString = FileHelper.formatBytes(activity.getResources(), size);
            String name = ExportApkTask.getApkName(appInfo);
            MultipartStream.ProgressHandler handler = new MultipartStream.ProgressHandler() {
                @Override
                public void onProgress(long sent) {
                    final int progress = size > 0 ? (int) (70 * sent / size) : 0;
                    if (System.currentTimeMillis() - progressUpdateTime >= DEBOUNCE_DELAY) {
                        progressUpdateTime = System.currentTimeMillis();
                        MainExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                dialog.setProgress(progress);
                            }
                        });
                    }
                }

                @Override
                public void onError() {
                }
            };
            String boundary = StringUtil.generateBoundary();
            String hostUrl = "http://appsend.store/api/upload.php";
            HttpURLConnection connection = null;
            InputStream in = null;
            try {
                InputStream inputStream = activity.getContentResolver().openInputStream(uri);
                URL url = new URL(hostUrl);
                connection = (HttpURLConnection) url.openConnection();
                // Executing request.
                connection.setRequestMethod(HttpUtil.POST);
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                // Connect.
                connection.setReadTimeout((int) TimeUnit.MINUTES.toMillis(2));
                connection.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(30));
                connection.setRequestMethod("POST");
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
                MainExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        dialog.setProgress(100);
                        dialog.setMessage(activity.getString(R.string.obtaining_link_message));
                        dialog.setIndeterminate(true);
                    }
                });
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
                        text = appInfo.getLabel() + " (" + sizeString + ")\n" + location;
                        break;
                    }
                    default: {
                        throw new IOException("File upload error");
                    }
                }
            } finally {
                // Trying to disconnect in any case.
                if (connection != null) {
                    connection.disconnect();
                }
                HttpUtil.closeSafely(in);
            }

        }
    }

    @Override
    public void onSuccessMain() {
        final Activity activity = getWeakObject();
        if (activity != null) {
            new AlertDialog.Builder(activity)
                    .setMessage(R.string.uploading_successful)
                    .setPositiveButton(R.string.share_url, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent sendIntent = new Intent();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_TEXT, text);
                            sendIntent.setType("text/plain");
                            activity.startActivity(Intent.createChooser(sendIntent, activity.getResources().getText(R.string.send_url_to)));
                        }
                    })
                    .setNeutralButton(R.string.copy_url, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            StringUtil.copyStringToClipboard(activity, text);
                        }
                    })
                    .show();
        }
    }

    @Override
    public void onFailMain(Throwable ex) {
        Activity activity = getWeakObject();
        if (activity != null) {
            try {
                Logger.e(ex, "Upload failed");
                throw ex;
            } catch (Throwable e) {
                Toast.makeText(activity, R.string.uploading_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPostExecuteMain() {
        Activity activity = getWeakObject();
        if (activity != null && dialog != null) {
            dialog.dismiss();
        }
    }
}
