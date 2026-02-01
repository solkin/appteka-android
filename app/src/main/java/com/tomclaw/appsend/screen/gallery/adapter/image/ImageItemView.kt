package com.tomclaw.appsend.screen.gallery.adapter.image

import android.net.Uri
import android.view.View
import android.widget.ImageView
import com.tomclaw.appsend.util.adapter.BaseItemViewHolder
import com.tomclaw.appsend.util.adapter.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.view.ZoomImageView
import com.tomclaw.imageloader.util.fetch

interface ImageItemView : ItemView {

    fun setUri(uri: Uri)

}

class ImageItemViewHolder(view: View) : BaseItemViewHolder(view), ImageItemView {

    private val image: ZoomImageView = view.findViewById(R.id.gallery_image)

    init {
        image.disallowPagingWhenZoomed = true
    }

    override fun setUri(uri: Uri) {
        image.setImageResource(R.drawable.ic_cloud)
        image.scaleType = ImageView.ScaleType.CENTER_INSIDE
        image.fetch(uri) {
            onLoading { imageView ->
                imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                imageView.setImageResource(R.drawable.ic_cloud)
            }
            onSuccess { imageView, drawable ->
                imageView.scaleType = ImageView.ScaleType.MATRIX
                imageView.setImageDrawable(drawable)
            }
            onError { imageView, _ ->
                imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                imageView.setImageResource(R.drawable.ic_error)
            }
        }
    }

}
