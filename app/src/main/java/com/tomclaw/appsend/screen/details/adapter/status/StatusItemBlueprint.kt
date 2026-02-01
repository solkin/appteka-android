package com.tomclaw.appsend.screen.details.adapter.status

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class StatusItemBlueprint(override val presenter: ItemPresenter<StatusItemView, StatusItem>) :
    ItemBlueprint<StatusItemView, StatusItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.details_block_status,
        creator = { _, view -> StatusItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is StatusItem

}
