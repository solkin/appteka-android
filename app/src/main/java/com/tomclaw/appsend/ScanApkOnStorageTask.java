package com.tomclaw.appsend;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ivsolkin on 01.09.16.
 */
public class ScanApkOnStorageTask extends PleaseWaitTask {

    private static final CharSequence APK_EXTENSION = "apk";
    private List<AppInfo> appInfoList;

    public ScanApkOnStorageTask(Context context) {
        super(context);
        appInfoList = new ArrayList<>();
    }

    @Override
    public void executeBackground() throws Throwable {
        walkdir(Environment.getExternalStorageDirectory());
    }

    public void walkdir(File dir) {
        File listFile[] = dir.listFiles();
        if (listFile != null) {
            for (File file : listFile) {
                if (file.isDirectory()) {
                    walkdir(file);
                } else {
                    String extension = FileHelper.getFileExtensionFromPath(file.getName());
                    if (TextUtils.equals(extension, APK_EXTENSION)) {
                        processApk(file);
                    }
                }
            }
        }
    }

    private void processApk(File file) {
        Context activity = getWeakObject();
        if (activity != null && file.exists()) {
            PackageManager packageManager = activity.getPackageManager();
            try {
                PackageInfo packageInfo = packageManager.getPackageArchiveInfo(file.getAbsolutePath(), 0);
                if (packageInfo != null) {
                    ApplicationInfo info = packageInfo.applicationInfo;
                    info.sourceDir = file.getAbsolutePath();
                    info.publicSourceDir = file.getAbsolutePath();
                    Drawable icon = info.loadIcon(packageManager);
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

                    AppInfo appInfo = new AppInfo(icon, label, info.packageName,
                            version, instVersion, file.getPath(), file.length(), 0, 0, null);
                    appInfoList.add(appInfo);
                }
            } catch (Throwable ignored) {
                // Bad package.
            }
        }
    }

    @Override
    public void onSuccessMain() {
        InstallActivity activity = (InstallActivity) getWeakObject();
        if (activity != null) {
            activity.setAppInfoList(appInfoList);
        }
    }
}
