package com.tomclaw.appsend.screen.installed

import com.tomclaw.appsend.delivery.AppDelivery
import com.tomclaw.appsend.delivery.DeliveryState
import com.tomclaw.appsend.download.DownloadParams
import com.tomclaw.appsend.dto.UserMark
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
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy

class InstalledPresenterUpdateTest {

    private val io = TestScheduler()
    private val main = TestScheduler()
    private val deliveries = mapOf(
        "first" to BehaviorSubject.createDefault<DeliveryState>(DeliveryState.Idle),
        "second" to BehaviorSubject.createDefault<DeliveryState>(DeliveryState.Idle),
    )
    private val started = mutableListOf<DownloadParams>()
    private val confirmed = mutableListOf<String>()
    private val cancelled = mutableListOf<String>()
    private val detailsRequests = mutableListOf<String>()
    private val adapter = SimpleAdapterPresenter(ItemBinder.Builder().build())
    private val apps = deliveries.keys.map { appId ->
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

            override fun getDetails(appId: String): Observable<Details> = Observable.fromCallable {
                detailsRequests += appId
                details(appId)
            }.subscribeOn(io)
        },
        apkStorage = unused(),
        adapterPresenter = Lazy<AdapterPresenter> { adapter },
        appConverter = AppConverterImpl(),
        resourceProvider = unused(),
        appDelivery = object : AppDelivery by unused<AppDelivery>() {
            override fun state(appId: String): Observable<DeliveryState> = deliveries.getValue(appId)

            override fun confirm(appId: String) {
                confirmed += appId
            }

            override fun cancel(appId: String) {
                cancelled += appId
                deliveries.getValue(appId).onNext(DeliveryState.Idle)
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
                started += params
            }
        })
        attachView(view)
        main.triggerActions()
    }

    @Test
    fun `an update is handed to the delivery and the row follows it`() {
        presenter.onUpdateClick(item())
        assertEquals(DeliveryState.Queued, item().updateState)

        io.triggerActions()
        main.triggerActions()
        deliveries.getValue("first").onNext(DeliveryState.Downloading(40))
        main.triggerActions()

        assertEquals(listOf("first"), started.map { it.appId })
        assertEquals(DeliveryState.Downloading(40), item().updateState)
    }

    @Test
    fun `cancelling while the link is fetched starts nothing`() {
        presenter.onUpdateClick(item())

        presenter.onUpdateCancelClick(item())
        io.triggerActions()
        main.triggerActions()

        assertTrue(started.isEmpty())
        assertTrue(cancelled.isEmpty())
        assertEquals(DeliveryState.Idle, item().updateState)
    }

    @Test
    fun `cancelling one update leaves the other running`() {
        deliveries.getValue("first").onNext(DeliveryState.Installing(percent = null))
        deliveries.getValue("second").onNext(DeliveryState.Downloading(10))
        main.triggerActions()

        presenter.onUpdateCancelClick(item("first"))
        main.triggerActions()

        assertEquals(listOf("first"), cancelled)
        assertEquals(DeliveryState.Idle, item("first").updateState)
        assertEquals(DeliveryState.Downloading(10), item("second").updateState)
    }

    @Test
    fun `an unseen question is continued instead of starting the update again`() {
        deliveries.getValue("first").onNext(DeliveryState.AwaitingConfirmation)
        main.triggerActions()

        presenter.onUpdateClick(item())
        io.triggerActions()
        main.triggerActions()

        assertEquals(listOf("first"), confirmed)
        assertTrue(detailsRequests.isEmpty())
        assertTrue(started.isEmpty())
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
