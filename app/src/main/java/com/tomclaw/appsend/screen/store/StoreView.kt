package com.tomclaw.appsend.screen.store

import android.annotation.SuppressLint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.caverock.androidsvg.SVG
import com.github.rubensousa.bottomsheetbuilder.BottomSheetBuilder
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.categories.CategoryItem
import com.tomclaw.appsend.util.ColorHelper
import com.tomclaw.appsend.util.clicks
import com.tomclaw.appsend.util.dpToPx
import com.tomclaw.appsend.util.toBitmap
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

    override fun showCategories(items: List<CategoryItem>) {
        val theme = R.style.BottomSheetDialogDark.takeIf { preferences.isDarkTheme() }
            ?: R.style.BottomSheetDialogLight
        BottomSheetBuilder(context, theme)
            .setMode(BottomSheetBuilder.MODE_LIST)
            .setIconTintColor(ColorHelper.getAttributedColor(context, R.attr.menu_icons_tint))
            .setItemTextColor(ColorHelper.getAttributedColor(context, R.attr.text_primary_color))
            .apply {
                addItem(0, R.string.all_categories, R.drawable.ic_category)
            }
            .apply {
                for (item in items) {
                    val title = item.title
                    val icon = svgToDrawable(item.icon)
                    addItem(item.id, title, icon)
                }
            }
            .setItemClickListener { item ->
                val categoryItem = items.find {
                    it.id == item.itemId
                } ?: run {
                    categoryClearedRelay.accept(Unit)
                    return@setItemClickListener
                }
                categorySelectedRelay.accept(categoryItem)
            }
            .createDialog()
            .show()
    }

    private fun svgToDrawable(icon: String): Drawable {
        val picture = SVG.getFromString(icon).renderToPicture()
        val bitmap = picture.toBitmap(
            bitmapWidth = dpToPx(picture.width, context.resources),
            bitmapHeight = dpToPx(picture.height, context.resources)
        )
        return BitmapDrawable(context.resources, bitmap)
    }

    override fun setSelectedCategory(category: CategoryItem?) {
        category?.let {
            categoryIcon.setImageDrawable(svgToDrawable(it.icon))
            categoryTitle.text = it.title
        } ?: run {
            categoryIcon.setImageResource(R.drawable.ic_category)
            categoryTitle.setText(R.string.all_categories)
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
