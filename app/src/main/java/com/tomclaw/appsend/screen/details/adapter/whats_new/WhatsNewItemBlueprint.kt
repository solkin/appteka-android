package com.tomclaw.appsend.screen.details.adapter.whats_new

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class WhatsNewItemBlueprint(override val presenter: ItemPresenter<WhatsNewItemView, WhatsNewItem>) :
    ItemBlueprint<WhatsNewItemView, WhatsNewItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.details_block_whats_new,
        creator = { _, view -> WhatsNewItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is WhatsNewItem

}
