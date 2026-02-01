package com.tomclaw.appsend.screen.upload.adapter.exclusive

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class ExclusiveItemBlueprint(override val presenter: ItemPresenter<ExclusiveItemView, ExclusiveItem>) :
    ItemBlueprint<ExclusiveItemView, ExclusiveItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.upload_block_exclusive,
        creator = { _, view -> ExclusiveItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is ExclusiveItem

}
