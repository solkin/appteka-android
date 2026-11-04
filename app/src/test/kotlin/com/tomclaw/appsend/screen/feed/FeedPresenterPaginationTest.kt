package com.tomclaw.appsend.screen.feed

import com.tomclaw.appsend.core.permissions.Capability
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.screen.feed.adapter.FeedItem
import com.tomclaw.appsend.screen.feed.adapter.ProgressSide
import com.tomclaw.appsend.screen.feed.adapter.text.TextItem
import com.tomclaw.appsend.screen.feed.api.DeletePostResponse
import com.tomclaw.appsend.screen.feed.api.FeedReactionResponse
import com.tomclaw.appsend.screen.feed.api.FeedResponse
import com.tomclaw.appsend.screen.feed.api.PostEntity
import com.tomclaw.appsend.screen.feed.api.ReadResponse
import com.tomclaw.appsend.screen.feed.api.TYPE_TEXT
import com.tomclaw.appsend.screen.feed.api.TextPayload
import com.tomclaw.appsend.user.api.UserBrief
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.adapter.AdapterPresenter
import com.tomclaw.appsend.util.adapter.Item
import dagger.Lazy
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.schedulers.TestScheduler
import io.reactivex.rxjava3.subjects.PublishSubject
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.util.concurrent.TimeUnit

class FeedPresenterPaginationTest {

    private val view = FakeFeedView()
    private val interactor = FakeFeedInteractor()
    private val adapter = FakeAdapterPresenter()

    @Test
    fun `first page shows the full-screen overlay`() {
        val presenter = presenter()
        presenter.attachView(view)

        assertEquals(1, view.progressShown)
    }

    @Test
    fun `paginating down keeps the overlay away and spins under the last item`() {
        val presenter = presenter()
        presenter.attachView(view)
        interactor.respond(posts(1, 2))
        view.progressShown = 0

        presenter.onLoadMore(adapter.items.last())

        assertEquals(0, view.progressShown)
        assertEquals(ProgressSide.Bottom, adapter.items.last().progress)
        assertNull(adapter.items.first().progress)
        assertEquals(FeedDirection.After, interactor.lastDirection)
        assertEquals(2, interactor.lastOffsetId)
    }

    @Test
    fun `paginating up spins above the first item`() {
        val presenter = presenter()
        presenter.attachView(view)
        interactor.respond(posts(1, 2))

        presenter.onLoadMore(adapter.items.first())

        assertEquals(ProgressSide.Top, adapter.items.first().progress)
        assertNull(adapter.items.last().progress)
        assertEquals(FeedDirection.Before, interactor.lastDirection)
        assertEquals(1, interactor.lastOffsetId)
    }

    @Test
    fun `next page hides the spinner and re-binds the item that held it`() {
        val presenter = presenter()
        presenter.attachView(view)
        interactor.respond(posts(1, 2))
        presenter.onLoadMore(adapter.items.last())
        view.reset()

        interactor.respond(posts(3, 4))

        assertEquals(4, adapter.items.size)
        // The boundary item keeps its position, so an insert alone
        // would have left the spinner running.
        assertEquals(listOf(1), view.changedPositions)
        assertEquals(listOf(2 to 2), view.insertedRanges)
        assertNull(adapter.items[1].progress)
        assertTrue(adapter.items.last().hasMore)
    }

    @Test
    fun `previous page re-binds the item that held it at its new position`() {
        val presenter = presenter()
        presenter.attachView(view)
        interactor.respond(posts(3, 4))
        presenter.onLoadMore(adapter.items.first())
        view.reset()

        interactor.respond(posts(1, 2))

        assertEquals(listOf(1L, 2L, 3L, 4L), adapter.items.map { it.id })
        // Insert and re-bind go together, otherwise the list would jump
        // between them and hide what has just been loaded.
        assertEquals(listOf(2 to 2), view.prependedRanges)
        assertEquals(emptyList<Int>(), view.changedPositions)
        assertEquals(emptyList<Pair<Int, Int>>(), view.insertedRanges)
        assertNull(adapter.items[2].progress)
        assertTrue(adapter.items.first().hasMore)
    }

    @Test
    fun `failed page up stops the top end only`() {
        val presenter = presenter()
        presenter.attachView(view)
        interactor.respond(posts(1, 2))
        presenter.onLoadMore(adapter.items.first())
        view.reset()

        interactor.fail(httpError(code = 403))

        assertNull(adapter.items.first().progress)
        assertFalse(adapter.items.first().hasMore)
        assertTrue(adapter.items.last().hasMore)
        assertEquals(listOf(0), view.changedPositions)
        assertFalse(view.errorShown)
    }

    @Test
    fun `failed first page shows the error instead of a stuck overlay`() {
        val presenter = presenter(postId = 5)
        presenter.attachView(view)

        interactor.fail(httpError(code = 403))

        assertTrue(view.errorShown)
        assertFalse(view.placeholderShown)
    }

    @Test
    fun `restored state re-arms a request that was in flight`() {
        val items = listOf(
            item(id = 1, progress = ProgressSide.Top),
            item(id = 2),
            item(id = 3, progress = ProgressSide.Bottom),
        )

        items.rearmPagination()

        assertTrue(items.all { it.progress == null })
        assertEquals(listOf(true, false, true), items.map { it.hasMore })
    }

