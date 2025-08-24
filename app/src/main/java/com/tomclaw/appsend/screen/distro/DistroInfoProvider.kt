package com.tomclaw.appsend.screen.distro

import com.tomclaw.appsend.core.PackageInfoProvider
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
    private val packageInfoProvider: PackageInfoProvider,
) : DistroInfoProvider {

    override fun getApkItems(): List<DistroAppEntity> {
        return rootDir
            .walkTopDown()
            .map { file ->
                if (file.extension.equals(APK_EXTENSION, ignoreCase = true)) {
                    processApk(file)
                } else {
                    null
                }
            }
            .filterNotNull()
            .toList()
    }

    override fun getPackagePermissions(path: String) =
        packageInfoProvider.getPackagePermissions(path)

    private fun processApk(file: File): DistroAppEntity? {
        if (!file.exists()) return null
        return packageInfoProvider.getPackageInfo(file.absolutePath)?.let { packageInfo ->
            packageInfo.applicationInfo?.let { info ->
                DistroAppEntity(
                    packageName = info.packageName ?: "",
                    label = packageInfoProvider.getApplicationLabel(info),
                    icon = createApkIconURI(file.path),
                    verName = packageInfo.versionName.orEmpty(), // versionName nullable
                    verCode = packageInfo.versionCodeCompat(),
                    lastModified = file.lastModified(),
                    size = file.length(),
                    path = file.path,
                )
            }
        }
    }

    override fun getPackageUploadInfo(path: String): Pair<UploadPackage, UploadApk>? {
        val packageInfo = packageInfoProvider.getPackageInfo(path) ?: return null
        val file = File(path)
        val pkg = UploadPackage(
            uniqueId = file.path,
            sha1 = null,
            packageName = packageInfo.packageName,
            size = file.length()
        )
        val apk = UploadApk(
            path = file.path,
            version = packageInfo.versionName.orEmpty(),
            size = file.length(),
            packageInfo = packageInfo
        )
        return Pair(pkg, apk)
    }
}

const val APK_EXTENSION = "apk"
