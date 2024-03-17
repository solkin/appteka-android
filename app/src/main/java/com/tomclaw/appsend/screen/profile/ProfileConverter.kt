package com.tomclaw.appsend.screen.profile

import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.main.profile.Profile
import com.tomclaw.appsend.screen.profile.adapter.app.AppItem
import com.tomclaw.appsend.screen.profile.adapter.favorites.FavoritesItem
import com.tomclaw.appsend.screen.profile.adapter.header.HeaderItem
import com.tomclaw.appsend.screen.profile.adapter.rating.RatingItem
import com.tomclaw.appsend.screen.profile.adapter.ratings.RatingsItem
import com.tomclaw.appsend.screen.profile.adapter.uploads.UploadsItem
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

interface ProfileConverter {

    fun convertProfile(profile: Profile, grantRoles: List<Int>?, uploads: List<AppEntity>?): List<Item>

    fun convertApps(uploads: List<AppEntity>?): List<AppItem>

}

class ProfileConverterImpl : ProfileConverter {

    private val id = AtomicLong()

    override fun convertProfile(profile: Profile, grantRoles: List<Int>?, uploads: List<AppEntity>?): List<Item> {
        val items = mutableListOf<Item>()
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
            )
        )
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
        }
        if (profile.ratingsCount > 0) {
            items.add(
                RatingsItem(
                    id = id.incrementAndGet(),
                    count = profile.ratingsCount,
                    items = listOf(
                        RatingItem(
                            id = id.incrementAndGet(),
                            appId = "appId_1",
                            icon = "https://appteka.store/api/1/icon/get?hash=SOfk6EnCliqzlJTxKFB1Jfd8fWnxO6X4EoXYUOLctIp2kOZl4g8KhN1wblWcTAAG",
                            title = "AirBrush",
                            version = "6.3.1",
                            rating = 4.0f,
                            text = "Lorem ipsum dolor sit amet",
                            time = System.currentTimeMillis(),
                        ),
                        RatingItem(
                            id = id.incrementAndGet(),
                            appId = "appId_2",
                            icon = "https://appteka.store/api/1/icon/get?hash=ipykd8%2BOc%2Bfg7R6XEAWlnmN%2FT2H8QcBFrtpTROGNODo7gLWSRttCA4EeKSrEEiQx",
                            title = "Yaps",
                            version = "24.4",
                            rating = 3.5f,
                            text = null,
                            time = System.currentTimeMillis(),
                        ),
                        RatingItem(
                            id = id.incrementAndGet(),
                            appId = "appId_3",
                            icon = "https://appteka.store/api/1/icon/get?hash=AyE4A0N3UeRpBYSBcEH9Ajnr4wsHwKo7A7hWTvsWHwgwffJF7kHw8z9UstYDiqO0",
                            title = "Transparent clock & weather",
                            version = "6.79.4",
                            rating = 5.0f,
                            text = "Прекрасный погодный виджет!",
                            time = System.currentTimeMillis(),
                        ),
                    )
                )
            )
        }
        if (profile.favoritesCount > 0) {
            items.add(
                FavoritesItem(
                    id = id.incrementAndGet(),
                    count = profile.favoritesCount,
                )
            )
        }
        return items
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
