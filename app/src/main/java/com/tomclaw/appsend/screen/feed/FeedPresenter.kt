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

    fun scrollToBottom()

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
        subscriptions += view.scrollIdle()
            .debounce(READ_DELAY_MILLIS, TimeUnit.MILLISECONDS)
            .subscribe { position ->
                if (position < 0) {
                    return@subscribe
                }
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
        } else {
            items?.let { bindItems() }
                ?: postId?.takeIf { it > 0 }?.let { loadFeed(it) }
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
        subscriptions += interactor.listFeed(userId, postId = null, direction)
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { view?.showProgress() }
            .subscribe(
                { result ->
                    onLoaded(result.posts, direction)
                    bindItems(offsetId = result.offsetId.takeIf { it > 0 })
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
        subscriptions += interactor.listFeed(userId, offsetId, direction)
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { view?.showProgress() }
            .retryWhenNonAuthErrors()
            .subscribe(
                {
                    bindItems(
                        offsetId = offsetId.takeIf { scroll },
                        inserted = onLoaded(it.posts, direction)
                    )
                },
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

    private fun bindItems(
        offsetId: Int? = null,
        inserted: Range? = null,
        deleted: Range? = null,
    ) {
        val items = this.items

        if (items.isNullOrEmpty()) {
            view?.showContent()
            view?.showPlaceholder()
            return
        }

        val dataSource = ListDataSource(items)
        adapterPresenter.get().onDataSourceChanged(dataSource)

        view?.let { view ->
            view.showContent()

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

    private fun onLoadingError(err: Int) {
        when (err) {
            ERROR_UNAUTHORIZED -> {
                items = converter.unauthorized()
                bindItems()
            }

            else -> {
                this.error = err
                view?.showError()
            }
        }
    }

    private fun onLoadMoreError(ex: Throwable) {
        items?.last()
            ?.apply {
                hasProgress = false
                hasMore = false
            }
        bindItems()
    }

    override fun onUpdate() {
        view?.contentUpdated()
    }

    override fun scrollToBottom() {
        view?.scrollToBottom()
        items?.lastOrNull()?.let { lastItem ->
            onFeedRead(postId = lastItem.id.toInt())
        }
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
                when (action) {
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
        bindItems(
            offsetId = null,
            inserted = null,
            deleted = deleted
        )
    }

    override fun onLoginClick() {
        router?.openLoginScreen()
    }

    override fun onReactionClick(item: FeedItem, reaction: com.tomclaw.appsend.screen.feed.api.Reaction) {
        val items = items ?: return
        val itemIndex = items.indexOf(item)
        if (itemIndex < 0) return

        val currentReactions = item.getReactions()?.toMutableList() ?: return

        // Optimistic UI update
        val reactionIndex = currentReactions.indexOfFirst { it.id == reaction.id }
        val wasActive = reaction.active == true
        val currentCount = reaction.count ?: 0

        if (reactionIndex >= 0) {
            val existingReaction = currentReactions[reactionIndex]
            val updatedReaction = existingReaction.copy(
                active = !wasActive,
                count = if (!wasActive) currentCount + 1 else maxOf(0, currentCount - 1)
            )
            currentReactions[reactionIndex] = updatedReaction
        } else {
            // If reaction is not in the list, add it
            val newReaction = reaction.copy(
                active = true,
                count = 1
            )
            currentReactions.add(newReaction)
        }

        // Update item with new reactions
        val updatedItem = item.withReactions(currentReactions)

        // Update items list
        val updatedItems = items.toMutableList()
        updatedItems[itemIndex] = updatedItem
        this.items = updatedItems

        // Update UI
        bindItems()

        // Send API request
        val tag = item.id.toString()
        val postId = item.id
        subscriptions += interactor.reaction(tag, reaction.id)
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .subscribe(
                { response -> onReactionResponse(postId, response.reactions) },
                { onReactionError(postId, reaction, wasActive, currentCount) }
            )
    }

    private fun onReactionResponse(postId: Long, reactions: Map<String, com.tomclaw.appsend.screen.feed.api.Reaction>) {
        val items = items ?: return
        val itemIndex = items.indexOfFirst { it.id == postId }
        if (itemIndex < 0) return

        // Get current item from the list
        val currentItem = items[itemIndex]
        val currentReactions = currentItem.getReactions()?.toMutableList() ?: return

        // Update count and selection state for existing reactions
        reactions.forEach { (reactionId, updatedReaction) ->
            val reactionIndex = currentReactions.indexOfFirst { it.id == reactionId }
            if (reactionIndex >= 0) {
                // Update existing reaction: count and selection state
                val existingReaction = currentReactions[reactionIndex]
                currentReactions[reactionIndex] = existingReaction.copy(
                    count = updatedReaction.count,
                    active = updatedReaction.active
                )
            }
        }

        // For reactions not in API response, set count to 0 and remove selection
        currentReactions.forEachIndexed { index, reaction ->
            if (!reactions.containsKey(reaction.id)) {
                currentReactions[index] = reaction.copy(
                    count = 0,
                    active = false
                )
            }
        }

        val updatedItem = currentItem.withReactions(currentReactions)

        // Update items list
        val updatedItems = items.toMutableList()
        updatedItems[itemIndex] = updatedItem
        this.items = updatedItems

        // Update UI
        bindItems()
    }

    private fun onReactionError(
        postId: Long,
        reaction: com.tomclaw.appsend.screen.feed.api.Reaction,
        wasActive: Boolean,
        previousCount: Int
    ) {
        // Rollback optimistic update on error
        val items = items ?: return
        val itemIndex = items.indexOfFirst { it.id == postId }
        if (itemIndex < 0) return

        // Get current item from the list
        val currentItem = items[itemIndex]
        val currentReactions = currentItem.getReactions()?.toMutableList() ?: return

        val reactionIndex = currentReactions.indexOfFirst { it.id == reaction.id }
        if (reactionIndex >= 0) {
            val existingReaction = currentReactions[reactionIndex]
            val updatedReaction = existingReaction.copy(
                active = wasActive,
                count = previousCount
            )
            currentReactions[reactionIndex] = updatedReaction
        }

        val updatedItem = currentItem.withReactions(currentReactions)

        val updatedItems = items.toMutableList()
        updatedItems[itemIndex] = updatedItem
        this.items = updatedItems

        bindItems()
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
