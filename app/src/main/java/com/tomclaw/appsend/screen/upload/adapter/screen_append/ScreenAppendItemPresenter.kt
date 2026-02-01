package com.tomclaw.appsend.screen.upload.adapter.screen_append

import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.screen.upload.adapter.ItemListener

class ScreenAppendItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<ScreenAppendItemView, ScreenAppendItem> {

    override fun bindView(view: ScreenAppendItemView, item: ScreenAppendItem, position: Int) {
        with(view) {
            setOnClickListener { listener.onScreenAppendClick() }
        }
    }

}
