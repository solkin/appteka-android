package com.tomclaw.appsend.screen.upload

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.INFINITE
import android.animation.ValueAnimator.REVERSE
import android.annotation.SuppressLint
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import com.google.android.material.progressindicator.LinearProgressIndicator
import android.widget.TextView
import android.widget.ViewFlipper
import com.google.android.material.appbar.MaterialToolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.categories.CategoryItem
import com.tomclaw.appsend.main.adapter.files.ActionItem
import com.tomclaw.appsend.main.adapter.files.ActionsAdapter
import com.tomclaw.appsend.screen.upload.adapter.other_versions.VersionItem
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.hideWithAlphaAnimation
import com.tomclaw.appsend.util.show
import com.tomclaw.appsend.util.showWithAlphaAnimation
import com.tomclaw.imageloader.util.centerCrop
import com.tomclaw.imageloader.util.fetch
import com.tomclaw.imageloader.util.withPlaceholder
import io.reactivex.rxjava3.core.Observable

interface UploadView {
    fun showProgress()
    fun showContent()
    fun contentUpdated()
    fun showError()
    fun hideError()
    fun showUnauthorizedError()
    fun showAgreementError()
    fun showCategories(items: List<CategoryItem>)
    fun showVersionsDialog(items: List<VersionItem>)
    fun showUploadDialog()
    fun showUploadProgress()
    fun resetUploadProgress()
    fun setAppIcon(url: String?)
    fun setUploadProgress(value: Int)
    fun scrollToTop()

    fun navigationClicks(): Observable<Unit>
    fun retryClicks(): Observable<Unit>
    fun categorySelectedClicks(): Observable<CategoryItem>
    fun categoryClearedClicks(): Observable<Unit>
    fun versionClicks(): Observable<VersionItem>
    fun cancelClicks(): Observable<Unit>
    fun loginClicks(): Observable<Unit>
    fun pickAppClicks(): Observable<Int>
}

