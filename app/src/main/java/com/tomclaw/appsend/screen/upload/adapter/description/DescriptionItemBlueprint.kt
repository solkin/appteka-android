package com.tomclaw.appsend.screen.upload.adapter.description

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class DescriptionItemBlueprint(override val presenter: ItemPresenter<DescriptionItemView, DescriptionItem>) :
    ItemBlueprint<DescriptionItemView, DescriptionItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.upload_block_description,
        creator = { _, view -> DescriptionItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is DescriptionItem

}
