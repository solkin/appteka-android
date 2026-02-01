package com.tomclaw.appsend.screen.feed.adapter.subscribe

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.feed.adapter.ReactionsAdapter

class SubscribeItemBlueprint(
    override val presenter: ItemPresenter<SubscribeItemView, SubscribeItem>
) : ItemBlueprint<SubscribeItemView, SubscribeItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.feed_item_subscribe,
        creator = { _, view ->
            SubscribeItemViewHolder(view, ReactionsAdapter())
        }
    )

    override fun isRelevantItem(item: Item) = item is SubscribeItem

}
