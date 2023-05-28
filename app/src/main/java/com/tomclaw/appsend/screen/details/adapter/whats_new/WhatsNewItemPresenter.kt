package com.tomclaw.appsend.screen.details.adapter.whats_new

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.details.adapter.ItemListener

class WhatsNewItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<WhatsNewItemView, WhatsNewItem> {

    override fun bindView(view: WhatsNewItemView, item: WhatsNewItem, position: Int) {
        view.setText(item.text)
    }

}
