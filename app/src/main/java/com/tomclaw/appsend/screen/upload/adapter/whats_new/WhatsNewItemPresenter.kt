package com.tomclaw.appsend.screen.upload.adapter.whats_new

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.upload.adapter.ItemListener

class WhatsNewItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<WhatsNewItemView, WhatsNewItem> {

    override fun bindView(view: WhatsNewItemView, item: WhatsNewItem, position: Int) {
        with(view) {
            setText(item.text)
            setOnTextChangedListener { listener.onWhatsNewChanged(it) }
        }
    }

}
