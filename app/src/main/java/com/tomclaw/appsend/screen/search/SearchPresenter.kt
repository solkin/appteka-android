package com.tomclaw.appsend.screen.search

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.screen.store.AppConverter
import com.tomclaw.appsend.screen.store.adapter.app.AppItem
import com.tomclaw.appsend.screen.store.adapter.ItemListener
import com.tomclaw.appsend.util.Analytics
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.getParcelableArrayListCompat
import com.tomclaw.appsend.util.retryWhenNonAuthErrors
import dagger.Lazy
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import java.util.concurrent.TimeUnit

interface SearchPresenter : ItemListener {

    fun attachView(view: SearchView)

    fun detachView()

    fun attachRouter(router: SearchRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun invalidateSearch()

    interface SearchRouter {

        fun openAppScreen(appId: String, title: String)

    }

}

class SearchPresenterImpl(
    private val searchInteractor: SearchInteractor,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val appConverter: AppConverter,
    private val analytics: Analytics,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : SearchPresenter {

    private var view: SearchView? = null
    private var router: SearchPresenter.SearchRouter? = null

    private val subscriptions = CompositeDisposable()

    private var items: List<AppItem>? =
        state?.getParcelableArrayListCompat(KEY_APPS, AppItem::class.java)
    private var isError: Boolean = state?.getBoolean(KEY_ERROR) == true
    private var query: String = state?.getString(KEY_QUERY) ?: ""

    override fun attachView(view: SearchView) {
        this.view = view

        subscriptions += view.retryClicks().subscribe {
            performSearch()
        }
        subscriptions += view.refreshClicks().subscribe {
            invalidateSearch()
            analytics.trackEvent("search-refresh")
        }

        // Debounce search queries
        subscriptions += view.queryTextChanges()
            .debounce(DEBOUNCE_DELAY_MS, TimeUnit.MILLISECONDS, schedulers.mainThread())
            .distinctUntilChanged()
            .subscribe { text ->
                query = text
                if (text.isBlank()) {
                    clearResults()
                } else {
                    performSearch()
                }
            }

        // Restore query text
        if (query.isNotEmpty()) {
            view.setQueryText(query)
        }

        if (isError) {
            onError()
        } else {
            items?.let { bindItems() } ?: run {
                if (query.isNotEmpty()) {
                    performSearch()
                } else {
                    view.showPlaceholder()
                }
            }
        }
    }

    override fun detachView() {
        subscriptions.clear()
        this.view = null
    }

    override fun attachRouter(router: SearchPresenter.SearchRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState() = Bundle().apply {
        putParcelableArrayList(KEY_APPS, items?.let { ArrayList(items.orEmpty()) })
        putBoolean(KEY_ERROR, isError)
        putString(KEY_QUERY, query)
    }

    override fun invalidateSearch() {
        items = null
        isError = false
        performSearch()
    }

    private fun performSearch() {
        val currentQuery = query.trim()
        if (currentQuery.isEmpty()) {
            clearResults()
            return
        }

        // Clear previous results for new search
        items = null

        subscriptions += searchInteractor.searchApps(currentQuery)
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { if (view?.isPullRefreshing() == false) view?.showProgress() }
            .subscribe(
                { onLoaded(it, isNewSearch = true) },
                { onError() }
            )
    }

    private fun loadMore(offset: Int) {
        val currentQuery = query.trim()
        if (currentQuery.isEmpty()) {
            return
        }

        subscriptions += searchInteractor.searchApps(currentQuery, offset)
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .subscribe(
                { onLoaded(it, isNewSearch = false) },
                { onError() }
            )
    }

    private fun onLoaded(entities: List<AppEntity>, isNewSearch: Boolean) {
        isError = false
        val newItems = entities
            .map { appConverter.convert(it) }
            .toList()
            .apply { if (isNotEmpty()) last().hasMore = true }
        
        this.items = if (isNewSearch) {
            // Replace items for new search
            newItems
        } else {
            // Append items for pagination
            this.items
                ?.apply { if (isNotEmpty()) last().hasProgress = false }
                ?.plus(newItems) ?: newItems
        }
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

    private fun clearResults() {
        items = null
        view?.showPlaceholder()
    }

    private fun onError() {
        this.isError = true
        view?.showError()
    }

    override fun onItemClick(item: Item) {
        val app = items?.find { it.id == item.id } ?: return
        router?.openAppScreen(app.appId, app.title)
    }

    override fun onLoadMore(item: Item) {
        val offset = items?.size ?: return
        loadMore(offset)
    }

}

private const val KEY_APPS = "apps"
private const val KEY_ERROR = "error"
private const val KEY_QUERY = "query"
private const val DEBOUNCE_DELAY_MS = 500L

