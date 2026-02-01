package com.tomclaw.appsend.screen.profile.adapter.feed

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class FeedItemBlueprint(override val presenter: ItemPresenter<FeedItemView, FeedItem>) :
    ItemBlueprint<FeedItemView, FeedItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.profile_block_feed,
        creator = { _, view -> FeedItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is FeedItem

}
