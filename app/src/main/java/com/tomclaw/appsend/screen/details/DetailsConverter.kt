package com.tomclaw.appsend.screen.details

import android.net.Uri
import com.tomclaw.appsend.core.permissions.CapabilityAction
import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.categories.DEFAULT_LOCALE
import com.tomclaw.appsend.screen.details.adapter.abi.AbiItem
import com.tomclaw.appsend.screen.details.adapter.abi.AbiResourceProvider
import com.tomclaw.appsend.screen.details.adapter.ai_note.AINoteItem
import com.tomclaw.appsend.screen.details.adapter.all_ratings.AllRatingsItem
import com.tomclaw.appsend.screen.details.adapter.ai_note.AINoteState
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
import com.tomclaw.appsend.screen.details.adapter.similar.SimilarItem
import com.tomclaw.appsend.screen.details.adapter.content_flags.ContentFlagsItem
import com.tomclaw.appsend.screen.details.adapter.tags.TagsItem
import com.tomclaw.appsend.screen.profile.adapter.app.AppItem
import com.tomclaw.appsend.screen.details.adapter.user_rate.UserRateItem
import com.tomclaw.appsend.screen.details.adapter.user_review.UserReviewItem
import com.tomclaw.appsend.screen.details.adapter.whats_new.WhatsNewItem
import com.tomclaw.appsend.core.permissions.CapabilityPolicy
import com.tomclaw.appsend.screen.details.api.Details
import com.tomclaw.appsend.screen.details.api.STATUS_MODERATION
import com.tomclaw.appsend.screen.details.api.STATUS_NORMAL
import com.tomclaw.appsend.screen.details.api.SECURITY_STATUS_COMPLETED
import com.tomclaw.appsend.screen.details.api.SECURITY_STATUS_PENDING
import com.tomclaw.appsend.screen.details.api.SECURITY_STATUS_SCANNING
import com.tomclaw.appsend.screen.details.api.SECURITY_VERDICT_MALWARE
import com.tomclaw.appsend.screen.details.api.SECURITY_VERDICT_SAFE
import com.tomclaw.appsend.screen.details.api.SECURITY_VERDICT_SUSPICIOUS
import com.tomclaw.appsend.screen.details.api.STATUS_PRIVATE
import com.tomclaw.appsend.screen.details.api.MODERATION_STATUS_BLOCKED
import com.tomclaw.appsend.screen.details.api.STATUS_UNLINKED
import com.tomclaw.appsend.screen.details.api.MODERATION_STATUS_REJECTED
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
    private val abiResourceProvider: AbiResourceProvider,
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
            // Blocked: red, final, and no action — there is nothing the
            // author can do about it. The reason comes from the same
            // `moderation` block that explains a decline.
            STATUS_UNLINKED -> {
                val block = details.moderation
                    ?.takeIf { it.status == MODERATION_STATUS_BLOCKED }
                items += StatusItem(
                    id = id++,
                    type = StatusType.ERROR,
                    text = resourceProvider.unlinkedStatusText(
                        reasonText = block?.reasonText,
                        reasonComment = block?.reasonComment,
                    ),
                    actionType = StatusAction.NONE,
                    actionLabel = null,
                )
            }

            STATUS_PRIVATE -> {
                val canEdit = CapabilityPolicy.isAllowed(
                    action = CapabilityAction.APP_EDIT_META,
                    capabilities = details.capabilities,
                    allowOnUnknown = false,
                )
                // The server reports `file_status = -2` (Private) both for
                // apps the author chose to keep private and for apps a
                // moderator rejected. The two are distinguished by the
                // `moderation` block in the response: if it's present and
                // status="rejected", we surface the moderator's reason
                // instead of the generic private-app text.
                val decline = details.moderation
                    ?.takeIf { it.status == MODERATION_STATUS_REJECTED }
                if (decline?.reasonText != null) {
                    items += StatusItem(
                        id = id++,
                        // Warning, not error: unlike a block, this one
                        // is fixable — and the edit action below says so.
                        type = StatusType.WARNING,
                        text = resourceProvider.declinedStatusText(
                            reasonText = decline.reasonText,
                            reasonComment = decline.reasonComment,
                        ),
                        actionType = if (canEdit) StatusAction.EDIT_META else StatusAction.NONE,
                        actionLabel = resourceProvider.editMetaAction(),
                    )
                } else {
                    items += StatusItem(
                        id = id++,
                        type = StatusType.INFO,
                        text = resourceProvider.privateStatusText(),
                        actionType = if (canEdit) StatusAction.EDIT_META else StatusAction.NONE,
                        actionLabel = resourceProvider.editMetaAction(),
                    )
                }
            }

            STATUS_MODERATION -> {
                if (!moderation) {
                    val canUnpublish = CapabilityPolicy.isAllowed(
                        action = CapabilityAction.APP_UNPUBLISH,
                        capabilities = details.capabilities,
                        allowOnUnknown = false,
                    )
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

        items += HeaderItem(
            id = id++,
            icon = details.info.icon,
            packageName = details.info.packageName,
            label = details.info.label.orEmpty(),
            author = details.info.author,
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
            official = details.developer?.isOfficial == true,
            category = details.meta?.category,
            osVersion = details.info.androidVersion,
            minSdk = details.info.sdkVersion,
            securityStatus = convertPlaySecurityStatus(details.security),
            securityScore = details.security?.score,
        )

        // Add security scan status item (only if not scanned)
        convertSecurityItem(id++, details.info.appId, details.security, resourceProvider)?.let {
            items += it
        }

        // Blocking frees the APK from storage, so there is nothing left
        // to download — the install row would only lead to an error.
        if (details.info.fileStatus != STATUS_UNLINKED) {
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
        }
        val whatsNewText = details.meta?.whatsNew
            ?.takeIf { it.isNotBlank() }
            ?.let {
                when (translationState) {
                    TRANSLATION_TRANSLATED -> translationData?.whatsNew
                        ?.takeIf { text -> text.isNotBlank() } ?: it

                    else -> it
                }
            }
            ?.trim()
        val hasUpdate = installedVersionCode != NOT_INSTALLED &&
                installedVersionCode < details.info.versionCode
        // Right under the install button while an update is pending: that's
        // what the user came for. Otherwise it waits below, next to the
        // description.
        if (whatsNewText != null && hasUpdate) {
            items += WhatsNewItem(
                id = id++,
                text = whatsNewText,
            )
        }
        if (details.userRating != null) {
            items += UserReviewItem(
                id = id++,
                score = details.userRating.score,
                text = details.userRating.text,
                time = details.userRating.time * 1000,
                user = details.userRating.user,
            )
        } else if (installedVersionCode != NOT_INSTALLED && details.info.fileStatus == STATUS_NORMAL) {
            items += UserRateItem(
                id = id++,
                appId = details.info.appId,
                rateCapability = details.capabilities?.get(CapabilityAction.APP_RATE),
            )
        }
        if (details.info.fileStatus == STATUS_NORMAL || !details.versions.isNullOrEmpty()) {
            items += DiscussItem(
                id = id++,
                msgCount = details.msgCount,
            )
        }

        // Below the rate / discuss actions: someone who already has the app
        // installed came to act, not to look through the screenshots again.
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

        convertAINoteItem(
            id = id++,
            appId = details.info.appId,
            status = details.meta?.aiStatus,
            note = when (translationState) {
                TRANSLATION_TRANSLATED -> translationData?.aiNote ?: details.meta?.aiNote
                else -> details.meta?.aiNote
            },
            fileStatus = details.info.fileStatus,
        )?.let { items += it }

        if (whatsNewText != null && !hasUpdate) {
            items += WhatsNewItem(
                id = id++,
                text = whatsNewText,
            )
        }
        val descriptionText = when (translationState) {
            TRANSLATION_TRANSLATED -> translationData?.description?.takeIf { it.isNotBlank() }
                ?: details.meta?.description
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
        // Above the tags: what the app contains is a caution, and the
        // tags under it are an invitation to browse. Reading the caution
        // second would be the wrong way round.
        val contentFlags = details.meta?.contentFlags.orEmpty()
        if (contentFlags.isNotEmpty()) {
            items += ContentFlagsItem(
                id = id++,
                codes = contentFlags,
            )
        }
        val tags = details.meta?.aiTags.orEmpty()
        if (tags.isNotEmpty()) {
            items += TagsItem(
                id = id++,
                tags = tags,
            )
        }
        // High on the screen: the reviews list below is unbounded, and a
        // shelf under it would rarely be reached.
        val similar = details.similar.orEmpty()
        if (similar.isNotEmpty()) {
            items += SimilarItem(
                id = id++,
                items = similar.map { entity ->
                    AppItem(
                        id = id++,
                        appId = entity.appId,
                        icon = entity.icon,
                        title = entity.title,
                        rating = entity.rating,
                    )
                },
            )
        }
        if (!details.info.abi.isNullOrEmpty()) {
            items += AbiItem(
                id = id++,
                abiList = details.info.abi,
                isCompatible = abiResourceProvider.checkCompatibility(details.info.abi),
            )
        }
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
                    user = rating.user,
                )
            }
            // Only the freshest reviews are on this screen and nothing hints
            // at the rest, so the shortcut to the full list is spelled out —
            // unless they all fit here already.
            val rateCount = details.meta?.rateCount ?: details.ratingsList.size
            if (rateCount > details.ratingsList.size) {
                items += AllRatingsItem(
                    id = id++,
                    rateCount = rateCount,
                )
            }
        }

        return items
    }

}

