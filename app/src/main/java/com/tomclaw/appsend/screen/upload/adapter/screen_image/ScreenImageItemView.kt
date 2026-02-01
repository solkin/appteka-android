package com.tomclaw.appsend.screen.upload.adapter.screen_image

import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import com.tomclaw.appsend.util.adapter.BaseItemViewHolder
import com.tomclaw.appsend.util.adapter.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.imageloader.util.fetch

interface ScreenImageItemView : ItemView {

    fun setImage(item: ScreenImageItem)

    fun setRemote(remote: Boolean)

    fun setOnClickListener(listener: (() -> Unit)?)

    fun setOnDeleteListener(listener: (() -> Unit)?)

}

class ScreenImageItemViewHolder(view: View) : BaseItemViewHolder(view), ScreenImageItemView {

    private val card: View = view.findViewById(R.id.screenshot_card)
    private val image: ImageView = view.findViewById(R.id.screenshot)
    private val upload: View = view.findViewById(R.id.upload)
    private val deleteButton: View = view.findViewById(R.id.delete_button)

    private var clickListener: (() -> Unit)? = null
    private var deleteListener: (() -> Unit)? = null

    init {
        card.setOnClickListener { clickListener?.invoke() }
        deleteButton.setOnClickListener { deleteListener?.invoke() }
    }

    override fun setImage(item: ScreenImageItem) {
        val aspectRatio = item.width.toFloat() / item.height.toFloat()
        val width = image.layoutParams.height * aspectRatio
        image.layoutParams.width = width.toInt()

        image.fetch(item.preview.toString()) {
            centerCrop()
            onLoading { imageView ->
                imageView.setImageDrawable(null)
            }
        }
    }

    override fun setRemote(remote: Boolean) {
        upload.isVisible = !remote
    }

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    override fun setOnDeleteListener(listener: (() -> Unit)?) {
        this.deleteListener = listener
    }

    override fun onUnbind() {
        this.clickListener = null
        this.deleteListener = null
    }

}
