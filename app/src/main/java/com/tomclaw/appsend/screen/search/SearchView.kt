package com.tomclaw.appsend.screen.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.tomclaw.appsend.util.adapter.SimpleRecyclerAdapter
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.core.content.ContentFlag
import com.tomclaw.appsend.util.applyBottomInsetsWithIme
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.util.changes
import com.tomclaw.appsend.util.clicks
import com.tomclaw.appsend.util.hideWithAlphaAnimation
import com.tomclaw.appsend.util.showWithAlphaAnimation
import io.reactivex.rxjava3.core.Observable

interface SearchView {

    fun showProgress()

    fun showContent()

    fun contentUpdated()

    /** A fresh result set starts at the top, not where the last one was left. */
    fun scrollToTop()

    /** Nothing searched for yet — the state that offers popular tags. */
    fun showPlaceholder()

    /** Searched, and nothing matched. */
    fun showEmptyResult()

    fun showError()

    fun stopPullRefreshing()

    fun isPullRefreshing(): Boolean

    fun setQueryText(query: String)

    fun requestQueryFocus()

    /** Puts the keyboard away, and answers whether it was up at all. */
    fun hideKeyboard(): Boolean

    /**
     * The filter row next to the query: [selected] is what search is
     * narrowed by, [suggestions] is what it could be narrowed by next,
     * and [custom] is the typed text offered as a tag of its own.
     */
    fun showTags(selected: List<String>, suggestions: List<String>, custom: String?)

    /**
     * Says which content these results are missing because the viewer
     * asked for it to be. Hidden when nothing is being withheld.
     */
    fun showContentFilter(codes: List<String>)

    fun contentFilterClicks(): Observable<Unit>

    /** [hasMore] offers the next batch of the vocabulary, if any is left. */
    fun showPopularTags(tags: List<String>, hasMore: Boolean)

    /**
     * Searches made before, newest first, above the tags on the
     * placeholder. [hasMore] offers the next batch of them. An empty
     * list takes the whole section away rather than leaving a heading.
     */
    fun showHistory(items: List<SearchHistoryItem>, hasMore: Boolean)

    fun navigationClicks(): Observable<Unit>

    fun retryClicks(): Observable<Unit>

    fun refreshClicks(): Observable<Unit>

    fun queryTextChanges(): Observable<String>

    /** The search key on the keyboard: this query, and no more typing. */
    fun searchActions(): Observable<Unit>

    fun tagRemoveClicks(): Observable<String>

    fun tagSuggestionClicks(): Observable<String>

    fun customTagClicks(): Observable<String>

    fun popularTagClicks(): Observable<String>

    fun moreTagsClicks(): Observable<Unit>

    fun historyClicks(): Observable<SearchHistoryItem>

    fun historyRemoveClicks(): Observable<SearchHistoryItem>

    fun historyClearClicks(): Observable<Unit>

    fun moreHistoryClicks(): Observable<Unit>

}

