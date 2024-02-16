package com.tomclaw.appsend.screen.profile

import android.annotation.SuppressLint
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.hideWithAlphaAnimation
import com.tomclaw.appsend.util.show
import com.tomclaw.appsend.util.showWithAlphaAnimation
import io.reactivex.rxjava3.core.Observable

interface ProfileView {

    fun showProgress()

    fun showContent()

    fun showMenu(canEliminate: Boolean)

    fun hideMenu()

    fun showError()

    fun hideError()

    fun showUnauthorizedError()

    fun contentUpdated()

    fun contentUpdated(position: Int)

    fun navigationClicks(): Observable<Unit>

    fun swipeRefresh(): Observable<Unit>

    fun shareClicks(): Observable<Unit>

    fun retryClicks(): Observable<Unit>

    fun loginClicks(): Observable<Unit>

}

class ProfileViewImpl(
    view: View,
    private val adapter: SimpleRecyclerAdapter
) : ProfileView {

    private val context = view.context
    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val swipeRefresh: SwipeRefreshLayout = view.findViewById(R.id.swipe_refresh)
    private val recycler: RecyclerView = view.findViewById(R.id.recycler)
    private val error: View = view.findViewById(R.id.error)
    private val blockingProgress: View = view.findViewById(R.id.blocking_progress)
    private val retryButton: View = view.findViewById(R.id.retry_button)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val refreshRelay = PublishRelay.create<Unit>()
    private val shareRelay = PublishRelay.create<Unit>()
    private val eliminateRelay = PublishRelay.create<Unit>()
    private val retryRelay = PublishRelay.create<Unit>()
    private val loginRelay = PublishRelay.create<Unit>()

    private val layoutManager: LinearLayoutManager

    init {
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.share -> shareRelay.accept(Unit)
                R.id.eliminate -> eliminateRelay.accept(Unit)
            }
            true
        }

        swipeRefresh.setOnRefreshListener {
            refreshRelay.accept(Unit)
        }

        retryButton.setOnClickListener { retryRelay.accept(Unit) }

        val orientation = RecyclerView.VERTICAL
        layoutManager = LinearLayoutManager(view.context, orientation, false)
        adapter.setHasStableIds(true)
        recycler.adapter = adapter
        recycler.layoutManager = layoutManager
        recycler.itemAnimator = DefaultItemAnimator()
        recycler.itemAnimator?.changeDuration = DURATION_MEDIUM
    }

    override fun showProgress() {
        blockingProgress.showWithAlphaAnimation(animateFully = true)
    }

    override fun showContent() {
        blockingProgress.hideWithAlphaAnimation(animateFully = false)
        swipeRefresh.isRefreshing = false
    }

    override fun showMenu(
        canEliminate: Boolean,
    ) {
        toolbar.menu.clear()
        toolbar.inflateMenu(R.menu.profile_menu)
        if (!canEliminate) {
            toolbar.menu.removeItem(R.id.eliminate)
        }
        toolbar.invalidateMenu()
    }

    override fun hideMenu() {
        toolbar.menu.clear()
        toolbar.invalidateMenu()
    }

    override fun showError() {
        error.show()
    }

    override fun hideError() {
        error.hide()
    }

    override fun showUnauthorizedError() {
        Snackbar
            .make(recycler, R.string.authorization_required_message, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.login_button) {
                loginRelay.accept(Unit)
            }
            .show()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun contentUpdated() {
        adapter.notifyDataSetChanged()
    }

    override fun contentUpdated(position: Int) {
        adapter.notifyItemChanged(position)
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

    override fun swipeRefresh(): Observable<Unit> = refreshRelay

    override fun shareClicks(): Observable<Unit> = shareRelay

    override fun retryClicks(): Observable<Unit> = retryRelay

    override fun loginClicks(): Observable<Unit> = loginRelay

}

private const val DURATION_MEDIUM = 300L
