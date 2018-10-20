package com.tomclaw.appsend.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import androidx.annotation.NonNull;

import com.tomclaw.appsend.R;

import static android.content.pm.PermissionInfo.PROTECTION_DANGEROUS;

/**
 * Created by ivsolkin on 27.01.17.
 */
public class PermissionHelper {

    @NonNull
    public static PermissionSmallInfo getPermissionSmallInfo(@NonNull Context context, @NonNull String permission) {
        String description;
        boolean isDangerous;
        try {
            PackageManager packageManager = context.getPackageManager();
            PermissionInfo permissionInfo = packageManager
                    .getPermissionInfo(permission, PackageManager.GET_META_DATA);
            description = permissionInfo.loadLabel(packageManager).toString();
            isDangerous = permissionInfo.protectionLevel == PROTECTION_DANGEROUS;
        } catch (Throwable ignored) {
            description = context.getString(R.string.unknown_permission_description);
            isDangerous = false;
        }
        return new PermissionSmallInfo(description, isDangerous);
    }

    public static class PermissionSmallInfo {

        private String description;
        private boolean isDangerous;

        PermissionSmallInfo(String description, boolean isDangerous) {
            this.description = description;
            this.isDangerous = isDangerous;
        }

        public
        @NonNull
        String getDescription() {
            return description;
        }

        public boolean isDangerous() {
            return isDangerous;
        }
    }
}
