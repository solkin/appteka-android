package com.tomclaw.appsend.screen.feed

import android.net.Uri
import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.dto.Screenshot
import com.tomclaw.appsend.screen.feed.adapter.FeedItem
import com.tomclaw.appsend.screen.feed.adapter.ItemListener
import com.tomclaw.appsend.screen.feed.api.PostEntity
import com.tomclaw.appsend.screen.gallery.GalleryItem
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

    fun onBackPressed()

    fun invalidateApps()

    interface FeedRouter {

        fun openProfileScreen(userId: Int)

        fun openGallery(items: List<GalleryItem>, current: Int)

        fun leaveScreen()

    }

}

class FeedPresenterImpl(
    private val userId: Int?,
    private val postId: Int?,
    private val withToolbar: Boolean?,
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

        subscriptions += view.navigationClicks().subscribe { onBackPressed() }
        subscriptions += view.retryClicks().subscribe {
            loadApps()
        }
        subscriptions += view.refreshClicks().subscribe {
            invalidateApps()
        }

        if (withToolbar == true) {
            view.showToolbar()
        } else {
            view.hideToolbar()
        }

        if (isError) {
            onError()
            onReady()
        } else {
            items?.let { onReady() } ?: postId?.let { loadApps(it) } ?: loadApps()
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

    override fun onBackPressed() {
        router?.leaveScreen()
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
        val scroll = items.isNullOrEmpty()
        subscriptions += interactor.listFeed(userId, offsetId)
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .doAfterTerminate { onReady(offsetId.takeIf { scroll }) }
            .subscribe(
                { onLoaded(it) },
                { onLoadMoreError() }
            )
    }

    private fun onLoaded(posts: List<PostEntity>) {
        isError = false
        val newItems = posts
            .filterNot { post -> items?.find { it.id == post.postId.toLong() } != null }
            .map { converter.convert(it) }
            .toList()
            .apply { if (isNotEmpty()) last().hasMore = true }
        this.items = this.items
            ?.apply { if (isNotEmpty()) last().hasProgress = false }
            ?.plus(newItems) ?: newItems
    }

    private fun onReady(offsetId: Int? = null) {
        val items = this.items
        when {
            isError -> view?.showError()
            items.isNullOrEmpty() -> view?.showPlaceholder()
            else -> {
                val dataSource = ListDataSource(items)
                adapterPresenter.get().onDataSourceChanged(dataSource)
                view?.let { view ->
                    view.contentUpdated()
                    if (view.isPullRefreshing()) {
                        view.stopPullRefreshing()
                    } else {
                        view.showContent()
                    }
                    offsetId?.let { offsetId ->
                        items
                            .indexOfFirst { it.id.toInt() == offsetId }
                            .let { view.scrollTo(it) }
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

    override fun onImageClick(image: Screenshot) {
        router?.openGallery(
            items = listOf(GalleryItem(Uri.parse(image.original), image.width, image.height)),
            current = 0,
        )
    }

}

private const val KEY_APPS = "apps"
private const val KEY_ERROR = "error"
