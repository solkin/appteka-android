package com.tomclaw.appsend.screen.profile.adapter.placeholder

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class PlaceholderItemBlueprint(override val presenter: ItemPresenter<PlaceholderItemView, PlaceholderItem>) :
    ItemBlueprint<PlaceholderItemView, PlaceholderItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.profile_block_placeholder,
        creator = { _, view -> PlaceholderItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is PlaceholderItem

}
