package com.tomclaw.appsend.util;

import android.content.pm.PackageManager;

import com.tomclaw.appsend.main.item.StoreItem;

/**
 * Created by solkin on 23.04.17.
 */
public class PackageHelper {

    public static int getInstalledVersionCode(String packageName, PackageManager packageManager) {
        try {
            return packageManager.getPackageInfo(packageName, 0).versionCode;
        } catch (Throwable ex) {
            return StoreItem.NOT_INSTALLED;
        }
    }
}
