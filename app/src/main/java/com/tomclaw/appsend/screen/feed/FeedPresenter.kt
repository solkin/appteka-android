package com.tomclaw.appsend.screen.feed

import android.os.Bundle
import androidx.core.net.toUri
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.dto.Screenshot
import com.tomclaw.appsend.screen.feed.adapter.FeedItem
import com.tomclaw.appsend.screen.feed.adapter.ItemListener
import com.tomclaw.appsend.screen.feed.api.PostEntity
import com.tomclaw.appsend.screen.gallery.GalleryItem
import com.tomclaw.appsend.user.api.UserBrief
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.filterUnauthorizedErrors
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

    fun invalidate(offsetId: Int? = null)

    interface FeedRouter {

        fun openProfileScreen(userId: Int)

        fun openDetailsScreen(appId: String, label: String?, isFinish: Boolean)

        fun openGallery(items: List<GalleryItem>, current: Int)

        fun openLoginScreen()

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
    private val resourceProvider: FeedResourceProvider,
    private val schedulers: SchedulersFactory,
    state: Bundle?,
) : FeedPresenter {

    private var view: FeedView? = null
    private var router: FeedPresenter.FeedRouter? = null

    private val subscriptions = CompositeDisposable()

    private var items: List<FeedItem>? =
        state?.getParcelableArrayListCompat(KEY_APPS, FeedItem::class.java)
    private var error: Int = state?.getInt(KEY_ERROR) ?: ERROR_NO

    override fun attachView(view: FeedView) {
        this.view = view

        subscriptions += view.navigationClicks().subscribe { onBackPressed() }
        subscriptions += view.retryClicks().subscribe {
            loadFeed()
        }
        subscriptions += view.refreshClicks().subscribe {
            invalidate()
        }
        subscriptions += view.scrollIdle()
            .debounce(READ_DELAY_MILLIS, TimeUnit.MILLISECONDS)
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

        if (error != ERROR_NO) {
            onLoadingError(error)
            onReady()
        } else {
            items?.let { onReady() } ?: postId?.takeIf { it > 0 }?.let { loadFeed(it) }
            ?: loadFeed()
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
        putInt(KEY_ERROR, error)
    }

    override fun onBackPressed() {
        router?.leaveScreen()
    }

    override fun invalidate(offsetId: Int?) {
        items = null
        error = ERROR_NO

        val dataSource = ListDataSource(emptyList<FeedItem>())
        adapterPresenter.get().onDataSourceChanged(dataSource)
        view?.contentUpdated()

        if (offsetId != null) {
            loadFeed(offsetId)
        } else {
            loadFeed()
        }
    }

    private fun onFeedRead(postId: Int) {
        if (userId == null) {
            subscriptions += interactor.readFeed(postId)
                .observeOn(schedulers.mainThread())
                .subscribe({}, {})
        }
    }

    private fun loadFeed() {
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
                {
                    it.filterUnauthorizedErrors({
                        onLoadingError(ERROR_UNAUTHORIZED)
                    }) {
                        onLoadingError(ERROR_OTHER)
                    }

                }
            )
    }

    private fun loadFeed(offsetId: Int, direction: FeedDirection = FeedDirection.Both) {
        val items = items

        val scroll = items.isNullOrEmpty()

        var rangeInserted: Range? = null

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

    private fun onLoaded(posts: List<PostEntity>, direction: FeedDirection): Range {
        error = ERROR_NO
        val newItems = posts
            .filter { post ->
                items?.find { it.id == post.postId.toLong() } == null
            }
            .mapNotNull { converter.convert(it) }
            .toList()
            .apply { if (isNotEmpty()) applyWithDirection(direction) { hasMore = true } }

        var rangeInserted = Range(position = 0, count = newItems.size)

        this.items = this.items
            ?.apply { if (isNotEmpty()) applyWithDirection(direction) { hasProgress = false } }
            ?.let { currentItems ->
                when (direction) {
                    FeedDirection.Before -> {
                        rangeInserted = Range(position = 0, count = newItems.size)
                        newItems.plus(currentItems)
                    }

                    FeedDirection.After -> {
                        rangeInserted = Range(position = currentItems.size, count = newItems.size)
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

    private fun onReady(
        offsetId: Int? = null,
        inserted: Range? = null,
        deleted: Range? = null,
    ) {
        val items = this.items
        when {
            error != ERROR_NO -> view?.showError()
            items.isNullOrEmpty() -> view?.showPlaceholder()
            else -> {
                val dataSource = ListDataSource(items)
                adapterPresenter.get().onDataSourceChanged(dataSource)
                view?.let { view ->
                    if (view.isPullRefreshing()) {
                        view.stopPullRefreshing()
                    } else {
                        view.showContent()
                    }

                    inserted?.let { range ->
                        view.rangeInserted(range.position, range.count)
                    }
                    deleted?.let { range ->
                        view.rangeDeleted(range.position, range.count)
                    }
                    if (inserted == null && deleted == null) {
                        view.contentUpdated()
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

    private fun onLoadingError(err: Int) {
        when(err) {
            ERROR_UNAUTHORIZED -> items = converter.unauthorized()
            else -> this.error = err
        }
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
        val user = sub.user ?: return
        router?.openProfileScreen(user.userId)
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
        loadFeed(offsetId = sub.id.toInt(), direction)
    }

    override fun onImageClick(items: List<Screenshot>, clicked: Int) {
        router?.openGallery(
            items = items.map { GalleryItem(it.original.toUri(), it.width, it.height) },
            current = clicked,
        )
    }

    override fun onAppClick(appId: String, title: String?) {
        router?.openDetailsScreen(
            appId = appId,
            label = title,
            isFinish = false
        )
    }

    override fun onUserClick(user: UserBrief) {
        router?.openProfileScreen(user.userId)
    }

    override fun onMenuClick(item: FeedItem) {
        val actions = item.actions ?: return
        view?.showPostMenu(
            resourceProvider.prepareMenuActions(actions) { action ->
                when(action) {
                    MENU_DELETE -> onDeletePostClick(item)
                }
            }
        )
    }

    private fun onDeletePostClick(item: FeedItem) {
        subscriptions += interactor.deletePost(postId = item.id.toInt())
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .doOnSubscribe { view?.showProgress() }
            .doAfterTerminate { view?.showContent() }
            .subscribe(
                { onPostDeleted(item) },
                { view?.showPostDeletionFailed() }
            )
    }

    private fun onPostDeleted(item: FeedItem) {
        val items = items ?: return
        val index = items.indexOf(item)
        this.items = items.filterNot { it.id == item.id }
        val deleted = Range(position = index, count = 1)
        onReady(
            offsetId = null,
            inserted = null,
            deleted = deleted
        )
    }

    override fun onLoginClick() {
        router?.openLoginScreen()
    }

    private data class Range(
        val position: Int,
        val count: Int,
    )

}

private const val READ_DELAY_MILLIS = 200L
private const val KEY_APPS = "apps"
private const val KEY_ERROR = "error"
private const val ERROR_NO = 0
private const val ERROR_OTHER = 1
private const val ERROR_UNAUTHORIZED = 2
