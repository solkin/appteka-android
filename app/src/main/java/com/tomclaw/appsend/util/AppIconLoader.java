package com.tomclaw.appsend.util;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import com.tomclaw.imageloader.core.Loader;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;

public class AppIconLoader implements Loader {

    private final PackageManager packageManager;

    public AppIconLoader(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    @NonNull
    @Override
    public List<String> getSchemes() {
        return Collections.singletonList("app");
    }

    @Override
    public boolean load(@NonNull String s, @NonNull File file) {
        try {
            String packageName = parseUri(s);

            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);

            byte[] data = PackageHelper.getPackageIconPng(
                    packageInfo.applicationInfo, packageManager
            );
            OutputStream output = null;
            try {
                output = new FileOutputStream(file);
                output.write(data);
                output.flush();
                return true;
            } finally {
                safeClose(output);
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    public static String getUri(String packageName) {
        return "app://" + packageName;
    }

    public static String parseUri(String s) {
        URI uri = URI.create(s);
        return uri.getAuthority();
    }

    public static void safeClose(Closeable... streams) {
        for (Closeable stream : streams) {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
