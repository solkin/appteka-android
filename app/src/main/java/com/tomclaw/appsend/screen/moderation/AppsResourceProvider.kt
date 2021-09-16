package com.tomclaw.appsend.screen.moderation

import android.content.res.Resources
import com.tomclaw.appsend.R

interface AppsResourceProvider {

    fun formatAppVersion(verName: String, verCode: Int): String

}

class AppsResourceProviderImpl(val resources: Resources) : AppsResourceProvider {

    override fun formatAppVersion(verName: String, verCode: Int): String {
        return resources.getString(R.string.app_version, verName, verCode)
    }

}