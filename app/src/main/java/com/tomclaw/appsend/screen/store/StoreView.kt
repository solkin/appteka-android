package com.tomclaw.appsend.screen.store

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.categories.CategoryItem
import com.tomclaw.appsend.util.ActionItem
import com.tomclaw.appsend.util.ActionsAdapter
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

    fun showCategories(items: List<CategoryItem>)

    fun setSelectedCategory(category: CategoryItem?)

    fun stopPullRefreshing()

    fun isPullRefreshing(): Boolean

    fun retryClicks(): Observable<Unit>

    fun refreshClicks(): Observable<Unit>

    fun categoriesButtonClicks(): Observable<Unit>

    fun categorySelectedClicks(): Observable<CategoryItem>

    fun categoryClearedClicks(): Observable<Unit>

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
    private val categoriesButton: View = view.findViewById(R.id.button_categories)
    private val categoryIcon: ImageView = view.findViewById(R.id.category_icon)
    private val categoryTitle: TextView = view.findViewById(R.id.category_title)

    private val retryRelay = PublishRelay.create<Unit>()
    private val refreshRelay = PublishRelay.create<Unit>()
    private val categoriesButtonRelay = PublishRelay.create<Unit>()
    private val categorySelectedRelay = PublishRelay.create<CategoryItem>()
    private val categoryClearedRelay = PublishRelay.create<Unit>()

    init {
        val orientation = RecyclerView.VERTICAL
        val layoutManager = LinearLayoutManager(context, orientation, false)
        adapter.setHasStableIds(true)
        recycler.adapter = adapter
        recycler.layoutManager = layoutManager
        recycler.itemAnimator = DefaultItemAnimator()
        recycler.itemAnimator?.changeDuration = DURATION_MEDIUM

        refresher.setOnRefreshListener { refreshRelay.accept(Unit) }
        categoriesButton.setOnClickListener { categoriesButtonRelay.accept(Unit) }

        retryButton.clicks(retryRelay)
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

    override fun showCategories(items: List<CategoryItem>) {
        val dialog = BottomSheetDialog(context)

        val actionView = View.inflate(context, R.layout.bottom_sheet_actions, null)
        val actionsRecycler: RecyclerView = actionView.findViewById(R.id.actions_recycler)

        val actions = mutableListOf<ActionItem>()

        // "All Categories" item
        actions.add(
            ActionItem(
                id = 0,
                title = context.getString(R.string.all_categories),
                iconRes = R.drawable.ic_category,
                iconSvg = null
            )
        )

        // Category items
        for (item in items) {
            actions.add(
                ActionItem(
                    id = item.id,
                    title = item.title,
                    iconRes = 0,
                    iconSvg = item.icon
                )
            )
        }

        val actionsAdapter = ActionsAdapter(actions) { itemId ->
            dialog.dismiss()
            if (itemId == 0) {
                categoryClearedRelay.accept(Unit)
            } else {
                items.find { it.id == itemId }?.let {
                    categorySelectedRelay.accept(it)
                }
            }
        }

        actionsRecycler.layoutManager = LinearLayoutManager(context)
        actionsRecycler.adapter = actionsAdapter

        dialog.setContentView(actionView)
        dialog.show()
    }

    override fun setSelectedCategory(category: CategoryItem?) {
        category?.let {
            categoryIcon.setImageDrawable(svgToDrawable(it.icon, context.resources))
            categoryTitle.text = it.title
        } ?: run {
            categoryIcon.setImageResource(R.drawable.ic_category)
            categoryTitle.setText(R.string.category_not_defined)
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

    override fun categoriesButtonClicks(): Observable<Unit> = categoriesButtonRelay

    override fun categorySelectedClicks(): Observable<CategoryItem> = categorySelectedRelay

    override fun categoryClearedClicks(): Observable<Unit> = categoryClearedRelay

}

private const val DURATION_MEDIUM = 300L
private const val CHILD_CONTENT = 0
private const val CHILD_PLACEHOLDER = 1
private const val CHILD_ERROR = 2
