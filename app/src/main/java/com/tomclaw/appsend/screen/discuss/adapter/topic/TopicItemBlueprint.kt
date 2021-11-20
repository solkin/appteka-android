package com.tomclaw.appsend.screen.discuss.adapter.topic

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class TopicItemBlueprint(override val presenter: ItemPresenter<TopicItemView, TopicItem>) :
    ItemBlueprint<TopicItemView, TopicItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.topic_item,
        creator = { _, view -> TopicItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is TopicItem

}
