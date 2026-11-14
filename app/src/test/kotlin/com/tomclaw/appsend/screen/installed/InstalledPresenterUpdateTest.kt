package com.tomclaw.appsend.screen.installed

import android.net.TestUri
import android.net.Uri
import com.tomclaw.appsend.download.COMPLETED
import com.tomclaw.appsend.download.DownloadManager
import com.tomclaw.appsend.download.DownloadParams
import com.tomclaw.appsend.download.IDLE
import com.tomclaw.appsend.download.STARTED
import com.tomclaw.appsend.dto.UserMark
import com.tomclaw.appsend.install.AppInstaller
import com.tomclaw.appsend.install.InstallRequest
import com.tomclaw.appsend.install.InstallStatus
import com.tomclaw.appsend.install.InstallTracker
import com.tomclaw.appsend.screen.details.api.AppInfo
import com.tomclaw.appsend.screen.details.api.Details
import com.tomclaw.appsend.screen.installed.adapter.app.AppItem
import com.tomclaw.appsend.screen.installed.api.UpdateEntity
import com.tomclaw.appsend.util.PackageObserver
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.adapter.AdapterPresenter
import com.tomclaw.appsend.util.adapter.ItemBinder
import com.tomclaw.appsend.util.adapter.SimpleAdapterPresenter
import dagger.Lazy
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.TestScheduler
import io.reactivex.rxjava3.subjects.BehaviorSubject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy
import java.util.concurrent.TimeUnit

class InstalledPresenterUpdateTest {

    private val io = TestScheduler()
    private val main = TestScheduler()
    private val uri = TestUri()
    private val installs = mutableListOf<InstallRequest>()
    private val lookups = mutableListOf<String>()
    private val tracker = InstallTracker()
    private val downloads = mapOf(
        "first" to BehaviorSubject.createDefault(IDLE),
        "second" to BehaviorSubject.createDefault(IDLE),
    )
    private var availableUri: Uri? = uri
    private val adapter = SimpleAdapterPresenter(ItemBinder.Builder().build())
    private val apps = downloads.keys.map { appId ->
        InstalledAppEntity(
            packageName = appId,
            label = appId,
            icon = null,
            verName = "1",
            verCode = 1,
            firstInstallTime = 0,
            lastUpdateTime = 0,
            size = 1,
            path = null,
            isUserApp = true,
            editionKey = null,
        )
    }
    private val updates = apps.map { app ->
        UpdateEntity(
            appId = app.packageName,
            size = 1,
            time = 0,
            title = app.label,
            packageName = app.packageName,
            verName = "2",
            verCode = 2,
            downloads = 0,
            publishTime = null,
            userId = null,
            icon = null,
        )
    }

    private val view = object : InstalledView by unused<InstalledView>() {
        override fun navigationClicks(): Observable<Unit> = Observable.never()
        override fun itemMenuClicks(): Observable<Pair<Int, AppItem>> = Observable.never()
        override fun searchTextChanged(): Observable<String> = Observable.never()
        override fun retryClicks(): Observable<Unit> = Observable.never()
        override fun refreshClicks(): Observable<Unit> = Observable.never()
        override fun isPullRefreshing(): Boolean = false
        override fun showProgress() = Unit
        override fun showContent() = Unit
        override fun contentUpdated() = Unit
        override fun contentUpdated(position: Int) = Unit
    }

    private val presenter = InstalledPresenterImpl(
        picker = false,
        preferencesProvider = object : InstalledPreferencesProvider by unused<InstalledPreferencesProvider>() {
            override fun isShowSystemApps(): Boolean = false
            override fun getSortOrder(): SortOrder = SortOrder.ASCENDING
        },
        interactor = object : InstalledInteractor by unused<InstalledInteractor>() {
            override fun listInstalledApps(systemApps: Boolean): Observable<List<InstalledAppEntity>> =
                Observable.just(apps)

            override fun getUpdates(apps: Map<String, Long>, certs: Map<String, String>): Observable<InstalledUpdates> =
                Observable.just(InstalledUpdates(updates, emptyList()))

            override fun getDetails(appId: String): Observable<Details> = Observable.just(details(appId))
        },
        apkStorage = unused(),
        adapterPresenter = Lazy<AdapterPresenter> { adapter },
        appConverter = AppConverterImpl(),
        resourceProvider = unused(),
        downloadManager = object : DownloadManager by unused<DownloadManager>() {
            override fun status(appId: String): Observable<Int> = downloads.getValue(appId)

            override fun getInstallUri(label: String, version: String, appId: String): Uri? {
                lookups += appId
                return availableUri
            }

            override fun cancel(appId: String) {
                downloads.getValue(appId).onNext(IDLE)
            }
        },
        appInstaller = object : AppInstaller by unused<AppInstaller>() {
            override fun status(key: String): Observable<InstallStatus> = tracker.status(key)

            override fun install(request: InstallRequest) {
                installs += request
                tracker.update(request.key, InstallStatus.Writing(0))
            }
        },
        packageObserver = object : PackageObserver by unused<PackageObserver>() {
            override fun observe(packageName: String): Observable<Int> = Observable.just(1)
        },
        abiResourceProvider = unused(),
        schedulers = object : SchedulersFactory {
            override fun io(): Scheduler = io
            override fun mainThread(): Scheduler = main
        },
        state = null,
    ).apply {
        attachRouter(object : InstalledPresenter.InstalledRouter by unused<InstalledPresenter.InstalledRouter>() {
            override fun requestDownloadPermissions(callback: (Boolean) -> Unit) = callback(true)

            override fun startDownload(params: DownloadParams) {
                downloads.getValue(params.appId).onNext(STARTED)
            }
        })
        attachView(view)
        main.triggerActions()
    }

