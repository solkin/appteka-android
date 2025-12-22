package com.tomclaw.appsend.screen.details

import android.net.Uri
import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.categories.DEFAULT_LOCALE
import com.tomclaw.appsend.screen.details.adapter.controls.ControlsItem
import com.tomclaw.appsend.screen.details.adapter.description.DescriptionItem
import com.tomclaw.appsend.screen.details.adapter.discuss.DiscussItem
import com.tomclaw.appsend.screen.details.adapter.header.HeaderItem
import com.tomclaw.appsend.screen.details.adapter.permissions.PermissionsItem
import com.tomclaw.appsend.screen.details.adapter.play.PlayItem
import com.tomclaw.appsend.screen.details.adapter.play.PlaySecurityStatus
import com.tomclaw.appsend.screen.details.adapter.security.SecurityItem
import com.tomclaw.appsend.screen.details.adapter.security.SecurityType
import com.tomclaw.appsend.screen.details.adapter.rating.RatingItem
import com.tomclaw.appsend.screen.details.adapter.scores.ScoresItem
import com.tomclaw.appsend.screen.details.adapter.screenshot.ScreenshotItem
import com.tomclaw.appsend.screen.details.adapter.screenshots.ScreenshotsItem
import com.tomclaw.appsend.screen.details.adapter.status.StatusAction
import com.tomclaw.appsend.screen.details.adapter.status.StatusItem
import com.tomclaw.appsend.screen.details.adapter.status.StatusType
import com.tomclaw.appsend.screen.details.adapter.user_rate.UserRateItem
import com.tomclaw.appsend.screen.details.adapter.user_review.UserReviewItem
import com.tomclaw.appsend.screen.details.adapter.whats_new.WhatsNewItem
import com.tomclaw.appsend.screen.details.api.ACTION_EDIT_META
import com.tomclaw.appsend.screen.details.api.ACTION_UNPUBLISH
import com.tomclaw.appsend.screen.details.api.Details
import com.tomclaw.appsend.screen.details.api.STATUS_MODERATION
import com.tomclaw.appsend.screen.details.api.STATUS_NORMAL
import com.tomclaw.appsend.screen.details.api.SECURITY_STATUS_COMPLETED
import com.tomclaw.appsend.screen.details.api.SECURITY_STATUS_FAILED
import com.tomclaw.appsend.screen.details.api.SECURITY_STATUS_PENDING
import com.tomclaw.appsend.screen.details.api.SECURITY_STATUS_SCANNING
import com.tomclaw.appsend.screen.details.api.SECURITY_VERDICT_MALWARE
import com.tomclaw.appsend.screen.details.api.SECURITY_VERDICT_SAFE
import com.tomclaw.appsend.screen.details.api.SECURITY_VERDICT_SUSPICIOUS
import com.tomclaw.appsend.screen.details.api.STATUS_PRIVATE
import com.tomclaw.appsend.screen.details.api.STATUS_UNLINKED
import com.tomclaw.appsend.screen.details.api.Security
import com.tomclaw.appsend.screen.details.api.TranslationResponse
import com.tomclaw.appsend.util.NOT_INSTALLED
import java.util.Locale
import androidx.core.net.toUri

interface DetailsConverter {

    fun convert(
        details: Details,
        downloadState: Int,
        installedVersionCode: Int,
        moderation: Boolean,
        translationData: TranslationResponse?,
        translationState: Int
    ): List<Item>

}

