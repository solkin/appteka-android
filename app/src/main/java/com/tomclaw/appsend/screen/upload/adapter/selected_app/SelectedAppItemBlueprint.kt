package com.tomclaw.appsend.screen.upload.adapter.selected_app

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class SelectedAppItemBlueprint(override val presenter: ItemPresenter<SelectedAppItemView, SelectedAppItem>) :
    ItemBlueprint<SelectedAppItemView, SelectedAppItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.upload_block_selected_app,
        creator = { _, view -> SelectedAppItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is SelectedAppItem

}