    @Test
    fun `cancel stops waiting for the downloaded file`() {
        availableUri = null
        startUpdate()
        io.triggerActions()
        assertEquals(listOf("first"), lookups)

        presenter.onUpdateCancelClick(item())
        availableUri = uri
        io.advanceTimeBy(1, TimeUnit.SECONDS)
        main.triggerActions()

        assertEquals(listOf("first"), lookups)
        assertTrue(installs.isEmpty())
        assertEquals(IDLE, item().updateState)
    }

    @Test
    fun `cancel discards a file lookup queued for the main thread`() {
        startUpdate()
        io.triggerActions()
        assertEquals(listOf("first"), lookups)

        presenter.onUpdateCancelClick(item())
        main.triggerActions()

        assertTrue(installs.isEmpty())
        assertEquals(IDLE, item().updateState)
    }

    @Test
    fun `retry after cancellation installs once`() {
        startUpdate()
        io.triggerActions()
        presenter.onUpdateCancelClick(item())
        main.triggerActions()
        assertTrue(installs.isEmpty())
        assertEquals(IDLE, item().updateState)

        startUpdate()
        assertTrue(installs.isEmpty())
        io.triggerActions()
        main.triggerActions()

        assertEquals(listOf("first", "first"), lookups)
        assertEquals(listOf("first"), installs.map { it.key })
        assertSame(uri, installs.single().uri)
    }

    @Test
    fun `cancelling one update leaves the other installation running`() {
        startUpdate("first")
        startUpdate("second")
        io.triggerActions()

        presenter.onUpdateCancelClick(item("first"))
        main.triggerActions()

        assertEquals(listOf("second"), installs.map { it.key })
    }

    @Test
    fun `detaching cancels preparation and reattaching resumes the requested install`() {
        startUpdate()
        io.triggerActions()
        presenter.detachView()
        main.triggerActions()
        assertTrue(installs.isEmpty())

        presenter.attachView(view)
        main.triggerActions()
        io.triggerActions()
        main.triggerActions()

        assertEquals(listOf("first"), installs.map { it.key })
    }

    @Test
    fun `a completed download installs once even as installation progress changes`() {
        startUpdate()
        io.triggerActions()
        main.triggerActions()
        tracker.update("first", InstallStatus.Writing(50))
        main.triggerActions()
        io.advanceTimeBy(1, TimeUnit.SECONDS)
        main.triggerActions()

        assertEquals(listOf("first"), installs.map { it.key })
        assertSame(uri, installs.single().uri)
        assertEquals(50, item().updateState)
    }

    private fun startUpdate(appId: String = "first") {
        presenter.onUpdateClick(item(appId))
        main.triggerActions()
        downloads.getValue(appId).onNext(COMPLETED)
        main.triggerActions()
    }

    private fun item(appId: String = "first"): AppItem =
        (0 until adapter.getItemCount())
            .map { adapter.getItem(it) }
            .filterIsInstance<AppItem>()
            .first { it.packageName == appId }

    private fun details(appId: String) = Details(
        url = "",
        topicId = null,
        msgCount = null,
        actions = null,
        ratingsList = null,
        userRating = null,
        versions = null,
        link = "https://example.com/$appId.apk",
        expiresIn = 60,
        meta = null,
        info = AppInfo(
            appId = appId,
            label = appId,
            labels = null,
            icon = null,
            fileStatus = 0,
            packageName = appId,
            version = "2",
            versionCode = 2,
            sdkVersion = 23,
            androidVersion = "6.0",
            permissions = null,
            size = 1,
            downloads = null,
            downloadTime = null,
            favorites = null,
            time = 0,
            sha1 = "",
            author = UserMark(1),
            abi = null,
            languages = null,
        ),
        isFavorite = false,
        security = null,
        translation = null,
        developer = null,
        moderation = null,
    )

}

private inline fun <reified T : Any> unused(): T = requireNotNull(
    T::class.java.cast(
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, _ ->
            error("Unexpected ${T::class.java.simpleName}.${method.name}")
        }
    )
)
