package com.tomclaw.appsend.screen.profile

import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.main.profile.Profile
import com.tomclaw.appsend.screen.profile.adapter.header.HeaderItem
import com.tomclaw.appsend.screen.profile.adapter.uploads.UploadsItem
import java.util.concurrent.TimeUnit

interface ProfileConverter {

    fun convert(profile: Profile, grantRoles: List<Int>?): List<Item>

}

class ProfileConverterImpl() : ProfileConverter {

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
                items = listOf()
            )
        )
        return items
    }

}
