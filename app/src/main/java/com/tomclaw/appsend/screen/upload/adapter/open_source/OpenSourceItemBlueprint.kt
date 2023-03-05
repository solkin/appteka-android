package com.tomclaw.appsend.screen.upload.adapter.open_source

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class OpenSourceItemBlueprint(override val presenter: ItemPresenter<OpenSourceItemView, OpenSourceItem>) :
    ItemBlueprint<OpenSourceItemView, OpenSourceItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.upload_block_open_source,
        creator = { _, view -> OpenSourceItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is OpenSourceItem

}
