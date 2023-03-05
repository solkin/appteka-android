package com.tomclaw.appsend.screen.upload.adapter.exclusive

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class ExclusiveItemBlueprint(override val presenter: ItemPresenter<ExclusiveItemView, ExclusiveItem>) :
    ItemBlueprint<ExclusiveItemView, ExclusiveItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.upload_block_exclusive,
        creator = { _, view -> ExclusiveItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is ExclusiveItem

}
