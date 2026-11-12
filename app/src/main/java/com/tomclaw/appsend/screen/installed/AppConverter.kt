package com.tomclaw.appsend.screen.installed

import com.tomclaw.appsend.screen.installed.adapter.app.AppItem
import com.tomclaw.appsend.screen.installed.api.UpdateEntity
import com.tomclaw.appsend.util.isRecent

interface AppConverter {

    fun convert(appEntity: InstalledAppEntity, update: UpdateEntity?): AppItem

}

class AppConverterImpl : AppConverter {

    private var id: Long = 1

    override fun convert(appEntity: InstalledAppEntity, update: UpdateEntity?): AppItem {
        return AppItem(
            id = id++,
            icon = appEntity.icon,
            title = appEntity.label,
            version = appEntity.verName,
            size = appEntity.size,
            installTime = appEntity.firstInstallTime,
            updateTime = appEntity.lastUpdateTime,
            path = appEntity.path,
            packageName = appEntity.packageName,
            updateAppId = update?.appId,
            isUserApp = appEntity.isUserApp,
            isNew = isRecent(appEntity.firstInstallTime),
        )
    }

}
