package com.tomclaw.appsend.screen.upload.adapter.selected_app

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class SelectedAppItemBlueprint(override val presenter: ItemPresenter<SelectedAppItemView, SelectedAppItem>) :
    ItemBlueprint<SelectedAppItemView, SelectedAppItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.upload_block_selected_app,
        creator = { _, view -> SelectedAppItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is SelectedAppItem

}
