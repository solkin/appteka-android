package com.tomclaw.appsend.util

import android.content.pm.PackageInfo
import android.os.Build

fun PackageInfo.versionCodeCompat() : Long {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        longVersionCode
    } else {
        @Suppress("DEPRECATION")
        versionCode.toLong()
    }
}
