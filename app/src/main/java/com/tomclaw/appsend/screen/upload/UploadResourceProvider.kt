package com.tomclaw.appsend.screen.upload

import android.content.res.Resources
import com.tomclaw.appsend.R
import com.tomclaw.appsend.categories.DEFAULT_LOCALE
import com.tomclaw.appsend.core.permissions.Capability
import com.tomclaw.appsend.core.permissions.CapabilityHintResolver
import com.tomclaw.appsend.screen.details.api.AppVersion
import com.tomclaw.appsend.util.FileHelper
import java.util.Locale

interface UploadResourceProvider {

    fun shareTitle(): String

    fun formatShareText(
        url: String,
        defaultLabel: String?,
        labels: Map<String, String>?,
        size: Long
    ): String

    fun formatVersion(version: AppVersion): String

    fun createTopicError(): String

    /**
     * Text shown above the upload form to inform the user about how
     * their submission will be processed (auto-published vs queued for
     * moderation). `null` means "no notice required" — the calling
     * converter omits the moderation block entirely.
     */
    fun moderationNotice(bypassModerationCapability: Capability?): String?

}

class UploadResourceProviderImpl(
    val resources: Resources,
    val locale: Locale,
) : UploadResourceProvider {

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

    override fun moderationNotice(bypassModerationCapability: Capability?): String? {
        // Capability missing entirely (legacy server / not yet loaded):
        // we don't know how this user is treated, so show nothing rather
        // than picking a wrong copy.
        val capability = bypassModerationCapability ?: return null
        return if (capability.allowed) {
            resources.getString(R.string.permission_upload_publishes_immediately)
        } else {
            // Server already supplied a hint key for the rule; resolver
            // turns it into the localised text.
            CapabilityHintResolver(resources).resolveText(capability)
        }
    }

}
