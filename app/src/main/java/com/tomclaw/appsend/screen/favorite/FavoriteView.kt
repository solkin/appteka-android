package com.tomclaw.appsend.screen.favorite

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.adapter.SimpleRecyclerAdapter
import com.tomclaw.appsend.util.clicks
import io.reactivex.rxjava3.core.Observable

interface FavoriteView {

    fun showProgress()

    fun showContent()

    fun contentUpdated()

    fun contentUpdated(position: Int)

    fun itemRemoved(position: Int)

    fun itemInserted(position: Int)

    fun showPlaceholder()

    fun showError()

    fun stopPullRefreshing()

    fun isPullRefreshing(): Boolean

    fun showUndoSnackbar()

    fun showRemoveError()

    fun navigationClicks(): Observable<Unit>

    fun retryClicks(): Observable<Unit>

    fun refreshClicks(): Observable<Unit>

    fun removeSwipes(): Observable<Long>

    fun undoClicks(): Observable<Unit>

    fun removeCommits(): Observable<Unit>

}

class FavoriteViewImpl(
    view: View,
    private val adapter: SimpleRecyclerAdapter
) : FavoriteView {

    private val context = view.context
    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val refresher: SwipeRefreshLayout = view.findViewById(R.id.swipe_refresh)
    private val flipper: ViewFlipper = view.findViewById(R.id.view_flipper)
    private val recycler: RecyclerView = view.findViewById(R.id.recycler)
    private val error: TextView = view.findViewById(R.id.error_text)
    private val retryButton: View = view.findViewById(R.id.button_retry)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val retryRelay = PublishRelay.create<Unit>()
    private val refreshRelay = PublishRelay.create<Unit>()
    private val removeSwipesRelay = PublishRelay.create<Long>()
    private val undoRelay = PublishRelay.create<Unit>()
    private val removeCommitRelay = PublishRelay.create<Unit>()

    private var activeSnackbar: Snackbar? = null
    private var activeSnackbarCallback: UndoSnackbarCallback? = null

    init {
        toolbar.setTitle(R.string.favorite_activity)
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }

        val orientation = RecyclerView.VERTICAL
        val layoutManager = LinearLayoutManager(view.context, orientation, false)
        adapter.setHasStableIds(true)
        recycler.adapter = adapter
        recycler.layoutManager = layoutManager
        recycler.itemAnimator = DefaultItemAnimator()
        recycler.itemAnimator?.changeDuration = DURATION_MEDIUM

        refresher.setOnRefreshListener { refreshRelay.accept(Unit) }

        val swipeCallback = FavoriteSwipeCallback(
            context = context,
            onSwiped = { itemId -> removeSwipesRelay.accept(itemId) },
        )
        ItemTouchHelper(swipeCallback).attachToRecyclerView(recycler)
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

    @SuppressLint("NotifyDataSetChanged")
    override fun contentUpdated() {
        adapter.notifyDataSetChanged()
    }

    override fun contentUpdated(position: Int) {
        adapter.notifyItemChanged(position)
    }

    override fun itemRemoved(position: Int) {
        adapter.notifyItemRemoved(position)
    }

    override fun itemInserted(position: Int) {
        adapter.notifyItemInserted(position)
    }

    override fun stopPullRefreshing() {
        refresher.isRefreshing = false
    }

    override fun isPullRefreshing(): Boolean = refresher.isRefreshing

    override fun showUndoSnackbar() {
        activeSnackbarCallback?.superseded = true
        activeSnackbar?.dismiss()
        val callback = UndoSnackbarCallback(removeCommitRelay::accept)
        val snackbar = Snackbar
            .make(recycler, R.string.unmarked_favorite, Snackbar.LENGTH_LONG)
            .setAction(R.string.favorite_undo) { undoRelay.accept(Unit) }
            .addCallback(callback)
        activeSnackbar = snackbar
        activeSnackbarCallback = callback
        snackbar.show()
    }

    override fun showRemoveError() {
        activeSnackbarCallback?.superseded = true
        activeSnackbar?.dismiss()
        activeSnackbar = null
        activeSnackbarCallback = null
        Snackbar
            .make(recycler, R.string.unmark_favorite_error, Snackbar.LENGTH_SHORT)
            .show()
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

    override fun retryClicks(): Observable<Unit> = retryRelay

    override fun refreshClicks(): Observable<Unit> = refreshRelay

    override fun removeSwipes(): Observable<Long> = removeSwipesRelay

    override fun undoClicks(): Observable<Unit> = undoRelay

    override fun removeCommits(): Observable<Unit> = removeCommitRelay

}

private class UndoSnackbarCallback(
    private val onCommit: (Unit) -> Unit,
) : Snackbar.Callback() {

    var superseded: Boolean = false

    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
        if (superseded) return
        if (event != DISMISS_EVENT_ACTION) {
            onCommit(Unit)
        }
    }

}

private const val DURATION_MEDIUM = 300L
