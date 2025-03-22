package com.tomclaw.appsend.screen.post.adapter.image

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.post.adapter.ItemListener

class ImageItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<ImageItemView, ImageItem> {

    override fun bindView(view: ImageItemView, item: ImageItem, position: Int) {
        with(view) {
            setImage(item)
            setRemote(item.remote)
            setOnClickListener { listener.onImageClick(item) }
            setOnDeleteListener { listener.onImageDelete(item) }
        }
    }

}
