package com.tomclaw.appsend.screen.details.adapter.abi

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class AbiItemBlueprint(override val presenter: ItemPresenter<AbiItemView, AbiItem>) :
    ItemBlueprint<AbiItemView, AbiItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.details_block_abi,
        creator = { _, view -> AbiItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is AbiItem

}
