package com.tomclaw.appsend.screen.chat.adapter.incoming

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.chat.adapter.ItemListener

class IncomingMsgItemPresenter(
    private val listener: ItemListener
) : ItemPresenter<IncomingMsgItemView, IncomingMsgItem> {

    override fun bindView(view: IncomingMsgItemView, item: IncomingMsgItem, position: Int) {
        listener.onLoadMore(item.msgId)

        view.setUserIcon(item.userIcon)
        view.setTime(item.time)
        view.setDate(item.date)
        view.setText(item.text)

        view.setOnClickListener { listener.onItemClick(item) }
    }

}
