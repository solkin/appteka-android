package com.tomclaw.appsend.screen.about

import android.content.res.Resources
import com.tomclaw.appsend.R
import com.tomclaw.appsend.core.AppInfoProvider

interface AboutResourceProvider {

    fun getAppVersion(): String
}

class AboutResourceProviderImpl(
    private val infoProvider: AppInfoProvider,
    private val resources: Resources,
) : AboutResourceProvider {

    override fun getAppVersion(): String {
        return resources.getString(
            R.string.app_version,
            infoProvider.getVersionName(),
            infoProvider.getVersionCode()
        )
    }

}
