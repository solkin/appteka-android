package com.tomclaw.appsend.screen.feed.adapter.subscribe

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class SubscribeItemBlueprint(
    override val presenter: ItemPresenter<SubscribeItemView, SubscribeItem>,
) : ItemBlueprint<SubscribeItemView, SubscribeItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.feed_item_subscribe,
        creator = { _, view -> SubscribeItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is SubscribeItem

}
