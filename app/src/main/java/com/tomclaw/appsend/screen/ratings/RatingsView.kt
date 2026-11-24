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
import com.tomclaw.appsend.util.adapter.SimpleRecyclerAdapter
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.core.permissions.Capability
import com.tomclaw.appsend.core.permissions.CapabilityHintResolver
import com.tomclaw.appsend.screen.details.api.RATING_SCOPE_BUILD
import com.tomclaw.appsend.screen.details.api.RATING_SCOPE_EDITION
import com.tomclaw.appsend.util.applyBottomInsets
import com.tomclaw.appsend.util.clicks
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.show
import io.reactivex.rxjava3.core.Observable

interface RatingsView {

    fun showProgress()

    fun showContent()

    fun contentUpdated()

    fun contentUpdated(position: Int)

    fun showPlaceholder()

    fun showError()

    fun showRatingRemovalFailed()

    fun showUnauthorizedError()

    /**
     * Surface a capability-denied response (server rejected the
     * action because of an ACL/ownership/role check). Routed through
     * the same hint resolver as proactive UI.
     */
    fun showCapabilityDenied(capability: Capability)

    fun stopPullRefreshing()

    fun isPullRefreshing(): Boolean

    fun showScopeToggle(selectedScope: String)

    fun hideScopeToggle()

    fun navigationClicks(): Observable<Unit>

    fun retryClicks(): Observable<Unit>

    fun refreshClicks(): Observable<Unit>

    fun scopeClicks(): Observable<String>

}

class RatingsViewImpl(
    view: View,
    private val adapter: SimpleRecyclerAdapter
) : RatingsView {

    private val context = view.context
    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val scopeGroup: MaterialButtonToggleGroup = view.findViewById(R.id.reviews_scope_group)
    private val refresher: SwipeRefreshLayout = view.findViewById(R.id.swipe_refresh)
    private val flipper: ViewFlipper = view.findViewById(R.id.view_flipper)
    private val recycler: RecyclerView = view.findViewById(R.id.recycler)
    private val error: TextView = view.findViewById(R.id.error_text)
    private val retryButton: View = view.findViewById(R.id.button_retry)
    private val emptyText: TextView = view.findViewById(R.id.empty_text)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val retryRelay = PublishRelay.create<Unit>()
    private val refreshRelay = PublishRelay.create<Unit>()
    private val scopeRelay = PublishRelay.create<String>()
    private var suppressScopeChange: Boolean = false

    init {
        toolbar.setTitle(R.string.ratings)
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }

        scopeGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (suppressScopeChange || !isChecked) return@addOnButtonCheckedListener
            val scope = when (checkedId) {
                R.id.scope_build -> RATING_SCOPE_BUILD
                else -> RATING_SCOPE_EDITION
            }
            scopeRelay.accept(scope)
        }

        val orientation = RecyclerView.VERTICAL
        val layoutManager = LinearLayoutManager(view.context, orientation, false)
        adapter.setHasStableIds(true)
        recycler.adapter = adapter
        recycler.layoutManager = layoutManager
        recycler.itemAnimator = DefaultItemAnimator()
        recycler.itemAnimator?.changeDuration = DURATION_MEDIUM

        refresher.setOnRefreshListener { refreshRelay.accept(Unit) }

        emptyText.text = context.getString(R.string.empty_list)

        // Insets: the list keeps its last row clear of the
        // navigation bar while still scrolling underneath it.
        recycler.applyBottomInsets()
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

    override fun showUnauthorizedError() {
        Snackbar.make(recycler, R.string.authorization_required_message, Snackbar.LENGTH_LONG).show()
    }

    override fun showCapabilityDenied(capability: Capability) {
        val text = CapabilityHintResolver(recycler.resources).resolveText(capability)
        Snackbar.make(recycler, text, Snackbar.LENGTH_LONG).show()
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

    override fun showScopeToggle(selectedScope: String) {
        scopeGroup.show()
        suppressScopeChange = true
        val targetId = if (selectedScope == RATING_SCOPE_BUILD) {
            R.id.scope_build
        } else {
            R.id.scope_edition
        }
        if (scopeGroup.checkedButtonId != targetId) {
            scopeGroup.check(targetId)
        }
        suppressScopeChange = false
    }

    override fun hideScopeToggle() {
        scopeGroup.hide()
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

    override fun retryClicks(): Observable<Unit> = retryRelay

    override fun refreshClicks(): Observable<Unit> = refreshRelay

    override fun scopeClicks(): Observable<String> = scopeRelay

}

private const val DURATION_MEDIUM = 300L
