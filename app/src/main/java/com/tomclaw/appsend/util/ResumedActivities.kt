package com.tomclaw.appsend.util

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle

/** Opens screens on behalf of code that has no screen of its own, such as an install result. */
interface ActivityStarter {

    /**
     * Starts [intent] from the screen of ours the user is in, which Android never refuses.
     * Returns false and starts nothing when there is none. Main thread only.
     */
    fun start(intent: Intent): Boolean

}

/**
 * Follows the screen of ours the user is in. A screen only paused may already be on its way
 * out, and what it started would open behind whatever the user went to.
 */
class ResumedActivities : Application.ActivityLifecycleCallbacks, ActivityStarter {

    private var resumed: Activity? = null

    override fun start(intent: Intent): Boolean {
        val activity = resumed?.takeIf { !it.isFinishing } ?: return false
        activity.startActivity(intent)
        return true
    }

    override fun onActivityResumed(activity: Activity) {
        resumed = activity
    }

    override fun onActivityPaused(activity: Activity) {
        if (resumed === activity) resumed = null
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit

}
