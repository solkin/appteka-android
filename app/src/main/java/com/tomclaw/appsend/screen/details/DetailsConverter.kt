package com.tomclaw.appsend.screen.details

import android.net.Uri
import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.screen.details.adapter.controls.ControlsItem
import com.tomclaw.appsend.screen.details.adapter.description.DescriptionItem
import com.tomclaw.appsend.screen.details.adapter.discuss.DiscussItem
import com.tomclaw.appsend.screen.details.adapter.header.HeaderItem
import com.tomclaw.appsend.screen.details.adapter.permissions.PermissionsItem
import com.tomclaw.appsend.screen.details.adapter.play.PlayItem
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
import com.tomclaw.appsend.screen.details.api.STATUS_PRIVATE
import com.tomclaw.appsend.screen.details.api.STATUS_UNLINKED
import com.tomclaw.appsend.util.NOT_INSTALLED

interface DetailsConverter {

    fun convert(
        details: Details,
        downloadState: Int,
        installedVersionCode: Int,
        moderation: Boolean
    ): List<Item>

}

class DetailsConverterImpl(
    private val resourceProvider: DetailsResourceProvider
) : DetailsConverter {

    override fun convert(
        details: Details,
        downloadState: Int,
        installedVersionCode: Int,
        moderation: Boolean
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
                val canEdit = details.actions?.contains(ACTION_EDIT_META) ?: false
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
                    val canUnpublish = details.actions?.contains(ACTION_UNPUBLISH) ?: false
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
            userId = details.info.userId,
            userIcon = details.info.userIcon,
            userName = details.info.userName,
            downloadState = downloadState,
        )
        items += PlayItem(
            id = id++,
            rating = details.meta?.rating,
            downloads = details.info.downloads ?: 0,
            size = details.info.size,
            exclusive = details.meta?.exclusive ?: false,
            openSource = details.meta?.sourceUrl?.isNotEmpty() ?: false,
            category = details.meta?.category,
            osVersion = details.info.androidVersion,
            minSdk = details.info.sdkVersion,
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
        items += ScreenshotsItem(
            id = id++,
            items = listOf(
                ScreenshotItem(
                    id++,
                    Uri.parse("https://f-droid.org/repo/com.github.cvzi.screenshottile/en-US/phoneScreenshots/1_en-US.png"),
                    1080,
                    2220
                ),
                ScreenshotItem(
                    id++,
                    Uri.parse("https://cdn.digitbin.com/wp-content/uploads/Display_options.jpg"),
                    1080,
                    2412
                ),
                ScreenshotItem(
                    id++,
                    Uri.parse("https://i.stack.imgur.com/fNbz0.png"),
                    252,
                    448
                ),
                ScreenshotItem(
                    id++,
                    Uri.parse("https://cdn.afterdawn.fi/storage/pictures/1920/guide-force-landscape-android-landscape.jpg"),
                    1920,
                    1080
                ),
                ScreenshotItem(
                    id++,
                    Uri.parse("https://images.wondershare.com/images/mobile/mobilego/android-screenshot2.jpg"),
                    281,
                    500
                ),
                ScreenshotItem(
                    id++,
                    Uri.parse("https://www.guidingtech.com/wp-content/uploads/Take-Screenshot-With-Google-Assistant.jpg"),
                    782,
                    1602
                )
            )
        )
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
            items += WhatsNewItem(
                id = id++,
                text = details.meta?.whatsNew.orEmpty().trim(),
            )
        }
        items += DescriptionItem(
            id = id++,
            text = details.meta?.description.orEmpty().trim(),
            versionName = details.info.version,
            versionCode = details.info.versionCode,
            versionsCount = details.versions?.size ?: 0,
            uploadDate = details.info.time * 1000,
            checksum = details.info.sha1,
            sourceUrl = details.meta?.sourceUrl,
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
                    id++,
                    rating.score,
                    rating.text,
                    rating.time * 1000,
                    rating.userId,
                    rating.userIcon
                )
            }
        }

        return items
    }

}
