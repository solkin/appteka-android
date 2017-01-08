package com.tomclaw.appsend.main.controller;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;

import com.tomclaw.appsend.AppInfo;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.util.FileHelper;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by ivsolkin on 08.01.17.
 */
public class ApksController {

    private static class Holder {

        static ApksController instance = new ApksController();
    }

    public static ApksController getInstance() {
        return Holder.instance;
    }

    private static final CharSequence APK_EXTENSION = "apk";

    private WeakReference<ApksCallback> weakCallback;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private List<AppInfo> list;
    private boolean isError = false;

    private Future<?> future;

    public void onAttach(ApksCallback callback) {
        weakCallback = new WeakReference<>(callback);
        if (isLoaded()) {
            callback.onLoaded(list);
        } else if (isError) {
            callback.onError();
        } else {
            callback.onProgress();
        }
    }

    public boolean isLoaded() {
        return list != null;
    }

    public boolean isStarted() {
        return future != null;
    }

    public void reload(final Context context) {
        list = null;
        isError = false;
        future = executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    loadInternal(context);
                } catch (Throwable ignored) {
                    onError();
                }
            }
        });
    }

    public void onDetach() {
        if (weakCallback != null) {
            weakCallback.clear();
            weakCallback = null;
        }
    }

    private void onProgress() {
        MainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (weakCallback != null) {
                    ApksCallback callback = weakCallback.get();
                    if (callback != null) {
                        callback.onProgress();
                    }
                }
            }
        });
    }

    private void onLoaded(final List<AppInfo> list) {
        this.list = list;
        MainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (weakCallback != null) {
                    ApksCallback callback = weakCallback.get();
                    if (callback != null) {
                        callback.onLoaded(list);
                    }
                }
            }
        });
    }

    private void onError() {
        this.isError = true;
        MainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (weakCallback != null) {
                    ApksCallback callback = weakCallback.get();
                    if (callback != null) {
                        callback.onError();
                    }
                }
            }
        });
    }

    private void loadInternal(Context context) {
        onProgress();
        PackageManager packageManager = context.getPackageManager();
        ArrayList<AppInfo> appInfoList = new ArrayList<>();
        walkDir(packageManager, appInfoList, Environment.getExternalStorageDirectory());
        onLoaded(appInfoList);
    }

    private void walkDir(PackageManager packageManager, List<AppInfo> appInfoList, File dir) {
        File listFile[] = dir.listFiles();
        if (listFile != null) {
            for (File file : listFile) {
                if (file.isDirectory()) {
                    walkDir(packageManager, appInfoList, file);
                } else {
                    String extension = FileHelper.getFileExtensionFromPath(file.getName());
                    if (TextUtils.equals(extension, APK_EXTENSION)) {
                        processApk(packageManager, appInfoList, file);
                    }
                }
            }
        }
    }

    private void processApk(PackageManager packageManager, List<AppInfo> appInfoList, File file) {
        if (file.exists()) {
            try {
                PackageInfo packageInfo = packageManager.getPackageArchiveInfo(file.getAbsolutePath(), 0);
                if (packageInfo != null) {
                    ApplicationInfo info = packageInfo.applicationInfo;
                    info.sourceDir = file.getAbsolutePath();
                    info.publicSourceDir = file.getAbsolutePath();
                    String label = packageManager.getApplicationLabel(info).toString();
                    String version = packageInfo.versionName;

                    String instVersion = null;
                    try {
                        PackageInfo instPackageInfo = packageManager.getPackageInfo(info.packageName, 0);
                        if (instPackageInfo != null) {
                            instVersion = instPackageInfo.versionName;
                        }
                    } catch (Throwable ignored) {
                        // No package, maybe?
                    }

                    AppInfo appInfo = new AppInfo(label, info.packageName,
                            version, instVersion, file.getPath(), file.length(), 0, 0, null,
                            AppInfo.FLAG_APK_FILE);
                    appInfoList.add(appInfo);
                }
            } catch (Throwable ignored) {
                // Bad package.
            }
        }
    }

    public interface ApksCallback {

        void onProgress();
        void onLoaded(List<AppInfo> list);
        void onError();

    }
}
