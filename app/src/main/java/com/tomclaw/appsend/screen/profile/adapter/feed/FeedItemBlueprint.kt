package com.tomclaw.appsend.screen.profile.adapter.feed

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class FeedItemBlueprint(override val presenter: ItemPresenter<FeedItemView, FeedItem>) :
    ItemBlueprint<FeedItemView, FeedItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.profile_block_feed,
        creator = { _, view -> FeedItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is FeedItem

}
