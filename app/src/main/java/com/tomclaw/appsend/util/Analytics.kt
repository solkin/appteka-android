package com.tomclaw.appsend.util

import android.app.Application
import com.tomclaw.appsend.analytics.Bananalytics

interface Analytics {

    fun register()

    fun trackEvent(name: String)

}

class AnalyticsImpl(
    private val app: Application,
    private val bananalytics: Bananalytics,
) : Analytics {

    override fun register() {
        bananalytics.flushEvents()
    }

    override fun trackEvent(name: String ) {
        bananalytics.trackEvent(name)
    }

}
