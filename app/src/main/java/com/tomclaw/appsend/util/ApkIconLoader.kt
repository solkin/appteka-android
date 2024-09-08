package com.tomclaw.appsend.util

import android.content.pm.PackageManager
import com.tomclaw.imageloader.core.Loader
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder

class ApkIconLoader(private val packageManager: PackageManager) : Loader {

    override val schemes: List<String>
        get() = listOf("apk")

    override fun load(uriString: String, file: File): Boolean {
        try {
            val path = parseUri(uriString)
            val info = packageManager.getPackageArchiveInfo(path, 0)?.apply {
                applicationInfo.sourceDir = path
                applicationInfo.publicSourceDir = path
            } ?: return false
            val data = PackageHelper.getPackageIconPng(info.applicationInfo, packageManager)
            FileOutputStream(file).use { output ->
                output.write(data)
                output.flush()
            }
            return true
        } catch (ignored: Throwable) {
        }
        return false
    }

    private fun parseUri(s: String): String {
        val uri = URI.create(s)
        val path = URLDecoder.decode(uri.path.substring(1), "UTF-8")
        return path
    }

}

fun createApkIconURI(path: String): String {
    val encodedPath = URLEncoder.encode(path, "UTF-8")
    return "apk://path/$encodedPath"
}
