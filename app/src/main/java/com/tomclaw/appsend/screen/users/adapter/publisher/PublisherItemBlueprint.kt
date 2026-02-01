package com.tomclaw.appsend.screen.users.adapter.publisher

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class PublisherItemBlueprint(override val presenter: ItemPresenter<PublisherItemView, PublisherItem>) :
    ItemBlueprint<PublisherItemView, PublisherItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.publisher_item,
        creator = { _, view -> PublisherItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is PublisherItem

}
