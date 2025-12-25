package com.tomclaw.appsend.screen.store

import android.annotation.SuppressLint
import android.view.View
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.clicks
import com.tomclaw.appsend.util.hideWithAlphaAnimation
import com.tomclaw.appsend.util.showWithAlphaAnimation
import com.tomclaw.appsend.util.svgToDrawable
import io.reactivex.rxjava3.core.Observable

interface StoreView {

    fun showProgress()

    fun showContent()

    fun contentUpdated()

    fun contentUpdated(position: Int)

    fun showPlaceholder()

    fun showError()

    fun showCategories(items: List<CategoryDropdownItem>)

    fun setSelectedCategory(item: CategoryDropdownItem)

    fun scrollToTop()

    fun stopPullRefreshing()

    fun isPullRefreshing(): Boolean

    fun retryClicks(): Observable<Unit>

    fun refreshClicks(): Observable<Unit>

    fun categorySelectedClicks(): Observable<Int>

}

class StoreViewImpl(
    view: View,
    private val preferences: StorePreferencesProvider,
    private val adapter: SimpleRecyclerAdapter,
) : StoreView {

    private val context = view.context
    private val refresher: SwipeRefreshLayout = view.findViewById(R.id.swipe_refresh)
    private val flipper: ViewFlipper = view.findViewById(R.id.view_flipper)
    private val overlayProgress: View = view.findViewById(R.id.overlay_progress)
    private val recycler: RecyclerView = view.findViewById(R.id.recycler)
    private val error: TextView = view.findViewById(R.id.error_text)
    private val retryButton: View = view.findViewById(R.id.button_retry)
    private val categoryDropdownLayout: TextInputLayout =
        view.findViewById(R.id.category_dropdown_layout)
    private val categoryDropdown: AutoCompleteTextView =
        view.findViewById(R.id.category_dropdown)

    private val retryRelay = PublishRelay.create<Unit>()
    private val refreshRelay = PublishRelay.create<Unit>()
    private val categorySelectedRelay = PublishRelay.create<Int>()

    init {
        val orientation = RecyclerView.VERTICAL
        val layoutManager = LinearLayoutManager(context, orientation, false)
        adapter.setHasStableIds(true)
        recycler.adapter = adapter
        recycler.layoutManager = layoutManager
        recycler.itemAnimator = DefaultItemAnimator()
        recycler.itemAnimator?.changeDuration = DURATION_MEDIUM

        refresher.setOnRefreshListener { refreshRelay.accept(Unit) }

        retryButton.clicks(retryRelay)

        categoryDropdown.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                categorySelectedRelay.accept(position)
            }
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
        refresher.isEnabled = true
        flipper.displayedChild = CHILD_PLACEHOLDER
    }

    override fun showError() {
        refresher.isEnabled = true
        flipper.displayedChild = CHILD_ERROR

        error.setText(R.string.load_files_error)
    }

    override fun showCategories(items: List<CategoryDropdownItem>) {
        val dropdownAdapter = CategoryDropdownAdapter(context, items)
        categoryDropdown.setAdapter(dropdownAdapter)
    }

    override fun setSelectedCategory(item: CategoryDropdownItem) {
        categoryDropdown.setText(item.title, false)
        updateStartIcon(item)
    }

    override fun scrollToTop() {
        recycler.scrollToPosition(0)
    }

    private fun updateStartIcon(item: CategoryDropdownItem) {
        if (item.iconSvg != null) {
            categoryDropdownLayout.startIconDrawable = svgToDrawable(item.iconSvg, context.resources)
        } else {
            categoryDropdownLayout.setStartIconDrawable(R.drawable.ic_category)
        }
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

    override fun retryClicks(): Observable<Unit> = retryRelay

    override fun refreshClicks(): Observable<Unit> = refreshRelay

    override fun categorySelectedClicks(): Observable<Int> = categorySelectedRelay

}

private const val DURATION_MEDIUM = 300L
private const val CHILD_CONTENT = 0
private const val CHILD_PLACEHOLDER = 1
private const val CHILD_ERROR = 2
