package com.tomclaw.appsend.screen.moderation

import android.view.View
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.clicks
import io.reactivex.rxjava3.core.Observable

interface ModerationView {

    fun showProgress()

    fun showContent()

    fun contentUpdated()

    fun showPlaceholder()

    fun showError()

    fun navigationClicks(): Observable<Unit>

    fun retryClicks(): Observable<Unit>

}

class ModerationViewImpl(
    private val view: View,
    private val adapter: SimpleRecyclerAdapter
) : ModerationView {

    private val context = view.context
    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val refresher: SwipeRefreshLayout = view.findViewById(R.id.swipe_refresh)
    private val flipper: ViewFlipper = view.findViewById(R.id.view_flipper)
    private val recycler: RecyclerView = view.findViewById(R.id.recycler)
    private val error: TextView = view.findViewById(R.id.error_text)
    private val retryButton: View = view.findViewById(R.id.button_retry)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val retryRelay = PublishRelay.create<Unit>()

    init {
        toolbar.setTitle(R.string.apps_on_moderation)
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }

        val orientation = RecyclerView.VERTICAL
        val layoutManager = LinearLayoutManager(view.context, orientation, false)
        adapter.setHasStableIds(true)
        recycler.adapter = adapter
        recycler.layoutManager = layoutManager
        recycler.itemAnimator = DefaultItemAnimator()
        recycler.itemAnimator?.changeDuration = DURATION_MEDIUM
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
        refresher.isEnabled = true
        flipper.displayedChild = 2
    }

    override fun showError() {
        refresher.isEnabled = true
        flipper.displayedChild = 3

        error.setText(R.string.load_files_error)
        retryButton.clicks(retryRelay)
    }

    override fun contentUpdated() {
        adapter.notifyDataSetChanged()
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

    override fun retryClicks(): Observable<Unit> {
        return retryRelay
    }

}

private const val DURATION_MEDIUM = 300L
