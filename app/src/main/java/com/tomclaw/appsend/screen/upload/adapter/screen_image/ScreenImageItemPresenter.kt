package com.tomclaw.appsend.screen.upload.adapter.screen_image

import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.screen.upload.adapter.ItemListener

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
