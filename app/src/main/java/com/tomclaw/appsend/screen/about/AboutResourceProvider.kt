package com.tomclaw.appsend.screen.about

import android.content.res.Resources
import com.tomclaw.appsend.R
import com.tomclaw.appsend.core.AppInfoProvider

data class Contributor(
    val name: String,
    val url: String
)

interface AboutResourceProvider {

    fun getAppVersion(): String

    fun getContributors(): List<Contributor>

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

    override fun getContributors(): List<Contributor> {
        return listOf(
            Contributor(
                name = "Igor Solkin",
                url = "https://github.com/solkin"
            ),
            Contributor(
                name = "Ameer Muawiya",
                url = "https://github.com/ameermuawiya"
            ),
            Contributor(
                name = "tojik_proof_93",
                url = "https://github.com/FlutterGenerator"
            )
        )
    }

}
