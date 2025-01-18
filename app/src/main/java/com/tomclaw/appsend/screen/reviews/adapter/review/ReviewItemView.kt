package com.tomclaw.appsend.screen.reviews.adapter.review

import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.annotation.MenuRes
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.github.rubensousa.bottomsheetbuilder.BottomSheetBuilder
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.reviews.ReviewsPreferencesProvider
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.util.getAttributedColor
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.show
import com.tomclaw.imageloader.util.centerCrop
import com.tomclaw.imageloader.util.fetch
import com.tomclaw.imageloader.util.withPlaceholder

interface ReviewItemView : ItemView {

    fun setIcon(url: String?)

    fun setTitle(title: String)

    fun setVersion(version: String)

    fun setRating(value: Float)

    fun setDate(date: String)

    fun setReview(text: String?)

    fun showProgress()

    fun hideProgress()

    fun showRatingMenu()

    fun hideRatingMenu()

    fun setOnReviewClickListener(listener: (() -> Unit)?)

    fun setOnDeleteClickListener(listener: (() -> Unit)?)

}

class ReviewItemViewHolder(
    private val view: View,
    private val preferences: ReviewsPreferencesProvider,
) : BaseViewHolder(view), ReviewItemView {

    private val icon: ImageView = view.findViewById(R.id.app_icon)
    private val title: TextView = view.findViewById(R.id.app_name)
    private val version: TextView = view.findViewById(R.id.app_version)
    private val rating: RatingBar = view.findViewById(R.id.rating_view)
    private val dateView: TextView = view.findViewById(R.id.date_view)
    private val reviewView: TextView = view.findViewById(R.id.review_view)
    private val progress: View = view.findViewById(R.id.item_progress)
    private val menuView: View = view.findViewById(R.id.rating_menu)

    private var reviewClickListener: (() -> Unit)? = null
    private var deleteClickListener: (() -> Unit)? = null

    init {
        view.setOnClickListener { reviewClickListener?.invoke() }
        menuView.setOnClickListener {
            showRatingDialog()
        }
    }

    override fun setIcon(url: String?) {
        icon.fetch(url.orEmpty()) {
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

    override fun setTitle(title: String) {
        this.title.bind(title)
    }

    override fun setVersion(version: String) {
        this.version.bind(version)
    }

    override fun setRating(value: Float) {
        this.rating.rating = value
    }

    override fun setDate(date: String) {
        dateView.bind(date)
    }

    override fun setReview(text: String?) {
        reviewView.bind(text)
    }

    override fun showProgress() {
        progress.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        progress.visibility = View.GONE
    }

    override fun showRatingMenu() {
        menuView.show()
    }

    override fun hideRatingMenu() {
        menuView.hide()
    }

    private fun showRatingDialog() {
        showRatingDialog(R.menu.review_menu)
    }

    private fun showRatingDialog(@MenuRes menuId: Int) {
        val theme = R.style.BottomSheetDialogDark.takeIf { preferences.isDarkTheme() }
            ?: R.style.BottomSheetDialogLight
        BottomSheetBuilder(view.context, theme)
            .setMode(BottomSheetBuilder.MODE_LIST)
            .setIconTintColor(getAttributedColor(view.context, R.attr.menu_icons_tint))
            .setItemTextColor(getAttributedColor(view.context, R.attr.text_primary_color))
            .setMenu(menuId)
            .setItemClickListener {
                when (it.itemId) {
                    R.id.menu_details -> reviewClickListener?.invoke()
                    R.id.menu_delete -> deleteClickListener?.invoke()
                }
            }
            .createDialog()
            .show()
    }

    override fun setOnReviewClickListener(listener: (() -> Unit)?) {
        this.reviewClickListener = listener
    }

    override fun setOnDeleteClickListener(listener: (() -> Unit)?) {
        this.deleteClickListener = listener
    }

    override fun onUnbind() {
        this.reviewClickListener = null
    }

}
