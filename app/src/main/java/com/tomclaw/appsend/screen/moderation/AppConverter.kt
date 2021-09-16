package com.tomclaw.appsend.screen.moderation

import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.screen.moderation.adapter.app.AppItem

interface AppConverter {

    fun convert(appEntity: AppEntity): Item

}

class AppConverterImpl(
    private val resourceProvider: AppsResourceProvider
) : AppConverter {

    override fun convert(appEntity: AppEntity): Item {
        return AppItem(
            id = appEntity.appId.toLong(),
            icon = appEntity.icon,
            title = appEntity.title,
            subtitle = resourceProvider.formatAppVersion(appEntity.verName, appEntity.verCode)
        )
    }

}