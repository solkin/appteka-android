package com.tomclaw.appsend.screen.upload.adapter.selected_app

import android.content.res.Resources
import com.tomclaw.appsend.util.FileHelper

interface SelectedAppResourceProvider {

    fun formatFileSize(size: Long): String

}

class SelectedAppResourceProviderImpl(
    val resources: Resources,
) : SelectedAppResourceProvider {

    override fun formatFileSize(size: Long): String {
        return FileHelper.formatBytes(resources, size)
    }

}
