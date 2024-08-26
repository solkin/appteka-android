package com.tomclaw.appsend.screen.installed

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.annotation.MenuRes
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.github.rubensousa.bottomsheetbuilder.BottomSheetBuilder
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.installed.adapter.app.AppItem
import com.tomclaw.appsend.util.clicks
import com.tomclaw.appsend.util.getAttributedColor
import io.reactivex.rxjava3.core.Observable

interface InstalledView {

    fun showProgress()

    fun showContent()

    fun contentUpdated()

    fun contentUpdated(position: Int)

    fun showPlaceholder()

    fun showError()

    fun showItemDialog(item: AppItem)

    fun stopPullRefreshing()

    fun isPullRefreshing(): Boolean

    fun navigationClicks(): Observable<Unit>

    fun itemMenuClicks(): Observable<Pair<Int, AppItem>>

    fun retryClicks(): Observable<Unit>

    fun refreshClicks(): Observable<Unit>

}

class InstalledViewImpl(
    private val view: View,
    private val preferences: InstalledPreferencesProvider,
    private val adapter: SimpleRecyclerAdapter
) : InstalledView {

    private val context = view.context
    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val refresher: SwipeRefreshLayout = view.findViewById(R.id.swipe_refresh)
    private val flipper: ViewFlipper = view.findViewById(R.id.view_flipper)
    private val recycler: RecyclerView = view.findViewById(R.id.recycler)
    private val error: TextView = view.findViewById(R.id.error_text)
    private val retryButton: View = view.findViewById(R.id.button_retry)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val itemMenuRelay = PublishRelay.create<Pair<Int, AppItem>>()
    private val retryRelay = PublishRelay.create<Unit>()
    private val refreshRelay = PublishRelay.create<Unit>()

    init {
        toolbar.setTitle(R.string.nav_installed)
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }

        val orientation = RecyclerView.VERTICAL
        val layoutManager = LinearLayoutManager(view.context, orientation, false)
        adapter.setHasStableIds(true)
        recycler.adapter = adapter
        recycler.layoutManager = layoutManager
        recycler.itemAnimator = DefaultItemAnimator()
        recycler.itemAnimator?.changeDuration = DURATION_MEDIUM

        refresher.setOnRefreshListener { refreshRelay.accept(Unit) }
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

    override fun showItemDialog(item: AppItem) {
        val theme = R.style.BottomSheetDialogDark.takeIf { preferences.isDarkTheme() }
            ?: R.style.BottomSheetDialogLight
        BottomSheetBuilder(view.context, theme)
            .setMode(BottomSheetBuilder.MODE_LIST)
            .setIconTintColor(getAttributedColor(view.context, R.attr.menu_icons_tint))
            .setItemTextColor(getAttributedColor(view.context, R.attr.text_primary_color))
            .setMenu(R.menu.installed_app_menu)
            .setItemClickListener {
                val id = when (it.itemId) {
                    R.id.menu_run_app -> MENU_RUN
                    R.id.menu_share_apk -> MENU_SHARE
                    R.id.menu_extract_apk -> MENU_EXTRACT
                    R.id.menu_upload_apk -> MENU_UPLOAD
                    R.id.menu_bluetooth_apk -> MENU_BLUETOOTH
                    R.id.menu_find_on_gp -> MENU_FIND_ON_GP
                    R.id.menu_find_on_store -> MENU_FIND_ON_STORE
                    R.id.menu_required_permissions -> MENU_PERMISSIONS
                    R.id.menu_app_details -> MENU_DETAILS
                    R.id.menu_remove_app -> MENU_REMOVE
                    else -> return@setItemClickListener
                }
                itemMenuRelay.accept(Pair(id, item))
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

    override fun navigationClicks(): Observable<Unit> = navigationRelay

    override fun itemMenuClicks(): Observable<Pair<Int, AppItem>> = itemMenuRelay

    override fun retryClicks(): Observable<Unit> = retryRelay

    override fun refreshClicks(): Observable<Unit> = refreshRelay

}

const val MENU_RUN = 1
const val MENU_SHARE = 2
const val MENU_EXTRACT = 3
const val MENU_UPLOAD = 4
const val MENU_BLUETOOTH = 5
const val MENU_FIND_ON_GP = 6
const val MENU_FIND_ON_STORE = 7
const val MENU_PERMISSIONS = 8
const val MENU_DETAILS = 9
const val MENU_REMOVE = 10

private const val DURATION_MEDIUM = 300L
