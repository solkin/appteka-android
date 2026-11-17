package com.tomclaw.appsend.screen.store

import com.tomclaw.appsend.categories.CategoriesInteractor
import com.tomclaw.appsend.categories.Category
import com.tomclaw.appsend.categories.CategoryConverterImpl
import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.dto.AppsPage
import com.tomclaw.appsend.screen.store.adapter.app.AppItem
import com.tomclaw.appsend.util.Analytics
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.adapter.AdapterPresenter
import dagger.Lazy
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy
import java.util.Locale

class StoreLoadingTest {
    private var categoriesCalls = 0
    private var appsCalls = 0
    private var categories: () -> Single<List<Category>> = { Single.just(emptyList()) }
    private var apps: () -> Observable<AppsPage> = { Observable.just(AppsPage(emptyList())) }
    private var appConverter: AppConverter = stub { _, _ -> error("Empty catalog has no apps") }
    private var adapterPresenter: AdapterPresenter = stub { _, _ -> error("Empty catalog has no rows") }
    private val retry = PublishSubject.create<Unit>()
    private val openSource = PublishSubject.create<Boolean>()
    private val calls = mutableListOf<String>()
    private val view = stub<StoreView> { method, _ ->
        when (method) {
            "retryClicks" -> retry
            "openSourceClicks" -> openSource
            "categorySelectedClicks" -> Observable.never<Int>()
            "exclusiveClicks" -> Observable.never<Boolean>()
            "refreshClicks", "contentFilterClicks" -> Observable.never<Unit>()
            "isPullRefreshing" -> false
            else -> { calls += method; null }
        }
    }

    @Test
    fun `offline categories show an error and retry loads both categories and apps`() {
        categories = { Single.error(java.io.IOException("offline")) }
        val presenter = presenter()
        try {
            presenter.attachView(view)
            assertTrue(calls.contains("showError"))
            assertEquals(1, categoriesCalls)
            assertEquals(0, appsCalls)
            calls.clear()
            retry.onNext(Unit)
            assertTrue(calls.contains("showError"))
            assertEquals(2, categoriesCalls)
            categories = { Single.just(emptyList()) }
            retry.onNext(Unit)
            assertEquals(3, categoriesCalls)
            assertEquals(1, appsCalls)
            assertTrue(calls.contains("showCategories"))
            assertTrue(calls.contains("showPlaceholder"))
        } finally { presenter.detachView() }
    }

    @Test
    fun `apps timeout ends loading and retry retains the loaded categories`() {
        apps = { Observable.error(java.net.SocketTimeoutException("timeout")) }
        val presenter = presenter()
        try {
            presenter.attachView(view)
            assertTrue(calls.contains("showError"))
            assertEquals(1, appsCalls)
            apps = { Observable.just(AppsPage(emptyList())) }
            calls.clear()
            retry.onNext(Unit)
            assertEquals(1, categoriesCalls)
            assertEquals(2, appsCalls)
            assertTrue(calls.contains("showPlaceholder"))
            assertFalse(calls.contains("showError"))
        } finally { presenter.detachView() }
    }

    @Test
    fun `changed filters cancel the old request and detach cancels the current request`() {
        val old = PublishSubject.create<AppsPage>()
        val current = PublishSubject.create<AppsPage>()
        apps = { if (appsCalls == 1) old else current }
        val presenter = presenter()
        presenter.attachView(view)
        assertTrue(old.hasObservers())
        openSource.onNext(true)
        assertFalse(old.hasObservers())
        assertTrue(current.hasObservers())
        calls.clear()
        old.onNext(AppsPage(emptyList()))
        assertFalse(calls.contains("showPlaceholder"))
        presenter.detachView()
        assertFalse(current.hasObservers())
    }

    @Test
    fun `failed next page keeps the loaded catalog instead of showing an error`() {
        val nextPage = PublishSubject.create<AppsPage>()
        apps = { if (appsCalls == 1) Observable.just(AppsPage(listOf(app("1")))) else nextPage }
        appConverter = object : AppConverter {
            override fun convert(appEntity: AppEntity) = item(appEntity.appId)
        }
        adapterPresenter = stub { _, _ -> null }
        val presenter = presenter()
        try {
            presenter.attachView(view)
            assertTrue(calls.contains("showContent"))
            presenter.onLoadMore(item("1"))
            assertTrue(nextPage.hasObservers())
            calls.clear()
            nextPage.onError(java.io.IOException("offline"))
            assertFalse(calls.contains("showError"))
        } finally { presenter.detachView() }
    }

    @Test
    fun `a row with a release opens the release, one without opens the build`() {
        apps = { Observable.just(AppsPage(listOf(app("1"), app("2")))) }
        appConverter = object : AppConverter {
            override fun convert(appEntity: AppEntity) =
                item(appEntity.appId, ref = "com.tomclaw.1/a1b2c3d4e5f6a7b8c9d0e1f2/1".takeIf { appEntity.appId == "1" })
        }
        adapterPresenter = stub { _, _ -> null }
        val opened = mutableListOf<String>()
        val presenter = presenter()
        try {
            presenter.attachView(view)
            presenter.attachRouter(stub { method, args -> opened += "$method ${args[0]}"; null })
            presenter.onItemClick(item("1"))
            presenter.onItemClick(item("2"))
            assertEquals(
                listOf("openReleaseScreen com.tomclaw.1/a1b2c3d4e5f6a7b8c9d0e1f2/1", "openAppScreen 2"),
                opened,
            )
        } finally { presenter.detachRouter(); presenter.detachView() }
    }

    private fun app(id: String) = AppEntity(
        appId = id,
        packageName = "com.tomclaw.$id",
        icon = null,
        title = "app $id",
        verName = "1.0",
        verCode = 1,
        time = 0,
        size = 0,
        rating = 0f,
        downloads = 0,
        status = 0,
        category = null,
        exclusive = false,
        sourceUrl = null,
        abi = null,
    )

    private fun item(id: String, ref: String? = null) = AppItem(
        id = id.toLong(),
        appId = id,
        ref = ref,
        icon = null,
        title = "app $id",
        version = "1.0",
        size = "",
        rating = 0f,
        downloads = 0,
        status = 0,
        category = null,
        exclusive = false,
        openSource = false,
        mod = false,
        isAbiCompatible = true,
    )

    private fun presenter(): StorePresenterImpl = StorePresenterImpl(
        storeInteractor = object : StoreInteractor {
            override fun listApps(offsetAppId: String?, categoryId: Int?, openSource: Boolean, exclusive: Boolean): Observable<AppsPage> =
                Observable.defer { appsCalls++; apps() }
        },
        categoriesInteractor = object : CategoriesInteractor {
            override fun getCategories(): Single<List<Category>> = Single.defer { categoriesCalls++; categories() }
        },
        categoryConverter = CategoryConverterImpl(Locale.ENGLISH),
        dropdownItemConverter = object : CategoryDropdownItemConverter {
            override fun convert(items: List<com.tomclaw.appsend.categories.CategoryItem>) = listOf(CategoryDropdownItem(0, "All", null, 0))
        },
        adapterPresenter = Lazy { adapterPresenter },
        appConverter = appConverter,
        analytics = stub<Analytics> { _, _ -> null },
        schedulers = object : SchedulersFactory {
            override fun io(): Scheduler = Schedulers.trampoline()
            override fun mainThread(): Scheduler = Schedulers.trampoline()
        },
        state = null,
    )

    private inline fun <reified T> stub(crossinline call: (String, Array<out Any?>) -> Any?): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, args ->
            call(method.name, args ?: emptyArray())
        } as T
}
