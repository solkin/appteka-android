package com.tomclaw.appsend.screen.search

import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.screen.store.AppConverter
import com.tomclaw.appsend.screen.store.adapter.app.AppItem
import com.tomclaw.appsend.util.Analytics
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.adapter.AdapterPresenter
import com.tomclaw.appsend.util.adapter.Item
import dagger.Lazy
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.TestScheduler
import io.reactivex.rxjava3.subjects.PublishSubject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * When a search is worth remembering, and how a remembered one reads and
 * behaves on the placeholder. Time is virtual here: the screen searches
 * as it is typed into, so both the delay before a search runs and the
 * longer one before it is remembered have to be stepped through.
 */
class SearchPresenterHistoryTest {

    private val scheduler = TestScheduler()
    private val view = FakeSearchView()
    private val interactor = FakeSearchInteractor()
    private val adapter = FakeAdapterPresenter()
    private val router = FakeSearchRouter()

    @Test
    fun `a search left standing is remembered`() {
        attach()

        search("telegram")
        scheduler.advanceTimeBy(REMEMBER_DELAY_MS, TimeUnit.MILLISECONDS)

        assertEquals(listOf(entry("telegram")), interactor.added)
    }

    @Test
    fun `only the query the typing settled on is remembered`() {
        attach()

        search("tele")
        scheduler.advanceTimeBy(3, TimeUnit.SECONDS)
        search("telegram")
        scheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        assertEquals(emptyList<SearchHistoryEntry>(), interactor.added)

        scheduler.advanceTimeBy(3, TimeUnit.SECONDS)

        assertEquals(listOf(entry("telegram")), interactor.added)
    }

    @Test
    fun `a search that found nothing is not remembered`() {
        interactor.apps = emptyList()
        attach()

        search("zzzz")
        scheduler.advanceTimeBy(REMEMBER_DELAY_MS, TimeUnit.MILLISECONDS)

        assertEquals(emptyList<SearchHistoryEntry>(), interactor.added)
    }

    @Test
    fun `a search that failed is not remembered`() {
        interactor.failure = RuntimeException("network down")
        attach()

        search("telegram")
        scheduler.advanceTimeBy(REMEMBER_DELAY_MS, TimeUnit.MILLISECONDS)

        assertTrue(view.errorShown)
        assertEquals(emptyList<SearchHistoryEntry>(), interactor.added)
    }

    @Test
    fun `the search key remembers without waiting`() {
        attach()
        search("telegram")

        view.searchActions.onNext(Unit)

        assertEquals(listOf(entry("telegram")), interactor.added)
    }

    @Test
    fun `opening an app out of the results remembers the search`() {
        val presenter = attach()
        search("telegram")

        presenter.onItemClick(adapter.items.first())

        assertEquals(listOf(entry("telegram")), interactor.added)
    }

    @Test
    fun `a row shows the query with the tags it was filtered by`() {
        interactor.history = listOf(entry("telegram", "chat", "social"))
        attach()

        val item = view.history.first()

        assertEquals("telegram", item.title)
        assertEquals("chat, social", item.subtitle)
    }

    @Test
    fun `a search made of tags alone reads as its tags`() {
        interactor.history = listOf(entry("", "chat", "social"))
        attach()

        val item = view.history.first()

        assertEquals("chat, social", item.title)
        assertNull(item.subtitle)
    }

    @Test
    fun `a row restores the query and the tags it was made of`() {
        interactor.history = listOf(entry("telegram", "chat"))
        attach()

        view.historyClicks.onNext(view.history.first())
        scheduler.triggerActions()

        assertEquals("telegram", view.queryText)
        assertEquals("telegram", interactor.lastQuery)
        assertEquals(listOf("chat"), interactor.lastTags)
    }

    @Test
    fun `the history opens a few rows deep and the rest on request`() {
        interactor.history = (1..6).map { entry("query $it") }
        attach()

        assertEquals(4, view.history.size)
        assertTrue(view.historyHasMore)

        view.moreHistoryClicks.onNext(Unit)

        assertEquals(6, view.history.size)
        assertFalse(view.historyHasMore)
    }

    @Test
    fun `removing a row takes it out of the section`() {
        interactor.history = listOf(entry("telegram"), entry("browser"))
        attach()

        view.historyRemoveClicks.onNext(view.history.first())
        scheduler.triggerActions()

        assertEquals(listOf(entry("telegram")), interactor.removed)
        assertEquals(listOf("browser"), view.history.map { it.title })
    }

