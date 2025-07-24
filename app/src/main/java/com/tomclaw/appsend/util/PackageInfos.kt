package com.tomclaw.appsend.util

import android.content.pm.PackageInfo
import android.os.Build
import com.tomclaw.appsend.Appteka

fun PackageInfo.versionCodeCompat(): Long {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        longVersionCode
    } else {
        @Suppress("DEPRECATION")
        versionCode.toLong()
    }
}

fun PackageInfo.getLabel(): String {
    val packageManager = Appteka.app().packageManager
    return applicationInfo?.loadLabel(packageManager)?.toString() ?: "Unknown"
}
