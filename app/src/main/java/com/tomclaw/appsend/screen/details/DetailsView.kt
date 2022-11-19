package com.tomclaw.appsend.screen.details

import android.annotation.SuppressLint
import android.app.Dialog
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.github.rubensousa.bottomsheetbuilder.BottomSheetBuilder
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.getAttributedColor
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.hideWithAlphaAnimation
import com.tomclaw.appsend.util.show
import com.tomclaw.appsend.util.showWithAlphaAnimation
import io.reactivex.rxjava3.core.Observable

interface DetailsView {

    fun showProgress()

    fun showContent()

    fun contentUpdated()

    fun showVersionsDialog(items: List<VersionItem>)

    fun showSnackbar(text: String)

    fun showMenu(canEdit: Boolean, canUnlink: Boolean, canUnpublish: Boolean, canDelete: Boolean)

    fun hideMenu()

    fun showModeration()

    fun showError()

    fun hideError()

    fun navigationClicks(): Observable<Unit>

    fun swipeRefresh(): Observable<Unit>

    fun editClicks(): Observable<Unit>

    fun unpublishClicks(): Observable<Unit>

    fun unlinkClicks(): Observable<Unit>

    fun deleteClicks(): Observable<Unit>

    fun abuseClicks(): Observable<Unit>

    fun retryClicks(): Observable<Unit>

    fun versionClicks(): Observable<VersionItem>

    fun moderationClicks(): Observable<Boolean>

}

data class VersionItem(
    val versionId: Int,
    val appId: String,
    val title: String,
    val compatible: Boolean,
    val newer: Boolean,
)

class DetailsViewImpl(
    view: View,
    private val preferences: DetailsPreferencesProvider,
    private val adapter: SimpleRecyclerAdapter
) : DetailsView {

    private val context = view.context
    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val swipeRefresh: SwipeRefreshLayout = view.findViewById(R.id.swipe_refresh)
    private val recycler: RecyclerView = view.findViewById(R.id.recycler)
    private val error: View = view.findViewById(R.id.error)
    private val moderation: View = view.findViewById(R.id.moderation_container)
    private val approveButton: View = view.findViewById(R.id.button_approve)
    private val denyButton: View = view.findViewById(R.id.button_deny)
    private val blockingProgress: View = view.findViewById(R.id.blocking_progress)
    private val retryButton: View = view.findViewById(R.id.retry_button)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val refreshRelay = PublishRelay.create<Unit>()
    private val editRelay = PublishRelay.create<Unit>()
    private val unpublishRelay = PublishRelay.create<Unit>()
    private val unlinkRelay = PublishRelay.create<Unit>()
    private val deleteRelay = PublishRelay.create<Unit>()
    private val abuseRelay = PublishRelay.create<Unit>()
    private val retryRelay = PublishRelay.create<Unit>()
    private val versionRelay = PublishRelay.create<VersionItem>()
    private val moderationRelay = PublishRelay.create<Boolean>()

    private val layoutManager: LinearLayoutManager

    init {
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.edit_meta -> editRelay.accept(Unit)
                R.id.unpublish -> unpublishRelay.accept(Unit)
                R.id.unlink -> unlinkRelay.accept(Unit)
                R.id.delete -> deleteRelay.accept(Unit)
                R.id.abuse -> abuseRelay.accept(Unit)
            }
            true
        }

        swipeRefresh.setOnRefreshListener {
            refreshRelay.accept(Unit)
        }

        approveButton.setOnClickListener {
            moderationRelay.accept(true)
        }
        denyButton.setOnClickListener {
            moderationRelay.accept(false)
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

    @SuppressLint("NotifyDataSetChanged")
    override fun contentUpdated() {
        adapter.notifyDataSetChanged()
    }

    override fun showVersionsDialog(items: List<VersionItem>) {
        val theme = R.style.BottomSheetDialogDark.takeIf { preferences.isDarkTheme() }
            ?: R.style.BottomSheetDialogLight
        var bottomSheet: Dialog? = null
        bottomSheet = BottomSheetBuilder(context, theme)
            .setMode(BottomSheetBuilder.MODE_LIST)
            .apply {
                for (item in items) {
                    val icon = if (item.compatible) {
                        if (item.newer) {
                            setIconTintColorResource(R.color.newer_color)
                            setItemTextColorResource(R.color.newer_text_color)
                            R.drawable.ic_new
                        } else {
                            setIconTintColor(getAttributedColor(context, R.attr.menu_icons_tint))
                            setItemTextColor(getAttributedColor(context, R.attr.text_primary_color))
                            R.drawable.ic_download_circle
                        }
                    } else {
                        setIconTintColorResource(R.color.incompatible_color)
                        setItemTextColorResource(R.color.incompatible_text_color)
                        R.drawable.ic_alert_circle
                    }
                    addItem(item.versionId, item.title, icon)
                }
            }
            .setItemClickListener { item ->
                items.find {
                    it.versionId == item.itemId
                }?.let {
                    bottomSheet?.hide()
                    versionRelay.accept(it)
                }
            }
            .createDialog()
            .apply { show() }
    }

    override fun showSnackbar(text: String) {
        Snackbar.make(recycler, text, Snackbar.LENGTH_SHORT).show()
    }

    override fun showMenu(
        canEdit: Boolean,
        canUnlink: Boolean,
        canUnpublish: Boolean,
        canDelete: Boolean
    ) {
        toolbar.menu.clear()
        toolbar.inflateMenu(R.menu.details_menu)
        if (!canEdit) {
            toolbar.menu.removeItem(R.id.edit_meta)
        }
        if (!canUnlink) {
            toolbar.menu.removeItem(R.id.unlink)
        }
        if (!canUnpublish) {
            toolbar.menu.removeItem(R.id.unpublish)
        }
        if (!canDelete) {
            toolbar.menu.removeItem(R.id.delete)
        } else {
            toolbar.menu.removeItem(R.id.abuse)
        }
        toolbar.invalidateMenu()
    }

    override fun hideMenu() {
        toolbar.menu.clear()
        toolbar.invalidateMenu()
    }

    override fun showModeration() {
        moderation.show()
    }

    override fun showError() {
        error.show()
    }

    override fun hideError() {
        error.hide()
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

    override fun swipeRefresh(): Observable<Unit> = refreshRelay

    override fun editClicks(): Observable<Unit> = editRelay

    override fun unpublishClicks(): Observable<Unit> = unpublishRelay

    override fun unlinkClicks(): Observable<Unit> = unlinkRelay

    override fun deleteClicks(): Observable<Unit> = deleteRelay

    override fun abuseClicks(): Observable<Unit> = abuseRelay

    override fun retryClicks(): Observable<Unit> = retryRelay

    override fun versionClicks(): Observable<VersionItem> = versionRelay

    override fun moderationClicks(): Observable<Boolean> = moderationRelay

}

private const val DURATION_MEDIUM = 300L
