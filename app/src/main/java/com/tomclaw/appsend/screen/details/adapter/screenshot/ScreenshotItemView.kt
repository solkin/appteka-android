package com.tomclaw.appsend.screen.details.adapter.screenshot

import android.view.View
import android.widget.ImageView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.imageloader.util.centerCrop
import com.tomclaw.imageloader.util.fetch

interface ScreenshotItemView : ItemView {

    fun setImage(item: ScreenshotItem)

    fun setOnClickListener(listener: (() -> Unit)?)

}

class ScreenshotItemViewHolder(view: View) : BaseViewHolder(view), ScreenshotItemView {

    private val card: View = view.findViewById(R.id.screenshot_card)
    private val image: ImageView = view.findViewById(R.id.screenshot)

    private var clickListener: (() -> Unit)? = null

    init {
        card.setOnClickListener { clickListener?.invoke() }
    }

    override fun setImage(item: ScreenshotItem) {
        val aspectRatio = item.width.toFloat() / item.height.toFloat()
        val width = image.layoutParams.height * aspectRatio
        image.layoutParams.width = width.toInt()

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
