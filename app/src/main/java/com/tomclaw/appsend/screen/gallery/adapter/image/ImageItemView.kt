package com.tomclaw.appsend.screen.gallery.adapter.image

import android.net.Uri
import android.view.View
import android.widget.ImageView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.imageloader.util.centerInside
import com.tomclaw.imageloader.util.fetch

interface ImageItemView : ItemView {

    fun setUri(uri: Uri)

}

class ImageItemViewHolder(view: View) : BaseViewHolder(view), ImageItemView {

    private val image: ImageView = view.findViewById(R.id.gallery_image)

    override fun setUri(uri: Uri) {
        image.fetch(uri) {
            centerInside()
            placeholder = {
                with(it.get()) {
                    setImageDrawable(null)
                }
            }
        }
    }

}
