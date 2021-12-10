package com.tomclaw.appsend.screen.chat.adapter.outgoing

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.chat.adapter.ItemListener

class OutgoingMsgItemPresenter(
    private val listener: ItemListener
) : ItemPresenter<OutgoingMsgItemView, OutgoingMsgItem> {

    override fun bindView(view: OutgoingMsgItemView, item: OutgoingMsgItem, position: Int) {
        listener.onLoadMore(item.msgId)

        view.setUserIcon(item.userIcon)
        view.setTime(item.time)
        view.setDate(item.date)
        view.setText(item.text)
        if (item.msgId == 0) {
            if (item.sent) {
                view.sentState()
            } else {
                view.sendingState()
            }
        } else {
            view.deliveredState()
        }

        view.setOnClickListener { listener.onItemClick(item) }
    }

}
