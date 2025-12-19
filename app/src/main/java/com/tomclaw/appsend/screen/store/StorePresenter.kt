package com.tomclaw.appsend.screen.store

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.categories.CategoriesInteractor
import com.tomclaw.appsend.categories.Category
import com.tomclaw.appsend.categories.CategoryConverter
import com.tomclaw.appsend.categories.CategoryItem
import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.screen.store.adapter.ItemListener
import com.tomclaw.appsend.screen.store.adapter.app.AppItem
import com.tomclaw.appsend.util.Analytics
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.getParcelableArrayListCompat
import com.tomclaw.appsend.util.getParcelableCompat
import com.tomclaw.appsend.util.retryWhenNonAuthErrors
import dagger.Lazy
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign

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
    private val categoryConverter: CategoryConverter,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val appConverter: AppConverter,
    private val analytics: Analytics,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : StorePresenter {

    private var view: StoreView? = null
    private var router: StorePresenter.StoreRouter? = null

    private val subscriptions = CompositeDisposable()

    private var items: List<AppItem>? =
        state?.getParcelableArrayListCompat(KEY_APPS, AppItem::class.java)

    private var isError: Boolean =
        state?.getBoolean(KEY_ERROR) == true

    private var category: CategoryItem? =
        state?.getParcelableCompat(KEY_CATEGORY_ID, CategoryItem::class.java)

    private var isRefreshing: Boolean = false

    override fun attachView(view: StoreView) {
        this.view = view

        subscriptions += view.retryClicks().subscribe {
            isRefreshing = false
            invalidateApps()
        }

        subscriptions += view.refreshClicks().subscribe {
            isRefreshing = true
            loadApps()
            analytics.trackEvent("store-refresh")
        }

        subscriptions += view.categoriesButtonClicks().subscribe {
            loadCategories()
            analytics.trackEvent("store-category-list")
        }

        subscriptions += view.categorySelectedClicks().subscribe { categoryItem ->
            isRefreshing = false
            onCategorySelected(categoryItem)
            analytics.trackEvent("store-category-selected")
        }

        subscriptions += view.categoryClearedClicks().subscribe {
            isRefreshing = false
            onCategorySelected()
            analytics.trackEvent("store-category-cleared")
        }

        when {
            isError -> {
                view.showError()
            }

            items != null -> {
                view.setSelectedCategory(category)
                bindItems()
            }

            else -> {
                view.setSelectedCategory(category)
                view.showProgress()
                loadApps()
            }
        }
    }

    override fun detachView() {
        subscriptions.clear()
        view = null
    }

    override fun attachRouter(router: StorePresenter.StoreRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState(): Bundle = Bundle().apply {
        putBoolean(KEY_ERROR, isError)
        putParcelable(KEY_CATEGORY_ID, category)
    }

    override fun invalidateApps() {
        items = null
        isError = false
        isRefreshing = false
        view?.showProgress()
        loadApps()
    }

    private fun loadApps() {
        subscriptions += storeInteractor.listApps(categoryId = category?.id)
            .observeOn(schedulers.mainThread())
            .subscribe(
                { onLoaded(it) },
                { onError() }
            )
    }

    private fun loadApps(offsetAppId: String) {
        subscriptions += storeInteractor.listApps(offsetAppId, category?.id)
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
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

        items = items
            ?.apply { if (isNotEmpty()) last().hasProgress = false }
            ?.plus(newItems)
            ?: newItems

        bindItems()
        isRefreshing = false
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
        isError = true
        isRefreshing = false
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
            .retryWhenNonAuthErrors()
            .subscribe(
                { onCategoriesLoaded(it) },
                { onError() }
            )
    }

    private fun onCategoriesLoaded(categories: List<Category>) {
        val items = categories
            .map { categoryConverter.convert(it) }
            .sortedBy { it.title }

        view?.showCategories(items)
    }

    private fun onCategorySelected(categoryItem: CategoryItem? = null) {
        category = categoryItem
        view?.setSelectedCategory(categoryItem)
        invalidateApps()
    }
}

private const val KEY_APPS = "apps"
private const val KEY_ERROR = "error"
private const val KEY_CATEGORY_ID = "category"