package com.tomclaw.appsend.screen.details.adapter.controls

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class ControlsItemBlueprint(override val presenter: ItemPresenter<ControlsItemView, ControlsItem>) :
    ItemBlueprint<ControlsItemView, ControlsItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.details_block_controls,
        creator = { _, view -> ControlsItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is ControlsItem

}
