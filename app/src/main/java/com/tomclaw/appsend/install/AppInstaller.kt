package com.tomclaw.appsend.install

import io.reactivex.rxjava3.core.Observable

interface AppInstaller {

    fun install(request: InstallRequest)

    /** The way out when the chosen method has failed, whatever the setting says. */
    fun installWithSystem(request: InstallRequest)

    fun status(key: String): Observable<InstallStatus>

    fun cancel(key: String)

}

/** The one place that knows which method the user has chosen. */
class AppInstallerImpl(
    private val preferences: InstallPreferences,
    private val tracker: InstallTracker,
    private val session: InstallMethod,
    private val system: InstallMethod,
) : AppInstaller {

    override fun install(request: InstallRequest) {
        val method = when (preferences.method()) {
            InstallMethodType.SESSION -> session
            InstallMethodType.SYSTEM -> system
        }
        method.install(request)
    }

    override fun installWithSystem(request: InstallRequest) {
        system.install(request)
    }

    override fun status(key: String): Observable<InstallStatus> = tracker.status(key)

    // Not routed by the preference: it may have changed since this install began
    override fun cancel(key: String) {
        session.cancel(key)
        system.cancel(key)
    }

}
