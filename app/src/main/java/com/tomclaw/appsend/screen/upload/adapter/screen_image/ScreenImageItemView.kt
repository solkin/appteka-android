package com.tomclaw.appsend.screen.upload.adapter.screen_image

import android.view.View
import android.widget.ImageView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.imageloader.util.centerCrop
import com.tomclaw.imageloader.util.fetch

interface ScreenImageItemView : ItemView {

    fun setImage(item: ScreenImageItem)

    fun setOnClickListener(listener: (() -> Unit)?)

}

class ScreenImageItemViewHolder(view: View) : BaseViewHolder(view), ScreenImageItemView {

    private val card: View = view.findViewById(R.id.screenshot_card)
    private val image: ImageView = view.findViewById(R.id.screenshot)

    private var clickListener: (() -> Unit)? = null

    init {
        card.setOnClickListener { clickListener?.invoke() }
    }

    override fun setImage(item: ScreenImageItem) {
        image.fetch(item.uri.toString()) {
            centerCrop()
            placeholder = {
                with(it.get()) {
                    setImageDrawable(null)
                }
            }
        }
    }

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    override fun onUnbind() {
        this.clickListener = null
    }

}
