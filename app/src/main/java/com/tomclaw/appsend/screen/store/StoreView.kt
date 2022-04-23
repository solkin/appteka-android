package com.tomclaw.appsend.screen.store

import android.annotation.SuppressLint
import android.graphics.drawable.BitmapDrawable
import android.view.View
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
import com.tomclaw.appsend.categories.Category
import com.tomclaw.appsend.util.ColorHelper
import com.tomclaw.appsend.util.clicks
import com.tomclaw.appsend.util.dpToPx
import com.tomclaw.appsend.util.toBitmap
import io.reactivex.rxjava3.core.Observable
import java.util.Locale

interface StoreView {

    fun showProgress()

    fun showContent()

    fun contentUpdated()

    fun contentUpdated(position: Int)

    fun showPlaceholder()

    fun showError()

    fun showCategories(categories: List<Category>)

    fun stopPullRefreshing()

    fun isPullRefreshing(): Boolean

    fun retryClicks(): Observable<Unit>

    fun refreshClicks(): Observable<Unit>

    fun categoriesClicks(): Observable<Unit>

}

class StoreViewImpl(
    view: View,
    private val preferences: StorePreferencesProvider,
    private val adapter: SimpleRecyclerAdapter,
    private val locale: Locale,
) : StoreView {

    private val context = view.context
    private val refresher: SwipeRefreshLayout = view.findViewById(R.id.swipe_refresh)
    private val flipper: ViewFlipper = view.findViewById(R.id.view_flipper)
    private val recycler: RecyclerView = view.findViewById(R.id.recycler)
    private val error: TextView = view.findViewById(R.id.error_text)
    private val retryButton: View = view.findViewById(R.id.button_retry)
    private val categoriesButton: View = view.findViewById(R.id.button_categories)

    private val retryRelay = PublishRelay.create<Unit>()
    private val refreshRelay = PublishRelay.create<Unit>()
    private val categoriesRelay = PublishRelay.create<Unit>()

    init {
        val orientation = RecyclerView.VERTICAL
        val layoutManager = LinearLayoutManager(context, orientation, false)
        adapter.setHasStableIds(true)
        recycler.adapter = adapter
        recycler.layoutManager = layoutManager
        recycler.itemAnimator = DefaultItemAnimator()
        recycler.itemAnimator?.changeDuration = DURATION_MEDIUM

        refresher.setOnRefreshListener { refreshRelay.accept(Unit) }
        categoriesButton.setOnClickListener { categoriesRelay.accept(Unit) }
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

    override fun showCategories(categories: List<Category>) {
        val theme = R.style.BottomSheetDialogDark.takeIf { preferences.isDarkTheme() }
            ?: R.style.BottomSheetDialogLight
        BottomSheetBuilder(context, theme)
            .setMode(BottomSheetBuilder.MODE_LIST)
            .setIconTintColor(ColorHelper.getAttributedColor(context, R.attr.menu_icons_tint))
            .setItemTextColor(ColorHelper.getAttributedColor(context, R.attr.text_primary_color))
            .apply {
                for (category in categories) {
                    val title = category.name[locale.language] ?: category.name["en"]
                    val picture = SVG.getFromString(category.icon).renderToPicture()
                    val bitmap = picture.toBitmap(
                        bitmapWidth = dpToPx(picture.width, context.resources),
                        bitmapHeight = dpToPx(picture.height, context.resources)
                    )
                    val icon = BitmapDrawable(context.resources, bitmap)
                    addItem(category.id, title, icon)
                }
            }
            .setItemClickListener {

            }
            .createDialog()
            .show()
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

    override fun categoriesClicks(): Observable<Unit> = categoriesRelay

}

private const val DURATION_MEDIUM = 300L
