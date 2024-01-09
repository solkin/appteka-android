package com.tomclaw.appsend.screen.gallery.adapter.image

import android.net.Uri
import android.view.View
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.ZoomImageView
import com.tomclaw.imageloader.util.fetch

interface ImageItemView : ItemView {

    fun setUri(uri: Uri)

}

class ImageItemViewHolder(view: View) : BaseViewHolder(view), ImageItemView {

    private val image: ZoomImageView = view.findViewById(R.id.gallery_image)

    init {
        image.disallowPagingWhenZoomed = true
    }

    override fun setUri(uri: Uri) {
        image.fetch(uri) {
            placeholder = {
                with(it.get()) {
                    setImageDrawable(null)
                }
            }
            success = { viewHolder, result ->
                with(viewHolder.get()) {
                    setImageDrawable(result.getDrawable())
                }
            }
            error = {
                with(it.get()) {
                    setImageDrawable(null)
                }
            }
        }
    }

}
