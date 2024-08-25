package com.tomclaw.appsend.util;

import static android.content.pm.PackageManager.GET_PERMISSIONS;

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
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;

public class ApkIconLoader implements Loader {

    private final PackageManager packageManager;

    public ApkIconLoader(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    @NonNull
    @Override
    public List<String> getSchemes() {
        return Collections.singletonList("apk");
    }

    @Override
    public boolean load(@NonNull String s, @NonNull File file) {
        try {
            String path = parseUri(s);

            PackageInfo packageInfo = packageManager.getPackageArchiveInfo(
                    path, GET_PERMISSIONS);
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

    public static String getUri(String path) {
        String encodedPath = URLEncoder.encode(path);
        return "apk://path/" + encodedPath;
    }

    public static String parseUri(String s) {
        URI uri = URI.create(s);
        String encodedPath = uri.getPath();
        return URLDecoder.decode(encodedPath);
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
