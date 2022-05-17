package com.tomclaw.appsend.screen.details.adapter.play

import android.content.res.Resources
import com.tomclaw.appsend.util.FileHelper

interface PlayResourceProvider {

    fun formatFileSize(size: Long): String

}

class PlayResourceProviderImpl(val resources: Resources) : PlayResourceProvider {

    override fun formatFileSize(size: Long): String {
        return FileHelper.formatBytes(resources, size)
    }

}