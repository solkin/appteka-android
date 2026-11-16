package com.tomclaw.appsend.delivery

import android.net.TestUri
import android.net.Uri
import com.tomclaw.appsend.download.DownloadManager
import com.tomclaw.appsend.download.DownloadParams
import com.tomclaw.appsend.download.DownloadState
import com.tomclaw.appsend.install.AppInstaller
import com.tomclaw.appsend.install.InstallRequest
import com.tomclaw.appsend.install.InstallStatus
import com.tomclaw.appsend.install.InstallTracker
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.TestScheduler
import io.reactivex.rxjava3.subjects.BehaviorSubject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class AppDeliveryTest {

    private val main = TestScheduler()
    private val uri = TestUri()
    private val download = BehaviorSubject.createDefault<DownloadState>(DownloadState.Idle)
    private var downloaded: Uri? = null
    private val tracker = InstallTracker()
    private val installs = mutableListOf<InstallRequest>()
    private val cancels = mutableListOf<String>()

    private val downloadManager = object : DownloadManager {
        override fun status(appId: String): Observable<DownloadState> = download

        override fun download(label: String, version: String, appId: String, url: String, sha1: String?): String {
            download.onNext(downloaded?.let { DownloadState.Completed(it) } ?: DownloadState.Queued)
            return label
        }

        override fun exists(label: String, version: String, appId: String): Boolean = error("Not used")

        override fun cancel(appId: String) {
            cancels += "download"
            download.onNext(DownloadState.Idle)
        }
    }

    private val appInstaller = object : AppInstaller {
        override fun install(request: InstallRequest) {
            installs += request
            tracker.update(request.key, InstallStatus.Writing(0))
        }

        override fun installWithSystem(request: InstallRequest) = error("Not used")

        override fun confirm(key: String, confirmation: android.content.Intent?) = error("Not used")

        override fun status(key: String): Observable<InstallStatus> = tracker.status(key)

        override fun cancel(key: String) {
            cancels += "install"
        }
    }

    private val delivery = AppDeliveryImpl(
        downloadManager,
        appInstaller,
        object : SchedulersFactory {
            override fun io(): Scheduler = error("Not used")
            override fun mainThread(): Scheduler = main
        },
    )

    @Test
    fun `a completed download is installed once and never shows as idle on the way`() {
        delivery.deliver(params)
        val states = delivery.state(APP_ID).test()

        download.onNext(DownloadState.Running(50))
        download.onNext(DownloadState.Completed(uri))
        main.triggerActions()

        assertEquals(listOf(APP_ID), installs.map { it.key })
        assertSame(uri, installs.single().uri)
        states.assertValues(
            DeliveryState.Queued,
            DeliveryState.Downloading(50),
            DeliveryState.Installing(0),
        )
    }

    @Test
    fun `an app downloaded before is installed right away`() {
        downloaded = uri

        delivery.deliver(params)
        main.triggerActions()

        assertEquals(listOf(APP_ID), installs.map { it.key })
    }

    @Test
    fun `a failed download installs nothing and leaves the app idle`() {
        delivery.deliver(params)
        val states = delivery.state(APP_ID).test()

        download.onNext(DownloadState.Failed)
        main.triggerActions()

        assertEquals(emptyList<InstallRequest>(), installs)
        states.assertValues(DeliveryState.Queued, DeliveryState.Idle)
    }

    @Test
    fun `cancelling drops the install a completing download was about to start`() {
        delivery.deliver(params)
        download.onNext(DownloadState.Completed(uri))

        delivery.cancel(APP_ID)
        main.triggerActions()

        assertEquals(emptyList<InstallRequest>(), installs)
        assertEquals(listOf("download", "install"), cancels)
        delivery.state(APP_ID).test().assertValue(DeliveryState.Idle)
    }

    @Test
    fun `asking again while downloading still installs once`() {
        delivery.deliver(params)
        delivery.deliver(params)

        download.onNext(DownloadState.Completed(uri))
        main.triggerActions()

        assertEquals(listOf(APP_ID), installs.map { it.key })
    }

    private val params = DownloadParams(
        label = "App",
        version = "1.0",
        icon = null,
        appId = APP_ID,
        url = "https://example.com/app.apk",
        sha1 = null,
    )

}

private const val APP_ID = "app"
