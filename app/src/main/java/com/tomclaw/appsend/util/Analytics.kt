package com.tomclaw.appsend.util

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import com.microsoft.appcenter.AppCenter
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
        val appIdentifier = getAppIdentifier(app.applicationContext)
        AppCenter.start(
            app, appIdentifier,
            com.microsoft.appcenter.analytics.Analytics::class.java,
            com.microsoft.appcenter.crashes.Crashes::class.java
        )
        bananalytics.flushEvents()
    }

    override fun trackEvent(name: String) {
        com.microsoft.appcenter.analytics.Analytics.trackEvent(name)
        bananalytics.trackEvent(name)
    }

    private fun getAppIdentifier(context: Context): String? {
        val appID = getManifestString(context, APP_IDENTIFIER_KEY)
        if (TextUtils.isEmpty(appID)) {
            throw RuntimeException("AppCenter app identifier was not configured correctly in manifest or build configuration.")
        }
        return appID
    }

    private fun getManifestString(context: Context, key: String): String? {
        return getManifestBundle(context).getString(key)
    }

    private fun getManifestBundle(context: Context): Bundle {
        return try {
            context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            ).metaData
        } catch (e: PackageManager.NameNotFoundException) {
            throw RuntimeException(e)
        }
    }

}

const val APP_IDENTIFIER_KEY = "appcenter.app_identifier"
