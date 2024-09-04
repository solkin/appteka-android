package com.tomclaw.appsend.screen.installed

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.tomclaw.appsend.upload.UploadApk
import com.tomclaw.appsend.upload.UploadPackage
import com.tomclaw.appsend.util.createAppIconURI
import com.tomclaw.appsend.util.versionCodeCompat
import java.io.File

interface InstalledInfoProvider {

    fun getInstalledApps(): List<InstalledAppEntity>

    fun getPackagePermissions(packageName: String): List<String>

    fun getPackageUploadInfo(packageName: String): Pair<UploadPackage, UploadApk>

}

class InstalledInfoProviderImpl(
    private val packageManager: PackageManager,
) : InstalledInfoProvider {

    override fun getInstalledApps(): List<InstalledAppEntity> {
        val apps = ArrayList<InstalledAppEntity>()
        val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        for (info in packages) {
            try {
                val packageInfo = packageManager.getPackageInfo(info.packageName, 0)
                val file = File(info.publicSourceDir)
                if (file.exists()) {
                    val app = InstalledAppEntity(
                        packageName = info.packageName,
                        label = packageInfo.applicationInfo.loadLabel(packageManager).toString(),
                        icon = createAppIconURI(packageInfo.packageName),
                        verName = packageInfo.versionName,
                        verCode = packageInfo.versionCodeCompat(),
                        firstInstallTime = packageInfo.firstInstallTime,
                        lastUpdateTime = packageInfo.lastUpdateTime,
                        size = file.length(),
                        path = file.path,
                        isUserApp = ((info.flags and ApplicationInfo.FLAG_SYSTEM) != ApplicationInfo.FLAG_SYSTEM &&
                                (info.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)
                    )
                    apps.add(app)
                }
            } catch (ignored: Throwable) {
                // Bad package.
            }
        }
        return apps
    }

    override fun getPackagePermissions(packageName: String): List<String> {
        val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
        return listOf(*packageInfo.requestedPermissions)
    }

    override fun getPackageUploadInfo(packageName: String): Pair<UploadPackage, UploadApk> {
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val file = File(packageInfo.applicationInfo.publicSourceDir)
        val pkg = UploadPackage(file.path, null, packageName)
        val apk = UploadApk(file.path, packageInfo.versionName, file.length(), packageInfo)
        return Pair(pkg, apk)
    }

}
