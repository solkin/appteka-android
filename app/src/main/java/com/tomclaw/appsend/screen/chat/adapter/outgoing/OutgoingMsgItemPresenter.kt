package com.tomclaw.appsend.screen.chat.adapter.outgoing

import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.screen.chat.adapter.ItemListener

class OutgoingMsgItemPresenter(
    private val listener: ItemListener
) : ItemPresenter<OutgoingMsgItemView, OutgoingMsgItem> {

    override fun bindView(view: OutgoingMsgItemView, item: OutgoingMsgItem, position: Int) {
        item.author.icon?.let(view::setUserIcon)
        view.setUserBadge(item.author.primaryBadge)
        view.setTime(item.time)
        view.setDate(item.date)
        view.setText(item.text)
        view.setAttachments(item.attachments)
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
        view.setOnAvatarClickListener { listener.onAvatarClick(item.author.id) }
        view.setOnAttachmentClickListener { index -> listener.onAttachmentClick(item, index) }
    }

}
