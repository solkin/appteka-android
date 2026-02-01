package com.tomclaw.appsend.screen.chat.adapter.outgoing

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class OutgoingMsgItemBlueprint(override val presenter: ItemPresenter<OutgoingMsgItemView, OutgoingMsgItem>) :
    ItemBlueprint<OutgoingMsgItemView, OutgoingMsgItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.chat_item_out_text,
        creator = { _, view -> OutgoingMsgItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is OutgoingMsgItem

}
