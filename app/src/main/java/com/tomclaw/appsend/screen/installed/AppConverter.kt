package com.tomclaw.appsend.screen.installed

import com.tomclaw.appsend.net.AppEntry
import com.tomclaw.appsend.screen.installed.adapter.app.AppItem
import java.util.concurrent.TimeUnit

interface AppConverter {

    fun convert(appEntity: InstalledAppEntity, update: AppEntry?): AppItem

}

class AppConverterImpl(
    private val resourceProvider: AppsResourceProvider,
) : AppConverter {

    private var id: Long = 1

    override fun convert(appEntity: InstalledAppEntity, update: AppEntry?): AppItem {
        return AppItem(
            id = id++,
            icon = appEntity.icon,
            title = appEntity.label,
            version = appEntity.verName,
            size = appEntity.size,
            installTime = appEntity.firstInstallTime,
            updateTime = appEntity.lastUpdateTime,
            path = appEntity.path,
            updateAppId = update?.appId,
            isUserApp = appEntity.isUserApp,
            isNew = (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - appEntity.firstInstallTime) <
                    TimeUnit.DAYS.toSeconds(1),
        )
    }

}
