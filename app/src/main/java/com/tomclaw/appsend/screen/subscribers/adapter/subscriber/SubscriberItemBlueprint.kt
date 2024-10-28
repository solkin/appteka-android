package com.tomclaw.appsend.screen.subscribers.adapter.subscriber

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class SubscriberItemBlueprint(override val presenter: ItemPresenter<SubscriberItemView, SubscriberItem>) :
    ItemBlueprint<SubscriberItemView, SubscriberItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.subscriber_item,
        creator = { _, view -> SubscriberItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is SubscriberItem

}
