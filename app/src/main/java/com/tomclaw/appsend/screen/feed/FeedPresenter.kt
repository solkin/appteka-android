package com.tomclaw.appsend.screen.feed

import android.net.Uri
import android.os.Bundle
import androidx.core.net.toUri
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.dto.Screenshot
import com.tomclaw.appsend.screen.details.adapter.screenshot.ScreenshotItem
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
import java.util.concurrent.TimeUnit

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
        subscriptions += view.scrollIdle()
            .debounce(1000, TimeUnit.MILLISECONDS)
            .subscribe { position ->
                items?.get(position)?.let { item ->
                    onFeedRead(postId = item.id.toInt())
                }
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

    private fun onFeedRead(postId: Int) {
        if (userId == null) {
            subscriptions += interactor.readFeed(postId)
                .observeOn(schedulers.mainThread())
                .subscribe({}, {})
        }
    }

    private fun loadApps() {
        val direction = FeedDirection.Both
        var offsetId: Int? = null
        subscriptions += interactor.listFeed(userId, postId = null, direction)
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { if (view?.isPullRefreshing() == false) view?.showProgress() }
            .doAfterTerminate { onReady(offsetId) }
            .subscribe(
                { result ->
                    onLoaded(result.posts, direction)
                    if (result.offsetId > 0) {
                        offsetId = result.offsetId
                    }
                },
                { onError() }
            )
    }

    private fun loadApps(offsetId: Int, direction: FeedDirection = FeedDirection.Both) {
        val items = items

        val scroll = items.isNullOrEmpty()

        var rangeInserted: RangeInserted? = null

        subscriptions += interactor.listFeed(userId, offsetId, direction)
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .doAfterTerminate {
                onReady(offsetId.takeIf { scroll }, rangeInserted)
            }
            .subscribe(
                { rangeInserted = onLoaded(it.posts, direction) },
                { onLoadMoreError(it) }
            )
    }

    private fun onLoaded(posts: List<PostEntity>, direction: FeedDirection): RangeInserted {
        isError = false
        val newItems = posts
            .filter { post ->
                items?.find { it.id == post.postId.toLong() } == null
            }
            .map { converter.convert(it) }
            .toList()
            .apply { if (isNotEmpty()) applyWithDirection(direction) { hasMore = true } }

        var rangeInserted = RangeInserted(position = 0, count = newItems.size)

        this.items = this.items
            ?.apply { if (isNotEmpty()) applyWithDirection(direction) { hasProgress = false } }
            ?.let { currentItems ->
                when (direction) {
                    FeedDirection.Before -> {
                        rangeInserted = RangeInserted(position = 0, count = newItems.size)
                        newItems.plus(currentItems)
                    }

                    FeedDirection.After -> {
                        rangeInserted =
                            RangeInserted(position = currentItems.size, count = newItems.size)
                        currentItems.plus(newItems)
                    }

                    FeedDirection.Both -> newItems
                }
            } ?: newItems
        return rangeInserted
    }

    private fun <T> List<T>.applyWithDirection(direction: FeedDirection, fn: T.() -> Unit) {
        if (direction == FeedDirection.Before || direction == FeedDirection.Both) fn.invoke(first())
        if (direction == FeedDirection.After || direction == FeedDirection.Both) fn.invoke(last())
    }

    private fun onReady(offsetId: Int? = null, rangeInserted: RangeInserted? = null) {
        val items = this.items
        when {
            isError -> view?.showError()
            items.isNullOrEmpty() -> view?.showPlaceholder()
            else -> {
                val dataSource = ListDataSource(items)
                adapterPresenter.get().onDataSourceChanged(dataSource)
                view?.let { view ->
                    rangeInserted?.let { range ->
                        view.rangeInserted(range.position, range.count)
                    } ?: view.contentUpdated()

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

    private fun onLoadMoreError(ex: Throwable) {
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
        val sub: FeedItem
        val first = items?.first()
        val last = items?.last()
        val direction = when (item) {
            first -> {
                sub = first
                FeedDirection.Before
            }

            last -> {
                sub = last
                FeedDirection.After
            }

            else -> return
        }
        loadApps(offsetId = sub.id.toInt(), direction)
    }

    override fun onImageClick(items: List<Screenshot>, clicked: Int) {
        router?.openGallery(
            items = items.map { GalleryItem(it.original.toUri(), it.width, it.height) },
            current = clicked,
        )
    }

    private data class RangeInserted(
        val position: Int,
        val count: Int,
    )

}

private const val KEY_APPS = "apps"
private const val KEY_ERROR = "error"
