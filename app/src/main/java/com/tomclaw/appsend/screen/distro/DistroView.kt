package com.tomclaw.appsend.screen.distro

import android.annotation.SuppressLint
import android.text.Html
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.distro.adapter.apk.ApkItem
import com.tomclaw.appsend.util.ActionItem
import com.tomclaw.appsend.util.ActionsAdapter
import com.tomclaw.appsend.util.clicks
import com.tomclaw.appsend.util.hideWithAlphaAnimation
import com.tomclaw.appsend.util.showWithAlphaAnimation
import io.reactivex.rxjava3.core.Observable


interface DistroView {

    fun showProgress()

    fun showContent()

    fun contentUpdated()

    fun contentUpdated(position: Int)

    fun showPlaceholder()

    fun showError()

    fun showExtractSuccess(path: String)

    fun showExtractError()

    fun showItemDialog(item: ApkItem)

    fun showSnackbar(text: String)

    fun stopPullRefreshing()

    fun isPullRefreshing(): Boolean

    fun navigationClicks(): Observable<Unit>

    fun itemMenuClicks(): Observable<Pair<Int, ApkItem>>

    fun searchTextChanged(): Observable<String>

    fun shareExtractedClicks(): Observable<String>

    fun retryClicks(): Observable<Unit>

    fun refreshClicks(): Observable<Unit>

}

class DistroViewImpl(
    private val view: View,
    private val preferences: DistroPreferencesProvider,
    private val adapter: SimpleRecyclerAdapter
) : DistroView {

    private val context = view.context
    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val refresher: SwipeRefreshLayout = view.findViewById(R.id.swipe_refresh)
    private val flipper: ViewFlipper = view.findViewById(R.id.view_flipper)
    private val recycler: RecyclerView = view.findViewById(R.id.recycler)
    private val overlayProgress: View = view.findViewById(R.id.overlay_progress)
    private val error: TextView = view.findViewById(R.id.error_text)
    private val retryButton: View = view.findViewById(R.id.button_retry)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val itemMenuRelay = PublishRelay.create<Pair<Int, ApkItem>>()
    private val searchTextRelay = PublishRelay.create<String>()
    private val shareExtractedRelay = PublishRelay.create<String>()
    private val retryRelay = PublishRelay.create<Unit>()
    private val refreshRelay = PublishRelay.create<Unit>()

    init {
        toolbar.setTitle(R.string.nav_downloaded)
        toolbar.inflateMenu(R.menu.distro_menu)
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }
        val searchItem: MenuItem = toolbar.menu.findItem(R.id.menu_search)
        val searchView: SearchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchTextRelay.accept(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchTextRelay.accept(newText)
                return true
            }
        })

        val orientation = RecyclerView.VERTICAL
        val layoutManager = LinearLayoutManager(view.context, orientation, false)
        adapter.setHasStableIds(true)
        recycler.adapter = adapter
        recycler.layoutManager = layoutManager
        recycler.itemAnimator = DefaultItemAnimator()
        recycler.itemAnimator?.changeDuration = DURATION_MEDIUM

        refresher.setOnRefreshListener { refreshRelay.accept(Unit) }

        retryButton.clicks(retryRelay)
    }

    override fun showProgress() {
        refresher.isEnabled = false
        flipper.displayedChild = 0
        overlayProgress.showWithAlphaAnimation(animateFully = true)
    }

    override fun showContent() {
        refresher.isEnabled = true
        flipper.displayedChild = 0
        overlayProgress.hideWithAlphaAnimation(animateFully = false)
    }

    override fun showPlaceholder() {
        refresher.isRefreshing = false
        refresher.isEnabled = true
        flipper.displayedChild = 1
    }

    override fun showError() {
        refresher.isEnabled = true
        flipper.displayedChild = 2

        error.setText(R.string.load_files_error)
    }

    override fun showExtractSuccess(path: String) {
        val alertDialog = AlertDialog.Builder(context)
            .setTitle(R.string.success)
            .setMessage(
                Html.fromHtml(context.getString(R.string.app_extract_success, path))
            )
            .setPositiveButton(
                R.string.yes
            ) { _, _ -> shareExtractedRelay.accept(path) }
            .setNegativeButton(R.string.no, null)
            .create()
        alertDialog.show()
    }

    override fun showExtractError() {
        showSnackbar(context.resources.getString(R.string.app_extract_failed))
    }

    override fun showItemDialog(item: ApkItem) {
        val bottomSheetDialog = BottomSheetDialog(context)
        val sheetView = View.inflate(context, R.layout.bottom_sheet_actions, null)
        val actionsRecycler: RecyclerView = sheetView.findViewById(R.id.actions_recycler)

        val actions = listOf(
            ActionItem(MENU_INSTALL, context.getString(R.string.install_app), R.drawable.ic_install),
            ActionItem(MENU_SHARE, context.getString(R.string.share_apk), R.drawable.ic_share),
            ActionItem(MENU_UPLOAD, context.getString(R.string.upload_apk), R.drawable.ic_cloud_upload),
            ActionItem(MENU_BLUETOOTH, context.getString(R.string.bluetooth_apk), R.drawable.ic_bluetooth),
            ActionItem(MENU_FIND_ON_GP, context.getString(R.string.find_on_gp), R.drawable.ic_google_play),
            ActionItem(MENU_FIND_ON_STORE, context.getString(R.string.find_on_store), R.drawable.ic_store),
            ActionItem(MENU_PERMISSIONS, context.getString(R.string.required_permissions), R.drawable.ic_lock_open),
            ActionItem(MENU_REMOVE, context.getString(R.string.remove_app), R.drawable.ic_delete)
        )

        val actionsAdapter = ActionsAdapter(actions) { actionId ->
            bottomSheetDialog.dismiss()
            itemMenuRelay.accept(Pair(actionId, item))
        }

        actionsRecycler.layoutManager = LinearLayoutManager(context)
        actionsRecycler.adapter = actionsAdapter

        bottomSheetDialog.setContentView(sheetView)
        bottomSheetDialog.show()
    }

    override fun showSnackbar(text: String) {
        Snackbar.make(recycler, text, Snackbar.LENGTH_SHORT).show()
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

    override fun itemMenuClicks(): Observable<Pair<Int, ApkItem>> = itemMenuRelay

    override fun searchTextChanged(): Observable<String> = searchTextRelay

    override fun shareExtractedClicks(): Observable<String> = shareExtractedRelay

    override fun retryClicks(): Observable<Unit> = retryRelay

    override fun refreshClicks(): Observable<Unit> = refreshRelay

}

const val MENU_INSTALL = 1
const val MENU_SHARE = 2
const val MENU_UPLOAD = 3
const val MENU_BLUETOOTH = 4
const val MENU_FIND_ON_GP = 5
const val MENU_FIND_ON_STORE = 6
const val MENU_PERMISSIONS = 7
const val MENU_REMOVE = 8

private const val DURATION_MEDIUM = 300L
