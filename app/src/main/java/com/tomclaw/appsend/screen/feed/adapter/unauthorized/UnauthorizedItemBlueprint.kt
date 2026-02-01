package com.tomclaw.appsend.screen.feed.adapter.unauthorized

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class UnauthorizedItemBlueprint(override val presenter: ItemPresenter<UnauthorizedItemView, UnauthorizedItem>) :
    ItemBlueprint<UnauthorizedItemView, UnauthorizedItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.feed_block_unauthorized,
        creator = { _, view -> UnauthorizedItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is UnauthorizedItem

}
