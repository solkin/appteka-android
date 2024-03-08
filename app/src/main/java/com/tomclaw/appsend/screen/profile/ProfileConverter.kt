package com.tomclaw.appsend.screen.profile

import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.main.profile.Profile
import com.tomclaw.appsend.screen.profile.adapter.app.AppItem
import com.tomclaw.appsend.screen.profile.adapter.header.HeaderItem
import com.tomclaw.appsend.screen.profile.adapter.uploads.UploadsItem
import java.util.concurrent.TimeUnit

interface ProfileConverter {

    fun convert(profile: Profile, grantRoles: List<Int>?): List<Item>

}

class ProfileConverterImpl : ProfileConverter {

    override fun convert(profile: Profile, grantRoles: List<Int>?): List<Item> {
        var id: Long = 1
        val items = mutableListOf<Item>()
        items.add(
            HeaderItem(
                id = id++,
                userName = profile.name,
                userIcon = profile.userIcon,
                joinTime = TimeUnit.SECONDS.toMillis(profile.joinTime),
                lastSeen = TimeUnit.SECONDS.toMillis(profile.lastSeen),
                role = profile.role,
                isRegistered = profile.isRegistered,
                isVerified = profile.isVerified,
            )
        )
        items.add(
            UploadsItem(
                id = id++,
                uploads = profile.filesCount,
                downloads = profile.totalDownloads,
                items = listOf(
                    AppItem(
                        id = id++,
                        appId = "appId_1",
                        icon = "https://appteka.store/api/1/icon/get?hash=zJZqz%2BbHIQtYYqR8OKcFprhtietik3eYlKlZGmKgl1vwn1fwbmmrKaHu%2BW6VMfdv",
                        title = "Clone App",
                        rating = 5.0f
                    ),
                    AppItem(
                        id = id++,
                        appId = "appId_2",
                        icon = "https://appteka.store/api/1/icon/get?hash=6OHiXAEms8aJEDhyCGsxp9NykYxgqAbPSpNs81b%2BJRhP2mFdXj%2BQS6jL615YAGW2",
                        title = "Filmora",
                        rating = 4.5f
                    ),
                    AppItem(
                        id = id++,
                        appId = "appId_3",
                        icon = "https://appteka.store/api/1/icon/get?hash=4IxekRFKzVkmbnKDSrphqz6qur5PGiEaoKMiInHYUy3ruOCH5KCY0xUU5H%2BAAtze",
                        title = "OurGroceries",
                        rating = 0f
                    ),
                    AppItem(
                        id = id++,
                        appId = "appId_4",
                        icon = "https://appteka.store/api/1/icon/get?hash=xREs%2FR9OLxF%2FaeDamSukDRr08B3wyBGi%2BVOdkvap26M9vmJbLc20LRxSfDpZs8GU",
                        title = "Video Merge",
                        rating = 5.0f
                    ),
                    AppItem(
                        id = id++,
                        appId = "appId_5",
                        icon = "https://appteka.store/api/1/icon/get?hash=DdqeQBCvciACcEtG5%2BJzvbVzgWI6QyAxEViUuoRHPe00PKIH9qbxKeLbCz8XFYeb",
                        title = "Textra",
                        rating = 4.0f
                    )
                )
            )
        )
        return items
    }

}
