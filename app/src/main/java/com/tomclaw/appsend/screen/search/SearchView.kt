package com.tomclaw.appsend.screen.search

import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.tomclaw.appsend.util.adapter.SimpleRecyclerAdapter
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.changes
import com.tomclaw.appsend.util.clicks
import com.tomclaw.appsend.util.hideWithAlphaAnimation
import com.tomclaw.appsend.util.showWithAlphaAnimation
import io.reactivex.rxjava3.core.Observable

interface SearchView {

    fun showProgress()

    fun showContent()

    fun contentUpdated()

    fun showPlaceholder()

    fun showError()

    fun stopPullRefreshing()

    fun isPullRefreshing(): Boolean

    fun getQueryText(): String

    fun setQueryText(query: String)

    fun requestQueryFocus()

    fun retryClicks(): Observable<Unit>

    fun refreshClicks(): Observable<Unit>

    fun queryTextChanges(): Observable<String>

}

class SearchViewImpl(
    rootView: View,
    private val adapter: SimpleRecyclerAdapter,
) : SearchView {

    private val context = rootView.context
    private val refresher: SwipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh)
    private val flipper: ViewFlipper = rootView.findViewById(R.id.view_flipper)
    private val overlayProgress: View = rootView.findViewById(R.id.overlay_progress)
    private val recycler: RecyclerView = rootView.findViewById(R.id.recycler)
    private val error: TextView = rootView.findViewById(R.id.error_text)
    private val retryButton: View = rootView.findViewById(R.id.button_retry)
    private val queryEdit: EditText = rootView.findViewById(R.id.query_edit)

    private val retryRelay = PublishRelay.create<Unit>()
    private val refreshRelay = PublishRelay.create<Unit>()
    private val queryTextRelay = PublishRelay.create<String>()

    init {
        val orientation = RecyclerView.VERTICAL
        val layoutManager = LinearLayoutManager(context, orientation, false)
        adapter.setHasStableIds(true)
        recycler.adapter = adapter
        recycler.layoutManager = layoutManager
        recycler.itemAnimator = DefaultItemAnimator()
        recycler.itemAnimator?.changeDuration = DURATION_MEDIUM

        refresher.setOnRefreshListener { refreshRelay.accept(Unit) }

        queryEdit.changes { text ->
            queryTextRelay.accept(text)
        }
    }

    override fun showProgress() {
        refresher.isEnabled = false
        flipper.displayedChild = 0
        overlayProgress.showWithAlphaAnimation(animateFully = true)
    }

    override fun showContent() {
        refresher.isEnabled = true
        flipper.displayedChild = 0
        overlayProgress.hideWithAlphaAnimation(animateFully = false)
    }

    override fun showPlaceholder() {
        refresher.isRefreshing = false
        refresher.isEnabled = true
        flipper.displayedChild = 1
        overlayProgress.hideWithAlphaAnimation(animateFully = false)
    }

    override fun showError() {
        refresher.isEnabled = true
        flipper.displayedChild = 2

        error.setText(R.string.load_files_error)
        retryButton.clicks(retryRelay)
    }

    override fun contentUpdated() {
        adapter.notifyDataSetChanged()
    }

    override fun stopPullRefreshing() {
        refresher.isRefreshing = false
    }

    override fun isPullRefreshing(): Boolean = refresher.isRefreshing

    override fun getQueryText(): String = queryEdit.text.toString()

    override fun setQueryText(query: String) {
        queryEdit.setText(query)
    }

    override fun requestQueryFocus() {
        queryEdit.requestFocus()
    }

    override fun retryClicks(): Observable<Unit> = retryRelay

    override fun refreshClicks(): Observable<Unit> = refreshRelay

    override fun queryTextChanges(): Observable<String> = queryTextRelay

}

private const val DURATION_MEDIUM = 300L

