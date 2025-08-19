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
                if (file.extension.equals(APK_EXTENSION, ignoreCase = true)) {
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
        // requestedPermissions - массив nullable, надо проверить
        return packageInfo?.requestedPermissions?.toList() ?: emptyList()
    }

    private fun processApk(file: File): DistroAppEntity? {
        if (!file.exists()) return null

        return try {
            val packageInfo = packageManager.getPackageArchiveInfo(file.absolutePath, 0)
            packageInfo?.applicationInfo?.let { info ->
                // Обязательно присваиваем пути sourceDir и publicSourceDir для корректной работы
                info.sourceDir = file.absolutePath
                info.publicSourceDir = file.absolutePath

                val label = packageManager.getApplicationLabel(info).toString()
                DistroAppEntity(
                    packageName = info.packageName ?: "",
                    label = label,
                    icon = createApkIconURI(file.path),
                    verName = packageInfo.versionName.orEmpty(), // versionName nullable
                    verCode = packageInfo.versionCodeCompat(),
                    lastModified = file.lastModified(),
                    size = file.length(),
                    path = file.path,
                )
            }
        } catch (ignored: Throwable) {
            null // плохой APK
        }
    }

    override fun getPackageUploadInfo(path: String): Pair<UploadPackage, UploadApk>? {
        val packageInfo = packageManager.getPackageArchiveInfo(path, 0)?.apply {
            applicationInfo?.apply {
                sourceDir = path
                publicSourceDir = path
            } ?: return null // если applicationInfo == null — возвращаем null
        } ?: return null

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
