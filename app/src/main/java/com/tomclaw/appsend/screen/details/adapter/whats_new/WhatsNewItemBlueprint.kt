package com.tomclaw.appsend.screen.details.adapter.whats_new

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class WhatsNewItemBlueprint(override val presenter: ItemPresenter<WhatsNewItemView, WhatsNewItem>) :
    ItemBlueprint<WhatsNewItemView, WhatsNewItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.details_block_whats_new,
        creator = { _, view -> WhatsNewItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is WhatsNewItem

}
