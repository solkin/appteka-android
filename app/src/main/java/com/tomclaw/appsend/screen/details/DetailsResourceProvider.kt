package com.tomclaw.appsend.screen.details

import android.content.res.Resources
import com.tomclaw.appsend.R
import com.tomclaw.appsend.categories.DEFAULT_LOCALE
import com.tomclaw.appsend.screen.details.api.AppVersion
import com.tomclaw.appsend.util.FileHelper
import java.util.Locale

interface DetailsResourceProvider {

    fun shareTitle(): String

    fun formatShareText(
        url: String,
        defaultLabel: String?,
        labels: Map<String, String>?,
        size: Long
    ): String

    fun formatAbuseText(
        label: String?,
        packageName: String,
        version: String,
        versionCode: Int,
        size: Long,
        url: String,
    ): String

    fun formatVersion(version: AppVersion): String

    fun createTopicError(): String

    fun unlinkedStatusText(): String

    fun privateStatusText(): String

    fun moderationStatusText(): String

    fun editMetaAction(): String

    fun unpublishAction(): String

    fun markedFavorite(): String

    fun unmarkedFavorite(): String

    fun markFavoriteError(): String

    fun unmarkFavoriteError(): String

    fun rateAppError(): String

    fun translationError(): String

    fun requestScanAction(): String

    fun retryScanAction(): String

    fun securityNotScannedText(): String

    fun securityPendingText(): String

    fun securityScanningText(): String

    fun securitySafeText(): String

    fun securitySuspiciousText(): String

    fun securityMalwareText(): String

    fun securityUnknownText(): String

    fun securityFailedText(): String

    fun securityScanRequestedText(): String

    fun securityScanErrorText(): String

}

class DetailsResourceProviderImpl(
    val resources: Resources,
    val locale: Locale,
) : DetailsResourceProvider {

    override fun shareTitle(): String {
        return resources.getString(R.string.send_url_to)
    }

    override fun formatShareText(
        url: String,
        defaultLabel: String?,
        labels: Map<String, String>?,
        size: Long
    ): String {
        val localizedLabel = labels
            ?.let {
                labels[locale.language] ?: labels[DEFAULT_LOCALE]
            }
            ?: defaultLabel.orEmpty()
        val sizeString = FileHelper.formatBytes(resources, size)
        return resources.getString(R.string.uploaded_url, localizedLabel, sizeString, url)
    }

    override fun formatAbuseText(
        label: String?,
        packageName: String,
        version: String,
        versionCode: Int,
        size: Long,
        url: String
    ): String {
        val lines = listOf(
            "Label: $label",
            "Package: $packageName",
            "Version: $version ($versionCode)",
            "Size: ${FileHelper.formatBytes(resources, size)}",
            "URL: $url",
            "--",
            "Abuse reason (write here): "
        )
        return lines.reduce { acc, line -> "$acc\n$line" }
    }

    override fun formatVersion(version: AppVersion): String {
        return resources.getString(R.string.app_version_format, version.verName, version.verCode)
    }

    override fun createTopicError(): String {
        return resources.getString(R.string.error_app_topic_creation)
    }

    override fun unlinkedStatusText(): String {
        return resources.getString(R.string.unlinked_status_text)
    }

    override fun privateStatusText(): String {
        return resources.getString(R.string.private_status_text)
    }

    override fun moderationStatusText(): String {
        return resources.getString(R.string.moderation_status_text)
    }

    override fun editMetaAction(): String {
        return resources.getString(R.string.edit_meta)
    }

    override fun unpublishAction(): String {
        return resources.getString(R.string.unpublish_file)
    }

    override fun markedFavorite(): String {
        return resources.getString(R.string.marked_favorite)
    }

    override fun unmarkedFavorite(): String {
        return resources.getString(R.string.unmarked_favorite)
    }

    override fun markFavoriteError(): String {
        return resources.getString(R.string.mark_favorite_error)
    }

    override fun unmarkFavoriteError(): String {
        return resources.getString(R.string.unmark_favorite_error)
    }

    override fun rateAppError(): String {
        return resources.getString(R.string.error_rate_app)
    }

    override fun translationError(): String {
        return resources.getString(R.string.translation_error)
    }

    override fun requestScanAction(): String {
        return resources.getString(R.string.request_security_scan)
    }

    override fun retryScanAction(): String {
        return resources.getString(R.string.retry_security_scan)
    }

    override fun securityNotScannedText(): String {
        return resources.getString(R.string.security_not_scanned)
    }

    override fun securityPendingText(): String {
        return resources.getString(R.string.security_pending)
    }

    override fun securityScanningText(): String {
        return resources.getString(R.string.security_scanning)
    }

    override fun securitySafeText(): String {
        return resources.getString(R.string.security_safe)
    }

    override fun securitySuspiciousText(): String {
        return resources.getString(R.string.security_suspicious)
    }

    override fun securityMalwareText(): String {
        return resources.getString(R.string.security_malware)
    }

    override fun securityUnknownText(): String {
        return resources.getString(R.string.security_unknown)
    }

    override fun securityFailedText(): String {
        return resources.getString(R.string.security_failed)
    }

    override fun securityScanRequestedText(): String {
        return resources.getString(R.string.security_scan_requested)
    }

    override fun securityScanErrorText(): String {
        return resources.getString(R.string.security_scan_error)
    }

}
