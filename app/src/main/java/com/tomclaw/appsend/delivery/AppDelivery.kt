package com.tomclaw.appsend.delivery

import com.jakewharton.rxrelay3.BehaviorRelay
import com.tomclaw.appsend.download.DownloadManager
import com.tomclaw.appsend.download.DownloadParams
import com.tomclaw.appsend.download.DownloadState
import com.tomclaw.appsend.install.AppInstaller
import com.tomclaw.appsend.install.InstallRequest
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.disposables.SerialDisposable

/**
 * Takes an app from its download to the install, whether or not the screen that asked
 * for it is still around, and is the one place screens learn where it stands from.
 * Its actions are for the main thread.
 */
interface AppDelivery {

    /** Downloads the app, unless it is already downloaded, and installs it. */
    fun deliver(params: DownloadParams)

    fun state(appId: String): Observable<DeliveryState>

    /** Shows the system's question the delivery has stopped at. */
    fun confirm(appId: String)

    fun cancel(appId: String)

}

class AppDeliveryImpl(
    private val downloadManager: DownloadManager,
    private val appInstaller: AppInstaller,
    private val schedulers: SchedulersFactory,
) : AppDelivery {

    // Downloads to install once they complete, by app id
    private val pendingInstalls = HashMap<String, Disposable>()
    private val pendingIds = BehaviorRelay.createDefault(emptySet<String>())

    override fun deliver(params: DownloadParams) {
        val appId = params.appId
        // Started first: from here on the status belongs to this download, not an older one
        downloadManager.download(params.label, params.version, appId, params.url, params.sha1)
        val watch = SerialDisposable()
        pendingInstalls.put(appId, watch)?.dispose()
        publishPending()
        watch.set(
            downloadManager.status(appId)
                .filter { it.isFinal }
                .firstElement()
                .observeOn(schedulers.mainThread())
                .subscribe { state ->
                    if (pendingInstalls[appId] !== watch) return@subscribe
                    // Before it stops being pending, so no screen sees the app idle in between
                    if (state is DownloadState.Completed) {
                        appInstaller.install(InstallRequest(appId, params.label, state.uri))
                    }
                    pendingInstalls.remove(appId)
                    publishPending()
                }
        )
    }

    override fun state(appId: String): Observable<DeliveryState> = Observable.combineLatest(
        downloadManager.status(appId),
        appInstaller.status(appId),
        pendingIds.map { appId in it }.distinctUntilChanged(),
        ::deliveryStateOf,
    ).distinctUntilChanged()

    override fun confirm(appId: String) {
        appInstaller.confirm(appId)
    }

    override fun cancel(appId: String) {
        pendingInstalls.remove(appId)?.let {
            it.dispose()
            publishPending()
        }
        downloadManager.cancel(appId)
        appInstaller.cancel(appId)
    }

    private fun publishPending() {
        pendingIds.accept(pendingInstalls.keys.toSet())
    }

    private val DownloadState.isFinal: Boolean
        get() = this == DownloadState.Idle || this == DownloadState.Failed || this is DownloadState.Completed

}
