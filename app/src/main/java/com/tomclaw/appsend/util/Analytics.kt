package com.tomclaw.appsend.util

import android.app.Application
import com.tomclaw.appsend.analytics.Bananalytics
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig

interface Analytics {

    fun register()

    fun trackEvent(name: String)

}

class AnalyticsImpl(
    private val app: Application,
    private val bananalytics: Bananalytics,
) : Analytics {

    private val Y_API_KEY = "30ce63a5-985e-4274-9c1f-69e4834c0a32"

    override fun register() {
        val config = AppMetricaConfig.newConfigBuilder(Y_API_KEY).build()
        AppMetrica.activate(app, config)
        AppMetrica.sendEventsBuffer()
        bananalytics.flushEvents()
    }

    override fun trackEvent(name: String) {
        AppMetrica.reportEvent(name)
        bananalytics.trackEvent(name)
    }

}