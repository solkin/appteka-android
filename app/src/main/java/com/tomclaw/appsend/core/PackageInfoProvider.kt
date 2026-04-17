package com.tomclaw.appsend.core

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.tomclaw.appsend.util.getPackageArchiveInfoCompat

interface PackageInfoProvider {

    fun getApplicationLabel(info: ApplicationInfo): String

    fun getPackagePermissions(path: String): List<String>

    fun getPackageInfo(path: String): PackageInfo?

}

class PackageInfoProviderImpl(
    private val packageManager: PackageManager,
) : PackageInfoProvider {

    override fun getApplicationLabel(info: ApplicationInfo): String {
        return packageManager.getApplicationLabel(info).toString()
    }

    override fun getPackagePermissions(path: String): List<String> {
        val packageInfo = packageManager.getPackageArchiveInfoCompat(path, PackageManager.GET_PERMISSIONS)
        // requestedPermissions - массив nullable, надо проверить
        return packageInfo?.requestedPermissions?.toList() ?: emptyList()
    }

    override fun getPackageInfo(path: String): PackageInfo? {
        return try {
            packageManager.getPackageArchiveInfoCompat(path, 0)?.apply {
                applicationInfo?.apply {
                    sourceDir = path
                    publicSourceDir = path
                } ?: return null // если applicationInfo == null — возвращаем null
            }
        } catch (_: Throwable) {
            null // плохой APK
        }
    }

}
