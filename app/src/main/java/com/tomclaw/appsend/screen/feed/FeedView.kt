package com.tomclaw.appsend.screen.feed

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.github.rubensousa.bottomsheetbuilder.BottomSheetBuilder
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.clicks
import com.tomclaw.appsend.util.getAttributedColor
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.hideWithAlphaAnimation
import com.tomclaw.appsend.util.show
import com.tomclaw.appsend.util.showWithAlphaAnimation
import io.reactivex.rxjava3.core.Observable

interface FeedView {

    fun showProgress()

    fun showContent()

    fun showToolbar()

    fun hideToolbar()

    fun contentUpdated()

    fun contentUpdated(position: Int)

    fun rangeInserted(position: Int, count: Int)

    fun rangeDeleted(position: Int, count: Int)

    fun scrollTo(position: Int)

    fun showPlaceholder()

    fun showError()

    fun showPostDeletionFailed()

    fun showPostMenu(actions: List<MenuAction>)

    fun navigationClicks(): Observable<Unit>

    fun retryClicks(): Observable<Unit>

    fun scrollIdle(): Observable<Int>

}

class FeedViewImpl(
    private val view: View,
    private val adapter: SimpleRecyclerAdapter,
    private val preferences: FeedPreferencesProvider,
) : FeedView {

    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val flipper: ViewFlipper = view.findViewById(R.id.view_flipper)
    private val overlayProgress: View = view.findViewById(R.id.overlay_progress)
    private val recycler: RecyclerView = view.findViewById(R.id.recycler)
    private val error: TextView = view.findViewById(R.id.error_text)
    private val retryButton: View = view.findViewById(R.id.button_retry)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val retryRelay = PublishRelay.create<Unit>()
    private val scrollIdleRelay = PublishRelay.create<Int>()

    init {
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }
        toolbar.setTitle(R.string.user_feed)

        val orientation = RecyclerView.VERTICAL
        val layoutManager = LinearLayoutManager(view.context, orientation, false)
        adapter.setHasStableIds(true)
        recycler.adapter = adapter
        recycler.layoutManager = layoutManager
        recycler.itemAnimator = DefaultItemAnimator()
        recycler.itemAnimator?.changeDuration = DURATION_MEDIUM
        recycler.addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    scrollIdleRelay.accept(layoutManager.findLastVisibleItemPosition())
                }
            }
        })
    }

    override fun showProgress() {
        flipper.displayedChild = 0
        overlayProgress.showWithAlphaAnimation(animateFully = true)
    }

    override fun showContent() {
        flipper.displayedChild = 0
        overlayProgress.hideWithAlphaAnimation(animateFully = false)
    }

    override fun showToolbar() {
        toolbar.show()
    }

    override fun hideToolbar() {
        toolbar.hide()
    }

    override fun showPlaceholder() {
        flipper.displayedChild = 1
    }

    override fun showError() {
        flipper.displayedChild = 2

        error.setText(R.string.load_files_error)
        retryButton.clicks(retryRelay)
    }

    override fun showPostDeletionFailed() {
        Snackbar.make(recycler, R.string.error_post_deletion, Snackbar.LENGTH_LONG).show()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun contentUpdated() {
        adapter.notifyDataSetChanged()
    }

    override fun contentUpdated(position: Int) {
        adapter.notifyItemChanged(position)
    }

    override fun rangeInserted(position: Int, count: Int) {
        adapter.notifyItemRangeInserted(position, count)
    }

    override fun rangeDeleted(position: Int, count: Int) {
        adapter.notifyItemRangeRemoved(position, count)
    }

    override fun scrollTo(position: Int) {
        recycler.scrollToPosition(position)
    }

    override fun showPostMenu(actions: List<MenuAction>) {
        val theme = R.style.BottomSheetDialogDark.takeIf { preferences.isDarkTheme() }
            ?: R.style.BottomSheetDialogLight
        BottomSheetBuilder(view.context, theme)
            .setMode(BottomSheetBuilder.MODE_LIST)
            .setIconTintColor(getAttributedColor(view.context, R.attr.menu_icons_tint))
            .setItemTextColor(getAttributedColor(view.context, R.attr.text_primary_color))
            .apply {
                for (action in actions) {
                    addItem(action.id, action.title, action.icon)
                }
            }
            .setItemClickListener { menuItem ->
                actions.firstOrNull { it.id == menuItem.itemId }?.action?.invoke()
            }
            .createDialog()
            .show()
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

    override fun retryClicks(): Observable<Unit> = retryRelay

    override fun scrollIdle(): Observable<Int> = scrollIdleRelay

}

data class MenuAction(
    val id: Int,
    val title: String,
    @DrawableRes val icon: Int,
    val action: () -> Unit,
)

private const val DURATION_MEDIUM = 300L
