package com.tomclaw.appsend.screen.upload.adapter.description

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class DescriptionItemBlueprint(override val presenter: ItemPresenter<DescriptionItemView, DescriptionItem>) :
    ItemBlueprint<DescriptionItemView, DescriptionItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.upload_block_description,
        creator = { _, view -> DescriptionItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is DescriptionItem

}
