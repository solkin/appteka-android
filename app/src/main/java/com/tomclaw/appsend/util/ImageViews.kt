package com.tomclaw.appsend.util

import android.net.Uri
import android.widget.ImageView
import com.tomclaw.imageloader.SimpleImageLoader.imageLoader
import com.tomclaw.imageloader.core.Handlers

fun ImageView.fetch(uri: Uri, params: Handlers<ImageView>.() -> Unit = {}) {
    val handlers = Handlers<ImageView>()
        .apply {
            successHandler { viewHolder, result ->
                viewHolder.get().setImageDrawable(result.getDrawable())
            }
        }
        .apply(params)
    val viewHolder = ImageViewHolder(this)
    context.imageLoader().load(viewHolder, uri.toString(), handlers)
}