    @Test
    fun `scroll idle on an emptied list reads nothing`() {
        val errors = mutableListOf<Throwable>()
        val clock = TestScheduler()
        RxJavaPlugins.setErrorHandler { errors += it }
        RxJavaPlugins.setComputationSchedulerHandler { clock }
        try {
            val presenter = presenter()
            presenter.attachView(view)
            interactor.respond(posts())

            view.scrollIdle.onNext(0)
            clock.advanceTimeBy(1, TimeUnit.SECONDS)

            assertEquals(emptyList<Throwable>(), errors)
        } finally {
            RxJavaPlugins.reset()
        }
    }

    private fun presenter(postId: Int? = null) = FeedPresenterImpl(
        userId = null,
        postId = postId,
        withToolbar = false,
        interactor = interactor,
        adapterPresenter = Lazy { adapter },
        converter = FeedConverterImpl(),
        resourceProvider = FakeResourceProvider(),
        schedulers = TestSchedulers(),
        state = null,
    )

    private fun posts(vararg ids: Int) = ids.map { id ->
        PostEntity(
            postId = id,
            time = 0,
            type = TYPE_TEXT,
            payload = TextPayload(screenshots = emptyList(), text = "post $id"),
            reacts = null,
            user = user,
            actions = null,
        )
    }

    private fun item(id: Long, progress: ProgressSide? = null) = TextItem(
        id = id,
        time = 0,
        screenshots = emptyList(),
        text = "post $id",
        user = user,
        actions = null,
        reacts = null,
        progress = progress,
    )

    private val user = UserBrief(
        id = 1,
        icon = UserIcon(icon = "", label = emptyMap(), color = "#000"),
        joinTime = 0,
        lastSeen = 0,
        role = 0,
        name = "author",
        isRegistered = true,
        isVerified = false,
        url = null,
    )

    private fun httpError(code: Int) =
        HttpException(Response.error<Any>(code, "".toResponseBody(null)))

}

private class FakeFeedInteractor : FeedInteractor {

    var lastOffsetId: Int? = null
        private set
    var lastDirection: FeedDirection? = null
        private set

    private var pending: PublishSubject<FeedResponse>? = null

    override fun listFeed(
        userId: Int?,
        postId: Int?,
        direction: FeedDirection?,
    ): Observable<FeedResponse> {
        lastOffsetId = postId
        lastDirection = direction
        return PublishSubject.create<FeedResponse>().also { pending = it }
    }

    fun respond(posts: List<PostEntity>, offsetId: Int = 0) {
        val subject = requireNotNull(pending) { "no request in flight" }
        pending = null
        subject.onNext(
            FeedResponse(
                posts = posts,
                offsetId = offsetId,
                direction = lastDirection ?: FeedDirection.Both,
            )
        )
        subject.onComplete()
    }

    fun fail(ex: Throwable) {
        val subject = requireNotNull(pending) { "no request in flight" }
        pending = null
        subject.onError(ex)
    }

    override fun readFeed(postId: Int): Observable<ReadResponse> = Observable.never()

    override fun deletePost(postId: Int): Observable<DeletePostResponse> = Observable.never()

    override fun reaction(tag: String, reactId: String): Observable<FeedReactionResponse> =
        Observable.never()

}

private class FakeAdapterPresenter : AdapterPresenter {

    var items: List<FeedItem> = emptyList()
        private set

    override fun onDataSourceChanged(items: List<Item>) {
        this.items = items.filterIsInstance<FeedItem>()
    }

    override fun getItemCount(): Int = items.size

    override fun getItem(position: Int): Item = items[position]

    override fun getItemId(position: Int): Long = items[position].id

    override fun getItemViewType(position: Int): Int = 0

}

private class FakeFeedView : FeedView {

    var progressShown: Int = 0
    var errorShown: Boolean = false
        private set
    var placeholderShown: Boolean = false
        private set
    val changedPositions = mutableListOf<Int>()
    val insertedRanges = mutableListOf<Pair<Int, Int>>()
    val prependedRanges = mutableListOf<Pair<Int, Int>>()

    fun reset() {
        progressShown = 0
        changedPositions.clear()
        insertedRanges.clear()
        prependedRanges.clear()
    }

    override fun showProgress() {
        progressShown++
    }

    override fun showError() {
        errorShown = true
    }

    override fun showPlaceholder() {
        placeholderShown = true
    }

    override fun contentUpdated(position: Int) {
        changedPositions += position
    }

    override fun rangeInserted(position: Int, count: Int) {
        insertedRanges += position to count
    }

    override fun rangePrepended(count: Int, position: Int) {
        prependedRanges += count to position
    }

    override fun showContent() = Unit

    override fun showToolbar() = Unit

    override fun hideToolbar() = Unit

    override fun contentUpdated() = Unit

    override fun rangeDeleted(position: Int, count: Int) = Unit

    override fun scrollTo(position: Int) = Unit

    override fun scrollToBottom() = Unit

    override fun showPostDeletionFailed() = Unit

    override fun showUnauthorizedError() = Unit

    override fun showCapabilityDenied(capability: Capability) = Unit

    override fun showPostMenu(actions: List<MenuAction>) = Unit

    override fun navigationClicks(): Observable<Unit> = Observable.never()

    override fun retryClicks(): Observable<Unit> = Observable.never()

    val scrollIdle: PublishSubject<Int> = PublishSubject.create()

    override fun scrollIdle(): Observable<Int> = scrollIdle

}

private class FakeResourceProvider : FeedResourceProvider {

    override fun formatTime(value: Long): String = value.toString()

    override fun prepareMenuActions(
        actions: List<String>,
        handler: (Int) -> Unit,
    ): List<MenuAction> = emptyList()

}

private class TestSchedulers : SchedulersFactory {

    override fun io(): Scheduler = Schedulers.trampoline()

    override fun mainThread(): Scheduler = Schedulers.trampoline()

}
