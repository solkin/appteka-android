package com.tomclaw.appsend.screen.post.adapter.image

import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.imageloader.util.centerCrop
import com.tomclaw.imageloader.util.fetch

interface ImageItemView : ItemView {

    fun setImage(item: ImageItem)

    fun setRemote(remote: Boolean)

    fun setOnClickListener(listener: (() -> Unit)?)

    fun setOnDeleteListener(listener: (() -> Unit)?)

}

class ImageItemViewHolder(view: View) : BaseViewHolder(view), ImageItemView {

    private val card: View = view.findViewById(R.id.image_card)
    private val image: ImageView = view.findViewById(R.id.image)
    private val upload: View = view.findViewById(R.id.upload)
    private val deleteButton: View = view.findViewById(R.id.delete_button)

    private var clickListener: (() -> Unit)? = null
    private var deleteListener: (() -> Unit)? = null

    init {
        card.setOnClickListener { clickListener?.invoke() }
        deleteButton.setOnClickListener { deleteListener?.invoke() }
    }

    override fun setImage(item: ImageItem) {
        val aspectRatio = item.width.toFloat() / item.height.toFloat()
        val width = image.layoutParams.height * aspectRatio
        image.layoutParams.width = width.toInt()

        image.fetch(item.preview.toString()) {
            centerCrop()
            placeholder = {
                with(it.get()) {
                    setImageDrawable(null)
                }
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
