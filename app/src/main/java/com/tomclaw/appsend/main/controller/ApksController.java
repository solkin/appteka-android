package com.tomclaw.appsend.main.controller;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;

import com.tomclaw.appsend.main.item.ApkItem;
import com.tomclaw.appsend.main.item.AppItem;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.main.item.BaseItem;
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

    private List<BaseItem> list;
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

    private void onLoaded(final List<BaseItem> list) {
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
        ArrayList<BaseItem> itemList = new ArrayList<>();
        walkDir(packageManager, itemList, Environment.getExternalStorageDirectory());
        onLoaded(itemList);
    }

    private void walkDir(PackageManager packageManager, List<BaseItem> itemList, File dir) {
        File listFile[] = dir.listFiles();
        if (listFile != null) {
            for (File file : listFile) {
                if (file.isDirectory()) {
                    walkDir(packageManager, itemList, file);
                } else {
                    String extension = FileHelper.getFileExtensionFromPath(file.getName());
                    if (TextUtils.equals(extension, APK_EXTENSION)) {
                        processApk(packageManager, itemList, file);
                    }
                }
            }
        }
    }

    private void processApk(PackageManager packageManager, List<BaseItem> itemList, File file) {
        if (file.exists()) {
            try {
                PackageInfo packageInfo = packageManager.getPackageArchiveInfo(file.getAbsolutePath(), 0);
                if (packageInfo != null) {
                    ApplicationInfo info = packageInfo.applicationInfo;
                    info.sourceDir = file.getAbsolutePath();
                    info.publicSourceDir = file.getAbsolutePath();
                    String label = packageManager.getApplicationLabel(info).toString();
                    String version = packageInfo.versionName;

                    String installedVersion = null;
                    try {
                        PackageInfo instPackageInfo = packageManager.getPackageInfo(info.packageName, 0);
                        if (instPackageInfo != null) {
                            installedVersion = instPackageInfo.versionName;
                        }
                    } catch (Throwable ignored) {
                        // No package, maybe?
                    }

                    ApkItem item = new ApkItem(label, info.packageName, version, file.getPath(),
                            file.length(), installedVersion, file.lastModified(), packageInfo);
                    itemList.add(item);
                }
            } catch (Throwable ignored) {
                // Bad package.
            }
        }
    }

    public interface ApksCallback {

        void onProgress();
        void onLoaded(List<BaseItem> list);
        void onError();

    }
}
