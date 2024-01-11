package com.tomclaw.appsend.screen.upload.adapter.screenshots

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.upload.adapter.ItemListener

class ScreenshotsItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<ScreenshotsItemView, ScreenshotsItem> {

    override fun bindView(view: ScreenshotsItemView, item: ScreenshotsItem, position: Int) {
        view.setItems(item.items)
        view.setOnClickListener { clicked ->
            listener.onScreenshotClick(
                item.items,
                item.items.indexOf(clicked)
            )
        }
    }

}
