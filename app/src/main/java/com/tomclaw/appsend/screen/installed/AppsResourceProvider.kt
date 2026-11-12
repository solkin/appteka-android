package com.tomclaw.appsend.screen.installed

import android.content.res.Resources
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.FileHelper
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

interface AppsResourceProvider {

    fun formatAppVersion(verName: String, verCode: Int): String

    fun formatFileSize(size: Long): String

    fun installDate(value: Long): String

    fun updateDate(value: Long): String

    fun otherEditionVersion(verName: String, publishTime: Long?): String

    fun updateInfo(verName: String, size: Long): String

    fun updateFailed(): String

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

    override fun otherEditionVersion(verName: String, publishTime: Long?): String {
        // Catalog rows older than the publish date have none.
        return if (publishTime != null && publishTime > 0) {
            val date = formatDate(TimeUnit.SECONDS.toMillis(publishTime))
            resources.getString(R.string.other_edition_version_dated, verName, date)
        } else {
            resources.getString(R.string.other_edition_version, verName)
        }
    }

    override fun updateInfo(verName: String, size: Long): String {
        return resources.getString(R.string.installed_update_info, verName, formatFileSize(size))
    }

    override fun updateFailed(): String {
        return resources.getString(R.string.download_failed)
    }

    private fun formatDate(value: Long): String {
        return dateFormat.format(value)
    }

}
