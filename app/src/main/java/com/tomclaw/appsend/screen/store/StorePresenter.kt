package com.tomclaw.appsend.screen.store

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.categories.CategoriesInteractor
import com.tomclaw.appsend.categories.Category
import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.screen.store.adapter.ItemListener
import com.tomclaw.appsend.screen.store.adapter.app.AppItem
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import java.util.concurrent.TimeUnit

interface StorePresenter : ItemListener {

    fun attachView(view: StoreView)

    fun detachView()

    fun attachRouter(router: StoreRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onUpdate()

    fun invalidateApps()

    interface StoreRouter {

        fun openAppScreen(appId: String, title: String)

    }

}

class StorePresenterImpl(
    private val storeInteractor: StoreInteractor,
    private val categoriesInteractor: CategoriesInteractor,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val appConverter: AppConverter,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : StorePresenter {

    private var view: StoreView? = null
    private var router: StorePresenter.StoreRouter? = null

    private val subscriptions = CompositeDisposable()

    private var items: List<AppItem>? = state?.getParcelableArrayList(KEY_APPS)
    private var isError: Boolean = state?.getBoolean(KEY_ERROR) ?: false

    override fun attachView(view: StoreView) {
        this.view = view

        subscriptions += view.retryClicks().subscribe {
            loadApps()
        }
        subscriptions += view.refreshClicks().subscribe {
            invalidateApps()
        }
        subscriptions += view.categoriesClicks().subscribe {
            loadCategories()
        }

        if (isError) {
            onError()
        } else {
            items?.let { bindItems() } ?: loadApps()
        }
    }

    override fun detachView() {
        subscriptions.clear()
        this.view = null
    }

    override fun attachRouter(router: StorePresenter.StoreRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState() = Bundle().apply {
        putParcelableArrayList(KEY_APPS, items?.let { ArrayList(items.orEmpty()) })
        putBoolean(KEY_ERROR, isError)
    }

    override fun invalidateApps() {
        items = null
        loadApps()
    }

    private fun loadApps() {
        subscriptions += storeInteractor.listApps()
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { if (view?.isPullRefreshing() == false) view?.showProgress() }
            .subscribe(
                { onLoaded(it) },
                { onError() }
            )
    }

    private fun loadApps(offsetAppId: String) {
        subscriptions += storeInteractor.listApps(offsetAppId)
            .observeOn(schedulers.mainThread())
            .retryWhen { errors ->
                errors.flatMap {
                    println("[store] Retry after exception: " + it.message)
                    Observable.timer(3, TimeUnit.SECONDS)
                }
            }
            .subscribe(
                { onLoaded(it) },
                { onError() }
            )
    }

    private fun onLoaded(entities: List<AppEntity>) {
        isError = false
        val newItems = entities
            .map { appConverter.convert(it) }
            .toList()
            .apply { if (isNotEmpty()) last().hasMore = true }
        this.items = this.items
            ?.apply { if (isNotEmpty()) last().hasProgress = false }
            ?.plus(newItems) ?: newItems
        bindItems()
    }

    private fun bindItems() {
        val items = this.items
        when {
            items.isNullOrEmpty() -> {
                view?.showPlaceholder()
            }
            else -> {
                val dataSource = ListDataSource(items)
                adapterPresenter.get().onDataSourceChanged(dataSource)
                view?.let {
                    it.contentUpdated()
                    if (it.isPullRefreshing()) {
                        it.stopPullRefreshing()
                    } else {
                        it.showContent()
                    }
                }
            }
        }
    }

    private fun onError() {
        this.isError = true
        view?.showError()
    }

    override fun onUpdate() {
        view?.contentUpdated()
    }

    override fun onItemClick(item: Item) {
        val app = items?.find { it.id == item.id } ?: return
        router?.openAppScreen(app.appId, app.title)
    }

    override fun onLoadMore(item: Item) {
        val app = items?.find { it.id == item.id } ?: return
        loadApps(app.appId)
    }

    private fun loadCategories() {
        subscriptions += categoriesInteractor.getCategories()
            .toObservable()
            .observeOn(schedulers.mainThread())
            .retryWhen { errors ->
                errors.flatMap {
                    println("[categories] Retry after exception: " + it.message)
                    Observable.timer(3, TimeUnit.SECONDS)
                }
            }
            .subscribe(
                { onCategoriesLoaded(it) },
                { onError() }
            )
    }

    private fun onCategoriesLoaded(it: List<Category>) {
        view?.showCategories(it)
    }

}

private const val KEY_APPS = "apps"
private const val KEY_ERROR = "error"
