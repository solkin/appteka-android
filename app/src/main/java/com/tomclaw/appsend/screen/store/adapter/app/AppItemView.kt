package com.tomclaw.appsend.screen.store.adapter.app

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import com.tomclaw.appsend.util.adapter.BaseItemViewHolder
import com.tomclaw.appsend.util.adapter.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.categories.CategoryItem
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

    fun setStatus(status: String?, success: Boolean)

    fun showOpenSourceBadge()

    fun hideOpenSourceBadge()

    fun showAbiIncompatibleBadge()

    fun hideAbiIncompatibleBadge()

    fun setCategory(category: CategoryItem?)

    fun setOnClickListener(listener: (() -> Unit)?)

    fun setClickable(clickable: Boolean)

}

class AppItemViewHolder(view: View) : BaseItemViewHolder(view), AppItemView {

    private val context = view.context
    private val icon: ImageView = view.findViewById(R.id.app_icon)
    private val title: TextView = view.findViewById(R.id.app_name)
    private val version: TextView = view.findViewById(R.id.app_version)
    private val size: TextView = view.findViewById(R.id.app_size)
    private val rating: TextView = view.findViewById(R.id.app_rating)
    private val ratingIcon: View = view.findViewById(R.id.rating_icon)
    private val downloads: TextView = view.findViewById(R.id.app_downloads)
    private val openSource: View = view.findViewById(R.id.open_source)
    private val abiIncompatible: View = view.findViewById(R.id.abi_incompatible)
    private val badge: View = view.findViewById(R.id.badge_new)
    private val progress: View = view.findViewById(R.id.item_progress)
    private val statusContainer: View = view.findViewById(R.id.app_badge)
    private val statusIcon: ImageView = view.findViewById(R.id.app_badge_icon)
    private val statusText: TextView = view.findViewById(R.id.app_badge_text)
    private val categoryTitle: TextView = view.findViewById(R.id.app_category)
    private val categoryIcon: ImageView = view.findViewById(R.id.app_category_icon)

    private var clickListener: (() -> Unit)? = null

    init {
        view.setOnClickListener { clickListener?.invoke() }
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

    override fun showAbiIncompatibleBadge() {
        this.abiIncompatible.show()
    }

    override fun hideAbiIncompatibleBadge() {
        this.abiIncompatible.hide()
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

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    override fun setClickable(clickable: Boolean) {
        itemView.isClickable = clickable
    }

    override fun onUnbind() {
        this.clickListener = null
    }

}
