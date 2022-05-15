package com.tomclaw.appsend.screen.details.adapter.header

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class HeaderItemBlueprint(override val presenter: ItemPresenter<HeaderItemView, HeaderItem>) :
    ItemBlueprint<HeaderItemView, HeaderItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.details_block_header,
        creator = { _, view -> HeaderItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is HeaderItem

}
