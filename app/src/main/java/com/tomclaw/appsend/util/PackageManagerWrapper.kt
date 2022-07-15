package com.tomclaw.appsend.util

import android.content.pm.PackageManager

interface PackageManagerWrapper {

    fun getInstalledVersionCode(packageName: String): Int

}

class PackageManagerWrapperImpl(
    private val packageManager: PackageManager
) : PackageManagerWrapper {

    override fun getInstalledVersionCode(packageName: String): Int {
        return try {
            packageManager.getPackageInfo(packageName, 0).versionCode
        } catch (ex: Throwable) {
            NOT_INSTALLED
        }
    }

}

const val NOT_INSTALLED = -1