class SearchViewImpl(
    rootView: View,
    private val adapter: SimpleRecyclerAdapter,
) : SearchView {

    private val context = rootView.context
    private val toolbar: Toolbar = rootView.findViewById(R.id.toolbar)
    private val refresher: SwipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh)
    private val flipper: ViewFlipper = rootView.findViewById(R.id.view_flipper)
    private val overlayProgress: View = rootView.findViewById(R.id.overlay_progress)
    private val recycler: RecyclerView = rootView.findViewById(R.id.recycler)
    private val error: TextView = rootView.findViewById(R.id.error_text)
    private val retryButton: View = rootView.findViewById(R.id.button_retry)
    private val queryEdit: EditText = rootView.findViewById(R.id.query_edit)
    private val tagsScroll: View = rootView.findViewById(R.id.tags_scroll)
    private val contentFilterChip: Chip = rootView.findViewById(R.id.chip_content_filter)
    private val tagsGroup: ChipGroup = rootView.findViewById(R.id.tags)
    private val popularTagsTitle: View = rootView.findViewById(R.id.popular_tags_title)
    private val popularTags: ChipGroup = rootView.findViewById(R.id.popular_tags)
    private val historyBlock: View = rootView.findViewById(R.id.history_block)
    private val historyItems: ViewGroup = rootView.findViewById(R.id.history_items)
    private val historyClear: View = rootView.findViewById(R.id.history_clear)
    private val historyMore: View = rootView.findViewById(R.id.history_more)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val retryRelay = PublishRelay.create<Unit>()
    private val refreshRelay = PublishRelay.create<Unit>()
    private val queryTextRelay = PublishRelay.create<String>()
    private val searchActionRelay = PublishRelay.create<Unit>()
    private val tagRemoveRelay = PublishRelay.create<String>()
    private val tagSuggestionRelay = PublishRelay.create<String>()
    private val customTagRelay = PublishRelay.create<String>()
    private val popularTagRelay = PublishRelay.create<String>()
    private val contentFilterRelay = PublishRelay.create<Unit>()
    private val moreTagsRelay = PublishRelay.create<Unit>()
    private val historyRelay = PublishRelay.create<SearchHistoryItem>()
    private val historyRemoveRelay = PublishRelay.create<SearchHistoryItem>()
    private val historyClearRelay = PublishRelay.create<Unit>()
    private val moreHistoryRelay = PublishRelay.create<Unit>()

    init {
        val orientation = RecyclerView.VERTICAL
        val layoutManager = LinearLayoutManager(context, orientation, false)
        adapter.setHasStableIds(true)
        recycler.adapter = adapter
        recycler.layoutManager = layoutManager
        recycler.itemAnimator = DefaultItemAnimator()
        recycler.itemAnimator?.changeDuration = DURATION_MEDIUM

        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }

        // Not a criterion of this search — it says the account has a
        // content setting, and leads to where that is changed.
        contentFilterChip.setOnClickListener { contentFilterRelay.accept(Unit) }

        refresher.setOnRefreshListener { refreshRelay.accept(Unit) }

        queryEdit.changes { text ->
            queryTextRelay.accept(text)
        }
        queryEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // The results are already below, put there by the
                // debounce — so the search key has nothing left to do
                // but get the keyboard out of their way.
                hideKeyboard()
                searchActionRelay.accept(Unit)
                true
            } else {
                false
            }
        }

        historyClear.clicks(historyClearRelay)
        historyMore.clicks(moreHistoryRelay)

        // Insets: the list keeps its last row clear of the navigation bar,
        // and of the keyboard the query field brings up.
        recycler.applyBottomInsetsWithIme()
    }

    override fun showProgress() {
        refresher.isEnabled = false
        flipper.displayedChild = CHILD_CONTENT
        overlayProgress.showWithAlphaAnimation(animateFully = true)
    }

    override fun showContent() {
        refresher.isEnabled = true
        flipper.displayedChild = CHILD_CONTENT
        overlayProgress.hideWithAlphaAnimation(animateFully = false)
    }

    override fun showPlaceholder() {
        refresher.isRefreshing = false
        refresher.isEnabled = false
        flipper.displayedChild = CHILD_PLACEHOLDER
        overlayProgress.hideWithAlphaAnimation(animateFully = false)
    }

    override fun showEmptyResult() {
        refresher.isRefreshing = false
        refresher.isEnabled = true
        flipper.displayedChild = CHILD_EMPTY
        overlayProgress.hideWithAlphaAnimation(animateFully = false)
    }

    override fun showError() {
        refresher.isEnabled = true
        flipper.displayedChild = CHILD_ERROR

        error.setText(R.string.load_files_error)
        retryButton.clicks(retryRelay)
    }

    override fun contentUpdated() {
        adapter.notifyDataSetChanged()
    }

    override fun scrollToTop() {
        recycler.scrollToPosition(0)
    }

    override fun stopPullRefreshing() {
        refresher.isRefreshing = false
    }

    override fun isPullRefreshing(): Boolean = refresher.isRefreshing

    override fun setQueryText(query: String) {
        queryEdit.setText(query)
        queryEdit.setSelection(query.length)
    }

    override fun requestQueryFocus() {
        queryEdit.requestFocus()
    }

    override fun hideKeyboard(): Boolean {
        // Whether it was up is only knowable from API 30 on, so the
        // hiding is not conditional on it — the search key wants it
        // put away either way.
        val shown = ViewCompat.getRootWindowInsets(queryEdit)
            ?.isVisible(WindowInsetsCompat.Type.ime()) == true
        val manager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(queryEdit.windowToken, 0)
        return shown
    }

    override fun showContentFilter(codes: List<String>) {
        val flags = ContentFlag.entries.filter { it.code in codes }
        contentFilterChip.isVisible = flags.isNotEmpty()
        if (flags.isEmpty()) return
        val names = flags.joinToString { context.getString(it.titleRes) }
        contentFilterChip.text = context.getString(R.string.content_filter_active, names)
    }

    override fun contentFilterClicks(): Observable<Unit> = contentFilterRelay

    override fun showTags(selected: List<String>, suggestions: List<String>, custom: String?) {
        tagsGroup.removeAllViews()
        for (tag in selected) {
            val chip = inflateChip(R.layout.search_selected_tag_chip, tagsGroup, tag)
            chip.setOnCloseIconClickListener { tagRemoveRelay.accept(tag) }
            tagsGroup.addView(chip)
        }
        for (tag in suggestions) {
            val chip = inflateChip(R.layout.search_tag_chip, tagsGroup, tag)
            chip.setOnClickListener { tagSuggestionRelay.accept(tag) }
            tagsGroup.addView(chip)
        }
        if (custom != null) {
            val label = context.getString(R.string.search_add_tag, custom)
            val chip = inflateChip(R.layout.search_custom_tag_chip, tagsGroup, label)
            chip.setOnClickListener { customTagRelay.accept(custom) }
            tagsGroup.addView(chip)
        }
        tagsScroll.isVisible = tagsGroup.childCount > 0
    }

    override fun showPopularTags(tags: List<String>, hasMore: Boolean) {
        popularTagsTitle.isVisible = tags.isNotEmpty()
        popularTags.removeAllViews()
        for (tag in tags) {
            val chip = inflateChip(R.layout.search_tag_chip, popularTags, tag)
            chip.setOnClickListener { popularTagRelay.accept(tag) }
            popularTags.addView(chip)
        }
        if (hasMore) {
            val label = context.getString(R.string.search_more_tags)
            val chip = inflateChip(R.layout.search_more_tags_chip, popularTags, label)
            chip.setOnClickListener { moreTagsRelay.accept(Unit) }
            popularTags.addView(chip)
        }
    }

    override fun showHistory(items: List<SearchHistoryItem>, hasMore: Boolean) {
        historyBlock.isVisible = items.isNotEmpty()
        historyMore.isVisible = hasMore
        historyItems.removeAllViews()
        for (item in items) {
            historyItems.addView(inflateHistoryItem(item))
        }
    }

    private fun inflateHistoryItem(item: SearchHistoryItem): View {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.search_history_item, historyItems, false)
        view.findViewById<TextView>(R.id.history_query).text = item.title
        view.findViewById<TextView>(R.id.history_tags).bind(item.subtitle)
        view.setOnClickListener { historyRelay.accept(item) }
        view.findViewById<View>(R.id.history_remove).setOnClickListener {
            historyRemoveRelay.accept(item)
        }
        return view
    }

    private fun inflateChip(@LayoutRes layout: Int, group: ChipGroup, text: String): Chip {
        val chip = LayoutInflater.from(context).inflate(layout, group, false) as Chip
        chip.text = text
        // Without this the invisible 48dp touch target pads every
        // chip and the rows drift apart (see TagsItemView).
        chip.setEnsureMinTouchTargetSize(false)
        return chip
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

    override fun retryClicks(): Observable<Unit> = retryRelay

    override fun refreshClicks(): Observable<Unit> = refreshRelay

    override fun queryTextChanges(): Observable<String> = queryTextRelay

    override fun searchActions(): Observable<Unit> = searchActionRelay

    override fun tagRemoveClicks(): Observable<String> = tagRemoveRelay

    override fun tagSuggestionClicks(): Observable<String> = tagSuggestionRelay

    override fun customTagClicks(): Observable<String> = customTagRelay

    override fun popularTagClicks(): Observable<String> = popularTagRelay

    override fun moreTagsClicks(): Observable<Unit> = moreTagsRelay

    override fun historyClicks(): Observable<SearchHistoryItem> = historyRelay

    override fun historyRemoveClicks(): Observable<SearchHistoryItem> = historyRemoveRelay

    override fun historyClearClicks(): Observable<Unit> = historyClearRelay

    override fun moreHistoryClicks(): Observable<Unit> = moreHistoryRelay

}

/**
 * One row of the history, as the placeholder shows it. [title] and
 * [subtitle] are the row's two lines, already decided on; [query] and
 * [tags] are the criteria it restores when it is tapped.
 */
data class SearchHistoryItem(
    val query: String,
    val tags: List<String>,
    val title: String,
    val subtitle: String?,
)

private const val DURATION_MEDIUM = 300L

// ViewFlipper child order, mirroring activity_search.xml.
private const val CHILD_CONTENT = 0
private const val CHILD_PLACEHOLDER = 1
private const val CHILD_EMPTY = 2
private const val CHILD_ERROR = 3
