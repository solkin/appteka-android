package com.tomclaw.appsend.screen.downloads.adapter.app

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tomclaw.appsend.R
import com.tomclaw.appsend.categories.CategoryItem
import com.tomclaw.appsend.util.ActionItem
import com.tomclaw.appsend.util.ActionsAdapter
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.show
import com.tomclaw.appsend.util.svgToDrawable
import com.tomclaw.imageloader.util.fetch

interface AppItemView : ItemView {

    fun setIcon(url: String?)

    fun setTitle(title: String)

    fun setVersion(version: String)

    fun setSize(size: String)

    fun setRating(rating: Float?)

    fun setDownloads(downloads: Int)

    fun showBadge()

    fun hideBadge()

    fun showProgress()

    fun hideProgress()

    fun showError()

    fun hideError()

    fun setStatus(status: String?, success: Boolean)

    fun showOpenSourceBadge()

    fun hideOpenSourceBadge()

    fun setCategory(category: CategoryItem?)

    fun showMenu()

    fun hideMenu()

    fun setOnClickListener(listener: (() -> Unit)?)

    fun setOnRetryListener(listener: (() -> Unit)?)

    fun setOnDeleteClickListener(listener: (() -> Unit)?)

    fun setClickable(clickable: Boolean)

}

class AppItemViewHolder(view: View) : BaseViewHolder(view), AppItemView {

    private val context = view.context
    private val icon: ImageView = view.findViewById(R.id.app_icon)
    private val title: TextView = view.findViewById(R.id.app_name)
    private val version: TextView = view.findViewById(R.id.app_version)
    private val size: TextView = view.findViewById(R.id.app_size)
    private val rating: TextView = view.findViewById(R.id.app_rating)
    private val ratingIcon: View = view.findViewById(R.id.rating_icon)
    private val downloads: TextView = view.findViewById(R.id.app_downloads)
    private val openSource: View = view.findViewById(R.id.open_source)
    private val badge: View = view.findViewById(R.id.badge_new)
    private val progress: View = view.findViewById(R.id.item_progress)
    private val statusContainer: View = view.findViewById(R.id.app_badge)
    private val statusIcon: ImageView = view.findViewById(R.id.app_badge_icon)
    private val statusText: TextView = view.findViewById(R.id.app_badge_text)
    private val categoryTitle: TextView = view.findViewById(R.id.app_category)
    private val categoryIcon: ImageView = view.findViewById(R.id.app_category_icon)
    private val error: View = view.findViewById(R.id.error_view)
    private val retryButton: View = view.findViewById(R.id.button_retry)
    private val menuView: View = view.findViewById(R.id.app_menu)

    private var clickListener: (() -> Unit)? = null
    private var retryListener: (() -> Unit)? = null
    private var deleteClickListener: (() -> Unit)? = null

    init {
        view.setOnClickListener { clickListener?.invoke() }
        retryButton.setOnClickListener { retryListener?.invoke() }
        menuView.setOnClickListener { showAppDialog() }
    }

    private fun showAppDialog() {
        val bottomSheetDialog = BottomSheetDialog(context)
        val sheetView = View.inflate(context, R.layout.bottom_sheet_actions, null)
        val actionsRecycler: RecyclerView = sheetView.findViewById(R.id.actions_recycler)

        val actions = listOf(
            ActionItem(MENU_OPEN, context.getString(R.string.open), R.drawable.ic_info),
            ActionItem(MENU_DELETE, context.getString(R.string.delete), R.drawable.ic_delete)
        )

        val actionsAdapter = ActionsAdapter(actions) { actionId ->
            bottomSheetDialog.dismiss()
            when (actionId) {
                MENU_OPEN -> clickListener?.invoke()
                MENU_DELETE -> deleteClickListener?.invoke()
            }
        }

        actionsRecycler.layoutManager = LinearLayoutManager(context)
        actionsRecycler.adapter = actionsAdapter

        bottomSheetDialog.setContentView(sheetView)
        bottomSheetDialog.show()
    }

    override fun setIcon(url: String?) {
        icon.fetch(url.orEmpty()) {
            centerCrop()
            placeholder(R.drawable.app_placeholder)
            onLoading { imageView ->
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                imageView.setImageResource(R.drawable.app_placeholder)
            }
        }
    }

    override fun showProgress() {
        progress.visibility = VISIBLE
    }

    override fun hideProgress() {
        progress.visibility = GONE
    }

    override fun showError() {
        error.visibility = VISIBLE
    }

    override fun hideError() {
        error.visibility = GONE
    }

    override fun setTitle(title: String) {
        this.title.bind(title)
    }

    override fun setVersion(version: String) {
        this.version.bind(version)
    }

    override fun setSize(size: String) {
        this.size.bind(size)
    }

    override fun setRating(rating: Float?) {
        this.rating.bind(rating?.toString())
        rating?.let { this.ratingIcon.show() } ?: this.ratingIcon.hide()
    }

    override fun setDownloads(downloads: Int) {
        this.downloads.bind(downloads.toString())
    }

    override fun showBadge() {
        badge.show()
    }

    override fun hideBadge() {
        badge.hide()
    }

    override fun setStatus(status: String?, isPublished: Boolean) {
        this.statusText.bind(status)
        this.statusIcon.setImageResource(
            if (isPublished) R.drawable.ic_pill_ok else R.drawable.ic_pill_fail
        )
        this.statusContainer.visibility = statusText.visibility
    }

    override fun showOpenSourceBadge() {
        this.openSource.show()
    }

    override fun hideOpenSourceBadge() {
        this.openSource.hide()
    }

    override fun setCategory(category: CategoryItem?) {
        category?.let {
            categoryIcon.setImageDrawable(svgToDrawable(it.icon, context.resources))
            categoryTitle.text = it.title
        } ?: run {
            categoryIcon.setImageDrawable(null)
            categoryTitle.setText(R.string.category_not_set)
        }
    }

    override fun showMenu() {
        menuView.show()
    }

    override fun hideMenu() {
        menuView.hide()
    }

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    override fun setClickable(clickable: Boolean) {
        itemView.isClickable = clickable
    }

    override fun setOnRetryListener(listener: (() -> Unit)?) {
        this.retryListener = listener
    }

    override fun setOnDeleteClickListener(listener: (() -> Unit)?) {
        this.deleteClickListener = listener
    }

    override fun onUnbind() {
        this.clickListener = null
        this.retryListener = null
        this.deleteClickListener = null
    }

}

private const val MENU_OPEN = 1
private const val MENU_DELETE = 2
