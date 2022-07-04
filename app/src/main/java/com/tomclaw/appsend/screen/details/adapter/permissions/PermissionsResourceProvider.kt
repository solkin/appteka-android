package com.tomclaw.appsend.screen.details.adapter.permissions

import android.content.res.Resources
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.TimeHelper
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

interface PermissionsResourceProvider {

    fun formatFileVersion(versionName: String, versionCode: Int): String

    fun formatDate(value: Long): String

}

class PermissionsResourceProviderImpl(
    val resources: Resources,
    val locale: Locale,
) : PermissionsResourceProvider {

    private val dateFormat: DateFormat

    init {
        dateFormat = SimpleDateFormat("dd.MM.yy", locale)
    }

    override fun formatFileVersion(versionName: String, versionCode: Int): String {
        return resources.getString(R.string.app_version_format, versionName, versionCode)
    }

    override fun formatDate(value: Long): String {
        return dateFormat.format(value)
    }

}