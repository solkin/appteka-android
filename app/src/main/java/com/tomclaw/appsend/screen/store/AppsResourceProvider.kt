package com.tomclaw.appsend.screen.store

import android.content.res.Resources
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.FileHelper

interface AppsResourceProvider {

    fun formatAppVersion(verName: String, verCode: Int): String

    fun formatFileSize(size: Long): String

    fun getStatusUpdatableString(): String

    fun getStatusInstalledString(): String

    fun getStatusBlockedString(): String

    fun getStatusPrivateString(): String

    fun getStatusModerationString(): String

}

class AppsResourceProviderImpl(val resources: Resources) : AppsResourceProvider {

    override fun formatAppVersion(verName: String, verCode: Int): String {
        return resources.getString(R.string.app_version_format, verName, verCode)
    }

    override fun formatFileSize(size: Long): String {
        return FileHelper.formatBytes(resources, size)
    }

    override fun getStatusUpdatableString(): String {
        return resources.getString(R.string.store_app_update)
    }

    override fun getStatusInstalledString(): String {
        return resources.getString(R.string.store_app_installed)
    }

    override fun getStatusBlockedString(): String {
        return resources.getString(R.string.status_unlinked)
    }

    override fun getStatusPrivateString(): String {
        return resources.getString(R.string.status_private)
    }

    override fun getStatusModerationString(): String {
        return resources.getString(R.string.status_on_moderation)
    }

}