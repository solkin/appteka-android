package com.tomclaw.appsend.screen.profile.adapter.placeholder

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class PlaceholderItemBlueprint(override val presenter: ItemPresenter<PlaceholderItemView, PlaceholderItem>) :
    ItemBlueprint<PlaceholderItemView, PlaceholderItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.profile_block_placeholder,
        creator = { _, view -> PlaceholderItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is PlaceholderItem

}
