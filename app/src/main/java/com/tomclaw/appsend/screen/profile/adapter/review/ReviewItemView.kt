package com.tomclaw.appsend.screen.profile.adapter.review

import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.bind
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

    fun setOnClickListener(listener: (() -> Unit)?)

}

class ReviewItemViewHolder(view: View) : BaseViewHolder(view), ReviewItemView {

    private val icon: ImageView = view.findViewById(R.id.app_icon)
    private val title: TextView = view.findViewById(R.id.app_name)
    private val version: TextView = view.findViewById(R.id.app_version)
    private val rating: RatingBar = view.findViewById(R.id.rating_view)
    private val dateView: TextView = view.findViewById(R.id.date_view)
    private val reviewView: TextView = view.findViewById(R.id.review_view)

    private var clickListener: (() -> Unit)? = null

    init {
        view.setOnClickListener { clickListener?.invoke() }
    }

    override fun setIcon(url: String?) {
        icon.fetch(url.orEmpty()) {
            centerCrop()
            withPlaceholder(R.drawable.app_placeholder)
            placeholderHandler {
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

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    override fun onUnbind() {
        this.clickListener = null
    }

}