    @Test
    fun `clearing empties the section`() {
        interactor.history = listOf(entry("telegram"))
        attach()

        view.historyClearClicks.onNext(Unit)
        scheduler.triggerActions()

        assertEquals(1, interactor.cleared)
        assertEquals(emptyList<SearchHistoryItem>(), view.history)
    }

    @Test
    fun `going back out of the results gives up the criteria first`() {
        attach()
        search("telegram")
        view.reset()

        view.navigationClicks.onNext(Unit)
        scheduler.triggerActions()

        assertEquals("", view.queryText)
        assertTrue(view.placeholderShown)
        assertEquals(0, router.left)
    }

    @Test
    fun `going back gives up the tags along with the query`() {
        interactor.history = listOf(entry("telegram", "chat"))
        attach()
        view.historyClicks.onNext(view.history.first())
        scheduler.triggerActions()
        view.reset()

        view.navigationClicks.onNext(Unit)
        scheduler.triggerActions()

        assertTrue(view.placeholderShown)
        assertEquals(0, router.left)
    }

    @Test
    fun `going back with nothing searched for leaves the screen`() {
        attach()

        view.navigationClicks.onNext(Unit)

        assertEquals(1, router.left)
    }

    @Test
    fun `the system back is told as the criteria come and go`() {
        val presenter = attach()

        assertEquals(listOf(false), router.backCallbackStates)

        search("telegram")

        assertEquals(listOf(false, true), router.backCallbackStates)

        presenter.onBackPressed()
        scheduler.triggerActions()

        assertEquals(listOf(false, true, false), router.backCallbackStates)
        assertEquals(0, router.left)
    }

    // --- helpers ----------------------------------------------------

    private fun attach(): SearchPresenter = SearchPresenterImpl(
        searchInteractor = interactor,
        adapterPresenter = Lazy { adapter },
        appConverter = FakeAppConverter(),
        analytics = FakeAnalytics(),
        schedulers = TestSchedulers(scheduler),
        initialTags = emptyList(),
        state = null,
    ).apply {
        attachView(view)
        attachRouter(router)
        scheduler.triggerActions()
    }

    /** Typing, then the wait the screen searches after. */
    private fun search(text: String) {
        view.queryTextChanges.onNext(text)
        scheduler.advanceTimeBy(SEARCH_DELAY_MS, TimeUnit.MILLISECONDS)
    }

    private fun entry(query: String, vararg tags: String) =
        SearchHistoryEntry(query, tags.toList())

}

// Mirrors DEBOUNCE_DELAY_MS and HISTORY_DELAY_MS in SearchPresenter.
private const val SEARCH_DELAY_MS = 500L
private const val REMEMBER_DELAY_MS = 4000L

private class FakeSearchInteractor : SearchInteractor {

    var apps: List<AppEntity> = listOf(app("1"))
    var failure: Throwable? = null
    var history: List<SearchHistoryEntry> = emptyList()

    var lastQuery: String? = null
        private set
    var lastTags: List<String>? = null
        private set

    val added = mutableListOf<SearchHistoryEntry>()
    val removed = mutableListOf<SearchHistoryEntry>()
    var cleared: Int = 0
        private set

    override fun searchApps(
        query: String,
        tags: List<String>,
        offset: Int,
    ): Observable<List<AppEntity>> {
        lastQuery = query
        lastTags = tags
        return failure?.let { Observable.error(it) } ?: Observable.just(apps)
    }

    override fun loadPopularTags(): Observable<List<String>> = Observable.just(emptyList())

    override fun loadHistory(): Observable<List<SearchHistoryEntry>> =
        Observable.fromCallable { history }

    override fun addToHistory(
        query: String,
        tags: List<String>,
    ): Observable<List<SearchHistoryEntry>> = Observable.fromCallable {
        val entry = SearchHistoryEntry(query.trim(), tags)
        added += entry
        history = listOf(entry) + history.filterNot { it == entry }
        history
    }

    override fun removeFromHistory(
        entry: SearchHistoryEntry,
    ): Observable<List<SearchHistoryEntry>> = Observable.fromCallable {
        removed += entry
        history = history.filterNot { it == entry }
        history
    }

    override fun clearHistory(): Observable<List<SearchHistoryEntry>> = Observable.fromCallable {
        cleared++
        history = emptyList()
        history
    }

}

private fun app(id: String) = AppEntity(
    appId = id,
    packageName = "com.tomclaw.$id",
    icon = null,
    title = "app $id",
    verName = "1.0",
    verCode = 1,
    time = 0,
    size = 0,
    rating = 0f,
    downloads = 0,
    status = 0,
    category = null,
    exclusive = false,
    sourceUrl = null,
    abi = null,
)

