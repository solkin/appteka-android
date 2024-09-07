package com.tomclaw.appsend.screen.distro

import com.tomclaw.appsend.screen.distro.adapter.apk.ApkItem
import java.util.concurrent.TimeUnit

interface ApkConverter {

    fun convert(appEntity: DistroAppEntity): ApkItem

}

class ApkConverterImpl : ApkConverter {

    private var id: Long = 1

    override fun convert(appEntity: DistroAppEntity): ApkItem {
        return ApkItem(
            id = id++,
            icon = appEntity.icon,
            title = appEntity.label,
            version = appEntity.verName,
            size = appEntity.size,
            lastModified = appEntity.lastModified,
            path = appEntity.path,
            packageName = appEntity.packageName,
            isNew = (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - appEntity.lastModified) <
                    TimeUnit.DAYS.toSeconds(1),
        )
    }

}
