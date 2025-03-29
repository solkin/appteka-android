package com.tomclaw.appsend.screen.feed.adapter.unauthorized

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class UnauthorizedItemBlueprint(override val presenter: ItemPresenter<UnauthorizedItemView, UnauthorizedItem>) :
    ItemBlueprint<UnauthorizedItemView, UnauthorizedItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.feed_block_unauthorized,
        creator = { _, view -> UnauthorizedItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is UnauthorizedItem

}
