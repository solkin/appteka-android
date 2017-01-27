package com.tomclaw.appsend.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.tomclaw.appsend.R;

/**
 * Created by ivsolkin on 27.01.17.
 */
public class PermissionHelper {

    @Nullable
    public static String getPermissionDescription(@NonNull Context context, @NonNull String permission) {
        String description;
        try {
            PackageManager packageManager = context.getPackageManager();
            PermissionInfo permissionInfo = packageManager
                    .getPermissionInfo(permission, PackageManager.GET_META_DATA);
            description = permissionInfo.loadLabel(packageManager).toString();
        } catch (Throwable ignored) {
            description = context.getString(R.string.unknown_permission_description);
        }
        return description;
    }
}
