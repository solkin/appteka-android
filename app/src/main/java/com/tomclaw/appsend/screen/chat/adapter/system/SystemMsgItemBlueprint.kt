package com.tomclaw.appsend.screen.chat.adapter.system

import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder

class SystemMsgItemBlueprint(override val presenter: ItemPresenter<SystemMsgItemView, SystemMsgItem>) :
    ItemBlueprint<SystemMsgItemView, SystemMsgItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.chat_item_system,
        creator = { _, view -> SystemMsgItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is SystemMsgItem

}
