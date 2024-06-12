package com.tomclaw.appsend.main.local;

import static android.content.pm.PackageManager.GET_PERMISSIONS;
import static com.tomclaw.appsend.util.states.StateHolder.stateHolder;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.tomclaw.appsend.Appteka;
import com.tomclaw.appsend.di.legacy.LegacyInjector;
import com.tomclaw.appsend.di.legacy.LegacyModule;
import com.tomclaw.appsend.main.adapter.files.FileViewHolderCreator;
import com.tomclaw.appsend.main.item.ApkItem;
import com.tomclaw.appsend.util.FileHelper;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@EFragment
abstract class DistroFragment extends CommonItemFragment<ApkItem> {

    private static final CharSequence APK_EXTENSION = "apk";
    private static final String KEY_FILES = "files";

    private ArrayList<ApkItem> files;

    @Bean
    LegacyInjector injector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            String stateKey = savedInstanceState.getString(KEY_FILES);
            if (stateKey != null) {
                ApkItemsState itemsState = stateHolder().removeState(stateKey);
                if (itemsState != null) {
                    files = itemsState.getItems();
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (files != null) {
            String stateKey = stateHolder().putState(new ApkItemsState(files));
            outState.putString(KEY_FILES, stateKey);
        }
    }

    @Override
    protected List<ApkItem> getFiles() {
        return files;
    }

    @Override
    protected void setFiles(List<ApkItem> files) {
        if (files != null) {
            this.files = new ArrayList<>(files);
        } else {
            this.files = null;
        }
    }

    @Override
    protected FileViewHolderCreator<ApkItem> getViewHolderCreator() {
        return new ApkItemViewHolderCreator(getContext());
    }

    @Override
    List<ApkItem> loadItemsSync() {
        PackageManager packageManager = getContext().getPackageManager();
        ArrayList<ApkItem> itemList = new ArrayList<>();
        walkDir(packageManager, itemList, Environment.getExternalStorageDirectory());
        return itemList;
    }

    private void walkDir(PackageManager packageManager, List<ApkItem> itemList, File dir) {
        File[] listFile = dir.listFiles();
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

    private void processApk(PackageManager packageManager, List<ApkItem> itemList, File file) {
        if (file.exists()) {
            try {
                PackageInfo packageInfo = packageManager.getPackageArchiveInfo(
                        file.getAbsolutePath(), GET_PERMISSIONS);
                if (packageInfo != null) {
                    ApplicationInfo info = packageInfo.applicationInfo;
                    info.sourceDir = file.getAbsolutePath();
                    info.publicSourceDir = file.getAbsolutePath();
                    String label = packageManager.getApplicationLabel(info).toString();
                    String version = packageInfo.versionName;

                    ApkItem item = new ApkItem(label, info.packageName, version, file.getPath(),
                            file.length(), file.lastModified(), packageInfo);
                    itemList.add(item);
                }
            } catch (Throwable ignored) {
                // Bad package.
            }
        }
    }
}
