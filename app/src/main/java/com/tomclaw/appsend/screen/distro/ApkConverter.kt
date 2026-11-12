package com.tomclaw.appsend.screen.distro

import com.tomclaw.appsend.screen.distro.adapter.apk.ApkItem
import com.tomclaw.appsend.util.isRecent

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
            fileName = appEntity.fileName,
            packageName = appEntity.packageName,
            isNew = isRecent(appEntity.lastModified),
        )
    }

}