class UploadViewImpl(
    view: View,
    private val preferences: UploadPreferencesProvider,
    private val adapter: SimpleRecyclerAdapter
) : UploadView {

    private val context = view.context
    private val toolbar: MaterialToolbar = view.findViewById(R.id.toolbar)
    private val flipper: ViewFlipper = view.findViewById(R.id.flipper)
    private val recycler: RecyclerView = view.findViewById(R.id.recycler)
    private val error: View = view.findViewById(R.id.error)
    private val progress: View = view.findViewById(R.id.overlay_progress)
    private val retryButton: View = view.findViewById(R.id.retry_button)
    private val uploadProgress: LinearProgressIndicator = view.findViewById(R.id.upload_progress)
    private val uploadPercent: TextView = view.findViewById(R.id.upload_percent)
    private val appIcon: ImageView = view.findViewById(R.id.app_icon)
    private val appIconContainer: View = view.findViewById(R.id.app_icon_container)
    private val cancelButton: View = view.findViewById(R.id.cancel_button)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val retryRelay = PublishRelay.create<Unit>()
    private val categorySelectedRelay = PublishRelay.create<CategoryItem>()
    private val categoryClearedRelay = PublishRelay.create<Unit>()
    private val versionRelay = PublishRelay.create<VersionItem>()
    private val cancelRelay = PublishRelay.create<Unit>()
    private val loginRelay = PublishRelay.create<Unit>()
    private val pickAppRelay = PublishRelay.create<Int>()

    private var bounceAnimator: ValueAnimator? = null
    private var rotationAnimator: ValueAnimator? = null

    init {
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }
        retryButton.setOnClickListener { retryRelay.accept(Unit) }
        cancelButton.setOnClickListener { cancelRelay.accept(Unit) }

        adapter.setHasStableIds(true)
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        recycler.itemAnimator = DefaultItemAnimator().apply { changeDuration = DURATION_MEDIUM }
    }

    override fun showProgress() {
        progress.showWithAlphaAnimation(animateFully = true)
    }

    override fun showContent() {
        flipper.displayedChild = CHILD_CONTENT
        progress.hideWithAlphaAnimation(animateFully = false)
        bounceAnimator?.cancel()
        rotationAnimator?.cancel()
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

    override fun showUnauthorizedError() {
        Snackbar.make(recycler, R.string.authorization_required_message, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.login_button) { loginRelay.accept(Unit) }
            .show()
    }

    override fun showAgreementError() {
        Snackbar.make(recycler, R.string.agree_with_upload_notice, Snackbar.LENGTH_LONG).show()
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

    override fun showVersionsDialog(items: List<VersionItem>) {
        val dialog = BottomSheetDialog(context)
        val sheetView = View.inflate(context, R.layout.bottom_sheet_actions, null)
        val actionsRecycler: RecyclerView = sheetView.findViewById(R.id.actions_recycler)

        val actions = items.map {
            val icon = when {
                !it.compatible -> R.drawable.ic_alert_circle
                it.newer -> R.drawable.ic_new
                else -> R.drawable.ic_git
            }
            ActionItem(it.versionId, it.title, icon)
        }

        actionsRecycler.layoutManager = LinearLayoutManager(context)
        actionsRecycler.adapter = ActionsAdapter(actions) { id ->
            dialog.dismiss()
            items.find { it.versionId == id }?.let { versionRelay.accept(it) }
        }

        dialog.setContentView(sheetView)
        dialog.show()
    }

    override fun showUploadDialog() {
        val dialog = BottomSheetDialog(context)
        val sheetView = View.inflate(context, R.layout.bottom_sheet_actions, null)
        val actionsRecycler: RecyclerView = sheetView.findViewById(R.id.actions_recycler)

        val actions = listOf(
            ActionItem(R.id.menu_pick_apk, context.getString(R.string.pick_apk), R.drawable.ic_file_apk_box),
            ActionItem(R.id.menu_pick_installed, context.getString(R.string.pick_installed), R.drawable.ic_apps_box)
        )

        actionsRecycler.layoutManager = LinearLayoutManager(context)
        actionsRecycler.adapter = ActionsAdapter(actions) { id ->
            dialog.dismiss()
            when (id) {
                R.id.menu_pick_apk -> pickAppRelay.accept(MENU_APK)
                R.id.menu_pick_installed -> pickAppRelay.accept(MENU_INSTALLED)
            }
        }

        dialog.setContentView(sheetView)
        dialog.show()
    }

    override fun showUploadProgress() {
        flipper.displayedChild = CHILD_UPLOAD
        appIconContainer.post {
            val delta = appIconContainer.measuredHeight - appIcon.measuredHeight

            bounceAnimator?.cancel()
            bounceAnimator = ObjectAnimator.ofInt(0, delta).apply {
                addUpdateListener { appIconContainer.setPadding(0, 0, 0, it.animatedValue as Int) }
                duration = 1500
                interpolator = DecelerateInterpolator()
                repeatMode = REVERSE
                repeatCount = INFINITE
                start()
            }

            rotationAnimator?.cancel()
            rotationAnimator = ObjectAnimator.ofFloat(appIcon, "rotation", -360f, 360f).apply {
                duration = 1500
                interpolator = LinearInterpolator()
                repeatMode = REVERSE
                repeatCount = INFINITE
                start()
            }
        }
    }

    override fun resetUploadProgress() {
        showUploadProgress()
        uploadProgress.progress = 0
        uploadPercent.bind("0%")
    }

    override fun setAppIcon(url: String?) {
        appIcon.fetch(url.orEmpty()) {
            centerCrop()
            withPlaceholder(R.drawable.app_placeholder)
            placeholder = {
                with(it.get()) {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImageResource(R.drawable.app_placeholder)
                }
            }
        }
    }

    override fun setUploadProgress(value: Int) {
    if (flipper.displayedChild != CHILD_UPLOAD) showUploadProgress()
    uploadProgress.progress = value
    uploadPercent.bind("$value%")
}

    override fun scrollToTop() {
        recycler.scrollToPosition(0)
    }

    override fun navigationClicks() = navigationRelay
    override fun retryClicks() = retryRelay
    override fun categorySelectedClicks() = categorySelectedRelay
    override fun categoryClearedClicks() = categoryClearedRelay
    override fun versionClicks() = versionRelay
    override fun cancelClicks() = cancelRelay
    override fun loginClicks() = loginRelay
    override fun pickAppClicks() = pickAppRelay
}

private const val DURATION_MEDIUM = 300L
private const val CHILD_CONTENT = 0
private const val CHILD_UPLOAD = 1
internal const val MENU_APK = 1
internal const val MENU_INSTALLED = 2