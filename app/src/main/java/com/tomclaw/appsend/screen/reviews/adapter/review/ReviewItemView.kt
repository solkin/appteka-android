package com.tomclaw.appsend.screen.reviews.adapter.review

import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.tomclaw.appsend.R
import com.tomclaw.appsend.main.adapter.files.ActionItem
import com.tomclaw.appsend.main.adapter.files.ActionsAdapter
import com.tomclaw.appsend.screen.reviews.ReviewsPreferencesProvider
import com.tomclaw.appsend.util.bind
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

    private val context = view.context
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
        rating.rating = value
    }

    override fun setDate(date: String) {
        dateView.bind(date)
    }

    override fun setReview(text: String?) {
        reviewView.bind(text)
    }

    override fun showProgress() {
        progress.show()
    }

    override fun hideProgress() {
        progress.hide()
    }

    override fun showRatingMenu() {
        menuView.show()
    }

    override fun hideRatingMenu() {
        menuView.hide()
    }

    override fun setOnReviewClickListener(listener: (() -> Unit)?) {
        this.reviewClickListener = listener
    }

    override fun setOnDeleteClickListener(listener: (() -> Unit)?) {
        this.deleteClickListener = listener
    }

    // Correctly placed inside the class as a private function
    private fun showRatingDialog() {
        val bottomSheetDialog = BottomSheetDialog(context)
        val sheetView = View.inflate(context, R.layout.bottom_sheet_actions, null)
        val actionsRecycler: RecyclerView = sheetView.findViewById(R.id.actions_recycler)

        val actions = listOf(
            ActionItem(R.id.menu_details, context.getString(R.string.app_details), R.drawable.ic_info),
            ActionItem(R.id.menu_delete, context.getString(R.string.delete), R.drawable.ic_delete)
        )

        val adapter = ActionsAdapter(actions) { clickedId ->
            bottomSheetDialog.dismiss()
            when (clickedId) {
                R.id.menu_details -> reviewClickListener?.invoke()
                R.id.menu_delete -> deleteClickListener?.invoke()
            }
        }

        actionsRecycler.layoutManager = LinearLayoutManager(context)
        actionsRecycler.adapter = adapter

        bottomSheetDialog.setContentView(sheetView)
        bottomSheetDialog.show()
    }

    override fun onUnbind() {
        reviewClickListener = null
        deleteClickListener = null
    }
}