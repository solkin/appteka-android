package com.tomclaw.appsend.screen.users.adapter.publisher

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class PublisherItemBlueprint(override val presenter: ItemPresenter<PublisherItemView, PublisherItem>) :
    ItemBlueprint<PublisherItemView, PublisherItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.publisher_item,
        creator = { _, view -> PublisherItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is PublisherItem

}
