package com.tomclaw.appsend.util

import com.tomclaw.bananalytics.Bananalytics

interface Analytics {

    fun register()

    fun trackEvent(name: String)

}

class AnalyticsImpl(
    private val bananalytics: Bananalytics,
) : Analytics {

    override fun register() {
        bananalytics.install()
    }

    override fun trackEvent(name: String) {
        bananalytics.trackEvent(name)
    }

}
