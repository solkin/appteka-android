package com.tomclaw.appsend.screen.favorite

import com.tomclaw.appsend.categories.CategoryConverter
import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.screen.favorite.adapter.app.AppItem
import com.tomclaw.appsend.util.NOT_INSTALLED
import com.tomclaw.appsend.util.PackageObserver
import java.util.concurrent.TimeUnit

interface AppConverter {

    fun convert(appEntity: AppEntity): AppItem

}

class AppConverterImpl(
    private val resourceProvider: AppsResourceProvider,
    private val categoryConverter: CategoryConverter,
    private val packageObserver: PackageObserver
) : AppConverter {

    private var id: Long = 1

    override fun convert(appEntity: AppEntity): AppItem {
        val installedVersionCode = packageObserver.pickInstalledVersionCode(appEntity.packageName)
        return AppItem(
            id = id++,
            appId = appEntity.appId,
            icon = appEntity.icon,
            title = appEntity.title,
            version = appEntity.verName,
            size = resourceProvider.formatFileSize(appEntity.size),
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