class DetailsConverterImpl(
    private val resourceProvider: DetailsResourceProvider,
    private val locale: Locale
) : DetailsConverter {

    override fun convert(
        details: Details,
        downloadState: Int,
        installedVersionCode: Int,
        moderation: Boolean,
        translationData: TranslationResponse?,
        translationState: Int
    ): List<Item> {
        var id: Long = 1
        val items = ArrayList<Item>()

        when (details.info.fileStatus) {
            STATUS_UNLINKED -> items += StatusItem(
                id = id++,
                type = StatusType.ERROR,
                text = resourceProvider.unlinkedStatusText(),
                actionType = StatusAction.NONE,
                actionLabel = null,
            )

            STATUS_PRIVATE -> {
                val canEdit = details.actions?.contains(ACTION_EDIT_META) == true
                items += StatusItem(
                    id = id++,
                    type = StatusType.INFO,
                    text = resourceProvider.privateStatusText(),
                    actionType = if (canEdit) StatusAction.EDIT_META else StatusAction.NONE,
                    actionLabel = resourceProvider.editMetaAction(),
                )
            }

            STATUS_MODERATION -> {
                if (!moderation) {
                    val canUnpublish = details.actions?.contains(ACTION_UNPUBLISH) == true
                    items += StatusItem(
                        id = id++,
                        type = StatusType.WARNING,
                        text = resourceProvider.moderationStatusText(),
                        actionType = if (canUnpublish) StatusAction.UNPUBLISH else StatusAction.NONE,
                        actionLabel = resourceProvider.unpublishAction(),
                    )
                }
            }

            else -> Unit
        }

        // Add security scan status item (only if not in scanning mode)
        convertSecurityItem(id++, details.info.appId, details.security, resourceProvider)?.let {
            items += it
        }

        items += HeaderItem(
            id = id++,
            icon = details.info.icon,
            packageName = details.info.packageName,
            label = details.info.label.orEmpty(),
            userId = details.info.userId,
            userIcon = details.info.userIcon,
            userName = details.info.userName,
            downloadState = downloadState,
        )
        items += PlayItem(
            id = id++,
            rating = details.meta?.rating,
            downloads = details.info.downloads ?: 0,
            favorites = details.info.favorites ?: 0,
            size = details.info.size,
            exclusive = details.meta?.exclusive == true,
            openSource = details.meta?.sourceUrl?.isNotEmpty() == true,
            category = details.meta?.category,
            osVersion = details.info.androidVersion,
            minSdk = details.info.sdkVersion,
            securityStatus = convertPlaySecurityStatus(details.security),
        )
        items += ControlsItem(
            id = id++,
            appId = details.info.appId,
            packageName = details.info.packageName,
            versionCode = details.info.versionCode,
            sdkVersion = details.info.sdkVersion,
            androidVersion = details.info.androidVersion,
            size = details.info.size,
            link = details.link,
            expiresIn = details.expiresIn,
            installedVersionCode = installedVersionCode,
            downloadState = downloadState,
        )
        if (details.meta?.screenshots != null && details.meta.screenshots.isNotEmpty()) {
            items += ScreenshotsItem(
                id = id++,
                items = details.meta.screenshots.map {
                    ScreenshotItem(
                        id = it.scrId.hashCode().toLong(),
                        original = it.original.toUri(),
                        preview = it.preview.toUri(),
                        width = it.width,
                        height = it.height,
                    )
                }
            )
        }
        if (details.userRating != null) {
            items += UserReviewItem(
                id = id++,
                score = details.userRating.score,
                text = details.userRating.text,
                time = details.userRating.time * 1000,
                userId = details.userRating.userId,
                userIcon = details.userRating.userIcon,
                userName = details.userRating.userName,
            )
        } else if (installedVersionCode != NOT_INSTALLED && details.info.fileStatus == STATUS_NORMAL) {
            items += UserRateItem(
                id = id++,
                appId = details.info.appId,
            )
        }
        if (details.info.fileStatus == STATUS_NORMAL || !details.versions.isNullOrEmpty()) {
            items += DiscussItem(
                id = id++,
                msgCount = details.msgCount,
            )
        }
        if (!details.meta?.whatsNew.isNullOrBlank()) {
            val whatsNewText = when (translationState) {
                TRANSLATION_TRANSLATED -> translationData?.whatsNew
                else -> details.meta?.whatsNew
            }
            items += WhatsNewItem(
                id = id++,
                text = whatsNewText.orEmpty().trim(),
            )
        }
        val descriptionText = when (translationState) {
            TRANSLATION_TRANSLATED -> translationData?.description
            else -> details.meta?.description
        }
        items += DescriptionItem(
            id = id++,
            text = descriptionText.orEmpty().trim(),
            versionName = details.info.version,
            versionCode = details.info.versionCode,
            versionsCount = details.versions?.size ?: 0,
            uploadDate = details.info.time * 1000,
            checksum = details.info.sha1,
            sourceUrl = details.meta?.sourceUrl,
            translationState = translationState,
        )
        if (!details.info.permissions.isNullOrEmpty()) {
            items += PermissionsItem(
                id = id++,
                permissions = details.info.permissions,
            )
        }
        if (
            details.meta?.scores != null &&
            details.meta.rating != null &&
            details.meta.rateCount != null &&
            details.meta.rateCount > 0
        ) {
            items += ScoresItem(
                id = id++,
                rateCount = details.meta.rateCount,
                rating = details.meta.rating,
                scores = details.meta.scores
            )
        }

        if (!details.ratingsList.isNullOrEmpty()) {
            items += details.ratingsList.map { rating ->
                RatingItem(
                    id = id++,
                    score = rating.score,
                    text = rating.text,
                    time = rating.time * 1000,
                    userId = rating.userId,
                    userName = rating.userName.takeIf { !it.isNullOrBlank() }
                        ?: rating.userIcon.label[locale.language]
                        ?: rating.userIcon.label[DEFAULT_LOCALE].orEmpty(),
                    userIcon = rating.userIcon
                )
            }
        }

        return items
    }

}

const val TRANSLATION_ORIGINAL: Int = 0
const val TRANSLATION_PROGRESS: Int = 1
const val TRANSLATION_TRANSLATED: Int = 2

private fun convertSecurityItem(
    id: Long,
    appId: String,
    security: Security?,
    resourceProvider: DetailsResourceProvider
): SecurityItem? {
    // Only show the separate block when not scanned (security is null)
    // All other statuses are shown only in the horizontal bar
    return if (security == null) {
        SecurityItem(
            id = id,
            appId = appId,
            type = SecurityType.NOT_SCANNED,
            text = resourceProvider.securityNotScannedText(),
            score = null,
            showAction = true,
            actionLabel = resourceProvider.requestScanAction(),
        )
    } else {
        null
    }
}

private fun convertPlaySecurityStatus(security: Security?): PlaySecurityStatus? {
    return when {
        security == null -> null
        security.status == SECURITY_STATUS_PENDING || security.status == SECURITY_STATUS_SCANNING -> {
            PlaySecurityStatus.SCANNING
        }
        security.status == SECURITY_STATUS_COMPLETED -> when (security.verdict) {
            SECURITY_VERDICT_SAFE -> PlaySecurityStatus.SAFE
            SECURITY_VERDICT_SUSPICIOUS -> PlaySecurityStatus.SUSPICIOUS
            SECURITY_VERDICT_MALWARE -> PlaySecurityStatus.MALWARE
            else -> PlaySecurityStatus.NOT_CHECKED
        }
        security.status == SECURITY_STATUS_FAILED -> PlaySecurityStatus.NOT_CHECKED
        else -> PlaySecurityStatus.NOT_CHECKED
    }
}
