package com.tomclaw.appsend.screen.moderation

import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.screen.moderation.adapter.app.AppItem

interface AppConverter {

    fun convert(appEntity: AppEntity): AppItem

}

class AppConverterImpl(
    private val resourceProvider: AppsResourceProvider
) : AppConverter {

    private var id: Long = 1

    override fun convert(appEntity: AppEntity): AppItem {
        return AppItem(
            id = id++,
            appId = appEntity.appId,
            icon = appEntity.icon,
            title = appEntity.title,
            version = resourceProvider.formatAppVersion(appEntity.verName, appEntity.verCode),
            size = resourceProvider.formatFileSize(appEntity.size),
            rating = appEntity.rating,
            downloads = appEntity.downloads,
        )
    }

}