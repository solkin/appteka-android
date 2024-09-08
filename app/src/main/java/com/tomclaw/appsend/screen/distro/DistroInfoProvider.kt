package com.tomclaw.appsend.screen.distro

import android.content.pm.PackageManager
import com.tomclaw.appsend.upload.UploadApk
import com.tomclaw.appsend.upload.UploadPackage
import com.tomclaw.appsend.util.createApkIconURI
import com.tomclaw.appsend.util.versionCodeCompat
import java.io.File

interface DistroInfoProvider {

    fun getApkItems(): List<DistroAppEntity>

    fun getPackagePermissions(path: String): List<String>

    fun getPackageUploadInfo(path: String): Pair<UploadPackage, UploadApk>?

}

class DistroInfoProviderImpl(
    private val rootDir: File,
    private val packageManager: PackageManager,
) : DistroInfoProvider {

    override fun getApkItems(): List<DistroAppEntity> {
        return rootDir
            .walkTopDown()
            .map { file ->
                if (file.extension == APK_EXTENSION) {
                    processApk(file)
                } else {
                    null
                }
            }
            .filterNotNull()
            .toList()
    }

    override fun getPackagePermissions(path: String): List<String> {
        val packageInfo = packageManager.getPackageArchiveInfo(path, PackageManager.GET_PERMISSIONS)
        return packageInfo?.let {
            listOf(*packageInfo.requestedPermissions)
        } ?: emptyList()
    }

    private fun processApk(file: File): DistroAppEntity? {
        if (file.exists()) {
            try {
                val packageInfo = packageManager.getPackageArchiveInfo(file.absolutePath, 0)
                if (packageInfo != null) {
                    val info = packageInfo.applicationInfo
                    info.sourceDir = file.absolutePath
                    info.publicSourceDir = file.absolutePath
                    val label = packageManager.getApplicationLabel(info).toString()
                    val item = DistroAppEntity(
                        packageName = info.packageName,
                        label = label,
                        icon = createApkIconURI(file.path),
                        verName = packageInfo.versionName,
                        verCode = packageInfo.versionCodeCompat(),
                        lastModified = file.lastModified(),
                        size = file.length(),
                        path = file.path,
                    )
                    return item
                }
            } catch (ignored: Throwable) {
                // Bad package.
            }
        }
        return null
    }

    override fun getPackageUploadInfo(path: String): Pair<UploadPackage, UploadApk>? {
        val packageInfo = packageManager.getPackageArchiveInfo(path, 0)
        if (packageInfo != null) {
            val file = File(path)
            val pkg = UploadPackage(file.path, null, packageInfo.packageName)
            val apk = UploadApk(file.path, packageInfo.versionName, file.length(), packageInfo)
            return Pair(pkg, apk)
        }
        return null
    }

}

const val APK_EXTENSION = "apk"
