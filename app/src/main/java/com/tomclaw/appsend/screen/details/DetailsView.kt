package com.tomclaw.appsend.screen.details

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.details.adapter.play.PlaySecurityStatus
import com.tomclaw.appsend.util.ActionItem
import com.tomclaw.appsend.util.ActionsAdapter
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

    fun showSecurityInfoDialog(status: PlaySecurityStatus, score: Int?)

    fun showSnackbar(text: String)

    fun showMenu(
        isFavorite: Boolean,
        canEdit: Boolean,
        canUnlink: Boolean,
        canUnpublish: Boolean,
        canDelete: Boolean
    )

    fun hideMenu()

    fun showModeration()

    fun showError()

    fun hideError()

    fun showUnauthorizedError()

    fun showDeletionDialog()

    fun showSecurityWarningDialog(title: String, message: String, downloadButton: String)

    fun navigationClicks(): Observable<Unit>

    fun swipeRefresh(): Observable<Unit>

    fun shareClicks(): Observable<Unit>

    fun editClicks(): Observable<Unit>

    fun unpublishClicks(): Observable<Unit>

    fun unlinkClicks(): Observable<Unit>

    fun deleteClicks(): Observable<Boolean>

    fun abuseClicks(): Observable<Unit>

    fun retryClicks(): Observable<Unit>

    fun versionClicks(): Observable<VersionItem>

    fun moderationClicks(): Observable<Boolean>

    fun favoriteClicks(): Observable<Boolean>

    fun loginClicks(): Observable<Unit>

    fun securityDownloadConfirmClicks(): Observable<Unit>

    fun onDismiss()

}

data class VersionItem(
    val versionId: Int,
    val appId: String,
    val title: String,
    val compatible: Boolean,
    val newer: Boolean,
)

