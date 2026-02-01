package com.tomclaw.appsend.screen.users.adapter.subscriber

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class SubscriberItemBlueprint(override val presenter: ItemPresenter<SubscriberItemView, SubscriberItem>) :
    ItemBlueprint<SubscriberItemView, SubscriberItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.subscriber_item,
        creator = { _, view -> SubscriberItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is SubscriberItem

}
