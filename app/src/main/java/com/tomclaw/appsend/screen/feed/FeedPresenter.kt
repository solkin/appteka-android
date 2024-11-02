package com.tomclaw.appsend.screen.feed

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.screen.feed.adapter.FeedItem
import com.tomclaw.appsend.screen.feed.adapter.ItemListener
import com.tomclaw.appsend.screen.feed.api.PostEntity
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.getParcelableArrayListCompat
import com.tomclaw.appsend.util.retryWhenNonAuthErrors
import dagger.Lazy
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign

interface FeedPresenter : ItemListener {

    fun attachView(view: FeedView)

    fun detachView()

    fun attachRouter(router: FeedRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onUpdate()

    fun invalidateApps()

    interface FeedRouter {

        fun openProfileScreen(userId: Int)

        fun leaveScreen()

    }

}

class FeedPresenterImpl(
    private val userId: Int?,
    private val interactor: FeedInteractor,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val converter: FeedConverter,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : FeedPresenter {

    private var view: FeedView? = null
    private var router: FeedPresenter.FeedRouter? = null

    private val subscriptions = CompositeDisposable()

    private var items: List<FeedItem>? =
        state?.getParcelableArrayListCompat(KEY_APPS, FeedItem::class.java)
    private var isError: Boolean = state?.getBoolean(KEY_ERROR) ?: false

    override fun attachView(view: FeedView) {
        this.view = view

        subscriptions += view.retryClicks().subscribe {
            loadApps()
        }
        subscriptions += view.refreshClicks().subscribe {
            invalidateApps()
        }

        if (isError) {
            onError()
            onReady()
        } else {
            items?.let { onReady() } ?: loadApps()
        }
    }

    override fun detachView() {
        subscriptions.clear()
        this.view = null
    }

    override fun attachRouter(router: FeedPresenter.FeedRouter) {
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
        subscriptions += interactor.listFeed(userId)
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { if (view?.isPullRefreshing() == false) view?.showProgress() }
            .doAfterTerminate { onReady() }
            .subscribe(
                { onLoaded(it) },
                {
                    it.printStackTrace()
                    onError()
                }
            )
    }

    private fun loadApps(offsetId: Int) {
        subscriptions += interactor.listFeed(userId, offsetId)
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .doAfterTerminate { onReady() }
            .subscribe(
                { onLoaded(it) },
                { onLoadMoreError() }
            )
    }

    private fun onLoaded(posts: List<PostEntity>) {
        isError = false
        val newItems = posts
            .map { converter.convert(it) }
            .toList()
            .apply { if (isNotEmpty()) last().hasMore = true }
        this.items = this.items
            ?.apply { if (isNotEmpty()) last().hasProgress = false }
            ?.plus(newItems) ?: newItems
    }

    private fun onReady() {
        val items = this.items
        when {
            isError -> {
                view?.showError()
            }

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
    }

    private fun onLoadMoreError() {
        items?.last()
            ?.apply {
                hasProgress = false
                hasMore = false
            }
    }

    override fun onUpdate() {
        view?.contentUpdated()
    }

    override fun onItemClick(item: Item) {
        val sub = items?.find { it.id == item.id } ?: return
        router?.openProfileScreen(sub.user.userId)
    }

    override fun onLoadMore(item: Item) {
        val sub = items?.find { it.id == item.id } ?: return
        loadApps(sub.id.toInt())
    }

}

private const val KEY_APPS = "apps"
private const val KEY_ERROR = "error"
