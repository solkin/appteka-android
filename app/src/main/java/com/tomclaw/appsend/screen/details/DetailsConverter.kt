package com.tomclaw.appsend.screen.details

import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.screen.details.adapter.controls.ControlsItem
import com.tomclaw.appsend.screen.details.adapter.description.DescriptionItem
import com.tomclaw.appsend.screen.details.adapter.header.HeaderItem
import com.tomclaw.appsend.screen.details.adapter.permissions.PermissionsItem
import com.tomclaw.appsend.screen.details.adapter.play.PlayItem
import com.tomclaw.appsend.screen.details.adapter.rating.RatingItem
import com.tomclaw.appsend.screen.details.adapter.scores.ScoresItem
import com.tomclaw.appsend.screen.details.adapter.user_rate.UserRateItem
import com.tomclaw.appsend.screen.details.adapter.user_review.UserReviewItem
import com.tomclaw.appsend.screen.details.api.Details
import com.tomclaw.appsend.util.NOT_INSTALLED

interface DetailsConverter {

    fun convert(details: Details, downloadState: Int, installedVersionCode: Int): List<Item>

}

class DetailsConverterImpl : DetailsConverter {

    override fun convert(
        details: Details,
        downloadState: Int,
        installedVersionCode: Int
    ): List<Item> {
        var id: Long = 1
        val items = ArrayList<Item>()

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
        } else if (installedVersionCode != NOT_INSTALLED) {
            items += UserRateItem(
                id = id++,
                appId = details.info.appId,
            )
        }
        if (!details.meta?.description.isNullOrBlank()) {
            items += DescriptionItem(
                id = id++,
                text = details.meta?.description.orEmpty().trim(),
                versionName = details.info.version,
                versionCode = details.info.versionCode,
                versionsCount = details.versions?.size ?: 0,
                uploadDate = details.info.time * 1000,
                checksum = details.info.sha1,
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