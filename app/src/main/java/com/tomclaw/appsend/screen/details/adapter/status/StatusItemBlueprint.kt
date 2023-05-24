package com.tomclaw.appsend.screen.details.adapter.status

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class StatusItemBlueprint(override val presenter: ItemPresenter<StatusItemView, StatusItem>) :
    ItemBlueprint<StatusItemView, StatusItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.details_block_status,
        creator = { _, view -> StatusItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is StatusItem

}
