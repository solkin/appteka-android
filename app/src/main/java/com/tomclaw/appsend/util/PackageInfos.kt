package com.tomclaw.appsend.util

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

fun PackageInfo.versionCodeCompat(): Long {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        longVersionCode
    } else {
        @Suppress("DEPRECATION")
        versionCode.toLong()
    }
}

fun PackageInfo.getLabel(packageManager: PackageManager): String {
    return applicationInfo?.loadLabel(packageManager)?.toString() ?: "Unknown"
}