private class FakeAppConverter : AppConverter {

    override fun convert(appEntity: AppEntity) = AppItem(
        id = appEntity.appId.toLong(),
        appId = appEntity.appId,
        icon = null,
        title = appEntity.title,
        version = appEntity.verName,
        size = "",
        rating = appEntity.rating,
        downloads = appEntity.downloads,
        status = appEntity.status,
        category = null,
        exclusive = false,
        openSource = false,
        isAbiCompatible = true,
    )

}

private class FakeAdapterPresenter : AdapterPresenter {

    var items: List<AppItem> = emptyList()
        private set

    override fun onDataSourceChanged(items: List<Item>) {
        this.items = items.filterIsInstance<AppItem>()
    }

    override fun getItemCount(): Int = items.size

    override fun getItem(position: Int): Item = items[position]

    override fun getItemId(position: Int): Long = items[position].id

    override fun getItemViewType(position: Int): Int = 0

}

private class FakeSearchView : SearchView {

    var history: List<SearchHistoryItem> = emptyList()
        private set
    var historyHasMore: Boolean = false
        private set
    var queryText: String? = null
        private set
    var errorShown: Boolean = false
        private set
    var placeholderShown: Boolean = false
        private set

    fun reset() {
        placeholderShown = false
    }

    val queryTextChanges = PublishSubject.create<String>()
    val searchActions = PublishSubject.create<Unit>()
    val navigationClicks = PublishSubject.create<Unit>()
    val historyClicks = PublishSubject.create<SearchHistoryItem>()
    val historyRemoveClicks = PublishSubject.create<SearchHistoryItem>()
    val historyClearClicks = PublishSubject.create<Unit>()
    val moreHistoryClicks = PublishSubject.create<Unit>()

    override fun showHistory(items: List<SearchHistoryItem>, hasMore: Boolean) {
        history = items
        historyHasMore = hasMore
    }

    override fun setQueryText(query: String) {
        queryText = query
    }

    override fun showError() {
        errorShown = true
    }

    override fun isPullRefreshing(): Boolean = false

    override fun showProgress() = Unit

    override fun showContent() = Unit

    override fun contentUpdated() = Unit

    override fun scrollToTop() = Unit

    override fun showPlaceholder() {
        placeholderShown = true
    }

    override fun showEmptyResult() = Unit

    override fun stopPullRefreshing() = Unit

    override fun requestQueryFocus() = Unit

    override fun hideKeyboard(): Boolean = false

    override fun showTags(selected: List<String>, suggestions: List<String>, custom: String?) = Unit

    override fun showPopularTags(tags: List<String>, hasMore: Boolean) = Unit

    override fun navigationClicks(): Observable<Unit> = navigationClicks

    override fun retryClicks(): Observable<Unit> = Observable.never()

    override fun refreshClicks(): Observable<Unit> = Observable.never()

    override fun queryTextChanges(): Observable<String> = queryTextChanges

    override fun searchActions(): Observable<Unit> = searchActions

    override fun tagRemoveClicks(): Observable<String> = Observable.never()

    override fun tagSuggestionClicks(): Observable<String> = Observable.never()

    override fun customTagClicks(): Observable<String> = Observable.never()

    override fun popularTagClicks(): Observable<String> = Observable.never()

    override fun moreTagsClicks(): Observable<Unit> = Observable.never()

    override fun historyClicks(): Observable<SearchHistoryItem> = historyClicks

    override fun historyRemoveClicks(): Observable<SearchHistoryItem> = historyRemoveClicks

    override fun historyClearClicks(): Observable<Unit> = historyClearClicks

    override fun moreHistoryClicks(): Observable<Unit> = moreHistoryClicks

}

private class FakeSearchRouter : SearchPresenter.SearchRouter {

    var opened: String? = null
        private set
    var left: Int = 0
        private set
    val backCallbackStates = mutableListOf<Boolean>()

    override fun openAppScreen(appId: String, title: String) {
        opened = appId
    }

    override fun setBackCallbackEnabled(enabled: Boolean) {
        backCallbackStates += enabled
    }

    override fun leaveScreen() {
        left++
    }

}

private class FakeAnalytics : Analytics {

    override fun register() = Unit

    override fun trackEvent(name: String) = Unit

    override fun trackException(throwable: Throwable, context: Map<String, String>) = Unit

}

private class TestSchedulers(private val scheduler: Scheduler) : SchedulersFactory {

    override fun io(): Scheduler = scheduler

    override fun mainThread(): Scheduler = scheduler

}
