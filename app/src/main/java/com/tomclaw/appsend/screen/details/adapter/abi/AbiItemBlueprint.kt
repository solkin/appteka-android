package com.tomclaw.appsend.screen.details.adapter.abi

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class AbiItemBlueprint(override val presenter: ItemPresenter<AbiItemView, AbiItem>) :
    ItemBlueprint<AbiItemView, AbiItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.details_block_abi,
        creator = { _, view -> AbiItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is AbiItem

}
