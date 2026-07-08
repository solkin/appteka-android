package com.tomclaw.appsend.screen.store

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.ActionItem
import com.tomclaw.appsend.util.ActionsAdapter
import com.tomclaw.appsend.util.adapter.SimpleRecyclerAdapter
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

    fun setFilters(openSource: Boolean, exclusive: Boolean)

    fun scrollToTop()

    fun stopPullRefreshing()

    fun isPullRefreshing(): Boolean

    fun retryClicks(): Observable<Unit>

    fun refreshClicks(): Observable<Unit>

    fun categorySelectedClicks(): Observable<Int>

    fun openSourceClicks(): Observable<Boolean>

    fun exclusiveClicks(): Observable<Boolean>

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
    private val categoryChip: Chip = view.findViewById(R.id.chip_category)
    private val openSourceChip: Chip = view.findViewById(R.id.chip_open_source)
    private val exclusiveChip: Chip = view.findViewById(R.id.chip_exclusive)

    private val retryRelay = PublishRelay.create<Unit>()
    private val refreshRelay = PublishRelay.create<Unit>()
    private val categorySelectedRelay = PublishRelay.create<Int>()
    private val openSourceRelay = PublishRelay.create<Boolean>()
    private val exclusiveRelay = PublishRelay.create<Boolean>()

    private var categories: List<CategoryDropdownItem> = emptyList()

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

        categoryChip.setOnClickListener { showCategoryPicker() }
        // Filter chips are checkable, so a click has already toggled the
        // state by the time we read it; emit the new value.
        openSourceChip.setOnClickListener { openSourceRelay.accept(openSourceChip.isChecked) }
        exclusiveChip.setOnClickListener { exclusiveRelay.accept(exclusiveChip.isChecked) }
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
        categories = items
    }

    override fun setSelectedCategory(item: CategoryDropdownItem) {
        categoryChip.text = item.title
        when {
            item.iconSvg != null -> categoryChip.chipIcon = svgToDrawable(item.iconSvg, context.resources)
            item.iconRes != 0 -> categoryChip.setChipIconResource(item.iconRes)
            else -> categoryChip.setChipIconResource(R.drawable.ic_category)
        }
    }

    override fun setFilters(openSource: Boolean, exclusive: Boolean) {
        // Programmatic; does not fire the click listeners above.
        openSourceChip.isChecked = openSource
        exclusiveChip.isChecked = exclusive
    }

    private fun showCategoryPicker() {
        if (categories.isEmpty()) return
        val dialog = BottomSheetDialog(context)
        val sheet = View.inflate(context, R.layout.bottom_sheet_actions, null)
        val recycler: RecyclerView = sheet.findViewById(R.id.actions_recycler)
        val actions = categories.map { ActionItem(it.id, it.title, it.iconRes, it.iconSvg) }
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = ActionsAdapter(actions) { categoryId ->
            dialog.dismiss()
            categorySelectedRelay.accept(categoryId)
        }
        dialog.setContentView(sheet)
        dialog.show()
    }

    override fun scrollToTop() {
        recycler.scrollToPosition(0)
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

    override fun openSourceClicks(): Observable<Boolean> = openSourceRelay

    override fun exclusiveClicks(): Observable<Boolean> = exclusiveRelay

}

private const val DURATION_MEDIUM = 300L
private const val CHILD_CONTENT = 0
private const val CHILD_PLACEHOLDER = 1
private const val CHILD_ERROR = 2
