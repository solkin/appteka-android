package com.tomclaw.appsend.screen.upload.adapter.screen_image

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.upload.adapter.ItemListener

class ScreenImageItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<ScreenImageItemView, ScreenImageItem> {

    override fun bindView(view: ScreenImageItemView, item: ScreenImageItem, position: Int) {
        with(view) {
            setImage(item)
            setOnClickListener { listener.onScreenshotClick(item) }
        }
    }

}
