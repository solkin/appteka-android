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

}
