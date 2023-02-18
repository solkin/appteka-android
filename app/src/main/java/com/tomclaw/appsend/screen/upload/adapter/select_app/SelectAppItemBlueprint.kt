package com.tomclaw.appsend.screen.upload.adapter.select_app

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class SelectAppItemBlueprint(override val presenter: ItemPresenter<SelectAppItemView, SelectAppItem>) :
    ItemBlueprint<SelectAppItemView, SelectAppItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.upload_block_select_app,
        creator = { _, view -> SelectAppItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is SelectAppItem

}
