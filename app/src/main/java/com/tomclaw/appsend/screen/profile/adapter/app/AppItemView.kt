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
import com.tomclaw.imageloader.util.centerCrop
import com.tomclaw.imageloader.util.fetch
import com.tomclaw.imageloader.util.withPlaceholder

interface AppItemView : ItemView {

    fun setIcon(url: String?)

    fun setTitle(title: String)

    fun setRating(rating: Float?)

    fun setOnClickListener(listener: (() -> Unit)?)

    fun setClickable(clickable: Boolean)

}

class AppItemViewHolder(view: View) : BaseViewHolder(view), AppItemView {

    private val context = view.context
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

    override fun setRating(rating: Float?) {
        this.rating.bind(rating?.toString())
        rating?.let { this.ratingIcon.show() } ?: this.ratingIcon.hide()
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
