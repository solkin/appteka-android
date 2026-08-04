package com.tomclaw.appsend.screen.search

import android.os.Bundle
import com.tomclaw.appsend.util.adapter.AdapterPresenter
import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.dto.AppsPage
import com.tomclaw.appsend.screen.store.AppConverter
import com.tomclaw.appsend.screen.store.adapter.app.AppItem
import com.tomclaw.appsend.screen.store.adapter.ItemListener
import com.tomclaw.appsend.util.Analytics
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.getParcelableArrayListCompat
import com.tomclaw.appsend.util.retryWhenNonAuthErrors
import dagger.Lazy
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.plusAssign
import java.util.concurrent.TimeUnit

interface SearchPresenter : ItemListener {

    fun attachView(view: SearchView)

    fun detachView()

    fun attachRouter(router: SearchRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun invalidateSearch()

    /** Back, however it was asked for: the arrow or the system. */
    fun onBackPressed()

    interface SearchRouter {

        fun openAppScreen(appId: String, title: String)

        fun openContentFilterScreen()

        /**
         * Whether back has criteria to give up before it gives up the
         * screen. The system asks before the gesture starts, so it has
         * to be told as the criteria change rather than when back
         * arrives — otherwise it previews an exit that isn't going to
         * happen.
         */
        fun setBackCallbackEnabled(enabled: Boolean)

        fun leaveScreen()

    }

}

/**
 * Search over the catalog by free text, by tags, or by both at once.
 *
 * There is deliberately no notion of a "tag screen" here: arriving from
 * a tag on an app page just seeds [tags] with it. Text and tags are two
 * fields of the same query, so every path through this class is the
 * same path — the criteria change, the search runs.
 */
class SearchPresenterImpl(
    private val searchInteractor: SearchInteractor,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val appConverter: AppConverter,
    private val analytics: Analytics,
    private val schedulers: SchedulersFactory,
    initialTags: List<String>,
    state: Bundle?
) : SearchPresenter {

    private var view: SearchView? = null
    private var router: SearchPresenter.SearchRouter? = null

    private val subscriptions = CompositeDisposable()
    private var searchDisposable: Disposable? = null

    private var items: List<AppItem>? =
        state?.getParcelableArrayListCompat(KEY_APPS, AppItem::class.java)
    private var isError: Boolean = state?.getBoolean(KEY_ERROR) == true

    /** What the server withheld from these results, as it reported it. */
    private var contentFilter: List<String> =
        state?.getStringArrayList(KEY_CONTENT_FILTER).orEmpty()

    private var query: String = state?.getString(KEY_QUERY).orEmpty()
    private var tags: List<String> =
        state?.getStringArrayList(KEY_TAGS) ?: initialTags

    private var popularTags: List<String>? =
        state?.getStringArrayList(KEY_POPULAR_TAGS)
    private var isLoadingPopularTags: Boolean = false

    /** How much of the vocabulary the placeholder has been asked for. */
    private var popularTagsShown: Int =
        state?.getInt(KEY_POPULAR_TAGS_SHOWN, POPULAR_TAGS_PAGE) ?: POPULAR_TAGS_PAGE

    private var history: List<SearchHistoryEntry> = emptyList()

    /** Likewise, how much of the history has been asked for. */
    private var historyShown: Int =
        state?.getInt(KEY_HISTORY_SHOWN, HISTORY_PAGE) ?: HISTORY_PAGE

    /** Restarted by every search. See [rememberSearch]. */
    private var rememberDisposable: Disposable? = null

    private val hasCriteria: Boolean
        get() = query.isNotBlank() || tags.isNotEmpty()

    override fun attachView(view: SearchView) {
        this.view = view

        view.showContentFilter(contentFilter)

        subscriptions += view.contentFilterClicks().subscribe {
            router?.openContentFilterScreen()
        }

        subscriptions += view.navigationClicks().subscribe {
            onBackPressed()
        }
        subscriptions += view.retryClicks().subscribe {
            performSearch()
        }
        subscriptions += view.refreshClicks().subscribe {
            invalidateSearch()
            analytics.trackEvent("search-refresh")
        }
        subscriptions += view.queryTextChanges()
            .debounce(DEBOUNCE_DELAY_MS, TimeUnit.MILLISECONDS, schedulers.mainThread())
            .distinctUntilChanged()
            .subscribe { text ->
                // Restoring the field after a rotation fires the text
                // watcher just like typing does, and an empty relay lets
                // that first value through distinctUntilChanged. Acting
                // on it would re-run the search we just restored, so
                // compare against what we already hold: a change that
                // changes nothing is not a change.
                if (text == query) return@subscribe
                query = text
                onCriteriaChanged()
            }
        subscriptions += view.tagRemoveClicks().subscribe { tag ->
            tags = tags.filterNot { it == tag }
            onCriteriaChanged()
        }
        subscriptions += view.tagSuggestionClicks().subscribe { tag ->
            analytics.trackEvent("search-tag-suggestion")
            addTag(tag)
        }
        subscriptions += view.customTagClicks().subscribe { tag ->
            analytics.trackEvent("search-tag-custom")
            addTag(tag)
        }
        subscriptions += view.popularTagClicks().subscribe { tag ->
            analytics.trackEvent("search-popular-tag")
            addTag(tag)
        }
        subscriptions += view.moreTagsClicks().subscribe {
            popularTagsShown += POPULAR_TAGS_PAGE
            analytics.trackEvent("search-more-tags")
            bindTags()
        }
        subscriptions += view.searchActions().subscribe {
            analytics.trackEvent("search-ime-action")
            rememberSearch()
        }
        subscriptions += view.historyClicks().subscribe { item ->
            analytics.trackEvent("search-history-item")
            applyHistoryItem(item)
        }
        subscriptions += view.historyRemoveClicks().subscribe { item ->
            analytics.trackEvent("search-history-remove")
            bindHistoryFrom(searchInteractor.removeFromHistory(item.toEntry()))
        }
        subscriptions += view.historyClearClicks().subscribe {
            analytics.trackEvent("search-history-clear")
            bindHistoryFrom(searchInteractor.clearHistory())
        }
        subscriptions += view.moreHistoryClicks().subscribe {
            historyShown += HISTORY_PAGE_MORE
            analytics.trackEvent("search-more-history")
            bindHistory()
        }

        if (query.isNotEmpty()) {
            view.setQueryText(query)
        }
        bindTags()
        loadPopularTags()
        bindHistoryFrom(searchInteractor.loadHistory())

        when {
            isError -> onError()
            items != null -> bindItems()
            hasCriteria -> performSearch()
            else -> showPlaceholder()
        }
    }

    override fun detachView() {
        searchDisposable?.dispose()
        searchDisposable = null
        rememberDisposable?.dispose()
        rememberDisposable = null
        subscriptions.clear()
        this.view = null
    }

    override fun attachRouter(router: SearchPresenter.SearchRouter) {
        this.router = router
        router.setBackCallbackEnabled(hasCriteria)
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState() = Bundle().apply {
        putParcelableArrayList(KEY_APPS, items?.let { ArrayList(it) })
        putBoolean(KEY_ERROR, isError)
        putString(KEY_QUERY, query)
        putStringArrayList(KEY_TAGS, ArrayList(tags))
        popularTags?.let { putStringArrayList(KEY_POPULAR_TAGS, ArrayList(it)) }
        putInt(KEY_POPULAR_TAGS_SHOWN, popularTagsShown)
        // The history itself is on disk, only how much of it was asked
        // for is worth carrying across a rotation.
        putInt(KEY_HISTORY_SHOWN, historyShown)
        putStringArrayList(KEY_CONTENT_FILTER, ArrayList(contentFilter))
    }

    override fun invalidateSearch() {
        items = null
        isError = false
        if (hasCriteria) performSearch() else showPlaceholder()
    }

    /**
     * A tag arrives the same way from wherever it was offered. The text
     * it was picked out of has done its job by then, so it gives way to
     * the tag: keeping both would narrow the search to apps that carry
     * the tag *and* mention the word, which is not what picking a
     * suggestion means.
     */
    private fun addTag(tag: String) {
        if (tags.any { it.equals(tag, ignoreCase = true) }) return
        tags = tags + tag
        if (query.isNotEmpty()) {
            query = ""
            view?.setQueryText("")
        }
        onCriteriaChanged()
    }

    /**
     * Results are somewhere the visitor got to, not somewhere they
     * arrived from, so backing out of them is a step of its own: the
     * criteria go first and the screen only after that. Otherwise the
     * way back from a result is out of search altogether.
     */
    override fun onBackPressed() {
        if (!hasCriteria) {
            router?.leaveScreen()
            return
        }
        analytics.trackEvent("search-reset")
        query = ""
        tags = emptyList()
        view?.setQueryText("")
        onCriteriaChanged()
    }

    /** The one funnel: whatever changed the criteria, this decides what happens next. */
    private fun onCriteriaChanged() {
        bindTags()
        router?.setBackCallbackEnabled(hasCriteria)
        if (hasCriteria) performSearch() else showPlaceholder()
    }

    /**
     * The filter row and the placeholder's tag cloud say the same two
     * things — what is applied, and what the catalog offers — so they
     * are decided in one place.
     */
    private fun bindTags() {
        val view = this.view ?: return
        val available = popularTags.orEmpty().filterNot { isSelected(it) }
        val text = normalizeTag(query)
        val suggestions = when {
            // On the placeholder the cloud below already offers these.
            !hasCriteria -> emptyList()
            text.isEmpty() -> available
            else -> available
                .filter { it.contains(text, ignoreCase = true) }
                // A tag the text starts is a likelier match than one it
                // merely occurs in; popularity orders the rest.
                .sortedBy { if (it.startsWith(text, ignoreCase = true)) 0 else 1 }
        }.take(SUGGESTIONS_COUNT)
        // Suggestions are only the head of the vocabulary, so text that
        // matches nothing known is still worth offering as a tag.
        val custom = text.takeIf { it.isNotEmpty() && !isKnownTag(it) }
        view.showTags(selected = tags, suggestions = suggestions, custom = custom)
        // The cloud is browsed, not searched: a screenful at a time, and
        // the rest of the vocabulary only if it is asked for.
        val popular = available.take(popularTagsShown)
        view.showPopularTags(popular, hasMore = available.size > popular.size)
    }

    private fun isSelected(tag: String): Boolean =
        tags.any { it.equals(tag, ignoreCase = true) }

    /** Applied or on offer — either way, not a tag to invent. */
    private fun isKnownTag(tag: String): Boolean =
        isSelected(tag) || popularTags.orEmpty().any { it.equals(tag, ignoreCase = true) }

    /**
     * Tags reach the API comma-separated and in lower case, so that is
     * the shape a hand-typed one is offered in — commas and stray
     * spacing would otherwise arrive as a tag nothing can carry.
     */
    private fun normalizeTag(raw: String): String =
        raw.replace(TAG_SEPARATORS, " ").trim().lowercase()

    private fun performSearch() {
        if (!hasCriteria) {
            showPlaceholder()
            return
        }

        scheduleRemember()
        items = null

        // Criteria now change one tag at a time, so a search can well
        // begin while the previous one is still in the air. Only the
        // last one asked for is the one anybody is waiting for.
        searchDisposable?.dispose()
        searchDisposable = searchInteractor.searchApps(query.trim(), tags)
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { if (view?.isPullRefreshing() == false) view?.showProgress() }
            .subscribe(
                { onLoaded(it, isNewSearch = true) },
                { onError() }
            )
    }

    private fun loadMore(offset: Int) {
        if (!hasCriteria) return

        // Same disposable as the search itself: a page belongs to the
        // criteria it was asked for, and outlives neither them nor it.
        searchDisposable?.dispose()
        searchDisposable = searchInteractor.searchApps(query.trim(), tags, offset)
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .subscribe(
                { onLoaded(it, isNewSearch = false) },
                { onError() }
            )
    }

    private fun onLoaded(page: AppsPage, isNewSearch: Boolean) {
        isError = false
        contentFilter = page.contentFilter
        view?.showContentFilter(page.contentFilter)
        val newItems = page.entries
            .map { appConverter.convert(it) }
            .toList()
            .apply { if (isNotEmpty()) last().hasMore = true }

        this.items = if (isNewSearch) {
            newItems
        } else {
            this.items
                ?.apply { if (isNotEmpty()) last().hasProgress = false }
                ?.plus(newItems) ?: newItems
        }
        bindItems(scrollToTop = isNewSearch)
    }

    /**
     * [scrollToTop] only for a result set that replaces the previous
     * one. Paging appends to what is on screen, and a restored state
     * keeps the position the list was left at.
     */
    private fun bindItems(scrollToTop: Boolean = false) {
        val items = this.items
        if (items.isNullOrEmpty()) {
            // Nothing matched the criteria — that is a result, not an
            // invitation to browse tags, so no suggestions here.
            view?.showEmptyResult()
            return
        }
        adapterPresenter.get().onDataSourceChanged(items)
        view?.let {
            it.contentUpdated()
            if (scrollToTop) {
                it.scrollToTop()
            }
            if (it.isPullRefreshing()) {
                it.stopPullRefreshing()
            } else {
                it.showContent()
            }
        }
    }

    /** Nothing asked for yet — offer the catalog's own tags as a way in. */
    private fun showPlaceholder() {
        items = null
        view?.showPlaceholder()
        loadPopularTags()
    }

    /**
     * The catalog's vocabulary, fetched once. It feeds the suggestions
     * next to the query as much as the placeholder, so it is not tied to
     * either of them being on screen.
     */
    private fun loadPopularTags() {
        if (popularTags != null || isLoadingPopularTags) return
        isLoadingPopularTags = true
        subscriptions += searchInteractor.loadPopularTags()
            .observeOn(schedulers.mainThread())
            .doFinally { isLoadingPopularTags = false }
            .subscribe(
                { loadedTags ->
                    popularTags = loadedTags
                    bindTags()
                },
                {
                    // Suggestions are a convenience; failing to load them
                    // leaves a plain placeholder rather than an error,
                    // and hand-typed tags still work without them.
                }
            )
    }

    /**
     * Every keystroke runs a search of its own, so remembering them all
     * would fill the history with the way to a query rather than with
     * the query. Only a search left standing this long is taken to have
     * been meant; the other two ways of meaning one are pressing the
     * search key and acting on what it found.
     */
    private fun scheduleRemember() {
        rememberDisposable?.dispose()
        rememberDisposable = Observable
            .timer(HISTORY_DELAY_MS, TimeUnit.MILLISECONDS, schedulers.mainThread())
            .subscribe { rememberSearch() }
    }

    /** Worth offering again only if it came to something. */
    private fun rememberSearch() {
        if (isError || items.isNullOrEmpty()) return
        bindHistoryFrom(searchInteractor.addToHistory(query, tags))
    }

    /** A remembered search is restored whole — the text and the tags it was made of. */
    private fun applyHistoryItem(item: SearchHistoryItem) {
        query = item.query
        tags = item.tags
        view?.setQueryText(item.query)
        onCriteriaChanged()
    }

    private fun bindHistory() {
        val shown = history.take(historyShown)
        view?.showHistory(shown.map { it.toItem() }, hasMore = history.size > shown.size)
    }

    /**
     * A search made of tags alone is those tags, so on its row they
     * lead rather than trail a line with nothing above it.
     */
    private fun SearchHistoryEntry.toItem(): SearchHistoryItem {
        val joined = tags.joinToString(separator = ", ")
        return SearchHistoryItem(
            query = query,
            tags = tags,
            title = query.ifEmpty { joined },
            subtitle = joined.takeIf { query.isNotEmpty() && tags.isNotEmpty() },
        )
    }

    private fun SearchHistoryItem.toEntry() = SearchHistoryEntry(query, tags)

    /**
     * Reading and writing the history both answer with what it now
     * holds, so both end up on screen the same way. Like the tag
     * suggestions it is a convenience: a file that cannot be read or
     * written leaves the screen working without it.
     */
    private fun bindHistoryFrom(source: Observable<List<SearchHistoryEntry>>) {
        subscriptions += source
            .observeOn(schedulers.mainThread())
            .subscribe(
                { entries ->
                    history = entries
                    bindHistory()
                },
                { }
            )
    }

    private fun onError() {
        this.isError = true
        view?.showError()
    }

    override fun onItemClick(item: Item) {
        val app = items?.find { it.id == item.id } ?: return
        // Opening something out of the results is what a search was for.
        rememberSearch()
        router?.openAppScreen(app.appId, app.title)
    }

    override fun onLoadMore(item: Item) {
        val offset = items?.size ?: return
        loadMore(offset)
    }

}

private const val KEY_APPS = "apps"
private const val KEY_CONTENT_FILTER = "content_filter"
private const val KEY_ERROR = "error"
private const val KEY_QUERY = "query"
private const val KEY_TAGS = "tags"
private const val KEY_POPULAR_TAGS = "popular_tags"
private const val KEY_POPULAR_TAGS_SHOWN = "popular_tags_shown"
private const val KEY_HISTORY_SHOWN = "history_shown"
private const val DEBOUNCE_DELAY_MS = 500L

// Long enough that a query typed through is remembered as the query and
// not as the letters on the way to it.
private const val HISTORY_DELAY_MS = 4000L

// A few rows sit above the popular tags without pushing them off the
// placeholder, and the rest of the history is there for the asking.
private const val HISTORY_PAGE = 4
private const val HISTORY_PAGE_MORE = 8

// About a screenful of chips, so that asking for more is answered with
// more to look at rather than with a line or two appearing.
private const val POPULAR_TAGS_PAGE = 30

// The row next to the query scrolls, but it is still a row: past a
// handful of suggestions nobody reads them, they just get in the way of
// the tags already applied.
private const val SUGGESTIONS_COUNT = 8

private val TAG_SEPARATORS = Regex("[,\\s]+")
