package com.tomclaw.appsend.screen.details.adapter.discuss

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class DiscussItemBlueprint(override val presenter: ItemPresenter<DiscussItemView, DiscussItem>) :
    ItemBlueprint<DiscussItemView, DiscussItem> {

    override val viewHolderProvider =
        ViewHolderBuilder.ViewHolderProvider(
            layoutId = R.layout.details_block_discuss,
            creator = { _, view -> DiscussItemViewHolder(view) }
        )

    override fun isRelevantItem(item: Item) = item is DiscussItem

}
