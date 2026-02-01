package com.tomclaw.appsend.screen.details.adapter.screenshot

import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.screen.details.adapter.screenshots.ScreenshotItemListener

class ScreenshotItemPresenter(
    private val listener: ScreenshotItemListener,
) : ItemPresenter<ScreenshotItemView, ScreenshotItem> {

    override fun bindView(view: ScreenshotItemView, item: ScreenshotItem, position: Int) {
        with(view) {
            setImage(item)
            setOnClickListener { listener.onScreenshotClick(position) }
        }
    }

}
