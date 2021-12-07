package com.tomclaw.appsend.screen.chat.adapter.msg

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.chat.adapter.ItemListener

class IncomingMsgItemPresenter(
    private val listener: ItemListener
) : ItemPresenter<MsgItemView, IncomingMsgItem> {

    override fun bindView(view: MsgItemView, item: IncomingMsgItem, position: Int) {
        with(item) {
            listener.onLoadMore(this)
        }

        view.setOnClickListener { listener.onItemClick(item) }
    }

}
