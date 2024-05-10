package com.tomclaw.appsend.screen.ratings

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.clicks
import io.reactivex.rxjava3.core.Observable

interface RatingsView {

    fun showProgress()

    fun showContent()

    fun contentUpdated()

    fun contentUpdated(position: Int)

    fun showPlaceholder()

    fun showError()

    fun showRatingRemovalFailed()

    fun stopPullRefreshing()

    fun isPullRefreshing(): Boolean

    fun navigationClicks(): Observable<Unit>

    fun retryClicks(): Observable<Unit>

    fun refreshClicks(): Observable<Unit>

}

class RatingsViewImpl(
    view: View,
    private val adapter: SimpleRecyclerAdapter
) : RatingsView {

    private val context = view.context
    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val refresher: SwipeRefreshLayout = view.findViewById(R.id.swipe_refresh)
    private val flipper: ViewFlipper = view.findViewById(R.id.view_flipper)
    private val recycler: RecyclerView = view.findViewById(R.id.recycler)
    private val error: TextView = view.findViewById(R.id.error_text)
    private val retryButton: View = view.findViewById(R.id.button_retry)
    private val emptyText: TextView = view.findViewById(R.id.empty_text)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val retryRelay = PublishRelay.create<Unit>()
    private val refreshRelay = PublishRelay.create<Unit>()

    init {
        toolbar.setTitle(R.string.ratings)
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }

        val orientation = RecyclerView.VERTICAL
        val layoutManager = LinearLayoutManager(view.context, orientation, false)
        adapter.setHasStableIds(true)
        recycler.adapter = adapter
        recycler.layoutManager = layoutManager
        recycler.itemAnimator = DefaultItemAnimator()
        recycler.itemAnimator?.changeDuration = DURATION_MEDIUM

        refresher.setOnRefreshListener { refreshRelay.accept(Unit) }

        emptyText.text = context.getString(R.string.empty_list)
    }

    override fun showProgress() {
        refresher.isEnabled = false
        flipper.displayedChild = 0
    }

    override fun showContent() {
        refresher.isEnabled = true
        flipper.displayedChild = 1
    }

    override fun showPlaceholder() {
        refresher.isRefreshing = false
        refresher.isEnabled = true
        flipper.displayedChild = 2
    }

    override fun showError() {
        refresher.isEnabled = true
        flipper.displayedChild = 3

        error.setText(R.string.load_files_error)
        retryButton.clicks(retryRelay)
    }

    override fun showRatingRemovalFailed() {
        Snackbar.make(recycler, R.string.error_rating_deletion, Snackbar.LENGTH_LONG).show()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun contentUpdated() {
        adapter.notifyDataSetChanged()
    }

    override fun contentUpdated(position: Int) {
        adapter.notifyItemChanged(position)
    }

    override fun stopPullRefreshing() {
        refresher.isRefreshing = false
    }

    override fun isPullRefreshing(): Boolean = refresher.isRefreshing

    override fun navigationClicks(): Observable<Unit> = navigationRelay

    override fun retryClicks(): Observable<Unit> = retryRelay

    override fun refreshClicks(): Observable<Unit> = refreshRelay

}

private const val DURATION_MEDIUM = 300L