class DetailsViewImpl(
    private val context: Context,
    view: View,
    private val preferences: DetailsPreferencesProvider,
    private val adapter: SimpleRecyclerAdapter
) : DetailsView {

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
    private val shareRelay = PublishRelay.create<Unit>()
    private val editRelay = PublishRelay.create<Unit>()
    private val unpublishRelay = PublishRelay.create<Unit>()
    private val unlinkRelay = PublishRelay.create<Unit>()
    private val deleteRelay = PublishRelay.create<Boolean>()
    private val abuseRelay = PublishRelay.create<Unit>()
    private val retryRelay = PublishRelay.create<Unit>()
    private val versionRelay = PublishRelay.create<VersionItem>()
    private val moderationRelay = PublishRelay.create<Boolean>()
    private val favoriteRelay = PublishRelay.create<Boolean>()
    private val loginRelay = PublishRelay.create<Unit>()
    private val securityDownloadConfirmRelay = PublishRelay.create<Unit>()

    private val layoutManager: LinearLayoutManager

    private var dialog: Dialog? = null

    init {
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.share -> shareRelay.accept(Unit)
                R.id.edit_meta -> editRelay.accept(Unit)
                R.id.unpublish -> unpublishRelay.accept(Unit)
                R.id.unlink -> unlinkRelay.accept(Unit)
                R.id.delete -> deleteRelay.accept(false)
                R.id.abuse -> abuseRelay.accept(Unit)
                R.id.mark_favorite -> favoriteRelay.accept(true)
                R.id.unmark_favorite -> favoriteRelay.accept(false)
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
        val bottomSheetDialog = BottomSheetDialog(context)
        val sheetView = View.inflate(context, R.layout.bottom_sheet_actions, null)
        val actionsRecycler: RecyclerView = sheetView.findViewById(R.id.actions_recycler)

        val actions = items.map { item ->
            val icon = if (item.compatible) {
                if (item.newer) R.drawable.ic_new else R.drawable.ic_download_circle
            } else {
                R.drawable.ic_alert_circle
            }
            ActionItem(item.versionId, item.title, icon)
        }

        val actionsAdapter = ActionsAdapter(actions) { actionId ->
            bottomSheetDialog.dismiss()
            items.find { it.versionId == actionId }?.let {
                versionRelay.accept(it)
            }
        }

        actionsRecycler.layoutManager = LinearLayoutManager(context)
        actionsRecycler.adapter = actionsAdapter

        bottomSheetDialog.setContentView(sheetView)
        bottomSheetDialog.show()
    }

    override fun showSecurityInfoDialog(status: PlaySecurityStatus, score: Int?) {
        val isScanning = status == PlaySecurityStatus.SCANNING

        val (statusText, statusIcon, statusColor) = when (status) {
            PlaySecurityStatus.SCANNING -> Triple(
                context.getString(R.string.security_info_scanning),
                R.drawable.ic_timer_sand,
                R.color.block_info_color
            )
            PlaySecurityStatus.SAFE -> Triple(
                context.getString(R.string.security_info_safe),
                R.drawable.ic_verified,
                R.color.block_success_color
            )
            PlaySecurityStatus.SUSPICIOUS -> Triple(
                context.getString(R.string.security_info_suspicious),
                R.drawable.ic_warning,
                R.color.block_warning_color
            )
            PlaySecurityStatus.MALWARE -> Triple(
                context.getString(R.string.security_info_malware),
                R.drawable.ic_virus,
                R.color.block_error_color
            )
            else -> Triple(
                context.getString(R.string.security_info_unknown),
                R.drawable.ic_security,
                R.color.block_warning_color
            )
        }

        val bottomSheet = BottomSheetDialog(context)
        val sheetView = android.view.LayoutInflater.from(context)
            .inflate(R.layout.bottom_sheet_security_info, null)

        val statusIconView = sheetView.findViewById<android.widget.ImageView>(R.id.security_status_icon)
        val statusTextView = sheetView.findViewById<android.widget.TextView>(R.id.security_status_text)
        val scoreRow = sheetView.findViewById<View>(R.id.security_score_row)
        val scoreTextView = sheetView.findViewById<android.widget.TextView>(R.id.security_score_text)
        val toolsRow = sheetView.findViewById<View>(R.id.security_tools_row)
        val toolsTextView = sheetView.findViewById<android.widget.TextView>(R.id.security_tools_text)

        statusIconView.setImageResource(statusIcon)
        statusIconView.setColorFilter(ContextCompat.getColor(context, statusColor))
        statusTextView.text = statusText

        if (isScanning) {
            scoreRow.visibility = View.VISIBLE
            scoreTextView.text = context.getString(R.string.security_info_scanning_duration)
            toolsRow.visibility = View.GONE
        } else {
            score?.let {
                scoreRow.visibility = View.VISIBLE
                scoreTextView.text = context.getString(R.string.security_info_score, it)
            } ?: run {
                scoreRow.visibility = View.GONE
            }
            toolsRow.visibility = View.VISIBLE
            toolsTextView.text = context.getString(R.string.security_info_tools)
        }

        bottomSheet.setContentView(sheetView)
        bottomSheet.show()
    }

    override fun showSnackbar(text: String) {
        Snackbar.make(recycler, text, Snackbar.LENGTH_SHORT).show()
    }

    override fun showMenu(
        isFavorite: Boolean,
        canEdit: Boolean,
        canUnlink: Boolean,
        canUnpublish: Boolean,
        canDelete: Boolean
    ) {
        toolbar.menu.clear()
        toolbar.inflateMenu(R.menu.details_menu)
        if (isFavorite) {
            toolbar.menu.removeItem(R.id.mark_favorite)
        } else {
            toolbar.menu.removeItem(R.id.unmark_favorite)
        }
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

    override fun showUnauthorizedError() {
        Snackbar
            .make(recycler, R.string.authorization_required_message, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.login_button) {
                loginRelay.accept(Unit)
            }
            .show()
    }

    override fun showDeletionDialog() {
        dialog?.dismiss()
        dialog = MaterialAlertDialogBuilder(context)
            .setTitle(context.resources.getString(R.string.delete_app_title))
            .setMessage(context.resources.getString(R.string.delete_app_message))
            .setNegativeButton(R.string.yes) { _, _ ->
                deleteRelay.accept(true)
            }
            .setPositiveButton(R.string.no, null)
            .create()
        dialog?.show()
    }

    override fun showSecurityWarningDialog(title: String, message: String, downloadButton: String) {
        dialog?.dismiss()
        dialog = MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setNegativeButton(downloadButton) { _, _ ->
                securityDownloadConfirmRelay.accept(Unit)
            }
            .setPositiveButton(R.string.cancel, null)
            .create()
        dialog?.show()
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

    override fun swipeRefresh(): Observable<Unit> = refreshRelay

    override fun shareClicks(): Observable<Unit> = shareRelay

    override fun editClicks(): Observable<Unit> = editRelay

    override fun unpublishClicks(): Observable<Unit> = unpublishRelay

    override fun unlinkClicks(): Observable<Unit> = unlinkRelay

    override fun deleteClicks(): Observable<Boolean> = deleteRelay

    override fun abuseClicks(): Observable<Unit> = abuseRelay

    override fun retryClicks(): Observable<Unit> = retryRelay

    override fun versionClicks(): Observable<VersionItem> = versionRelay

    override fun moderationClicks(): Observable<Boolean> = moderationRelay

    override fun favoriteClicks(): Observable<Boolean> = favoriteRelay

    override fun loginClicks(): Observable<Unit> = loginRelay

    override fun securityDownloadConfirmClicks(): Observable<Unit> = securityDownloadConfirmRelay

    override fun onDismiss() {
        dialog?.dismiss()
    }

}

private const val DURATION_MEDIUM = 300L
