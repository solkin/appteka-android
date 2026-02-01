package com.tomclaw.appsend.screen.topics.adapter.topic

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class TopicItemBlueprint(override val presenter: ItemPresenter<TopicItemView, TopicItem>) :
    ItemBlueprint<TopicItemView, TopicItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.topic_item,
        creator = { _, view -> TopicItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is TopicItem

}
