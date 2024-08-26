package com.tomclaw.appsend.screen.installed

import android.content.res.Resources
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.FileHelper
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

interface AppsResourceProvider {

    fun formatAppVersion(verName: String, verCode: Int): String

    fun formatFileSize(size: Long): String

    fun installDate(value: Long): String

    fun updateDate(value: Long): String

}

class AppsResourceProviderImpl(
    val resources: Resources,
    val locale: Locale,
) : AppsResourceProvider {

    private val dateFormat: DateFormat = SimpleDateFormat("dd.MM.yy", locale)

    override fun formatAppVersion(verName: String, verCode: Int): String {
        return resources.getString(R.string.app_version_format, verName, verCode)
    }

    override fun formatFileSize(size: Long): String {
        return FileHelper.formatBytes(resources, size)
    }

    override fun installDate(value: Long): String {
        return resources.getString(R.string.install_date, formatDate(value))
    }

    override fun updateDate(value: Long): String {
        return resources.getString(R.string.update_date, formatDate(value))
    }

    private fun formatDate(value: Long): String {
        return dateFormat.format(value)
    }

}
