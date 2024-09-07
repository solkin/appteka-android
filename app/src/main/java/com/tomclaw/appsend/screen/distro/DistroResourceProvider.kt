package com.tomclaw.appsend.screen.distro

import android.content.res.Resources
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.FileHelper
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

interface DistroResourceProvider {

    fun formatAppVersion(verName: String, verCode: Int): String

    fun formatFileSize(size: Long): String

}

class DistroResourceProviderImpl(
    val resources: Resources,
    val locale: Locale,
) : DistroResourceProvider {

    private val dateFormat: DateFormat = SimpleDateFormat("dd.MM.yy", locale)

    override fun formatAppVersion(verName: String, verCode: Int): String {
        return resources.getString(R.string.app_version_format, verName, verCode)
    }

    override fun formatFileSize(size: Long): String {
        return FileHelper.formatBytes(resources, size)
    }

    private fun formatDate(value: Long): String {
        return dateFormat.format(value)
    }

}
