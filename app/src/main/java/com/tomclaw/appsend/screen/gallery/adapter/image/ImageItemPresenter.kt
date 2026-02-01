package com.tomclaw.appsend.screen.gallery.adapter.image

import com.tomclaw.appsend.util.adapter.ItemPresenter

class ImageItemPresenter : ItemPresenter<ImageItemView, ImageItem> {

    override fun bindView(view: ImageItemView, item: ImageItem, position: Int) {
        view.setUri(item.uri)
    }

}
