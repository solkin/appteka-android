package com.tomclaw.appsend.screen.upload.adapter.origin

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class OriginItemBlueprint(override val presenter: ItemPresenter<OriginItemView, OriginItem>) :
    ItemBlueprint<OriginItemView, OriginItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.upload_block_origin,
        creator = { _, view -> OriginItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is OriginItem

}
