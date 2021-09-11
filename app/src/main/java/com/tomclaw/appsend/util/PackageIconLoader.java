package com.tomclaw.appsend.util;

import static com.tomclaw.appsend.util.StreamHelper.safeClose;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Parcel;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.tomclaw.imageloader.core.Loader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;

public class PackageIconLoader implements Loader {

    private final PackageManager packageManager;

    public PackageIconLoader(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    @NonNull
    @Override
    public List<String> getSchemes() {
        return Collections.singletonList("package");
    }

    @Override
    public boolean load(@NonNull String s, @NonNull File file) {
        try {
            PackageInfo packageInfo = parseUri(s);
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

    public static String getUri(PackageInfo packageInfo) {
        byte[] bytes = ParcelableUtil.marshall(packageInfo);
        String data = Base64.encodeToString(bytes, Base64.NO_WRAP | Base64.URL_SAFE);
        return "package://" + packageInfo.packageName + "/" + data;
    }

    public static PackageInfo parseUri(String s) {
        URI uri = URI.create(s);
        String path = uri.getPath();
        byte[] data = Base64.decode(path.substring(1), Base64.NO_WRAP | Base64.URL_SAFE);
        Parcel parcel = ParcelableUtil.unmarshall(data);
        return PackageInfo.CREATOR.createFromParcel(parcel);
    }

}