const val TRANSLATION_ORIGINAL: Int = 0
const val TRANSLATION_PROGRESS: Int = 1
const val TRANSLATION_TRANSLATED: Int = 2

// convertAINoteItem maps the server-reported ai_status into the
// three view states the block renders. Hidden for unpublished /
// private apps — there's no public catalog view there.
private fun convertAINoteItem(
    id: Long,
    appId: String,
    status: String?,
    note: String?,
    fileStatus: Int,
): AINoteItem? {
    if (fileStatus != STATUS_NORMAL && fileStatus != STATUS_MODERATION) {
        return null
    }
    return when (status) {
        AI_STATUS_COMPLETED -> AINoteItem(
            id = id,
            appId = appId,
            state = AINoteState.COMPLETED,
            note = note?.takeIf { it.isNotBlank() } ?: return null,
        )
        AI_STATUS_PENDING -> AINoteItem(
            id = id,
            appId = appId,
            state = AINoteState.PENDING,
            note = null,
        )
        AI_STATUS_IDLE, null -> AINoteItem(
            id = id,
            appId = appId,
            state = AINoteState.IDLE,
            note = null,
        )
        // Server has the enrichment task disabled (budget kill switch).
        // Hide the block entirely; existing notes still render via the
        // COMPLETED branch above because the server keeps emitting that
        // status as long as ai_note is populated.
        AI_STATUS_DISABLED -> null
        else -> null
    }
}

private const val AI_STATUS_IDLE = "idle"
private const val AI_STATUS_PENDING = "pending"
private const val AI_STATUS_COMPLETED = "completed"
private const val AI_STATUS_DISABLED = "disabled"

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
        // Failed, unavailable and anything a newer server invents read
        // the same to a user: nobody can vouch for this build.
        else -> PlaySecurityStatus.NOT_CHECKED
    }
}
