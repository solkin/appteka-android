package com.tomclaw.appsend.screen.profile.adapter.app

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.show
import com.tomclaw.imageloader.util.fetch

interface AppItemView : ItemView {

    fun setIcon(url: String?)

    fun setTitle(title: String)

    fun setRating(rating: Float?)

    fun setOnClickListener(listener: (() -> Unit)?)

}

class AppItemViewHolder(view: View) : BaseViewHolder(view), AppItemView {

    private val icon: ImageView = view.findViewById(R.id.app_icon)
    private val title: TextView = view.findViewById(R.id.app_name)
    private val rating: TextView = view.findViewById(R.id.app_rating)
    private val ratingIcon: View = view.findViewById(R.id.rating_icon)

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

    override fun setTitle(title: String) {
        this.title.bind(title)
    }

    override fun setRating(rating: Float?) {
        this.rating.bind(rating?.toString())
        rating?.let { this.ratingIcon.show() } ?: this.ratingIcon.hide()
    }

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    override fun onUnbind() {
        this.clickListener = null
    }

}
