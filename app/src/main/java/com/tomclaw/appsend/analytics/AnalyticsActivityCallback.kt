package com.tomclaw.appsend.analytics

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.tomclaw.bananalytics.Bananalytics
import com.tomclaw.bananalytics.api.BreadcrumbCategory

class AnalyticsActivityCallback(
    private val bananalytics: Bananalytics
) : Application.ActivityLifecycleCallbacks {

    override fun onActivityResumed(activity: Activity) {
        bananalytics.leaveBreadcrumb(
            message = activity.javaClass.simpleName,
            category = BreadcrumbCategory.NAVIGATION
        )
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
