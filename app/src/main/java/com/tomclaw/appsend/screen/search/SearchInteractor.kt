package com.tomclaw.appsend.screen.search

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.dto.AppsPage
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import java.util.Locale

interface SearchInteractor {

    /**
     * Text and tags are independent filters. Either narrows the catalog
     * on its own; together, tags narrow what the text found.
     */
    fun searchApps(
        query: String,
        tags: List<String>,
        offset: Int = 0
    ): Observable<AppsPage>

    /** Tags carried by enough apps to be worth offering as a starting point. */
    fun loadPopularTags(): Observable<List<String>>

    /** Searches made before, newest first. */
    fun loadHistory(): Observable<List<SearchHistoryEntry>>

    /**
     * Remembers a search, if it is one worth offering again. These three
     * answer with the history as it stands afterwards.
     */
    fun addToHistory(query: String, tags: List<String>): Observable<List<SearchHistoryEntry>>

    fun removeFromHistory(entry: SearchHistoryEntry): Observable<List<SearchHistoryEntry>>

    fun clearHistory(): Observable<List<SearchHistoryEntry>>

}

class SearchInteractorImpl(
    private val api: StoreApi,
    private val historyStorage: SearchHistoryStorage,
    private val locale: Locale,
    private val schedulers: SchedulersFactory
) : SearchInteractor {

    override fun searchApps(
        query: String,
        tags: List<String>,
        offset: Int
    ): Observable<AppsPage> {
        return api.searchApps(
            query = query.takeIf { it.isNotBlank() },
            tags = tags.takeIf { it.isNotEmpty() }?.joinToString(separator = ","),
            offset = offset.takeIf { it > 0 },
            locale = locale.language
        )
            .map { list ->
                AppsPage(
                    entries = list.result.files,
                    contentFilter = list.result.contentFilter.orEmpty(),
                )
            }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun loadPopularTags(): Observable<List<String>> {
        return api.getTags(minCount = POPULAR_TAGS_MIN_COUNT)
            .map { response ->
                response.result.tags
                    .take(POPULAR_TAGS_COUNT)
                    .map { it.tag }
            }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun loadHistory(): Observable<List<SearchHistoryEntry>> {
        return Observable.fromCallable { historyStorage.load() }
            .subscribeOn(schedulers.io())
    }

    override fun addToHistory(
        query: String,
        tags: List<String>
    ): Observable<List<SearchHistoryEntry>> {
        return Observable.fromCallable { remember(SearchHistoryEntry(query.trim(), tags)) }
            .subscribeOn(schedulers.io())
    }

    override fun removeFromHistory(
        entry: SearchHistoryEntry
    ): Observable<List<SearchHistoryEntry>> {
        return Observable.fromCallable { store(historyStorage.load().filterNot { it == entry }) }
            .subscribeOn(schedulers.io())
    }

    override fun clearHistory(): Observable<List<SearchHistoryEntry>> {
        return Observable.fromCallable { store(emptyList()) }
            .subscribeOn(schedulers.io())
    }

    private fun remember(entry: SearchHistoryEntry): List<SearchHistoryEntry> {
        val previous = historyStorage.load()
        if (!entry.isWorthRemembering()) return previous
        // A word is typed through the words before it, and each of them
        // stands still long enough to be remembered. Only the latest
        // such step gives way to what it grew into — an older search
        // that happens to read as a prefix is a search in its own right.
        val head = previous.firstOrNull()
        val kept = (if (head != null && head.isStepTowards(entry)) previous.drop(1) else previous)
            // The same search asked twice is one entry, back at the top.
            .filterNot { it.isSameSearch(entry) }
        return store(listOf(entry) + kept)
    }

    private fun store(history: List<SearchHistoryEntry>): List<SearchHistoryEntry> {
        return history.take(HISTORY_SIZE).also { historyStorage.save(it) }
    }

    /** A single letter is a search for everything; tags carry a search on their own. */
    private fun SearchHistoryEntry.isWorthRemembering(): Boolean =
        tags.isNotEmpty() || query.length >= MIN_QUERY_LENGTH

    private fun SearchHistoryEntry.isSameSearch(other: SearchHistoryEntry): Boolean =
        query.equals(other.query, ignoreCase = true) && hasSameTags(other)

    private fun SearchHistoryEntry.isStepTowards(other: SearchHistoryEntry): Boolean =
        query.isNotEmpty() && other.query.startsWith(query, ignoreCase = true) && hasSameTags(other)

    private fun SearchHistoryEntry.hasSameTags(other: SearchHistoryEntry): Boolean =
        tags.sorted() == other.tags.sorted()

}

// The vocabulary has a long tail — most tags belong to a couple of apps
// — so the suggestions come from the substantial end of it. Even so it
// runs into the thousands, and the whole of it arrives in one response,
// so what is kept is the head: enough to browse batch by batch and to
// match what is being typed against, without holding a dictionary.
private const val POPULAR_TAGS_MIN_COUNT = 3
private const val POPULAR_TAGS_COUNT = 300

// Deep enough that asking for more of the history has something to
// show, short enough that nobody scrolls one looking for a search.
private const val HISTORY_SIZE = 20

// The history is for searches somebody could mean to make again.
private const val MIN_QUERY_LENGTH = 2
