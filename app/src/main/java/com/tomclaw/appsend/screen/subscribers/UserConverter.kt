package com.tomclaw.appsend.screen.subscribers

import com.tomclaw.appsend.categories.CategoryConverter
import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.screen.subscribers.adapter.user.UserItem
import com.tomclaw.appsend.util.NOT_INSTALLED
import com.tomclaw.appsend.util.PackageObserver
import java.util.concurrent.TimeUnit

interface UserConverter {

    fun convert(appEntity: AppEntity): UserItem

}

class UserConverterImpl(
    private val categoryConverter: CategoryConverter,
    private val packageObserver: PackageObserver
) : UserConverter {

    private var id: Long = 1

    override fun convert(appEntity: AppEntity): UserItem {
        val installedVersionCode = packageObserver.pickInstalledVersionCode(appEntity.packageName)
        return UserItem(
            id = id++,
            appId = appEntity.appId,
            icon = appEntity.icon,
            title = appEntity.title,
            version = appEntity.verName,
            size = "",
            rating = appEntity.rating,
            downloads = appEntity.downloads,
            status = appEntity.status,
            category = appEntity.category?.let { categoryConverter.convert(it) },
            exclusive = appEntity.exclusive,
            openSource = !appEntity.sourceUrl.isNullOrEmpty(),
            isInstalled = installedVersionCode != NOT_INSTALLED,
            isUpdatable = installedVersionCode < appEntity.verCode,
            isNew = (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - appEntity.time) <
                    TimeUnit.DAYS.toSeconds(1)
        )
    }

}
