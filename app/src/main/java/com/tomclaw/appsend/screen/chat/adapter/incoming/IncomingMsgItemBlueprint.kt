package com.tomclaw.appsend.screen.chat.adapter.incoming

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class IncomingMsgItemBlueprint(override val presenter: ItemPresenter<IncomingMsgItemView, IncomingMsgItem>) :
    ItemBlueprint<IncomingMsgItemView, IncomingMsgItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.chat_item_inc_text,
        creator = { _, view -> IncomingMsgItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is IncomingMsgItem

}
