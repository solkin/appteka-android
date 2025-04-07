package com.tomclaw.appsend.screen.profile

import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.screen.profile.adapter.app.AppItem
import com.tomclaw.appsend.screen.profile.adapter.downloads.DownloadsItem
import com.tomclaw.appsend.screen.profile.adapter.favorites.FavoritesItem
import com.tomclaw.appsend.screen.profile.adapter.feed.FeedItem
import com.tomclaw.appsend.screen.profile.adapter.header.HeaderItem
import com.tomclaw.appsend.screen.profile.adapter.placeholder.PlaceholderItem
import com.tomclaw.appsend.screen.profile.adapter.review.ReviewItem
import com.tomclaw.appsend.screen.profile.adapter.reviews.ReviewsItem
import com.tomclaw.appsend.screen.profile.adapter.unauthorized.UnauthorizedItem
import com.tomclaw.appsend.screen.profile.adapter.uploads.UploadsItem
import com.tomclaw.appsend.screen.profile.api.Profile
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

interface ProfileConverter {

    fun convertProfile(
        profile: Profile,
        grantRoles: List<Int>?,
        uploads: List<AppEntity>?,
        isSelf: Boolean,
    ): List<Item>

    fun unauthorizedProfile(): List<Item>

    fun convertApps(uploads: List<AppEntity>?): List<AppItem>

}

class ProfileConverterImpl : ProfileConverter {

    private val id = AtomicLong(1)

    override fun convertProfile(
        profile: Profile,
        grantRoles: List<Int>?,
        uploads: List<AppEntity>?,
        isSelf: Boolean,
    ): List<Item> {
        val items = mutableListOf<Item>()
        var isInactive = true
        items.add(
            HeaderItem(
                id = id.incrementAndGet(),
                userName = profile.name,
                userIcon = profile.userIcon,
                joinTime = TimeUnit.SECONDS.toMillis(profile.joinTime),
                lastSeen = TimeUnit.SECONDS.toMillis(profile.lastSeen),
                role = profile.role,
                isRegistered = profile.isRegistered,
                isVerified = profile.isVerified,
                isSelf = isSelf,
                isSubscribed = profile.isSubscribed,
                nameRegex = profile.nameRegex,
            )
        )
        if (profile.feedCount + profile.subsCount + profile.pubsCount > 0) {
            items.add(
                FeedItem(
                    id = id.incrementAndGet(),
                    feedCount = profile.feedCount,
                    subsCount = profile.subsCount,
                    pubsCount = profile.pubsCount,
                )
            )
        }
        if (profile.filesCount > 0) {
            val appItems = convertApps(uploads)

            items.add(
                UploadsItem(
                    id = id.incrementAndGet(),
                    userId = profile.userId,
                    uploads = profile.filesCount,
                    downloads = profile.totalDownloads,
                    items = appItems
                )
            )
            isInactive = false
        }
        if (profile.reviewsCount > 0 && profile.lastReviews != null) {
            items.add(
                ReviewsItem(
                    id = id.incrementAndGet(),
                    count = profile.reviewsCount,
                    items = profile.lastReviews.map { entity ->
                        ReviewItem(
                            id = id.incrementAndGet(),
                            appId = entity.file.appId,
                            rateId = entity.rating.rateId,
                            icon = entity.file.icon,
                            title = entity.file.title,
                            version = entity.file.verName,
                            rating = entity.rating.score.toFloat(),
                            text = entity.rating.text,
                            time = TimeUnit.SECONDS.toMillis(entity.rating.time),
                        )
                    }
                )
            )
            isInactive = false
        }
        if (profile.favoritesCount > 0) {
            items.add(
                FavoritesItem(
                    id = id.incrementAndGet(),
                    count = profile.favoritesCount,
                )
            )
            isInactive = false
        }
        if (profile.downloadsCount != null && profile.downloadsCount > 0) {
            items.add(
                DownloadsItem(
                    id = id.incrementAndGet(),
                    count = profile.downloadsCount,
                )
            )
            isInactive = false
        }
        if (isInactive) {
            items.add(
                PlaceholderItem(id = id.incrementAndGet())
            )
        }
        return items
    }

    override fun unauthorizedProfile(): List<Item> {
        return listOf(
            UnauthorizedItem(id = 1)
        )
    }

    override fun convertApps(uploads: List<AppEntity>?): List<AppItem> {
        return uploads.orEmpty().map { entity ->
            AppItem(
                id = id.incrementAndGet(),
                appId = entity.appId,
                icon = entity.icon,
                title = entity.title,
                rating = entity.rating
            )
        }
    }

}
