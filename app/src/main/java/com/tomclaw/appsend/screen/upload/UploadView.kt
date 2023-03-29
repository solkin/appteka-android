package com.tomclaw.appsend.screen.upload

import android.annotation.SuppressLint
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.github.rubensousa.bottomsheetbuilder.BottomSheetBuilder
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.categories.CategoryItem
import com.tomclaw.appsend.util.getAttributedColor
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.hideWithAlphaAnimation
import com.tomclaw.appsend.util.show
import com.tomclaw.appsend.util.showWithAlphaAnimation
import com.tomclaw.appsend.util.svgToDrawable
import io.reactivex.rxjava3.core.Observable

interface UploadView {

    fun showProgress()

    fun showContent()

    fun contentUpdated()

    fun showError()

    fun hideError()

    fun showCategories(items: List<CategoryItem>)

    fun navigationClicks(): Observable<Unit>

    fun retryClicks(): Observable<Unit>

    fun categorySelectedClicks(): Observable<CategoryItem>

    fun categoryClearedClicks(): Observable<Unit>

}

class UploadViewImpl(
    view: View,
    private val preferences: UploadPreferencesProvider,
    private val adapter: SimpleRecyclerAdapter
) : UploadView {

    private val context = view.context
    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val recycler: RecyclerView = view.findViewById(R.id.recycler)
    private val error: View = view.findViewById(R.id.error)
    private val progress: View = view.findViewById(R.id.overlay_progress)
    private val retryButton: View = view.findViewById(R.id.retry_button)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val retryRelay = PublishRelay.create<Unit>()
    private val categorySelectedRelay = PublishRelay.create<CategoryItem>()
    private val categoryClearedRelay = PublishRelay.create<Unit>()

    private val layoutManager: LinearLayoutManager

    init {
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }
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
        progress.showWithAlphaAnimation(animateFully = true)
    }

    override fun showContent() {
        progress.hideWithAlphaAnimation(animateFully = false)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun contentUpdated() {
        adapter.notifyDataSetChanged()
    }

    override fun showError() {
        error.show()
    }

    override fun hideError() {
        error.hide()
    }

    override fun showCategories(items: List<CategoryItem>) {
        val theme = R.style.BottomSheetDialogDark.takeIf { preferences.isDarkTheme() }
            ?: R.style.BottomSheetDialogLight
        BottomSheetBuilder(context, theme)
            .setMode(BottomSheetBuilder.MODE_LIST)
            .setIconTintColor(getAttributedColor(context, R.attr.menu_icons_tint))
            .setItemTextColor(getAttributedColor(context, R.attr.text_primary_color))
            .apply {
                addItem(0, R.string.category_not_defined, R.drawable.ic_category)
            }
            .apply {
                for (item in items) {
                    val title = item.title
                    val icon = svgToDrawable(item.icon, context.resources)
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

    override fun navigationClicks(): Observable<Unit> = navigationRelay

    override fun retryClicks(): Observable<Unit> = retryRelay

    override fun categorySelectedClicks(): Observable<CategoryItem> = categorySelectedRelay

    override fun categoryClearedClicks(): Observable<Unit> = categoryClearedRelay

}

private const val DURATION_MEDIUM = 300L
