package com.tomclaw.appsend.screen.post.adapter.screen_image

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.post.adapter.ItemListener

class ScreenImageItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<ScreenImageItemView, ScreenImageItem> {

    override fun bindView(view: ScreenImageItemView, item: ScreenImageItem, position: Int) {
        with(view) {
            setImage(item)
            setRemote(item.remote)
            setOnClickListener { listener.onScreenshotClick(item) }
            setOnDeleteListener { listener.onScreenshotDelete(item) }
        }
    }

}
