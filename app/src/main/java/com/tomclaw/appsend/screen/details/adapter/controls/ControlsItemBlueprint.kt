package com.tomclaw.appsend.screen.details.adapter.controls

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class ControlsItemBlueprint(override val presenter: ItemPresenter<ControlsItemView, ControlsItem>) :
    ItemBlueprint<ControlsItemView, ControlsItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.details_block_controls,
        creator = { _, view -> ControlsItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is ControlsItem

}
