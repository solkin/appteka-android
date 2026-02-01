package com.tomclaw.appsend.screen.chat.adapter.incoming

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class IncomingMsgItemBlueprint(override val presenter: ItemPresenter<IncomingMsgItemView, IncomingMsgItem>) :
    ItemBlueprint<IncomingMsgItemView, IncomingMsgItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.chat_item_inc_text,
        creator = { _, view -> IncomingMsgItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is IncomingMsgItem

}
