package com.tomclaw.appsend.screen.chat.adapter.outgoing

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.chat.adapter.ItemListener

class OutgoingMsgItemPresenter(
    private val listener: ItemListener
) : ItemPresenter<OutgoingMsgItemView, OutgoingMsgItem> {

    override fun bindView(view: OutgoingMsgItemView, item: OutgoingMsgItem, position: Int) {
        with(item) {
            listener.onLoadMore(this)
        }

        view.setOnClickListener { listener.onItemClick(item) }
    }

}
