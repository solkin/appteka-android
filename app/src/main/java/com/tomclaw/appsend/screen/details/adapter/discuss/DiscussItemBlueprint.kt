package com.tomclaw.appsend.screen.details.adapter.discuss

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class DiscussItemBlueprint(override val presenter: ItemPresenter<DiscussItemView, DiscussItem>) :
    ItemBlueprint<DiscussItemView, DiscussItem> {

    override val viewHolderProvider =
        ViewHolderBuilder.ViewHolderProvider(
            layoutId = R.layout.details_block_discuss,
            creator = { _, view -> DiscussItemViewHolder(view) }
        )

    override fun isRelevantItem(item: Item) = item is DiscussItem

}
