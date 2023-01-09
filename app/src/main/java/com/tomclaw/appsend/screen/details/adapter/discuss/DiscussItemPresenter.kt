package com.tomclaw.appsend.screen.details.adapter.discuss

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.details.adapter.ItemListener

class DiscussItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<DiscussItemView, DiscussItem> {

    override fun bindView(view: DiscussItemView, item: DiscussItem, position: Int) {
        item.msgCount?.let { view.showMsgCount(it) } ?: view.showNoMsgIndicator()
        view.setOnDiscussClickListener {
            listener.onDiscussClick()
        }
    }

}
