package com.tomclaw.appsend.screen.details.adapter.description

import android.content.res.Resources
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.TimeHelper
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

interface DescriptionResourceProvider {

    fun formatFileVersion(versionName: String, versionCode: Int): String

    fun formatDate(value: Long): String

}

class DescriptionResourceProviderImpl(
    val resources: Resources,
    val locale: Locale,
) : DescriptionResourceProvider {

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