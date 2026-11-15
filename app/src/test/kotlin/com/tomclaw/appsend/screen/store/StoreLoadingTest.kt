package com.tomclaw.appsend.screen.store

import com.tomclaw.appsend.categories.CategoriesInteractor
import com.tomclaw.appsend.categories.Category
import com.tomclaw.appsend.categories.CategoryConverterImpl
import com.tomclaw.appsend.dto.AppsPage
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
        adapterPresenter = Lazy { stub<AdapterPresenter> { _, _ -> error("Empty catalog has no rows") } },
        appConverter = stub { _, _ -> error("Empty catalog has no apps") },
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
