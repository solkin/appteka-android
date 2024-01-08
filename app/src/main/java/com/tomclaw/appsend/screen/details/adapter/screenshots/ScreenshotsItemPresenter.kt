package com.tomclaw.appsend.screen.details.adapter.screenshots

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.details.adapter.ItemListener

class ScreenshotsItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<ScreenshotsItemView, ScreenshotsItem> {

    override fun bindView(view: ScreenshotsItemView, item: ScreenshotsItem, position: Int) {
        view.setScreenshots(item.items)
        view.setOnClickListener { clicked -> listener.onScreenshotClick(item.items, item.items.indexOf(clicked)) }
    }

}
