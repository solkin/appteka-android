package com.tomclaw.appsend.screen.chat.adapter.incoming

import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.screen.chat.adapter.ItemListener

class IncomingMsgItemPresenter(
    private val listener: ItemListener
) : ItemPresenter<IncomingMsgItemView, IncomingMsgItem> {

    override fun bindView(view: IncomingMsgItemView, item: IncomingMsgItem, position: Int) {
        listener.onLoadMore(item.msgId)

        view.setUserIcon(item.userIcon)
        view.setUserBadge(item.userBadge)
        view.setTime(item.time)
        view.setDate(item.date)
        view.setText(item.text)
        view.setAttachments(item.attachments)

        view.setOnClickListener { listener.onItemClick(item) }
        view.setOnAttachmentClickListener { index -> listener.onAttachmentClick(item, index) }
    }

}
