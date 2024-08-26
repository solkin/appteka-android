package com.tomclaw.appsend.util

import android.content.pm.PackageManager
import com.tomclaw.imageloader.core.Loader
import java.io.File
import java.io.FileOutputStream
import java.net.URI

class AppIconLoader(private val packageManager: PackageManager) : Loader {

    override val schemes: List<String>
        get() = listOf("app")

    override fun load(uriString: String, file: File): Boolean {
        try {
            val packageName = parseUri(uriString)
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val data = PackageHelper.getPackageIconPng(
                packageInfo.applicationInfo, packageManager
            )
            FileOutputStream(file).use { output ->
                output.write(data)
                output.flush()
            }
            return true
        } catch (ignored: Throwable) {
        }
        return false
    }

    private fun parseUri(s: String?): String {
        val uri = URI.create(s)
        return uri.authority
    }

}

fun createAppIconURI(packageName: String): String {
    return "app://$packageName"
}
